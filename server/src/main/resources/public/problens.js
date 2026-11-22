const problemModal = document.getElementById('problemModal');
const problemForm = document.getElementById('problemForm');
const problemPdfInput = document.getElementById('problemPdf');
const inputExample = document.getElementById("input");
const outputExample = document.getElementById("output");
const problemPdfStatus = document.getElementById('problemPdfStatus');
const problemPdfPreview = document.getElementById('problemPdfPreview');
const problemPdfViewer = document.getElementById('problemPdfViewer');
const pdfViewerUrl = '/public/vendor/pdfjs/web/viewer.html';
const startDate = document.getElementById("startDate");
const endDate = document.getElementById("endDate");
let problemPdfUrl = null;

const languages = ['Python', 'Java', 'Kotlin', 'Groovy', 'C', 'C++', 'Go', 'Rust', 'JavaScript'];

const bigO = [
    { value: 'n1', label: 'O(1)' },
    { value: 'logn', label: 'O(log N)' },
    { value: 'n', label: 'O(N)' },
    { value: 'nlogn', label: 'O(N log N)' },
    { value: 'ne2', label: 'O(N²)' },
    { value: 'ne3', label: 'O(N³)' }
];

const balloonColors = [
    '#dc2626', '#2563eb', '#16a34a', '#ca8a04', '#ea580c', '#9333ea', '#db2777', '#0891b2',
    '#65a30d', '#c026d3', '#4f46e5', '#0d9488', '#e11d48', '#0284c7', '#059669', '#d97706',
    '#7c3aed', '#78716c', '#f43f5e', '#1d4ed8'
];
function createBallons() {
    const sectionBallon = document.getElementById("content-dropdown");

    balloonColors.forEach((color, i) => {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.dataset.color = color;
        btn.className = 'flex items-center justify-center rounded-lg transition hover:bg-zinc-100 dark:hover:bg-zinc-800';
        btn.appendChild(createBalloon(true, color));
        btn.addEventListener('click', () => selectBalloon(btn, color));
        sectionBallon.appendChild(btn);
    });
}

function openModalStartCompetition(){
    console.log("Not implmeted Open modal")
}

function addLangSoluction(primary) {
    const container = document.getElementById("soluction-group");

    const row = document.createElement('div');
    row.className = 'lang-soluctions flex items-center gap-3  border-b border-zinc-200 p-3 dark:border-zinc-800';

    row.innerHTML = `
    <select name="langSoluctions[language]" required
            class="w-32 rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-red-500 dark:border-zinc-800 dark:bg-zinc-900 dark:text-zinc-100">
            ${languages.map(l => `<option value="${l}">${l}</option>`).join('')}
        </select>
        <textarea name="soluctionsLang[]" rows="3" placeholder="Example code..."
                                    class="w-full resize-y rounded-lg border border-zinc-200 bg-white px-3.5 py-2.5 text-sm text-zinc-900 outline-none focus:border-red-500 dark:border-zinc-800 dark:bg-zinc-900 dark:text-zinc-100"></textarea>
        <button type="button" onclick="this.closest('.lang-soluctions').remove()"
            class="ml-auto rounded-lg p-2 text-red-500 transition hover:bg-red-50 dark:hover:bg-red-950/40"
            aria-label="Remove">
            ${primary ? "" : '<i class="fa-solid fa-trash"></i>'}   
        </button>
    `;

    container.appendChild(row);
}

function createBigO() {
    const bigo = document.getElementById("bigo");
    bigo.innerHTML = `
        <select id="bigO" name="bigO" required
            class="w-full rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-red-500 dark:border-zinc-800 dark:bg-zinc-900 dark:text-zinc-100">
            ${bigO.map(o => `<option value="${o.value}">${o.label}</option>`).join('')}
        </select>`;
}

function addLangLimit(primary) {
    const container = document.getElementById('langLimitsContainer');

    const row = document.createElement('div');
    row.className = 'lang-limit flex items-center gap-3 rounded-xl border border-zinc-200 p-3 dark:border-zinc-800';

    row.innerHTML = `
        <select name="langLimits[language]" required
            class="w-32 rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-red-500 dark:border-zinc-800 dark:bg-zinc-900 dark:text-zinc-100">
            ${languages.map(l => `<option value="${l}">${l}</option>`).join('')}
        </select>

        <div class="flex items-center gap-1">
            <input type="number" name="langLimits[time]" min="1" max="60" required placeholder="1"
                class="w-16 rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-red-500 dark:border-zinc-800 dark:bg-zinc-900 dark:text-zinc-100">
            <span class="text-xs text-zinc-500 dark:text-zinc-400">s</span>
        </div>


        <button type="button" onclick="this.closest('.lang-limit').remove()"
            class="ml-auto rounded-lg p-2 text-red-500 transition hover:bg-red-50 dark:hover:bg-red-950/40"
            aria-label="Remove">
            ${primary ? "" : '<i class="fa-solid fa-trash"></i>'}   
        </button>
    `;

    container.appendChild(row);
}



