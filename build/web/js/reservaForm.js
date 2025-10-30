(function () {
    const FORM_SELECTOR = 'form.reserva-form';
    const DATE_FORMAT = "Y-m-d\\TH:i";

    function localizeCalendar() {
        if (window.flatpickr && window.flatpickr.l10ns && window.flatpickr.l10ns.es) {
            window.flatpickr.localize(window.flatpickr.l10ns.es);
        }
    }

    function parseDisabledRanges(rawValue) {
        if (!rawValue) {
            return [];
        }

        try {
            const parsed = JSON.parse(rawValue);
            if (!Array.isArray(parsed)) {
                return [];
            }

            return parsed
                .map((range) => {
                    if (!range || !range.from || !range.to) {
                        return null;
                    }

                    const fromDate = new Date(range.from);
                    const toDate = new Date(range.to);

                    if (isNaN(fromDate.getTime()) || isNaN(toDate.getTime())) {
                        return null;
                    }

                    if (fromDate.getTime() >= toDate.getTime()) {
                        return {
                            from: fromDate
                        };
                    }

                    const adjustedTo = new Date(toDate.getTime() - 60000);

                    if (adjustedTo.getTime() < fromDate.getTime()) {
                        return {
                            from: fromDate
                        };
                    }

                    return {
                        from: fromDate,
                        to: adjustedTo
                    };
                })
                .filter(Boolean);
        } catch (error) {
            console.warn('No se pudieron interpretar las fechas ocupadas de la habitación.', error);
            return [];
        }
    }

    function readDisabledRanges(form) {
        const dataNode = form.querySelector('[id$="ocupacionesJson"]');

        if (!dataNode) {
            return [];
        }

        const rawValue = 'value' in dataNode
            ? dataNode.value
            : dataNode.textContent;

        return parseDisabledRanges(rawValue && rawValue.trim());
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

        const checkinInput = form.querySelector('input[id$="checkin"]');
        const checkoutInput = form.querySelector('input[id$="checkout"]');

        if (!checkinInput || !checkoutInput) {
            return;
        }

        ensurePickerDestroyed(checkinInput);
        ensurePickerDestroyed(checkoutInput);

        const disabledRanges = readDisabledRanges(form);
        let checkoutPicker = null;

        checkoutPicker = window.flatpickr(checkoutInput, {
            enableTime: true,
            dateFormat: DATE_FORMAT,
            altInput: true,
            altFormat: 'd/m/Y H:i',
            time_24hr: true,
            disable: disabledRanges,
            allowInput: true
        });

        window.flatpickr(checkinInput, {
            enableTime: true,
            dateFormat: DATE_FORMAT,
            altInput: true,
            altFormat: 'd/m/Y H:i',
            time_24hr: true,
            disable: disabledRanges,
            allowInput: true,
            onReady(selectedDates) {
                if (selectedDates && selectedDates.length && checkoutPicker) {
                    checkoutPicker.set('minDate', selectedDates[0]);
                }
            },
            onChange(selectedDates) {
                if (!checkoutPicker) {
                    return;
                }

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

        if (checkoutPicker && checkinInput._flatpickr && checkinInput._flatpickr.selectedDates.length) {
            const currentCheckin = checkinInput._flatpickr.selectedDates[0];
            checkoutPicker.set('minDate', currentCheckin);

            if (checkoutPicker.selectedDates.length && checkoutPicker.selectedDates[0] < currentCheckin) {
                checkoutPicker.clear();
            }
        }
    }

    function initializeForms() {
        if (!window.flatpickr) {
            return;
        }

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
