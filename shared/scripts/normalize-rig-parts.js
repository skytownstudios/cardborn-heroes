#!/usr/bin/env node
/**
 * Place knight rig slices on a shared 1024×1024 canvas so layers stack into one figure.
 * Each source PNG is an isolated part centered in its file — this script anchors them
 * to body joints on a common canvas (feet, waist, neck, shoulders).
 */
const fs = require("fs");
const path = require("path");
const { PNG } = require("pngjs");

const ROOT = path.join(__dirname, "..", "..");
const ASSETS = path.join(ROOT, "shared", "assets");
const CANVAS = 1024;

/** @typedef {{ mode: 'bottomCenter'|'shoulderPivot', x: number, y: number }} Placement */

/** Canvas anchor targets for 3/4 knight facing right. Tune via rig_preview.png */
const KNIGHT_PLACEMENTS = /** @type {Record<string, Placement>} */ ({
  legs: { mode: "bottomCenter", x: 512, y: 972 },
  torso: { mode: "bottomCenter", x: 512, y: 708 },
  head: { mode: "bottomCenter", x: 512, y: 452 },
  arm_left: { mode: "shoulderPivot", x: 418, y: 392 },
  arm_right: { mode: "shoulderPivot", x: 608, y: 392 },
});

function isBg(r, g, b, a) {
  if (a < 8) return true;
  const lum = 0.299 * r + 0.587 * g + 0.114 * b;
  if (lum < 24 && Math.abs(r - g) < 12 && Math.abs(g - b) < 12) return true;
  if (lum > 238 && Math.abs(r - g) < 18 && Math.abs(g - b) < 28) return true;
  if (r > 245 && g > 240 && b > 225) return true;
  return false;
}

function stripBlack(png) {
  for (let i = 0; i < png.width * png.height; i++) {
    const o = i * 4;
    const r = png.data[o];
    const g = png.data[o + 1];
    const b = png.data[o + 2];
    const a = png.data[o + 3];
    if (isBg(r, g, b, a)) png.data[o + 3] = 0;
  }
}

function boundsOf(png) {
  const { width, height, data } = png;
  let minX = width,
    minY = height,
    maxX = 0,
    maxY = 0;
  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const a = data[(y * width + x) * 4 + 3];
      if (a > 16) {
        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);
      }
    }
  }
  if (maxX < minX) return null;
  return { minX, minY, maxX, maxY };
}

function anchorOnPart(b, mode) {
  const cx = (b.minX + b.maxX) / 2;
  if (mode === "bottomCenter") return { ax: cx, ay: b.maxY };
  // Shoulder: upper-inner corner for 3/4 figure facing right.
  const shoulderX = b.maxX - (b.maxX - b.minX) * 0.22;
  const shoulderY = b.minY + (b.maxY - b.minY) * 0.08;
  return { ax: shoulderX, ay: shoulderY };
}

function placePart(src, placement) {
  stripBlack(src);
  const b = boundsOf(src);
  if (!b) return null;

  const { ax, ay } = anchorOnPart(b, placement.mode);
  const dx = Math.round(placement.x - ax);
  const dy = Math.round(placement.y - ay);

  const out = new PNG({ width: CANVAS, height: CANVAS });
  for (let y = 0; y < src.height; y++) {
    for (let x = 0; x < src.width; x++) {
      const si = (y * src.width + x) * 4;
      const a = src.data[si + 3];
      if (a <= 0) continue;
      const tx = x + dx;
      const ty = y + dy;
      if (tx < 0 || ty < 0 || tx >= CANVAS || ty >= CANVAS) continue;
      const di = (ty * CANVAS + tx) * 4;
      const srcA = a / 255;
      const dstA = out.data[di + 3] / 255;
      const outA = srcA + dstA * (1 - srcA);
      if (outA <= 0) continue;
      out.data[di] = Math.round(
        (src.data[si] * srcA + out.data[di] * dstA * (1 - srcA)) / outA
      );
      out.data[di + 1] = Math.round(
        (src.data[si + 1] * srcA + out.data[di + 1] * dstA * (1 - srcA)) / outA
      );
      out.data[di + 2] = Math.round(
        (src.data[si + 2] * srcA + out.data[di + 2] * dstA * (1 - srcA)) / outA
      );
      out.data[di + 3] = Math.round(outA * 255);
    }
  }
  return out;
}

function normalizeRole(roleId, placements) {
  const rigDir = path.join(ASSETS, "heroes", roleId, "rig");
  if (!fs.existsSync(rigDir)) {
    console.warn("Skip missing rig:", roleId);
    return;
  }
  for (const [part, placement] of Object.entries(placements)) {
    const file = path.join(rigDir, `${part}.png`);
    if (!fs.existsSync(file)) {
      console.warn("Missing", file);
      continue;
    }
    const src = PNG.sync.read(fs.readFileSync(file));
    const out = placePart(src, placement);
    if (!out) {
      console.warn("Empty part:", file);
      continue;
    }
    fs.writeFileSync(file, PNG.sync.write(out));
    console.log("Normalized", path.relative(ASSETS, file));
  }
}

normalizeRole("knight", KNIGHT_PLACEMENTS);
console.log("Done. Run validate-rig-alignment.js to preview.");
