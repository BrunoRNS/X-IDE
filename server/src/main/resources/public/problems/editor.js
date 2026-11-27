import { selectBalloon } from './balloons.js';
import { createNewExample, updateExampleNumbers } from './examples.js';
import { addLangLimit, addLangSolution } from './fields.js';
import { clearProblemPdfPreview, closeProblemModal } from './pdf-preview.js';
import {
    languageLabels,
    problemForm,
    problemModal,
    problemPdfInput,
    problemPdfStatus,
    problemState
} from './state.js';

export function openProblemModal() {
    resetProblemForm();
    problemModal.classList.remove('hidden');
    problemModal.classList.add('flex');
    document.getElementById('problemTitle').focus();
}

export function resetProblemForm() {
    problemState.editingId = null;
    problemForm.reset();
    clearProblemPdfPreview();

    document.getElementById('problemModalTitle').textContent = 'Create problem';
    document.getElementById('submitProblemLabel').textContent = 'Create problem';
    document.querySelector('#submitProblemButton i').className = 'fa-solid fa-plus';

    problemPdfInput.required = true;
    problemPdfStatus.textContent = 'Choose a PDF to preview it.';

    document.getElementById('soluction-group').replaceChildren();
    document.getElementById('langLimitsContainer').replaceChildren();
    document.getElementById('examplesContainer').replaceChildren();

    addLangSolution(true);
    addLangLimit(true);
    createNewExample();
    selectBalloon(null, '#dc2626');
    updateExampleNumbers();
}

export async function openProblemEditor(id) {
    resetProblemForm();
    problemState.editingId = id;

    const submitButton = document.getElementById('submitProblemButton');
    submitButton.disabled = true;
    setEditorMode();

    try {
        const problem = await fetchProblemDetails(id);
        fillProblemForm(problem);
    } catch (error) {
        closeProblemModal();
        alert(error.message);
    } finally {
        submitButton.disabled = false;
    }
}

function setEditorMode() {
    document.getElementById('problemModalTitle').textContent = 'Edit problem';
    document.getElementById('submitProblemLabel').textContent = 'Save changes';
    document.querySelector('#submitProblemButton i').className = 'fa-solid fa-floppy-disk';

    problemPdfInput.required = false;
    problemPdfStatus.textContent = 'Loading problem details…';
    problemModal.classList.remove('hidden');
    problemModal.classList.add('flex');
}

async function fetchProblemDetails(id) {
    const response = await fetch('/problens/id', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify({ id })
    });

    const problem = await response.json();
    if (!response.ok) {
        throw new Error(problem.error || 'Could not load problem.');
    }

    return problem;
}

function fillProblemForm(problem) {
    problemForm.elements.title.value = problem.title || '';
    problemForm.elements.bigO.value = String(problem.bigo || 'n1').toLowerCase();
    problemForm.elements.recommentation.value = problem.recommentation || '';
    selectBalloon(null, problem.balloon || '#dc2626');

    fillSolutionRows(problem.exampleCodes || []);
    fillExampleRows(problem.examples || []);
    fillLanguageLimitRows(problem.limits || []);

    const pdfUrl = `/problems/${problemState.editingId}/pdf`;
    problemPdfStatus.innerHTML = `Current PDF: <a
        class="font-semibold text-red-600 hover:underline"
        target="_blank"
        rel="noopener"
        href="${pdfUrl}">Open PDF</a>. Upload a replacement if needed.`;
    document.getElementById('problemTitle').focus();
}

function fillSolutionRows(solutions) {
    const container = document.getElementById('soluction-group');
    container.replaceChildren();

    solutions.forEach((solution, index) => {
        addLangSolution(index === 0);

        const row = container.lastElementChild;
        row.querySelector('select').value = languageLabel(solution.lang);
        row.querySelector('textarea').value = solution.code || '';
    });

    if (solutions.length === 0) addLangSolution(true);
}

function fillExampleRows(examples) {
    const container = document.getElementById('examplesContainer');
    container.replaceChildren();

    examples.forEach((example) => {
        createNewExample();

        const row = container.lastElementChild;
        row.querySelector('[name="inputDescription[]"]').value = example.input || '';
        row.querySelector('[name="outputDescription[]"]').value = example.output || '';
    });

    if (examples.length === 0) createNewExample();
    updateExampleNumbers();
}

function fillLanguageLimitRows(limits) {
    const container = document.getElementById('langLimitsContainer');
    container.replaceChildren();

    limits.forEach((limit, index) => {
        addLangLimit(index === 0);

        const row = container.lastElementChild;
        row.querySelector('select').value = languageLabel(limit.lang);
        row.querySelector('[name="langLimits[time]"]').value = String(
            Number(limit.timeMs) / 1000
        );
    });

    if (limits.length === 0) addLangLimit(true);
}

function languageLabel(language) {
    const key = String(language || 'java').toLowerCase();
    return languageLabels[key] || 'Java';
}
