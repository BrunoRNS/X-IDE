export const problemModal = document.getElementById('problemModal');
export const problemForm = document.getElementById('problemForm');
export const problemPdfInput = document.getElementById('problemPdf');
export const problemPdfStatus = document.getElementById('problemPdfStatus');
export const problemPdfPreview = document.getElementById('problemPdfPreview');
export const problemPdfViewer = document.getElementById('problemPdfUploadViewer');

export const pdfViewerUrl = '/public/vendor/pdfjs/web/viewer.html';

export const problemState = {
    pdfUrl: null,
    editingId: null
};

export const languages = [
    'Python',
    'Java',
    'Kotlin',
    'Groovy',
    'C',
    'C++',
    'Go',
    'Rust',
    'JavaScript'
];

export const languageLabels = {
    python: 'Python',
    java: 'Java',
    kotlin: 'Kotlin',
    groovy: 'Groovy',
    c: 'C',
    cpp: 'C++',
    'c++': 'C++',
    go: 'Go',
    rust: 'Rust',
    javascript: 'JavaScript'
};

export const complexities = [
    { value: 'n1', label: 'O(1)' },
    { value: 'logn', label: 'O(log N)' },
    { value: 'n', label: 'O(N)' },
    { value: 'nlogn', label: 'O(N log N)' },
    { value: 'ne2', label: 'O(N²)' },
    { value: 'ne3', label: 'O(N³)' }
];

export const balloonColors = [
    '#dc2626', '#2563eb', '#16a34a', '#ca8a04', '#ea580c', '#9333ea', '#db2777', '#0891b2',
    '#65a30d', '#c026d3', '#4f46e5', '#0d9488', '#e11d48', '#0284c7', '#059669', '#d97706',
    '#7c3aed', '#78716c', '#f43f5e', '#1d4ed8'
];
