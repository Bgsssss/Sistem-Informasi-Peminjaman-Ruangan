const session = API.requireRole('admin');
const state = { students: [], filter: 'semua', query: '', editNim: null };

const rowsEl = document.getElementById('rows');
const noticeEl = document.getElementById('notice');
const formCard = document.getElementById('formCard');
const formNotice = document.getElementById('formNotice');
const nimEl = document.getElementById('fNim');
const nameEl = document.getElementById('fName');
const facEl = document.getElementById('fFaculty');
const emailEl = document.getElementById('fEmail');
const pwEl = document.getElementById('fPassword');
const pwWrap = document.getElementById('pwWrap');

if (session) {
  mountShell(session, 'mahasiswa');
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
  document.getElementById('search').addEventListener('input', e => { state.query = e.target.value.trim().toLowerCase(); render(); });
  document.getElementById('addBtn').addEventListener('click', () => openForm(null));
  document.getElementById('cancelBtn').addEventListener('click', closeForm);
  document.getElementById('studentForm').addEventListener('submit', save);
  load();
}

function load() {
  return API.getAdminStudents().then(list => { state.students = list; render(); });
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
  const list = state.students.filter(s =>
    (state.filter === 'semua' || (state.filter === 'aktif' ? s.active : !s.active)) &&
    (!state.query || s.name.toLowerCase().includes(state.query) || s.nim.includes(state.query)));
  rowsEl.innerHTML = '';

  if (list.length === 0) {
    const tr = document.createElement('tr');
    const td = cell('Tidak ada mahasiswa untuk filter ini.', 'empty-row');
    td.colSpan = 7;
    tr.appendChild(td);
    rowsEl.appendChild(tr);
    return;
  }

  list.forEach(s => {
    const tr = document.createElement('tr');

    const who = document.createElement('td');
    const name = document.createElement('strong');
    name.textContent = s.name;
    const mail = document.createElement('span');
    mail.className = 'muted';
    mail.textContent = s.email;
    who.append(name, mail);

    const score = cell(s.trustScore + ' / 100', s.trustScore < CONFIG.minScore ? 'num-neg' : '');

    const statusTd = document.createElement('td');
    const chip = document.createElement('span');
    chip.className = s.active ? 'chip' : 'chip done';
    chip.textContent = s.active ? 'Aktif' : 'Nonaktif';
    statusTd.appendChild(chip);

    const actionTd = document.createElement('td');
    const row = document.createElement('div');
    row.className = 'btn-row';
    row.append(
      actionBtn('Edit', () => openForm(s)),
      actionBtn(s.active ? 'Nonaktifkan' : 'Aktifkan', () => toggle(s)),
      actionBtn('Reset password', () => reset(s)),
      actionBtn('Hapus', () => remove(s))
    );
    actionTd.appendChild(row);

    tr.append(who, cell(s.nim), cell(s.faculty), score, cell(String(s.activeBookings)), statusTd, actionTd);
    rowsEl.appendChild(tr);
  });
}

// ---- Form tambah/ubah (FR-30) ----
function openForm(s) {
  state.editNim = s ? s.nim : null;
  document.getElementById('formTitle').textContent = s ? 'Ubah ' + s.name : 'Tambah mahasiswa';
  nimEl.value = s ? s.nim : '';
  nimEl.disabled = !!s;
  nameEl.value = s ? s.name : '';
  facEl.value = s ? s.faculty : '';
  emailEl.value = s ? s.email : '';
  pwEl.value = '';
  pwWrap.hidden = !!s; // password awal hanya saat membuat akun; setelahnya lewat Reset password
  formNotice.textContent = '';
  formCard.hidden = false;
  formCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
  (s ? nameEl : nimEl).focus();
}

function closeForm() {
  formCard.hidden = true;
  state.editNim = null;
}

function save(e) {
  e.preventDefault();
  const data = { nim: nimEl.value, name: nameEl.value, faculty: facEl.value, email: emailEl.value, password: pwEl.value };
  const editing = state.editNim;
  const request = editing ? API.updateStudent(editing, data) : API.addStudent(data);

  request
    .then(st => {
      closeForm();
      say((editing ? 'Data ' : 'Akun baru ') + st.name + ' berhasil disimpan.', 'ok');
      return load();
    })
    .catch(err => {
      formNotice.className = 'notice err';
      formNotice.textContent = err.message;
    });
}

// ---- Aktif/nonaktif, reset password, hapus ----
function toggle(s) {
  if (!s.active) {
    API.setStudentActive(s.nim, true).then(() => {
      say(s.name + ' diaktifkan kembali dan bisa login.', 'ok');
      return load();
    });
    return;
  }
  const msg = s.activeBookings > 0
    ? s.name + ' punya ' + s.activeBookings + ' booking aktif. Booking itu TIDAK dibatalkan otomatis; tindak lanjuti manual lewat Kelola Booking.\n\nNonaktifkan akun? Mahasiswa tidak akan bisa login.'
    : 'Nonaktifkan akun ' + s.name + '? Mahasiswa tidak akan bisa login.';
  if (!confirm(msg)) return;

  API.setStudentActive(s.nim, false).then(res => {
    say(s.name + ' dinonaktifkan.' + (res.affected ? ' ' + res.affected + ' booking aktif perlu ditindaklanjuti lewat Kelola Booking.' : ''), 'ok');
    return load();
  });
}

function reset(s) {
  if (!confirm('Reset password ' + s.name + '? Password lama tidak berlaku lagi.')) return;
  API.resetPassword(s.nim).then(res => {
    say('Password sementara untuk ' + s.name + ': ' + res.password + '. Catat sekarang, password ini tidak ditampilkan lagi. Mahasiswa bisa menggantinya di halaman Profil setelah login.', 'ok');
  }).catch(err => say(err.message, 'err'));
}

function remove(s) {
  if (!confirm('Hapus data ' + s.name + ' secara permanen? Tindakan ini tidak bisa dibatalkan.')) return;
  API.deleteStudent(s.nim)
    .then(() => { say(s.name + ' dihapus.', 'ok'); return load(); })
    .catch(err => say(err.message, 'err'));
}
