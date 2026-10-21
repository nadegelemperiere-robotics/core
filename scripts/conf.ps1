# -------------------------------------------------------
# Copyright (c) [2026] MantaBots
# All rights reserved
# -------------------------------------------------------
# Merge config files and push to the Control Hub
# Usage: .\conf.ps1 <hardware> <config> [conf root]
#   <conf root>/hardware/<hardware>.xml : hub hardware configuration
#   <conf root>/hub/<config>.*.json     : code configuration files, merged into <config>.json
# -------------------------------------------------------

param(
    [string]$HwName,
    [string]$Name,
    [string]$ConfDir = (Join-Path $PSScriptRoot '..\conf')
)

$ErrorActionPreference = 'Stop'

# -- Arguments ------------------------------------------
if (-not $HwName -or -not $Name) {
    Write-Host "Usage: .\conf.ps1 <hardware> <config> [conf root]"
    Write-Host "  hardware  : hub hardware configuration name, without .xml (e.g. starter-bot)"
    Write-Host "  config    : code configuration name, without .json (e.g. mantabots)"
    Write-Host "  conf root : directory containing hub/ and hardware/ (default: ../conf)"
    exit 1
}

$HubDir      = Join-Path $ConfDir 'hub'
$HardwareDir = Join-Path $ConfDir 'hardware'

# -- Find all <n>.*.json files ---------------------------
$Files = @(Get-ChildItem -Path $HubDir -File -Filter "$Name.*.json" | Sort-Object Name)

if ($Files.Count -eq 0) {
    Write-Host "[WARN] No files matching $Name.*.json found in $HubDir"
    exit 0
}

Write-Host "Found files:"
foreach ($F in $Files) { Write-Host "   $($F.FullName)" }

# -- JSON helpers ---------------------------------------
# ConvertFrom-Json returns PSCustomObjects : convert them to ordered
# hashtables so that they can be merged like python dicts
function ConvertTo-Hashtable($Value) {
    if ($Value -is [System.Management.Automation.PSCustomObject]) {
        $Table = [ordered]@{}
        foreach ($P in $Value.PSObject.Properties) { $Table[$P.Name] = ConvertTo-Hashtable $P.Value }
        return $Table
    }
    if ($Value -is [System.Collections.IList]) {
        $List = New-Object System.Collections.ArrayList
        foreach ($Item in $Value) { [void]$List.Add((ConvertTo-Hashtable $Item)) }
        return ,$List
    }
    return $Value
}

function Test-Dict($Value) { return $Value -is [System.Collections.IDictionary] }

# Deep-merge value into base at the given key path
function Merge-Deep($Base, [string[]]$Keys, $Value) {
    if ($Keys.Count -eq 0) {
        if ((Test-Dict $Base) -and (Test-Dict $Value)) {
            foreach ($K in @($Value.Keys)) {
                if ($Base.Contains($K) -and (Test-Dict $Base[$K]) -and (Test-Dict $Value[$K])) {
                    Merge-Deep $Base[$K] @() $Value[$K]
                }
                else { $Base[$K] = $Value[$K] }
            }
        }
        return
    }
    $K = $Keys[0]
    if ($Keys.Count -eq 1) {
        if ($Base.Contains($K) -and (Test-Dict $Base[$K]) -and (Test-Dict $Value)) {
            Merge-Deep $Base[$K] @() $Value
        }
        else { $Base[$K] = $Value }
    }
    else {
        if (-not $Base.Contains($K)) { $Base[$K] = [ordered]@{} }
        Merge-Deep $Base[$K] $Keys[1..($Keys.Count - 1)] $Value
    }
}

# Key path encoded in the file name : <n>.a.b.c.json -> a, b, c
function Get-Keys($File) {
    return [string[]]($File.Name.Substring($Name.Length + 1, $File.Name.Length - $Name.Length - 6).Split('.'))
}

# -- Merge into a single JSON ---------------------------
# Files are processed shallowest-first.
# When a file's key path is a sub-path of an already-merged entry
# (e.g. example.robot.subsystems.drivetrain.follower.json extends
#  example.robot.json), its content is deep-merged into that entry
# at the remaining path rather than placed in a parallel branch.
# File name keys must exactly match the JSON keys they extend.
$Files  = $Files | Sort-Object @{ Expression = { (Get-Keys $_).Count } }, Name
$Merged = [ordered]@{}

