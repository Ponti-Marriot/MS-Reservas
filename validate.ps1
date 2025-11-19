# Script de validación rápida sin Maven
# Este script verifica la estructura del código Java

Write-Host "=== Validación del Proyecto MS-Reservas ===" -ForegroundColor Cyan
Write-Host ""

$projectRoot = "."
$srcPath = Join-Path $projectRoot "src\main\java\com\pontimarriot\reservas"

# Verificar que exista la estructura de directorios
Write-Host "1. Verificando estructura de directorios..." -ForegroundColor Yellow
$requiredDirs = @(
    "api\controller",
    "api\dto",
    "application\service\impl",
    "domain\model",
    "domain\enums",
    "domain\event",
    "infrastructure\config",
    "infrastructure\dbclient",
    "infrastructure\kafka",
    "infrastructure\properties"
)

$allDirsExist = $true
foreach ($dir in $requiredDirs) {
    $fullPath = Join-Path $srcPath $dir
    if (Test-Path $fullPath) {
        Write-Host "  ✓ $dir" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $dir (NO ENCONTRADO)" -ForegroundColor Red
        $allDirsExist = $false
    }
}

Write-Host ""

# Contar archivos Java
Write-Host "2. Contando archivos Java..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Path $srcPath -Recurse -Filter "*.java" -File
Write-Host "  Total de archivos .java: $($javaFiles.Count)" -ForegroundColor Cyan

Write-Host ""

# Verificar archivos clave
Write-Host "3. Verificando archivos clave..." -ForegroundColor Yellow
$keyFiles = @(
    "ReservasApplication.java",
    "domain\model\Reservation.java",
    "domain\model\Payment.java",
    "domain\model\AuditLog.java",
    "api\controller\ReservationController.java",
    "application\service\impl\ReservationServiceImpl.java"
)

foreach ($file in $keyFiles) {
    $fullPath = Join-Path $srcPath $file
    if (Test-Path $fullPath) {
        $lineCount = (Get-Content $fullPath | Measure-Object -Line).Lines
        Write-Host "  ✓ $file ($lineCount líneas)" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $file (NO ENCONTRADO)" -ForegroundColor Red
    }
}

Write-Host ""

# Verificar que todos los archivos tengan package declaration
Write-Host "4. Verificando package declarations..." -ForegroundColor Yellow
$filesWithoutPackage = @()
foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw
    if ($content -notmatch "^package com\.pontimarriot\.reservas") {
        $filesWithoutPackage += $file.Name
    }
}

if ($filesWithoutPackage.Count -eq 0) {
    Write-Host "  ✓ Todos los archivos tienen package declaration" -ForegroundColor Green
} else {
    Write-Host "  ✗ Archivos sin package declaration:" -ForegroundColor Red
    $filesWithoutPackage | ForEach-Object { Write-Host "    - $_" -ForegroundColor Red }
}

Write-Host ""

# Buscar TODOs o código incompleto
Write-Host "5. Buscando código incompleto..." -ForegroundColor Yellow
$patterns = @("// TODO", "// FIXME", "\.\.\.methodBody")
$issuesFound = $false

foreach ($pattern in $patterns) {
    $matches = Get-ChildItem -Path $srcPath -Recurse -Filter "*.java" | Select-String -Pattern $pattern
    if ($matches.Count -gt 0) {
        Write-Host "  ✗ Encontrado: $pattern" -ForegroundColor Red
        $issuesFound = $true
    }
}

if (-not $issuesFound) {
    Write-Host "  ✓ No se encontró código incompleto" -ForegroundColor Green
}

Write-Host ""

# Verificar archivos de configuración
Write-Host "6. Verificando archivos de configuración..." -ForegroundColor Yellow
$configFiles = @(
    "pom.xml",
    "src\main\resources\application.properties",
    "README.md",
    ".gitignore"
)

foreach ($file in $configFiles) {
    $fullPath = Join-Path $projectRoot $file
    if (Test-Path $fullPath) {
        Write-Host "  ✓ $file" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $file (NO ENCONTRADO)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "=== Validación Completada ===" -ForegroundColor Cyan
Write-Host ""

# Verificar si Maven está instalado
Write-Host "7. Verificando herramientas necesarias..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn -version 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ Maven instalado" -ForegroundColor Green
        Write-Host ""
        Write-Host "Puedes compilar el proyecto con:" -ForegroundColor Cyan
        Write-Host "  mvn clean compile" -ForegroundColor White
    }
} catch {
    Write-Host "  ✗ Maven NO instalado" -ForegroundColor Red
    Write-Host "    Descarga Maven de: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
}

try {
    $javaVersion = java -version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ Java instalado" -ForegroundColor Green
        $javaVersion[0]
    }
} catch {
    Write-Host "  ✗ Java NO instalado" -ForegroundColor Red
    Write-Host "    Descarga Java 17+ de: https://adoptium.net/" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Para más información, consulta TESTING.md" -ForegroundColor Cyan
