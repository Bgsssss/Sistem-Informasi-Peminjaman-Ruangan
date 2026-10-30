// Dasar path proyek dihitung dari lokasi file ini, jadi tidak bergantung nama folder (/users/, /, dst).
window.APP_CONFIG = Object.freeze({
  API_BASE_URL: "/api",
  REQUEST_TIMEOUT_MS: 15000,
  PAGES: Object.freeze({
    LOGIN: "/login",
    STUDENT_HOME: "/dashboard",
    ADMIN_HOME: "/admin/dashboard",
  }),
});
