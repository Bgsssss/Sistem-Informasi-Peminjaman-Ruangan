// Mengisi sidebar dan header. Dipakai semua halaman setelah login.
const MENUS = {
  mahasiswa: [
    { key: 'dashboard', label: 'Dashboard', href: '/dashboard' },
    { key: 'booking', label: 'Booking ruangan', href: '/booking' },
    { key: 'riwayat', label: 'Riwayat booking', href: '/history' },
    { key: 'checkin', label: 'Check-in', href: '/check-in' },
    { key: 'profil', label: 'Profil', href: '/profile' }
  ],
  admin: [
    { key: 'dashboard', label: 'Dashboard', href: '/admin/dashboard' },
    { key: 'ruangan', label: 'Kelola ruangan', href: '/admin/rooms' },
    { key: 'mahasiswa', label: 'Kelola mahasiswa', href: '/admin/students' },
    { key: 'booking', label: 'Kelola booking', href: '/admin/bookings' },
    { key: 'pelanggaran', label: 'Log pelanggaran', href: '/admin/violations' },
    { key: 'laporan', label: 'Laporan', href: '/admin/reports' },
    { key: 'pengaturan', label: 'Pengaturan sistem', href: '/admin/settings' }
  ]
};

function mountShell(session, activeKey) {
  const nav = document.getElementById('sidebar');
  nav.innerHTML =
    '<div class="sidebar-head">' +
      '<div class="brand">Peminjaman Ruangan</div>' +
      '<button type="button" class="burger" aria-label="Buka menu" aria-expanded="false" aria-controls="menu">' +
        '<span></span><span></span><span></span>' +
      '</button>' +
    '</div><nav id="menu"></nav>';
  const list = nav.querySelector('nav');
  MENUS[session.role].forEach(item => {
    const a = document.createElement('a');
    a.href = item.href;
    a.textContent = item.label;
    if (item.key === activeKey) a.setAttribute('aria-current', 'page');
    list.appendChild(a);
  });

  // Burger menu (tampil di HP)
  const burger = nav.querySelector('.burger');
  const setOpen = open => {
    nav.classList.toggle('open', open);
    burger.setAttribute('aria-expanded', String(open));
    burger.setAttribute('aria-label', open ? 'Tutup menu' : 'Buka menu');
  };
  burger.addEventListener('click', () => setOpen(!nav.classList.contains('open')));
  document.addEventListener('keydown', e => { if (e.key === 'Escape') setOpen(false); });

  document.addEventListener('keydown', e => { if (e.key === 'Escape') setOpen(false); });

  const bar = document.getElementById('topbar');
    bar.innerHTML = '<div class="who"><strong></strong><span></span></div><button type="button" class="logout">Keluar</button>';
    bar.querySelector('strong').textContent = session.name;
    bar.querySelector('span').textContent = session.role === 'admin' ? 'Admin' : 'NIM ' + session.nim;
    bar.querySelector('.logout').addEventListener('click', () => API.logout());
}
