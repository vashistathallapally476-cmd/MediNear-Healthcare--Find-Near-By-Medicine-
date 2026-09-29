/**
 * MediNear Authentication Module
 * Handles login, registration, role-based navigation, and auth modals
 */

const Auth = {
    init() {
        this.bindEvents();
        this.updateNavUI();
    },

    bindEvents() {
        // Modal toggles
        const loginBtn = document.getElementById('btn-open-login');
        const registerBtn = document.getElementById('btn-open-register');
        const logoutBtn = document.getElementById('btn-logout');
        const authModal = document.getElementById('auth-modal');
        const closeAuthBtn = document.getElementById('btn-close-auth');
        
        const switchToRegister = document.getElementById('switch-to-register');
        const switchToLogin = document.getElementById('switch-to-login');

        if (loginBtn) {
            loginBtn.addEventListener('click', () => this.showModal('login'));
        }
        if (registerBtn) {
            registerBtn.addEventListener('click', () => this.showModal('register'));
        }
        if (logoutBtn) {
            logoutBtn.addEventListener('click', () => this.logout());
        }
        if (closeAuthBtn) {
            closeAuthBtn.addEventListener('click', () => this.hideModal());
        }

        if (switchToRegister) {
            switchToRegister.addEventListener('click', (e) => {
                e.preventDefault();
                this.switchTab('register');
            });
        }
        if (switchToLogin) {
            switchToLogin.addEventListener('click', (e) => {
                e.preventDefault();
                this.switchTab('login');
            });
        }

        // Close on outside click
        window.addEventListener('click', (e) => {
            if (e.target === authModal) this.hideModal();
        });

        // Form submissions
        const loginForm = document.getElementById('login-form');
        const registerForm = document.getElementById('register-form');

        if (loginForm) {
            loginForm.addEventListener('submit', (e) => this.handleLogin(e));
        }
        if (registerForm) {
            registerForm.addEventListener('submit', (e) => this.handleRegister(e));
        }
    },

    showModal(tab = 'login') {
        const modal = document.getElementById('auth-modal');
        if (!modal) return;
        modal.classList.add('active');
        this.switchTab(tab);
    },

    hideModal() {
        const modal = document.getElementById('auth-modal');
        if (modal) modal.classList.remove('active');
    },

    switchTab(tab) {
        const loginBox = document.getElementById('login-tab-content');
        const registerBox = document.getElementById('register-tab-content');
        const modalTitle = document.getElementById('auth-modal-title');

        if (tab === 'login') {
            if (loginBox) loginBox.classList.remove('hidden');
            if (registerBox) registerBox.classList.add('hidden');
            if (modalTitle) modalTitle.textContent = 'Welcome Back to MediNear';
        } else {
            if (loginBox) loginBox.classList.add('hidden');
            if (registerBox) registerBox.classList.remove('hidden');
            if (modalTitle) modalTitle.textContent = 'Create a MediNear Account';
        }
    },

    async handleLogin(e) {
        e.preventDefault();
        const email = document.getElementById('login-email').value.trim();
        const password = document.getElementById('login-password').value;
        const btn = document.getElementById('btn-submit-login');

        if (!email || !password) {
            API.showToast('Please fill in both email and password', 'warning');
            return;
        }

        try {
            btn.disabled = true;
            btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Logging in...';

            const res = await API.post(CONFIG.ENDPOINTS.AUTH_LOGIN, { email, password });

            if (res.success && res.data) {
                API.setToken(res.data.token);
                API.setUser({
                    id: res.data.id,
                    name: res.data.name,
                    email: res.data.email,
                    role: res.data.role
                });

                API.showToast(`Welcome back, ${res.data.name}!`, 'success');
                this.hideModal();
                this.updateNavUI();

                // If owner, auto open owner tab
                if (res.data.role === 'OWNER' && window.StoreOwner) {
                    window.StoreOwner.loadOwnerStores();
                }
            } else {
                API.showToast(res.message || 'Login failed', 'error');
            }
        } catch (err) {
            API.showToast(err.message || 'Invalid credentials', 'error');
        } finally {
            btn.disabled = false;
            btn.innerHTML = 'Sign In';
        }
    },

    async handleRegister(e) {
        e.preventDefault();
        const name = document.getElementById('reg-name').value.trim();
        const email = document.getElementById('reg-email').value.trim();
        const password = document.getElementById('reg-password').value;
        const role = document.getElementById('reg-role').value;
        const btn = document.getElementById('btn-submit-register');

        if (!name || !email || !password) {
            API.showToast('Please fill in all required fields', 'warning');
            return;
        }

        try {
            btn.disabled = true;
            btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating account...';

            const res = await API.post(CONFIG.ENDPOINTS.AUTH_REGISTER, {
                name,
                email,
                password,
                role
            });

            if (res.success && res.data) {
                API.setToken(res.data.token);
                API.setUser({
                    id: res.data.id,
                    name: res.data.name,
                    email: res.data.email,
                    role: res.data.role
                });

                API.showToast(`Account created! Welcome, ${res.data.name}.`, 'success');
                this.hideModal();
                this.updateNavUI();
            } else {
                API.showToast(res.message || 'Registration failed', 'error');
            }
        } catch (err) {
            API.showToast(err.message || 'Registration failed', 'error');
        } finally {
            btn.disabled = false;
            btn.innerHTML = 'Create Account';
        }
    },

    logout() {
        API.clearAuth();
        API.showToast('Logged out successfully', 'info');
        this.updateNavUI();
        
        // Return to customer search view if in owner view
        const ownerSection = document.getElementById('owner-section');
        const customerSection = document.getElementById('customer-section');
        if (ownerSection && customerSection) {
            ownerSection.classList.add('hidden');
            customerSection.classList.remove('hidden');
        }
    },

    updateNavUI() {
        const user = API.getUser();
        const guestNav = document.getElementById('nav-guest');
        const userNav = document.getElementById('nav-user');
        const userNameSpan = document.getElementById('user-display-name');
        const userRoleBadge = document.getElementById('user-role-badge');
        const ownerDashboardNavBtn = document.getElementById('nav-btn-owner');

        if (user) {
            if (guestNav) guestNav.classList.add('hidden');
            if (userNav) userNav.classList.remove('hidden');
            if (userNameSpan) userNameSpan.textContent = user.name;
            if (userRoleBadge) {
                userRoleBadge.textContent = user.role;
                userRoleBadge.className = `role-badge role-${user.role.toLowerCase()}`;
            }

            // Show Owner Dashboard button if role is OWNER
            if (ownerDashboardNavBtn) {
                if (user.role === 'OWNER') {
                    ownerDashboardNavBtn.classList.remove('hidden');
                } else {
                    ownerDashboardNavBtn.classList.add('hidden');
                }
            }
        } else {
            if (guestNav) guestNav.classList.remove('hidden');
            if (userNav) userNav.classList.add('hidden');
            if (ownerDashboardNavBtn) ownerDashboardNavBtn.classList.add('hidden');
        }
    }
};

window.Auth = Auth;
document.addEventListener('DOMContentLoaded', () => Auth.init());
