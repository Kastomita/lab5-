Get-ChildItem -Path "C:\Program Files\PostgreSQL" -Recurse -Filter "pg_dump.exe" -ErrorAction SilentlyContinue | ForEach-Object {
    Write-Host "$($_.FullName) - Version: $((& $_.FullName --version))"
}

# Использовать найденную версию 18 (если установлена)
$pgDump18 = "C:\Program Files\PostgreSQL\18\bin\pg_dump.exe"
if (Test-Path $pgDump18) {
    & $pgDump18 -U postgres -d photoprinting -h localhost > database_dump.sql
    Write-Host "Dump created with PostgreSQL 18" -ForegroundColor Green
} else {
    Write-Host "PostgreSQL 18 not found. Installing..." -ForegroundColor Yellow
    # Скачать PostgreSQL 18
    $url = "https://get.enterprisedb.com/postgresql/postgresql-18.0-1-windows-x64.exe"
    $output = "$env:TEMP\postgresql-18.exe"
    Invoke-WebRequest -Uri $url -OutFile $output
    Write-Host "Please install PostgreSQL 18 manually from: $output" -ForegroundColor Yellow
}