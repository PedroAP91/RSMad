# Security

## Estado actual (Basic Auth)

### Rutas actuales
- Publica: `/health`
- Publica (flujo interno Spring): `/error`
- Protegidas con autenticacion: `/api/**`
- Cualquier otra ruta: bloqueada (`denyAll`)

### Perfil `dev`
- Se usan credenciales fijas para desarrollo local:
  - usuario: `rsmad`
  - password: `rsmad`
- Estas credenciales estan definidas solo en `application-dev.yml`.

### Perfil `default` (sin perfil activo)
- `rsmad/rsmad` NO funciona.
- Motivo: en `default` no se definen `spring.security.user.name/password`, por lo que Spring Security genera credenciales en memoria diferentes (password aleatoria en arranque).

## Como probar en local (PowerShell + curl.exe)

### 1) Arrancar en perfil default
```powershell
cd backend
.\mvnw21.cmd spring-boot:run
```

#### `/health` sin auth (debe dar 200)
```powershell
curl.exe -i http://localhost:8080/health
```

#### `/api/resources` sin auth (debe dar 401)
```powershell
curl.exe -i http://localhost:8080/api/resources
```

#### `/api/resources` con `rsmad/rsmad` en default (debe dar 401 o 403, no 200)
```powershell
curl.exe -i -u rsmad:rsmad http://localhost:8080/api/resources
```

### 2) Arrancar en perfil dev
```powershell
cd backend
.\mvnw21.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

#### `/api/resources` con `rsmad/rsmad` en dev (debe dar 200)
```powershell
curl.exe -i -u rsmad:rsmad http://localhost:8080/api/resources
```

## Plan JWT (sin codigo)

### Endpoints previstos (futuro)
- `POST /api/auth/register`
- `POST /api/auth/login`
- Opcional: `POST /api/auth/refresh`

### Roles previstos
- `USER`
- `WORKER`
- `ADMIN`

### Estrategia prevista de autorizacion por rutas
- Publicas:
  - `/health`
  - `/error`
  - `/api/auth/**` (login/registro/refresh cuando exista)
- Protegidas por rol:
  - `/api/admin/**` -> `ADMIN`
  - `/api/worker/**` -> `WORKER` o `ADMIN`
  - `/api/user/**` -> `USER`, `WORKER` o `ADMIN`
- Resto de `/api/**`:
  - autenticado (sin rol especifico, segun caso de uso)

### Migracion desde Basic (dev) a JWT (default/prod)
1. Implementar capa de autenticacion JWT (`/api/auth/login`) y emision de access token.
2. Incorporar registro (`/api/auth/register`) con asignacion de rol base (`USER`).
3. Añadir filtro JWT y validacion de token en cada request a `/api/**`.
4. Definir reglas por rol en `SecurityConfig` para rutas `/api/admin/**`, `/api/worker/**`, `/api/user/**`.
5. Mantener Basic solo para `dev` durante transicion; deshabilitarlo en `default/prod`.
6. Ajustar tests de integracion para cubrir login JWT, acceso por rol y rechazos (401/403).
7. Eliminar dependencia operativa de usuario en memoria para entornos no-dev.

## TODO checklist
- [ ] Definir modelo de usuario/rol y almacenamiento.
- [ ] Crear `POST /api/auth/register`.
- [ ] Crear `POST /api/auth/login`.
- [ ] (Opcional) Crear `POST /api/auth/refresh`.
- [ ] Implementar servicio de firma/validacion JWT.
- [ ] Integrar filtro JWT en `SecurityConfig`.
- [ ] Migrar reglas de autorizacion a esquema por roles.
- [ ] Mantener Basic solo en `dev` durante rollout.
- [ ] Añadir/actualizar tests de seguridad (401/403/200 por rol).
- [ ] Desactivar Basic en `default/prod` y documentar operativa final.
