# Generate Gilded Ivory placeholder PNGs (Windows System.Drawing)
$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Drawing
$root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$assets = Join-Path $root "shared\assets"

function Save-Card($rel, $label, $accent) {
    $path = Join-Path $assets $rel
    New-Item -ItemType Directory -Force -Path (Split-Path $path) | Out-Null
    $bmp = New-Object System.Drawing.Bitmap 512, 512
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.Clear([System.Drawing.Color]::FromArgb(255, 245, 240, 230))
    $gold = [System.Drawing.Color]::FromArgb(255, 212, 175, 55)
    $pen = New-Object System.Drawing.Pen $gold, 6
    $g.DrawRectangle($pen, 32, 32, 416, 416)
    $brush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(180, $accent[0], $accent[1], $accent[2]))
    $g.FillRectangle($brush, 64, 64, 384, 336)
    $font = New-Object System.Drawing.Font "Arial", 24
    $textBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 59, 47, 47))
    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = [System.Drawing.StringAlignment]::Center
    $g.DrawString($label, $font, $textBrush, 256, 440, $sf)
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose(); $bmp.Dispose()
}

function Save-Icon($rel, $label) {
    $path = Join-Path $assets $rel
    New-Item -ItemType Directory -Force -Path (Split-Path $path) | Out-Null
    $bmp = New-Object System.Drawing.Bitmap 128, 128
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.Clear([System.Drawing.Color]::Transparent)
    $gold = [System.Drawing.Color]::FromArgb(255, 212, 175, 55)
    $g.FillEllipse((New-Object System.Drawing.SolidBrush $gold), 8, 8, 112, 112)
    $font = New-Object System.Drawing.Font "Arial", 14
    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = [System.Drawing.StringAlignment]::Center
    $sf.LineAlignment = [System.Drawing.StringAlignment]::Center
    $g.DrawString($label, $font, (New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255,59,47,47))), 64, 64, $sf)
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose(); $bmp.Dispose()
}

$heroes = @("knight","archer","mage")
$gear = @("sword","bow","staff")
$tierSuffix = @("_common","_uncommon","_rare","_epic","_mythic","_cardborn")
foreach ($h in $heroes) {
    Save-Card "cards\$h.png" $h.Substring(0,1).ToUpper()+$h.Substring(1) @(212,175,55)
    Save-Card "cards\${h}_battle.png" $h.Substring(0,1).ToUpper()+$h.Substring(1) @(176,141,87)
    foreach ($t in $tierSuffix) { Save-Card "cards\$h$t.png" $h @(212,175,55) }
}
foreach ($g in $gear) {
    Save-Card "cards\$g.png" $g.Substring(0,1).ToUpper()+$g.Substring(1) @(176,141,87)
    foreach ($t in $tierSuffix) { Save-Card "cards\$g$t.png" $g @(176,141,87) }
}
Save-Icon "ui\currency_crowns.png" "Cr"
Save-Icon "ui\currency_sigils.png" "Si"
Save-Icon "ui\mat_hero_essence.png" "Es"
Save-Icon "ui\mat_steel.png" "St"
Save-Icon "ui\mat_arcane_dust.png" "Du"
@("basic","common","uncommon","rare","epic","mythic") | ForEach-Object { Save-Icon "ui\pack_ascendant_$_.png" $_.Substring(0,2).ToUpper() }
Write-Host "Placeholder assets under $assets"
