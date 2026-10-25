# VOLTLEDGER local demo (no Firebase key required).
$ErrorActionPreference = 'Stop'
Set-Location (Resolve-Path (Join-Path $PSScriptRoot '..'))
$env:SPRING_PROFILES_ACTIVE = 'preview'
Remove-Item Env:GOOGLE_APPLICATION_CREDENTIALS -ErrorAction SilentlyContinue
Remove-Item Env:FIREBASE_SERVICE_ACCOUNT_JSON_BASE64 -ErrorAction SilentlyContinue
Write-Host 'VOLTLEDGER preview: data is temporary and clears on restart.' -ForegroundColor Yellow
mvn spring-boot:run
