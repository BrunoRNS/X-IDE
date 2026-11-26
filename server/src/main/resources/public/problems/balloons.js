import { balloonColors, problemForm } from './state.js';

export function createBalloon(filled, color) {
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

export function selectBalloon(button, color) {
    const selected = document.getElementById('dropdown-selected');
    selected.replaceChildren(createBalloon(true, color));

    let colorInput = document.getElementById('balloonColor');
    if (!colorInput) {
        colorInput = document.createElement('input');
        colorInput.type = 'hidden';
        colorInput.id = 'balloonColor';
        colorInput.name = 'color';
        (button?.closest('form') || problemForm).appendChild(colorInput);
    }
    colorInput.value = color;

    document.getElementById('content-dropdown').classList.add('hidden');

    const dropdownButton = document.getElementById('button-dropdown');
    dropdownButton.innerHTML = `
        <span id="dropdown-selected" class="flex items-center gap-2"></span>
        <i class="fa-solid fa-chevron-down text-xs text-zinc-400"></i>
    `;
    dropdownButton.querySelector('#dropdown-selected').append(createBalloon(true, color));
}

export function openDropdown() {
    const content = document.getElementById('content-dropdown');
    const chevron = document.getElementById('dropdown-chevron');
    const isHidden = content.classList.contains('hidden');

    content.classList.toggle('hidden', !isHidden);
    chevron?.classList.toggle('rotate-180', isHidden);
}

export function initializeBalloonPicker() {
    const container = document.getElementById('content-dropdown');
    if (!container) return;

    balloonColors.forEach((color) => {
        const button = document.createElement('button');
        button.type = 'button';
        button.dataset.color = color;
        button.className = 'flex items-center justify-center rounded-lg transition hover:bg-zinc-100 dark:hover:bg-zinc-800';
        button.append(createBalloon(true, color));
        button.addEventListener('click', () => selectBalloon(button, color));
        container.append(button);
    });

    document.addEventListener('click', (event) => {
        const dropdown = document.getElementById('button-dropdown');
        const content = document.getElementById('content-dropdown');
        if (!dropdown || !content) return;

        if (!dropdown.contains(event.target) && !content.classList.contains('hidden')) {
            content.classList.add('hidden');
        }
    });
}
