$ErrorActionPreference = "Stop"

$repoRoot = if ($PSScriptRoot) { $PSScriptRoot } else { (Get-Location).Path }
$backendPath = Join-Path $repoRoot "backend"
$frontendPath = Join-Path $repoRoot "frontend"

if (-not (Test-Path -Path $backendPath -PathType Container)) {
    Write-Error "No se encontro la carpeta 'backend' en: $backendPath"
    exit 1
}

if (-not (Test-Path -Path $frontendPath -PathType Container)) {
    Write-Error "No se encontro la carpeta 'frontend' en: $frontendPath"
    exit 1
}

$backendCommand = @"
Set-Location '$backendPath'
`$env:SPRING_PROFILES_ACTIVE = 'dev'
.\mvnw21.cmd spring-boot:run
"@

$frontendCommand = @"
Set-Location '$frontendPath'
if (-not (Test-Path -Path 'node_modules' -PathType Container)) {
    npm install
}
npm run dev
"@

Start-Process powershell.exe -ArgumentList @(
    "-NoProfile",
    "-NoExit",
    "-ExecutionPolicy", "Bypass",
    "-Command", $backendCommand
)

Start-Process powershell.exe -ArgumentList @(
    "-NoProfile",
    "-NoExit",
    "-ExecutionPolicy", "Bypass",
    "-Command", $frontendCommand
)

Write-Host "Backend en http://localhost:8080"
Write-Host "Frontend normalmente en http://localhost:5173"
