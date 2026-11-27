export function relativePath(file) {
    const raw = (file.webkitRelativePath || file.name).replace(/\\/g, '/');
    const parts = raw.split('/').filter(Boolean);

    if (parts.length <= 1) return file.name;
    return parts.slice(1).join('/');
}

function readAllDirectoryEntries(reader) {
    return new Promise((resolve, reject) => {
        const entries = [];

        function readBatch() {
            reader.readEntries((batch) => {
                if (batch.length === 0) {
                    resolve(entries);
                    return;
                }

                entries.push(...batch);
                readBatch();
            }, reject);
        }

        readBatch();
    });
}

export async function collectEntry(entry, basePath = '') {
    if (entry.isFile) {
        const file = await new Promise((resolve, reject) => entry.file(resolve, reject));
        const path = basePath ? `${basePath}/${file.name}` : file.name;
        return [{ file, path }];
    }

    if (!entry.isDirectory) return [];

    const children = await readAllDirectoryEntries(entry.createReader());
    const directoryPath = basePath ? `${basePath}/${entry.name}` : '';
    const files = [];

    for (const child of children) {
        files.push(...await collectEntry(child, directoryPath));
    }

    return files;
}
