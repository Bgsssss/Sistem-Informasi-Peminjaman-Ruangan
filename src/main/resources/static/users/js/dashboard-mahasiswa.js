const session = API.requireRole('mahasiswa');

if (session) {
  mountShell(session, 'dashboard');
  API.getStudentDashboard().then(render);
}

function scoreLabel(score) {
  if (score < CONFIG.minScore) return 'Di bawah batas minimal ' + CONFIG.minScore + '. Booking baru dinonaktifkan.';
  if (score < 80) return 'Di atas batas minimal ' + CONFIG.minScore + ', tapi hindari telat check-in dan batal mendadak.';
  return 'Baik. Pertahankan dengan check-in tepat waktu.';
}

function formatDate(iso) {
  return new Date(iso + 'T00:00:00').toLocaleDateString('id-ID', {
    weekday: 'long', day: 'numeric', month: 'long'
  });
}

function render(data) {
  const { student, bookings } = data;

  document.getElementById('greeting').textContent = 'Halo, ' + student.name;
  document.getElementById('score').textContent = student.trustScore;
  document.getElementById('scoreMax').textContent = student.maxScore;
  document.getElementById('scoreLabel').textContent = scoreLabel(student.trustScore);
  document.getElementById('scoreBar').style.width = (student.trustScore / student.maxScore * 100) + '%';

  document.getElementById('limitNote').textContent = data.canBook.ok ? '' : data.canBook.reason;

  const list = document.getElementById('bookingList');
  list.innerHTML = '';

  if (bookings.length === 0) {
    const empty = document.createElement('li');
    empty.className = 'empty';
    empty.innerHTML = '<p>Belum ada booking aktif. Pilih ruangan untuk mulai.</p><a class="btn" href="booking.html">Booking ruangan</a>';
    list.appendChild(empty);
    return;
  }

  bookings.forEach(b => {
    const li = document.createElement('li');
    li.className = 'item';

    const info = document.createElement('div');
    const room = document.createElement('strong');
    room.textContent = b.room;
    const when = document.createElement('span');
    when.className = 'muted';
    when.textContent = formatDate(b.date) + ', ' + b.start + '-' + b.end;
    info.append(room, when);

    const chip = document.createElement('span');
    chip.className = 'chip';
    chip.textContent = b.checkedIn ? 'Sudah check-in' : 'Aktif';

    li.append(info, chip);
    list.appendChild(li);
  });
}