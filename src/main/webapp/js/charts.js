// Chart drawing functions using HTML5 Canvas API

function drawDonutChart(canvasId, data, colors) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    const radius = Math.min(centerX, centerY) - 20;
    const innerRadius = radius * 0.6;

    let total = 0;
    for (let key in data) {
        total += data[key];
    }

    let startAngle = -Math.PI / 2;

    for (let key in data) {
        const sliceAngle = (data[key] / total) * 2 * Math.PI;
        const endAngle = startAngle + sliceAngle;

        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.arc(centerX, centerY, radius, startAngle, endAngle);
        ctx.closePath();
        ctx.fillStyle = colors[key] || '#f4a261';
        ctx.fill();

        startAngle = endAngle;
    }

    // Draw inner circle to create donut effect
    ctx.beginPath();
    ctx.arc(centerX, centerY, innerRadius, 0, 2 * Math.PI);
    ctx.fillStyle = '#ffffff';
    ctx.fill();
}

function drawBarChart(canvasId, labels, values, color) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const padding = 40;
    const chartWidth = canvas.width - padding * 2;
    const chartHeight = canvas.height - padding * 2;

    const maxValue = Math.max(...values);
    const barWidth = chartWidth / values.length - 10;

    // Draw axes
    ctx.beginPath();
    ctx.moveTo(padding, padding);
    ctx.lineTo(padding, canvas.height - padding);
    ctx.lineTo(canvas.width - padding, canvas.height - padding);
    ctx.strokeStyle = '#eaedf3';
    ctx.stroke();

    // Draw bars
    values.forEach((value, index) => {
        const barHeight = (value / maxValue) * chartHeight;
        const x = padding + index * (chartWidth / values.length) + 5;
        const y = canvas.height - padding - barHeight;

        ctx.fillStyle = color || '#f4a261';
        ctx.fillRect(x, y, barWidth, barHeight);

        // Draw label
        ctx.fillStyle = '#1e1e2e';
        ctx.font = '12px Segoe UI';
        ctx.textAlign = 'center';
        ctx.fillText(labels[index], x + barWidth / 2, canvas.height - padding + 20);
    });
}
