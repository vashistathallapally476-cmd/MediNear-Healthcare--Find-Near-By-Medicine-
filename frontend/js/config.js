/**
 * MediNear Frontend Configuration
 * Connects to Spring Boot backend at http://localhost:8080
 */
const CONFIG = {
    // Spring Boot Backend Base URL
    API_BASE_URL: 'http://localhost:8080',
    
    // REST API Endpoints
    ENDPOINTS: {
        // Authentication
        AUTH_LOGIN: '/api/auth/login',
        AUTH_REGISTER: '/api/auth/register',
        
        // Medicines & Availability
        MEDICINE_AUTOCOMPLETE: '/api/medicines/autocomplete',
        MEDICINE_NEARBY: '/api/medicines/nearby',
        
        // OCR Prescription Scanner
        OCR_SCAN: '/api/ocr/scan',
        OCR_HISTORY: '/api/ocr/history',
        
        // Stores (Pharmacy Owner)
        STORES: '/api/stores',
        STORES_MY: '/api/stores/my',
        
        // Inventory Management
        INVENTORY: '/api/inventory',
        INVENTORY_BY_STORE: '/api/inventory/store',
        
        // Maps & Route
        MAPS_ROUTE: '/api/maps/route',
        MAPS_STATIC_URL: '/api/maps/static-url',
        
        // AI Assistant
        AI_CHAT: '/api/ai/chat',
        
        // WebSocket STOMP endpoint
        WS_ENDPOINT: '/ws'
    },
    
    // Default fallback coordinates (Hyderabad, India) if user denies geolocation
    DEFAULT_LOCATION: {
        latitude: 17.3850,
        longitude: 78.4867,
        label: 'Hyderabad (Default)'
    },
    
    // Search defaults
    SEARCH: {
        DEFAULT_RADIUS_KM: 5.0,
        DEFAULT_PAGE_SIZE: 10
    }
};
