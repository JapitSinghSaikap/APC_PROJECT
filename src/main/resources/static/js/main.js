// API Base URL
const API_BASE_URL = '/api';

// Utility functions
class ApiClient {
    static async get(endpoint) {
        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error('GET request failed:', error);
            throw error;
        }
    }

    static async post(endpoint, data) {
        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data)
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error('POST request failed:', error);
            throw error;
        }
    }

    static async put(endpoint, data) {
        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data)
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error('PUT request failed:', error);
            throw error;
        }
    }

    static async delete(endpoint) {
        try {
            const response = await fetch(`${API_BASE_URL}${endpoint}`, {
                method: 'DELETE'
            });
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.status === 204 ? null : await response.json();
        } catch (error) {
            console.error('DELETE request failed:', error);
            throw error;
        }
    }
}

// Dashboard functions
async function loadDashboardStats() {
    try {
        // Load total products
        const products = await ApiClient.get('/products');
        document.getElementById('total-products').textContent = products.length || 0;

        // Calculate low stock items
        const lowStockItems = products.filter(product => product.stockQuantity <= product.minStockLevel);
        document.getElementById('low-stock-items').textContent = lowStockItems.length || 0;

        // Load suppliers
        const suppliers = await ApiClient.get('/suppliers');
        const activeSuppliers = suppliers.filter(supplier => supplier.status === 'ACTIVE');
        document.getElementById('total-suppliers').textContent = activeSuppliers.length || 0;

        // Load orders
        const orders = await ApiClient.get('/orders');
        const pendingOrders = orders.filter(order => order.status === 'PENDING');
        document.getElementById('pending-orders').textContent = pendingOrders.length || 0;

    } catch (error) {
        console.error('Failed to load dashboard stats:', error);
        // Show fallback data
        document.getElementById('total-products').textContent = '0';
        document.getElementById('low-stock-items').textContent = '0';
        document.getElementById('total-suppliers').textContent = '0';
        document.getElementById('pending-orders').textContent = '0';
    }
}

async function loadLowStockAlerts() {
    try {
        const alerts = await ApiClient.get('/dashboard/alerts');
        const allAlerts = [
            ...alerts.productAlerts,
            ...alerts.supplierAlerts,
            ...alerts.orderAlerts,
            ...alerts.warehouseAlerts
        ];
        
        const alertsContainer = document.getElementById('low-stock-alerts');
        
        if (allAlerts.length === 0) {
            alertsContainer.innerHTML = '<p class="text-center" style="color: #22543d;">✓ All systems are running smoothly!</p>';
            return;
        }

        alertsContainer.innerHTML = allAlerts.map(alert => `
            <div class="alert-item">
                <i class="fas fa-exclamation-triangle"></i>
                <div>
                    <span>${alert}</span>
                </div>
            </div>
        `).join('');

    } catch (error) {
        console.error('Failed to load alerts:', error);
        document.getElementById('low-stock-alerts').innerHTML = 
            '<p class="error">Failed to load alerts. Please try again.</p>';
    }
}

async function loadRecentOrders() {
    try {
        const orders = await ApiClient.get('/orders');
        const recentOrders = orders
            .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
            .slice(0, 5);

        const ordersContainer = document.getElementById('recent-orders');
        
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
        document.getElementById('recent-orders').innerHTML = 
            '<p class="error">Failed to load orders. Please try again.</p>';
    }
}

// Product management functions
async function loadProducts() {
    try {
        const products = await ApiClient.get('/products');
        displayProducts(products);
    } catch (error) {
        console.error('Failed to load products:', error);
        showError('Failed to load products. Please try again.');
    }
}

function displayProducts(products) {
    const tableBody = document.querySelector('#products-table tbody');
    if (!tableBody) return;

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
                <button class="btn btn-sm btn-secondary" onclick="editProduct(${product.id})">
                    <i class="fas fa-edit"></i> Edit
                </button>
                <button class="btn btn-sm btn-danger" onclick="deleteProduct(${product.id})">
                    <i class="fas fa-trash"></i> Delete
                </button>
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
        // Load dropdowns first
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

// Supplier management functions
async function loadSuppliers() {
    try {
        const suppliers = await ApiClient.get('/suppliers');
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
                <button class="btn btn-sm btn-secondary" onclick="editSupplier(${supplier.id})">
                    <i class="fas fa-edit"></i> Edit
                </button>
                <button class="btn btn-sm btn-danger" onclick="deleteSupplier(${supplier.id})">
                    <i class="fas fa-trash"></i> Delete
                </button>
            </td>
        </tr>
    `).join('');
}

// Order management functions
async function loadOrders() {
    try {
        const orders = await ApiClient.get('/orders');
        displayOrders(orders);
    } catch (error) {
        console.error('Failed to load orders:', error);
        showError('Failed to load orders. Please try again.');
    }
}

function displayOrders(orders) {
    const tableBody = document.querySelector('#orders-table tbody');
    if (!tableBody) return;

    tableBody.innerHTML = orders.map(order => `
        <tr>
            <td>${order.orderNumber}</td>
            <td>${order.type}</td>
            <td>${order.supplier?.name || 'N/A'}</td>
            <td>$${order.totalAmount || '0.00'}</td>
            <td>${formatDate(order.orderDate)}</td>
            <td><span class="order-status status-${order.status.toLowerCase()}">${order.status}</span></td>
            <td class="table-actions">
                <button class="btn btn-sm btn-secondary" onclick="viewOrder(${order.id})">
                    <i class="fas fa-eye"></i> View
                </button>
                <button class="btn btn-sm btn-primary" onclick="updateOrderStatus(${order.id})">
                    <i class="fas fa-edit"></i> Update
                </button>
            </td>
        </tr>
    `).join('');
}

// Warehouse management functions
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

    tableBody.innerHTML = warehouses.map(warehouse => `
        <tr>
            <td>${warehouse.name}</td>
            <td>${warehouse.location}</td>
            <td>${warehouse.products?.length || 0}</td>
            <td>${formatDate(warehouse.createdAt)}</td>
            <td class="table-actions">
                <button class="btn btn-sm btn-secondary" onclick="editWarehouse(${warehouse.id})">
                    <i class="fas fa-edit"></i> Edit
                </button>
                <button class="btn btn-sm btn-danger" onclick="deleteWarehouse(${warehouse.id})">
                    <i class="fas fa-trash"></i> Delete
                </button>
            </td>
        </tr>
    `).join('');
}

// Utility functions
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
}

function showSuccess(message) {
    showNotification(message, 'success');
}

function showError(message) {
    showNotification(message, 'error');
}

function showNotification(message, type) {
    // Remove existing notifications
    const existing = document.querySelector('.notification');
    if (existing) existing.remove();

    // Create notification
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 1rem 1.5rem;
        border-radius: 5px;
        z-index: 3000;
        max-width: 300px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    `;

    document.body.appendChild(notification);

    // Auto remove after 3 seconds
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
        }
    }, 3000);
}

