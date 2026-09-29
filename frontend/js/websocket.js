/**
 * MediNear WebSocket Real-Time Stock Updates Module
 * Uses SockJS + STOMP to connect to Spring Boot at /ws
 * Subscribes to /topic/inventory/{storeId} for instant stock changes
 */

const WS = {
    stompClient: null,
    connected: false,
    subscribedStores: new Set(),

    init() {
        this.connect();
    },

    connect() {
        if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
            console.warn('SockJS or STOMP library not loaded from CDN. Real-time updates disabled.');
            return;
        }

        try {
            const socket = new SockJS(`${CONFIG.API_BASE_URL}${CONFIG.ENDPOINTS.WS_ENDPOINT}`);
            this.stompClient = Stomp.over(socket);
            this.stompClient.debug = null; // Disable noisy console logs

            this.stompClient.connect({}, (frame) => {
                this.connected = true;
                console.log('✅ Connected to MediNear WebSocket broker');

                // Re-subscribe any stores that were active before reconnect
                this.subscribedStores.forEach(storeId => {
                    this.doSubscribe(storeId);
                });
            }, (error) => {
                this.connected = false;
                console.warn('WebSocket connection lost. Reconnecting in 6 seconds...', error);
                setTimeout(() => this.connect(), 6000);
            });
        } catch (e) {
            console.warn('WebSocket initialization error:', e);
        }
    },

    subscribeToStore(storeId) {
        if (!storeId || this.subscribedStores.has(storeId)) return;
        this.subscribedStores.add(storeId);

        if (this.connected && this.stompClient) {
            this.doSubscribe(storeId);
        }
    },

    doSubscribe(storeId) {
        const topic = `/topic/inventory/${storeId}`;
        this.stompClient.subscribe(topic, (message) => {
            try {
                const event = JSON.parse(message.body);
                this.handleInventoryUpdate(event);
            } catch (e) {
                console.error('Error parsing STOMP message:', e);
            }
        });
    },

    handleInventoryUpdate(event) {
        console.log('⚡ Real-time inventory event received:', event);

        // Find store card on screen
        const card = document.getElementById(`store-card-${event.storeId}`);
        const badge = document.getElementById(`stock-badge-${event.storeId}`);

        if (badge) {
            // Update stock badge
            let badgeClass = 'badge-success';
            let badgeText = `${event.newQuantity} in stock`;

            if (event.stockStatus === 'OUT_OF_STOCK' || event.newQuantity === 0) {
                badgeClass = 'badge-danger';
                badgeText = 'OUT OF STOCK';
            } else if (event.stockStatus === 'CRITICAL') {
                badgeClass = 'badge-danger';
                badgeText = `Only ${event.newQuantity} left!`;
            } else if (event.stockStatus === 'LOW') {
                badgeClass = 'badge-warning';
                badgeText = `Low stock: ${event.newQuantity}`;
            }

            badge.className = `stock-badge ${badgeClass} pulse-update`;
            badge.textContent = badgeText;

            // Flash toast notification for user
            API.showToast(
                `⚡ Real-time update: ${event.storeName} updated stock for ${event.medicineName} (${event.newQuantity} available)`,
                'info'
            );

            // Remove pulse animation after 2.5s
            setTimeout(() => badge.classList.remove('pulse-update'), 2500);
        }

        // If currently in owner inventory view, refresh table
        if (window.StoreOwner && window.StoreOwner.activeStoreId === event.storeId) {
            window.StoreOwner.loadStoreInventory(event.storeId);
        }
    }
};

window.WS = WS;
document.addEventListener('DOMContentLoaded', () => WS.init());
