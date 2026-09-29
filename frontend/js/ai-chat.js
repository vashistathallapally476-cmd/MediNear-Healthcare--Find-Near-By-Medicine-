/**
 * MediNear Claude AI Assistant Module
 * Handles conversational symptom assistant via Spring Boot /api/ai/chat
 * Renders interactive recommendation chips that link directly to nearby pharmacy search.
 */

const AiChat = {
    isOpen: false,

    init() {
        this.bindEvents();
    },

    bindEvents() {
        const toggleBtn = document.getElementById('btn-toggle-ai-chat');
        const closeBtn = document.getElementById('btn-close-ai-chat');
        const sendBtn = document.getElementById('btn-ai-send');
        const input = document.getElementById('ai-chat-input');

        if (toggleBtn) {
            toggleBtn.addEventListener('click', () => this.toggleChat());
        }
        if (closeBtn) {
            closeBtn.addEventListener('click', () => this.toggleChat(false));
        }

        if (sendBtn && input) {
            sendBtn.addEventListener('click', () => this.sendMessage());
            input.addEventListener('keydown', (e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    this.sendMessage();
                }
            });
        }
    },

    toggleChat(forceState) {
        const drawer = document.getElementById('ai-chat-drawer');
        const toggleBtn = document.getElementById('btn-toggle-ai-chat');

        this.isOpen = forceState !== undefined ? forceState : !this.isOpen;

        if (drawer) {
            if (this.isOpen) {
                drawer.classList.add('active');
                if (toggleBtn) toggleBtn.classList.add('hidden');
                const input = document.getElementById('ai-chat-input');
                if (input) input.focus();
            } else {
                drawer.classList.remove('active');
                if (toggleBtn) toggleBtn.classList.remove('hidden');
            }
        }
    },

    async sendMessage() {
        const input = document.getElementById('ai-chat-input');
        const message = input ? input.value.trim() : '';

        if (!message) return;

        // Render user message
        this.appendMessage('user', message);
        if (input) input.value = '';

        // Render typing indicator
        const typingId = this.showTypingIndicator();

        try {
            const payload = {
                message,
                latitude: Search.currentLocation.latitude,
                longitude: Search.currentLocation.longitude
            };

            const res = await API.post(CONFIG.ENDPOINTS.AI_CHAT, payload);

            this.removeTypingIndicator(typingId);

            if (res.success && res.data) {
                const data = res.data;
                this.appendMessage('bot', data.reply, data.suggestedMedicines, data.nearbyStores);
            } else {
                this.appendMessage('bot', 'I could not process that request. Please try asking about your symptoms or medicines.');
            }
        } catch (err) {
            this.removeTypingIndicator(typingId);
            this.appendMessage('bot', 'I am currently unable to connect to the medical knowledge base. Please check back shortly or search directly above.');
        }
    },

    appendMessage(sender, text, suggestions = [], stores = []) {
        const messagesContainer = document.getElementById('ai-chat-messages');
        if (!messagesContainer) return;

        const msgDiv = document.createElement('div');
        msgDiv.className = `ai-message ai-message-${sender}`;

        let contentHtml = `<div class="message-bubble"><p>${text}</p></div>`;

        // Render suggested medicine chips
        if (suggestions && suggestions.length > 0) {
            contentHtml += `
                <div class="ai-suggestions-box">
                    <p class="suggestions-label"><i class="fas fa-lightbulb"></i> Suggested Medicines:</p>
                    <div class="suggestion-chips">
                        ${suggestions.map(med => `
                            <button class="btn-suggestion-chip" onclick="AiChat.handleSuggestionClick('${med}')">
                                <i class="fas fa-pills"></i> ${med}
                            </button>
                        `).join('')}
                    </div>
                </div>
            `;
        }

        msgDiv.innerHTML = contentHtml;
        messagesContainer.appendChild(msgDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    },

    handleSuggestionClick(medicineName) {
        API.showToast(`Checking pharmacy stock for ${medicineName}...`, 'info');
        
        // Put in search box and execute search
        const searchInput = document.getElementById('search-medicine-input');
        if (searchInput) searchInput.value = medicineName;

        if (window.Search && window.Search.executeSearch) {
            window.Search.executeSearch(medicineName);
        }

        // Minimize chat drawer to reveal search results
        this.toggleChat(false);
    },

    showTypingIndicator() {
        const messagesContainer = document.getElementById('ai-chat-messages');
        if (!messagesContainer) return null;

        const id = 'typing-' + Date.now();
        const typingDiv = document.createElement('div');
        typingDiv.className = 'ai-message ai-message-bot typing-indicator-box';
        typingDiv.id = id;
        typingDiv.innerHTML = `
            <div class="message-bubble">
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
            </div>
        `;
        messagesContainer.appendChild(typingDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
        return id;
    },

    removeTypingIndicator(id) {
        if (!id) return;
        const el = document.getElementById(id);
        if (el) el.remove();
    }
};

window.AiChat = AiChat;
document.addEventListener('DOMContentLoaded', () => AiChat.init());
