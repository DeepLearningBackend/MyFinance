/**
 * MyFinance — Main Client UI Utility
 * Provides:
 *  1. Smooth Counter Animation (for dashboard stat cards and hero numbers)
 *  2. Modern Toast Notification System (auto-dismissing, slide-in alerts)
 *  3. Global Flash Feedback Dismissal
 */

(function () {
    'use strict';

    // ── Toast Notification Engine ───────────────────────────────────────────
    const ToastManager = {
        container: null,

        init() {
            if (!this.container) {
                this.container = document.createElement('div');
                this.container.id = 'toastContainer';
                this.container.className = 'toast-container';
                document.body.appendChild(this.container);
            }

            // Auto-convert existing flash banners into modern dismissible notifications
            const banners = document.querySelectorAll('.alert-banner');
            banners.forEach(banner => {
                const isSuccess = banner.classList.contains('alert-banner-success');
                const isError = banner.classList.contains('alert-banner-danger') || banner.classList.contains('alert-banner-error');
                const text = banner.textContent.trim();
                if (text) {
                    setTimeout(() => {
                        banner.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                        banner.style.opacity = '0';
                        banner.style.transform = 'translateY(-10px)';
                        setTimeout(() => banner.remove(), 500);
                    }, 5000);
                }
            });
        },

        show(message, type = 'info', duration = 4000) {
            this.init();

            const toast = document.createElement('div');
            toast.className = `toast-item toast-${type}`;

            let icon = 'ℹ️';
            if (type === 'success') icon = '✨';
            else if (type === 'error' || type === 'danger') icon = '⚠️';
            else if (type === 'warning') icon = '⚡';

            toast.innerHTML = `
                <div class="toast-icon">${icon}</div>
                <div class="toast-body">${this.escape(message)}</div>
                <button class="toast-close" aria-label="Close">&times;</button>
            `;

            this.container.appendChild(toast);

            // Trigger animation frame
            requestAnimationFrame(() => {
                toast.classList.add('toast-visible');
            });

            // Close button
            const closeBtn = toast.querySelector('.toast-close');
            closeBtn.addEventListener('click', () => this.dismiss(toast));

            // Auto-dismiss
            if (duration > 0) {
                setTimeout(() => this.dismiss(toast), duration);
            }
        },

        dismiss(toast) {
            toast.classList.remove('toast-visible');
            toast.classList.add('toast-hiding');
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 300);
        },

        escape(str) {
            if (!str) return '';
            const div = document.createElement('div');
            div.textContent = str;
            return div.innerHTML;
        }
    };

    // ── Number Counter Animation ────────────────────────────────────────────
    const CounterAnimator = {
        init() {
            const elements = document.querySelectorAll('[data-counter], .stat-value, .hero-amount, .counter');
            elements.forEach(el => this.observeOrAnimate(el));
        },

        observeOrAnimate(el) {
            if ('IntersectionObserver' in window) {
                const observer = new IntersectionObserver((entries, obs) => {
                    entries.forEach(entry => {
                        if (entry.isIntersecting) {
                            this.animate(el);
                            obs.unobserve(el);
                        }
                    });
                }, { threshold: 0.2 });
                observer.observe(el);
            } else {
                this.animate(el);
            }
        },

        animate(el) {
            const rawText = el.getAttribute('data-value') || el.innerText.trim();
            // Match currency symbols, numbers, decimals
            const match = rawText.match(/^([^\d\-\.]*)([\d,]+(?:\.\d+)?)(.*)$/);
            if (!match) return;

            const prefix = match[1] || '';
            const numStr = match[2].replace(/,/g, '');
            const suffix = match[3] || '';

            const targetVal = parseFloat(numStr);
            if (isNaN(targetVal)) return;

            const hasDecimals = match[2].includes('.');
            const decimalPlaces = hasDecimals ? (match[2].split('.')[1].length || 2) : 0;
            const duration = 1200; // 1.2s
            const startTime = performance.now();

            const step = (currentTime) => {
                const elapsed = currentTime - startTime;
                const progress = Math.min(elapsed / duration, 1);
                // Ease out cubic
                const easeOut = 1 - Math.pow(1 - progress, 3);
                const currentVal = targetVal * easeOut;

                let formatted = currentVal.toLocaleString('en-IN', {
                    minimumFractionDigits: decimalPlaces,
                    maximumFractionDigits: decimalPlaces
                });

                el.innerText = `${prefix}${formatted}${suffix}`;

                if (progress < 1) {
                    requestAnimationFrame(step);
                } else {
                    // Final exact render
                    let finalFormatted = targetVal.toLocaleString('en-IN', {
                        minimumFractionDigits: decimalPlaces,
                        maximumFractionDigits: decimalPlaces
                    });
                    el.innerText = `${prefix}${finalFormatted}${suffix}`;
                }
            };

            requestAnimationFrame(step);
        }
    };

    // ── Global API Exports ──────────────────────────────────────────────────
    window.showToast = function (message, type = 'info', duration = 4000) {
        ToastManager.show(message, type, duration);
    };

    window.MyFinance = window.MyFinance || {};
    window.MyFinance.toast = window.showToast;
    window.MyFinance.animateCounters = () => CounterAnimator.init();

    // ── Document Ready ──────────────────────────────────────────────────────
    document.addEventListener('DOMContentLoaded', () => {
        ToastManager.init();
        CounterAnimator.init();
    });

})();
