export function createRoomRenderer({
    elements,
    state,
    el,
    icon,
    formatHost,
    copyText
}) {
    function createRoomCard(room, index) {
        const card = el('article', 'room-card group flex flex-col rounded-2xl border border-zinc-200 ' +
            'bg-white p-5 transition-colors hover:border-red-600 ' +
            'dark:border-zinc-800 dark:bg-zinc-950 dark:hover:border-red-600');
        card.style.animationDelay = `${Math.min(index * 45, 270)}ms`;

        const header = el('div', 'flex items-start justify-between gap-3');
        const identity = el('div', 'flex items-center gap-3');

        const badge = el('span', 'flex h-10 w-10 shrink-0 items-center justify-center rounded-xl ' +
            'bg-red-600/10 text-red-600');
        badge.append(icon('fa-solid fa-chalkboard'));

        const titleWrap = el('div');
        titleWrap.append(el('h3', 'text-sm font-semibold', `Classroom ${room.id}`));

        const online = el('p', 'mt-0.5 flex items-center gap-1.5 text-xs text-zinc-500 dark:text-zinc-400');
        online.append(
            el('span', 'h-1.5 w-1.5 rounded-full bg-emerald-500'),
            document.createTextNode('Online')
        );
        titleWrap.append(online);

        identity.append(badge, titleWrap);
        header.append(
            identity,
            el('span', 'rounded-full border border-zinc-200 px-2.5 py-1 text-[11px] ' +
                'font-medium text-zinc-500 dark:border-zinc-800 dark:text-zinc-400', `ID ${room.id}`)
        );

        const addressBox = el('div', 'mt-4 rounded-xl border border-zinc-200 bg-zinc-50 p-3 ' +
            'dark:border-zinc-800 dark:bg-zinc-900/60');
        addressBox.append(el('p', 'text-[10px] font-medium uppercase tracking-wider ' +
            'text-zinc-400 dark:text-zinc-500', 'Address'));

        const addressRow = el('div', 'mt-1 flex min-w-0 items-center gap-2');
        const address = el('code', 'min-w-0 flex-1 truncate font-mono text-sm font-medium ' +
            'text-zinc-800 dark:text-zinc-100', `${formatHost(room.ip)}:${room.port}`);
        address.title = room.url;

        const copyButton = el('button', 'action-button flex h-8 w-8 shrink-0 items-center justify-center ' +
            'rounded-lg border border-zinc-200 text-zinc-500 hover:border-red-600 ' +
            'hover:text-red-600 dark:border-zinc-800');
        copyButton.type = 'button';
        copyButton.title = 'Copy address';
        copyButton.setAttribute('aria-label', `Copy the address of classroom ${room.id}`);
        copyButton.append(icon('fa-regular fa-copy'));

        copyButton.addEventListener('click', async () => {
            try {
                await copyText(room.url);
                copyButton.replaceChildren(icon('fa-solid fa-check'));
                copyButton.classList.add('border-emerald-500', 'text-emerald-600');

                setTimeout(() => {
                    copyButton.replaceChildren(icon('fa-regular fa-copy'));
                    copyButton.classList.remove('border-emerald-500', 'text-emerald-600');
                }, 1400);
            } catch (error) {
                window.prompt('Copy this address:', room.url);
            }
        });

        const expandButton = el('button', 'action-button flex h-8 w-8 shrink-0 items-center justify-center ' +
            'rounded-lg border border-zinc-200 text-zinc-500 hover:border-red-600 hover:text-red-600 dark:border-zinc-800');
        expandButton.type = 'button';
        expandButton.title = 'Expand IP';
        expandButton.setAttribute('aria-label', `Show the IP address of classroom ${room.id}`);
        expandButton.append(icon('fa-solid fa-up-right-and-down-left-from-center'));
        expandButton.addEventListener('click', () => {
            elements.expandedIp.textContent = room.ip;
            elements.ipDialog.showModal();
        });

        addressRow.append(address, expandButton, copyButton);
        addressBox.append(addressRow);

        const footer = el('div', 'mt-4 flex flex-col items-stretch justify-between gap-3 sm:flex-row sm:items-center');
        const fileCount = room.files === null
            ? 'Hosted folder'
            : `${room.files} ${room.files === 1 ? 'file' : 'files'}`;
        footer.append(el('p', 'text-xs text-zinc-400 dark:text-zinc-500', fileCount));

        const openLink = el('a', 'action-button inline-flex items-center justify-center gap-2 ' +
            'w-full rounded-lg bg-red-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-red-700 sm:w-auto');
        openLink.href = room.url;
        openLink.target = '_blank';
        openLink.rel = 'noopener noreferrer';
        openLink.append(
            document.createTextNode('Open class'),
            icon('fa-solid fa-arrow-up-right-from-square text-xs')
        );
        footer.append(openLink);

        card.append(header, addressBox, footer);
        return card;
    }

    function renderRooms() {
        const rooms = Array.from(state.rooms.values()).sort((a, b) => a.id - b.id);

        elements.roomCount.textContent =
            `${rooms.length} ${rooms.length === 1 ? 'room' : 'rooms'}`;
        elements.emptyState.classList.toggle('hidden', rooms.length > 0);
        elements.roomsGrid.replaceChildren(...rooms.map(createRoomCard));
    }

    return renderRooms;
}
