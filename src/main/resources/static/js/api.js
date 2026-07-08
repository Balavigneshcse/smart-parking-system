/* ───── Smart Parking — Centralised API + Auth Helpers ─────────────────── */
const API = (() => {
  const BASE = '/api';

  function token() { return localStorage.getItem('sp_token'); }
  function user()  { return JSON.parse(localStorage.getItem('sp_user') || 'null'); }
  function role()  { return localStorage.getItem('sp_role'); }

  function headers(auth = true) {
    const h = { 'Content-Type': 'application/json' };
    if (auth && token()) h['Authorization'] = 'Bearer ' + token();
    return h;
  }

  async function req(method, path, body, auth = true) {
    const opts = { method, headers: headers(auth) };
    if (body) opts.body = JSON.stringify(body);
    const res = await fetch(BASE + path, opts);
    let data = null;
    try { data = await res.json(); } catch {}
    if (!res.ok) {
      const msg = data?.message || data?.error || 'Request failed';
      throw new Error(msg);
    }
    return data;
  }

  function saveAuth(data) {
    localStorage.setItem('sp_token', data.token);
    localStorage.setItem('sp_role',  data.role);
    localStorage.setItem('sp_user',  JSON.stringify({
      id: data.userId, name: data.name, email: data.email,
      placeId: data.placeId, stateId: data.stateId
    }));
  }

  function clearAuth() {
    localStorage.removeItem('sp_token');
    localStorage.removeItem('sp_role');
    localStorage.removeItem('sp_user');
  }

  function requireRole(...roles) {
    const r = role();
    if (!r || !roles.includes(r)) {
      clearAuth();
      const map = {
        SUPER_ADMIN:'super', STATE_MANAGER:'state', ADMIN:'admin',
        MANAGER:'manager', SECURITY:'staff', CUSTOMER:'customer'
      };
      const dest = roles[0] ? (map[roles[0]] || 'customer') : 'customer';
      window.location.href = `/${dest}/login.html`;
    }
  }

  function requireAuth() { if (!token()) window.location.href = '/index.html'; }
  function logout() { clearAuth(); window.location.href = '/index.html'; }

  return {
    get:  (path, auth) => req('GET', path, null, auth),
    post: (path, body, auth) => req('POST', path, body, auth),
    put:  (path, body) => req('PUT', path, body),
    del:  (path) => req('DELETE', path),
    token, user, role, saveAuth, clearAuth, requireRole, requireAuth, logout
  };
})();

/* ───── Toast Notifications ────────────────────────────────────────────── */
const Toast = {
  _container: null,
  _init() {
    if (!this._container) {
      this._container = document.getElementById('toast-container');
      if (!this._container) {
        this._container = document.createElement('div');
        this._container.id = 'toast-container';
        document.body.appendChild(this._container);
      }
    }
  },
  show(msg, type = 'info', ms = 4000) {
    this._init();
    const icons = { success:'✓', error:'✕', info:'i', warn:'!' };
    const el = document.createElement('div');
    el.className = `toast ${type}`;
    el.innerHTML = `<span style="font-weight:800">${icons[type]||'i'}</span><span>${msg}</span>`;
    this._container.appendChild(el);
    setTimeout(() => { el.style.opacity = '0'; setTimeout(() => el.remove(), 300); }, ms);
  },
  success: (m) => Toast.show(m, 'success'),
  error:   (m) => Toast.show(m, 'error'),
  info:    (m) => Toast.show(m, 'info'),
};

/* ───── Format Helpers ─────────────────────────────────────────────────── */
const fmt = {
  money: (n) => '₹' + Number(n || 0).toLocaleString('en-IN', {minimumFractionDigits:2}),
  date:  (s) => s || '—',
  plate: (p) => p ? p.replace(/([A-Z]{2})(\d{2})([A-Z]{1,3})(\d{1,4})/, '$1 $2 $3 $4') : '—',
};

/* ───── Counter Animation ──────────────────────────────────────────────── */
function animateCount(el, target, duration = 1200, prefix = '', suffix = '') {
  if (!el || target == null) return;
  const step = target / (duration / 16);
  let cur = 0;
  const t = setInterval(() => {
    cur = Math.min(cur + step, target);
    el.textContent = prefix + Math.round(cur).toLocaleString('en-IN') + suffix;
    if (cur >= target) clearInterval(t);
  }, 16);
}

