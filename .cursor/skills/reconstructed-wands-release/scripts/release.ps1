# Collect runtime jars for a version into releases/
param(
    [Parameter(Mandatory = $true)]
    [string]$Version
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..\..\..\..")
Set-Location $root

$dest = Join-Path $root "releases"
New-Item -ItemType Directory -Force -Path $dest | Out-Null
Remove-Item (Join-Path $dest "*.jar") -Force -ErrorAction SilentlyContinue

$targets = @(
    @{ workspace = "1.20.1"; loader = "fabric";  jdk = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot" },
    @{ workspace = "1.20.1"; loader = "forge";   jdk = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot" },
    @{ workspace = "1.21.1"; loader = "fabric";  jdk = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot" },
    @{ workspace = "1.21.1"; loader = "neoforge"; jdk = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot" },
    @{ workspace = "26.2";   loader = "fabric";  jdk = $env:JAVA_HOME },
    @{ workspace = "26.2";   loader = "neoforge"; jdk = $env:JAVA_HOME }
)

foreach ($ws in @("1.20.1", "1.21.1", "26.2")) {
    $wsPath = Join-Path $root $ws
    $jdk = ($targets | Where-Object { $_.workspace -eq $ws } | Select-Object -First 1).jdk
    if ($jdk -and (Test-Path $jdk)) {
        $env:JAVA_HOME = $jdk
        $env:PATH = "$jdk\bin;$env:PATH"
    }
    Write-Host "Building $ws with JAVA_HOME=$env:JAVA_HOME"
    Push-Location $wsPath
    & .\gradlew.bat build --no-daemon
    if ($LASTEXITCODE -ne 0) { Pop-Location; throw "Build failed for $ws" }
    Pop-Location
}

foreach ($t in $targets) {
    $libs = Join-Path $root (Join-Path $t.workspace "$($t.loader)\build\libs")
    $jar = Get-ChildItem $libs -Filter "*-$Version.jar" -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notmatch 'sources|javadoc' } |
        Select-Object -First 1
    if (-not $jar) { throw "Missing jar for $($t.workspace)/$($t.loader) version $Version" }
    Copy-Item $jar.FullName (Join-Path $dest $jar.Name) -Force
    Write-Host "Copied $($jar.Name)"
}

Write-Host "Release artifacts in $dest"
Get-ChildItem $dest -Filter "*.jar" | Format-Table Name, Length -AutoSize
