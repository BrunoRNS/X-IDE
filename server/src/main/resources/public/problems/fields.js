import { complexities, languages } from './state.js';

export function addLangSolution(primary) {
    const container = document.getElementById('soluction-group');
    const row = document.createElement('div');
    row.className = 'lang-soluctions flex items-center gap-3 border-b border-zinc-200 p-3 dark:border-zinc-800';

    row.innerHTML = `
        <select name="langSoluctions[language]" required
            class="w-32 rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-red-500 dark:border-zinc-800 dark:bg-zinc-900 dark:text-zinc-100">
            ${languages.map((language) => `<option value="${language}">${language}</option>`).join('')}
        </select>
        <textarea name="soluctionsLang[]" rows="3" placeholder="Example code..."
            class="w-full resize-y rounded-lg border border-zinc-200 bg-white px-3.5 py-2.5 text-sm text-zinc-900 outline-none focus:border-red-500 dark:border-zinc-800 dark:bg-zinc-900 dark:text-zinc-100"></textarea>
        <button type="button" onclick="this.closest('.lang-soluctions').remove()"
            class="ml-auto rounded-lg p-2 text-red-500 transition hover:bg-red-50 dark:hover:bg-red-950/40"
            aria-label="Remove">
            ${primary ? '' : '<i class="fa-solid fa-trash"></i>'}
        </button>
    `;

    container.append(row);
}

export function addLangLimit(primary = false) {
    const container = document.getElementById('langLimitsContainer');
    const row = document.createElement('div');
    row.className = 'lang-limit flex items-center gap-3 rounded-xl border border-zinc-200 p-3 dark:border-zinc-800';

    row.innerHTML = `
        <select name="langLimits[language]" required
            class="w-32 rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-red-500 dark:border-zinc-800 dark:bg-zinc-900 dark:text-zinc-100">
            ${languages.map((language) => `<option value="${language}">${language}</option>`).join('')}
        </select>

        <div class="flex items-center gap-1">
            <input type="number" name="langLimits[time]" min="1" max="60" required placeholder="1"
                class="w-16 rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-red-500 dark:border-zinc-800 dark:bg-zinc-900 dark:text-zinc-100">
            <span class="text-xs text-zinc-500 dark:text-zinc-400">s</span>
        </div>

        <button type="button" onclick="this.closest('.lang-limit').remove()"
            class="ml-auto rounded-lg p-2 text-red-500 transition hover:bg-red-50 dark:hover:bg-red-950/40"
            aria-label="Remove">
            ${primary ? '' : '<i class="fa-solid fa-trash"></i>'}
        </button>
    `;

    container.append(row);
}

export function createBigO() {
    const container = document.getElementById('bigo');
    container.innerHTML = `
        <select id="bigO" name="bigO" required
            class="w-full rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-900 outline-none focus:border-red-500 dark:border-zinc-800 dark:bg-zinc-900 dark:text-zinc-100">
            ${complexities.map(({ value, label }) => `<option value="${value}">${label}</option>`).join('')}
        </select>
    `;
}
