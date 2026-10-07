const API_BASE = '';

// Navegación de secciones
function showSection(sectionId) {
    document.querySelectorAll('.section-pane').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.nav-link').forEach(el => el.classList.remove('active'));
    
    document.getElementById(sectionId).classList.add('active');
    event.target.classList.add('active');

    // Cargar datos al entrar a la sección
    if(sectionId === 'clientes') cargarClientes();
    if(sectionId === 'vehiculos') cargarVehiculos();
    if(sectionId === 'ventas') {
        cargarVentas();
        cargarCombosVenta();
        cargarTasaCambio();
    }
    if(sectionId === 'mantenimientos') {
        cargarMantenimientos();
        cargarCombosMantenimiento();
    }
}

// Escapa texto antes de insertarlo con innerHTML
function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

// Alertas UI (success | error | warning | info)
const TOAST_STYLES = {
    success: { bg: 'bg-success',   text: 'text-white', icon: 'bi-check-circle' },
    error:   { bg: 'bg-danger',    text: 'text-white', icon: 'bi-x-circle' },
    warning: { bg: 'bg-warning',   text: 'text-dark',  icon: 'bi-exclamation-triangle' },
    info:    { bg: 'bg-secondary', text: 'text-white', icon: 'bi-info-circle' }
};

function showAlert(message, type = 'success') {
    const toastContainer = document.getElementById('toastContainer');
    const style = TOAST_STYLES[type] || TOAST_STYLES.error;
    const closeClass = style.text === 'text-white' ? 'btn-close-white' : '';

    const toast = document.createElement('div');
    toast.className = `toast align-items-center border-0 mb-2 ${style.bg} ${style.text}`;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');

    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body"><i class="bi ${style.icon} me-1"></i>${escapeHtml(message)}</div>
            <button type="button" class="btn-close ${closeClass} me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;
    toastContainer.appendChild(toast);

    const bsToast = new bootstrap.Toast(toast, { delay: 3500 });
    bsToast.show();

    toast.addEventListener('hidden.bs.toast', () => toast.remove());
}

async function fetchAPI(url, options = {}) {
    try {
        const response = await fetch(API_BASE + url, {
            ...options,
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            }
        });

        if (response.status === 204) return null;

        const text = await response.text();
        let data = null;
        try { data = text ? JSON.parse(text) : null; } catch (_) { /* cuerpo no JSON */ }

        if (!response.ok) {
            let errorMsg = (data && data.message) || `Error ${response.status} en la petición`;
            if (data && data.validationErrors) {
                errorMsg = Object.values(data.validationErrors).join(' - ');
            }
            throw new Error(errorMsg);
        }
        return data;
    } catch (error) {
        const msg = error instanceof TypeError
            ? 'No se pudo conectar con el servidor'
            : error.message;
        showAlert(msg, 'error');
        throw error;
    }
}

// Formateadores
const formatCOP = (num) => new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(num);
const formatUSD = (num) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(num);
const formatFecha = (fecha) => new Date(fecha).toLocaleString('es-CO');

// Normaliza texto para búsquedas (sin tildes, minúsculas)
const normalizar = (s) => String(s ?? '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();

// Ejecuta fn tras una pausa en la escritura
function debounce(fn, ms = 400) {
    let t;
    return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), ms); };
}

function filaVacia(colspan, mensaje) {
    return `<tr><td colspan="${colspan}" class="text-center text-muted py-4">${escapeHtml(mensaje)}</td></tr>`;
}

// --- CLIENTES ---
let clientesCache = [];

async function cargarClientes() {
    try {
        clientesCache = await fetchAPI('/clientes') || [];
        renderClientes();
    } catch (e) {}
}

function filtrarClientes() {
    const q = normalizar(document.getElementById('buscarCliente').value);
    if (!q) return clientesCache;
    return clientesCache.filter(c =>
        normalizar(c.cedula).includes(q) ||
        normalizar(`${c.nombre} ${c.apellido}`).includes(q)
    );
}

function renderClientes() {
    const tbody = document.getElementById('tbodyClientes');
    const lista = filtrarClientes();

    if (lista.length === 0) {
        const q = document.getElementById('buscarCliente').value.trim();
        tbody.innerHTML = filaVacia(6, q ? `Sin resultados para "${q}"` : 'No hay clientes registrados');
        return;
    }

    tbody.innerHTML = lista.map(c => `
        <tr>
            <td>${c.id}</td>
            <td>${escapeHtml(c.cedula)}</td>
            <td>${escapeHtml(c.nombre)} ${escapeHtml(c.apellido)}</td>
            <td>${escapeHtml(c.email)}</td>
            <td>${escapeHtml(c.telefono)}</td>
            <td class="text-end">
                <button class="btn btn-sm btn-danger" onclick="eliminarCliente(${c.id})">Eliminar</button>
            </td>
        </tr>
    `).join('');
}

