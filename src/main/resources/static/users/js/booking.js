const session = API.requireRole('mahasiswa');
const STATUS_LABEL = { ok: 'Tersedia', booked: 'Dibooking', busy: 'Sedang dipakai' };
const state = { room: null, can: { ok: true, reason: '' } };

const typeEl = document.getElementById('type');
const dateEl = document.getElementById('date');
const slotEl = document.getElementById('slot');
const roomsEl = document.getElementById('rooms');
const summaryEl = document.getElementById('summary');
const bookBtn = document.getElementById('bookBtn');
const noticeEl = document.getElementById('notice');
const bannerEl = document.getElementById('banner');

if (session) {
  mountShell(session, 'booking');
  init();
}

function formatDate(iso) {
  return new Date(iso + 'T00:00:00').toLocaleDateString('id-ID', {
    weekday: 'long', day: 'numeric', month: 'long'
  });
}

function init() {
  typeEl.add(new Option('Semua tipe', ''));
  Object.entries(API.getRoomTypes()).forEach(([key, t]) =>
    typeEl.add(new Option(t.label + ' (' + t.min + '-' + t.max + ' orang)', key)));

  const q = new URLSearchParams(location.search);
  if (q.get('type') && API.getRoomTypes()[q.get('type')]) typeEl.value = q.get('type');

  API.getSlots().forEach(s => slotEl.add(new Option(s.start + '-' + s.end, s.start)));

  API.getBookableDates().forEach(d => {
    const text = d.open ? d.label + ', ' + formatDate(d.value) : d.label + ' (dibuka pukul ' + CONFIG.openNextDayAt + '.00)';
    const opt = new Option(text, d.value);
    opt.disabled = !d.open;
    dateEl.add(opt);
  });
  dateEl.value = todayISO();
  const qDate = q.get('date');
  if (qDate && Array.from(dateEl.options).some(o => o.value === qDate && !o.disabled)) dateEl.value = qDate;

  typeEl.addEventListener('change', load);
  slotEl.addEventListener('change', load);
  dateEl.addEventListener('change', () => { refreshSlots(); load(); });
  bookBtn.addEventListener('click', submit);

  refreshSlots();
  const qSlot = Array.from(slotEl.options).find(o => o.value === q.get('slot') && !o.disabled);
  if (qSlot) slotEl.value = qSlot.value;
  load();
  setInterval(refresh, 30000); // ketersediaan "real-time" lewat polling; BE bisa menggantinya dengan WebSocket
}

// Slot yang sudah mulai tidak bisa dipilih
function refreshSlots() {
  Array.from(slotEl.options).forEach(o => { o.disabled = API.isSlotPassed(dateEl.value, o.value); });
  const current = slotEl.options[slotEl.selectedIndex];
  if (!current || current.disabled) {
    const first = Array.from(slotEl.options).find(o => !o.disabled);
    slotEl.value = first ? first.value : '';
  }
}

function load() {
  state.room = null;
  noticeEl.textContent = '';
  refresh();
}

function refresh() {
  state.can = API.canBook();
  bannerEl.hidden = state.can.ok;
  bannerEl.textContent = state.can.reason;

  if (!dateEl.value || !slotEl.value) {
    roomsEl.innerHTML = '<p class="muted">Tidak ada slot tersisa untuk tanggal ini. Pilih tanggal lain.</p>';
    renderSummary();
    return;
  }
  API.getAvailability(dateEl.value, slotEl.value, typeEl.value).then(renderRooms).catch(err => { noticeEl.className = 'notice err'; noticeEl.textContent = API.errorMessage(err); });
}

function renderRooms(rooms) {
  state.can = API.canBook();
  bannerEl.hidden = state.can.ok;
  bannerEl.textContent = state.can.reason;
  // Ruangan yang dipilih bisa saja baru terisi oleh mahasiswa lain
  const keep = state.room && rooms.find(r => r.id === state.room.id && r.status === 'ok');
  if (state.room && !keep) {
    state.room = null;
    noticeEl.className = 'notice err';
    noticeEl.textContent = 'Ruangan yang kamu pilih baru saja terisi. Pilih ruangan lain.';
  }

  roomsEl.innerHTML = '';
  rooms.forEach(room => {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'room';
    btn.dataset.s = room.status;
    btn.setAttribute('aria-label', room.name + ', ' + STATUS_LABEL[room.status]);

    const name = document.createElement('strong');
    name.textContent = room.name;
    const cap = document.createElement('small');
    cap.textContent = room.capacity + ' orang';
    const st = document.createElement('small');
    st.textContent = STATUS_LABEL[room.status];
    btn.append(name, cap, st);

    if (room.status === 'ok') {
      if (state.room && state.room.id === room.id) btn.classList.add('selected');
      btn.addEventListener('click', () => {
        roomsEl.querySelectorAll('.room.selected').forEach(el => el.classList.remove('selected'));
        btn.classList.add('selected');
        state.room = room;
        noticeEl.textContent = '';
        renderSummary();
      });
    } else {
      btn.disabled = true;
    }
    roomsEl.appendChild(btn);
  });
  renderSummary();
}

function renderSummary() {
  const r = state.room;
  bookBtn.disabled = !r || !state.can.ok;
  if (!r) {
    summaryEl.innerHTML = '';
    const p = document.createElement('p');
    p.className = 'muted';
    p.textContent = state.can.ok ? 'Pilih ruangan hijau untuk melanjutkan.' : 'Booking dinonaktifkan. Lihat keterangan di atas.';
    summaryEl.appendChild(p);
    return;
  }
  const rows = [
    ['Ruangan', r.name],
    ['Tipe', API.getRoomTypes()[r.type].label],
    ['Lokasi', r.location],
    ['Kapasitas', r.capacity + ' orang'],
    ['Tanggal', formatDate(dateEl.value)],
    ['Waktu', slotEl.options[slotEl.selectedIndex].text]
  ];
  summaryEl.innerHTML = '';
  const dl = document.createElement('dl');
  rows.forEach(([k, v]) => {
    const dt = document.createElement('dt'); dt.textContent = k;
    const dd = document.createElement('dd'); dd.textContent = v;
    dl.append(dt, dd);
  });
  summaryEl.appendChild(dl);
  const more = document.createElement('a');
  more.href = 'detail-ruangan.html?id=' + r.id;
  more.className = 'link';
  more.textContent = 'Lihat detail ruangan';
  summaryEl.appendChild(more);
}

function submit() {
  if (!state.room) return;
  const room = state.room;
  bookBtn.disabled = true;

  API.createBooking(room.id, dateEl.value, slotEl.value)
    .then(b => {
      load();
      noticeEl.className = 'notice ok';
      noticeEl.textContent = 'Booking berhasil: ' + b.room + ', ' + formatDate(b.date) + ', ' + b.start + '-' + b.end + '. Jangan lupa check-in.';
    })
    .catch(err => {
      load();
      noticeEl.className = 'notice err';
      noticeEl.textContent = err.message;
    });
}