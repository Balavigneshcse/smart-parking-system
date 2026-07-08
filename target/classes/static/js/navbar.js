/* ───── Dynamic Navbar + Sidebar Logic ─────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
  const user = API.user();

  // Mobile hamburger toggle
  const ham = document.querySelector('.hamburger');
  const navLinks = document.querySelector('.nav-links');
  if (ham && navLinks) {
    ham.addEventListener('click', () => navLinks.classList.toggle('open'));
  }

  // Highlight active nav link
  document.querySelectorAll('.sidebar-nav a, .nav-links a').forEach(a => {
    if (a.href === window.location.href) a.classList.add('active');
  });

  // Populate user info placeholders
  document.querySelectorAll('[data-user-name]').forEach(el => {
    el.textContent = user?.name || 'User';
  });
  document.querySelectorAll('[data-user-email]').forEach(el => {
    el.textContent = user?.email || '';
  });

  // Logout buttons
  document.querySelectorAll('.logout-btn').forEach(btn => {
    btn.addEventListener('click', (e) => { e.preventDefault(); API.logout(); });
  });
});
