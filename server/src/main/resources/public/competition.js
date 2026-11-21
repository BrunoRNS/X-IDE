(() => {
    const history = document.getElementById('submissionHistory');
    if (!history) return;

    const connection = document.getElementById('submissionConnection');
    const message = document.getElementById('submissionHistoryMessage');
    const historyWrap = document.getElementById('submissionHistoryWrap');
    const palette = [
        'text-red-600 dark:text-red-400',
        'text-blue-600 dark:text-blue-400',
        'text-emerald-600 dark:text-emerald-400',
        'text-amber-600 dark:text-amber-400',
        'text-violet-600 dark:text-violet-400',
        'text-pink-600 dark:text-pink-400'
    ];
    const balloonLetters = 'ABCDEFGHIJKLMNOPQRST';
    let socket;
    let retryTimer;
    let refreshTimer;
    let requestPending = false;

    function setConnection(text, online) {
        connection.textContent = text;
        connection.className = `text-xs ${online ? 'text-emerald-600 dark:text-emerald-400' : 'text-zinc-500 dark:text-zinc-400'}`;
    }

    function balloonId(submissionId) {
        const id = Number(submissionId);
        if (!Number.isInteger(id) || id < 1) return '—';
        return balloonLetters[(id - 1) % balloonLetters.length];
    }

    function renderSubmissions(items) {
        if (!Array.isArray(items) || items.length === 0) {
            history.replaceChildren();
            historyWrap.classList.add('hidden');
            message.textContent = 'No submissions yet.';
            message.classList.remove('hidden');
            return;
        }

        message.classList.add('hidden');
        historyWrap.classList.remove('hidden');
        history.replaceChildren(...items.map((submission, index) => {
            const row = document.createElement('tr');
            row.className = 'bg-white text-zinc-700 dark:bg-zinc-950 dark:text-zinc-300';

            const balloonCell = document.createElement('td');
            balloonCell.className = 'whitespace-nowrap px-4 py-3';
            const icon = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
            icon.setAttribute('viewBox', '0 0 24 32');
            icon.setAttribute('width', '16');
            icon.setAttribute('height', '20');
            icon.setAttribute('fill', 'currentColor');
            icon.setAttribute('aria-hidden', 'true');
            icon.classList.add('mr-2', 'inline-block', 'align-middle', ...palette[index % palette.length].split(' '));
            const body = document.createElementNS('http://www.w3.org/2000/svg', 'path');
            body.setAttribute('d', 'M12 1C6.2 1 2 5.5 2 11.5c0 5.3 3.8 10.8 8.2 12.1L12 27l1.8-3.4C18.2 22.3 22 16.8 22 11.5 22 5.5 17.8 1 12 1Z');
            const string = document.createElementNS('http://www.w3.org/2000/svg', 'path');
            string.setAttribute('d', 'M12 27v2c0 1.2-1 2-1 3');
            string.setAttribute('fill', 'none');
            string.setAttribute('stroke', 'currentColor');
            string.setAttribute('stroke-width', '1.5');
            string.setAttribute('stroke-linecap', 'round');
            icon.append(body, string);
            balloonCell.append(icon, document.createTextNode(balloonId(submission.id)));

            const participantCell = document.createElement('td');
            participantCell.className = 'px-4 py-3 font-medium text-zinc-900 dark:text-zinc-100';
            participantCell.textContent = submission.userName || 'Unknown user';

            const idCell = document.createElement('td');
            idCell.className = 'whitespace-nowrap px-4 py-3 font-mono text-xs';
            idCell.textContent = `#${submission.id ?? '—'}`;

            const stateCell = document.createElement('td');
            stateCell.className = 'whitespace-nowrap px-4 py-3 capitalize';
            stateCell.textContent = submission.state || 'unknown';

            row.append(balloonCell, participantCell, idCell, stateCell);
            return row;
        }));
    }

    function requestHistory() {
        if (!socket || socket.readyState !== WebSocket.OPEN || requestPending) return;
        requestPending = true;
        socket.send(JSON.stringify({ route: '/submission/all', payload: null }));
    }

    function connect() {
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        socket = new WebSocket(`${protocol}//${window.location.host}/ws/socket`);

        socket.addEventListener('open', () => {
            setConnection('Online · live updates', true);
            requestHistory();
            refreshTimer = window.setInterval(requestHistory, 10000);
        });
        socket.addEventListener('message', (event) => {
            let response;
            try {
                response = JSON.parse(event.data);
            } catch (error) {
                return;
            }
            if (response.route === '/submission/all') {
                requestPending = false;
                renderSubmissions(response.payload);
            } else if (response.route === '/error') {
                requestPending = false;
                message.textContent = 'Could not load submission history.';
                message.classList.remove('hidden');
            }
        });
        socket.addEventListener('close', () => {
            requestPending = false;
            window.clearInterval(refreshTimer);
            setConnection('Offline · reconnecting…', false);
            retryTimer = window.setTimeout(connect, 3000);
        });
        socket.addEventListener('error', () => socket.close());
    }

    window.addEventListener('beforeunload', () => {
        window.clearTimeout(retryTimer);
        window.clearInterval(refreshTimer);
        if (socket && socket.readyState < WebSocket.CLOSING) socket.close();
    });

    connect();
})();
