document.addEventListener('DOMContentLoaded', function() {
    // Configuración de formularios
    setupLoginForm();

    function setupLoginForm() {
        const loginForm = document.getElementById('loginForm');
        
        // Validación del formulario de login
        loginForm.addEventListener('submit', function(e) {
            const email = document.getElementById('EmailUser').value;
            const password = document.getElementById('PwUsr').value;
            let isValid = true;

            // Validar email
            if (!validateEmail(email)) {
                showError('EmailUser', 'Por favor ingresa un email válido');
                isValid = false;
            } else {
                hideError('EmailUser');
            }

            // Validar contraseña
            if (password.length < 6) {
                showError('PwUsr', 'La contraseña debe tener al menos 6 caracteres');
                isValid = false;
            } else {
                hideError('PwUsr');
            }

            if (!isValid) {
                e.preventDefault();
            }
        });
    }

    // Funciones de utilidad
    function validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    function showError(inputId, message) {
        const input = document.getElementById(inputId);
        let errorDiv = input.parentNode.querySelector('.error-message');
        
        if (!errorDiv) {
            errorDiv = document.createElement('div');
            errorDiv.className = 'error-message';
            input.parentNode.appendChild(errorDiv);
        }
        
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
        input.style.borderColor = '#ff6b6b';
    }

    function hideError(inputId) {
        const input = document.getElementById(inputId);
        const errorDiv = input.parentNode.querySelector('.error-message');
        
        if (errorDiv) {
            errorDiv.style.display = 'none';
        }
        
        input.style.borderColor = 'rgba(255, 255, 255, 0.3)';
    }
});