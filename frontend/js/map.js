/**
 * MediNear Map & Navigation Module
 * Calls Spring Boot /api/maps/route and displays turn-by-turn navigation modal
 */

const MapModule = {
    init() {
        this.bindEvents();
    },

    bindEvents() {
        const closeBtn = document.getElementById('btn-close-map-modal');
        const modal = document.getElementById('map-modal');

        if (closeBtn) {
            closeBtn.addEventListener('click', () => this.hideModal());
        }

        window.addEventListener('click', (e) => {
            if (e.target === modal) this.hideModal();
        });
    },

    async showDirections(originLat, originLng, destLat, destLng, storeNameEncoded) {
        const storeName = decodeURIComponent(storeNameEncoded || 'Pharmacy');
        const modal = document.getElementById('map-modal');
        const modalTitle = document.getElementById('map-modal-title');
        const stepsContainer = document.getElementById('route-steps-list');
        const distanceSpan = document.getElementById('route-distance');
        const durationSpan = document.getElementById('route-duration');
        const gmapsBtn = document.getElementById('btn-open-gmaps');

        if (modalTitle) modalTitle.textContent = `Route to ${storeName}`;
        if (modal) modal.classList.add('active');
        if (stepsContainer) stepsContainer.innerHTML = '<div class="loader-box"><i class="fas fa-spinner fa-spin"></i> Calculating optimal route...</div>';

        // Set fallback Google Maps direct link
        const directMapsUrl = `https://www.google.com/maps/dir/?api=1&origin=${originLat},${originLng}&destination=${destLat},${destLng}&travelmode=driving`;
        if (gmapsBtn) gmapsBtn.href = directMapsUrl;

        try {
            const res = await API.get(CONFIG.ENDPOINTS.MAPS_ROUTE, {
                originLat,
                originLng,
                destLat,
                destLng,
                mode: 'driving'
            });

            if (res.success && res.data) {
                const data = res.data;
                if (distanceSpan) distanceSpan.textContent = data.distanceText || 'N/A';
                if (durationSpan) durationSpan.textContent = data.durationText || 'N/A';
                if (gmapsBtn && data.mapsUrl) gmapsBtn.href = data.mapsUrl;

                this.renderSteps(data.steps || []);
            } else {
                this.renderFallbackRoute(originLat, originLng, destLat, destLng);
            }
        } catch (err) {
            console.warn('Google Maps route API error:', err);
            this.renderFallbackRoute(originLat, originLng, destLat, destLng);
        }
    },

    renderSteps(steps) {
        const container = document.getElementById('route-steps-list');
        if (!container) return;

        if (steps.length === 0) {
            container.innerHTML = '<p class="text-muted">Turn-by-turn directions unavailable. Click "Open in Google Maps" below.</p>';
            return;
        }

        container.innerHTML = '';
        steps.forEach((step, idx) => {
            const div = document.createElement('div');
            div.className = 'route-step-item';
            div.innerHTML = `
                <div class="step-num">${idx + 1}</div>
                <div class="step-content">
                    <p class="step-instruction">${step.instruction}</p>
                    <span class="step-meta">${step.distance} • ${step.duration}</span>
                </div>
            `;
            container.appendChild(div);
        });
    },

    renderFallbackRoute(originLat, originLng, destLat, destLng) {
        const container = document.getElementById('route-steps-list');
        const distanceSpan = document.getElementById('route-distance');
        const durationSpan = document.getElementById('route-duration');

        if (distanceSpan) distanceSpan.textContent = 'Calculated on Map';
        if (durationSpan) durationSpan.textContent = 'Few minutes';

        if (container) {
            container.innerHTML = `
                <div class="route-fallback-notice">
                    <i class="fas fa-map-marked-alt text-primary"></i>
                    <p>Live route preview is ready. Tap the button below to launch Google Maps with turn-by-turn voice navigation.</p>
                </div>
            `;
        }
    },

    hideModal() {
        const modal = document.getElementById('map-modal');
        if (modal) modal.classList.remove('active');
    }
};

window.MapModule = MapModule;
document.addEventListener('DOMContentLoaded', () => MapModule.init());
