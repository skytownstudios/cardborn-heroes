#!/usr/bin/env node
/**
 * Build rig layers from portrait — each pixel belongs to ONE part only.
 * Torso is composed last so arms/head/legs never duplicate on the body layer.
 */
const fs = require("fs");
const path = require("path");
const { PNG } = require("pngjs");

const ROOT = path.join(__dirname, "..", "..");
const ASSETS = path.join(ROOT, "shared", "assets");
const CANVAS = 1024;

/** Claim order: earlier parts own overlapping pixels. Torso is last (body minus limbs). */
const PART_ORDER = ["head", "arm_left", "arm_right", "legs", "torso"];

const KNIGHT_MASKS = {
  head: { x0: 0.3, y0: 0.02, x1: 0.7, y1: 0.3 },
  arm_left: { x0: 0.14, y0: 0.28, x1: 0.34, y1: 0.62 },
  arm_right: { x0: 0.66, y0: 0.28, x1: 0.86, y1: 0.62 },
  legs: { x0: 0.24, y0: 0.52, x1: 0.76, y1: 0.98 },
  torso: { x0: 0.1, y0: 0.26, x1: 0.9, y1: 0.56 },
};

function isBg(r, g, b, a) {
  if (a < 8) return true;
  const lum = 0.299 * r + 0.587 * g + 0.114 * b;
  if (lum < 24 && Math.abs(r - g) < 12 && Math.abs(g - b) < 12) return true;
  if (lum > 238 && Math.abs(r - g) < 18 && Math.abs(g - b) < 28) return true;
  if (r > 245 && g > 240 && b > 225) return true;
  if (Math.abs(r - g) < 8 && Math.abs(g - b) < 8 && lum > 90 && lum < 220) return true;
  return false;
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
    if (visited[i]) png.data[i * 4 + 3] = 0;
  }
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

function boundsOf(png) {
  let minX = png.width,
    minY = png.height,
    maxX = 0,
    maxY = 0;
  for (let y = 0; y < png.height; y++) {
    for (let x = 0; x < png.width; x++) {
      const a = png.data[(y * png.width + x) * 4 + 3];
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

function normalizePortraitToCanvas(src) {
  stripBg(src);
  const b = boundsOf(src);
  if (!b) throw new Error("Portrait has no opaque pixels");
  const cx = (b.minX + b.maxX) / 2;
  const targetH = CANVAS * 0.9;
  const scale = targetH / (b.maxY - b.minY + 1);
  const feetX = CANVAS / 2;
  const feetY = Math.round(CANVAS * 0.96);

  const out = new PNG({ width: CANVAS, height: CANVAS });
  for (let y = 0; y < src.height; y++) {
    for (let x = 0; x < src.width; x++) {
      const si = (y * src.width + x) * 4;
      const a = src.data[si + 3];
      if (a <= 0) continue;
      const dx = Math.round(feetX + (x - cx) * scale);
      const dy = Math.round(feetY + (y - b.maxY) * scale);
      if (dx < 0 || dy < 0 || dx >= CANVAS || dy >= CANVAS) continue;
      const di = (dy * CANVAS + dx) * 4;
      out.data[di] = src.data[si];
      out.data[di + 1] = src.data[si + 1];
      out.data[di + 2] = src.data[si + 2];
      out.data[di + 3] = src.data[si + 3];
    }
  }
  return out;
}

function rectPx(mask) {
  return {
    x0: Math.round(mask.x0 * CANVAS),
    y0: Math.round(mask.y0 * CANVAS),
    x1: Math.round(mask.x1 * CANVAS),
    y1: Math.round(mask.y1 * CANVAS),
  };
}

function inRect(x, y, r) {
  return x >= r.x0 && x < r.x1 && y >= r.y0 && y < r.y1;
}

/** Each pixel assigned to at most one part — no shared torso pixels on arm layers. */
function composeExclusive(portrait, masks) {
  const claimed = new Uint8Array(CANVAS * CANVAS);
  const layers = {};

  for (const part of PART_ORDER) {
    const r = rectPx(masks[part]);
    const out = new PNG({ width: CANVAS, height: CANVAS });
    for (let y = r.y0; y < r.y1; y++) {
      for (let x = r.x0; x < r.x1; x++) {
        const i = y * CANVAS + x;
        if (claimed[i]) continue;
        const si = i * 4;
        if (portrait.data[si + 3] <= 0) continue;
        out.data[si] = portrait.data[si];
        out.data[si + 1] = portrait.data[si + 1];
        out.data[si + 2] = portrait.data[si + 2];
        out.data[si + 3] = portrait.data[si + 3];
        claimed[i] = 1;
      }
    }
    stripBg(out);
    layers[part] = out;
  }
  return layers;
}

function composeRole(roleId, masks) {
  const portraitPath = path.join(ASSETS, "heroes", roleId, "portrait.png");
  if (!fs.existsSync(portraitPath)) {
    console.warn("Skip", roleId, "- no portrait");
    return;
  }
  const raw = loadPng(portraitPath);
  const portrait = raw.width === CANVAS && raw.height === CANVAS ? raw : normalizePortraitToCanvas(raw);
  if (raw.width !== CANVAS || raw.height !== CANVAS) {
    fs.writeFileSync(portraitPath, PNG.sync.write(portrait));
    console.log("Normalized portrait ->", CANVAS, roleId);
  }

  const rigDir = path.join(ASSETS, "heroes", roleId, "rig");
  fs.mkdirSync(rigDir, { recursive: true });
  const layers = composeExclusive(portrait, masks);

  for (const part of PART_ORDER) {
    const outPath = path.join(rigDir, `${part}.png`);
    fs.writeFileSync(outPath, PNG.sync.write(layers[part]));
    console.log("Composed (exclusive)", path.relative(ASSETS, outPath));
  }
}

function loadPng(p) {
  return PNG.sync.read(fs.readFileSync(p));
}

composeRole("knight", KNIGHT_MASKS);
console.log("Done. Run validate-rig-alignment.js to preview.");
