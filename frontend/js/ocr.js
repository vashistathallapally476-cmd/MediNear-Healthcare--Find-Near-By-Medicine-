/**
 * MediNear Prescription OCR Scanner Module
 * Handles camera/file upload, calls Spring Boot POST /api/ocr/scan,
 * renders an interactive Prescription Tablet Menu, and allows one-click availability check.
 */

const OcrModule = {
    selectedFile: null,

    init() {
        this.bindEvents();
    },

    bindEvents() {
        const fileInput = document.getElementById('ocr-file-input');
        const uploadZone = document.getElementById('ocr-drop-zone');
        const scanBtn = document.getElementById('btn-start-ocr');
        const removeImgBtn = document.getElementById('btn-remove-preview');

        if (uploadZone && fileInput) {
            uploadZone.addEventListener('click', () => fileInput.click());

            uploadZone.addEventListener('dragover', (e) => {
                e.preventDefault();
                uploadZone.classList.add('drag-active');
            });

            uploadZone.addEventListener('dragleave', () => {
                uploadZone.classList.remove('drag-active');
            });

            uploadZone.addEventListener('drop', (e) => {
                e.preventDefault();
                uploadZone.classList.remove('drag-active');
                if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
                    this.handleFileSelected(e.dataTransfer.files[0]);
                }
            });

            fileInput.addEventListener('change', (e) => {
                if (e.target.files && e.target.files.length > 0) {
                    this.handleFileSelected(e.target.files[0]);
                }
            });
        }

        if (removeImgBtn) {
            removeImgBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                this.clearSelectedFile();
            });
        }

        if (scanBtn) {
            scanBtn.addEventListener('click', () => this.executeScan());
        }
    },

    handleFileSelected(file) {
        if (!file.type.startsWith('image/')) {
            API.showToast('Please select a valid image file (JPG, PNG, WEBP)', 'warning');
            return;
        }

        this.selectedFile = file;

        // Show image preview
        const reader = new FileReader();
        reader.onload = (e) => {
            const previewContainer = document.getElementById('ocr-preview-container');
            const previewImg = document.getElementById('ocr-preview-img');
            const uploadPlaceholder = document.getElementById('ocr-upload-placeholder');
            const scanBtn = document.getElementById('btn-start-ocr');

            if (previewImg) previewImg.src = e.target.result;
            if (previewContainer) previewContainer.classList.remove('hidden');
            if (uploadPlaceholder) uploadPlaceholder.classList.add('hidden');
            if (scanBtn) scanBtn.disabled = false;
        };
        reader.readAsDataURL(file);
    },

    clearSelectedFile() {
        this.selectedFile = null;
        const fileInput = document.getElementById('ocr-file-input');
        const previewContainer = document.getElementById('ocr-preview-container');
        const previewImg = document.getElementById('ocr-preview-img');
        const uploadPlaceholder = document.getElementById('ocr-upload-placeholder');
        const scanBtn = document.getElementById('btn-start-ocr');
        const resultsMenu = document.getElementById('ocr-prescription-menu');

        if (fileInput) fileInput.value = '';
        if (previewImg) previewImg.src = '';
        if (previewContainer) previewContainer.classList.add('hidden');
        if (uploadPlaceholder) uploadPlaceholder.classList.remove('hidden');
        if (scanBtn) scanBtn.disabled = true;
        if (resultsMenu) resultsMenu.classList.add('hidden');
    },

    async executeScan() {
        if (!this.selectedFile) {
            API.showToast('Please upload or snap a prescription image first', 'warning');
            return;
        }

        const scanBtn = document.getElementById('btn-start-ocr');
        const menuContainer = document.getElementById('ocr-prescription-menu');
        const menuList = document.getElementById('ocr-medicine-list');

        try {
            if (scanBtn) {
                scanBtn.disabled = true;
                scanBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Scanning & Extracting...';
            }

            const formData = new FormData();
            formData.append('image', this.selectedFile);

            // POST to Spring Boot backend
            const res = await API.post(CONFIG.ENDPOINTS.OCR_SCAN, formData);

            if (res.success && res.data) {
                const candidates = res.data.candidates || [];

                if (candidates.length === 0 || res.data.status === 'FAILED') {
                    // Fallback to manual typing
                    API.showToast('OCR model is offline or could not detect medicines. Please type directly in the search bar.', 'info');
                    this.fallbackToManualSearch();
                    return;
                }

                // Render the Prescription Tablet Menu
                this.renderPrescriptionMenu(candidates, res.data.rawText);
                API.showToast(`Extracted ${candidates.length} medicine candidate(s)! Tap one to view stock.`, 'success');
            } else {
                API.showToast(res.message || 'OCR processing failed', 'error');
                this.fallbackToManualSearch();
            }
        } catch (err) {
            console.error('OCR Error:', err);
            API.showToast('OCR Service is currently offline. You can still search by typing below!', 'warning');
            this.fallbackToManualSearch();
        } finally {
            if (scanBtn) {
                scanBtn.disabled = false;
                scanBtn.innerHTML = '<i class="fas fa-magic"></i> Scan Prescription';
            }
        }
    },

    renderPrescriptionMenu(candidates, rawText) {
        const menuContainer = document.getElementById('ocr-prescription-menu');
        const menuList = document.getElementById('ocr-medicine-list');

        if (!menuContainer || !menuList) return;

        menuList.innerHTML = '';

        candidates.forEach((cand, index) => {
            const item = document.createElement('div');
            item.className = 'prescription-menu-item';
            item.id = `ocr-cand-${index}`;

            let badgeClass = 'badge-success';
            let badgeText = 'MATCHED';
            let scorePercent = cand.matchScore ? Math.round(cand.matchScore * 100) : 90;

            if (cand.matchStatus === 'LOW_CONFIDENCE') {
                badgeClass = 'badge-warning';
                badgeText = 'LOW CONFIDENCE';
            } else if (cand.matchStatus === 'NOT_FOUND') {
                badgeClass = 'badge-danger';
                badgeText = 'NOT IN CATALOG';
            }

            item.innerHTML = `
                <div class="menu-item-left">
                    <div class="medicine-icon-circle">
                        <i class="fas fa-tablets"></i>
                    </div>
                    <div class="medicine-details">
                        <h4 class="menu-med-name">${cand.medicineName}</h4>
                        <div class="menu-med-sub">
                            ${cand.genericName ? `<span><i class="fas fa-dna"></i> ${cand.genericName}</span>` : ''}
                            ${cand.strength ? `<span><i class="fas fa-weight-hanging"></i> ${cand.strength}</span>` : ''}
                        </div>
                    </div>
                </div>

                <div class="menu-item-right">
                    <span class="match-badge ${badgeClass}">${badgeText} (${scorePercent}%)</span>
                    <button class="btn btn-primary btn-sm btn-check-stock" onclick="OcrModule.selectMedicine('${cand.medicineName}')">
                        <i class="fas fa-search-location"></i> View Stock
                    </button>
                </div>
            `;

            menuList.appendChild(item);
        });

        menuContainer.classList.remove('hidden');
        menuContainer.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    },

    selectMedicine(medicineName) {
        // 1. Put medicine name into the main search box
        const searchInput = document.getElementById('search-medicine-input');
        if (searchInput) searchInput.value = medicineName;

        // 2. Trigger nearby pharmacy search directly
        if (window.Search && window.Search.executeSearch) {
            window.Search.executeSearch(medicineName);
        }
    },

    fallbackToManualSearch() {
        const searchInput = document.getElementById('search-medicine-input');
        if (searchInput) {
            searchInput.focus();
            searchInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
            searchInput.classList.add('pulse-highlight');
            setTimeout(() => searchInput.classList.remove('pulse-highlight'), 2000);
        }
    }
};

window.OcrModule = OcrModule;
document.addEventListener('DOMContentLoaded', () => OcrModule.init());
