const CONFIG = {
  openHour: 8, closeHour: 21, openNextDayAt: 20, minScore: 60, maxActive: 2,
  maxHoursPerDay: 3, lateCancelHours: 2, lateToleranceMin: 10,
  penalty: { noShow: -15, lateCancel: -5, onTime: 5 }
};
const pad = n => String(n).padStart(2, '0');
const now = () => new Date();
function toISO(d) { return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()); }
function todayISO() { return toISO(now()); }
function addDays(n) { const d = now(); d.setDate(d.getDate() + n); return toISO(d); }
function hhmm(d) { return pad(d.getHours()) + ':' + pad(d.getMinutes()); }
const clampScore = s => Math.max(0, Math.min(100, s));
function placeholderPhoto(room, i) {
  let cells = '';
  for (let k = 0; k < 40; k++) cells += '<rect x="' + (40 + (k % 8) * 70) + '" y="' + (60 + Math.floor(k / 8) * 34) + '" width="56" height="22" rx="3" fill="none" stroke="#2a2a2e"/>';
  const svg = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 400"><rect width="640" height="400" fill="#050505"/>' + cells +
    '<text x="320" y="285" fill="#f2f4f8" font-family="sans-serif" font-size="26" text-anchor="middle">' + room.name + '</text>' +
    '<text x="320" y="322" fill="#8d939e" font-family="sans-serif" font-size="16" text-anchor="middle">Foto ' + (i + 1) + ' belum tersedia</text></svg>';
  return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
}

const TYPES = { kecil: { label: 'Diskusi Kelompok Kecil', min: 2, max: 4 }, besar: { label: 'Diskusi Kelompok Besar', min: 4, max: 8 } };
const T2B = { kecil: 'SMALL', besar: 'LARGE' }, B2T = { SMALL: 'kecil', LARGE: 'besar' };
const hm = t => (t ? String(t).slice(0, 5) : '');
const MSG = {
  SLOT_ALREADY_BOOKED: 'Slot ini sudah terisi. Pilih ruangan atau waktu lain.',
  TRUST_SCORE_TOO_LOW: 'Trust score kamu di bawah batas minimal, jadi booking baru dinonaktifkan. Hubungi admin fasilitas kampus.',
  ACTIVE_BOOKING_LIMIT_REACHED: 'Kamu sudah mencapai batas booking aktif. Batalkan salah satu untuk membuat booking baru.',
  DAILY_HOURS_LIMIT_EXCEEDED: 'Batas peminjaman per hari sudah tercapai untuk tanggal ini.',
  DATE_NOT_OPEN: 'Tanggal ini belum dibuka untuk booking.', DATE_IN_PAST: 'Tanggal ini sudah lewat.',
  SLOT_ALREADY_STARTED: 'Slot ini sudah lewat.', ROOM_INACTIVE: 'Ruangan sedang tidak aktif.', ROOM_NOT_FOUND: 'Ruangan tidak ditemukan.',
  CANCELLATION_AFTER_START: 'Booking yang sudah dimulai tidak bisa dibatalkan.',
  CHECK_IN_TOO_EARLY: 'Check-in belum dibuka.', CHECK_IN_WINDOW_CLOSED: 'Batas check-in sudah lewat.',
  INVALID_BOOKING_STATUS: 'Booking ini tidak bisa diproses.', ACCOUNT_INACTIVE: 'Akun kamu tidak aktif. Hubungi admin fasilitas kampus.'
};
const errMsg = e => (e && MSG[e.code]) || (e && e.message) || 'Terjadi kesalahan.';
const call = p => p.catch(e => { throw (e && MSG[e.code]) ? new Error(MSG[e.code]) : e; });
const get = path => call(ApiClient.get(path));

const _can = { ok: true, reason: '' };
let _pp = null, _rooms = null;
// Profil dipakai banyak method; permintaan beruntun digabung. Sekaligus memperbarui status canBook.
function prof() {
  if (!_pp) _pp = get('/profile').finally(() => setTimeout(() => { _pp = null; }, 1000));
  return _pp.then(p => { CONFIG.minScore = p.minScoreToBook; Object.assign(_can, { ok: p.canBook, reason: p.blockedMessage || '' }); return p; });
}
const roomList = () => _rooms ? Promise.resolve(_rooms) : get('/rooms').then(r => (_rooms = r));

