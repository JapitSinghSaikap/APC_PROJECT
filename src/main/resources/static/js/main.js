// ------------------ GLOBAL STATE ------------------ //
let allOrders = [];
let currentOrder = null;
let orderItemIndex = 0;
let availableProducts = [];
let availableSuppliers = [];

// ------------------ CONFIG ------------------ //
const API_BASE_URL = '/api';

// ------------------ API CLIENT WITH JWT ------------------ //
class ApiClient {
    static getToken() { return localStorage.getItem('jwtToken'); }

    static async get(endpoint) { return this.request(endpoint, 'GET'); }
    static async post(endpoint, data) { return this.request(endpoint, 'POST', data); }
    static async put(endpoint, data) { return this.request(endpoint, 'PUT', data); }
    static async delete(endpoint) { return this.request(endpoint, 'DELETE'); }

    static async request(endpoint, method, data = null) {
        try {
            const options = {
                method,
                headers: {
                    'Authorization': `Bearer ${this.getToken()}`,
                    'Content-Type': 'application/json'
                }
            };
            if (data) options.body = JSON.stringify(data);

            const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    alert('Session expired or unauthorized. Please log in again.');
                    localStorage.removeItem('jwtToken');
                    window.location.href = '/login.html';
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.status === 204 ? null : await response.json();
        } catch (error) {
            console.error(`${method} request failed:`, error);
            throw error;
        }
    }
}

// ------------------ DASHBOARD FUNCTIONS ------------------ //
async function loadDashboardStats() {
    try {
        const products = await ApiClient.get('/products');
        document.getElementById('total-products').textContent = products.length || 0;

        const lowStockItems = products.filter(p => p.stockQuantity <= p.minStockLevel);
        document.getElementById('low-stock-items').textContent = lowStockItems.length || 0;

        const suppliers = await ApiClient.get('/suppliers');
        const activeSuppliers = suppliers.filter(s => s.status === 'ACTIVE');
        document.getElementById('total-suppliers').textContent = activeSuppliers.length || 0;

        const orders = await ApiClient.get('/orders');
        allOrders = orders || [];
        const pendingOrders = orders.filter(o => o.status === 'PENDING');
        document.getElementById('pending-orders').textContent = pendingOrders.length || 0;
    } catch (error) {
        console.error('Failed to load dashboard stats:', error);
        ['total-products','low-stock-items','total-suppliers','pending-orders'].forEach(id => document.getElementById(id).textContent = '0');
    }
}

// ------------------ USER EMAIL ------------------ //
async function loadUserEmail() {
    const userEmailDiv = document.getElementById('user-email');
    const token = ApiClient.getToken();

     console.log('JWT Token:', token);

    if (!token) {
        window.location.href = '/login.html';
        return;
    }

    // Try localStorage first
    let storedEmail = localStorage.getItem('userEmail');
    if (storedEmail) {
        userEmailDiv.textContent = storedEmail;
        return;
    }

    // Fetch from API
    try {
        const user = await ApiClient.get('/auth/me');
        userEmailDiv.textContent = user.email || 'Unknown User';
        localStorage.setItem('userEmail', user.email);
    } catch (error) {
        console.error('Failed to fetch user info:', error);
        userEmailDiv.textContent = 'Not logged in';
    }
}

// ------------------ SIGN OUT ------------------ //
function initSignOut() {
    const signoutBtn = document.getElementById('signout-btn');
    if(signoutBtn){
        signoutBtn.addEventListener('click', () => {
            localStorage.removeItem('jwtToken');
            localStorage.removeItem('userEmail');
            window.location.href = '/login.html';
        });
    }
}

// ------------------ PRODUCTS ------------------ //
async function loadProducts() {
    try {
        const products = await ApiClient.get('/products');
        availableProducts = products || [];
        displayProducts(products);
    } catch (error) {
        console.error('Failed to load products:', error);
        showError('Failed to load products. Please try again.');
    }
}