function selectBalloon(btn, color) {
    const selected = document.getElementById('dropdown-selected');
    selected.innerHTML = '';
    selected.appendChild(createBalloon(true, color));


    let hidden = document.getElementById('balloonColor');
    if (!hidden) {
        hidden = document.createElement('input');
        hidden.type = 'hidden';
        hidden.id = 'balloonColor';
        hidden.name = 'color';
        btn.closest('form').appendChild(hidden);
    }
    hidden.value = color;


    document.getElementById('content-dropdown').classList.add('hidden');
    document.getElementById('button-dropdown').textContent = '';
    document.getElementById('button-dropdown').innerHTML = `
        <span id="dropdown-selected" class="flex items-center gap-2"></span>
        <i class="fa-solid fa-chevron-down text-xs text-zinc-400"></i>
    `;
    const newSelected = document.getElementById('dropdown-selected');
    newSelected.appendChild(createBalloon(true, color));
}

function openDropdown(button) {
    const content = document.getElementById("content-dropdown");
    const chevron = document.getElementById('dropdown-chevron');

    if (content.classList.contains('hidden')) {
        content.classList.remove('hidden');
        chevron?.classList.add('rotate-180');
    } else {
        content.classList.add('hidden');
        chevron?.classList.remove('rotate-180');
    }
}

document.addEventListener('click', (e) => {
    const dropdown = document.getElementById('dropdown');
    const content = document.getElementById('content-dropdown');
    if (!dropdown.contains(e.target) && !content.classList.contains('hidden')) {
        content.classList.add('hidden');
    }
});
function createBalloon(filled, color) {
    const icon = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    icon.setAttribute('viewBox', '0 0 24 32');
    icon.setAttribute('width', '20');
    icon.setAttribute('height', '26');
    icon.setAttribute('aria-hidden', 'true');
    icon.classList.add('inline-block');
    icon.style.color = color || '#dc2626';
    const body = document.createElementNS('http://www.w3.org/2000/svg', 'path');
    body.setAttribute('d', 'M12 1C6.2 1 2 5.5 2 11.5c0 5.3 3.8 10.8 8.2 12.1L12 27l1.8-3.4C18.2 22.3 22 16.8 22 11.5 22 5.5 17.8 1 12 1Z');
    body.setAttribute('fill', filled ? 'currentColor' : 'none');
    body.setAttribute('stroke', 'currentColor');
    body.setAttribute('stroke-width', '1.5');
    const string = document.createElementNS('http://www.w3.org/2000/svg', 'path');
    string.setAttribute('d', 'M12 27v2c0 1.2-1 2-1 3');
    string.setAttribute('fill', 'none');
    string.setAttribute('stroke', 'currentColor');
    string.setAttribute('stroke-width', '1.5');
    string.setAttribute('stroke-linecap', 'round');
    icon.append(body, string);
    return icon;
}
function openModel() {
    problemModal.classList.remove('hidden');
    problemModal.classList.add('flex');
    document.getElementById('problemTitle').focus();
}

function clearProblemPdfPreview() {
    problemPdfPreview.classList.add('hidden');
    problemPdfViewer.src = pdfViewerUrl;
    problemPdfInput.value = '';
    problemPdfStatus.textContent = 'Choose a PDF to preview it.';
    problemPdfStatus.classList.remove('text-red-600', 'dark:text-red-400');

    if (problemPdfUrl) {
        const previousUrl = problemPdfUrl;
        problemPdfUrl = null;
        window.setTimeout(() => URL.revokeObjectURL(previousUrl), 1000);
    }
}

function closeProblemModal() {
    problemModal.classList.add('hidden');
    problemModal.classList.remove('flex');
    clearProblemPdfPreview();
}

