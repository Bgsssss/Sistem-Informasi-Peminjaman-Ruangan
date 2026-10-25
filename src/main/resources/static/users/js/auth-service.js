// Login, token, role, guard halaman. Butuh: config.js, api-client.js.
(() => {
  const K = { access: "access_token", refresh: "refresh_token", type: "token_type", role: "role" };
  const P = () => window.APP_CONFIG.PAGES;

  const AuthService = {
    getAccessToken: () => sessionStorage.getItem(K.access),
    getRefreshToken: () => sessionStorage.getItem(K.refresh),
    getTokenType: () => sessionStorage.getItem(K.type) || "Bearer",
    getRole: () => sessionStorage.getItem(K.role), // "STUDENT" | "ADMIN" — hanya untuk UX
    isLoggedIn: () => !!sessionStorage.getItem(K.access),

    setTokens(r) {
      if (r.access_token) sessionStorage.setItem(K.access, r.access_token);
      if (r.refresh_token) sessionStorage.setItem(K.refresh, r.refresh_token);
      if (r.token_type) sessionStorage.setItem(K.type, r.token_type);
    },

    clearSession() {
      Object.values(K).forEach((k) => sessionStorage.removeItem(k));
      sessionStorage.removeItem("session");
    },

    // Dipanggil ApiClient saat refresh gagal.
    expire() {
      this.clearSession();
      if (!location.pathname.endsWith(P().LOGIN)) location.assign(P().LOGIN);
    },

    // Backend login memakai { email, password } (AuthenticationRequest).
    async login({ email, password }) {
      let r;
      try {
        r = await ApiClient.post("/auth/login", { email, password });
      } catch (e) {
        if ([400, 401, 403].includes(e.status) && !["ACCOUNT_INACTIVE"].includes(e.code)) {
          throw new ApiClient.ApiError("Email atau password salah.", { status: e.status, code: "BAD_CREDENTIALS" });
        }
        throw e;
      }
      if (!r?.access_token || !r?.refresh_token) {
        throw new Error("Response login tidak berisi token yang diperlukan.");
      }
      this.setTokens(r);
      try {
        const role = await this.detectRole();
        sessionStorage.setItem(K.role, role);
        return { role };
      } catch (e) {
        this.clearSession();
        throw e;
      }
    },

    // Response login tidak memuat role. /api/profile hanya untuk STUDENT:
    // 200 => STUDENT; 403 tanpa kode bisnis (ditolak @PreAuthorize) => ADMIN.
    async detectRole() {
      try {
        await ApiClient.get("/profile");
        return "STUDENT";
      } catch (e) {
        if (e.status === 403 && (e.code === "ACCESS_DENIED" || e.code.startsWith("HTTP_"))) return "ADMIN";
        throw e; // mis. ACCOUNT_INACTIVE tetap tampil sebagai error
      }
    },

    homeFor: (role) => (role === "ADMIN" ? P().ADMIN_HOME : P().STUDENT_HOME),

    // Panggil di awal halaman terlindungi: AuthService.requireRole("STUDENT")
    // Ini hanya UX; keamanan sebenarnya tetap di backend.
    requireRole(role) {
      if (!this.isLoggedIn()) return this.expire(), false;
      if (role && this.getRole() !== role) {
        location.assign(this.homeFor(this.getRole()));
        return false;
      }
      return true;
    },

    // Belum ada endpoint logout/revoke di backend -> hanya hapus sesi di browser.
    logout() {
      this.clearSession();
      location.assign(P().LOGIN);
    },
  };

  window.AuthService = Object.freeze(AuthService);
})();
