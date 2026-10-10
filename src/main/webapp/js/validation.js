/**
 * MyFinance 2.0 — Client-Side Form Validation & UX Enhancements
 */

document.addEventListener('DOMContentLoaded', () => {
    // Expense Form Validation
    const expenseForm = document.getElementById('expenseForm');
    if (expenseForm) {
        expenseForm.addEventListener('submit', (e) => {
            const amountInput = expenseForm.querySelector('input[name="amount"]');
            const titleInput = expenseForm.querySelector('input[name="title"]');

            if (titleInput && titleInput.value.trim().length === 0) {
                e.preventDefault();
                alert('Please enter a description or vendor title for this expense.');
                titleInput.focus();
                return;
            }

            if (amountInput && (Number(amountInput.value) <= 0 || isNaN(amountInput.value))) {
                e.preventDefault();
                alert('Please enter a positive expense amount greater than ₹0.');
                amountInput.focus();
                return;
            }
        });
    }

    // Budget Form Validation
    const budgetForm = document.getElementById('budgetForm');
    if (budgetForm) {
        budgetForm.addEventListener('submit', (e) => {
            const limitInput = budgetForm.querySelector('input[name="limitAmount"]');
            const startInput = budgetForm.querySelector('input[name="startDate"]');
            const endInput = budgetForm.querySelector('input[name="endDate"]');

            if (limitInput && Number(limitInput.value) <= 0) {
                e.preventDefault();
                alert('Budget limit must be greater than ₹0.');
                limitInput.focus();
                return;
            }

            if (startInput && endInput && startInput.value > endInput.value) {
                e.preventDefault();
                alert('Budget start date cannot be after the end date.');
                endInput.focus();
                return;
            }
        });
    }

    // Goal Form Validation
    const goalForm = document.getElementById('goalForm');
    if (goalForm) {
        goalForm.addEventListener('submit', (e) => {
            const targetInput = goalForm.querySelector('input[name="targetAmount"]');
            const deadlineInput = goalForm.querySelector('input[name="deadline"]');

            if (targetInput && Number(targetInput.value) <= 0) {
                e.preventDefault();
                alert('Target savings goal amount must be positive.');
                targetInput.focus();
                return;
            }

            if (deadlineInput && deadlineInput.value) {
                const today = new Date().toISOString().split('T')[0];
                if (deadlineInput.value < today) {
                    if (!confirm('The selected deadline date is in the past. Do you still wish to proceed?')) {
                        e.preventDefault();
                        deadlineInput.focus();
                        return;
                    }
                }
            }
        });
    }
});
