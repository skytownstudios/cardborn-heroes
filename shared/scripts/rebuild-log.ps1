param(
    [Parameter(Mandatory)][string]$Phase,
    [Parameter(Mandatory)][string]$Message
)
$root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$log = Join-Path $root "docs\REBUILD_SESSION.log"
$c = (Get-CimInstance Win32_Processor).LoadPercentage
$o = Get-CimInstance Win32_OperatingSystem
$ru = [math]::Round(($o.TotalVisibleMemorySize - $o.FreePhysicalMemory) / 1MB, 2)
$rt = [math]::Round($o.TotalVisibleMemorySize / 1MB, 2)
$df = [math]::Round((Get-PSDrive C).Free / 1GB, 2)
$ts = (Get-Date).ToUniversalTime().ToString("yyyy-MM-dd HH:mm:ss") + " UTC"
$line = "$ts | phase=$Phase | $Message | cpu=$c% ram=$ru/$rt GB disk_free=$df GB"
New-Item -ItemType Directory -Force -Path (Split-Path $log) | Out-Null
Add-Content -Path $log -Value $line -Encoding UTF8
Write-Host $line
if ($c -gt 90) { Write-Warning "CPU ${c}% high - pause before next heavy step" }
$freeRam = [math]::Round($rt - $ru, 1)
if ($freeRam -lt 2) { Write-Warning "Low RAM: ${freeRam} GB free" }
