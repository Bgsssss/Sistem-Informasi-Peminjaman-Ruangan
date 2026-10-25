// Satu-satunya tempat fetch(). Butuh: config.js. (AuthService dipanggil saat runtime.)
(() => {
  const BASE_URL = window.APP_CONFIG?.API_BASE_URL ?? "/api";
  const TIMEOUT_MS = window.APP_CONFIG?.REQUEST_TIMEOUT_MS ?? 15000;

  class ApiError extends Error {
    constructor(message, { status = 0, code = "UNKNOWN_ERROR", payload = null } = {}) {
      super(message);
      this.name = "ApiError";
      this.status = status;
      this.code = code;
      this.payload = payload;
    }
  }

  const isAuthPath = (p) => p.startsWith("/auth/");

  // --- refresh token: satu refresh bersama untuk request yang gagal bersamaan ---
  let refreshPromise = null;
  function refreshAccessToken() {
    const refreshToken = window.AuthService?.getRefreshToken();
    if (!refreshToken) return Promise.resolve(false);
    if (!refreshPromise) {
      refreshPromise = (async () => {
        try {
          const res = await fetch(`${BASE_URL}/auth/refresh`, {
            method: "POST",
            headers: { Accept: "application/json", "Content-Type": "application/json" },
            // RefreshRequest tidak punya @JsonProperty -> camelCase.
            body: JSON.stringify({ refreshToken }),
          });
          if (!res.ok) return false;
          const body = await res.json();
          const data = body && "success" in body ? body.data : body;
          if (!data?.access_token) return false;
          window.AuthService.setTokens(data);
          return true;
        } catch {
          return false;
        } finally {
          refreshPromise = null;
        }
      })();
    }
    return refreshPromise;
  }

  async function request(path, options = {}, retried = false) {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), TIMEOUT_MS);

    const headers = new Headers(options.headers || {});
    headers.set("Accept", "application/json");
    if (options.body != null && !(options.body instanceof FormData) && !headers.has("Content-Type")) {
      headers.set("Content-Type", "application/json");
    }
    const token = window.AuthService?.getAccessToken();
    if (token && !isAuthPath(path)) {
      headers.set("Authorization", `${window.AuthService.getTokenType()} ${token}`);
    }

    try {
      const response = await fetch(`${BASE_URL}${path}`, { ...options, headers, signal: controller.signal });

      const ct = response.headers.get("content-type") || "";
      const payload = ct.includes("application/json") ? await response.json() : await response.text();

      if (!response.ok) {
        // Access token kedaluwarsa -> refresh sekali, ulangi request sekali.
        if (response.status === 401 && !retried && !isAuthPath(path)) {
          if (await refreshAccessToken()) return request(path, options, true);
          window.AuthService?.expire();
        }
        const message =
          payload && typeof payload === "object"
            ? payload.message || `Permintaan gagal (${response.status})`
            : `Permintaan gagal (${response.status})`;
        throw new ApiError(message, {
          status: response.status,
          code: (payload && payload.code) || `HTTP_${response.status}`,
          payload,
        });
      }

      if (response.status === 204 || payload === "") return null;
      // Sukses dibungkus ApiResponse -> kembalikan data. Response login/refresh tidak dibungkus.
      if (payload && typeof payload === "object" && "success" in payload) {
        if (payload.success === false) {
          throw new ApiError(payload.message || "Permintaan gagal.", { status: response.status, code: payload.code || "API_ERROR", payload });
        }
        return payload.data ?? null;
      }
      return payload;
    } catch (error) {
      if (error instanceof ApiError) throw error;
      if (error.name === "AbortError") throw new ApiError("Permintaan terlalu lama. Coba lagi.", { code: "TIMEOUT" });
      if (error instanceof TypeError) {
        throw new ApiError("Tidak dapat terhubung ke server. Periksa koneksi lalu coba lagi.", { code: "NETWORK_ERROR" });
      }
      throw error;
    } finally {
      clearTimeout(timeoutId);
    }
  }

  const withBody = (method) => (path, body, options = {}) =>
    request(path, { ...options, method, ...(body === undefined ? {} : { body: JSON.stringify(body) }) });

  window.ApiClient = Object.freeze({
    get: (path, options = {}) => request(path, { ...options, method: "GET" }),
    post: withBody("POST"),
    put: withBody("PUT"),
    patch: withBody("PATCH"),
    delete: (path, options = {}) => request(path, { ...options, method: "DELETE" }),
    ApiError,
  });
})();