function displayProducts(products) {
    const tableBody = document.querySelector('#products-table tbody');
    if (!tableBody) return;

    if(products.length === 0){
        tableBody.innerHTML = '<tr><td colspan="8" class="text-center">No products found.</td></tr>';
        return;
    }

    tableBody.innerHTML = products.map(product => `
        <tr>
            <td>${product.name}</td>
            <td>${product.sku}</td>
            <td>${product.category}</td>
            <td class="${product.stockQuantity <= product.minStockLevel ? 'error' : ''}">${product.stockQuantity}</td>
            <td>${product.minStockLevel}</td>
            <td>$${product.price}</td>
            <td>${product.warehouse?.name || 'N/A'}</td>
            <td class="table-actions">
                <button class="btn btn-sm btn-secondary" onclick="editProduct(${product.id})"><i class="fas fa-edit"></i> Edit</button>
                <button class="btn btn-sm btn-danger" onclick="deleteProduct(${product.id})"><i class="fas fa-trash"></i> Delete</button>
            </td>
        </tr>
    `).join('');
}

async function addProduct(productData) {
    try {
        await ApiClient.post('/products', productData);
        showSuccess('Product added successfully!');
        loadProducts();
        closeModal('product-modal');
    } catch (error) {
        console.error('Failed to add product:', error);
        showError('Failed to add product. Please try again.');
    }
}

async function editProduct(productId) {
    try {
        await loadWarehouseOptions();
        await loadSupplierOptions();
        const product = await ApiClient.get(`/products/${productId}`);
        document.getElementById('modal-title').textContent = 'Edit Product';
        populateProductForm(product);
        openModal('product-modal');
    } catch (error) {
        console.error('Failed to load product:', error);
        showError('Failed to load product details.');
    }
}

async function deleteProduct(productId) {
    if (!confirm('Are you sure you want to delete this product?')) return;
    try {
        await ApiClient.delete(`/products/${productId}`);
        showSuccess('Product deleted successfully!');
        loadProducts();
    } catch (error) {
        console.error('Failed to delete product:', error);
        showError('Failed to delete product. Please try again.');
    }
}

// ------------------ SUPPLIERS & WAREHOUSES ------------------ //
async function loadSuppliers() {
    try {
        const suppliers = await ApiClient.get('/suppliers');
        availableSuppliers = suppliers || [];
        displaySuppliers(suppliers);
    } catch (error) {
        console.error('Failed to load suppliers:', error);
        showError('Failed to load suppliers. Please try again.');
    }
}

function displaySuppliers(suppliers) {
    const tableBody = document.querySelector('#suppliers-table tbody');
    if (!tableBody) return;

    tableBody.innerHTML = suppliers.map(supplier => `
        <tr>
            <td>${supplier.name}</td>
            <td>${supplier.email || 'N/A'}</td>
            <td>${supplier.phone || 'N/A'}</td>
            <td>${supplier.contactPerson || 'N/A'}</td>
            <td><span class="order-status status-${supplier.status.toLowerCase()}">${supplier.status}</span></td>
            <td class="table-actions">
                <button class="btn btn-sm btn-secondary" onclick="editSupplier(${supplier.id})"><i class="fas fa-edit"></i> Edit</button>
                <button class="btn btn-sm btn-danger" onclick="deleteSupplier(${supplier.id})"><i class="fas fa-trash"></i> Delete</button>
            </td>
        </tr>
    `).join('');
}

async function loadWarehouses() {
    try {
        const warehouses = await ApiClient.get('/warehouses');
        displayWarehouses(warehouses);
    } catch (error) {
        console.error('Failed to load warehouses:', error);
        showError('Failed to load warehouses. Please try again.');
    }
}

function displayWarehouses(warehouses) {
    const tableBody = document.querySelector('#warehouses-table tbody');
    if (!tableBody) return;

    tableBody.innerHTML = warehouses.map(w => `
        <tr>
            <td>${w.name}</td>
            <td>${w.location}</td>
            <td>${w.products?.length || 0}</td>
            <td>${formatDate(w.createdAt)}</td>
            <td class="table-actions">
                <button class="btn btn-sm btn-secondary" onclick="editWarehouse(${w.id})"><i class="fas fa-edit"></i> Edit</button>
                <button class="btn btn-sm btn-danger" onclick="deleteWarehouse(${w.id})"><i class="fas fa-trash"></i> Delete</button>
            </td>
        </tr>
    `).join('');
}

