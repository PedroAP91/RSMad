# Frontend (Vue 3 + Vite)

## Proxy API en desarrollo

- Vite reenvia `/api` a `http://localhost:8080`.
- Esto evita problemas de CORS en local cuando backend y frontend corren por separado.

## Basic Auth (solo dev)

1. Copia el ejemplo de entorno:
```powershell
cd frontend
Copy-Item .env.development.example .env.development.local
```

## Comandos (PowerShell)

```powershell
cd frontend
npm install
npm run dev
```

## Build de produccion

```powershell
cd frontend
npm run build
npm run preview
```

El cliente API usa `fetch` y, si `VITE_BASIC_USER`/`VITE_BASIC_PASS` existen en dev, envia `Authorization: Basic ...`.

## Troubleshooting 401

- Comprueba autenticacion contra `/api/health`.
- Si aparece `No autorizado`, copia `.env.development.example` a `.env.development.local`.
- Define `VITE_BASIC_USER` y `VITE_BASIC_PASS` y reinicia `npm run dev`.
