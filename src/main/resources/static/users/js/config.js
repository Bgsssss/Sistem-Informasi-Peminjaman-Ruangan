// Dasar path proyek dihitung dari lokasi file ini, jadi tidak bergantung nama folder (/users/, /, dst).
const __ROOT = new URL("../", document.currentScript.src).pathname;
window.APP_CONFIG = Object.freeze({
  API_BASE_URL: "/api",
  REQUEST_TIMEOUT_MS: 15000,
  PAGES: Object.freeze({
    LOGIN: __ROOT + "index.html",
    STUDENT_HOME: __ROOT + "pages/dashboard-mahasiswa.html",
    ADMIN_HOME: __ROOT + "admin/pages/dashboard.html",
  }),
});
