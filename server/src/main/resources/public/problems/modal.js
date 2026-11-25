const studentModal = document.getElementById('studentModal');
const studentModalContent = document.getElementById('studentModalContent');
const competitionForm = document.getElementById('competitionForm');
const itensId = [];


async function getAllProblens() {
    const result = await fetch("/problens/all");
    if (!result.ok) {
        return [];
    }
    return await result.json();
}

export async function createItens() {
    const select = document.getElementById('options');
    const list = await getAllProblens();
    for (const item of list) {
        const newItem = document.createElement("option");
        newItem.value = item.title;
        newItem.value = item.id;
        newItem.textContent = item.title;
        select.appendChild(newItem);
    }
}

export function addItem() {
    const select = document.getElementById('options');
    const list = document.getElementById('list-selections');
    const span = document.getElementById('selectionCount');
    const selected = select.value;

    const selectedTitle = select.options[select.selectedIndex].text;


    if (selected === "") {
        alert("Please select an item!");
        return;
    }

    if (itensId.includes(selected)) {
        alert("Item already selected!");
        return;
    }


    itensId.push(selected);

    const emptyMsg = document.getElementById('emptySelection');
    if (emptyMsg) {
        emptyMsg.remove();
    }


    const currentCount = parseInt(span.textContent, 10) || 0;
    const newCount = currentCount + 1;
    span.textContent = `${newCount} ${newCount === 1 ? 'item' : 'items'}`;
    const newItem = document.createElement('li');

    newItem.className = `
    flex items-center justify-between gap-3
    px-4 py-3
    transition-colors
    hover:bg-zinc-50
    dark:hover:bg-zinc-900/60
`;

    const itemName = document.createElement('span');

    itemName.textContent = selectedTitle;

    itemName.className = `
    flex-1 min-w-0
    text-sm font-medium
    text-zinc-700 dark:text-zinc-300
    break-words
`;

    const buttonRemove = document.createElement('button');

    buttonRemove.type = 'button';
    buttonRemove.innerHTML = '<i class="fa-solid fa-trash-can"></i>';

    buttonRemove.title = 'Remove item';
    buttonRemove.setAttribute('aria-label', `Remove ${selectedTitle}`);

    buttonRemove.className = `
    flex h-8 w-8 shrink-0 items-center justify-center
    rounded-lg
    text-zinc-400
    transition-all duration-200
    hover:bg-red-100 hover:text-red-600
    dark:hover:bg-red-950/50 dark:hover:text-red-400
`;

    buttonRemove.addEventListener('click', () => {
        newItem.remove();
    });


    newItem.appendChild(itemName);



    buttonRemove.onclick = function () {

        list.removeChild(newItem);

        const index = itensId.indexOf(selectedId);
        if (index > -1) {
            itensId.splice(index, 1);
        }


        const updatedCount = (parseInt(span.textContent, 10) || 1) - 1;
        span.textContent = `${updatedCount} ${updatedCount === 1 ? 'item' : 'items'}`;
    };

    newItem.appendChild(buttonRemove);
    list.appendChild(newItem);
    select.value = "";
}


export function openModal(options = {}) {
    if (!studentModal || !studentModalContent) return;


    if (options.title) {
        document.getElementById('modalTitle').textContent = options.title;
    }

    if (options.description) {
        document.getElementById('modalDescription').textContent =
            options.description;
    }


    if (options.reset !== false) {
        studentModal.querySelectorAll('form').forEach(form => form.reset());
    }

    showModal();
}

function showModal() {
    studentModal.classList.remove('hidden');
    studentModal.classList.add('flex');

    requestAnimationFrame(() => {
        studentModalContent.classList.remove('scale-95', 'opacity-0');
        studentModalContent.classList.add('scale-100', 'opacity-100');
    });
}

export function closeStudentModal() {
    if (!studentModal || !studentModalContent) return;

    studentModalContent.classList.remove('scale-100', 'opacity-100');
    studentModalContent.classList.add('scale-95', 'opacity-0');

    setTimeout(() => {
        studentModal.classList.add('hidden');
        studentModal.classList.remove('flex');
    }, 300);
}


if (studentModal) {
    studentModal.addEventListener('click', (event) => {
        if (event.target === studentModal) {
            closeStudentModal();
        }
    });


    document.addEventListener('keydown', (event) => {
        if (
            event.key === 'Escape' &&
            !studentModal.classList.contains('hidden')
        ) {
            closeStudentModal();
        }
    });

    competitionForm?.addEventListener('submit', async (event) => {
        event.preventDefault();

        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;


        if (new Date(endDate) <= new Date(startDate)) {
            alert('End date must be after start date!');
            return;
        }

        if (itensId.length === 0) {
            alert('Select at least one problem!');
            return;
        }


        const competitionData = {
            startDate: new Date(startDate).toISOString(),
            endDate: new Date(endDate).toISOString(),
            problemIds: itensId.map(Number)
        };

        const submitButton = competitionForm.querySelector(
            'button[type="submit"]'
        );

        submitButton.disabled = true;

        try {
            const response = await fetch('/competition/start', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(competitionData)
            });

            if (!response.ok) {
                throw new Error('Failed to create competition');
            }

            const result = await response.json().catch(() => ({}));

            console.log('Competition created:', result);

            alert('Competition created successfully!');

            closeStudentModal();
            startCountdown();



        } catch (error) {
            console.error('Error:', error);
            alert('An error occurred while creating the competition.');

        } finally {
            submitButton.disabled = false;
        }
    });

}


