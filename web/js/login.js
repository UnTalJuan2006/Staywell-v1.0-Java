(() => {
    function byId(id) {
        return document.getElementById(id);
    }

    function validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    function showError(inputId, message) {
        const input = byId(inputId);
        if (!input) return;

        let errorDiv = input.parentNode.querySelector('.error-message');
        if (!errorDiv) {
            errorDiv = document.createElement('div');
            errorDiv.className = 'error-message';
            input.parentNode.appendChild(errorDiv);
        }

        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
        input.style.borderColor = '#dc2626';
    }

    function hideError(inputId) {
        const input = byId(inputId);
        if (!input) return;

        const errorDiv = input.parentNode.querySelector('.error-message');
        if (errorDiv) {
            errorDiv.style.display = 'none';
        }
        input.style.borderColor = '';
    }

    function setupLoginForm() {
        const loginForm = byId('loginForm');
        if (!loginForm) return;

        loginForm.addEventListener('submit', (e) => {
            const email = byId('EmailUser');
            const password = byId('PwUsr');
            let isValid = true;

            if (!email || !validateEmail(email.value)) {
                showError('EmailUser', 'Por favor ingresa un email válido');
                isValid = false;
            } else {
                hideError('EmailUser');
            }

            if (!password || password.value.length < 1) {
                showError('PwUsr', 'Debe especificar una contraseña');
                isValid = false;
            } else {
                hideError('PwUsr');
            }

            if (!isValid) {
                e.preventDefault();
            }
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', setupLoginForm);
    } else {
        setupLoginForm();
    }
})();
