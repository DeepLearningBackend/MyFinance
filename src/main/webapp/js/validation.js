// Frontend form validation

function validateExpenseForm(form) {
    const title = form.querySelector('[name="title"]').value.trim();
    const amount = parseFloat(form.querySelector('[name="amount"]').value);
    const category = form.querySelector('[name="category"]').value;
    const expenseDate = form.querySelector('[name="expenseDate"]').value;

    clearErrors(form);

    let isValid = true;

    if (!title) {
        showError(form, 'title', 'Title is required');
        isValid = false;
    }

    if (isNaN(amount) || amount <= 0) {
        showError(form, 'amount', 'Amount must be greater than 0');
        isValid = false;
    }

    if (!category) {
        showError(form, 'category', 'Category is required');
        isValid = false;
    }

    if (!expenseDate) {
        showError(form, 'expenseDate', 'Date is required');
        isValid = false;
    }

    return isValid;
}

function validateBudgetForm(form) {
    const category = form.querySelector('[name="category"]').value;
    const limitAmount = parseFloat(form.querySelector('[name="limitAmount"]').value);
    const startDate = form.querySelector('[name="startDate"]').value;
    const endDate = form.querySelector('[name="endDate"]').value;

    clearErrors(form);

    let isValid = true;

    if (!category) {
        showError(form, 'category', 'Category is required');
        isValid = false;
    }

    if (isNaN(limitAmount) || limitAmount <= 0) {
        showError(form, 'limitAmount', 'Limit amount must be greater than 0');
        isValid = false;
    }

    if (!startDate) {
        showError(form, 'startDate', 'Start date is required');
        isValid = false;
    }

    if (!endDate) {
        showError(form, 'endDate', 'End date is required');
        isValid = false;
    }

    if (startDate && endDate && new Date(startDate) > new Date(endDate)) {
        showError(form, 'endDate', 'End date must be after start date');
        isValid = false;
    }

    return isValid;
}

function validateGoalForm(form) {
    const title = form.querySelector('[name="title"]').value.trim();
    const targetAmount = parseFloat(form.querySelector('[name="targetAmount"]').value);
    const deadline = form.querySelector('[name="deadline"]').value;

    clearErrors(form);

    let isValid = true;

    if (!title) {
        showError(form, 'title', 'Title is required');
        isValid = false;
    }

    if (isNaN(targetAmount) || targetAmount <= 0) {
        showError(form, 'targetAmount', 'Target amount must be greater than 0');
        isValid = false;
    }

    if (!deadline) {
        showError(form, 'deadline', 'Deadline is required');
        isValid = false;
    }

    if (deadline && new Date(deadline) < new Date()) {
        showError(form, 'deadline', 'Deadline must be in the future');
        isValid = false;
    }

    return isValid;
}

function showError(form, fieldName, message) {
    const input = form.querySelector(`[name="${fieldName}"]`);
    if (!input) return;

    let errorDiv = input.nextElementSibling;
    if (!errorDiv || !errorDiv.classList.contains('error-message')) {
        errorDiv = document.createElement('div');
        errorDiv.className = 'error-message';
        errorDiv.style.cssText = 'color: #ef4444; font-size: 12px; margin-top: 4px;';
        input.parentNode.insertBefore(errorDiv, input.nextSibling);
    }

    errorDiv.textContent = message;
    input.style.borderColor = '#ef4444';
}

function clearErrors(form) {
    const errorMessages = form.querySelectorAll('.error-message');
    errorMessages.forEach(msg => msg.remove());

    const inputs = form.querySelectorAll('input, select, textarea');
    inputs.forEach(input => {
        input.style.borderColor = '#eaedf3';
    });
}

// Add validation to forms on page load
document.addEventListener('DOMContentLoaded', () => {
    const expenseForm = document.querySelector('form[action*="expenses"]');
    if (expenseForm) {
        expenseForm.addEventListener('submit', (e) => {
            if (!validateExpenseForm(expenseForm)) {
                e.preventDefault();
            }
        });
    }

    const budgetForm = document.querySelector('form[action*="budgets"]');
    if (budgetForm) {
        budgetForm.addEventListener('submit', (e) => {
            if (!validateBudgetForm(budgetForm)) {
                e.preventDefault();
            }
        });
    }

    const goalForm = document.querySelector('form[action*="goals"]');
    if (goalForm) {
        goalForm.addEventListener('submit', (e) => {
            if (!validateGoalForm(goalForm)) {
                e.preventDefault();
            }
        });
    }
});
