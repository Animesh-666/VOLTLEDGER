// Enhancement only: tariff calculations are performed on the Java server.
document.addEventListener('DOMContentLoaded', () => {
  const toggle = document.querySelector('.menu-toggle');
  const menu = document.querySelector('.mobile-nav');
  if (toggle && menu) {
    toggle.addEventListener('click', () => {
      const expanded = toggle.getAttribute('aria-expanded') === 'true';
      toggle.setAttribute('aria-expanded', String(!expanded));
      menu.hidden = expanded;
    });
  }
});

// Keep JavaScript in same-origin assets to comply with Content-Security-Policy.
document.querySelectorAll('[data-action="print"]').forEach(button => {
  button.addEventListener("click", () => window.print());
});
