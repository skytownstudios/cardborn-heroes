#!/usr/bin/env node
/**
 * Place hand-painted rig layers onto 1024×1024 at body-grid anchors.
 * Uses torso height vs portrait to pick one uniform scale for all parts.
 */
const fs = require("fs");
const path = require("path");
const { PNG } = require("pngjs");

const ROOT = path.join(__dirname, "..", "..");
const ASSETS = path.join(ROOT, "shared", "assets");
const CANVAS = 1024;

const PARTS = [
  "head",
  "torso",
  "arm_left",
  "arm_right",
  "leg_left",
  "leg_right",
];

/** Where each part anchors on the full-body grid (3/4 facing right, no cape). */
const ANCHORS = {
  head: { mode: "bottomCenter", x: 512, y: 307 },
  torso: { mode: "bottomCenter", x: 512, y: 573 },
  arm_left: { mode: "shoulder", x: 430, y: 389, inner: "left" },
  arm_right: { mode: "shoulder", x: 593, y: 389, inner: "right" },
  leg_left: { mode: "bottomCenter", x: 378, y: 1003 },
  leg_right: { mode: "bottomCenter", x: 645, y: 1003 },
};

const PORTRAIT_TORSO = { x0: 0.22, y0: 0.28, x1: 0.78, y1: 0.56 };

function isBg(r, g, b, a) {
  if (a < 8) return true;
  const lum = 0.299 * r + 0.587 * g + 0.114 * b;
  const sat = Math.max(r, g, b) - Math.min(r, g, b);
  if (sat < 40 && lum > 150) return true;
  if (lum < 24 && Math.abs(r - g) < 12 && Math.abs(g - b) < 12) return true;
  if (lum > 238 && Math.abs(r - g) < 18 && Math.abs(g - b) < 28) return true;
  if (r > 245 && g > 240 && b > 225) return true;
  if (Math.abs(r - g) < 8 && Math.abs(g - b) < 8 && lum > 90 && lum < 220) return true;
  return false;
}

function stripBg(png) {
  floodStrip(png);
  for (let i = 0; i < png.width * png.height; i++) {
    const o = i * 4;
    if (isBg(png.data[o], png.data[o + 1], png.data[o + 2], png.data[o + 3])) {
      png.data[o + 3] = 0;
    }
  }
}

function floodStrip(png) {
  const { width, height, data } = png;
  const visited = new Uint8Array(width * height);
  const queue = [];
  function push(x, y) {
    if (x < 0 || y < 0 || x >= width || y >= height) return;
    const i = y * width + x;
    if (visited[i]) return;
    const o = i * 4;
    if (!isBg(data[o], data[o + 1], data[o + 2], data[o + 3])) return;
    visited[i] = 1;
    queue.push(i);
  }
  for (let x = 0; x < width; x++) {
    push(x, 0);
    push(x, height - 1);
  }
  for (let y = 0; y < height; y++) {
    push(0, y);
    push(width - 1, y);
  }
  while (queue.length) {
    const i = queue.pop();
    const x = i % width;
    const y = (i / width) | 0;
    push(x - 1, y);
    push(x + 1, y);
    push(x, y - 1);
    push(x, y + 1);
  }
  for (let i = 0; i < width * height; i++) {
    if (visited[i]) data[i * 4 + 3] = 0;
  }
}

function boundsOf(png) {
  let minX = png.width,
    minY = png.height,
    maxX = 0,
    maxY = 0;
  for (let y = 0; y < png.height; y++) {
    for (let x = 0; x < png.width; x++) {
      const o = (y * png.width + x) * 4;
      const r = png.data[o];
      const g = png.data[o + 1];
      const b = png.data[o + 2];
      const a = png.data[o + 3];
      if (a <= 16 || isBg(r, g, b, a)) continue;
      minX = Math.min(minX, x);
      minY = Math.min(minY, y);
      maxX = Math.max(maxX, x);
      maxY = Math.max(maxY, y);
    }
  }
  if (maxX < minX) return null;
  return { minX, minY, maxX, maxY };
}

function anchorPoint(b, mode, inner) {
  const cx = (b.minX + b.maxX) / 2;
  if (mode === "bottomCenter") return { ax: cx, ay: b.maxY };
  const w = b.maxX - b.minX;
  const h = b.maxY - b.minY;
  const shoulderX =
    inner === "left"
      ? b.minX + w * 0.12
      : b.maxX - w * 0.12;
  const shoulderY = b.minY + h * 0.1;
  return { ax: shoulderX, ay: shoulderY };
}

function blitScaled(out, src, dx, dy, scale) {
  for (let y = 0; y < src.height; y++) {
    for (let x = 0; x < src.width; x++) {
      const si = (y * src.width + x) * 4;
      const a = src.data[si + 3];
      if (a <= 0) continue;
      const r = src.data[si];
      const g = src.data[si + 1];
      const b = src.data[si + 2];
      if (isBg(r, g, b, a)) continue;
      const tx = Math.round(dx + x * scale);
      const ty = Math.round(dy + y * scale);
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
}

function placePart(src, anchor, scale) {
  stripBg(src);
  const b = boundsOf(src);
  if (!b) return new PNG({ width: CANVAS, height: CANVAS });
  const { ax, ay } = anchorPoint(b, anchor.mode, anchor.inner);
  const out = new PNG({ width: CANVAS, height: CANVAS });
  const dx = anchor.x - ax * scale;
  const dy = anchor.y - ay * scale;
  blitScaled(out, src, dx, dy, scale);
  return out;
}

function portraitTorsoHeight(portraitPath) {
  const p = PNG.sync.read(fs.readFileSync(portraitPath));
  stripBg(p);
  const y0 = Math.round(PORTRAIT_TORSO.y0 * CANVAS);
  const y1 = Math.round(PORTRAIT_TORSO.y1 * CANVAS);
  let minY = CANVAS,
    maxY = 0;
  for (let y = y0; y < y1; y++) {
    for (let x = 0; x < CANVAS; x++) {
      if (p.data[(y * CANVAS + x) * 4 + 3] > 16) {
        minY = Math.min(minY, y);
        maxY = Math.max(maxY, y);
      }
    }
  }
  return maxY - minY + 1;
}

const rigDir = path.join(ASSETS, "heroes", "knight", "rig");
const portraitPath = path.join(ASSETS, "heroes", "knight", "portrait.png");
const targetTorsoH = portraitTorsoHeight(portraitPath);

const torsoSrc = PNG.sync.read(
  fs.readFileSync(path.join(rigDir, "torso.png"))
);
stripBg(torsoSrc);
const torsoB = boundsOf(torsoSrc);
if (!torsoB) throw new Error("torso has no pixels");
const torsoH = torsoB.maxY - torsoB.minY + 1;
const scale = targetTorsoH / torsoH;
console.log("Uniform scale", scale.toFixed(3), "(torso ref", targetTorsoH, "px)");

for (const part of PARTS) {
  const p = path.join(rigDir, `${part}.png`);
  if (!fs.existsSync(p)) {
    console.warn("Skip missing", part);
    continue;
  }
  const src = PNG.sync.read(fs.readFileSync(p));
  const out = placePart(src, ANCHORS[part], scale);
  fs.writeFileSync(p, PNG.sync.write(out));
  console.log("Placed", part);
}

// Remove legacy combined legs layer if present
const legacyLegs = path.join(rigDir, "legs.png");
if (fs.existsSync(legacyLegs)) {
  fs.unlinkSync(legacyLegs);
  console.log("Removed legacy legs.png");
}

console.log("Done.");
