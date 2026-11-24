(() => {
    // Modal references and labels
    const modal = document.getElementById('problemViewModal');
    if (!modal) return;
    const pdfViewer = document.getElementById('problemPdfViewer');
    const pdfDownload = document.getElementById('problemViewPdfDownload');
    const pdfSection = document.getElementById('problemViewPdfSection');
    const title = document.getElementById('problemViewTitle');
    const subtitle = document.getElementById('problemViewSubtitle');
    const balloon = document.getElementById('problemViewBalloon');
    const complexity = document.getElementById('problemViewComplexity');
    const problemIdBadge = document.getElementById('problemViewId');
    const loading = document.getElementById('problemViewLoading');
    const error = document.getElementById('problemViewError');
    const errorMessage = document.getElementById('problemViewErrorMessage');
    const content = document.getElementById('problemViewContent');
    const recommendationSection = document.getElementById('problemViewRecommendationSection');
    const recommendation = document.getElementById('problemViewRecommendation');
    const examplesContainer = document.getElementById('problemViewExamples');
    const examplesEmpty = document.getElementById('problemViewExamplesEmpty');
    const codeExamplesContainer = document.getElementById('problemViewCodeExamples');
    const codeExamplesEmpty = document.getElementById('problemViewCodeExamplesEmpty');
    const limitsContainer = document.getElementById('problemViewLimits');
    const limitsEmpty = document.getElementById('problemViewLimitsEmpty');
    const closeButton = document.getElementById('closeProblemViewModal');
    const closeFooterButton = document.getElementById('closeProblemViewFooter');
    const retryButton = document.getElementById('retryProblemView');

    const langLabels = {
        python: 'Python',
        kotlin: 'Kotlin',
        java: 'Java',
        cpp: 'C++',
        'c++': 'C++',
        c: 'C',
        rust: 'Rust',
        go: 'Go',
        golang: 'Go',
        groovy: 'Groovy',
        javascript: 'JavaScript',
        js: 'JavaScript'
    };

    const bigOLabels = {
        n1: 'O(1)',
        '1': 'O(1)',
        'o(1)': 'O(1)',
        logn: 'O(log N)',
        'o(logn)': 'O(log N)',
        n: 'O(N)',
        'o(n)': 'O(N)',
        nlogn: 'O(N log N)',
        'o(nlogn)': 'O(N log N)',
        ne2: 'O(N²)',
        n2: 'O(N²)',
        'o(n2)': 'O(N²)',
        'o(n²)': 'O(N²)',
        ne3: 'O(N³)',
        n3: 'O(N³)',
        'o(n3)': 'O(N³)',
        'o(n³)': 'O(N³)'
    };

    // PDF and formatting helpers
    function renderPdf(problem) {
        const pdfUrl = `/problems/${problem.id}/pdf`;

        if (!pdfViewer || !pdfSection) {
            console.error('PDF viewer elements not found');
            return;
        }

        pdfSection.classList.remove('hidden');

        pdfViewer.src = 'about:blank';

        requestAnimationFrame(() => {
            pdfViewer.src =
                `/public/vendor/pdfjs/web/viewer.html?file=${encodeURIComponent(pdfUrl)}`;

            console.log('PDF viewer URL:', pdfViewer.src);
        });

        if (pdfDownload) {
            pdfDownload.href = pdfUrl;
        }
    }
    const state = { currentId: null, token: 0, lastFocused: null };

    function labelForLang(value) {
        if (!value) return 'Unknown';
        const key = String(value).trim().toLowerCase();
        return langLabels[key] || String(value);
    }

    function labelForBigO(value) {
        if (!value) return '—';
        const key = String(value).trim().toLowerCase().replace(/\s+/g, '');
        return bigOLabels[key] || String(value);
    }

    function formatLimit(timeMs) {
        const seconds = Number(timeMs) / 1000;
        if (!Number.isFinite(seconds)) return '—';
        return `${Number.isInteger(seconds) ? seconds : seconds.toFixed(1)}s`;
    }

    function createElement(tag, className, text) {
        const element = document.createElement(tag);
        if (className) element.className = className;
        if (text !== undefined) element.textContent = text;
        return element;
    }

    function createBalloon(color) {
        const safeColor = /^#[0-9a-fA-F]{6}$/.test(String(color)) ? color : '#dc2626';
        const icon = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        icon.setAttribute('viewBox', '0 0 24 32');
        icon.setAttribute('width', '20');
        icon.setAttribute('height', '26');
        icon.setAttribute('aria-hidden', 'true');
        icon.classList.add('inline-block');
        icon.style.color = safeColor;

        const body = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        body.setAttribute('d', 'M12 1C6.2 1 2 5.5 2 11.5c0 5.3 3.8 10.8 8.2 12.1L12 27l1.8-3.4C18.2 22.3 22 16.8 22 11.5 22 5.5 17.8 1 12 1Z');
        body.setAttribute('fill', 'currentColor');
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

    // Problem content rendering
    function showState(name) {
        loading.classList.toggle('hidden', name !== 'loading');
        error.classList.toggle('hidden', name !== 'error');
        content.classList.toggle('hidden', name !== 'content');
    }

    function createValueBlock(label, value) {
        const block = createElement('div', 'min-w-0');
        block.append(
            createElement('p', 'text-xs font-semibold uppercase tracking-wide text-zinc-400', label),
            createElement('pre',
                'mt-2 max-h-56 overflow-auto whitespace-pre-wrap break-words rounded-lg bg-zinc-50 p-3 font-mono text-xs leading-relaxed text-zinc-700 dark:bg-zinc-900 dark:text-zinc-300',
                value || '—')
        );
        return block;
    }

    function renderExamples(examples) {
        examplesContainer.replaceChildren(...examples.map((example, index) => {
            const card = createElement('article',
                'overflow-hidden rounded-xl border border-zinc-200 dark:border-zinc-800');
            const header = createElement('div',
                'border-b border-zinc-200 bg-zinc-50 px-4 py-2.5 text-xs font-semibold uppercase tracking-wide text-zinc-500 dark:border-zinc-800 dark:bg-zinc-900/50 dark:text-zinc-400',
                `Example ${index + 1}`);
            const grid = createElement('div', 'grid grid-cols-1 gap-4 p-4 sm:grid-cols-2');
            grid.append(
                createValueBlock('Input', example?.input),
                createValueBlock('Output', example?.output)
            );
            card.append(header, grid);
            return card;
        }));
        examplesEmpty.classList.toggle('hidden', examples.length > 0);
    }

    function renderCodeExamples(exampleCodes) {
        codeExamplesContainer.replaceChildren(...exampleCodes.map((example) => {
            const card = createElement('article',
                'overflow-hidden rounded-xl border border-zinc-200 dark:border-zinc-800');
            const header = createElement('div',
                'flex items-center justify-between gap-3 border-b border-zinc-200 bg-zinc-50 px-4 py-2.5 dark:border-zinc-800 dark:bg-zinc-900/50');
            header.append(
                createElement('span',
                    'inline-block rounded-md bg-red-50 px-2 py-0.5 text-xs font-bold text-red-600 dark:bg-red-500/10 dark:text-red-400',
                    labelForLang(example?.lang)),
                createElement('span', 'text-xs text-zinc-400', 'Example code')
            );
            const code = createElement('pre',
                'max-h-80 overflow-auto p-4 font-mono text-xs leading-relaxed text-zinc-700 dark:text-zinc-300',
                example?.code || '—');
            card.append(header, code);
            return card;
        }));
        codeExamplesEmpty.classList.toggle('hidden', exampleCodes.length > 0);
    }

    function renderLimits(limits) {
        limitsContainer.replaceChildren(...limits.map((limit) => {
            const chip = createElement('span',
                'inline-flex items-center gap-2 rounded-lg border border-zinc-200 bg-white px-3 py-1.5 text-xs dark:border-zinc-800 dark:bg-zinc-900');
            chip.append(
                createElement('span', 'font-semibold text-zinc-700 dark:text-zinc-200', labelForLang(limit?.lang)),
                createElement('span', 'text-zinc-500 dark:text-zinc-400', formatLimit(limit?.timeMs))
            );
            return chip;
        }));
        limitsEmpty.classList.toggle('hidden', limits.length > 0);
    }

    function renderProblem(problem) {
        const problemId = problem.id ?? state.currentId;
        title.textContent = problem.title || 'Untitled problem';
        subtitle.textContent = `Problem #${problemId} · Preview`;
        balloon.replaceChildren(createBalloon(problem.balloon));
        complexity.textContent = labelForBigO(problem.bigO);
        problemIdBadge.textContent = `#${problemId}`;

        const recommendationText = String(problem.recommentation ?? '').trim();
        recommendationSection.classList.toggle('hidden', recommendationText.length === 0);
        recommendation.textContent = recommendationText;

        renderExamples(Array.isArray(problem.examples) ? problem.examples : []);
        renderCodeExamples(Array.isArray(problem.exampleCodes) ? problem.exampleCodes : []);
        renderLimits(Array.isArray(problem.limits) ? problem.limits : []);
        renderPdf(problem);
        showState('content');

        requestAnimationFrame(() => {
            renderPdf(problem);
        });
    }

    // Problem loading and modal controls
    async function loadProblem(id) {
        const token = ++state.token;
        state.currentId = id;

        title.textContent = 'Problem';
        subtitle.textContent = `Loading problem #${id}…`;
        balloon.replaceChildren();
        showState('loading');

        try {
            const response = await fetch('/problens/id', {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ id })
            });
            const payload = await response.json().catch(() => ({}));

            if (token !== state.token) return;

            if (!response.ok) {
                throw new Error(payload.error
                    || (response.status === 404 ? 'Problem not found.' : 'Could not load the problem.'));
            }

            renderProblem(payload);
        } catch (requestError) {
            if (token !== state.token) return;

            console.error('Problem view error:', requestError);
            title.textContent = 'Problem';
            subtitle.textContent = 'Problem unavailable';
            balloon.replaceChildren();
            errorMessage.textContent = requestError.message || 'Could not load the problem.';
            showState('error');
        }
    }

    function openProblemView(id) {
        state.lastFocused = document.activeElement instanceof HTMLElement ? document.activeElement : null;
        modal.classList.remove('hidden');
        modal.classList.add('flex');
        closeButton?.focus();

        const problemId = Number(id);
        if (!Number.isInteger(problemId) || problemId < 1) {
            state.currentId = null;
            state.token++;
            title.textContent = 'Problem';
            subtitle.textContent = 'Problem unavailable';
            balloon.replaceChildren();
            errorMessage.textContent = 'The problem id is invalid.';
            showState('error');
            return;
        }

        loadProblem(problemId);
    }

    function closeProblemView() {
        state.token++;
        state.currentId = null;
        modal.classList.add('hidden');
        modal.classList.remove('flex');
        state.lastFocused?.focus?.();
        state.lastFocused = null;
    }

    window.viewProblem = openProblemView;

    // Modal events and startup
    closeButton?.addEventListener('click', closeProblemView);
    closeFooterButton?.addEventListener('click', closeProblemView);
    retryButton?.addEventListener('click', () => {
        if (Number.isInteger(state.currentId)) loadProblem(state.currentId);
    });
    modal.addEventListener('click', (event) => {
        if (event.target === modal) closeProblemView();
    });
    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && !modal.classList.contains('hidden')) closeProblemView();
    });
})();
