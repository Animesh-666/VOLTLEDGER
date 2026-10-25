# Run before committing. Prints suspect paths only, never secret values.
$ErrorActionPreference = 'Stop'
Set-Location (Resolve-Path (Join-Path $PSScriptRoot '..'))
if (-not (Get-Command git -ErrorAction SilentlyContinue)) { throw 'Git is required.' }
$paths = @(git ls-files --cached --others --exclude-standard)
$bad = $paths | Where-Object {
    $_ -match '(?i)(firebase-adminsdk|service.account|serviceaccount|firebase.secrets|\.env($|\.)|\.mv\.db$|\.trace\.db$|(^|/|\\)secrets[/\\])' -and $_ -ne '.env.example'
}
if ($bad) {
    Write-Warning 'Files that look like secrets are present in the repository candidate list:'
    $bad | ForEach-Object { Write-Host " - $_" }
    throw 'Remove these files from Git before committing. Check gitignore and git status.'
}
Write-Host 'No suspicious secret filenames found among tracked/unignored files.' -ForegroundColor Green
