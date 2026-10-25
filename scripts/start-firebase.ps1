# VOLTLEDGER Firestore connection, after your Firebase account and private key exist.
$ErrorActionPreference = 'Stop'
Set-Location (Resolve-Path (Join-Path $PSScriptRoot '..'))
$env:SPRING_PROFILES_ACTIVE = 'firebase'
$env:FIREBASE_PROJECT_ID = (Read-Host 'Firebase Project ID').Trim()
$env:GOOGLE_APPLICATION_CREDENTIALS = (Read-Host 'Absolute path of private service-account JSON').Trim('"').Trim()
if ([string]::IsNullOrWhiteSpace($env:FIREBASE_PROJECT_ID)) { throw 'Project ID is required.' }
if (-not (Test-Path -LiteralPath $env:GOOGLE_APPLICATION_CREDENTIALS -PathType Leaf)) {
    throw 'Credential JSON file does not exist. Check the path; never paste JSON contents.'
}
$env:VL_BOOTSTRAP_ADMIN_USERNAME = 'admin'
$secure = Read-Host 'Choose first-run admin password (12+ characters)' -AsSecureString
$env:VL_BOOTSTRAP_ADMIN_PASSWORD = [System.Net.NetworkCredential]::new('', $secure).Password
Remove-Variable secure
if ($env:VL_BOOTSTRAP_ADMIN_PASSWORD.Length -lt 12) { throw 'Admin password must contain at least 12 characters.' }
Write-Host 'Connecting VOLTLEDGER to Cloud Firestore.' -ForegroundColor Green
mvn spring-boot:run