window.closeStudentModal = closeStudentModal;

let countdownSocket = null;
let countdownTimer = null;
let endCountdownTimer = null;

function formatCountdown(seconds) {
    const safe = Math.max(0, seconds);
    const minutes = Math.floor(safe / 60);
    const rest = safe % 60;
    return `${minutes}:${String(rest).padStart(2, '0')}`;
}


function startCountdown() {
    const overlay = document.getElementById('countdownOverlay');
    if (!overlay) return;

    clearInterval(countdownTimer);
    clearInterval(endCountdownTimer);

    if (countdownSocket) {
        countdownSocket.close();
        countdownSocket = null;
    }

    const valueEl = document.getElementById('countdownValue');
    const labelEl = document.getElementById('countdownLabel');
    const endEl = document.getElementById('countdownEnd');

    overlay.classList.remove('hidden');
    overlay.classList.add('flex');

    labelEl.textContent = 'Starts in';
    valueEl.textContent = '--';
    endEl.textContent = '';

    const protocol =
        window.location.protocol === 'https:' ? 'wss:' : 'ws:';

    countdownSocket = new WebSocket(
        `${protocol}//${window.location.host}/ws/socket`
    );

    countdownSocket.addEventListener('open', () => {

        countdownSocket.send(JSON.stringify({
            route: '/competition/time-to-start',
            payload: null
        }));

        countdownSocket.send(JSON.stringify({
            route: '/competition/time-to-end',
            payload: null
        }));

    });

    countdownSocket.addEventListener('message', (event) => {

        let response;

        try {
            response = JSON.parse(event.data);
        } catch {
            return;
        }

        const total = Number(response.payload);

        if (!Number.isFinite(total)) return;

        if (response.route === '/competition/time-to-start') {

            if (total < 0) {
                showCompetitionStarted();
                return;
            }

            runCountdown(total);

        }

        if (response.route === '/competition/time-to-end') {

            if (total < 0) {
                document.getElementById('countdownEnd')
                    .textContent = 'Competition ended';
                return;
            }

            runEndCountdown(total);
        }

    });

    countdownSocket.addEventListener('error', () => {
        console.error('Countdown WebSocket error');
    });

}


function runCountdown(totalSeconds) {
    clearInterval(countdownTimer);

    const valueEl = document.getElementById('countdownValue');
    const labelEl = document.getElementById('countdownLabel');

    const targetTime = Date.now() + (totalSeconds * 1000);

    const render = () => {
        const remaining = Math.ceil(
            (targetTime - Date.now()) / 1000
        );

        if (remaining <= 0) {
            clearInterval(countdownTimer);
            showCompetitionStarted();
            return;
        }

        labelEl.textContent = 'Starts in';
        valueEl.textContent = formatCountdown(remaining);

        valueEl.style.color = remaining <= 10
            ? '#ef4444'
            : '#ffffff';
    };

    render();
    countdownTimer = setInterval(render, 250);
}

function runEndCountdown(totalSeconds) {
    clearInterval(endCountdownTimer);

    const endEl = document.getElementById('countdownEnd');

    const targetTime = Date.now() + (totalSeconds * 1000);

    const render = () => {
        const remaining = Math.ceil(
            (targetTime - Date.now()) / 1000
        );

        if (remaining <= 0) {
            endEl.textContent = 'Competition ended';
            clearInterval(endCountdownTimer);
            return;
        }

        endEl.textContent = `Ends in ${formatCountdown(remaining)}`;
    };

    render();
    endCountdownTimer = setInterval(render, 250);
}

function showCompetitionStarted() {
    clearInterval(countdownTimer);

    const valueEl = document.getElementById('countdownValue');
    const labelEl = document.getElementById('countdownLabel');

    labelEl.textContent = 'Competition started';
    valueEl.textContent = 'LIVE';
    valueEl.style.color = '#22c55e';
}

function hideCountdown() {
    window.clearInterval(countdownTimer);
    const overlay = document.getElementById('countdownOverlay');
    if (overlay) {
        overlay.classList.add('hidden');
        overlay.classList.remove('flex');
    }
    if (countdownSocket) {
        countdownSocket.close();
        countdownSocket = null;
    }
}

createItens();