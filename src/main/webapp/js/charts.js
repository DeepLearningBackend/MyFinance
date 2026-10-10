/**
 * MyFinance 2.0 — Interactive Canvas Charts Engine
 * Vanilla HTML5 Canvas drawing with retina display support and glowing dark-mode aesthetics.
 */

const CATEGORY_COLORS = {
    "Food & Dining": "#f97316",
    "Food": "#f97316",
    "Dining": "#f97316",
    "Shopping": "#ec4899",
    "Transport & Fuel": "#06b6d4",
    "Transport": "#06b6d4",
    "Rent & Housing": "#8b5cf6",
    "Housing": "#8b5cf6",
    "Bills & Utilities": "#eab308",
    "Utilities": "#eab308",
    "Entertainment": "#a855f7",
    "Health & Medical": "#ef4444",
    "Health": "#ef4444",
    "Education": "#3b82f6",
    "Investment & Savings": "#10b981",
    "Investment": "#10b981",
    "Other": "#94a3b8"
};

/**
 * Draws a sleek donut chart on an HTML5 canvas and populates a companion legend.
 *
 * @param {string} canvasId ID of the HTML canvas
 * @param {string} legendId ID of the companion legend container
 * @param {Object} data Key-value map of category -> amount
 */
function renderCategoryDonut(canvasId, legendId, data) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const legend = document.getElementById(legendId);

    // Support High-DPI screens
    const dpr = window.devicePixelRatio || 1;
    const width = 220;
    const height = 220;

    canvas.width = width * dpr;
    canvas.height = height * dpr;
    canvas.style.width = width + 'px';
    canvas.style.height = height + 'px';
    ctx.scale(dpr, dpr);

    const entries = Object.entries(data || {}).filter(([_, val]) => val > 0);
    const total = entries.reduce((sum, [_, val]) => sum + Number(val), 0);

    if (total <= 0 || entries.length === 0) {
        ctx.clearRect(0, 0, width, height);
        ctx.fillStyle = '#64748b';
        ctx.font = '13px Inter, sans-serif';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText('No data available', width / 2, height / 2);
        return;
    }

    const centerX = width / 2;
    const centerY = height / 2;
    const radius = 90;
    const innerRadius = 60;

    let startAngle = -0.5 * Math.PI;

    if (legend) legend.innerHTML = '';

    entries.forEach(([category, amount]) => {
        const val = Number(amount);
        const sliceAngle = (val / total) * 2 * Math.PI;
        const color = CATEGORY_COLORS[category] || '#a855f7';

        // Draw Arc Slice
        ctx.beginPath();
        ctx.arc(centerX, centerY, radius, startAngle, startAngle + sliceAngle);
        ctx.arc(centerX, centerY, innerRadius, startAngle + sliceAngle, startAngle, true);
        ctx.closePath();
        ctx.fillStyle = color;
        ctx.shadowColor = color;
        ctx.shadowBlur = 6;
        ctx.fill();

        // Slice separation line
        ctx.lineWidth = 2;
        ctx.strokeStyle = '#0b0d17';
        ctx.stroke();

        startAngle += sliceAngle;

        // Populate Legend
        if (legend) {
            const pct = ((val / total) * 100).toFixed(0);
            const item = document.createElement('div');
            item.style.display = 'flex';
            item.style.alignItems = 'center';
            item.style.justifyContent = 'space-between';
            item.style.gap = '12px';
            item.style.fontSize = '0.8rem';
            item.style.color = '#e2e8f0';

            item.innerHTML = `
                <div style="display: flex; align-items: center; gap: 8px;">
                    <span style="display: inline-block; width: 10px; height: 10px; border-radius: 50%; background: ${color}; box-shadow: 0 0 6px ${color};"></span>
                    <span style="font-weight: 500;">${category}</span>
                </div>
                <div style="font-family: 'Space Grotesk', sans-serif; font-weight: 700; color: #ffffff;">
                    ₹${val.toLocaleString('en-IN')} <span style="font-size: 0.7rem; color: #94a3b8;">(${pct}%)</span>
                </div>
            `;
            legend.appendChild(item);
        }
    });

    // Draw Center Summary Label
    ctx.shadowBlur = 0;
    ctx.fillStyle = '#94a3b8';
    ctx.font = '10px Inter, sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('TOTAL', centerX, centerY - 10);

    ctx.fillStyle = '#ffffff';
    ctx.font = 'bold 13px Space Grotesk, sans-serif';
    ctx.fillText('₹' + Math.round(total).toLocaleString('en-IN'), centerX, centerY + 8);
}
