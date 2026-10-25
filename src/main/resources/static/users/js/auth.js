// Login ke backend (POST /api/auth/login). Mahasiswa: email. Admin: username (lihat catatan @Email di backend).
const HOME = { mahasiswa: 'pages/dashboard-mahasiswa.html', admin: 'admin/pages/dashboard.html' };

(function redirectIfLoggedIn() {
  try {
    const session = JSON.parse(sessionStorage.getItem('session'));
    if (session && HOME[session.role] && AuthService.isLoggedIn()) location.replace(HOME[session.role]);
  } catch (e) { /* sesi rusak, abaikan */ }
})();

const form = document.getElementById('loginForm');
const errorEl = document.getElementById('error');
const submitBtn = document.getElementById('submitBtn');
const passInput = document.getElementById('password');
const toggleBtn = document.getElementById('togglePass');

toggleBtn.addEventListener('click', () => {
  const show = passInput.type === 'password';
  passInput.type = show ? 'text' : 'password';
  toggleBtn.textContent = show ? 'Sembunyikan' : 'Tampilkan';
  toggleBtn.setAttribute('aria-pressed', String(show));
});

form.addEventListener('submit', async (e) => {
  e.preventDefault();
  errorEl.textContent = '';
  const username = form.username.value.trim();
  const password = form.password.value;
  if (!username || !password) { errorEl.textContent = 'Isi email/username dan kata sandi.'; return; }

  submitBtn.disabled = true;
  submitBtn.textContent = 'Memeriksa...';
  try {
    const { role } = await AuthService.login({ email: username, password });
    await API.startSession(username, role);
    location.href = HOME[role === 'ADMIN' ? 'admin' : 'mahasiswa'];
  } catch (err) {
    AuthService.clearSession();
    errorEl.textContent = API.errorMessage(err);
    submitBtn.disabled = false;
    submitBtn.textContent = 'Masuk';
  }
});
