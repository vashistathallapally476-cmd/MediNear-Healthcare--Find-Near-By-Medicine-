/**
 * MediNear Pharmacy Owner Management Dashboard
 * Handles store registration, viewing stores, inventory CRUD, and stock modification
 */

const StoreOwner = {
    activeStoreId: null,
    stores: [],

    init() {
        this.bindEvents();
    },

    bindEvents() {
        const navBtnOwner = document.getElementById('nav-btn-owner');
        const navBtnCustomer = document.getElementById('nav-btn-customer');
        const btnOpenAddStore = document.getElementById('btn-open-add-store');
        const btnOpenAddStock = document.getElementById('btn-open-add-stock');
        const storeSelect = document.getElementById('owner-store-selector');

        const addStoreForm = document.getElementById('form-add-store');
        const addStockForm = document.getElementById('form-add-stock');
        const closeStoreModal = document.getElementById('btn-close-store-modal');
        const closeStockModal = document.getElementById('btn-close-stock-modal');

        if (navBtnOwner) {
            navBtnOwner.addEventListener('click', () => this.showOwnerDashboard());
        }
        if (navBtnCustomer) {
            navBtnCustomer.addEventListener('click', () => this.showCustomerView());
        }

        if (btnOpenAddStore) {
            btnOpenAddStore.addEventListener('click', () => this.showModal('store-modal'));
        }
        if (btnOpenAddStock) {
            btnOpenAddStock.addEventListener('click', () => {
                if (!this.activeStoreId) {
                    API.showToast('Please select or register a store first', 'warning');
                    return;
                }
                this.showModal('stock-modal');
            });
        }

        if (closeStoreModal) closeStoreModal.addEventListener('click', () => this.hideModal('store-modal'));
        if (closeStockModal) closeStockModal.addEventListener('click', () => this.hideModal('stock-modal'));

        if (storeSelect) {
            storeSelect.addEventListener('change', (e) => {
                const storeId = parseInt(e.target.value);
                if (storeId) {
                    this.activeStoreId = storeId;
                    this.loadStoreInventory(storeId);
                }
            });
        }

        if (addStoreForm) {
            addStoreForm.addEventListener('submit', (e) => this.handleRegisterStore(e));
        }
        if (addStockForm) {
            addStockForm.addEventListener('submit', (e) => this.handleUpsertStock(e));
        }
    },

    showOwnerDashboard() {
        const user = API.getUser();
        if (!user || user.role !== 'OWNER') {
            API.showToast('You must be logged in as a Pharmacy Owner to access this area.', 'warning');
            if (window.Auth) window.Auth.showModal('login');
            return;
        }

        const customerSec = document.getElementById('customer-section');
        const ownerSec = document.getElementById('owner-section');

        if (customerSec) customerSec.classList.add('hidden');
        if (ownerSec) ownerSec.classList.remove('hidden');

        this.loadOwnerStores();
    },

    showCustomerView() {
        const customerSec = document.getElementById('customer-section');
        const ownerSec = document.getElementById('owner-section');

        if (ownerSec) ownerSec.classList.add('hidden');
        if (customerSec) customerSec.classList.remove('hidden');
    },

    async loadOwnerStores() {
        try {
            const res = await API.get(CONFIG.ENDPOINTS.STORES_MY);
            if (res.success && Array.isArray(res.data)) {
                this.stores = res.data;
                this.renderStoreSelector(this.stores);

                if (this.stores.length > 0) {
                    this.activeStoreId = this.stores[0].storeId;
                    this.loadStoreInventory(this.activeStoreId);
                } else {
                    this.renderEmptyStores();
                }
            }
        } catch (err) {
            API.showToast(err.message || 'Could not load your pharmacies', 'error');
        }
    },

    renderStoreSelector(stores) {
        const select = document.getElementById('owner-store-selector');
        if (!select) return;

        select.innerHTML = '';
        stores.forEach(s => {
            const opt = document.createElement('option');
            opt.value = s.storeId;
            opt.textContent = `${s.storeName} (${s.address})`;
            select.appendChild(opt);
        });

        if (this.activeStoreId) {
            select.value = this.activeStoreId;
        }
    },

    async loadStoreInventory(storeId) {
        const tableBody = document.getElementById('owner-inventory-tbody');
        const emptyState = document.getElementById('owner-inventory-empty');
        if (!tableBody) return;

        tableBody.innerHTML = '<tr><td colspan="5" class="text-center"><i class="fas fa-spinner fa-spin"></i> Loading stock items...</td></tr>';

        try {
            const res = await API.get(`${CONFIG.ENDPOINTS.INVENTORY_BY_STORE}/${storeId}`);
            if (res.success && Array.isArray(res.data)) {
                const items = res.data;
                if (items.length === 0) {
                    tableBody.innerHTML = '';
                    if (emptyState) emptyState.classList.remove('hidden');
                } else {
                    if (emptyState) emptyState.classList.add('hidden');
                    this.renderInventoryTable(items);
                }
            }
        } catch (err) {
            tableBody.innerHTML = `<tr><td colspan="5" class="text-danger text-center">Error: ${err.message}</td></tr>`;
        }
    },

    renderInventoryTable(items) {
        const tbody = document.getElementById('owner-inventory-tbody');
        if (!tbody) return;

        tbody.innerHTML = '';

        items.forEach(item => {
            const tr = document.createElement('tr');

            let badgeClass = 'badge-success';
            if (item.stockStatus === 'CRITICAL' || item.quantity === 0) badgeClass = 'badge-danger';
            else if (item.stockStatus === 'LOW') badgeClass = 'badge-warning';

            tr.innerHTML = `
                <td><strong>${item.medicineName}</strong></td>
                <td><span class="stock-badge ${badgeClass}">${item.stockStatus}</span></td>
                <td>
                    <input type="number" min="0" value="${item.quantity}" class="inline-qty-input" id="qty-input-${item.inventoryId}" />
                </td>
                <td><small class="text-muted">${item.lastUpdated ? new Date(item.lastUpdated).toLocaleString() : 'Just now'}</small></td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="StoreOwner.updateQuantity(${item.inventoryId}, '${item.medicineName}')">
                        <i class="fas fa-save"></i> Save
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="StoreOwner.deleteInventoryItem(${item.inventoryId})">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            `;

            tbody.appendChild(tr);
        });
    },

    renderEmptyStores() {
        const tbody = document.getElementById('owner-inventory-tbody');
        const emptyState = document.getElementById('owner-inventory-empty');
        if (tbody) tbody.innerHTML = '';
        if (emptyState) {
            emptyState.classList.remove('hidden');
            emptyState.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-clinic-medical empty-icon"></i>
                    <h3>No Pharmacies Registered Yet</h3>
                    <p>Click "Register New Store" above to add your pharmacy location to MediNear.</p>
                </div>
            `;
        }
    },

    async handleRegisterStore(e) {
        e.preventDefault();
        const storeName = document.getElementById('new-store-name').value.trim();
        const address = document.getElementById('new-store-address').value.trim();
        const phone = document.getElementById('new-store-phone').value.trim();
        const latitude = parseFloat(document.getElementById('new-store-lat').value);
        const longitude = parseFloat(document.getElementById('new-store-lng').value);

        if (!storeName || !address || !phone || isNaN(latitude) || isNaN(longitude)) {
            API.showToast('Please fill all store fields with valid coordinates', 'warning');
            return;
        }

        try {
            const res = await API.post(CONFIG.ENDPOINTS.STORES, {
                storeName,
                address,
                phone,
                latitude,
                longitude
            });

            if (res.success && res.data) {
                API.showToast('Store registered successfully!', 'success');
                this.hideModal('store-modal');
                document.getElementById('form-add-store').reset();
                this.loadOwnerStores();
            } else {
                API.showToast(res.message || 'Could not register store', 'error');
            }
        } catch (err) {
            API.showToast(err.message || 'Registration failed', 'error');
        }
    },

    async handleUpsertStock(e) {
        e.preventDefault();
        const medicineName = document.getElementById('new-stock-med-name').value.trim();
        const quantity = parseInt(document.getElementById('new-stock-qty').value);

        if (!medicineName || isNaN(quantity)) {
            API.showToast('Please enter medicine name and valid quantity', 'warning');
            return;
        }

        try {
            const res = await API.put(CONFIG.ENDPOINTS.INVENTORY, {
                storeId: this.activeStoreId,
                medicineName,
                quantity
            });

            if (res.success) {
                API.showToast(`Stock updated for ${medicineName}! Broadcasted to live map.`, 'success');
                this.hideModal('stock-modal');
                document.getElementById('form-add-stock').reset();
                this.loadStoreInventory(this.activeStoreId);
            } else {
                API.showToast(res.message || 'Stock update failed', 'error');
            }
        } catch (err) {
            API.showToast(err.message || 'Stock update failed', 'error');
        }
    },

    async updateQuantity(inventoryId, medicineName) {
        const input = document.getElementById(`qty-input-${inventoryId}`);
        if (!input) return;

        const quantity = parseInt(input.value);
        if (isNaN(quantity) || quantity < 0) {
            API.showToast('Please enter a valid non-negative number', 'warning');
            return;
        }

        try {
            const res = await API.put(CONFIG.ENDPOINTS.INVENTORY, {
                storeId: this.activeStoreId,
                medicineName,
                quantity
            });

            if (res.success) {
                API.showToast(`Stock for ${medicineName} updated to ${quantity}!`, 'success');
                this.loadStoreInventory(this.activeStoreId);
            }
        } catch (err) {
            API.showToast(err.message || 'Update failed', 'error');
        }
    },

    async deleteInventoryItem(inventoryId) {
        if (!confirm('Are you sure you want to remove this medicine from your store inventory?')) {
            return;
        }

        try {
            const res = await API.delete(`${CONFIG.ENDPOINTS.INVENTORY}/${inventoryId}`);
            if (res.success) {
                API.showToast('Item deleted from inventory', 'info');
                this.loadStoreInventory(this.activeStoreId);
            }
        } catch (err) {
            API.showToast(err.message || 'Could not delete item', 'error');
        }
    },

    showModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) modal.classList.add('active');
    },

    hideModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) modal.classList.remove('active');
    }
};

window.StoreOwner = StoreOwner;
document.addEventListener('DOMContentLoaded', () => StoreOwner.init());
