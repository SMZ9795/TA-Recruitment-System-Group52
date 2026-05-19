# Compile production sources (src\) into build\classes.
# test.ps1 handles compiling tests on top. UTF-8 is forced so Chinese
# paths work on Windows.
$ErrorActionPreference = "Stop"

$root = (Resolve-Path "$PSScriptRoot\..").Path
Set-Location $root

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

$classesDir = "build\classes"
if (Test-Path build) { Remove-Item -Recurse -Force build }
New-Item -ItemType Directory -Force -Path $classesDir | Out-Null

Write-Host "==> Compiling production sources (src\) ..."
$srcFiles = (Get-ChildItem -Path "src" -Recurse -Filter "*.java" |
    ForEach-Object { '"' + $_.FullName + '"' }) -join ' '
cmd /c "javac -encoding UTF-8 -d $classesDir $srcFiles"
if ($LASTEXITCODE -ne 0) { throw "src compilation failed" }

Write-Host "==> Build OK -> $classesDir"