/* ───── PDF Tax Receipt generator (uses jsPDF, loaded per-page via CDN) ──── */
function downloadReceiptPDF(r) {
  if (!window.jspdf || !window.jspdf.jsPDF) {
    Toast.error('PDF library not loaded on this page.');
    return;
  }
  const { jsPDF } = window.jspdf;
  const doc = new jsPDF({ unit: 'pt', format: 'a4' });
  const W = doc.internal.pageSize.getWidth();
  const money = n => 'INR ' + Number(n || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 });
  let y = 56;

  // Header
  doc.setFont('helvetica', 'bold'); doc.setFontSize(20);
  doc.text('SmartPark', 40, y);
  doc.setFont('helvetica', 'normal'); doc.setFontSize(10); doc.setTextColor(110);
  doc.text('TAX INVOICE / RECEIPT', W - 40, y, { align: 'right' });
  y += 18;
  doc.setTextColor(60); doc.setFontSize(10);
  doc.text(r.placeName || '', 40, y); y += 14;
  if (r.placeAddress) { doc.setTextColor(120); doc.setFontSize(9); doc.text(r.placeAddress, 40, y); y += 12; }
  doc.setTextColor(120); doc.setFontSize(9); doc.text(r.gstin || '', 40, y);

  // Invoice meta (right)
  doc.setTextColor(60); doc.setFontSize(10);
  doc.text('Invoice: ' + (r.invoiceNo || '—'), W - 40, y - 12, { align: 'right' });
  doc.text('Date: ' + (r.issuedAt || ''), W - 40, y, { align: 'right' });

  y += 22;
  doc.setDrawColor(220); doc.line(40, y, W - 40, y); y += 24;

  // Details grid
  doc.setFontSize(10);
  const row = (label, val) => {
    doc.setTextColor(130); doc.text(label, 40, y);
    doc.setTextColor(30); doc.text(String(val || '—'), 200, y);
    y += 18;
  };
  row('Vehicle', (r.plateNumber || '') + '  (' + (r.vehicleType || '') + ')');
  row('Owner', r.ownerName);
  row('Space', r.spaceName);
  row('Entry', r.entryTime);
  row('Exit', r.exitTime);
  row('Duration', r.durationText);
  row('Payment', r.paymentMethod);
  if (r.reservationRef) row('Booking Ref', r.reservationRef);

  y += 6; doc.setDrawColor(220); doc.line(40, y, W - 40, y); y += 26;

  // Charge breakdown
  doc.setFont('helvetica', 'bold'); doc.setFontSize(11); doc.setTextColor(30);
  doc.text('Charge Breakdown', 40, y); y += 20;
  doc.setFont('helvetica', 'normal'); doc.setFontSize(10);
  const line = (label, val, bold) => {
    doc.setFont('helvetica', bold ? 'bold' : 'normal');
    doc.setTextColor(bold ? 20 : 90);
    doc.text(label, 40, y);
    doc.text(money(val), W - 40, y, { align: 'right' });
    y += 18;
  };
  line('Taxable value', r.taxableValue);
  line('CGST (9%)', r.cgst);
  line('SGST (9%)', r.sgst);
  y += 4; doc.setDrawColor(220); doc.line(40, y, W - 40, y); y += 20;
  line('Total Paid (incl. ' + (r.gstRate || 18) + '% GST)', r.total, true);

  y += 30;
  doc.setFont('helvetica', 'italic'); doc.setFontSize(8); doc.setTextColor(150);
  doc.text('This is a computer-generated receipt and does not require a signature.', 40, y);
  y += 12;
  doc.text('Thank you for parking with SmartPark.', 40, y);

  doc.save((r.invoiceNo || 'receipt') + '.pdf');
}

/* ───── Demo-mode auto-login button ────────────────────────────────────── */
/**
 * If the server is in demo mode, injects a one-click auto-login button that
 * fills the email/password fields for the given role and submits.
 * @param {string} role   e.g. 'CUSTOMER','ADMIN','SECURITY','MANAGER','SUPER_ADMIN','STATE_MANAGER'
 * @param {function} onFill  callback(email, password) that fills fields and logs in
 */
async function initDemoLogin(role, onFill) {
  try {
    const cfg = await fetch('/api/config').then(r => r.json());
    if (!cfg.demoMode || !cfg.demoCredentials || !cfg.demoCredentials[role]) return;
    const c = cfg.demoCredentials[role];

    const btn = document.getElementById('demo-login-btn');
    const target = btn || (() => {
      const b = document.createElement('button');
      b.id = 'demo-login-btn';
      b.type = 'button';
      b.className = 'btn btn-secondary';
      b.style.cssText = 'width:100%;margin-top:10px';
      b.innerHTML = '<span class="mi" style="font-size:1.1em">bolt</span> Demo Login';
      // Place it right after the primary login button if present
      const primary = document.getElementById('login-btn');
      if (primary && primary.parentNode) primary.parentNode.insertBefore(b, primary.nextSibling);
      else document.querySelector('.auth-card')?.appendChild(b);
      return b;
    })();

    target.style.display = '';
    target.addEventListener('click', () => onFill(c.email, c.password));
  } catch (e) { /* silent — demo button just won't appear */ }
}

/* ───── Background Mesh (auto-inject) ──────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
  if (!document.querySelector('.bg-mesh')) {
    const mesh = document.createElement('div');
    mesh.className = 'bg-mesh';
    document.body.prepend(mesh);
  }
});