foreach ($File in $Files) {
    $Keys = Get-Keys $File
    try {
        $Content = ConvertTo-Hashtable ((Get-Content -Raw -Encoding UTF8 $File.FullName) | ConvertFrom-Json)

        # Find the longest prefix of keys already present in merged.
        # If found, deep-merge into that existing node at the remaining path.
        $Best = 0
        for ($PrefixLen = 1; $PrefixLen -lt $Keys.Count; $PrefixLen++) {
            $Node = $Merged
            $Ok   = $true
            foreach ($K in $Keys[0..($PrefixLen - 1)]) {
                if ((Test-Dict $Node) -and $Node.Contains($K)) { $Node = $Node[$K] }
                else { $Ok = $false; break }
            }
            if ($Ok) { $Best = $PrefixLen }
        }

        if ($Best -gt 0) {
            $Node = $Merged
            foreach ($K in $Keys[0..($Best - 1)]) { $Node = $Node[$K] }
            $Remaining = if ($Best -lt $Keys.Count) { [string[]]$Keys[$Best..($Keys.Count - 1)] } else { [string[]]@() }
            Merge-Deep $Node $Remaining $Content
            $Anchor = $Keys[0..($Best - 1)] -join '.'
            $Tail   = if ($Remaining.Count -gt 0) { $Remaining -join '.' } else { '(root)' }
            Write-Host "   [OK] $($File.Name) -> merged into [$Anchor] at $Tail"
        }
        else {
            Merge-Deep $Merged $Keys $Content
            Write-Host "   [OK] $($File.Name) -> $($Keys -join '.')"
        }
    }
    catch {
        Write-Host "   [WARN] Skipping $($File.Name): $($_.Exception.Message)"
    }
}

# -- Write merged file ----------------------------------
# UTF-8 without BOM : the robot side JSON parser does not expect a BOM
$TmpFile = Join-Path $env:TEMP "$Name.json"
[System.IO.File]::WriteAllText($TmpFile, ($Merged | ConvertTo-Json -Depth 100), (New-Object System.Text.UTF8Encoding($false)))
Write-Host "[OK] Merged config written to $TmpFile"

# -- Try ADB push (non-fatal if Control Hub unreachable) -
$ErrorActionPreference = 'Continue'

$Adb = Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe'
if (-not (Test-Path $Adb)) { $Adb = 'adb' }

# Prefer a USB connected device, fall back on the Control Hub Wi-Fi address
# (with both connected, a plain "adb push" fails with "more than one device")
$Devices = @(& $Adb devices | Select-String '^(\S+)\s+device$' | ForEach-Object { $_.Matches[0].Groups[1].Value })
$Serial  = $Devices | Where-Object { $_ -notmatch ':' } | Select-Object -First 1
if (-not $Serial) {
    & $Adb connect 192.168.43.1:5555 *> $null
    $Devices = @(& $Adb devices | Select-String '^(\S+)\s+device$' | ForEach-Object { $_.Matches[0].Groups[1].Value })
    if ($Devices -contains '192.168.43.1:5555') { $Serial = '192.168.43.1:5555' }
}

if ($Serial) {
    Write-Host "Pushing $Name.json to Control Hub ($Serial)..."
    & $Adb -s $Serial push $TmpFile "/sdcard/FIRST/$Name.json"

    $HwFile = Join-Path $HardwareDir "$HwName.xml"
    if (Test-Path $HwFile) {
        & $Adb -s $Serial push $HwFile "/sdcard/FIRST/$HwName.xml"
        Write-Host "[OK] Pushed: $HwFile -> /sdcard/FIRST/$HwName.xml"
    }
    else {
        Write-Host "[WARN] No hardware file found: $HwFile"
    }
    Write-Host "Done - configuration '$Name' and hardware '$HwName' deployed to /sdcard/FIRST/"
}
else {
    Write-Host "[WARN] Control Hub not reachable - merged file kept at $TmpFile"
}
