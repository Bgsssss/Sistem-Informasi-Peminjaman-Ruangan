const session = API.requireRole('admin');
const TYPE_LABEL = {
  'no-show': 'Tidak check-in (no-show)',
  'cancel-mendadak': 'Batal mendadak',
  'selesai': 'Booking selesai, check-in tepat waktu',
  'admin': 'Penyesuaian oleh admin'
};
const state = { list: [], filter: 'pelanggaran', sort: 'skor', selected: null };

const rowsEl = document.getElementById('rows');
const noticeEl = document.getElementById('notice');
const detailCard = document.getElementById('detailCard');
const formNotice = document.getElementById('formNotice');
const deltaEl = document.getElementById('fDelta');
const reasonEl = document.getElementById('fReason');

if (session) {
  mountShell(session, 'pelanggaran');
  init();
}

function init() {
  document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', () => {
      state.filter = tab.dataset.f;
      document.querySelectorAll('.tab').forEach(t => t.setAttribute('aria-pressed', String(t === tab)));
      render();
    });
  });
  document.getElementById('sort').addEventListener('change', e => { state.sort = e.target.value; render(); });
  document.getElementById('closeBtn').addEventListener('click', closeDetail);
  document.getElementById('adjustForm').addEventListener('submit', adjust);
  load();
  setInterval(load, 30000);
}

function load() {
  return API.getViolationLog().then(list => { state.list = list; render(); });
}

function say(text, kind) {
  noticeEl.className = 'notice ' + kind;
  noticeEl.textContent = text;
}

function fmtDate(iso) {
  return new Date(iso.length <= 10 ? iso + 'T00:00:00' : iso).toLocaleDateString('id-ID', { day: 'numeric', month: 'short', year: 'numeric' });
}

function signed(n) { return (n > 0 ? '+' : '') + n; }

function cell(text, cls) {
  const td = document.createElement('td');
  td.textContent = text;
  if (cls) td.className = cls;
  return td;
}

function render() {
  // Ringkasan
  const withViolation = state.list.filter(s => s.noShow + s.lateCancel > 0);
  document.getElementById('sMhs').textContent = withViolation.length;
  document.getElementById('sKunci').textContent = state.list.filter(s => s.trustScore < CONFIG.minScore).length;
  document.getElementById('sNoShow').textContent = state.list.reduce((n, s) => n + s.noShow, 0);
  document.getElementById('sBatal').textContent = state.list.reduce((n, s) => n + s.lateCancel, 0);

  // Filter dan urutan (FR-34: urut/filter berdasarkan skor terendah)
  let list = state.list.filter(s =>
    state.filter === 'semua' ||
    (state.filter === 'pelanggaran' ? s.noShow + s.lateCancel > 0 : s.trustScore < CONFIG.minScore));
  list = list.slice().sort((a, b) => state.sort === 'skor'
    ? a.trustScore - b.trustScore || a.name.localeCompare(b.name)
    : (b.noShow + b.lateCancel) - (a.noShow + a.lateCancel) || a.trustScore - b.trustScore);

  rowsEl.innerHTML = '';
  if (list.length === 0) {
    const tr = document.createElement('tr');
    const td = cell('Tidak ada mahasiswa untuk filter ini.', 'empty-row');
    td.colSpan = 8;
    tr.appendChild(td);
    rowsEl.appendChild(tr);
    return;
  }

  list.forEach(s => {
    const tr = document.createElement('tr');

    const who = document.createElement('td');
    const name = document.createElement('strong');
    name.textContent = s.name;
    const nim = document.createElement('span');
    nim.className = 'muted';
    nim.textContent = 'NIM ' + s.nim;
    who.append(name, nim);

    const locked = s.trustScore < CONFIG.minScore;
    const rightTd = document.createElement('td');
    const chip = document.createElement('span');
    chip.className = locked ? 'chip cancel' : 'chip';
    chip.textContent = locked ? 'Terkunci' : 'Normal';
    rightTd.appendChild(chip);

    const actionTd = document.createElement('td');
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'small-btn';
    btn.textContent = 'Detail dan penyesuaian';
    btn.addEventListener('click', () => openDetail(s.nim));
    actionTd.appendChild(btn);

    tr.append(who, cell(s.faculty), cell(s.trustScore + ' / 100', locked ? 'num-neg' : ''),
      cell(String(s.noShow)), cell(String(s.lateCancel)), cell(s.last ? fmtDate(s.last) : '-'), rightTd, actionTd);
    rowsEl.appendChild(tr);
  });
}

// ---- Detail dan penyesuaian skor (FR-26) ----
function openDetail(nim) {
  state.selected = nim;
  return API.getStudentLog(nim).then(d => {
    const s = d.student;
    document.getElementById('detailTitle').textContent = s.name;
    document.getElementById('detailMeta').textContent =
      'NIM ' + s.nim + ', ' + s.faculty + '. Trust score ' + s.trustScore + ' / 100' +
      (s.trustScore < CONFIG.minScore ? ' (di bawah batas booking ' + CONFIG.minScore + ').' : '.');

    const hist = document.getElementById('histRows');
    hist.innerHTML = '';
    if (d.history.length === 0) {
      const tr = document.createElement('tr');
      const td = cell('Belum ada perubahan skor.', 'empty-row');
      td.colSpan = 4;
      tr.appendChild(td);
      hist.appendChild(tr);
    }
    d.history.slice(0, 30).forEach(e => {
      const tr = document.createElement('tr');
      const label = (TYPE_LABEL[e.type] || e.type) + (e.reason ? ': ' + e.reason : '');
      tr.append(cell(fmtDate(e.at)), cell(label, 'wrap'), cell(signed(e.delta), e.delta > 0 ? 'num-pos' : 'num-neg'), cell(String(e.scoreAfter)));
      hist.appendChild(tr);
    });

    const list = document.getElementById('vioList');
    list.innerHTML = '';
    if (d.violations.length === 0) {
      const li = document.createElement('li');
      li.className = 'empty';
      li.innerHTML = '<p>Belum ada pelanggaran.</p>';
      list.appendChild(li);
    }
    d.violations.forEach(v => {
      const li = document.createElement('li');
      li.className = 'item';
      const info = document.createElement('div');
      const title = document.createElement('strong');
      title.textContent = TYPE_LABEL[v.type];
      const meta = document.createElement('span');
      meta.className = 'muted';
      meta.textContent = v.room + ', ' + fmtDate(v.date) + ', ' + v.start + '-' + v.end;
      info.append(title, meta);
      const delta = document.createElement('span');
      delta.className = 'num-neg';
      delta.textContent = signed(v.delta) + ' poin';
      li.append(info, delta);
      list.appendChild(li);
    });

    deltaEl.value = '';
    reasonEl.value = '';
    formNotice.textContent = '';
    detailCard.hidden = false;
    detailCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
  });
}

function closeDetail() {
  detailCard.hidden = true;
  state.selected = null;
}

function adjust(e) {
  e.preventDefault();
  const nim = state.selected;
  if (!nim) return;
  API.adjustScore(nim, deltaEl.value, reasonEl.value)
    .then(res => {
      say('Skor diubah dari ' + res.before + ' menjadi ' + res.after + '. Perubahan tercatat di riwayat skor mahasiswa.', 'ok');
      return Promise.all([load(), openDetail(nim)]);
    })
    .catch(err => {
      formNotice.className = 'notice err';
      formNotice.textContent = err.message;
    });
}
