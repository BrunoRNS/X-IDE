import { collectEntry, relativePath } from './classroom/folder-files.js';
import { createRoomRenderer } from './classroom/room-renderer.js';

AOS.init({
    duration: 700,
    easing: 'ease-out-cubic',
    once: true,
    offset: 40
});

const API_URL = '/class';
const STORAGE_KEY = 'xide.classroom.id';
const MAX_ROOM_ID = 2147483647;

const elements = {
    roomId: document.getElementById('roomId'),
    folderInput: document.getElementById('folderInput'),
    chooseFolderBtn: document.getElementById('chooseFolderBtn'),
    dropZone: document.getElementById('dropZone'),
    refreshBtn: document.getElementById('refreshBtn'),
    refreshIcon: document.getElementById('refreshIcon'),
    uploadStatus: document.getElementById('uploadStatus'),
    uploadStatusIcon: document.getElementById('uploadStatusIcon'),
    uploadStatusText: document.getElementById('uploadStatusText'),
    roomCount: document.getElementById('roomCount'),
    emptyState: document.getElementById('emptyState'),
    roomsGrid: document.getElementById('roomsGrid'),
    ipDialog: document.getElementById('ipDialog'),
    expandedIp: document.getElementById('expandedIp')
};

const state = {
    rooms: new Map(),
    busy: false
};

/* -------------------------------------------------------------------------- */
/* DOM and room helpers                                                       */
/* -------------------------------------------------------------------------- */

function el(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text !== undefined) node.textContent = text;
    return node;
}

function icon(classes) {
    const node = el('i', classes);
    node.setAttribute('aria-hidden', 'true');
    return node;
}

function formatHost(ip) {
    return ip.includes(':') && !ip.startsWith('[') ? `[${ip}]` : ip;
}

function buildUrl(ip, port) {
    return `http://${formatHost(ip)}:${port}`;
}

function saveRoomId(id) {
    try {
        localStorage.setItem(STORAGE_KEY, String(id));
    } catch (error) {
        console.debug('Could not persist classroom ID:', error);
    }
}

function savedRoomId() {
    try {
        const id = Number(localStorage.getItem(STORAGE_KEY));
        return Number.isInteger(id) && id > 0 ? id : null;
    } catch (error) {
        return null;
    }
}

function validateRoomId() {
    const id = Number(elements.roomId.value);

    if (!Number.isInteger(id) || id < 1 || id > MAX_ROOM_ID) {
        showStatus(`Enter a classroom ID between 1 and ${MAX_ROOM_ID}.`, 'error');
        elements.roomId.focus();
        return null;
    }

    saveRoomId(id);
    return id;
}

function normalizeRoom(raw) {
    if (!raw || typeof raw !== 'object') return null;

    const id = Number(raw.id);
    const port = Number(raw.port);
    const ip = typeof raw.ip === 'string' && raw.ip.trim()
        ? raw.ip.trim()
        : window.location.hostname;

    if (!Number.isInteger(id) || id < 1) return null;
    if (!Number.isInteger(port) || port < 1 || port > 65535) return null;
    if (!ip) return null;

    const files = Number(raw.files);
    const url = typeof raw.url === 'string' && /^https?:\/\//i.test(raw.url)
        ? raw.url
        : buildUrl(ip, port);

    return {
        id,
        ip,
        port,
        files: Number.isInteger(files) && files >= 0 ? files : null,
        url
    };
}

// Upload feedback and controls
function showStatus(message, tone = 'info') {
    const tones = {
        info: {
            border: 'border-zinc-200 dark:border-zinc-800',
            icon: 'fa-solid fa-circle-info mt-0.5 text-red-600',
            text: 'leading-6 text-zinc-600 dark:text-zinc-300'
        },
        success: {
            border: 'border-emerald-200 dark:border-emerald-900/60',
            icon: 'fa-solid fa-circle-check mt-0.5 text-emerald-600',
            text: 'leading-6 text-emerald-700 dark:text-emerald-400'
        },
        error: {
            border: 'border-red-200 dark:border-red-900/60',
            icon: 'fa-solid fa-circle-exclamation mt-0.5 text-red-600',
            text: 'leading-6 text-red-600 dark:text-red-400'
        }
    };

    const style = tones[tone] || tones.info;

    elements.uploadStatus.className =
        `mt-4 flex items-start gap-3 rounded-lg border px-4 py-3 text-sm ${style.border}`;
    elements.uploadStatusIcon.className = style.icon;
    elements.uploadStatusText.className = style.text;
    elements.uploadStatusText.textContent = message;
}

