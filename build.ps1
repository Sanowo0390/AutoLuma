# Builds AutoLuma with JDK 25 and a SHA-256-verified official Gradle release.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Push-Location $root
try {
    if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
        throw 'Java 25 JDK is required.'
    }
    $javaText = (& java -version 2>&1 | Out-String)
    if ($LASTEXITCODE -ne 0 -or $javaText -notmatch '(?:version "25(?:\.|\+|")|openjdk 25(?:\.|\+|\s))') {
        throw "Java 25 is required. Found: $javaText"
    }
    $v = '9.7.1'
    $cache = Join-Path $root '.gradle-bootstrap'
    $home = Join-Path $cache "gradle-$v"
    $bat = Join-Path $home 'bin\gradle.bat'
    if (-not (Test-Path $bat)) {
        New-Item -ItemType Directory -Force -Path $cache | Out-Null
        $zip = Join-Path $cache "gradle-$v-bin.zip"
        $uri = "https://services.gradle.org/distributions/gradle-$v-bin.zip"
        Invoke-WebRequest $uri -OutFile $zip
        $expected = (Invoke-WebRequest "$uri.sha256").Content.ToString().Trim().Split(' ')[0].ToLowerInvariant()
        $actual = (Get-FileHash -Algorithm SHA256 -Path $zip).Hash.ToLowerInvariant()
        if ($actual -ne $expected) { Remove-Item $zip -Force; throw 'Gradle checksum mismatch' }
        Expand-Archive -Path $zip -DestinationPath $cache -Force
        Remove-Item $zip -Force
    }
    & $bat --no-daemon build
    if ($LASTEXITCODE -ne 0) { throw "Gradle build failed: $LASTEXITCODE" }
    Write-Host 'Build complete: build\libs\autoluma-1.1.2.jar'
} finally { Pop-Location }
