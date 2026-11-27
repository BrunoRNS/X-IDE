#!/bin/sh
# Starts the Ollama API and downloads OLLAMA_MODEL before serving requests, so a
# freshly started server-ai container already has the configured model.
set -eu

MODEL="${OLLAMA_MODEL:-qwen2.5-coder:3b}"

ollama serve &
server_pid=$!

shutdown() {
    kill "$server_pid" 2>/dev/null || true
    wait "$server_pid" 2>/dev/null || true
}
trap shutdown TERM INT

# Wait until the local API answers before asking it to pull anything.
until ollama list >/dev/null 2>&1; do
    if ! kill -0 "$server_pid" 2>/dev/null; then
        echo "Ollama exited during startup" >&2
        exit 1
    fi
    sleep 1
done

if [ -n "$MODEL" ]; then
    printf 'Pulling Ollama model: %s\n' "$MODEL"
    if ! ollama pull "$MODEL"; then
        echo "Warning: could not pull $MODEL; the API will still start without it." >&2
    fi
fi

wait "$server_pid"
