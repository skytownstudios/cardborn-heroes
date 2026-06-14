# Copy AI-generated PNGs from Cursor assets into shared/assets/
param(
    [string]$Source = "$env:USERPROFILE\.cursor\projects\c-Users-colan-Desktop-SkyTown-Studios-phone-emulator\assets",
    [string]$Dest = (Join-Path (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent) "shared\assets")
)

$ErrorActionPreference = "Stop"
if (-not (Test-Path $Source)) {
    Write-Error "Source not found: $Source"
}

# Map flat generated filenames -> monorepo paths
$map = @{
    "knight_portrait.png"       = "heroes\knight\portrait.png"
    "archer_portrait.png"       = "heroes\archer\portrait.png"
    "mage_portrait.png"         = "heroes\mage\portrait.png"
    "knight_leg_left.png"       = "heroes\knight\rig\leg_left.png"
    "knight_leg_right.png"      = "heroes\knight\rig\leg_right.png"
    "knight_torso.png"          = "heroes\knight\rig\torso.png"
    "knight_arm_left.png"       = "heroes\knight\rig\arm_left.png"
    "knight_arm_right.png"      = "heroes\knight\rig\arm_right.png"
    "knight_head.png"           = "heroes\knight\rig\head.png"
    "shield.png"                = "gear\shield.png"
    "archer_legs.png"           = "heroes\archer\rig\legs.png"
    "archer_torso.png"          = "heroes\archer\rig\torso.png"
    "archer_arm_left.png"       = "heroes\archer\rig\arm_left.png"
    "archer_arm_right.png"      = "heroes\archer\rig\arm_right.png"
    "archer_head.png"           = "heroes\archer\rig\head.png"
    "archer_weapon.png"         = "heroes\archer\rig\weapon.png"
    "mage_legs.png"             = "heroes\mage\rig\legs.png"
    "mage_torso.png"            = "heroes\mage\rig\torso.png"
    "mage_arm_left.png"         = "heroes\mage\rig\arm_left.png"
    "mage_arm_right.png"        = "heroes\mage\rig\arm_right.png"
    "mage_head.png"             = "heroes\mage\rig\head.png"
    "mage_weapon.png"           = "heroes\mage\rig\weapon.png"
    "sword.png"                 = "gear\sword.png"
    "bow.png"                   = "gear\bow.png"
    "staff.png"                 = "gear\staff.png"
    "currency_crowns.png"       = "ui\currency_crowns.png"
    "currency_sigils.png"       = "ui\currency_sigils.png"
    "mat_hero_essence.png"      = "ui\mat_hero_essence.png"
    "mat_steel.png"             = "ui\mat_steel.png"
    "mat_arcane_dust.png"       = "ui\mat_arcane_dust.png"
    "pack_ascendant_basic.png"  = "ui\pack_ascendant_basic.png"
    "pack_ascendant_common.png" = "ui\pack_ascendant_common.png"
    "pack_ascendant_uncommon.png" = "ui\pack_ascendant_uncommon.png"
    "pack_ascendant_rare.png"   = "ui\pack_ascendant_rare.png"
    "pack_ascendant_epic.png"   = "ui\pack_ascendant_epic.png"
    "pack_ascendant_mythic.png" = "ui\pack_ascendant_mythic.png"
    "tab_battle.png"            = "ui\tab_battle.png"
    "tab_hand.png"              = "ui\tab_hand.png"
    "tab_cards.png"             = "ui\tab_cards.png"
    "tab_forge.png"             = "ui\tab_forge.png"
    "tab_shop.png"              = "ui\tab_shop.png"
    "icon_quests.png"           = "ui\icon_quests.png"
    "icon_bag.png"              = "ui\icon_bag.png"
    "icon_profile.png"          = "ui\icon_profile.png"
    "hand_slot_empty.png"       = "ui\hand_slot_empty.png"
    "whispering_woods.png"      = "maps\whispering_woods.png"
    "node_locked.png"           = "maps\node_locked.png"
    "node_current.png"          = "maps\node_current.png"
    "node_cleared.png"          = "maps\node_cleared.png"
    "farm_goblin_hills.png"     = "maps\farm_goblin_hills.png"
    "farm_arcane_ruins.png"     = "maps\farm_arcane_ruins.png"
    "farm_heroes_rest.png"      = "maps\farm_heroes_rest.png"
    "farm_cardborn_vault.png"   = "maps\farm_cardborn_vault.png"
    "arena_whispering_woods.png" = "battle\arena_whispering_woods.png"
    "pack_card_back.png"        = "shop\pack_card_back.png"
    "recipe_frame.png"          = "forge\recipe_frame.png"
}

$copied = 0
foreach ($entry in $map.GetEnumerator()) {
    $src = Join-Path $Source $entry.Key
    if (-not (Test-Path $src)) { continue }
    $out = Join-Path $Dest $entry.Value
    New-Item -ItemType Directory -Force -Path (Split-Path $out) | Out-Null
    Copy-Item $src $out -Force
    Write-Host "Installed $($entry.Value)"
    $copied++
}

Write-Host "Installed $copied asset(s) -> $Dest"
