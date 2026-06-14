# Cardborn Heroes — Art direction (Style C locked)



## Chosen style: Western Cartoon RPG (Style C)



**Reference:** Hearthstone / western MMO hero icons — round friendly shapes, colorful armor, warm gold/cream accents, illustrated but not busy.



**Avoid:** anime, manga, chibi, tier/rank text in images, weapons baked into body rig parts.



**Tier badges & frames:** UI code only. One portrait + one rig set per hero role, shared across all tiers.



---



## Rig specification (`shared/config/rig_manifest.json`)



| Constant | Value |

|----------|-------|

| Canvas | 1024 × 1024 px, all rig parts + weapons |

| Facing | 3/4 view, facing **right** |

| Weapon grip anchor | `(512, 640)` — handle center on every 1H weapon PNG |

| Background | Fully transparent (run `strip-png-backgrounds.js`) |



### Knight rig parts (generate separately — do NOT slice from portrait)

Each part is **1024×1024** with **only that limb** drawn at the position it would occupy on a full standing knight (3/4 facing right). Stack all layers at `(0,0)` with no scaling. **No cape, no weapons** baked into rig parts.

| File | Contains |
|------|----------|
| `heroes/knight/rig/head.png` | Head + neck collar only |
| `heroes/knight/rig/torso.png` | Breastplate, belt, fauld — no arms, no cape |
| `heroes/knight/rig/arm_left.png` | Character's left arm + fist |
| `heroes/knight/rig/arm_right.png` | Character's right arm + fist (attack arm) |
| `heroes/knight/rig/leg_left.png` | Character's left leg + boot |
| `heroes/knight/rig/leg_right.png` | Character's right leg + boot |
| `heroes/knight/portrait.png` | Full hero for cards/UI |



**Weapons come from gear**, not rig: `gear/sword.png`, `gear/shield.png` (grip at anchor).



### Layer order (battle)



`leg_left → leg_right → torso → arm_left → [off-hand weapon] → head → arm_right → [main-hand weapon]`



---



## Pipeline



```powershell
powershell -File shared/scripts/install-generated-art.ps1
node shared/scripts/place-rig-layers.js   # align if AI centered parts
node shared/scripts/prepare-rig-art.js
node shared/scripts/validate-rig-alignment.js   # → shared/assets/_debug/rig_preview.png
node shared/scripts/sync.js
```



Generate flat filenames per `install-generated-art.ps1` mapping into Cursor assets folder.



---



## Obsolete



- `heroes/*/rig/weapon.png` — removed; use gear battle art

- `shared/assets/cards/` tier placeholders

