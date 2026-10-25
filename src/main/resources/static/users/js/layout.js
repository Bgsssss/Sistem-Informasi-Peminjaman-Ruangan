// Mengisi sidebar dan header. Dipakai semua halaman setelah login.
const MENUS = {
  mahasiswa: [
    { key: 'dashboard', label: 'Dashboard', href: 'dashboard-mahasiswa.html' },
    { key: 'booking', label: 'Booking ruangan', href: 'booking.html' },
    { key: 'riwayat', label: 'Riwayat booking', href: 'riwayat.html' },
    { key: 'checkin', label: 'Check-in', href: 'checkin.html' },
    { key: 'profil', label: 'Profil', href: 'profil.html' }
  ],
  admin: [
    { key: 'dashboard', label: 'Dashboard', href: 'dashboard.html' },
    { key: 'ruangan', label: 'Kelola ruangan', href: 'kelola-ruangan.html' },
    { key: 'mahasiswa', label: 'Kelola mahasiswa', href: 'kelola-mahasiswa.html' },
    { key: 'booking', label: 'Kelola booking', href: 'kelola-booking.html' },
    { key: 'pelanggaran', label: 'Log pelanggaran', href: 'log-pelanggaran.html' },
    { key: 'laporan', label: 'Laporan', href: 'laporan.html' },
    { key: 'pengaturan', label: 'Pengaturan sistem', href: 'pengaturan.html' }
  ]
};

function mountShell(session, activeKey) {
  const nav = document.getElementById('sidebar');
  nav.innerHTML = '<div class="brand">Peminjaman Ruangan</div><nav></nav>';
  const list = nav.querySelector('nav');
  MENUS[session.role].forEach(item => {
    const a = document.createElement('a');
    a.href = item.href;
    a.textContent = item.label;
    if (item.key === activeKey) a.setAttribute('aria-current', 'page');
    list.appendChild(a);
  });

  const bar = document.getElementById('topbar');
  bar.innerHTML = '<div class="who"><strong></strong><span></span></div><button type="button" class="logout">Keluar</button>';
  bar.querySelector('strong').textContent = session.name;
  bar.querySelector('span').textContent = session.role === 'admin' ? 'Admin' : 'NIM ' + session.nim;
  bar.querySelector('.logout').addEventListener('click', () => API.logout());
}
