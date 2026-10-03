// Offline expense entry functionality

let pendingExpenses = [];

// Listen for online/offline events
window.addEventListener('online', handleOnline);
window.addEventListener('offline', handleOffline);

function handleOnline() {
    console.log('Back online');
    syncPendingExpenses();
    hideOfflineBanner();
}

function handleOffline() {
    console.log('Gone offline');
    showOfflineBanner();
}

function showOfflineBanner() {
    const banner = document.createElement('div');
    banner.id = 'offline-banner';
    banner.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        background: #f59e0b;
        color: white;
        padding: 12px;
        text-align: center;
        z-index: 1000;
        font-weight: 500;
    `;
    banner.textContent = 'You are offline. Expenses will be saved and synced when you are back online.';
    document.body.appendChild(banner);
}

function hideOfflineBanner() {
    const banner = document.getElementById('offline-banner');
    if (banner) {
        banner.remove();
    }
}

function saveExpenseOffline(expenseData) {
    pendingExpenses.push(expenseData);
    localStorage.setItem('pendingExpenses', JSON.stringify(pendingExpenses));
    console.log('Expense saved offline:', expenseData);
}

async function syncPendingExpenses() {
    const stored = localStorage.getItem('pendingExpenses');
    if (!stored) return;

    pendingExpenses = JSON.parse(stored);

    if (pendingExpenses.length === 0) return;

    console.log(`Syncing ${pendingExpenses.length} pending expenses...`);

    for (const expense of pendingExpenses) {
        try {
            const response = await fetch('/expenses', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams(expense)
            });

            if (response.ok) {
                console.log('Synced expense:', expense);
            }
        } catch (error) {
            console.error('Failed to sync expense:', error);
        }
    }

    // Clear pending expenses after sync
    pendingExpenses = [];
    localStorage.removeItem('pendingExpenses');
    console.log('All pending expenses synced');
}

// Load pending expenses on page load
window.addEventListener('load', () => {
    const stored = localStorage.getItem('pendingExpenses');
    if (stored) {
        pendingExpenses = JSON.parse(stored);
        if (pendingExpenses.length > 0 && navigator.onLine) {
            syncPendingExpenses();
        }
    }
});
