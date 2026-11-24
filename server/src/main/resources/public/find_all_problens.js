// Problem data and card rendering
async function findAll() {
    const response = await fetch("/problens/all", {
        method: "GET",
        headers: { "Accept": "application/json" }
    });

    const payload = await response.json();

    if (!response.ok) {
        throw new Error(payload.error || "Could not fetch problems.");
    }

    return payload;
}

function createBalloonHTML(color) {
    const safeColor = /^#[0-9a-fA-F]{6}$/.test(color)
        ? color
        : "#dc2626";

    return `
        <svg viewBox="0 0 24 32"
             width="20"
             height="26"
             aria-hidden="true"
             style="color: ${safeColor}"
             class="inline-block">
            <path
                d="M12 1C6.2 1 2 5.5 2 11.5c0 5.3 3.8 10.8 8.2 12.1L12 27l1.8-3.4C18.2 22.3 22 16.8 22 11.5 22 5.5 17.8 1 12 1Z"
                fill="currentColor"
                stroke="currentColor"
                stroke-width="1.5"/>
            <path
                d="M12 27v2c0 1.2-1 2-1 3"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
                stroke-linecap="round"/>
        </svg>
    `;
}

async function renderProblems() {
    const container = document.getElementById('problemsContainer');
    const empty = document.getElementById('empty');
    const loading = document.getElementById('problemsLoading');
    const count = document.getElementById('problemCount');

    loading.classList.remove('hidden');
    container.innerHTML = '';
    empty.classList.add('hidden');

    try {
        const problems = await findAll();

        if (!Array.isArray(problems)) {
            throw new Error("Invalid problems response.");
        }

        count.textContent = `${problems.length} problems`;

        if (problems.length === 0) {
            empty.classList.remove('hidden');
            return;
        }

        container.innerHTML = problems
            .map((problem, index) => renderProblemCard(problem, index))
            .join('');

        window.problemsData = problems;
    } catch (error) {
        console.error(error);
        container.innerHTML = renderProblemLoadError();
    } finally {
        loading.classList.add('hidden');
    }
}

function renderProblemCard(problem, index) {
    const title = escapeHTML(problem.title);
    const complexity = escapeHTML(problem.bigo);

    return `
        <article class="group relative overflow-hidden rounded-xl border border-zinc-200 bg-white p-5 shadow-sm transition-all duration-200 hover:-translate-y-1 hover:border-red-300 hover:shadow-lg dark:border-zinc-800 dark:bg-zinc-950 dark:hover:border-red-500/40">
            <div class="relative flex items-center justify-between">
                <div class="flex h-11 w-11 items-center justify-center rounded-xl bg-zinc-100 dark:bg-zinc-900">
                    <i class="fa-solid fa-code text-lg text-zinc-600 dark:text-zinc-300"></i>
                </div>

                <div class="inline-flex items-center gap-2 px-3 py-1.5 text-xs font-medium text-zinc-600 dark:text-zinc-300">
                    ${createBalloonHTML(problem.hexa)}
                    ${renderProblemMenu(problem.id)}
                </div>
            </div>

            <div class="relative mt-5">
                <p class="text-xs font-medium uppercase tracking-wider text-zinc-400">
                    Problem ${index + 1}
                </p>
                <h3 class="mt-1 truncate text-lg font-bold text-zinc-900 dark:text-zinc-100" title="${title}">
                    ${title}
                </h3>
            </div>

            <div class="mt-5 flex items-center justify-between border-t border-zinc-100 pt-4 dark:border-zinc-800">
                <div>
                    <p class="text-xs text-zinc-400">Minimum complexity</p>
                    <span class="mt-1 inline-block rounded-md bg-red-50 px-2.5 py-1 text-xs font-bold text-red-600 dark:bg-red-500/10 dark:text-red-400">
                        ${complexity}
                    </span>
                </div>

                <button type="button"
                    onclick="viewProblem(${problem.id})"
                    class="flex h-9 w-9 items-center justify-center rounded-lg text-zinc-400 transition hover:bg-red-50 hover:text-red-600 dark:hover:bg-red-500/10 dark:hover:text-red-400"
                    title="View problem">
                    <i class="fa-solid fa-arrow-right"></i>
                </button>
            </div>
        </article>
    `;
}

function renderProblemMenu(problemId) {
    return `
        <div class="relative" data-menu>
            <button type="button" data-menu-trigger
                class="flex h-9 w-9 items-center justify-center rounded-lg text-zinc-400 transition hover:bg-zinc-100 hover:text-zinc-900 dark:hover:bg-zinc-800 dark:hover:text-zinc-100">
                <i class="fa-solid fa-ellipsis-vertical"></i>
            </button>

            <div data-menu-content
                class="absolute right-0 top-full z-30 mt-2 hidden w-40 rounded-xl border border-zinc-200 bg-white p-1.5 shadow-xl dark:border-zinc-800 dark:bg-zinc-950">
                <button type="button" onclick="editProblem(${problemId})"
                    class="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm text-zinc-700 transition hover:bg-zinc-100 dark:text-zinc-300 dark:hover:bg-zinc-900">
                    <i class="fa-solid fa-pen w-4 text-zinc-400"></i>
                    Edit
                </button>
                <button type="button" onclick="deleteProblem(${problemId})"
                    class="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm text-red-600 transition hover:bg-red-50 dark:hover:bg-red-500/10">
                    <i class="fa-solid fa-trash w-4"></i>
                    Delete
                </button>
            </div>
        </div>
    `;
}

function renderProblemLoadError() {
    return `
        <div class="col-span-full rounded-xl border border-red-200 bg-red-50 p-6 text-center dark:border-red-900/50 dark:bg-red-950/20">
            <i class="fa-solid fa-circle-exclamation text-2xl text-red-500"></i>
            <p class="mt-2 text-sm font-medium text-red-600 dark:text-red-400">
                Could not load problems.
            </p>
            <button onclick="renderProblems()"
                class="mt-3 text-sm font-semibold text-red-600 hover:underline">
                Try again
            </button>
        </div>
    `;
}

// HTML safety and menu events
function escapeHTML(value) {
    return String(value ?? "").replace(/[&<>"']/g, char => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[char]);
}

document.addEventListener("click", (event) => {
    const trigger = event.target.closest("[data-menu-trigger]");

    if (trigger) {
        const menu = trigger.closest("[data-menu]");
        const content = menu.querySelector("[data-menu-content]");
        const shouldOpen = content.classList.contains("hidden");

        document.querySelectorAll("[data-menu-content]").forEach(item => {
            item.classList.add("hidden");
        });

        if (shouldOpen) {
            content.classList.remove("hidden");
        }

        return;
    }

    if (!event.target.closest("[data-menu]")) {
        document.querySelectorAll("[data-menu-content]").forEach(item => {
            item.classList.add("hidden");
        });
    }
});

// Problem actions and startup
function editProblem(id) {
    if (window.openProblemEditor) window.openProblemEditor(id);
}

async function deleteProblemById(id) {
    try {
        const response = await fetch('/problems/delete/' + id, {
            method: 'DELETE',
        });
        if (!response.ok) {
            throw new Error(`error: ${response.status}`);
        }
    } catch (error) {
        console.error('Erro:', error);
    }
}


function deleteProblem(id) {
    if (confirm("Are you sure you wantto delete problem by id: " + id)) {
        deleteProblemById(id);
        alert("Deleted problem sucess");
    }
}
document.addEventListener("DOMContentLoaded", renderProblems);
