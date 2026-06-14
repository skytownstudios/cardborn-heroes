#!/usr/bin/env node
/**
 * Normalize battle sprites onto a shared 1024×1024 canvas.
 * - Portraits: feet at bottom center
 * - Weapons: grip anchor at (512, 640) per rig_manifest
 */
const fs = require("fs");
const path = require("path");
const { PNG } = require("pngjs");

const ROOT = path.join(__dirname, "..", "..");
const ASSETS = path.join(ROOT, "shared", "assets");
const CANVAS = 1024;
const GRIP = { x: 512, y: 640 };
const FEET = { x: 512, y: 980 };

function isBg(r, g, b, a) {
  if (a < 8) return true;
  const lum = 0.299 * r + 0.587 * g + 0.114 * b;
  if (lum < 24 && Math.abs(r - g) < 12 && Math.abs(g - b) < 12) return true;
  if (lum > 238 && Math.abs(r - g) < 18 && Math.abs(g - b) < 28) return true;
  if (r > 245 && g > 240 && b > 225) return true;
  return false;
}

function stripBg(png) {
  for (let i = 0; i < png.width * png.height; i++) {
    const o = i * 4;
    if (isBg(png.data[o], png.data[o + 1], png.data[o + 2], png.data[o + 3])) {
      png.data[o + 3] = 0;
    }
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

function blitPixel(out, src, sx, sy, dx, dy) {
  if (sx < 0 || sy < 0 || sx >= src.width || sy >= src.height) return;
  if (dx < 0 || dy < 0 || dx >= CANVAS || dy >= CANVAS) return;
  const si = (sy * src.width + sx) * 4;
  const a = src.data[si + 3];
  if (a <= 0) return;
  const di = (dy * CANVAS + dx) * 4;
  const srcA = a / 255;
  const dstA = out.data[di + 3] / 255;
  const outA = srcA + dstA * (1 - srcA);
  if (outA <= 0) return;
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

function placeScaled(src, anchorSrcX, anchorSrcY, anchorDstX, anchorDstY, scale) {
  const out = new PNG({ width: CANVAS, height: CANVAS });
  for (let y = 0; y < src.height; y++) {
    for (let x = 0; x < src.width; x++) {
      const dx = Math.round(anchorDstX + (x - anchorSrcX) * scale);
      const dy = Math.round(anchorDstY + (y - anchorSrcY) * scale);
      blitPixel(out, src, x, y, dx, dy);
    }
  }
  return out;
}

function normalizePortrait(filePath) {
  const src = PNG.sync.read(fs.readFileSync(filePath));
  stripBg(src);
  const b = boundsOf(src);
  if (!b) return;
  const cx = (b.minX + b.maxX) / 2;
  const targetH = CANVAS * 0.9;
  const scale = targetH / (b.maxY - b.minY + 1);
  const out = placeScaled(src, cx, b.maxY, FEET.x, FEET.y, scale);
  fs.writeFileSync(filePath, PNG.sync.write(out));
  console.log("Portrait -> 1024 canvas:", path.relative(ASSETS, filePath));
}

function normalizeWeapon(filePath) {
  const src = PNG.sync.read(fs.readFileSync(filePath));
  stripBg(src);
  const b = boundsOf(src);
  if (!b) return;
  // Source art uses grip near canvas center-bottom on 1536-wide exports.
  const gripSrcX = src.width * (512 / 1024);
  const gripSrcY = src.height * (640 / 1024);
  const targetW = CANVAS * 0.92;
  const scale = targetW / src.width;
  const out = placeScaled(src, gripSrcX, gripSrcY, GRIP.x, GRIP.y, scale);
  fs.writeFileSync(filePath, PNG.sync.write(out));
  console.log("Weapon grip anchored:", path.relative(ASSETS, filePath));
}

for (const role of ["knight", "archer", "mage"]) {
  const portrait = path.join(ASSETS, "heroes", role, "portrait.png");
  if (fs.existsSync(portrait)) normalizePortrait(portrait);
}

for (const weapon of ["sword.png", "shield.png", "bow.png", "staff.png"]) {
  const p = path.join(ASSETS, "gear", weapon);
  if (fs.existsSync(p)) normalizeWeapon(p);
}

console.log("Done.");