// ------------------ FORM MODALS & HELPERS ------------------ //
function openModal(id) { const m=document.getElementById(id); if(m)m.style.display='block'; }
function closeModal(id) { const m=document.getElementById(id); if(m){ m.style.display='none'; const f=m.querySelector('form'); if(f)f.reset(); } }

function populateProductForm(product){
    document.getElementById('product-name').value=product.name||'';
    document.getElementById('product-sku').value=product.sku||'';
    document.getElementById('product-category').value=product.category||'';
    document.getElementById('product-price').value=product.price||'';
    document.getElementById('product-stock').value=product.stockQuantity||'';
    document.getElementById('product-min-stock').value=product.minStockLevel||'';
    document.getElementById('product-warehouse').value=product.warehouse?.id||'';
    document.getElementById('product-supplier').value=product.supplier?.id||'';
    document.getElementById('product-description').value=product.description||'';
}

async function loadWarehouseOptions() {
    try {
        const warehouses = await ApiClient.get('/warehouses');
        const select = document.getElementById('product-warehouse');
        if(!select) return;
        select.innerHTML='<option value="">Select Warehouse</option>';
        warehouses.forEach(w=>{ const option = document.createElement('option'); option.value = w.id; option.textContent = w.name; select.appendChild(option); });
    } catch (e) { console.error('Failed to load warehouses:', e); }
}

async function loadSupplierOptions() {
    try {
        const suppliers = await ApiClient.get('/suppliers');
        const select = document.getElementById('product-supplier');
        if(!select) return;
        select.innerHTML='<option value="">Select Supplier</option>';
        suppliers.forEach(s=>{ const option = document.createElement('option'); option.value = s.id; option.textContent = s.name; select.appendChild(option); });
    } catch (e) { console.error('Failed to load suppliers:', e); }
}

// ------------------ UTILITIES ------------------ //
function formatDate(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return isNaN(d) ? dateStr : d.toISOString().split('T')[0];
}

function showSuccess(msg) { showNotification(msg, 'success'); }
function showError(msg) { showNotification(msg, 'error'); }
function showNotification(message, type) {
    const existing = document.querySelector('.notification'); if(existing) existing.remove();
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    notification.style.cssText = `position: fixed; top: 20px; right: 20px; padding: 1rem 1.5rem; border-radius: 5px; z-index: 3000; max-width: 300px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);`;
    document.body.appendChild(notification);
    setTimeout(()=>notification.remove(),3000);
}

function validateRequired(formId) {
    const form = document.getElementById(formId);
    if (!form) return false;
    let isValid = true;
    form.querySelectorAll('[required]').forEach(f=>{
        if(!f.value.trim()){ f.style.borderColor='#e53e3e'; isValid=false; }
        else f.style.borderColor='#e2e8f0';
    });
    return isValid;
}