function onBuscarCliente() {
    renderClientes();
}

async function eliminarCliente(id) {
    if (!confirm('¿Deseas eliminar este cliente?')) return;

    try {
        const response = await fetch(API_BASE + '/clientes/' + id, { method: 'DELETE' });
        if (!response.ok) {
            const data = await response.json().catch(() => ({}));
            throw new Error(data.message || 'Error al eliminar el cliente');
        }
        showAlert('Cliente eliminado correctamente');
        cargarClientes();
    } catch (e) {
        alert(e.message);
    }
}

async function registrarCliente(e) {
    e.preventDefault();
    const payload = {
        cedula: document.getElementById('cliCedula').value,
        nombre: document.getElementById('cliNombre').value,
        apellido: document.getElementById('cliApellido').value,
        email: document.getElementById('cliEmail').value,
        telefono: document.getElementById('cliTelefono').value,
        direccion: document.getElementById('cliDireccion').value
    };
    
    try {
        await fetchAPI('/clientes', { method: 'POST', body: JSON.stringify(payload) });
        showAlert('Cliente registrado con éxito');
        e.target.reset();
        cargarClientes();
    } catch (e) {}
}

// --- VEHÍCULOS ---
let vehiculosCache = [];

function badgeEstado(estado) {
    if(estado === 'DISPONIBLE') return `<span class="badge bg-success">DISPONIBLE</span>`;
    if(estado === 'VENDIDO') return `<span class="badge bg-secondary">VENDIDO</span>`;
    if(estado === 'EN_MANTENIMIENTO') return `<span class="badge bg-warning text-dark">MANTENIMIENTO</span>`;
    return `<span class="badge bg-light text-dark">${escapeHtml(estado)}</span>`;
}

function filtroEstadoVehiculos() {
    const sel = document.querySelector('input[name="filtroVehiculos"]:checked');
    return sel ? sel.value : 'todos';
}

async function cargarVehiculos() {
    const url = filtroEstadoVehiculos() === 'disponibles' ? '/vehiculos/disponibles' : '/vehiculos';
    try {
        vehiculosCache = await fetchAPI(url) || [];
        renderVehiculos();
    } catch (e) {}
}

function filtrarVehiculos() {
    const q = normalizar(document.getElementById('buscarVehiculo').value);
    if (!q) return vehiculosCache;
    return vehiculosCache.filter(v =>
        normalizar(v.marca).includes(q) ||
        normalizar(v.modelo).includes(q) ||
        normalizar(`${v.marca} ${v.modelo}`).includes(q)
    );
}

function renderVehiculos() {
    const tbody = document.getElementById('tbodyVehiculos');
    const lista = filtrarVehiculos();

    if (lista.length === 0) {
        const q = document.getElementById('buscarVehiculo').value.trim();
        const msg = q
            ? `Sin resultados para "${q}"`
            : (filtroEstadoVehiculos() === 'disponibles' ? 'No hay vehículos disponibles' : 'No hay vehículos registrados');
        tbody.innerHTML = filaVacia(7, msg);
        return;
    }

    tbody.innerHTML = lista.map(v => {
        const num = Number(v.precioCop ?? v.precio ?? 0);
        const precioTxt = isNaN(num) ? '$ 0' : `$ ${num.toLocaleString('es-CO')}`;
        return `
        <tr>
            <td>${v.id}</td>
            <td><strong>${escapeHtml(v.placa)}</strong></td>
            <td>${escapeHtml(v.marca)} ${escapeHtml(v.modelo)}</td>
            <td>${v.anio}</td>
            <td>${precioTxt}</td>
            <td>${badgeEstado(v.estado)}</td>
            <td class="text-end">
                <button class="btn btn-sm btn-danger" onclick="eliminarVehiculo(${v.id})">Eliminar</button>
            </td>
        </tr>
        `;
    }).join('');
}

function onBuscarVehiculo() {
    renderVehiculos();
}

async function eliminarVehiculo(id) {
    if (!confirm('¿Deseas eliminar este vehículo?')) return;

    try {
        const response = await fetch(API_BASE + '/vehiculos/' + id, { method: 'DELETE' });
        if (!response.ok) {
            const data = await response.json().catch(() => ({}));
            throw new Error(data.message || 'Error al eliminar el vehículo');
        }
        showAlert('Vehículo eliminado correctamente');
        cargarVehiculos();
    } catch (e) {
        alert(e.message);
    }
}

