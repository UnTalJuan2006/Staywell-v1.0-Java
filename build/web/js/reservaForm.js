(function () {
    const FORM_SELECTOR = 'form.reserva-form';
    const DATE_FORMAT = "Y-m-d\\TH:i";

    function localizeCalendar() {
        if (window.flatpickr && window.flatpickr.l10ns && window.flatpickr.l10ns.es) {
            window.flatpickr.localize(window.flatpickr.l10ns.es);
        }
    }

    function inicioDeDia(fecha) {
        return new Date(fecha.getFullYear(), fecha.getMonth(), fecha.getDate());
    }

    function finDeDia(fecha) {
        return new Date(fecha.getFullYear(), fecha.getMonth(), fecha.getDate(), 23, 59, 59, 999);
    }

    function parseIsoLocalDateTime(valor) {
        if (!valor || typeof valor !== 'string') return null;

        const [fechaStr, horaStr = '00:00'] = valor.split('T');
        const [anio, mes = '1', dia = '1'] = fechaStr.split('-').map(Number);
        const [hora = 0, minuto = 0, segundo = 0] = horaStr.split(':').map(Number);

        if (!anio || !mes || !dia) return null;

        return new Date(anio, (mes - 1), dia, hora, minuto, segundo);
    }

    function parseDisabledRanges(rawValue) {
        if (!rawValue) return [];

        try {
            const parsed = JSON.parse(rawValue);
            if (!Array.isArray(parsed)) return [];

            return parsed
                .map(range => {
                    if (!range || !range.from || !range.to) return null;

                    const fromDate = parseIsoLocalDateTime(range.from);
                    const toDate = parseIsoLocalDateTime(range.to);

                    if (isNaN(fromDate.getTime()) || isNaN(toDate.getTime())) return null;

                    const fromStart = inicioDeDia(fromDate);
                    const toEnd = finDeDia(toDate);

                    if (fromStart.getTime() >= toEnd.getTime()) {
                        return { from: fromStart, to: fromStart };
                    }

                    return { from: fromStart, to: toEnd };
                })
                .filter(Boolean);
        } catch (error) {
            console.warn('No se pudieron interpretar las fechas ocupadas de la habitación.', error);
            return [];
        }
    }

    function readDisabledRanges(form) {
        const dataNode = form.querySelector('[id$="ocupacionesJson"]');
        if (!dataNode) return [];

        const rawValue = 'value' in dataNode ? dataNode.value : dataNode.textContent;
        return parseDisabledRanges(rawValue && rawValue.trim());
    }

    function ensurePickerDestroyed(input) {
        if (input && input._flatpickr) input._flatpickr.destroy();
    }

    function initForm(form) {
        if (!window.flatpickr) return;

        const checkinInput = form.querySelector('input[id$="checkin"]');
        const checkoutInput = form.querySelector('input[id$="checkout"]');
        if (!checkinInput || !checkoutInput) return;

        ensurePickerDestroyed(checkinInput);
        ensurePickerDestroyed(checkoutInput);

        const disabledRanges = readDisabledRanges(form);
        const today = new Date();
        const todayStart = inicioDeDia(today);

        const disableRules = [
            // Regla para bloquear cualquier fecha pasada
            (date) => inicioDeDia(date) < todayStart,
            // Reglas de rangos ocupados (inclusivos en check-in y check-out)
            ...disabledRanges.map(range => ({
                from: range.from,
                to: range.to || range.from
            }))
        ];

        let checkoutPicker = window.flatpickr(checkoutInput, {
            enableTime: true,
            dateFormat: DATE_FORMAT,
            altInput: true,
            altFormat: 'd/m/Y H:i',
            time_24hr: true,
            allowInput: true,
            disable: disableRules,
            minDate: todayStart
        });

        window.flatpickr(checkinInput, {
            enableTime: true,
            dateFormat: DATE_FORMAT,
            altInput: true,
            altFormat: 'd/m/Y H:i',
            time_24hr: true,
            allowInput: true,
            disable: disableRules,
            minDate: todayStart,
            onReady(selectedDates) {
                if (selectedDates && selectedDates.length && checkoutPicker) {
                    checkoutPicker.set('minDate', selectedDates[0]);
                }
            },
            onChange(selectedDates) {
                if (!checkoutPicker) return;
                if (selectedDates && selectedDates.length) {
                    const checkinDate = selectedDates[0];
                    checkoutPicker.set('minDate', checkinDate);

                    if (checkoutPicker.selectedDates.length && checkoutPicker.selectedDates[0] < checkinDate) {
                        checkoutPicker.clear();
                    }
                } else {
                    checkoutPicker.set('minDate', null);
                }
            }
        });

        // Si ya hay check-in seleccionado al cargar
        if (checkoutPicker && checkinInput._flatpickr && checkinInput._flatpickr.selectedDates.length) {
            const currentCheckin = checkinInput._flatpickr.selectedDates[0];
            checkoutPicker.set('minDate', currentCheckin);
            if (checkoutPicker.selectedDates.length && checkoutPicker.selectedDates[0] < currentCheckin) {
                checkoutPicker.clear();
            }
        }
    }

    function initializeForms() {
        if (!window.flatpickr) return;
        localizeCalendar();
        const forms = document.querySelectorAll(FORM_SELECTOR);
        forms.forEach(initForm);
    }

    document.addEventListener('DOMContentLoaded', initializeForms);

    if (window.jsf && window.jsf.ajax) {
        window.jsf.ajax.addOnEvent(function (event) {
            if (event && event.status === 'success') {
                initializeForms();
            }
        });
    }
})();
