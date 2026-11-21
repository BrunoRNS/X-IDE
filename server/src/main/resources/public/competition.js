(() => {
    const history = document.getElementById('submissionHistory');
    if (!history) return;

    const connection = document.getElementById('submissionConnection');
    const message = document.getElementById('submissionHistoryMessage');
    const historyWrap = document.getElementById('submissionHistoryWrap');
    const soundToggleButton = document.getElementById('soundToggleButton');
    const historyView = document.getElementById('submissionHistoryView');
    const rankingView = document.getElementById('rankingView');
    const rankingHead = document.getElementById('rankingHead');
    const rankingBody = document.getElementById('rankingBody');
    const rankingMessage = document.getElementById('rankingMessage');
    const rankingWrap = document.getElementById('rankingWrap');
    const rankingToggleButton = document.getElementById('rankingToggleButton');
    const balloonColors = [
        'text-red-600 dark:text-red-400', 'text-blue-600 dark:text-blue-400',
        'text-emerald-600 dark:text-emerald-400', 'text-amber-600 dark:text-amber-400',
        'text-violet-600 dark:text-violet-400', 'text-pink-600 dark:text-pink-400',
        'text-cyan-600 dark:text-cyan-400', 'text-orange-600 dark:text-orange-400',
        'text-lime-600 dark:text-lime-400', 'text-fuchsia-600 dark:text-fuchsia-400',
        'text-indigo-600 dark:text-indigo-400', 'text-teal-600 dark:text-teal-400',
        'text-rose-600 dark:text-rose-400', 'text-sky-600 dark:text-sky-400',
        'text-green-600 dark:text-green-400', 'text-yellow-600 dark:text-yellow-400',
        'text-purple-600 dark:text-purple-400', 'text-stone-600 dark:text-stone-400',
        'text-red-500 dark:text-red-300', 'text-blue-500 dark:text-blue-300'
    ];
    let socket;
    let retryTimer;
    let refreshTimer;
    let requestPending = false;
    let showingRanking = false;
    let soundContext = null;
    let soundLimiter = null;
    let soundEnabled = false;
    let seenSubmissionIds = null;

    function setConnection(text, online) {
        connection.textContent = text;
        connection.className = `text-xs ${online ? 'text-emerald-600 dark:text-emerald-400' : 'text-zinc-500 dark:text-zinc-400'}`;
    }

    function createBalloon(filled, colorClass) {
        const icon = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        icon.setAttribute('viewBox', '0 0 24 32');
        icon.setAttribute('width', '20');
        icon.setAttribute('height', '26');
        icon.setAttribute('aria-hidden', 'true');
        icon.classList.add('inline-block', ...colorClass.split(/\s+/));
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

    function renderHistory(items) {
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
            row.dataset.submissionId = String(submission.id ?? '');
            row.className = 'bg-white text-zinc-700 dark:bg-zinc-950 dark:text-zinc-300';
            const balloonCell = document.createElement('td');
            balloonCell.className = 'whitespace-nowrap px-4 py-3';
            const balloon = createBalloon(submission.state === 'accepted', balloonColors[index % balloonColors.length]);
            balloon.classList.add('mr-2', 'align-middle');
            balloonCell.append(balloon, document.createTextNode(submission.problemTitle || 'Unknown problem'));

            const participantCell = document.createElement('td');
            participantCell.className = 'px-4 py-3 font-medium text-zinc-900 dark:text-zinc-100';
            participantCell.textContent = submission.userName || 'Unknown user';
            const idCell = document.createElement('td');
            idCell.className = 'whitespace-nowrap px-4 py-3 font-mono text-xs';
            idCell.textContent = `#${submission.id ?? '—'}`;
            const stateCell = document.createElement('td');
            stateCell.className = 'whitespace-nowrap px-4 py-3 capitalize';
            stateCell.textContent = (submission.state || 'unknown').replaceAll('_', ' ');
            row.append(balloonCell, participantCell, idCell, stateCell);
            return row;
        }));
    }

    function playPlim() {
        if (!soundEnabled || !soundContext || soundContext.state !== 'running') return;

        const now = soundContext.currentTime;
        const notes = [
            { frequency: 880, start: 0, duration: 0.62 },
            { frequency: 1174.66, start: 0.13, duration: 0.72 },
            { frequency: 1760, start: 0.3, duration: 0.55 }
        ];

        for (const note of notes) {
            const oscillator = soundContext.createOscillator();
            const gain = soundContext.createGain();
            const start = now + note.start;
            const end = start + note.duration;

            oscillator.type = 'sine';
            oscillator.frequency.setValueAtTime(note.frequency, start);
            gain.gain.setValueAtTime(0.0001, start);
            gain.gain.exponentialRampToValueAtTime(0.72, start + 0.025);
            gain.gain.setValueAtTime(0.72, start + 0.12);
            gain.gain.exponentialRampToValueAtTime(0.0001, end);

            oscillator.connect(gain);
            gain.connect(soundLimiter || soundContext.destination);
            oscillator.start(start);
            oscillator.stop(end + 0.02);
        }
    }

    async function toggleSound() {
        if (soundEnabled) {
            soundEnabled = false;
            soundToggleButton.setAttribute('aria-pressed', 'false');
            soundToggleButton.innerHTML = '<i class="fa-solid fa-volume-high" aria-hidden="true"></i> Enable sound';
            return;
        }

        const AudioContextType = window.AudioContext || window.webkitAudioContext;
        if (!AudioContextType) {
            soundToggleButton.textContent = 'Sound unavailable';
            soundToggleButton.disabled = true;
            return;
        }

        try {
            if (!soundContext) {
                soundContext = new AudioContextType();
                soundLimiter = soundContext.createDynamicsCompressor();
                soundLimiter.threshold.setValueAtTime(-8, soundContext.currentTime);
                soundLimiter.knee.setValueAtTime(8, soundContext.currentTime);
                soundLimiter.ratio.setValueAtTime(12, soundContext.currentTime);
                soundLimiter.attack.setValueAtTime(0.003, soundContext.currentTime);
                soundLimiter.release.setValueAtTime(0.2, soundContext.currentTime);
                soundLimiter.connect(soundContext.destination);
            }
            await soundContext.resume();
            if (soundContext.state !== 'running') throw new Error('Audio is not running');
            soundEnabled = true;
            soundToggleButton.setAttribute('aria-pressed', 'true');
            soundToggleButton.innerHTML = '<i class="fa-solid fa-volume-xmark" aria-hidden="true"></i> Mute alerts';
            playPlim();
        } catch (error) {
            console.warn('Could not enable submission sounds:', error);
            soundToggleButton.textContent = 'Could not enable sound';
        }
    }

    soundToggleButton.addEventListener('click', toggleSound);

    function renderRanking(board) {
        if (!board || !Array.isArray(board.problems) || !Array.isArray(board.participants)
            || !Array.isArray(board.submissions) || board.participants.length === 0) {
            rankingBody.replaceChildren();
            rankingHead.replaceChildren();
            rankingWrap.classList.add('hidden');
            rankingMessage.textContent = 'No participants to rank yet.';
            rankingMessage.classList.remove('hidden');
            return;
        }

        rankingMessage.classList.add('hidden');
        rankingWrap.classList.remove('hidden');
        const headerLabels = ['Rank', 'Participant', 'Solved', 'Time'];
        const headerCells = headerLabels.map((label) => {
            const cell = document.createElement('th');
            cell.scope = 'col';
            cell.className = 'whitespace-nowrap px-4 py-3 font-medium';
            cell.textContent = label;
            return cell;
        });
        for (const [index, problem] of board.problems.entries()) {
            const cell = document.createElement('th');
            cell.scope = 'col';
            cell.className = 'min-w-36 max-w-48 whitespace-normal break-words px-3 py-3 text-center align-bottom font-medium leading-tight';
            cell.title = problem.title || `Problem ${index + 1}`;
            cell.textContent = cell.title;
            headerCells.push(cell);
        }
        rankingHead.replaceChildren(...headerCells);

        const submissionsByUser = new Map();
        for (const submission of board.submissions) {
            const userId = String(submission.userId);
            if (!submissionsByUser.has(userId)) submissionsByUser.set(userId, []);
            submissionsByUser.get(userId).push(submission);
        }

        const standings = board.participants.map((participant) => {
            const submissions = submissionsByUser.get(String(participant.id)) || [];
            const firstAttempt = submissions.reduce((earliest, submission) => {
                if (!submission.date) return earliest;
                const date = Date.parse(submission.date);
                return Number.isFinite(date) && (earliest === null || date < earliest) ? date : earliest;
            }, null);
            const solvedAt = new Map();
            for (const submission of submissions) {
                if (submission.state !== 'accepted' || !submission.date) continue;
                const date = Date.parse(submission.date);
                if (!Number.isFinite(date)) continue;
                const problemId = String(submission.problemId);
                if (!solvedAt.has(problemId) || date < solvedAt.get(problemId)) solvedAt.set(problemId, date);
            }
            const timeMinutes = firstAttempt === null ? 0 : Array.from(solvedAt.values())
                .reduce((total, solvedTime) => total + Math.max(0, Math.floor((solvedTime - firstAttempt) / 60000)), 0);
            return { participant, solvedAt, solved: solvedAt.size, timeMinutes };
        }).sort((a, b) => b.solved - a.solved || a.timeMinutes - b.timeMinutes
            || (a.participant.userName || '').localeCompare(b.participant.userName || ''));

        rankingBody.replaceChildren(...standings.map((standing, index) => {
            const row = document.createElement('tr');
            row.className = 'bg-white text-zinc-700 dark:bg-zinc-950 dark:text-zinc-300';

            const rankCell = document.createElement('td');
            rankCell.className = 'px-4 py-3 font-semibold';
            rankCell.textContent = String(index + 1);
            const participantCell = document.createElement('td');
            participantCell.className = 'whitespace-nowrap px-4 py-3 font-medium text-zinc-900 dark:text-zinc-100';
            participantCell.textContent = standing.participant.userName || 'Unknown user';
            const solvedCell = document.createElement('td');
            solvedCell.className = 'px-4 py-3 font-semibold';
            solvedCell.textContent = String(standing.solved);
            const timeCell = document.createElement('td');
            timeCell.className = 'whitespace-nowrap px-4 py-3 tabular-nums';
            timeCell.textContent = `${standing.timeMinutes} min`;
            row.append(rankCell, participantCell, solvedCell, timeCell);

            for (const [problemIndex, problem] of board.problems.entries()) {
                const cell = document.createElement('td');
                cell.className = 'px-3 py-2 text-center';
                cell.title = problem.title || `Problem ${problemIndex + 1}`;
                const accepted = standing.solvedAt.has(String(problem.id));
                cell.append(createBalloon(accepted, balloonColors[problemIndex % balloonColors.length]));
                row.append(cell);
            }
            return row;
        }));
    }

    function requestCurrentView() {
        if (!socket || socket.readyState !== WebSocket.OPEN || requestPending) return;
        requestPending = true;
        socket.send(JSON.stringify({ route: showingRanking ? '/submission/ranking' : '/submission/all', payload: null }));
    }

    rankingToggleButton.addEventListener('click', () => {
        showingRanking = !showingRanking;
        historyView.classList.toggle('hidden', showingRanking);
        rankingView.classList.toggle('hidden', !showingRanking);
        rankingToggleButton.setAttribute('aria-pressed', String(showingRanking));
        rankingToggleButton.innerHTML = showingRanking
            ? '<i class="fa-solid fa-list" aria-hidden="true"></i> Submission history'
            : '<i class="fa-solid fa-trophy text-amber-500" aria-hidden="true"></i> Ranking';
        requestCurrentView();
    });

    function connect() {
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        socket = new WebSocket(`${protocol}//${window.location.host}/ws/socket`);

        socket.addEventListener('open', () => {
            setConnection('Online · live updates', true);
            requestCurrentView();
            refreshTimer = window.setInterval(requestCurrentView, 10000);
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
                const currentIds = new Set(Array.isArray(response.payload)
                    ? response.payload.map(item => String(item.id))
                    : []);
                const newSubmission = seenSubmissionIds !== null
                    && Array.from(currentIds).some(id => !seenSubmissionIds.has(id));
                seenSubmissionIds = currentIds;
                renderHistory(response.payload);
                if (newSubmission) playPlim();
            } else if (response.route === '/submission/ranking') {
                requestPending = false;
                renderRanking(response.payload);
            } else if (response.route === '/error') {
                requestPending = false;
                const activeMessage = showingRanking ? rankingMessage : message;
                activeMessage.textContent = showingRanking ? 'Could not load ranking.' : 'Could not load submission history.';
                activeMessage.classList.remove('hidden');
            }
            if ((response.route === '/submission/all' || response.route === '/submission/ranking')
                && response.route !== (showingRanking ? '/submission/ranking' : '/submission/all')) {
                requestCurrentView();
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