async function registrarVehiculo(e) {
    e.preventDefault();
    const payload = {
        placa: document.getElementById('vehPlaca').value,
        marca: document.getElementById('vehMarca').value,
        modelo: document.getElementById('vehModelo').value,
        anio: parseInt(document.getElementById('vehAnio').value),
        color: document.getElementById('vehColor').value,
        precio: parseFloat(document.getElementById('vehPrecio').value)
    };
    
    try {
        await fetchAPI('/vehiculos', { method: 'POST', body: JSON.stringify(payload) });
        showAlert('Vehículo registrado con éxito');
        e.target.reset();
        cargarVehiculos();
    } catch (e) {}
}

// --- VENTAS ---
async function cargarCombosVenta() {
    try {
        const [clientes, vehiculos] = await Promise.all([
            fetchAPI('/clientes'),
            fetchAPI('/vehiculos/disponibles')
        ]);
        
        document.getElementById('venClienteId').innerHTML = `<option value="">Seleccione Cliente...</option>` + 
            clientes.map(c => `<option value="${c.id}">${escapeHtml(c.cedula)} - ${escapeHtml(c.nombre)} ${escapeHtml(c.apellido)}</option>`).join('');
            
        document.getElementById('venVehiculoId').innerHTML = `<option value="">Seleccione Vehículo...</option>` + 
            vehiculos.map(v => `<option value="${v.id}">${escapeHtml(v.placa)} - ${escapeHtml(v.marca)} ${escapeHtml(v.modelo)} (${formatCOP(v.precio)})</option>`).join('');
    } catch (e) {}
}

async function cargarTasaCambio() {
    try {
        const data = await fetchAPI('/ventas/tasa-cambio');
        if (data && data.tasa) {
            const tasa = Number(data.tasa);
            const copValue = (1 / tasa).toFixed(2);
            document.getElementById('tasaCambioBadge').innerHTML = 
                `1 USD = $${copValue} COP <small class="text-muted ms-1">(${escapeHtml(data.fuente)})</small>`;
        }
    } catch (e) {
        document.getElementById('tasaCambioBadge').innerHTML = 'No disponible';
    }
}

async function cargarVentas() {
    try {
        const ventas = await fetchAPI('/ventas');
        const tbody = document.getElementById('tbodyVentas');
        tbody.innerHTML = ventas.map(v => `
            <tr>
                <td>${v.id}</td>
                <td>${formatFecha(v.fechaVenta)}</td>
                <td>${escapeHtml(v.cliente?.nombreCompleto || '')}</td>
                <td>${escapeHtml(v.vehiculo?.placa || '')}</td>
                <td>${formatCOP(Number(v.precioBaseCop) || 0)}</td>
                <td>${Number(v.porcentajeDescuento) || 0}%</td>
                <td><strong>${formatCOP(Number(v.totalPagadoCop) || 0)}</strong></td>
                <td class="text-success fw-bold">${v.totalPagadoUsd ? formatUSD(Number(v.totalPagadoUsd)) : 'N/A'} <br><small class="text-muted" style="font-size: 0.75em;">Tasa: ${v.tasaCambioCopUsd || 'N/A'}</small></td>
            </tr>
        `).join('');
    } catch (e) {}
}

async function registrarVenta(e) {
    e.preventDefault();
    const payload = {
        clienteId: parseInt(document.getElementById('venClienteId').value),
        vehiculoId: parseInt(document.getElementById('venVehiculoId').value),
        observaciones: document.getElementById('venObservaciones').value
    };
    
    try {
        const result = await fetchAPI('/ventas', { method: 'POST', body: JSON.stringify(payload) });
        showAlert(`Venta registrada exitosamente. Total: ${formatCOP(Number(result.totalPagadoCop))} (${formatUSD(Number(result.totalPagadoUsd))})`);
        e.target.reset();
        cargarVentas();
        cargarCombosVenta(); // Refrescar combos (el vehículo ya no debe salir)
    } catch (e) {}
}

// --- MANTENIMIENTOS ---
let mantenimientosCache = [];

function badgeEstadoMantenimiento(estado) {
    if(estado === 'EN_PROCESO') return `<span class="badge bg-warning text-dark">EN PROCESO</span>`;
    if(estado === 'FINALIZADO') return `<span class="badge bg-success">FINALIZADO</span>`;
    return `<span class="badge bg-secondary">${escapeHtml(estado)}</span>`;
}

