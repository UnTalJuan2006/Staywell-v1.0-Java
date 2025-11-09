document.addEventListener('DOMContentLoaded', function() {
    // Elementos del DOM
    const lightEffect = document.getElementById('lightEffect');
    const particlesContainer = document.getElementById('particles');

    // Efecto de luz que sigue al cursor
    document.addEventListener('mousemove', function(e) {
        const x = e.clientX;
        const y = e.clientY;
        
        lightEffect.style.left = `${x - 150}px`;
        lightEffect.style.top = `${y - 150}px`;
    });

    // Crear partículas de fondo
    function createParticles() {
        for (let i = 0; i < 25; i++) {
            const particle = document.createElement('div');
            particle.classList.add('particle');
            
            // Posición aleatoria
            particle.style.left = `${Math.random() * 100}vw`;
            particle.style.top = `${Math.random() * 100}vh`;
            
            // Tamaño aleatorio
            const size = Math.random() * 4 + 2;
            particle.style.width = `${size}px`;
            particle.style.height = `${size}px`;
            
            // Opacidad aleatoria
            particle.style.opacity = Math.random() * 0.8 + 0.2;
            
            // Retraso de animación aleatorio
            particle.style.animationDelay = `${Math.random() * 20}s`;
            
            // Duración de animación aleatoria
            particle.style.animationDuration = `${Math.random() * 15 + 15}s`;
            
            particlesContainer.appendChild(particle);
        }
    }

    createParticles();

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