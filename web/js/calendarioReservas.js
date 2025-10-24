(function () {
    'use strict';

    const DIA_EN_MS = 86400000;

    const formatearFechaTexto = (fecha) => {
        if (!fecha) {
            return '';
        }
        return new Intl.DateTimeFormat('es-ES', {
            year: 'numeric',
            month: 'short',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        }).format(fecha);
    };

    const formatoRango = (evento) => {
        const inicio = evento.start ? formatearFechaTexto(evento.start) : '';
        const fin = evento.end ? formatearFechaTexto(evento.end) : '';
        if (!inicio && !fin) {
            return '';
        }
        return `${inicio} - ${fin}`;
    };

    const toLocalIso = (date) => {
        if (!date) {
            return '';
        }
        const tzOffset = date.getTimezoneOffset() * 60000;
        return new Date(date.getTime() - tzOffset).toISOString().slice(0, 19);
    };

    const obtenerJson = (id) => {
        const elemento = document.getElementById(id);
        if (!elemento) {
            return [];
        }
        try {
            const contenido = elemento.textContent || elemento.innerText || '[]';
            return JSON.parse(contenido);
        } catch (error) {
            console.error('Error al parsear el JSON del elemento', id, error);
            return [];
        }
    };

    const eventosData = obtenerJson('eventos-json');
    const tiposHabitacionData = obtenerJson('tipos-json');
    const habitacionesData = obtenerJson('habitaciones-json');
    const usuariosData = obtenerJson('usuarios-json');

    let calendario;
    let crearReservaModalInstance;

    const mostrarDetalleReserva = (evento) => {
        const detalleModalEl = document.getElementById('detalleReservaModal');
        if (!detalleModalEl || !window.bootstrap) {
            return;
        }

        const modal = new window.bootstrap.Modal(detalleModalEl);
        const setText = (id, valor) => {
            const elemento = document.getElementById(id);
            if (elemento) {
                elemento.textContent = valor;
            }
        };

        setText('detalle-habitacion', `Habitación ${evento.extendedProps.habitacionNumero || ''}`);
        setText('detalle-tipo', evento.extendedProps.tipoHabitacion || 'Sin tipo asignado');
        setText('detalle-fechas', formatoRango(evento));
        setText('detalle-estado', `Estado: ${evento.extendedProps.estado || 'Sin estado'}`);
        setText('detalle-cliente', evento.extendedProps.cliente || 'Sin información');
        setText('detalle-email', evento.extendedProps.email || '-');
        setText('detalle-telefono', evento.extendedProps.telefono || '-');
        setText('detalle-observaciones', evento.extendedProps.observaciones || 'Sin observaciones');

        modal.show();
    };

    const actualizarFechasReserva = (evento, revertir) => {
        const inicio = evento.start;
        const fin = evento.end || (inicio ? new Date(inicio.getTime() + DIA_EN_MS) : null);
        const parametros = [
            {name: 'id', value: evento.id},
            {name: 'start', value: toLocalIso(inicio)},
            {name: 'end', value: toLocalIso(fin)}
        ];

        if (typeof window.actualizarReservaFechasCommand === 'function') {
            window.actualizarReservaFechasCommand(parametros, {
                oncomplete: (xhr, status, args) => {
                    if (!args || !args.success) {
                        if (typeof revertir === 'function') {
                            revertir();
                        }
                    }
                }
            });
        }
    };

    const prepararSelectHabitaciones = (tipoId) => {
        const selectHabitacion = document.getElementById('crear-habitacion');
        if (!selectHabitacion) {
            return;
        }

        const valorActual = selectHabitacion.value;
        selectHabitacion.innerHTML = '';

        const opcionDefault = document.createElement('option');
        opcionDefault.value = '';
        opcionDefault.textContent = 'Selecciona una habitación';
        selectHabitacion.appendChild(opcionDefault);

        habitacionesData
            .filter((habitacion) => !tipoId || habitacion.tipoId === tipoId)
            .forEach((habitacion) => {
                const option = document.createElement('option');
                option.value = habitacion.id;
                option.textContent = `Habitación ${habitacion.numero} (${habitacion.tipoNombre})`;
                selectHabitacion.appendChild(option);
            });

        if (valorActual) {
            selectHabitacion.value = valorActual;
        }
    };

    const prepararSelectUsuarios = () => {
        const selectUsuario = document.getElementById('crear-usuario');
        if (!selectUsuario) {
            return;
        }

        const valorSeleccionado = selectUsuario.value;
        selectUsuario.innerHTML = '';

        const opcionDefault = document.createElement('option');
        opcionDefault.value = '';
        opcionDefault.textContent = 'Selecciona un huésped';
        selectUsuario.appendChild(opcionDefault);

        usuariosData.forEach((usuario) => {
            const option = document.createElement('option');
            option.value = usuario.id;
            option.textContent = `${usuario.nombre} (${usuario.email})`;
            selectUsuario.appendChild(option);
        });

        if (valorSeleccionado) {
            selectUsuario.value = valorSeleccionado;
        }

        selectUsuario.onchange = (event) => {
            const seleccionado = usuariosData.find((usuario) => `${usuario.id}` === event.target.value);
            if (seleccionado) {
                const setValue = (id, valor) => {
                    const elemento = document.getElementById(id);
                    if (elemento) {
                        elemento.value = valor || '';
                    }
                };
                setValue('crear-nombre', seleccionado.nombre);
                setValue('crear-email', seleccionado.email);
                setValue('crear-telefono', seleccionado.telefono);
            }
        };
    };

    const abrirModalCreacion = ({tipoId, tipoNombre, inicio, fin}) => {
        const asignarValor = (id, valor) => {
            const elemento = document.getElementById(id);
            if (elemento) {
                elemento.value = valor || '';
            }
        };

        asignarValor('crear-tipo-id', tipoId || '');
        asignarValor('crear-tipo-nombre', tipoNombre || '');
        asignarValor('crear-start', inicio || '');
        asignarValor('crear-end', fin || '');

        const resumenFechas = document.getElementById('crear-resumen-fechas');
        if (resumenFechas) {
            const inicioFecha = inicio ? new Date(inicio) : null;
            const finFecha = fin ? new Date(fin) : null;
            resumenFechas.textContent = `Check-in: ${formatearFechaTexto(inicioFecha)} | Check-out: ${formatearFechaTexto(finFecha)}`;
        }

        asignarValor('crear-observaciones', '');
        asignarValor('crear-nombre', '');
        asignarValor('crear-email', '');
        asignarValor('crear-telefono', '');

        const estado = document.getElementById('crear-estado');
        if (estado) {
            estado.value = 'ACTIVA';
        }

        prepararSelectHabitaciones(tipoId);
        prepararSelectUsuarios();

        if (crearReservaModalInstance) {
            crearReservaModalInstance.show();
        }
    };

    const manejarNuevaReserva = (evento) => {
        const tipoId = evento.extendedProps.tipoId;
        const tipoNombre = evento.extendedProps.tipoNombre;
        const inicio = toLocalIso(evento.start);
        const fin = toLocalIso(evento.end || (evento.start ? new Date(evento.start.getTime() + DIA_EN_MS) : null));

        evento.remove();
        abrirModalCreacion({
            tipoId,
            tipoNombre,
            inicio,
            fin
        });
    };

    const inicializarCalendario = () => {
        const calendarEl = document.getElementById('calendar');
        if (!calendarEl || !window.FullCalendar) {
            return;
        }

        calendario = new window.FullCalendar.Calendar(calendarEl, {
            locale: 'es',
            initialView: 'dayGridMonth',
            height: 'auto',
            headerToolbar: {
                left: 'prev,next today',
                center: 'title',
                right: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek'
            },
            selectable: true,
            editable: true,
            droppable: true,
            events: eventosData,
            eventContent: (arg) => {
                const container = document.createElement('div');
                container.classList.add('fc-event-custom');

                const room = document.createElement('div');
                room.classList.add('fc-event-room');
                room.textContent = `Hab. ${arg.event.extendedProps.habitacionNumero || ''}`;

                const client = document.createElement('div');
                client.classList.add('fc-event-client');
                client.textContent = arg.event.extendedProps.cliente || '';

                const range = document.createElement('div');
                range.classList.add('fc-event-dates');
                range.textContent = formatoRango(arg.event);

                container.appendChild(room);
                container.appendChild(client);
                container.appendChild(range);

                return {domNodes: [container]};
            },
            eventClick: (info) => {
                if (info.jsEvent) {
                    info.jsEvent.preventDefault();
                }
                mostrarDetalleReserva(info.event);
            },
            eventDrop: (info) => {
                actualizarFechasReserva(info.event, info.revert);
            },
            eventResize: (info) => {
                actualizarFechasReserva(info.event, info.revert);
            },
            eventReceive: (info) => {
                manejarNuevaReserva(info.event);
            }
        });

        calendario.render();
    };

    const inicializarDraggables = () => {
        const draggableContainer = document.getElementById('tipos-draggable');
        if (!draggableContainer || !window.FullCalendar || !window.FullCalendar.Draggable) {
            return;
        }

        new window.FullCalendar.Draggable(draggableContainer, {
            itemSelector: '.external-event',
            eventData: (eventEl) => {
                const tipoId = parseInt(eventEl.dataset.tipoId, 10);
                const nombre = eventEl.dataset.nombre;
                return {
                    title: nombre,
                    extendedProps: {
                        tipoId,
                        tipoNombre: nombre
                    },
                    duration: {days: 1}
                };
            }
        });
    };

    const inicializarModal = () => {
        const modalEl = document.getElementById('crearReservaModal');
        if (modalEl && window.bootstrap) {
            crearReservaModalInstance = new window.bootstrap.Modal(modalEl);
        }
    };

    window.guardarNuevaReserva = () => {
        const inicio = document.getElementById('crear-start')?.value;
        const fin = document.getElementById('crear-end')?.value;
        const habitacionId = document.getElementById('crear-habitacion')?.value;
        const usuarioId = document.getElementById('crear-usuario')?.value;

        if (!habitacionId || !usuarioId) {
            window.alert('Selecciona una habitación y un huésped para continuar.');
            return;
        }

        const parametros = [
            {name: 'start', value: inicio},
            {name: 'end', value: fin},
            {name: 'habitacionId', value: habitacionId},
            {name: 'usuarioId', value: usuarioId},
            {name: 'estado', value: document.getElementById('crear-estado')?.value},
            {name: 'clienteNombre', value: document.getElementById('crear-nombre')?.value},
            {name: 'email', value: document.getElementById('crear-email')?.value},
            {name: 'telefono', value: document.getElementById('crear-telefono')?.value},
            {name: 'observaciones', value: document.getElementById('crear-observaciones')?.value}
        ];

        if (typeof window.crearReservaCalendarioCommand === 'function') {
            window.crearReservaCalendarioCommand(parametros, {
                oncomplete: (xhr, status, args) => {
                    if (args && args.success) {
                        if (args.evento && calendario) {
                            try {
                                const nuevoEvento = JSON.parse(args.evento);
                                calendario.addEvent(nuevoEvento);
                            } catch (error) {
                                console.error('No se pudo agregar el evento devuelto por el servidor', error);
                            }
                        }
                        if (crearReservaModalInstance) {
                            crearReservaModalInstance.hide();
                        }
                    }
                }
            });
        }
    };

    document.addEventListener('DOMContentLoaded', () => {
        inicializarCalendario();
        inicializarDraggables();
        inicializarModal();
        prepararSelectUsuarios();
    });
})();