async function cargarCombosMantenimiento() {
    try {
        const vehiculos = await fetchAPI('/vehiculos');
        const elegibles = vehiculos.filter(v => v.estado !== 'EN_MANTENIMIENTO');
        
        const select = document.getElementById('manVehiculoId');
        if (elegibles.length === 0) {
            select.innerHTML = `<option value="">No hay vehículos disponibles para mantenimiento</option>`;
        } else {
            select.innerHTML = `<option value="">Seleccione Vehículo...</option>` + 
                elegibles.map(v => `<option value="${v.id}">${escapeHtml(v.placa)} - ${escapeHtml(v.marca)} ${escapeHtml(v.modelo)} (${escapeHtml(v.estado)})</option>`).join('');
        }
    } catch (e) {}
}

async function cargarMantenimientos() {
    try {
        mantenimientosCache = await fetchAPI('/mantenimientos') || [];
        renderMantenimientos();
    } catch (e) {}
}

function filtrarMantenimientos() {
    const q = normalizar(document.getElementById('buscarMantenimiento').value);
    if (!q) return mantenimientosCache;
    return mantenimientosCache.filter(m =>
        normalizar(m.placa).includes(q) ||
        normalizar(m.estado).includes(q) ||
        normalizar(m.tipo).includes(q) ||
        normalizar(m.descripcion).includes(q)
    );
}

function renderMantenimientos() {
    const tbody = document.getElementById('tbodyMantenimientos');
    const lista = filtrarMantenimientos();

    if (lista.length === 0) {
        const q = document.getElementById('buscarMantenimiento').value.trim();
        tbody.innerHTML = filaVacia(7, q ? `Sin resultados para "${q}"` : 'No hay mantenimientos registrados');
        return;
    }

    tbody.innerHTML = lista.map(m => {
        const accionesHtml = m.estado === 'EN_PROCESO'
            ? `<button class="btn btn-sm btn-success ms-1" onclick="finalizarMantenimiento(${m.id})">Finalizar</button>`
            : `<span class="badge bg-secondary">Completado</span>`;

        return `
        <tr>
            <td>${m.id}</td>
            <td>${formatFecha(m.fechaIngreso)}<br><small class="text-muted">${m.fechaSalida ? formatFecha(m.fechaSalida) : ''}</small></td>
            <td><strong>${escapeHtml(m.placa || 'N/A')}</strong></td>
            <td>${escapeHtml(m.tipo)}<br><small class="text-muted">${escapeHtml(m.descripcion || '')}</small></td>
            <td>${badgeEstadoMantenimiento(m.estado)}</td>
            <td>$ ${Number(m.costoCop || 0).toLocaleString('es-CO')}</td>
            <td class="text-end">
                ${accionesHtml}
                <button class="btn btn-sm btn-danger ms-1" onclick="eliminarMantenimiento(${m.id})">Eliminar</button>
            </td>
        </tr>
        `;
    }).join('');
}

function onBuscarMantenimiento() {
    renderMantenimientos();
}

async function finalizarMantenimiento(id) {
    if (!confirm('¿Deseas marcar este mantenimiento como finalizado y actualizar el estado del vehículo?')) return;
    try {
        await fetchAPI(`/mantenimientos/${id}/finalizar`, { method: 'PATCH' });
        showAlert('Mantenimiento finalizado con éxito');
        cargarMantenimientos();
        cargarCombosMantenimiento();
        cargarVehiculos();
    } catch (e) {}
}

async function eliminarMantenimiento(id) {
    if (!confirm('¿Seguro que deseas eliminar este registro de mantenimiento?')) return;
    try {
        await fetchAPI(`/mantenimientos/${id}`, { method: 'DELETE' });
        showAlert('Mantenimiento eliminado correctamente');
        cargarMantenimientos();
        cargarCombosMantenimiento();
        cargarVehiculos();
    } catch (e) {}
}

async function registrarMantenimiento(e) {
    e.preventDefault();
    const payload = {
        vehiculoId: parseInt(document.getElementById('manVehiculoId').value),
        tipo: document.getElementById('manTipo').value,
        costo: parseFloat(document.getElementById('manCosto').value),
        descripcion: document.getElementById('manDescripcion').value
    };
    
    try {
        await fetchAPI('/mantenimientos', { method: 'POST', body: JSON.stringify(payload) });
        showAlert('Mantenimiento registrado con éxito');
        e.target.reset();
        cargarMantenimientos();
        cargarCombosMantenimiento();
        cargarVehiculos();
    } catch (e) {}
}

// Inicialización
document.addEventListener('DOMContentLoaded', () => {
    cargarClientes();
});
