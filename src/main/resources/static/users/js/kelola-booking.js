const session = API.requireRole('admin');
const MAX_ROWS = 100;
const state = { rows: [], target: null };

const dateEl = document.getElementById('fDate');
const roomEl = document.getElementById('fRoom');
const statusEl = document.getElementById('fStatus');
const rowsEl = document.getElementById('rows');
const countEl = document.getElementById('count');
const noticeEl = document.getElementById('notice');
const cancelCard = document.getElementById('cancelCard');
const targetEl = document.getElementById('target');
const startedWarn = document.getElementById('startedWarn');
const reasonEl = document.getElementById('reason');
const formNotice = document.getElementById('formNotice');
const auditEl = document.getElementById('audit');

if (session) {
  mountShell(session, 'booking');
  init();
}

function init() {
  API.getAdminRooms().then(rooms => {
    rooms.forEach(r => roomEl.add(new Option(r.name + (r.active ? '' : ' (nonaktif)'), r.id)));
  });

  dateEl.min = addDays(-30);
  dateEl.max = addDays(1);
  dateEl.value = todayISO();

  [dateEl, roomEl, statusEl].forEach(el => el.addEventListener('change', load));
  document.getElementById('resetBtn').addEventListener('click', () => {
    dateEl.value = '';
    roomEl.value = '';
    statusEl.value = '';
    load();
  });
  document.getElementById('closeBtn').addEventListener('click', closeCancel);
  document.getElementById('cancelForm').addEventListener('submit', submitCancel);

  load();
  loadAudit();
  setInterval(load, 30000);
}

function load() {
  API.getAdminBookings({ date: dateEl.value, roomId: roomEl.value, status: statusEl.value })
    .then(rows => { state.rows = rows; render(); });
}

function say(text, kind) {
  noticeEl.className = 'notice ' + kind;
  noticeEl.textContent = text;
}

function fmtDate(iso) {
  return new Date(iso + 'T00:00:00').toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' });
}

function cell(text, cls) {
  const td = document.createElement('td');
  td.textContent = text;
  if (cls) td.className = cls;
  return td;
}

function note(b) {
  if (b.status === 'dibatalkan') {
    if (b.cancelledBy === 'sistem') return 'Auto-cancel (tidak check-in)';
    if (b.cancelledBy === 'admin') return 'Dibatalkan admin: ' + (b.cancelReason || '-');
    return b.cancelType === 'cancel-mendadak' ? 'Dibatalkan mahasiswa (mendadak)' : 'Dibatalkan mahasiswa';
  }
  if (b.status === 'selesai') return 'Check-in tepat waktu';
  return b.checkedIn ? 'Sudah check-in' : '-';
}

function chip(b) {
  const el = document.createElement('span');
  el.className = b.status === 'aktif' ? 'chip' : (b.status === 'selesai' ? 'chip done' : 'chip cancel');
  el.textContent = b.status === 'aktif' ? 'Aktif' : (b.status === 'selesai' ? 'Selesai' : 'Dibatalkan');
  return el;
}

function render() {
  rowsEl.innerHTML = '';

  if (state.rows.length === 0) {
    const tr = document.createElement('tr');
    const td = cell('Tidak ada booking untuk filter ini.', 'empty-row');
    td.colSpan = 7;
    tr.appendChild(td);
    rowsEl.appendChild(tr);
    countEl.textContent = '';
    return;
  }

  state.rows.slice(0, MAX_ROWS).forEach(b => {
    const tr = document.createElement('tr');

    const who = document.createElement('td');
    const name = document.createElement('strong');
    name.textContent = b.studentName;
    const nim = document.createElement('span');
    nim.className = 'muted';
    nim.textContent = 'NIM ' + b.nim;
    who.append(name, nim);

    const statusTd = document.createElement('td');
    statusTd.appendChild(chip(b));

    const actionTd = document.createElement('td');
    if (b.status === 'aktif' && !b.checkedIn) {
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'small-btn danger';
      btn.textContent = 'Batalkan';
      btn.addEventListener('click', () => openCancel(b));
      actionTd.appendChild(btn);
    } else {
      actionTd.textContent = '-';
    }

    tr.append(cell(fmtDate(b.date)), cell(b.start + '-' + b.end), cell(b.room), who, statusTd, cell(note(b), 'wrap'), actionTd);
    rowsEl.appendChild(tr);
  });

  countEl.textContent = state.rows.length > MAX_ROWS
    ? 'Menampilkan ' + MAX_ROWS + ' dari ' + state.rows.length + ' booking. Persempit filter untuk melihat sisanya.'
    : state.rows.length + ' booking.';
}

// ---- Pembatalan manual (FR-33) ----
function openCancel(b) {
  state.target = b;
  targetEl.innerHTML = '';
  const title = document.createElement('strong');
  title.textContent = b.room + ', ' + fmtDate(b.date) + ', ' + b.start + '-' + b.end;
  const who = document.createElement('span');
  who.className = 'muted';
  who.textContent = b.studentName + ' (NIM ' + b.nim + ')';
  targetEl.append(title, who);

  startedWarn.hidden = !(new Date(b.date + 'T' + b.start + ':00') <= now());
  reasonEl.value = '';
  formNotice.textContent = '';
  cancelCard.hidden = false;
  cancelCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
  reasonEl.focus();
}

function closeCancel() {
  cancelCard.hidden = true;
  state.target = null;
}

function submitCancel(e) {
  e.preventDefault();
  const b = state.target;
  if (!b) return;
  API.adminCancelBooking(b.id, reasonEl.value)
    .then(() => {
      closeCancel();
      say('Booking ' + b.room + ' milik ' + b.studentName + ' dibatalkan. Trust score mahasiswa tidak berubah.', 'ok');
      load();
      loadAudit();
    })
    .catch(err => {
      formNotice.className = 'notice err';
      formNotice.textContent = err.message;
    });
}

function loadAudit() {
  API.getAuditLog().then(entries => {
    auditEl.innerHTML = '';
    if (entries.length === 0) {
      const li = document.createElement('li');
      li.className = 'empty';
      li.innerHTML = '<p>Belum ada pembatalan oleh admin.</p>';
      auditEl.appendChild(li);
      return;
    }
    entries.slice(0, 10).forEach(en => {
      const li = document.createElement('li');
      li.className = 'item';
      const info = document.createElement('div');
      const title = document.createElement('strong');
      title.textContent = en.room + ', ' + fmtDate(en.date) + ', ' + en.start + '-' + en.end + ' (' + en.student + ')';
      const meta = document.createElement('span');
      meta.className = 'muted';
      const when = new Date(en.at);
      meta.textContent = 'Dibatalkan oleh ' + en.admin + ' pada ' + fmtDate(en.at.slice(0, 10)) + ', ' + hhmm(when) + '. Alasan: ' + en.reason;
      info.append(title, meta);
      li.appendChild(info);
      auditEl.appendChild(li);
    });
  });
}
