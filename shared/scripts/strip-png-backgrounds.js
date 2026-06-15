#!/usr/bin/env node
/**
 * Flood-fill near-white/cream backgrounds to transparent.
 * Normalizes pack images to 512×640 upright canvas (fit, centered).
 */
const fs = require("fs");
const path = require("path");
const { PNG } = require("pngjs");

const ROOT = path.join(__dirname, "..", "..");
const ASSETS = path.join(ROOT, "shared", "assets");

function isBg(r, g, b, a) {
  if (a < 8) return true;
  const lum = 0.299 * r + 0.587 * g + 0.114 * b;
  if (lum > 238 && Math.abs(r - g) < 18 && Math.abs(g - b) < 28) return true;
  if (r > 245 && g > 240 && b > 225) return true;
  if (r > 230 && g > 220 && b > 200 && lum > 220) return true;
  return false;
}

/** Pack art often ships on black or white — strip both for a clean silhouette. */
function isPackBg(r, g, b, a) {
  if (isBg(r, g, b, a)) return true;
  const lum = 0.299 * r + 0.587 * g + 0.114 * b;
  if (lum < 24 && Math.abs(r - g) < 12 && Math.abs(g - b) < 12) return true;
  return false;
}

function floodStrip(data, width, height, isBackground) {
  const visited = new Uint8Array(width * height);
  const queue = [];

  function push(x, y) {
    if (x < 0 || y < 0 || x >= width || y >= height) return;
    const i = y * width + x;
    if (visited[i]) return;
    const o = i * 4;
    if (!isBackground(data[o], data[o + 1], data[o + 2], data[o + 3])) return;
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

  return visited;
}

function stripFile(filePath) {
  const buf = fs.readFileSync(filePath);
  const src = PNG.sync.read(buf);
  const { width, height, data } = src;
  const visited = floodStrip(data, width, height, isBg);

  const out = new PNG({ width, height });
  for (let i = 0; i < width * height; i++) {
    const o = i * 4;
    if (visited[i]) {
      out.data[o + 3] = 0;
    } else {
      out.data[o] = data[o];
      out.data[o + 1] = data[o + 1];
      out.data[o + 2] = data[o + 2];
      out.data[o + 3] = data[o + 3];
    }
  }

  fs.writeFileSync(filePath, PNG.sync.write(out));
  return filePath;
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

function stripPackFile(filePath) {
  const buf = fs.readFileSync(filePath);
  const src = PNG.sync.read(buf);
  const { width, height, data } = src;
  const visited = floodStrip(data, width, height, isPackBg);

  const out = new PNG({ width, height });
  for (let i = 0; i < width * height; i++) {
    const o = i * 4;
    if (visited[i]) {
      out.data[o + 3] = 0;
    } else {
      out.data[o] = data[o];
      out.data[o + 1] = data[o + 1];
      out.data[o + 2] = data[o + 2];
      out.data[o + 3] = data[o + 3];
    }
  }

  fs.writeFileSync(filePath, PNG.sync.write(out));
  return filePath;
}

function normalizePack(filePath, canvasW = 512, canvasH = 640) {
  stripPackFile(filePath);
  const src = PNG.sync.read(fs.readFileSync(filePath));
  const b = boundsOf(src);
  if (!b) return;
  const cw = b.maxX - b.minX + 1;
  const ch = b.maxY - b.minY + 1;
  // Fixed height fill so every tier reads the same size in Shop rows.
  const targetH = canvasH * 0.88;
  const scale = targetH / ch;
  const dw = Math.round(cw * scale);
  const dh = Math.round(ch * scale);
  const ox = Math.round((canvasW - dw) / 2);
  const oy = Math.round((canvasH - dh) / 2);

  const out = new PNG({ width: canvasW, height: canvasH });
  for (let y = 0; y < dh; y++) {
    for (let x = 0; x < dw; x++) {
      const sx = b.minX + (x / scale) | 0;
      const sy = b.minY + (y / scale) | 0;
      const si = (sy * src.width + sx) * 4;
      const dx = ox + x;
      const dy = oy + y;
      const di = (dy * canvasW + dx) * 4;
      out.data[di] = src.data[si];
      out.data[di + 1] = src.data[si + 1];
      out.data[di + 2] = src.data[si + 2];
      out.data[di + 3] = src.data[si + 3];
    }
  }
  fs.writeFileSync(filePath, PNG.sync.write(out));
}

function walk(dir, fn) {
  if (!fs.existsSync(dir)) return;
  for (const ent of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, ent.name);
    if (ent.isDirectory()) walk(p, fn);
    else if (ent.name.endsWith(".png")) fn(p);
  }
}

const packDir = path.join(ASSETS, "ui");
if (fs.existsSync(packDir)) {
  for (const f of fs.readdirSync(packDir)) {
    if (f.startsWith("pack_ascendant_") && f.endsWith(".png")) {
      const p = path.join(packDir, f);
      console.log("Normalize pack:", f);
      normalizePack(p);
    }
  }
}

const stripDirs = [
  path.join(ASSETS, "heroes"),
  path.join(ASSETS, "gear"),
  path.join(ASSETS, "ui"),
  path.join(ASSETS, "shop"),
];

for (const dir of stripDirs) {
  walk(dir, (p) => {
    if (p.includes("pack_ascendant_")) return;
    if (p.includes(`${path.sep}rig${path.sep}`)) return;
    console.log("Strip:", path.relative(ASSETS, p));
    stripFile(p);
  });
}

const mapNodes = [
  path.join(ASSETS, "maps", "node_cleared.png"),
  path.join(ASSETS, "maps", "node_current.png"),
  path.join(ASSETS, "maps", "node_locked.png"),
];
for (const p of mapNodes) {
  if (fs.existsSync(p)) {
    console.log("Strip map node:", path.relative(ASSETS, p));
    stripFile(p);
  }
}

const mapFarmIcons = [
  "farm_goblin_hills.png",
  "farm_arcane_ruins.png",
  "farm_heroes_rest.png",
  "farm_cardborn_vault.png",
];
for (const name of mapFarmIcons) {
  const p = path.join(ASSETS, "maps", name);
  if (fs.existsSync(p)) {
    console.log("Strip farm icon:", path.join("maps", name));
    stripFile(p);
  }
}

console.log("Done.");
