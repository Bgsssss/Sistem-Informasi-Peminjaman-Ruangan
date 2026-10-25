const session = API.requireRole('admin');

if (session) {
  mountShell(session, 'pengaturan');
  API.getSystemSettings().then(render);
}

function cell(text, cls) {
  const td = document.createElement('td');
  td.textContent = text;
  if (cls) td.className = cls;
  return td;
}

function render(groups) {
  const root = document.getElementById('groups');
  root.innerHTML = '';

  groups.forEach((g, i) => {
    const card = document.createElement('section');
    card.className = 'card' + (i ? ' stack-top' : '');
    const id = 'grp' + i;
    card.setAttribute('aria-labelledby', id);

    const h = document.createElement('h2');
    h.id = id;
    h.textContent = g.title;

    const wrap = document.createElement('div');
    wrap.className = 'tbl-wrap';
    const table = document.createElement('table');
    table.innerHTML = '<thead><tr><th>Parameter</th><th>Nilai saat ini</th><th>Rujukan SRS</th></tr></thead>';
    const body = document.createElement('tbody');
    g.items.forEach(it => {
      const tr = document.createElement('tr');
      tr.append(cell(it.name), cell(it.value, 'val'), cell(it.ref, 'ref'));
      body.appendChild(tr);
    });
    table.appendChild(body);
    wrap.appendChild(table);

    card.append(h, wrap);
    root.appendChild(card);
  });
}