// Modal functions
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'block';
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
        // Reset form if exists
        const form = modal.querySelector('form');
        if (form) form.reset();
    }
}

// Navigation
document.addEventListener('DOMContentLoaded', function() {
    // Mobile menu toggle
    const hamburger = document.querySelector('.hamburger');
    const navMenu = document.querySelector('.nav-menu');

    if (hamburger && navMenu) {
        hamburger.addEventListener('click', function() {
            hamburger.classList.toggle('active');
            navMenu.classList.toggle('active');
        });

        // Close menu when clicking on a link
        document.querySelectorAll('.nav-link').forEach(n => n.addEventListener('click', () => {
            hamburger.classList.remove('active');
            navMenu.classList.remove('active');
        }));
    }

    // Close modals when clicking outside
    window.addEventListener('click', function(event) {
        if (event.target.classList.contains('modal')) {
            event.target.style.display = 'none';
        }
    });
});

// Export/Report functions
async function generateReport() {
    try {
        showSuccess('Generating report... (Feature coming soon)');
        // This would typically trigger a report generation API call
        // const report = await ApiClient.get('/reports/inventory');
        // downloadFile(report.url, 'inventory-report.pdf');
    } catch (error) {
        console.error('Failed to generate report:', error);
        showError('Failed to generate report. Please try again.');
    }
}

function downloadFile(url, filename) {
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

// Search and filter functions
function filterTable(tableId, searchTerm) {
    const table = document.getElementById(tableId);
    if (!table) return;

    const rows = table.getElementsByTagName('tr');
    
    for (let i = 1; i < rows.length; i++) { // Skip header row
        const row = rows[i];
        const cells = row.getElementsByTagName('td');
        let found = false;

        for (let j = 0; j < cells.length; j++) {
            if (cells[j].textContent.toLowerCase().includes(searchTerm.toLowerCase())) {
                found = true;
                break;
            }
        }

        row.style.display = found ? '' : 'none';
    }
}

// Form validation
function validateRequired(formId) {
    const form = document.getElementById(formId);
    if (!form) return false;

    const requiredFields = form.querySelectorAll('[required]');
    let isValid = true;

    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            field.style.borderColor = '#e53e3e';
            isValid = false;
        } else {
            field.style.borderColor = '#e2e8f0';
        }
    });

    return isValid;
}

// Load warehouses and suppliers into dropdowns
async function loadWarehouseOptions() {
    try {
        console.log('Fetching warehouses...');
        const warehouses = await ApiClient.get('/warehouses');
        console.log('Warehouses received:', warehouses);
        const warehouseSelect = document.getElementById('product-warehouse');
        if (warehouseSelect) {
            warehouseSelect.innerHTML = '<option value="">Select Warehouse</option>';
            warehouses.forEach(warehouse => {
                warehouseSelect.innerHTML += `<option value="${warehouse.id}">${warehouse.name}</option>`;
            });
            console.log('Warehouse options loaded:', warehouseSelect.children.length);
        } else {
            console.error('Warehouse select element not found!');
        }
    } catch (error) {
        console.error('Failed to load warehouses:', error);
    }
}

async function loadSupplierOptions() {
    try {
        console.log('Fetching suppliers...');
        const suppliers = await ApiClient.get('/suppliers');
        console.log('Suppliers received:', suppliers);
        const supplierSelect = document.getElementById('product-supplier');
        if (supplierSelect) {
            supplierSelect.innerHTML = '<option value="">Select Supplier</option>';
            suppliers.forEach(supplier => {
                supplierSelect.innerHTML += `<option value="${supplier.id}">${supplier.name}</option>`;
            });
            console.log('Supplier options loaded:', supplierSelect.children.length);
        } else {
            console.error('Supplier select element not found!');
        }
    } catch (error) {
        console.error('Failed to load suppliers:', error);
    }
}

// Function to populate form for editing
function populateProductForm(product) {
    document.getElementById('product-name').value = product.name || '';
    document.getElementById('product-sku').value = product.sku || '';
    document.getElementById('product-category').value = product.category || '';
    document.getElementById('product-price').value = product.price || '';
    document.getElementById('product-stock').value = product.stockQuantity || '';
    document.getElementById('product-min-stock').value = product.minStockLevel || '';
    document.getElementById('product-warehouse').value = product.warehouse?.id || '';
    document.getElementById('product-supplier').value = product.supplier?.id || '';
    document.getElementById('product-description').value = product.description || '';
}
