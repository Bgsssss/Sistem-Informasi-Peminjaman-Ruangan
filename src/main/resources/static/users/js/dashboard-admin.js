const session = API.requireRole('admin');

if (session) {
  mountShell(session, 'dashboard');
  load();
  setInterval(load, 30000);
}

function load() {
  API.getAdminDashboard().then(render);
}

function cell(text) {
  const td = document.createElement('td');
  td.textContent = text;
  return td;
}

function statusChip(b) {
  const chip = document.createElement('span');
  if (b.status === 'aktif') {
    chip.className = 'chip';
    chip.textContent = b.checkedIn ? 'Aktif, sudah check-in' : 'Aktif';
  } else if (b.status === 'selesai') {
    chip.className = 'chip done';
    chip.textContent = 'Selesai';
  } else {
    chip.className = 'chip cancel';
    chip.textContent = b.cancelledBy === 'sistem' ? 'Dibatalkan (no-show)' : 'Dibatalkan';
  }
  return chip;
}

function render(d) {
  document.getElementById('dateLabel').textContent = 'Ringkasan booking ' +
    new Date(d.date + 'T00:00:00').toLocaleDateString('id-ID', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }) + '.';

  document.getElementById('sTotal').textContent = d.total;
  document.getElementById('sAktif').textContent = d.aktif;
  document.getElementById('sSelesai').textContent = d.selesai;
  document.getElementById('sBatal').textContent = d.dibatalkan;

  document.getElementById('occPercent').textContent = d.occupancy.percent;
  document.getElementById('occBar').style.width = d.occupancy.percent + '%';
  document.getElementById('occDetail').textContent =
    d.occupancy.filled + ' dari ' + d.occupancy.total + ' slot terisi (ruangan aktif x slot 08.00-21.00).';

  // Ruangan paling sering dipakai
  const rank = document.getElementById('rank');
  rank.innerHTML = '';
  if (d.topRooms.length === 0) {
    const li = document.createElement('li');
    li.className = 'muted';
    li.textContent = 'Belum ada data pemakaian.';
    rank.appendChild(li);
  }
  const max = d.topRooms.length ? d.topRooms[0].total : 1;
  d.topRooms.forEach(r => {
    const li = document.createElement('li');
    li.className = 'rank-row';
    const name = document.createElement('strong');
    name.textContent = r.name;
    const count = document.createElement('span');
    count.className = 'count';
    count.textContent = r.total + ' booking';
    const track = document.createElement('div');
    track.className = 'track';
    const fill = document.createElement('i');
    fill.style.width = (r.total / max * 100) + '%';
    track.appendChild(fill);
    li.append(name, count, track);
    rank.appendChild(li);
  });

  // Daftar booking hari ini
  const rows = document.getElementById('rows');
  rows.innerHTML = '';
  if (d.bookings.length === 0) {
    const tr = document.createElement('tr');
    const td = cell('Belum ada booking hari ini.');
    td.colSpan = 5;
    td.className = 'empty-row';
    tr.appendChild(td);
    rows.appendChild(tr);
    return;
  }
  d.bookings.forEach(b => {
    const tr = document.createElement('tr');
    const statusTd = document.createElement('td');
    statusTd.appendChild(statusChip(b));
    tr.append(cell(b.start + '-' + b.end), cell(b.room), cell(b.studentName), cell(b.nim), statusTd);
    rows.appendChild(tr);
  });
}
