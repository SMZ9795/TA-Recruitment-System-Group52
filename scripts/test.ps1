# Compile src + tests then run the 42-test integration suite.
$ErrorActionPreference = "Stop"
$root = (Resolve-Path "$PSScriptRoot\..").Path
Set-Location $root

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

& "$PSScriptRoot\build.ps1"

$testClassesDir = "build\test-classes"
New-Item -ItemType Directory -Force -Path $testClassesDir | Out-Null

Write-Host "==> Compiling tests (tests\) ..."
$testFiles = (Get-ChildItem -Path "tests" -Recurse -Filter "*.java" |
    ForEach-Object { '"' + $_.FullName + '"' }) -join ' '
cmd /c "javac -encoding UTF-8 -cp build\classes -d $testClassesDir $testFiles"
if ($LASTEXITCODE -ne 0) { throw "test compilation failed" }

Write-Host "==> Running RecruitmentSystemTestRunner ..."
cmd /c "java -cp build\classes;$testClassesDir com.group52.tarecruitment.tests.RecruitmentSystemTestRunner"
if ($LASTEXITCODE -ne 0) { throw "tests failed" }
