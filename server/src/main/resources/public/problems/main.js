import { openDropdown, initializeBalloonPicker } from './balloons.js?v=problem-edit-2';
import { deleteExample, createNewExample } from './examples.js';
import { addLangLimit, addLangSolution, createBigO } from './fields.js';
import { openProblemEditor, openProblemModal, resetProblemForm } from './editor.js';
import {
    clearProblemPdfPreview,
    closeProblemModal,
    initializePdfPreview
} from './pdf-preview.js';
import { problemForm, problemModal, problemState } from './state.js';
import { openModal, closeStudentModal, addItem } from './modal.js?v=problem-edit-3';


function exposeInlineHandlers() {
    window.openModel = openProblemModal;
    window.openDropdown = openDropdown;
    window.addLangSoluction = addLangSolution;
    window.addLangLimit = addLangLimit;
    window.createNewExample = createNewExample;
    window.deleteExample = deleteExample;
    window.openProblemEditor = openProblemEditor;
    window.addItem = addItem;
}

function initializeProblemForm() {
    initializeBalloonPicker();
    initializePdfPreview();

    addLangLimit(true);
    addLangSolution(true);
    createBigO();

    document.getElementById('closeProblemModal')
        .addEventListener('click', closeProblemModal);
    document.getElementById('cancelProblemModal')
        .addEventListener('click', closeProblemModal);

    problemModal.addEventListener('click', (event) => {
        if (event.target === problemModal) closeProblemModal();
    });

    document.addEventListener('keydown', (event) => {
        const isOpen = !problemModal.classList.contains('hidden');
        if (event.key === 'Escape' && isOpen) closeProblemModal();
    });

    problemForm.addEventListener('submit', submitProblemForm);
}

async function submitProblemForm(event) {
    event.preventDefault();

    const submitButton = problemForm.querySelector('button[type="submit"]');
    submitButton.disabled = true;

    try {
        const response = await fetch(problemUrl(), {
            method: problemState.editingId ? 'PUT' : 'POST',
            headers: { 'Accept': 'application/json' },
            body: new FormData(problemForm)
        });
        const payload = await response.json().catch(() => ({}));

        if (!response.ok) {
            throw new Error(payload.error || 'Could not save the problem.');
        }

        closeProblemModal();
        resetProblemForm();
        window.location.reload();
    } catch (error) {
        console.error('Problem save error:', error);
        alert(error.message);
    } finally {
        submitButton.disabled = false;
    }
}

function problemUrl() {
    return problemState.editingId
        ? `/problems/${problemState.editingId}`
        : '/problens/new';
}

function initializeDateValidation() {
    const startDate = document.getElementById('startDate');
    const endDate = document.getElementById('endDate');

    startDate.addEventListener('change', () => {
        endDate.min = startDate.value;

        if (endDate.value && endDate.value < startDate.value) {
            endDate.value = '';
        }
    });

    endDate.addEventListener('change', () => {
        const isBeforeStart = startDate.value && endDate.value < startDate.value;
        endDate.setCustomValidity(
            isBeforeStart ? 'End date must be after start date.' : ''
        );
    });
}

window.openCompetitionModal = function () {
    openModal({
        title: 'Start new competition',
        description: 'Start competition in server X-IDE.',
    });
};

exposeInlineHandlers();
initializeProblemForm();
initializeDateValidation();
