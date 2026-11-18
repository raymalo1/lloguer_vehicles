/**
 * Loading overlay per informar l'usuari durant operacions llargues
 * (enviament de correus, etc.) - compleix requisit de fluïdesa.
 */
(function () {
  'use strict';

  function ensureOverlay() {
    var overlay = document.getElementById('global-loading-overlay');
    if (!overlay) {
      overlay = document.createElement('div');
      overlay.id = 'global-loading-overlay';
      overlay.className = 'loading-overlay';
      overlay.setAttribute('role', 'status');
      overlay.setAttribute('aria-live', 'polite');
      overlay.setAttribute('aria-busy', 'true');
      overlay.innerHTML =
        '<div class="spinner-lg" aria-hidden="true"></div>' +
        '<span class="loading-text">Enviant correu... Si us plau, espera</span>';
      document.body.appendChild(overlay);
    }
    return overlay;
  }

  window.showLoading = function (message) {
    var overlay = ensureOverlay();
    var textEl = overlay.querySelector('.loading-text');
    if (textEl && message) {
      textEl.textContent = message;
    }
    overlay.classList.add('active');
    document.body.style.overflow = 'hidden';
  };

  window.hideLoading = function () {
    var overlay = document.getElementById('global-loading-overlay');
    if (overlay) {
      overlay.classList.remove('active');
    }
    document.body.style.overflow = '';
  };

  /**
   * Adjunta el loading a un formulari: es mostra en submit.
   * @param {HTMLFormElement|string} formOrSelector
   * @param {string} [message]
   */
  window.attachLoadingToForm = function (formOrSelector, message) {
    var form = typeof formOrSelector === 'string'
      ? document.querySelector(formOrSelector)
      : formOrSelector;
    if (!form) return;

    form.addEventListener('submit', function () {
      // Només si el formulari és vàlid (HTML5 validation)
      if (form.checkValidity && !form.checkValidity()) {
        return;
      }
      showLoading(message || 'Processant... Si us plau, espera');
    });
  };

  // Auto-attach a formularis amb data-loading="true" o class "js-loading"
  document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('form[data-loading], form.js-loading').forEach(function (form) {
      var msg = form.getAttribute('data-loading-message') || 'Processant... Si us plau, espera';
      attachLoadingToForm(form, msg);
    });
  });
})();
