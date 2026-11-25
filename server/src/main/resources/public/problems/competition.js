async function closeCompetition() {
    const date = await fetch('/problems/delete/' + id, {
        method: 'DELETE',
    });

    if (!response.ok) {
        let message = `error: ${response.status}`;
        try {
            const payload = await response.json();
            message = payload.error || message;
        } catch (ignored) {
            
        }
        throw new Error(message);
    }
}

export async function closeCompetition() {
    if (confirm("Are you sure you want to end the competition?")) {
        closeCompetition();
    }
}