function setBusy(busy) {
    state.busy = busy;
    elements.chooseFolderBtn.disabled = busy;
    elements.refreshBtn.disabled = busy;
    elements.roomId.disabled = busy;
    elements.dropZone.setAttribute('aria-disabled', String(busy));
}

async function copyText(text) {
    if (navigator.clipboard && window.isSecureContext) {
        return navigator.clipboard.writeText(text);
    }

    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.setAttribute('readonly', '');
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.append(textarea);
    textarea.select();

    const copied = document.execCommand('copy');
    textarea.remove();

    if (!copied) throw new Error('Copy failed');
}

/* -------------------------------------------------------------------------- */
/* Folder selection                                                            */
/* -------------------------------------------------------------------------- */

function handleFolderInput() {
    const files = Array.from(elements.folderInput.files || []);
    elements.folderInput.value = '';

    if (!files.length) return;

    const entries = files
        .map((file) => ({ file, path: relativePath(file) }))
        .filter((entry) => entry.path);

    uploadEntries(entries);
}

async function handleDrop(event) {
    event.preventDefault();
    elements.dropZone.classList.remove('is-dragging');

    if (state.busy) return;

    try {
        const items = Array.from(event.dataTransfer?.items || []);
        const entries = [];
        const supportsEntries = items.some((item) => typeof item.webkitGetAsEntry === 'function');

        if (supportsEntries) {
            // Snapshot the entries before awaiting: DataTransferItem is only valid
            // while the drop event handler is running.
            const droppedEntries = items
                .map((item) => item.webkitGetAsEntry())
                .filter(Boolean);

            for (const entry of droppedEntries) {
                entries.push(...await collectEntry(entry, ''));
            }
        } else {
            const files = Array.from(event.dataTransfer?.files || []);
            entries.push(...files.map((file) => ({ file, path: relativePath(file) })));
        }

        if (!entries.length) {
            showStatus('No files were found in that drop.', 'error');
            return;
        }

        uploadEntries(entries);
    } catch (error) {
        console.error('Drop error:', error);
        showStatus('Could not read the dropped folder.', 'error');
    }
}

/* -------------------------------------------------------------------------- */
/* Upload and API requests                                                    */
/* -------------------------------------------------------------------------- */

function errorMessage(body) {
    try {
        const parsed = JSON.parse(body);
        if (parsed && typeof parsed.error === 'string') return parsed.error;
    } catch (error) {
        // The body is not JSON.
    }

    const text = (body || '').trim();
    return text.length > 0 && text.length < 200 ? text : '';
}

function roomFromResponse(roomId, body, fileCount) {
    let data = null;

    try {
        data = JSON.parse(body);
    } catch (error) {
        // Older servers return only the port as plain text.
    }

    if (data && typeof data === 'object' && data.port !== undefined) {
        return normalizeRoom({ id: roomId, files: fileCount, ...data });
    }

    const port = Number((body || '').trim());
    if (Number.isInteger(port) && port > 0 && port <= 65535) {
        return normalizeRoom({
            id: roomId,
            ip: window.location.hostname || '127.0.0.1',
            port,
            files: fileCount
        });
    }

    return null;
}

