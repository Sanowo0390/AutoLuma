# Builds AutoLuma from source using JDK 25 and the official Gradle distribution.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Push-Location $root
try {
    if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
        throw 'Java 25 (JDK) が必要です。JAVA_HOME と PATH を設定してください。'
    }
    $javaText = (& java -version 2>&1 | Out-String)
    if ($LASTEXITCODE -ne 0 -or $javaText -notmatch '(?:version "25(?:\.|\+|")|openjdk 25(?:\.|\+|\s))') {
        throw "このプロジェクトにはJava 25が必要です。現在のJava: $javaText"
    }

    $v = '9.7.1'
    $cache = Join-Path $root '.gradle-bootstrap'
    $home = Join-Path $cache "gradle-$v"
    $bat = Join-Path $home 'bin\gradle.bat'
    if (-not (Test-Path $bat)) {
        New-Item -ItemType Directory -Force -Path $cache | Out-Null
        $zip = Join-Path $cache "gradle-$v-bin.zip"
        $uri = "https://services.gradle.org/distributions/gradle-$v-bin.zip"
        Write-Host "Gradle $v を公式サイトからダウンロードしています..."
        Invoke-WebRequest $uri -OutFile $zip
        $expected = (Invoke-WebRequest "$uri.sha256").Content.ToString().Trim().Split(' ')[0].ToLowerInvariant()
        $actual = (Get-FileHash -Algorithm SHA256 -Path $zip).Hash.ToLowerInvariant()
        if ($actual -ne $expected) {
            Remove-Item $zip -Force
            throw 'Gradle ZIPのSHA-256検証に失敗しました。'
        }
        Expand-Archive -Path $zip -DestinationPath $cache -Force
        Remove-Item $zip -Force
    }

    & $bat --no-daemon build
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed (exit: $LASTEXITCODE)"
    }
    Write-Host 'ビルド完了: build\libs\autoluma-1.0.0.jar'
} finally {
    Pop-Location
}
