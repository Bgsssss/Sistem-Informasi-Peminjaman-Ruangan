const session = API.requireRole('admin');
const PERIOD_LABEL = { harian: 'Hari ini', mingguan: 'Minggu ini (Senin sampai hari ini)', bulanan: 'Bulan ini (tanggal 1 sampai hari ini)' };
const state = { period: 'harian', report: null };

const rowsEl = document.getElementById('rows');
const footEl = document.getElementById('foot');
const rangeEl = document.getElementById('range');
const noticeEl = document.getElementById('notice');

if (session) {
  mountShell(session, 'laporan');
  document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', () => {
      state.period = tab.dataset.p;
      document.querySelectorAll('.tab').forEach(t => t.setAttribute('aria-pressed', String(t === tab)));
      load();
    });
  });
  document.getElementById('csvBtn').addEventListener('click', exportCsv);
  load();
  setInterval(load, 30000);
}

function load() {
  API.getUsageReport(state.period).then(report => { state.report = report; render(); });
}

function fmt(iso) {
  return new Date(iso + 'T00:00:00').toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' });
}

function cell(text, cls) {
  const td = document.createElement('td');
  td.textContent = text;
  if (cls) td.className = cls;
  return td;
}

function render() {
  const r = state.report;
  rangeEl.textContent = PERIOD_LABEL[r.period] + ': ' + fmt(r.from) + (r.from === r.to ? '' : ' sampai ' + fmt(r.to)) + '.';

  rowsEl.innerHTML = '';
  r.rows.forEach(x => {
    const tr = document.createElement('tr');
    const name = document.createElement('td');
    const strong = document.createElement('strong');
    strong.textContent = x.room;
    name.appendChild(strong);
    if (!x.active) {
      const note = document.createElement('span');
      note.className = 'muted';
      note.textContent = 'Nonaktif';
      name.appendChild(note);
    }
    tr.append(name, cell(x.typeLabel), cell(String(x.total), 'num'), cell(String(x.selesai), 'num'),
      cell(String(x.aktif), 'num'), cell(String(x.dibatalkan), 'num'), cell(String(x.noShow), 'num'));
    rowsEl.appendChild(tr);
  });

  footEl.innerHTML = '';
  const t = document.createElement('tr');
  const label = cell('Total');
  label.colSpan = 2;
  t.append(label, cell(String(r.totals.total), 'num'), cell(String(r.totals.selesai), 'num'),
    cell(String(r.totals.aktif), 'num'), cell(String(r.totals.dibatalkan), 'num'), cell(String(r.totals.noShow), 'num'));
  footEl.appendChild(t);
}

// Unduh laporan periode yang sedang dipilih (untuk pembukuan)
function exportCsv() {
  const r = state.report;
  const esc = v => '"' + String(v).replace(/"/g, '""') + '"';
  const lines = [['Ruangan', 'Tipe', 'Total booking', 'Selesai', 'Aktif', 'Dibatalkan', 'No-show'].map(esc).join(',')]
    .concat(r.rows.map(x => [x.room, x.typeLabel, x.total, x.selesai, x.aktif, x.dibatalkan, x.noShow].map(esc).join(',')))
    .concat([['Total', '', r.totals.total, r.totals.selesai, r.totals.aktif, r.totals.dibatalkan, r.totals.noShow].map(esc).join(',')]);

  const blob = new Blob(['\ufeff' + lines.join('\r\n')], { type: 'text/csv;charset=utf-8' });
  const a = document.createElement('a');
  a.href = URL.createObjectURL(blob);
  a.download = 'laporan-ruangan-' + r.period + '-' + r.from + '.csv';
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(a.href);
  noticeEl.className = 'notice ok';
  noticeEl.textContent = 'Laporan ' + r.period + ' diunduh.';
}
