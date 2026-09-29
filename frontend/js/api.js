/**
 * MediNear Central API Client & HTTP Interceptor
 * Handles all REST communication with Spring Boot backend
 */

const API = {
    // ── Token Management ──────────────────────────────────────────────────
    getToken() {
        return localStorage.getItem('mednear_jwt_token');
    },

    setToken(token) {
        if (token) {
            localStorage.setItem('mednear_jwt_token', token);
        } else {
            localStorage.removeItem('mednear_jwt_token');
        }
    },

    getUser() {
        const u = localStorage.getItem('mednear_user');
        return u ? JSON.parse(u) : null;
    },

    setUser(user) {
        if (user) {
            localStorage.setItem('mednear_user', JSON.stringify(user));
        } else {
            localStorage.removeItem('mednear_user');
        }
    },

    clearAuth() {
        localStorage.removeItem('mednear_jwt_token');
        localStorage.removeItem('mednear_user');
    },

    isAuthenticated() {
        return !!this.getToken();
    },

    // ── Generic Request Wrapper ──────────────────────────────────────────
    async request(endpoint, options = {}) {
        const url = CONFIG.API_BASE_URL + endpoint;
        const headers = options.headers || {};

        // Auto-attach JWT Bearer token if present
        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        // Default content type to JSON unless it's FormData (which needs browser boundary)
        if (!(options.body instanceof FormData) && !headers['Content-Type']) {
            headers['Content-Type'] = 'application/json';
        }

        const config = {
            ...options,
            headers
        };

        try {
            const response = await fetch(url, config);

            // Handle HTTP 401 Unauthorized
            if (response.status === 401) {
                // If it wasn't a login attempt itself, clear token and notify
                if (!endpoint.includes('/api/auth/login')) {
                    this.clearAuth();
                    API.showToast('Session expired. Please log in again.', 'warning');
                    if (window.Auth && window.Auth.updateNavUI) {
                        window.Auth.updateNavUI();
                    }
                }
            }

            const data = await response.json().catch(() => ({
                success: response.ok,
                message: response.statusText
            }));

            if (!response.ok) {
                const errMsg = data.message || `Request failed with status ${response.status}`;
                throw new Error(errMsg);
            }

            return data;
        } catch (err) {
            console.error(`[API Error] ${endpoint}:`, err);
            throw err;
        }
    },

    // ── Shorthand HTTP Methods ───────────────────────────────────────────
    get(endpoint, params = {}) {
        const query = new URLSearchParams();
        Object.entries(params).forEach(([key, val]) => {
            if (val !== undefined && val !== null && val !== '') {
                query.append(key, val);
            }
        });
        const qs = query.toString();
        const url = qs ? `${endpoint}?${qs}` : endpoint;
        return this.request(url, { method: 'GET' });
    },

    post(endpoint, body = {}) {
        return this.request(endpoint, {
            method: 'POST',
            body: body instanceof FormData ? body : JSON.stringify(body)
        });
    },

    put(endpoint, body = {}) {
        return this.request(endpoint, {
            method: 'PUT',
            body: body instanceof FormData ? body : JSON.stringify(body)
        });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    },

    // ── Toast Notification Helper ────────────────────────────────────────
    showToast(message, type = 'info') {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        
        let icon = 'info-circle';
        if (type === 'success') icon = 'check-circle';
        if (type === 'error')   icon = 'exclamation-circle';
        if (type === 'warning') icon = 'exclamation-triangle';

        toast.innerHTML = `
            <i class="fas fa-${icon}"></i>
            <span>${message}</span>
            <button class="toast-close" onclick="this.parentElement.remove()">&times;</button>
        `;

        container.appendChild(toast);

        // Auto remove after 4.5 seconds
        setTimeout(() => {
            toast.classList.add('fade-out');
            setTimeout(() => toast.remove(), 400);
        }, 4500);
    }
};
