const session = API.requireRole('mahasiswa');

const TYPE_LABEL = {
  'no-show': 'Tidak check-in (no-show)',
  'cancel-mendadak': 'Batal mendadak',
  'selesai': 'Booking selesai, check-in tepat waktu',
  'admin': 'Penyesuaian oleh admin',
  'awal': 'Skor awal'
};

if (session) {
  mountShell(session, 'profil');
  document.getElementById('pwForm').addEventListener('submit', changePassword);
  load();
}

function load() {
  Promise.all([API.getStudent(), API.getTrustHistory(), API.getViolations()])
    .then(([student, history, violations]) => render(student, history, violations));
}

function fmtDate(iso) {
  return new Date(iso).toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' });
}

function fmtDay(isoDate) {
  return new Date(isoDate + 'T00:00:00').toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' });
}

function signed(n) { return (n > 0 ? '+' : '') + n; }

function scoreLabel(score) {
  if (score < CONFIG.minScore) return 'Di bawah batas minimal ' + CONFIG.minScore + '. Booking baru dinonaktifkan; hubungi admin fasilitas kampus untuk penyesuaian skor.';
  if (score < 80) return 'Di atas batas minimal ' + CONFIG.minScore + ', tapi hindari telat check-in dan batal mendadak.';
  return 'Baik. Pertahankan dengan check-in tepat waktu.';
}

function render(student, history, violations) {
  document.getElementById('score').textContent = student.trustScore;
  document.getElementById('scoreMax').textContent = student.maxScore;
  document.getElementById('scoreBar').style.width = (student.trustScore / student.maxScore * 100) + '%';
  document.getElementById('scoreLabel').textContent = scoreLabel(student.trustScore);
  document.getElementById('rules').textContent =
    'Naik ' + CONFIG.penalty.onTime + ' poin tiap booking selesai dengan check-in tepat waktu. Turun ' +
    Math.abs(CONFIG.penalty.noShow) + ' poin untuk no-show dan ' + Math.abs(CONFIG.penalty.lateCancel) +
    ' poin untuk batal kurang dari ' + CONFIG.lateCancelHours + ' jam sebelum jadwal.';

  // Data diri
  const info = document.getElementById('info');
  info.innerHTML = '';
  [['Nama', student.name], ['NIM', student.nim], ['Fakultas', student.faculty], ['Email', student.email]]
    .forEach(([k, v]) => {
      const dt = document.createElement('dt'); dt.textContent = k;
      const dd = document.createElement('dd'); dd.textContent = v;
      info.append(dt, dd);
    });

  // Riwayat trust score
  const rows = document.getElementById('scoreRows');
  rows.innerHTML = '';
  if (history.length === 0) {
    rows.appendChild(emptyRow(4, 'Belum ada perubahan skor.'));
  }
  history.forEach(e => {
    const tr = document.createElement('tr');
    const d = new Date(e.at);
    tr.append(
      cell(fmtDate(e.at) + ', ' + hhmm(d)),
      cell(TYPE_LABEL[e.type] || e.type),
      cell(signed(e.delta), e.delta > 0 ? 'num-pos' : 'num-neg'),
      cell(String(e.scoreAfter))
    );
    rows.appendChild(tr);
  });

  // Riwayat pelanggaran
  const list = document.getElementById('violations');
  list.innerHTML = '';
  if (violations.length === 0) {
    const li = document.createElement('li');
    li.className = 'empty';
    li.innerHTML = '<p>Belum ada pelanggaran. Pertahankan.</p>';
    list.appendChild(li);
  }
  violations.forEach(v => {
    const li = document.createElement('li');
    li.className = 'item';
    const info = document.createElement('div');
    const title = document.createElement('strong');
    title.textContent = TYPE_LABEL[v.type];
    const meta = document.createElement('span');
    meta.className = 'muted';
    meta.textContent = [v.room, fmtDay(v.date), v.start && v.start + '-' + v.end].filter(Boolean).join(', ');
    info.append(title, meta);
    const delta = document.createElement('span');
    delta.className = 'num-neg';
    delta.textContent = signed(v.delta) + ' poin';
    li.append(info, delta);
    list.appendChild(li);
  });
}

function cell(text, cls) {
  const td = document.createElement('td');
  td.textContent = text;
  if (cls) td.className = cls;
  return td;
}

function emptyRow(span, text) {
  const tr = document.createElement('tr');
  const td = cell(text, 'empty-row');
  td.colSpan = span;
  tr.appendChild(td);
  return tr;
}

function changePassword(e) {
  e.preventDefault();
  const notice = document.getElementById('pwNotice');
  const current = document.getElementById('pwCurrent').value;
  const next = document.getElementById('pwNew').value;
  const confirmPw = document.getElementById('pwConfirm').value;

  if (!current || !next || !confirmPw) {
    notice.className = 'notice err';
    notice.textContent = 'Isi semua kolom kata sandi.';
    return;
  }
  API.changePassword(current, next, confirmPw)
    .then(() => {
      e.target.reset();
      notice.className = 'notice ok';
      notice.textContent = 'Kata sandi berhasil diganti. Gunakan yang baru saat login berikutnya.';
    })
    .catch(err => {
      notice.className = 'notice err';
      notice.textContent = err.message;
    });
}
