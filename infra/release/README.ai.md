# X-IDE AI server __VERSION__

This bundle runs the Ollama service used by the X-IDE server:

- `server-ai`: Ollama API with `MODEL_NAME` pulled at startup

## Requirements

- Docker Engine or Docker Desktop with Compose v2.
- Network access on first run to download the configured model.

## Start

```sh
bash run.sh
```

On Windows, use `run.cmd` or `run.ps1`. The launcher loads the image archive that
matches your Docker architecture, copies `.env.example` to `.env` on first run, and
starts the service.

Edit `.env` to change `MODEL_NAME` (for example `qwen2.5-coder:1.5b` on low-memory
machines) or `OLLAMA_PORT`. The entrypoint pulls the model before reporting ready.

The API is published on `http://localhost:11434/` by default.

## Images

- `image-ai-linux-amd64.tar`
- `image-ai-linux-arm64.tar`

Load an archive manually with `docker load -i <archive>`.

## Stop

```sh
docker compose down
```

Add `-v` only when you also want to delete the downloaded model volume.
