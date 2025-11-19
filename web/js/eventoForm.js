(function () {
    const FORM_SELECTOR = 'form.evento-form';
    const DATE_FORMAT = "Y-m-d";

    function localizeCalendar() {
        if (window.flatpickr && window.flatpickr.l10ns && window.flatpickr.l10ns.es) {
            window.flatpickr.localize(window.flatpickr.l10ns.es);
        }
    }

    // Convierte el JSON generado en el backend en rangos deshabilitados
    function parseDisabledDates(rawValue) {
        if (!rawValue) {
            return [];
        }

        try {
            const parsed = JSON.parse(rawValue);
            if (!Array.isArray(parsed)) {
                return [];
            }

            return parsed
                .map(range => {
                    if (!range || !range.from) {
                        return null;
                    }

                    const dateObj = new Date(range.from);
                    if (isNaN(dateObj.getTime())) {
                        return null;
                    }

                    // Flatpickr espera objetos tipo: { from: Date, to: Date }
                    return {
                        from: dateObj,
                        to: dateObj
                    };
                })
                .filter(Boolean);

        } catch (error) {
            console.warn('No se pudieron interpretar las fechas ocupadas del espacio.', error);
            return [];
        }
    }

    function readDisabledDates(form) {
        const dataNode = form.querySelector('[id$="ocupacionesJson"]');

        if (!dataNode) {
            return [];
        }

        const rawValue = 'value' in dataNode
            ? dataNode.value
            : dataNode.textContent;

        return parseDisabledDates(rawValue && rawValue.trim());
    }

    function ensurePickerDestroyed(input) {
        if (input && input._flatpickr) {
            input._flatpickr.destroy();
        }
    }

    function initForm(form) {
        if (!window.flatpickr) {
            return;
        }

        const fechaInput = form.querySelector('input[id$="fechaEvento"]');
        if (!fechaInput) {
            return;
        }

        ensurePickerDestroyed(fechaInput);

        const disabledDates = readDisabledDates(form);

        window.flatpickr(fechaInput, {
            enableTime: false,            // SOLO FECHA
            dateFormat: DATE_FORMAT,
            altInput: true,
            altFormat: 'd/m/Y',
            disable: disabledDates,
            allowInput: true
        });
    }

    function initializeForms() {
        if (!window.flatpickr) {
            return;
        }

        localizeCalendar();
        const forms = document.querySelectorAll(FORM_SELECTOR);

        forms.forEach(initForm);
    }

    // Cuando carga la página
    document.addEventListener('DOMContentLoaded', initializeForms);

    // Cuando JSF hace AJAX (actualiza el formulario)
    if (window.jsf && window.jsf.ajax) {
        window.jsf.ajax.addOnEvent(function (event) {
            if (event && event.status === 'success') {
                initializeForms();
            }
        });
    }
})();