problemPdfInput.addEventListener('change', () => {
    const file = problemPdfInput.files?.[0];
    if (!file) return;

    if (file.type !== 'application/pdf' && !file.name.toLowerCase().endsWith('.pdf')) {
        problemPdfInput.value = '';
        problemPdfStatus.textContent = 'Choose a PDF file.';
        problemPdfStatus.classList.add('text-red-600', 'dark:text-red-400');
        return;
    }

    if (problemPdfUrl) URL.revokeObjectURL(problemPdfUrl);
    problemPdfUrl = URL.createObjectURL(file);
    problemPdfViewer.src = `${pdfViewerUrl}?file=${encodeURIComponent(problemPdfUrl)}`;
    problemPdfPreview.classList.remove('hidden');
    problemPdfStatus.textContent = 'PDF opened in the viewer.';
    problemPdfStatus.classList.remove('text-red-600', 'dark:text-red-400');
});

function createNewExample() {
    const container = document.getElementById('examplesContainer');

    const group = document.createElement('div');

    group.className = 'example-group rounded-xl border border-zinc-200 p-4 dark:border-zinc-800';

    group.innerHTML = `
        <div class="mb-3 flex items-center justify-between">
            <span class="example-title text-sm font-semibold text-zinc-700 dark:text-zinc-300"></span>
            <button type="button" data-delete-example
                onclick="deleteExample(this)"
                class="rounded-lg p-2 text-red-500 transition hover:bg-red-50 dark:hover:bg-red-950/40"
                aria-label="Delete example">
                <i class="fa-solid fa-trash"></i>
            </button>
        </div>

        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
                <label class="mb-2 block text-sm font-medium text-zinc-700 dark:text-zinc-300">
                    Input description
                </label>

                <textarea name="inputDescription[]" rows="3"
                    placeholder="Describe the input format..."
                    class="w-full resize-y rounded-lg border border-zinc-200 bg-white px-3.5 py-2.5 text-sm text-zinc-900 outline-none focus:border-red-500 dark:border-zinc-800 dark:bg-zinc-900 dark:text-zinc-100"></textarea>
            </div>

            <div>
                <label class="mb-2 block text-sm font-medium text-zinc-700 dark:text-zinc-300">
                    Output description
                </label>

                <textarea name="outputDescription[]" rows="3"
                    placeholder="Describe the expected output..."
                    class="w-full resize-y rounded-lg border border-zinc-200 bg-white px-3.5 py-2.5 text-sm text-zinc-900 outline-none focus:border-red-500 dark:border-zinc-800 dark:bg-zinc-900 dark:text-zinc-100"></textarea>
            </div>
        </div>
    `;

    container.appendChild(group);
    updateExampleNumbers();
}

function deleteExample(button) {
    const container = document.getElementById('examplesContainer');
    const groups = container.querySelectorAll('.example-group');

    if (groups.length === 1) {
        groups[0].querySelectorAll('textarea').forEach(input => {
            input.value = '';
        });

        return;
    }

    button.closest('.example-group').remove();

    updateExampleNumbers();
}

function updateExampleNumbers() {
    document.querySelectorAll('#examplesContainer .example-group')
        .forEach((group, index) => {
            group.querySelector('.example-title').textContent =
                `Example ${index + 1}`;
        });
}

function updateExampleNumbers() {
    document.querySelectorAll('#examplesContainer .example-group').forEach((group, index) => {
        group.querySelector('.example-title').textContent = `Example ${index + 1}`;
    });
}

document.getElementById('closeProblemModal').addEventListener('click', closeProblemModal);
document.getElementById('cancelProblemModal').addEventListener('click', closeProblemModal);

problemModal.addEventListener('click', (event) => {
    if (event.target === problemModal) closeProblemModal();
});

document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && !problemModal.classList.contains('hidden')) closeProblemModal();
});
createBallons();
addLangLimit(true);
addLangSoluction(true);
createBigO();

problemForm.addEventListener('submit', async (event) => {
    event.preventDefault();

    const submitButton = problemForm.querySelector('button[type="submit"]');
    if (submitButton) submitButton.disabled = true;

    try {
        const response = await fetch('/problens/new', {
            method: 'POST',
            headers: { 'Accept': 'application/json' },
            body: new FormData(problemForm)
        });
        const payload = await response.json().catch(() => ({}));

        if (!response.ok) {
            throw new Error(payload.error || 'Could not create the problem.');
        }

        closeProblemModal();
        problemForm.reset();
        window.location.reload();
    } catch (error) {
        console.error('Problem creation error:', error);
        alert(error.message);
    } finally {
        if (submitButton) submitButton.disabled = false;
    }
});



startDate.addEventListener("change", () => {
    endDate.min = startDate.value;

    if (endDate.value && endDate.value < startDate.value) {
        endDate.value = "";
    }
});

endDate.addEventListener("change", () => {
    if (startDate.value && endDate.value < startDate.value) {
        endDate.setCustomValidity("End date must be after start date.");
    } else {
        endDate.setCustomValidity("");
    }
});