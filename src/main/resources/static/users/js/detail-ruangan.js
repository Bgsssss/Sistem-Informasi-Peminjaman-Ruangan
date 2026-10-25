const session = API.requireRole('mahasiswa');
const STATUS_LABEL = { ok: 'Tersedia', booked: 'Dibooking', busy: 'Sedang dipakai', passed: 'Sudah lewat' };

const dateEl = document.getElementById('date');
const stripEl = document.getElementById('strip');
let currentRoom = null;

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
  const id = new URLSearchParams(location.search).get('id');
  if (!id) return showMissing();
  API.getRoom(id).then(render).catch(showMissing);
}

function showMissing() {
  document.getElementById('missing').hidden = false;
}

function render(room) {
  currentRoom = room;
  document.title = room.name + ' | Peminjaman Ruangan';
  document.getElementById('roomName').textContent = room.name;
  document.getElementById('roomSub').textContent = room.typeLabel + ', ' + room.location;

  // Galeri foto
  const main = document.getElementById('photoMain');
  const thumbs = document.getElementById('thumbs');
  const show = i => {
    main.src = room.photos[i];
    main.alt = room.name + ', foto ' + (i + 1);
    Array.from(thumbs.children).forEach((t, k) => t.setAttribute('aria-pressed', String(k === i)));
  };
  room.photos.forEach((src, i) => {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'thumb';
    btn.setAttribute('aria-label', 'Lihat foto ' + (i + 1));
    const img = document.createElement('img');
    img.src = src;
    img.alt = '';
    btn.appendChild(img);
    btn.addEventListener('click', () => show(i));
    thumbs.appendChild(btn);
  });
  show(0);

  // Informasi dan fasilitas
  const info = document.getElementById('info');
  const t = API.getRoomTypes()[room.type];
  [['Tipe', room.typeLabel], ['Lokasi', room.location], ['Kapasitas', room.capacity + ' orang'], ['Rentang tipe', t.min + '-' + t.max + ' orang']]
    .forEach(([k, v]) => {
      const dt = document.createElement('dt'); dt.textContent = k;
      const dd = document.createElement('dd'); dd.textContent = v;
      info.append(dt, dd);
    });
  const fac = document.getElementById('facilities');
  room.facilities.forEach(f => {
    const li = document.createElement('li');
    li.textContent = f;
    fac.appendChild(li);
  });
  document.getElementById('bookLink').href = 'booking.html?type=' + room.type;

  // Ketersediaan per jam
  API.getBookableDates().forEach(d => {
    const text = d.open ? d.label + ', ' + formatDate(d.value) : d.label + ' (dibuka pukul ' + CONFIG.openNextDayAt + '.00)';
    const opt = new Option(text, d.value);
    opt.disabled = !d.open;
    dateEl.add(opt);
  });
  dateEl.value = todayISO();
  dateEl.addEventListener('change', loadStrip);
  loadStrip();
  setInterval(loadStrip, 30000);

  document.getElementById('detail').hidden = false;
}

function loadStrip() {
  API.getRoomDay(currentRoom.id, dateEl.value).then(slots => {
    stripEl.innerHTML = '';
    slots.forEach(s => {
      const el = document.createElement(s.status === 'ok' ? 'a' : 'span');
      el.className = 'slot';
      el.dataset.s = s.status;
      el.setAttribute('aria-label', s.start + ', ' + STATUS_LABEL[s.status]);
      if (s.status === 'ok') {
        el.href = 'booking.html?type=' + currentRoom.type + '&date=' + dateEl.value + '&slot=' + s.start;
      }
      const time = document.createElement('strong');
      time.textContent = s.start;
      const label = document.createElement('small');
      label.textContent = STATUS_LABEL[s.status];
      el.append(time, label);
      stripEl.appendChild(el);
    });
  });
}
