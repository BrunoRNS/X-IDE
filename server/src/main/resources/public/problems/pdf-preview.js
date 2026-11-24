import {
    pdfViewerUrl,
    problemModal,
    problemPdfInput,
    problemPdfPreview,
    problemPdfStatus,
    problemPdfViewer,
    problemState
} from './state.js';

export function closeProblemModal() {
    problemModal.classList.add('hidden');
    problemModal.classList.remove('flex');
    clearProblemPdfPreview();
}

export function clearProblemPdfPreview() {
    problemPdfPreview.classList.add('hidden');
    problemPdfViewer.src = pdfViewerUrl;
    problemPdfInput.value = '';
    problemPdfStatus.textContent = 'Choose a PDF to preview it.';
    problemPdfStatus.classList.remove('text-red-600', 'dark:text-red-400');

    if (problemState.pdfUrl) {
        const previousUrl = problemState.pdfUrl;
        problemState.pdfUrl = null;
        window.setTimeout(() => URL.revokeObjectURL(previousUrl), 1000);
    }
}

export function initializePdfPreview() {
    problemPdfInput.addEventListener('change', () => {
        const file = problemPdfInput.files?.[0];
        if (!file) return;

        const isPdf = file.type === 'application/pdf'
            || file.name.toLowerCase().endsWith('.pdf');
        if (!isPdf) {
            problemPdfInput.value = '';
            problemPdfStatus.textContent = 'Choose a PDF file.';
            problemPdfStatus.classList.add('text-red-600', 'dark:text-red-400');
            return;
        }

        if (problemState.pdfUrl) URL.revokeObjectURL(problemState.pdfUrl);

        problemState.pdfUrl = URL.createObjectURL(file);
        problemPdfViewer.src = `${pdfViewerUrl}?file=${encodeURIComponent(problemState.pdfUrl)}`;
        problemPdfPreview.classList.remove('hidden');
        problemPdfStatus.textContent = 'PDF opened in the viewer.';
        problemPdfStatus.classList.remove('text-red-600', 'dark:text-red-400');
    });
}
