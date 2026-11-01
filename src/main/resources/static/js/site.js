/* Same-origin enhancement only. Form submission, CSRF and bill calculations remain in Java. */
document.addEventListener('DOMContentLoaded', () => {
  const toggle = document.querySelector('.menu-toggle');
  const menu = document.querySelector('.mobile-nav');
  if (toggle && menu) {
    toggle.addEventListener('click', () => {
      const expanded = toggle.getAttribute('aria-expanded') === 'true';
      toggle.setAttribute('aria-expanded', String(!expanded));
      menu.hidden = expanded;
    });
    menu.querySelectorAll('a').forEach(a => a.addEventListener('click', () => {
      menu.hidden = true;
      toggle.setAttribute('aria-expanded', 'false');
    }));
  }
  document.querySelectorAll('[data-action="print"]').forEach(button => {
    button.addEventListener('click', () => window.print());
  });
  if ('IntersectionObserver' in window && !window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    const observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible');
          observer.unobserve(entry.target);
        }
      });
    },{threshold:.08,rootMargin:'0px 0px 30px 0px'});
    document.querySelectorAll('[data-reveal]').forEach(item => {
      if (item.getBoundingClientRect().top < window.innerHeight * .92) item.classList.add('is-visible');
      observer.observe(item);
    });
    document.body.classList.add('js-motion');
  }
});
