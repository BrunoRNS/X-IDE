export function createNewExample() {
    const container = document.getElementById('examplesContainer');
    const row = document.createElement('div');
    row.className = 'example-group rounded-xl border border-zinc-200 p-4 dark:border-zinc-800';

    row.innerHTML = `
        <div class="mb-3 flex items-center justify-between">
            <span class="example-title text-sm font-semibold text-zinc-700 dark:text-zinc-300"></span>
            <button type="button" data-delete-example onclick="deleteExample(this)"
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

    container.append(row);
    updateExampleNumbers();
}

export function deleteExample(button) {
    const container = document.getElementById('examplesContainer');
    const rows = container.querySelectorAll('.example-group');

    if (rows.length === 1) {
        rows[0].querySelectorAll('textarea').forEach((field) => {
            field.value = '';
        });
        return;
    }

    button.closest('.example-group').remove();
    updateExampleNumbers();
}

export function updateExampleNumbers() {
    document.querySelectorAll('#examplesContainer .example-group')
        .forEach((group, index) => {
            group.querySelector('.example-title').textContent = `Example ${index + 1}`;
        });
}
