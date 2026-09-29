/**
 * MediNear Medicine Search Module
 * Handles autocomplete, nearby pharmacy search, geolocation, and card rendering
 */

const Search = {
    currentLocation: {
        latitude: CONFIG.DEFAULT_LOCATION.latitude,
        longitude: CONFIG.DEFAULT_LOCATION.longitude,
        detected: false
    },
    currentRadius: CONFIG.SEARCH.DEFAULT_RADIUS_KM,
    activeSubscriptions: new Set(),

    init() {
        this.detectLocation();
        this.bindEvents();
    },

    bindEvents() {
        const searchInput = document.getElementById('search-medicine-input');
        const searchBtn = document.getElementById('btn-search');
        const radiusSelect = document.getElementById('search-radius-select');
        const btnUseLocation = document.getElementById('btn-use-location');

        if (searchInput) {
            let debounceTimer;
            searchInput.addEventListener('input', (e) => {
                clearTimeout(debounceTimer);
                const q = e.target.value.trim();
                if (q.length >= 2) {
                    debounceTimer = setTimeout(() => this.fetchAutocomplete(q), 250);
                } else {
                    this.hideAutocomplete();
                }
            });

            // Enter key to search
            searchInput.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    this.hideAutocomplete();
                    this.executeSearch(searchInput.value.trim());
                }
            });
        }

        if (searchBtn) {
            searchBtn.addEventListener('click', () => {
                const val = searchInput ? searchInput.value.trim() : '';
                this.hideAutocomplete();
                this.executeSearch(val);
            });
        }

        if (radiusSelect) {
            radiusSelect.addEventListener('change', (e) => {
                this.currentRadius = parseFloat(e.target.value) || 5.0;
                const val = searchInput ? searchInput.value.trim() : '';
                if (val) this.executeSearch(val);
            });
        }

        if (btnUseLocation) {
            btnUseLocation.addEventListener('click', () => this.detectLocation(true));
        }

        // Close dropdown when clicking outside
        document.addEventListener('click', (e) => {
            const dropdown = document.getElementById('autocomplete-dropdown');
            const searchBox = document.getElementById('search-medicine-input');
            if (dropdown && e.target !== dropdown && e.target !== searchBox) {
                this.hideAutocomplete();
            }
        });
    },

    detectLocation(notify = false) {
        const locationBadge = document.getElementById('location-status-badge');
        if (!navigator.geolocation) {
            if (locationBadge) locationBadge.textContent = '📍 Hyderabad (Default)';
            return;
        }

        if (locationBadge) locationBadge.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Detecting location...';

        navigator.geolocation.getCurrentPosition(
            (pos) => {
                this.currentLocation.latitude = pos.coords.latitude;
                this.currentLocation.longitude = pos.coords.longitude;
                this.currentLocation.detected = true;
                if (locationBadge) {
                    locationBadge.innerHTML = `📍 GPS Active (${pos.coords.latitude.toFixed(3)}, ${pos.coords.longitude.toFixed(3)})`;
                    locationBadge.classList.add('badge-success');
                }
                if (notify) API.showToast('Location updated from device GPS!', 'success');
            },
            (err) => {
                console.warn('Geolocation error / denied:', err.message);
                this.currentLocation.latitude = CONFIG.DEFAULT_LOCATION.latitude;
                this.currentLocation.longitude = CONFIG.DEFAULT_LOCATION.longitude;
                if (locationBadge) {
                    locationBadge.textContent = '📍 Hyderabad (Default)';
                    locationBadge.classList.remove('badge-success');
                }
                if (notify) API.showToast('Could not access GPS. Using default location.', 'info');
            },
            { timeout: 7000, enableHighAccuracy: true }
        );
    },

    async fetchAutocomplete(query) {
        try {
            const res = await API.get(CONFIG.ENDPOINTS.MEDICINE_AUTOCOMPLETE, { q: query });
            if (res.success && Array.isArray(res.data) && res.data.length > 0) {
                this.renderAutocomplete(res.data);
            } else {
                this.hideAutocomplete();
            }
        } catch (e) {
            this.hideAutocomplete();
        }
    },

    renderAutocomplete(suggestions) {
        const dropdown = document.getElementById('autocomplete-dropdown');
        if (!dropdown) return;

        dropdown.innerHTML = '';
        suggestions.forEach(item => {
            const div = document.createElement('div');
            div.className = 'autocomplete-item';
            div.innerHTML = `<i class="fas fa-pills"></i> <span>${item}</span>`;
            div.addEventListener('click', () => {
                const searchInput = document.getElementById('search-medicine-input');
                if (searchInput) searchInput.value = item;
                this.hideAutocomplete();
                this.executeSearch(item);
            });
            dropdown.appendChild(div);
        });

        dropdown.classList.remove('hidden');
    },

    hideAutocomplete() {
        const dropdown = document.getElementById('autocomplete-dropdown');
        if (dropdown) dropdown.classList.add('hidden');
    },

    async executeSearch(medicineName) {
        if (!medicineName) {
            API.showToast('Please type a medicine name to search', 'warning');
            return;
        }

        const resultsContainer = document.getElementById('search-results-list');
        const resultsTitle = document.getElementById('results-header-title');
        const resultsSection = document.getElementById('search-results-section');
        const loader = document.getElementById('search-loader');

        if (resultsSection) resultsSection.classList.remove('hidden');
        if (resultsTitle) resultsTitle.innerHTML = `Showing pharmacies with <strong>"${medicineName}"</strong> within ${this.currentRadius} km`;
        if (loader) loader.classList.remove('hidden');
        if (resultsContainer) resultsContainer.innerHTML = '';

        // Scroll to results
        resultsSection.scrollIntoView({ behavior: 'smooth', block: 'start' });

        try {
            const params = {
                medicine: medicineName,
                latitude: this.currentLocation.latitude,
                longitude: this.currentLocation.longitude,
                radius: this.currentRadius,
                page: 0,
                size: 20
            };

            const res = await API.get(CONFIG.ENDPOINTS.MEDICINE_NEARBY, params);

            if (res.success && res.data) {
                const stores = res.data.content || [];
                this.renderStoreCards(stores, medicineName);
            } else {
                this.renderEmptyResults(medicineName);
            }
        } catch (err) {
            this.renderErrorState(err.message);
        } finally {
            if (loader) loader.classList.add('hidden');
        }
    },

    renderStoreCards(stores, searchedMedicine) {
        const container = document.getElementById('search-results-list');
        if (!container) return;

        container.innerHTML = '';

        if (stores.length === 0) {
            this.renderEmptyResults(searchedMedicine);
            return;
        }

        stores.forEach(store => {
            const card = document.createElement('div');
            card.className = 'store-card';
            card.id = `store-card-${store.storeId}`;

            let badgeClass = 'badge-success';
            let badgeText = `${store.quantity} in stock`;
            if (store.stockStatus === 'CRITICAL') {
                badgeClass = 'badge-danger';
                badgeText = `Only ${store.quantity} left!`;
            } else if (store.stockStatus === 'LOW') {
                badgeClass = 'badge-warning';
                badgeText = `Low stock: ${store.quantity}`;
            }

            card.innerHTML = `
                <div class="store-card-header">
                    <div>
                        <h3 class="store-name">${store.storeName}</h3>
                        <p class="store-address"><i class="fas fa-map-marker-alt"></i> ${store.address}</p>
                    </div>
                    <span class="distance-pill"><i class="fas fa-route"></i> ${store.distanceKm} km</span>
                </div>
                
                <div class="store-card-body">
                    <div class="stock-info">
                        <span class="stock-badge ${badgeClass}" id="stock-badge-${store.storeId}">${badgeText}</span>
                        <span class="medicine-tag"><i class="fas fa-capsules"></i> ${store.medicineName}</span>
                    </div>
                    <p class="store-phone"><i class="fas fa-phone"></i> <a href="tel:${store.phone}">${store.phone}</a></p>
                </div>

                <div class="store-card-footer">
                    <button class="btn btn-outline btn-sm" onclick="MapModule.showDirections(${this.currentLocation.latitude}, ${this.currentLocation.longitude}, ${store.latitude}, ${store.longitude}, '${encodeURIComponent(store.storeName)}')">
                        <i class="fas fa-directions"></i> Get Directions
                    </button>
                    <a href="https://www.google.com/maps/dir/?api=1&origin=${this.currentLocation.latitude},${this.currentLocation.longitude}&destination=${store.latitude},${store.longitude}" 
                       target="_blank" rel="noopener noreferrer" class="btn btn-secondary btn-sm">
                        <i class="fas fa-external-link-alt"></i> Google Maps
                    </a>
                </div>
            `;

            container.appendChild(card);

            // Auto-subscribe this store to real-time WebSocket inventory updates!
            if (window.WS && window.WS.subscribeToStore) {
                window.WS.subscribeToStore(store.storeId);
            }
        });
    },

    renderEmptyResults(medicineName) {
        const container = document.getElementById('search-results-list');
        if (!container) return;
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-store-slash empty-icon"></i>
                <h3>No pharmacies found with "${medicineName}" nearby</h3>
                <p>Try increasing your search radius or search for a generic alternative (e.g. Paracetamol instead of brand name).</p>
            </div>
        `;
    },

    renderErrorState(errMsg) {
        const container = document.getElementById('search-results-list');
        if (!container) return;
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-exclamation-triangle empty-icon text-danger"></i>
                <h3>Search Error</h3>
                <p>${errMsg}</p>
            </div>
        `;
    }
};

window.Search = Search;
document.addEventListener('DOMContentLoaded', () => Search.init());