function mapB(b) {
  const s = b.status;
  return {
    id: b.id, roomId: b.roomId, room: b.roomName, date: b.date, start: hm(b.startTime), end: hm(b.endTime),
    status: s === 'COMPLETED' ? 'selesai' : s.startsWith('CANCELLED') ? 'dibatalkan' : 'aktif',
    checkedIn: s === 'ATTENDED' || s === 'COMPLETED' || !!b.checkedInAt,
    cancelledBy: { CANCELLED_BY_USER: 'mahasiswa', CANCELLED_BY_SYSTEM: 'sistem', CANCELLED_BY_ADMIN: 'admin' }[s],
    cancelType: s === 'CANCELLED_BY_SYSTEM' ? 'no-show' : s === 'CANCELLED_BY_USER' ? 'normal' : undefined,
    cancelReason: b.cancelReason, canCancel: !!b.canCancel, canCheckIn: !!b.canCheckIn,
    checkInDeadline: b.checkInDeadline, penalty: b.scorePenaltyIfCancelled || 0
  };
}
const bookings = () => get('/bookings').then(l => l.map(mapB));
const SLOT = { AVAILABLE: 'ok', BOOKED: 'booked', PAST: 'passed' };
const SCORE_TYPE = { NO_SHOW: 'no-show', LATE_CANCELLATION: 'cancel-mendadak', COMPLETED_ON_TIME: 'selesai', ADMIN_ADJUSTMENT: 'admin', INITIAL_SCORE: 'awal' };

// ---- Pembantu admin ----
const aStudents = () => get('/admin/students?size=500').then(p => p.content);
const findStudent = nim => aStudents().then(l => l.find(s => s.studentNumber === nim) || Promise.reject(new Error('Mahasiswa tidak ditemukan.')));
// Hitung booking aktif (ACTIVE + ATTENDED) per kunci (studentNumber / roomId)
const activeCount = key => Promise.all([get('/admin/bookings?status=ACTIVE&size=100'), get('/admin/bookings?status=ATTENDED&size=100')]).then(([a, b]) => {
  const m = {}; a.content.concat(b.content).forEach(x => { m[x[key]] = (m[x[key]] || 0) + 1; }); return m;
});
function tempPassword() {
  const c = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789', a = new Uint32Array(12);
  crypto.getRandomValues(a);
  return Array.from(a, x => c[x % c.length]).join('');
}
const ST = s => ({
  status: s === 'COMPLETED' ? 'selesai' : s.startsWith('CANCELLED') ? 'dibatalkan' : 'aktif',
  checkedIn: s === 'ATTENDED' || s === 'COMPLETED',
  cancelledBy: { CANCELLED_BY_USER: 'mahasiswa', CANCELLED_BY_SYSTEM: 'sistem', CANCELLED_BY_ADMIN: 'admin' }[s],
  cancelType: s === 'CANCELLED_BY_SYSTEM' ? 'no-show' : s === 'CANCELLED_BY_USER' ? 'normal' : undefined
});
const mapAB = b => Object.assign({ id: b.id, roomId: b.roomId, room: b.roomName, date: b.date, start: hm(b.startTime), end: hm(b.endTime),
  studentName: b.studentName, nim: b.studentNumber, cancelReason: b.cancelReason, cancelledAt: b.cancelledAt, cancelledByName: b.cancelledBy }, ST(b.status));
const mapAR = (r, m) => ({ id: r.id, name: r.name, type: B2T[r.type], typeLabel: TYPES[B2T[r.type]].label, capacity: r.capacity, location: r.location,
  facilities: r.facilities || [], photoUrl: r.photo || '', active: r.active, activeBookings: m[r.id] || 0 });
const roomBody = (d, active) => ({ name: d.name.trim(), type: T2B[d.type], location: d.location.trim(), capacity: Number(d.capacity),
  facilities: d.facilities.map(x => x.trim()).filter(Boolean), photo: (d.photoUrl || '').trim() || null, active });

