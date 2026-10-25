const session = API.requireRole('mahasiswa');
const listEl = document.getElementById('list');
const noticeEl = document.getElementById('notice');

if (session) {
  mountShell(session, 'checkin');
  load();
  setInterval(load, 30000); // perbarui status jendela check-in
}

function load() {
  API.getCheckinList().then(render);
}

function render(items) {
  listEl.innerHTML = '';

  if (items.length === 0) {
    const li = document.createElement('li');
    li.className = 'empty';
    li.innerHTML = '<p>Tidak ada booking aktif untuk hari ini.</p><a class="btn" href="booking.html">Booking ruangan</a>';
    listEl.appendChild(li);
    return;
  }

  items.forEach(b => {
    const li = document.createElement('li');
    li.className = 'item';

    const info = document.createElement('div');
    const room = document.createElement('strong');
    room.textContent = b.room;
    const when = document.createElement('span');
    when.className = 'muted';
    when.textContent = b.start + '-' + b.end;
    const hint = document.createElement('span');
    hint.className = 'muted';
    hint.textContent = b.state === 'belum' ? 'Check-in dibuka pukul ' + b.opensAt
      : b.state === 'buka' ? 'Batas check-in pukul ' + b.closesAt
      : 'Check-in tercatat';
    info.append(room, when, hint);

    li.appendChild(info);

    if (b.state === 'sudah') {
      const chip = document.createElement('span');
      chip.className = 'chip';
      chip.textContent = 'Sudah check-in';
      li.appendChild(chip);
    } else {
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'btn';
      btn.textContent = 'Check-in';
      btn.disabled = b.state !== 'buka';
      btn.addEventListener('click', () => checkIn(b));
      li.appendChild(btn);
    }
    listEl.appendChild(li);
  });
}

function checkIn(b) {
  API.checkIn(b.id)
    .then(() => {
      noticeEl.className = 'notice ok';
      noticeEl.textContent = 'Check-in berhasil di ' + b.room + '. Selamat berdiskusi.';
      load();
    })
    .catch(err => {
      noticeEl.className = 'notice err';
      noticeEl.textContent = err.message;
      load();
    });
}
