const session = API.requireRole('admin');
const state = { rooms: [], filter: 'semua', editId: null };

const rowsEl = document.getElementById('rows');
const noticeEl = document.getElementById('notice');
const formCard = document.getElementById('formCard');
const formNotice = document.getElementById('formNotice');
const nameEl = document.getElementById('fName');
const typeEl = document.getElementById('fType');
const locEl = document.getElementById('fLocation');
const capEl = document.getElementById('fCapacity');
const facEl = document.getElementById('fFacilities');
const photoEl = document.getElementById('fPhoto');

if (session) {
  mountShell(session, 'ruangan');
  init();
}

function init() {
  Object.entries(API.getRoomTypes()).forEach(([key, t]) =>
    typeEl.add(new Option(t.label + ' (' + t.min + '-' + t.max + ' orang)', key)));
  typeEl.addEventListener('change', updateHint);

  document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', () => {
      state.filter = tab.dataset.f;
      document.querySelectorAll('.tab').forEach(t => t.setAttribute('aria-pressed', String(t === tab)));
      render();
    });
  });

  document.getElementById('addBtn').addEventListener('click', () => openForm(null));
  document.getElementById('cancelBtn').addEventListener('click', closeForm);
  document.getElementById('roomForm').addEventListener('submit', save);
  load();
}

function load() {
  return API.getAdminRooms().then(rooms => { state.rooms = rooms; render(); });
}

function say(text, kind) {
  noticeEl.className = 'notice ' + kind;
  noticeEl.textContent = text;
}

function cell(text, cls) {
  const td = document.createElement('td');
  td.textContent = text;
  if (cls) td.className = cls;
  return td;
}

function actionBtn(text, handler) {
  const b = document.createElement('button');
  b.type = 'button';
  b.className = 'small-btn';
  b.textContent = text;
  b.addEventListener('click', handler);
  return b;
}

function render() {
  const list = state.rooms.filter(r =>
    state.filter === 'semua' || (state.filter === 'aktif' ? r.active : !r.active));
  rowsEl.innerHTML = '';

  if (list.length === 0) {
    const tr = document.createElement('tr');
    const td = cell('Tidak ada ruangan untuk filter ini.', 'empty-row');
    td.colSpan = 7;
    tr.appendChild(td);
    rowsEl.appendChild(tr);
    return;
  }

  list.forEach(r => {
    const tr = document.createElement('tr');

    const nameTd = document.createElement('td');
    const strong = document.createElement('strong');
    strong.textContent = r.name;
    const loc = document.createElement('span');
    loc.className = 'muted';
    loc.textContent = r.location;
    nameTd.append(strong, loc);

    const statusTd = document.createElement('td');
    const chip = document.createElement('span');
    chip.className = r.active ? 'chip' : 'chip done';
    chip.textContent = r.active ? 'Aktif' : 'Nonaktif';
    statusTd.appendChild(chip);

    const actionTd = document.createElement('td');
    const row = document.createElement('div');
    row.className = 'btn-row';
    row.append(
      actionBtn('Edit', () => openForm(r)),
      actionBtn(r.active ? 'Nonaktifkan' : 'Aktifkan', () => toggle(r)),
      actionBtn('Hapus', () => remove(r))
    );
    actionTd.appendChild(row);

    tr.append(nameTd, cell(r.typeLabel), cell(r.capacity + ' orang'),
      cell(r.facilities.join(', ') || '-', 'wrap'), cell(String(r.activeBookings)), statusTd, actionTd);
    rowsEl.appendChild(tr);
  });
}

// ---- Form tambah/ubah ----
function updateHint() {
  const t = API.getRoomTypes()[typeEl.value];
  document.getElementById('capHint').textContent = 'Rentang ' + t.min + '-' + t.max + ' orang untuk tipe ini.';
  capEl.min = t.min;
  capEl.max = t.max;
}

function openForm(room) {
  state.editId = room ? room.id : null;
  document.getElementById('formTitle').textContent = room ? 'Ubah ' + room.name : 'Tambah ruangan';
  nameEl.value = room ? room.name : '';
  typeEl.value = room ? room.type : Object.keys(API.getRoomTypes())[0];
  locEl.value = room ? room.location : '';
  capEl.value = room ? room.capacity : '';
  facEl.value = room ? room.facilities.join(', ') : '';
  photoEl.value = room ? (room.photoUrl || '') : '';
  formNotice.textContent = '';
  updateHint();
  formCard.hidden = false;
  formCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
  nameEl.focus();
}

function closeForm() {
  formCard.hidden = true;
  state.editId = null;
}

function save(e) {
  e.preventDefault();
  const data = {
    name: nameEl.value, type: typeEl.value, location: locEl.value,
    capacity: capEl.value, facilities: facEl.value.split(','), photoUrl: photoEl.value
  };
  const editing = state.editId;
  const request = editing ? API.updateRoom(editing, data) : API.addRoom(data);

  request
    .then(room => {
      closeForm();
      say((editing ? 'Ruangan ' : 'Ruangan baru ') + room.name + ' berhasil disimpan.', 'ok');
      return load();
    })
    .catch(err => {
      formNotice.className = 'notice err';
      formNotice.textContent = err.message;
    });
}

// ---- Aktif/nonaktif dan hapus ----
function toggle(room) {
  if (!room.active) {
    API.setRoomActive(room.id, true).then(() => {
      say(room.name + ' diaktifkan kembali dan muncul di pencarian ketersediaan.', 'ok');
      return load();
    });
    return;
  }

  const msg = room.activeBookings > 0
    ? room.name + ' punya ' + room.activeBookings + ' booking aktif. Booking itu TIDAK dibatalkan otomatis; tindak lanjuti manual lewat Kelola Booking.\n\nNonaktifkan ruangan?'
    : 'Nonaktifkan ' + room.name + '? Ruangan tidak akan muncul di pencarian ketersediaan.';
  if (!confirm(msg)) return;

  API.setRoomActive(room.id, false).then(res => {
    say(room.name + ' dinonaktifkan.' + (res.affected ? ' ' + res.affected + ' booking aktif perlu ditindaklanjuti lewat Kelola Booking.' : ''), 'ok');
    return load();
  });
}

function remove(room) {
  if (!confirm('Hapus ' + room.name + ' secara permanen? Tindakan ini tidak bisa dibatalkan.')) return;
  API.deleteRoom(room.id)
    .then(() => { say(room.name + ' dihapus.', 'ok'); return load(); })
    .catch(err => say(err.message, 'err'));
}