const API = {
  errorMessage: errMsg,
  session() { try { return JSON.parse(sessionStorage.getItem('session')); } catch (e) { return null; } },
  requireRole(role) {
    const s = this.session();
    if (!s || s.role !== role || !AuthService.isLoggedIn()) { location.replace(APP_CONFIG.PAGES.LOGIN); return null; }
    return s;
  },
  logout() { AuthService.clearSession(); location.replace(APP_CONFIG.PAGES.LOGIN); },
  // Dipanggil setelah AuthService.login berhasil (dari index).
  async startSession(username, backendRole) {
    let s = { username, name: 'Admin', role: 'admin', nim: '' };
    if (backendRole !== 'ADMIN') { const p = await prof(); s = { username, name: p.name, role: 'mahasiswa', nim: p.studentNumber }; }
    sessionStorage.setItem('session', JSON.stringify(s));
  },

  getConfig() { return CONFIG; },
  getRoomTypes() { return TYPES; },
  getSlots() {
    const slots = [];
    for (let h = CONFIG.openHour; h < CONFIG.closeHour; h++) slots.push({ start: pad(h) + ':00', end: pad(h + 1) + ':00' });
    return slots;
  },
  // Hanya untuk tampilan menu tanggal; backend tetap menolak tanggal yang belum dibuka (DATE_NOT_OPEN).
  getBookableDates() {
    return [{ value: todayISO(), label: 'Hari ini', open: true },
            { value: addDays(1), label: 'Besok', open: now().getHours() >= CONFIG.openNextDayAt }];
  },
  isSlotPassed(date, start) { return new Date(date + 'T' + start + ':00') <= now(); },
  canBook() { return _can; },

  getStudent() {
    return prof().then(p => ({ nim: p.studentNumber, name: p.name, faculty: p.faculty, email: p.email, trustScore: p.trustScore, maxScore: 100 }));
  },
  getTrustHistory() {
    return prof().then(p => (p.scoreHistory || []).map(e => ({ at: e.createdAt, type: SCORE_TYPE[e.type] || e.type, delta: e.delta, scoreAfter: e.scoreAfter }))
      .sort((a, b) => String(b.at).localeCompare(String(a.at))));
  },
  getViolations() {
    return Promise.all([prof(), bookings()]).then(([p, bs]) => {
      const nos = bs.filter(b => b.cancelType === 'no-show').map(b => ({ id: b.id, room: b.room, date: b.date, start: b.start, end: b.end, type: 'no-show', delta: CONFIG.penalty.noShow }));
      const late = (p.scoreHistory || []).filter(e => e.type === 'LATE_CANCELLATION').map(e => ({ id: 'l' + e.createdAt, room: e.note || 'Pembatalan mendadak', date: String(e.createdAt).slice(0, 10), start: '', end: '', type: 'cancel-mendadak', delta: e.delta }));
      return nos.concat(late).sort((a, b) => b.date.localeCompare(a.date));
    });
  },
  getStudentDashboard() {
    return Promise.all([this.getStudent(), bookings()]).then(([student, bs]) => ({
      student, canBook: Object.assign({}, _can),
      bookings: bs.filter(b => b.status === 'aktif').sort((a, b) => (a.date + a.start).localeCompare(b.date + b.start))
    }));
  },

  getAvailability(date, start, type) {
    const qs = new URLSearchParams({ date }); if (type) qs.set('type', T2B[type]);
    return Promise.all([get('/availability?' + qs), roomList(), prof()]).then(([a, rs]) => a.rooms.map(r => {
      const sl = r.slots.find(s => hm(s.startTime) === start), info = rs.find(x => x.id === r.roomId) || {};
      return { id: r.roomId, name: r.name, type: B2T[info.type], capacity: r.capacity, location: r.location, status: sl && sl.status === 'AVAILABLE' ? 'ok' : 'booked' };
    }));
  },
  getRoom(id) {
    return get('/rooms/' + encodeURIComponent(id)).then(r => {
      const room = { id: r.id, name: r.name, type: B2T[r.type], capacity: r.capacity, location: r.location, facilities: r.facilities || [], active: r.active };
      return Object.assign(room, { typeLabel: TYPES[room.type].label, photos: r.photo ? [r.photo] : [placeholderPhoto(room, 0)] });
    });
  },
  getRoomDay(roomId, date) {
    return get('/availability?' + new URLSearchParams({ date })).then(a => {
      const r = a.rooms.find(x => String(x.roomId) === String(roomId));
      return r ? r.slots.map(s => ({ start: hm(s.startTime), end: hm(s.endTime), status: SLOT[s.status] || 'booked' })) : [];
    });
  },

  createBooking(roomId, date, start) { return call(ApiClient.post('/bookings', { roomId, date, startTime: start })).then(mapB); },
  getHistory() { return bookings().then(l => l.sort((a, b) => (b.date + b.start).localeCompare(a.date + a.start))); },
  hasStarted(b) { return !b.canCancel; },
  isLateCancel(b) { return (b.penalty || 0) > 0; },
  cancelBooking(id) {
    return call(ApiClient.patch('/bookings/' + encodeURIComponent(id) + '/cancel')).then(r => ({ late: r.scoreChange < 0, penalty: r.scoreChange }));
  },
  getCheckinList() {
    const tol = CONFIG.lateToleranceMin * 60000;
    return bookings().then(l => l.filter(b => b.status === 'aktif' && b.date === todayISO()).sort((a, b) => a.start.localeCompare(b.start)).map(b => {
      const st = new Date(b.date + 'T' + b.start + ':00');
      return Object.assign({}, b, {
        state: b.checkedIn ? 'sudah' : b.canCheckIn ? 'buka' : 'belum',
        opensAt: hhmm(new Date(st.getTime() - tol)),
        closesAt: b.checkInDeadline ? hm(String(b.checkInDeadline).split('T')[1]) : hhmm(new Date(st.getTime() + tol))
      });
    }));
  },
  checkIn(id) { return call(ApiClient.post('/bookings/' + encodeURIComponent(id) + '/check-in')).then(() => ({})); },

  // ASUMSI nama field ChangePasswordRequest (dua nama konfirmasi dikirim; yang tak dikenal diabaikan Jackson). Cocokkan dengan DTO Anda.
  changePassword(current, next, confirm) {
    if (next === current) return Promise.reject(new Error('Kata sandi baru harus berbeda dari yang lama.'));
    if (next !== confirm) return Promise.reject(new Error('Konfirmasi kata sandi tidak cocok.'));
    return call(ApiClient.post('/users/me/password', {
    currentPassword: current,
    newPassword: next,
    confirmNewPassword: confirm,
    confirmPassword: confirm })).then(() => undefined);
  },
  // ===================== ADMIN =====================
  getAdminDashboard() {
    return Promise.all([get('/admin/dashboard'), get('/admin/bookings?' + new URLSearchParams({ date: todayISO(), size: 100 })), get('/admin/reports?period=weekly')]).then(([d, pg, rep]) => {
      const bs = pg.content.map(mapAB).sort((a, b) => a.start.localeCompare(b.start));
      const n = s => bs.filter(b => b.status === s).length;
      return {
        date: d.date, total: d.totalBookingsToday, aktif: n('aktif'), selesai: n('selesai'), dibatalkan: n('dibatalkan'),
        occupancy: { percent: Math.round(d.occupancyTodayPercent), filled: bs.filter(b => b.status !== 'dibatalkan').length, total: d.activeRooms * (CONFIG.closeHour - CONFIG.openHour) },
        // 7 hari terakhir tanpa booking dibatalkan: pendekatan dari laporan mingguan
        topRooms: rep.rows.map(r => ({ name: r.room, total: r.totalBookings - r.totalNoShows - r.totalOtherCancellations })).filter(r => r.total > 0).sort((a, b) => b.total - a.total).slice(0, 5),
        bookings: bs
      };
    });
  },

  // ---- Mahasiswa (backend memakai id numerik; UI memakai NIM) ----
  getAdminStudents() {
    return Promise.all([aStudents(), activeCount('studentNumber')]).then(([l, m]) => l.map(s => ({
      id: s.id, nim: s.studentNumber, name: s.name, faculty: s.faculty, email: s.email, active: s.active, trustScore: s.trustScore, activeBookings: m[s.studentNumber] || 0 })));
  },
  addStudent(d) { return call(ApiClient.post('/admin/students', { studentNumber: d.nim.trim(), name: d.name.trim(), faculty: d.faculty.trim(), email: d.email.trim(), password: d.password, active: true })); },
  updateStudent(nim, d) {
    return findStudent(nim).then(s => call(ApiClient.put('/admin/students/' + s.id, { studentNumber: s.studentNumber, name: d.name.trim(), faculty: d.faculty.trim(), email: d.email.trim(), active: s.active })));
  },
  setStudentActive(nim, active) {
    return Promise.all([findStudent(nim), activeCount('studentNumber')]).then(([s, m]) =>
      call(ApiClient.put('/admin/students/' + s.id, { studentNumber: s.studentNumber, name: s.name, faculty: s.faculty, email: s.email, active })).then(() => ({ affected: active ? 0 : (m[nim] || 0) })));
  },
  // Backend menerima password baru; password sementara dibuat di browser dan hanya ditampilkan sekali.
  resetPassword(nim) {
    const pw = tempPassword();
    return findStudent(nim).then(s => call(ApiClient.patch('/admin/students/' + s.id + '/password', { newPassword: pw }))).then(() => ({ password: pw }));
  },
  deleteStudent(nim) { return findStudent(nim).then(s => call(ApiClient.delete('/admin/students/' + s.id))); },

  // ---- Ruangan ----
  getAdminRooms() { return Promise.all([get('/admin/rooms'), activeCount('roomId')]).then(([l, m]) => l.map(r => mapAR(r, m))); },
  addRoom(d) { return call(ApiClient.post('/admin/rooms', roomBody(d, true))).then(x => x.room); },
  updateRoom(id, d) {
    return get('/admin/rooms').then(l => { const r = l.find(x => String(x.id) === String(id)); return call(ApiClient.put('/admin/rooms/' + id, roomBody(d, r ? r.active : true))); }).then(x => x.room);
  },
  setRoomActive(id, active) {
    return get('/admin/rooms').then(l => {
      const r = l.find(x => String(x.id) === String(id));
      if (!r) throw new Error('Ruangan tidak ditemukan.');
      return call(ApiClient.put('/admin/rooms/' + id, { name: r.name, type: r.type, location: r.location, capacity: r.capacity, facilities: r.facilities, photo: r.photo, active }));
    }).then(x => ({ affected: x.affectedActiveBookings }));
  },
  deleteRoom(id) { return call(ApiClient.delete('/admin/rooms/' + id)); },

  // ---- Booking ----
  getAdminBookings({ date, roomId, status } = {}) {
    const qs = new URLSearchParams({ size: 100 });
    if (date) qs.set('date', date);
    if (roomId) qs.set('room', roomId);
    return get('/admin/bookings?' + qs).then(p => p.content.map(mapAB).filter(b => !status || b.status === status));
  },
  adminCancelBooking(id, reason) {
    if (!reason || !reason.trim()) return Promise.reject(new Error('Alasan pembatalan wajib diisi.'));
    return ApiClient.patch('/admin/bookings/' + id + '/cancel', { reason: reason.trim() }).catch(e => {
      if (e && e.code === 'INVALID_BOOKING_STATUS') throw new Error('Booking yang sudah check-in, selesai, atau dibatalkan tidak bisa dibatalkan admin.');
      throw (e && MSG[e.code]) ? new Error(MSG[e.code]) : e;
    });
  },
  getAuditLog() {
    return get('/admin/bookings?status=CANCELLED_BY_ADMIN&size=10').then(p => p.content.map(mapAB).map(b => ({
      room: b.room, date: b.date, start: b.start, end: b.end, student: b.studentName, admin: b.cancelledByName || 'admin', at: b.cancelledAt || (b.date + 'T00:00:00'), reason: b.cancelReason || '-' })));
  },

  // ---- Pelanggaran dan skor ----
  getViolationLog() {
    return get('/admin/violations?sort=asc').then(l => l.map(v => ({ id: v.studentId, nim: v.studentNumber, name: v.name, faculty: v.faculty, trustScore: v.trustScore, noShow: v.noShowCount, lateCancel: v.lateCancellationCount, last: v.lastViolationAt || null })));
  },
  // Riwayat skor per mahasiswa belum punya endpoint admin; dicoba /score-history dan kosong jika belum ada.
  getStudentLog(nim) {
    return findStudent(nim).then(s => Promise.all([
      get('/admin/bookings?status=CANCELLED_BY_SYSTEM&size=100'),
      call(ApiClient.get('/admin/students/' + s.id + '/score-history')).catch(() => [])
    ]).then(([pg, h]) => ({
      student: { nim: s.studentNumber, name: s.name, faculty: s.faculty, trustScore: s.trustScore },
      history: (h || []).map(e => ({ at: e.createdAt, type: SCORE_TYPE[e.type] || e.type, delta: e.delta, scoreAfter: e.scoreAfter, reason: e.note })),
      violations: pg.content.filter(b => b.studentNumber === nim).map(b => ({ type: 'no-show', room: b.roomName, date: b.date, start: hm(b.startTime), end: hm(b.endTime), delta: CONFIG.penalty.noShow }))
    })));
  },
  // UI mengirim selisih; backend memakai skor akhir (0-100) -> dihitung dari skor saat ini.
  adjustScore(nim, delta, reason) {
    const d = Number(delta);
    if (!Number.isInteger(d) || d === 0) return Promise.reject(new Error('Isi perubahan skor berupa bilangan bulat, bukan 0.'));
    if (!reason || !reason.trim()) return Promise.reject(new Error('Alasan penyesuaian wajib diisi.'));
    return findStudent(nim).then(s => call(ApiClient.patch('/admin/trust-scores/' + s.id, { newScore: clampScore(s.trustScore + d), note: reason.trim() })))
      .then(r => ({ before: r.oldScore, after: r.newScore }));
  },

  // ---- Laporan dan pengaturan ----
  getUsageReport(period) {
    const P = { harian: 'daily', mingguan: 'weekly', bulanan: 'monthly' };
    return get('/admin/reports?period=' + P[period]).then(r => {
      const rows = r.rows.map(x => {
        const dib = x.totalNoShows + x.totalOtherCancellations;
        return { room: x.room, typeLabel: (TYPES[B2T[x.type]] || {}).label || x.type, active: true, total: x.totalBookings, selesai: x.totalAttended,
          dibatalkan: dib, noShow: x.totalNoShows, aktif: Math.max(0, x.totalBookings - x.totalAttended - dib) };
      });
      const sum = k => rows.reduce((n, x) => n + x[k], 0);
      return { period, from: r.from, to: r.to, rows, totals: { total: sum('total'), selesai: sum('selesai'), aktif: sum('aktif'), dibatalkan: sum('dibatalkan'), noShow: sum('noShow') } };
    });
  },
  // Parameter ditetapkan di backend; halaman ini hanya baca.
  getSystemSettings() {
    const C = CONFIG, sgn = n => (n > 0 ? '+' : '') + n;
    return Promise.resolve([
      { title: 'Jadwal dan slot', items: [
        { name: 'Jam operasional', value: pad(C.openHour) + '.00 - ' + pad(C.closeHour) + '.00 (slot 1 jam)', ref: 'FR-7' },
        { name: 'Tanggal H+1 dibuka', value: 'Pukul ' + C.openNextDayAt + '.00', ref: 'FR-8' } ] },
      { title: 'Aturan booking', items: [
        { name: 'Skor minimal untuk booking', value: String(C.minScore), ref: 'FR-24' },
        { name: 'Maksimal booking aktif', value: String(C.maxActive), ref: 'FR-18' },
        { name: 'Maksimal jam per hari', value: String(C.maxHoursPerDay) + ' jam', ref: 'FR-19' } ] },
      { title: 'Check-in dan sanksi', items: [
        { name: 'Jendela check-in', value: C.lateToleranceMin + ' menit sebelum sampai ' + C.lateToleranceMin + ' menit sesudah mulai', ref: 'FR-15' },
        { name: 'Batal mendadak', value: 'Kurang dari ' + C.lateCancelHours + ' jam sebelum mulai: ' + sgn(C.penalty.lateCancel) + ' poin', ref: 'FR-14, FR-22' },
        { name: 'No-show', value: sgn(C.penalty.noShow) + ' poin', ref: 'FR-23' },
        { name: 'Booking selesai tepat waktu', value: sgn(C.penalty.onTime) + ' poin', ref: 'FR-22' } ] }
    ]);
  }
};
