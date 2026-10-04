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
    }
    if(sectionId === 'mantenimientos') {
        cargarMantenimientos();
        cargarCombosMantenimiento();
    }
}

// Alertas UI
function showAlert(message, type = 'success') {
    const toastContainer = document.getElementById('toastContainer');
    const bgClass = type === 'success' ? 'bg-success' : 'bg-danger';
    
    const toast = document.createElement('div');
    toast.className = `toast align-items-center text-white border-0 mb-2 ${bgClass}`;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');
    
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">${message}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;
    toastContainer.appendChild(toast);
    
    const bsToast = new bootstrap.Toast(toast, { delay: 4000 });
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
        
        const data = await response.json();
        
        if (!response.ok) {
            let errorMsg = data.message || 'Error en la petición';
            if (data.validationErrors) {
                errorMsg = Object.values(data.validationErrors).join(' - ');
            }
            throw new Error(errorMsg);
        }
        return data;
    } catch (error) {
        showAlert(error.message, 'error');
        throw error;
    }
}

// Formateadores
const formatCOP = (num) => new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(num);
const formatUSD = (num) => new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(num);
const formatFecha = (fecha) => new Date(fecha).toLocaleString('es-CO');

// --- CLIENTES ---
async function cargarClientes() {
    try {
        const clientes = await fetchAPI('/clientes');
        const tbody = document.getElementById('tbodyClientes');
        tbody.innerHTML = clientes.map(c => `
            <tr>
                <td>${c.id}</td>
                <td>${c.cedula}</td>
                <td>${c.nombre} ${c.apellido}</td>
                <td>${c.email}</td>
                <td>${c.telefono}</td>
            </tr>
        `).join('');
    } catch (e) {}
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
function badgeEstado(estado) {
    if(estado === 'DISPONIBLE') return `<span class="badge bg-success">DISPONIBLE</span>`;
    if(estado === 'VENDIDO') return `<span class="badge bg-secondary">VENDIDO</span>`;
    if(estado === 'EN_MANTENIMIENTO') return `<span class="badge bg-warning text-dark">MANTENIMIENTO</span>`;
    return `<span class="badge bg-light text-dark">${estado}</span>`;
}

async function cargarVehiculos() {
    const soloDisponibles = document.getElementById('chkDisponibles').checked;
    const url = soloDisponibles ? '/vehiculos/disponibles' : '/vehiculos';
    try {
        const vehiculos = await fetchAPI(url);
        const tbody = document.getElementById('tbodyVehiculos');
        tbody.innerHTML = vehiculos.map(v => `
            <tr>
                <td>${v.id}</td>
                <td><strong>${v.placa}</strong></td>
                <td>${v.marca} ${v.modelo}</td>
                <td>${v.anio}</td>
                <td>${formatCOP(v.precio)}</td>
                <td>${badgeEstado(v.estado)}</td>
            </tr>
        `).join('');
    } catch (e) {}
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
            clientes.map(c => `<option value="${c.id}">${c.cedula} - ${c.nombre} ${c.apellido}</option>`).join('');
            
        document.getElementById('venVehiculoId').innerHTML = `<option value="">Seleccione Vehículo...</option>` + 
            vehiculos.map(v => `<option value="${v.id}">${v.placa} - ${v.marca} ${v.modelo} (${formatCOP(v.precio)})</option>`).join('');
    } catch (e) {}
}

async function cargarVentas() {
    try {
        const ventas = await fetchAPI('/ventas');
        const tbody = document.getElementById('tbodyVentas');
        tbody.innerHTML = ventas.map(v => `
            <tr>
                <td>${v.id}</td>
                <td>${formatFecha(v.fechaVenta)}</td>
                <td>${v.cliente.nombre} ${v.cliente.apellido}</td>
                <td>${v.vehiculo.placa}</td>
                <td>${formatCOP(v.precioBase)}</td>
                <td>${v.porcentajeDescuento}%</td>
                <td><strong>${formatCOP(v.totalPagado)}</strong></td>
                <td class="text-success fw-bold">${v.conversionUsd ? formatUSD(v.conversionUsd.montoUsd) : 'N/A'}</td>
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
        showAlert(`Venta registrada exitosamente. Total: ${formatCOP(result.totalPagado)} (${formatUSD(result.conversionUsd.montoUsd)})`);
        e.target.reset();
        cargarVentas();
        cargarCombosVenta(); // Refrescar combos (el vehículo ya no debe salir)
    } catch (e) {}
}

// --- MANTENIMIENTOS ---
async function cargarCombosMantenimiento() {
    try {
        // En mantenimiento sí podríamos querer buscar cualquier vehículo, 
        // pero por simplicidad listamos todos los que NO estén vendidos
        const vehiculos = await fetchAPI('/vehiculos');
        const elegibles = vehiculos.filter(v => v.estado !== 'VENDIDO');
        
        document.getElementById('manVehiculoId').innerHTML = `<option value="">Seleccione Vehículo...</option>` + 
            elegibles.map(v => `<option value="${v.id}">${v.placa} - ${v.marca} ${v.modelo}</option>`).join('');
    } catch (e) {}
}

async function cargarMantenimientos() {
    try {
        const mants = await fetchAPI('/mantenimientos');
        const tbody = document.getElementById('tbodyMantenimientos');
        tbody.innerHTML = mants.map(m => `
            <tr>
                <td>${m.id}</td>
                <td>${formatFecha(m.fechaIngreso)}</td>
                <td>${m.vehiculo.placa} - ${m.vehiculo.marca}</td>
                <td>${m.tipo}</td>
                <td>${m.estado}</td>
                <td>${formatCOP(m.costo)}</td>
            </tr>
        `).join('');
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
    } catch (e) {}
}

// Inicialización
document.addEventListener('DOMContentLoaded', () => {
    cargarClientes();
});