async function uploadEntries(entries) {
    if (state.busy) return;

    const roomId = validateRoomId();
    if (roomId === null) return;

    if (!entries.length) {
        showStatus('That folder is empty. Choose a folder with at least one file.', 'error');
        return;
    }

    const formData = new FormData();
    for (const entry of entries) {
        // The manifest keeps the folder structure even when the browser strips
        // directories from the multipart filename.
        formData.append('paths', entry.path);
        formData.append('files', entry.file, entry.path);
    }

    setBusy(true);
    showStatus(
        `Uploading ${entries.length} ${entries.length === 1 ? 'file' : 'files'} to classroom ${roomId}…`,
        'info'
    );

    try {
        const response = await fetch(`${API_URL}/${roomId}`, {
            method: 'POST',
            headers: { 'Accept': 'application/json' },
            body: formData
        });

        const body = await response.text();

        if (!response.ok) {
            throw new Error(errorMessage(body) || `Request failed with status ${response.status}.`);
        }

        const room = roomFromResponse(roomId, body, entries.length);
        if (!room) throw new Error('The server did not return an address for this room.');

        state.rooms.set(String(room.id), room);
        renderRooms();
        showStatus(`Classroom ${room.id} is online at ${formatHost(room.ip)}:${room.port}.`, 'success');

        await loadRooms({ silent: true });
    } catch (error) {
        console.error('Upload error:', error);
        showStatus(error.message || 'Could not host this folder. Check the server connection and try again.', 'error');
    } finally {
        setBusy(false);
    }
}

const renderRooms = createRoomRenderer({
    elements,
    state,
    el,
    icon,
    formatHost,
    copyText
});

async function loadRooms({ silent = false } = {}) {
    if (!silent) {
        elements.refreshIcon.classList.add('fa-spin');
        elements.refreshBtn.disabled = true;
    }

    try {
        const response = await fetch(API_URL, {
            headers: { 'Accept': 'application/json' },
            cache: 'no-store'
        });

        if (!response.ok) {
            throw new Error(`Request failed with status ${response.status}.`);
        }

        const payload = await response.json();
        const roomsPayload = Array.isArray(payload)
            ? payload
            : Array.isArray(payload.classes) ? payload.classes : [];

        const rooms = new Map();
        for (const item of roomsPayload) {
            const room = normalizeRoom(item);
            if (room) rooms.set(String(room.id), room);
        }

        state.rooms = rooms;
        renderRooms();
    } catch (error) {
        console.error('Load rooms error:', error);
        if (!silent) {
            showStatus('Could not load the open rooms. Check the server connection and try again.', 'error');
        }
    } finally {
        elements.refreshIcon.classList.remove('fa-spin');
        elements.refreshBtn.disabled = state.busy;
    }
}

/* -------------------------------------------------------------------------- */
/* Events                                                                      */
/* -------------------------------------------------------------------------- */

elements.chooseFolderBtn.addEventListener('click', () => {
    if (!state.busy) elements.folderInput.click();
});

elements.folderInput.addEventListener('change', handleFolderInput);

elements.refreshBtn.addEventListener('click', () => loadRooms());

elements.ipDialog.addEventListener('click', (event) => {
    if (event.target === elements.ipDialog) elements.ipDialog.close();
});

elements.roomId.addEventListener('change', () => {
    const id = Number(elements.roomId.value);
    if (Number.isInteger(id) && id > 0) saveRoomId(id);
});

elements.dropZone.addEventListener('click', () => {
    if (!state.busy) elements.folderInput.click();
});

elements.dropZone.addEventListener('keydown', (event) => {
    if (event.key !== 'Enter' && event.key !== ' ') return;

    event.preventDefault();
    if (!state.busy) elements.folderInput.click();
});

['dragenter', 'dragover'].forEach((type) => {
    elements.dropZone.addEventListener(type, (event) => {
        event.preventDefault();
        event.stopPropagation();

        if (!state.busy) elements.dropZone.classList.add('is-dragging');
    });
});

elements.dropZone.addEventListener('dragleave', (event) => {
    if (!elements.dropZone.contains(event.relatedTarget)) {
        elements.dropZone.classList.remove('is-dragging');
    }
});

elements.dropZone.addEventListener('drop', handleDrop);

window.addEventListener('dragover', (event) => {
    if (Array.from(event.dataTransfer?.types || []).includes('Files')) {
        event.preventDefault();
    }
});

window.addEventListener('drop', (event) => {
    if (Array.from(event.dataTransfer?.types || []).includes('Files')) {
        event.preventDefault();
    }
});

const initialRoomId = savedRoomId();
if (initialRoomId !== null) {
    elements.roomId.value = String(initialRoomId);
}

loadRooms();