// ------------------ CSV REPORT FUNCTIONALITY ------------------ //
function downloadCSV(data, filename = 'report.csv') {
    if (!data || data.length === 0) { alert('No data available to export.'); return; }

    const csvRows = [];
    const headers = Object.keys(data[0]);
    csvRows.push(headers.join(','));

    data.forEach(row => { csvRows.push(headers.map(h => `"${row[h]}"`).join(',')); });

    const csvString = csvRows.join('\n');
    const blob = new Blob([csvString], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.setAttribute('hidden', '');
    a.setAttribute('href', url);
    a.setAttribute('download', filename);
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
}

async function generateReport() {
    try {
        if (!allOrders || allOrders.length === 0) {
            alert('No orders available to export.');
            return;
        }

        const statusFilter = document.getElementById('statusFilter')?.value || '';
        const typeFilter = document.getElementById('typeFilter')?.value || '';
        const supplierFilter = document.getElementById('supplierFilter')?.value || '';
        const searchTerm = document.getElementById('searchInput')?.value.toLowerCase() || '';

        const filteredOrders = allOrders.filter(order => {
            const matchesStatus = !statusFilter || order.status === statusFilter;
            const matchesType = !typeFilter || order.type === typeFilter;
            const matchesSupplier = !supplierFilter || (order.supplier && order.supplier.id.toString() === supplierFilter);
            const matchesSearch = !searchTerm || order.orderNumber.toLowerCase().includes(searchTerm);
            return matchesStatus && matchesType && matchesSupplier && matchesSearch;
        });

        if (filteredOrders.length === 0) {
            alert('No orders match the current filters.');
            return;
        }

        const csvData = filteredOrders.map(order => ({
            'Order Number': order.orderNumber,
            'Type': order.type,
            'Status': order.status,
            'Supplier': order.supplier ? order.supplier.name : 'N/A',
            'Order Date': formatDate(order.orderDate),
            'Total Amount': (order.totalAmount || 0).toFixed(2)
        }));

        downloadCSV(csvData, 'orders-report.csv');
        alert('CSV report generated successfully!');
    } catch (error) {
        console.error('Failed to generate report:', error);
        alert('Failed to generate report. Please try again.');
    }
}

// ------------------ DASHBOARD ALERTS & RECENT ORDERS ------------------ //
async function loadLowStockAlerts() {
    try {
        const alertsResponse = await ApiClient.get('/dashboard/alerts');
        const allAlerts = [
            ...(alertsResponse.productAlerts || []),
            ...(alertsResponse.supplierAlerts || []),
            ...(alertsResponse.orderAlerts || []),
            ...(alertsResponse.warehouseAlerts || [])
        ];

        const alertsContainer = document.getElementById('low-stock-alerts');
        if (!alertsContainer) return;

        if (allAlerts.length === 0) {
            alertsContainer.innerHTML = '<p class="text-center" style="color: #22543d;">✓ All systems are running smoothly!</p>';
            return;
        }

        alertsContainer.innerHTML = allAlerts.map(alert => `
            <div class="alert-item">
                <i class="fas fa-exclamation-triangle"></i>
                <div><span>${alert}</span></div>
            </div>
        `).join('');

    } catch (error) {
        console.error('Failed to load low stock alerts:', error);
        const alertsContainer = document.getElementById('low-stock-alerts');
        if (alertsContainer) alertsContainer.innerHTML = '<p class="error">Failed to load alerts. Please try again.</p>';
    }
}

async function loadRecentOrders() {
    try {
        const orders = await ApiClient.get('/orders');
        const recentOrders = (orders || []).sort((a,b)=>new Date(b.createdAt)-new Date(a.createdAt)).slice(0,5);

        const ordersContainer = document.getElementById('recent-orders');
        if (!ordersContainer) return;

        if (recentOrders.length === 0) {
            ordersContainer.innerHTML = '<p class="text-center">No recent orders found.</p>';
            return;
        }

        ordersContainer.innerHTML = recentOrders.map(order => `
            <div class="order-item">
                <div class="order-info">
                    <h4>${order.orderNumber}</h4>
                    <p>${formatDate(order.orderDate)} • $${order.totalAmount || '0.00'}</p>
                </div>
                <span class="order-status status-${order.status.toLowerCase()}">${order.status}</span>
            </div>
        `).join('');

    } catch (error) {
        console.error('Failed to load recent orders:', error);
        const ordersContainer = document.getElementById('recent-orders');
        if (ordersContainer) ordersContainer.innerHTML = '<p class="error">Failed to load orders. Please try again.</p>';
    }
}

// ------------------ INITIAL LOAD ------------------ //
document.addEventListener('DOMContentLoaded', async function() {
    await loadUserEmail();
    initSignOut();

    await loadDashboardStats();
    await loadProducts();
    await loadSuppliers();
    await loadLowStockAlerts();
    await loadRecentOrders();
    await loadWarehouses();

    const hamburger = document.querySelector('.hamburger');
    const navMenu = document.querySelector('.nav-menu');
    if(hamburger && navMenu){
        hamburger.addEventListener('click', ()=>{hamburger.classList.toggle('active'); navMenu.classList.toggle('active');});
        document.querySelectorAll('.nav-link').forEach(n=>n.addEventListener('click',()=>{hamburger.classList.remove('active');navMenu.classList.remove('active');}));
    }

    window.addEventListener('click', e => { if(e.target.classList.contains('modal')) e.target.style.display='none'; });
});
