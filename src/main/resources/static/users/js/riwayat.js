const session = API.requireRole('mahasiswa');
const STATUS = {
  aktif: { label: 'Aktif', cls: '' },
  selesai: { label: 'Selesai', cls: 'done' },
  dibatalkan: { label: 'Dibatalkan', cls: 'cancel' }
};
const state = { filter: 'semua', data: [] };

const rowsEl = document.getElementById('rows');
const noticeEl = document.getElementById('notice');

if (session) {
  mountShell(session, 'riwayat');

  document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', () => {
      state.filter = tab.dataset.f;
      document.querySelectorAll('.tab').forEach(t =>
        t.setAttribute('aria-pressed', String(t === tab)));
      render();
    });
  });
  document.getElementById('csvBtn').addEventListener('click', exportCsv);
  load();
}

function load() {
  API.getHistory().then(data => { state.data = data; render(); });
}

function formatDate(iso) {
  return new Date(iso + 'T00:00:00').toLocaleDateString('id-ID', {
    day: 'numeric', month: 'short', year: 'numeric'
  });
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

function count(status) { return state.data.filter(b => b.status === status).length; }

function render() {
  document.getElementById('sTotal').textContent = state.data.length;
  document.getElementById('sAktif').textContent = count('aktif');
  document.getElementById('sSelesai').textContent = count('selesai');
  document.getElementById('sBatal').textContent = count('dibatalkan');

  const list = state.filter === 'semua' ? state.data : state.data.filter(b => b.status === state.filter);
  rowsEl.innerHTML = '';

  if (list.length === 0) {
    const tr = document.createElement('tr');
    const td = document.createElement('td');
    td.colSpan = 7;
    td.className = 'empty-row';
    td.textContent = 'Belum ada booking untuk filter ini.';
    tr.appendChild(td);
    rowsEl.appendChild(tr);
    return;
  }

  list.forEach(b => {
    const tr = document.createElement('tr');
    const cell = text => { const td = document.createElement('td'); td.textContent = text; return td; };

    tr.append(cell(b.id), cell(b.room), cell(formatDate(b.date)), cell(b.start + '-' + b.end));

    const statusTd = document.createElement('td');
    const chip = document.createElement('span');
    chip.className = 'chip ' + STATUS[b.status].cls;
    chip.textContent = STATUS[b.status].label;
    statusTd.appendChild(chip);

    const actionTd = document.createElement('td');
    if (b.status === 'aktif' && !API.hasStarted(b)) {
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'small-btn';
      btn.textContent = 'Batalkan';
      btn.addEventListener('click', () => cancel(b));
      actionTd.appendChild(btn);
    } else {
      actionTd.textContent = '-';
    }

    tr.append(statusTd, cell(note(b)), actionTd);
    rowsEl.appendChild(tr);
  });
}

function cancel(b) {
  const late = API.isLateCancel(b);
  const msg = 'Batalkan booking ' + b.room + ' pada ' + formatDate(b.date) + ', ' + b.start + '-' + b.end + '?' +
    (late ? '\n\nKurang dari ' + CONFIG.lateCancelHours + ' jam sebelum jadwal: trust score kamu berkurang ' + Math.abs(CONFIG.penalty.lateCancel) + ' poin.' : '');
  if (!confirm(msg)) return;
  API.cancelBooking(b.id)
    .then(res => {
      load();
      noticeEl.className = 'notice ok';
      noticeEl.textContent = 'Booking ' + b.id + ' dibatalkan.' + (res.penalty ? ' Trust score ' + res.penalty + '.' : '');
    })
    .catch(err => {
      noticeEl.className = 'notice err';
      noticeEl.textContent = err.message;
    });
}

// Unduh catatan sesuai filter yang sedang dipilih
function exportCsv() {
  const list = state.filter === 'semua' ? state.data : state.data.filter(b => b.status === state.filter);
  const esc = v => '"' + String(v).replace(/"/g, '""') + '"';
  const lines = [['ID', 'Ruangan', 'Tanggal', 'Mulai', 'Selesai', 'Status', 'Keterangan'].map(esc).join(',')]
    .concat(list.map(b => [b.id, b.room, b.date, b.start, b.end, STATUS[b.status].label, note(b)].map(esc).join(',')));

  const blob = new Blob(['\ufeff' + lines.join('\r\n')], { type: 'text/csv;charset=utf-8' });
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = 'riwayat-booking-' + state.filter + '.csv';
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(a.href);
}