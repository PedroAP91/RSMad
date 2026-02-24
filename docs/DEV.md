# DEV Setup (Windows PowerShell)

## Requisitos

- Java 21
- Node.js LTS
- npm

## Backend (dev)

```powershell
cd backend
$env:SPRING_PROFILES_ACTIVE="dev"
.\mvnw21.cmd spring-boot:run
```

## Frontend

```powershell
cd frontend
Copy-Item .env.development.example .env.development.local
npm install
npm run dev
```

## Probar con curl.exe

### 1) GET /health -> 200
```powershell
curl.exe -i http://localhost:8080/health
```

### 2) GET /api/requests sin auth -> 401
```powershell
curl.exe -i http://localhost:8080/api/requests
```

### 3) GET /api/requests con Basic -> 200
```powershell
curl.exe -i -u rsmad:rsmad http://localhost:8080/api/requests
```

Nota: Vite proxy `/api` -> `http://localhost:8080`.
