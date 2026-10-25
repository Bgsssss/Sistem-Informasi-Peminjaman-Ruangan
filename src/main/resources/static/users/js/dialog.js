// Dialog konfirmasi modern (pengganti confirm() bawaan browser).
// Memakai elemen <dialog> + variabel warna dari app.css. Pemakaian:
//   if (!(await Dialog.confirm({ title: '...', message: '...', confirmText: 'Hapus', danger: true }))) return;
const Dialog = (() => {
  const style = document.createElement('style');
  style.textContent = `
    .dlg{border:1px solid var(--line);border-radius:16px;background:#0d0f12;color:var(--text);font-family:var(--font);padding:0;width:min(92vw,440px);box-shadow:0 24px 80px rgba(0,0,0,.6)}
    .dlg::backdrop{background:rgba(0,0,0,.65);backdrop-filter:blur(4px)}
    .dlg[open]{animation:dlgIn .16s ease-out}
    @keyframes dlgIn{from{opacity:0;transform:translateY(8px) scale(.98)}to{opacity:1;transform:none}}
    .dlg form{margin:0}
    .dlg-body{padding:22px 22px 6px}
    .dlg h2{margin:0 0 10px;font-size:1.05rem}
    .dlg p{margin:0 0 10px;color:var(--muted);font-size:.9rem;line-height:1.55}
    .dlg-actions{display:flex;justify-content:flex-end;gap:10px;padding:14px 22px 20px}
    .dlg-btn{padding:9px 18px;border-radius:10px;border:1px solid var(--line);background:transparent;color:var(--text);font:inherit;font-size:.875rem;cursor:pointer}
    .dlg-btn:hover{border-color:var(--text)}
    .dlg-btn.ok{background:var(--accent);border-color:var(--accent);color:#001014;font-weight:600}
    .dlg-btn.danger{background:var(--booked);border-color:var(--booked);color:#fff;font-weight:600}
    .dlg-btn:focus-visible{outline:2px solid var(--accent);outline-offset:2px}
    @media (prefers-reduced-motion:reduce){.dlg[open]{animation:none}}`;
  document.head.appendChild(style);

  function button(text, cls, value) {
    const b = document.createElement('button');
    b.type = 'submit'; b.className = cls; b.value = value; b.textContent = text;
    return b;
  }

  function confirm({ title = 'Konfirmasi', message = '', confirmText = 'Ya, lanjutkan', cancelText = 'Batal', danger = false } = {}) {
    const dlg = document.createElement('dialog');
    if (typeof dlg.showModal !== 'function') return Promise.resolve(window.confirm(message));
    return new Promise(resolve => {
      dlg.className = 'dlg';
      dlg.setAttribute('aria-labelledby', 'dlgTitle');
      const form = document.createElement('form');
      form.method = 'dialog';
      const body = document.createElement('div');
      body.className = 'dlg-body';
      const h = document.createElement('h2');
      h.id = 'dlgTitle'; h.textContent = title;
      body.appendChild(h);
      String(message).split('\n\n').forEach(t => { const p = document.createElement('p'); p.textContent = t; body.appendChild(p); });
      const actions = document.createElement('div');
      actions.className = 'dlg-actions';
      const no = button(cancelText, 'dlg-btn', 'cancel');
      const yes = button(confirmText, 'dlg-btn ' + (danger ? 'danger' : 'ok'), 'ok');
      actions.append(no, yes);
      form.append(body, actions);
      dlg.appendChild(form);
      dlg.addEventListener('click', e => { if (e.target === dlg) dlg.close('cancel'); }); // klik area gelap = batal
      dlg.addEventListener('close', () => { const ok = dlg.returnValue === 'ok'; dlg.remove(); resolve(ok); }); // Esc = batal
      document.body.appendChild(dlg);
      dlg.showModal();
      (danger ? no : yes).focus(); // aksi berbahaya: fokus awal di "Batal"
    });
  }
  return { confirm };
})();