# Build (if needed) then launch the Swing GUI.
$ErrorActionPreference = "Stop"
$root = (Resolve-Path "$PSScriptRoot\..").Path
Set-Location $root

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

if (-not (Test-Path "build\classes\com\group52\tarecruitment")) {
    & "$PSScriptRoot\build.ps1"
}

Write-Host "==> Launching Swing GUI (SwingMain) ..."
cmd /c "java -cp build\classes com.group52.tarecruitment.SwingMain"
