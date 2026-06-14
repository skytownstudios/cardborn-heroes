#!/usr/bin/env node
/**
 * Verify rig PNGs are exactly canvasSize×canvasSize. Strip bg only — never reposition.
 * Fails if any rig part is wrong dimensions.
 */
const fs = require("fs");
const path = require("path");
const { PNG } = require("pngjs");

const ROOT = path.join(__dirname, "..", "..");
const ASSETS = path.join(ROOT, "shared", "assets");
const CONFIG = JSON.parse(
  fs.readFileSync(path.join(ROOT, "shared", "config", "rig_manifest.json"), "utf8")
);
const CANVAS = CONFIG.canvasSize || 1024;

function isBg(r, g, b, a) {
  if (a < 8) return true;
  const lum = 0.299 * r + 0.587 * g + 0.114 * b;
  if (lum < 24 && Math.abs(r - g) < 12 && Math.abs(g - b) < 12) return true;
  if (lum > 238 && Math.abs(r - g) < 18 && Math.abs(g - b) < 28) return true;
  if (r > 245 && g > 240 && b > 225) return true;
  if (Math.abs(r - g) < 8 && Math.abs(g - b) < 8 && lum > 90 && lum < 220) return true;
  return false;
}

function stripBgOnly(filePath) {
  const src = PNG.sync.read(fs.readFileSync(filePath));
  floodStrip(src);
  for (let i = 0; i < src.width * src.height; i++) {
    const o = i * 4;
    if (isBg(src.data[o], src.data[o + 1], src.data[o + 2], src.data[o + 3])) {
      src.data[o + 3] = 0;
    }
  }
  fs.writeFileSync(filePath, PNG.sync.write(src));
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

function assertCanvas(filePath) {
  const png = PNG.sync.read(fs.readFileSync(filePath));
  if (png.width !== CANVAS || png.height !== CANVAS) {
    throw new Error(
      `${path.relative(ASSETS, filePath)} is ${png.width}x${png.height}, expected ${CANVAS}x${CANVAS}`
    );
  }
}

const knightRig = path.join(ASSETS, "heroes", "knight", "rig");
const parts = ["leg_left.png", "leg_right.png", "torso.png", "arm_left.png", "head.png", "arm_right.png"];
for (const part of parts) {
  const p = path.join(knightRig, part);
  if (!fs.existsSync(p)) throw new Error("Missing " + p);
  stripBgOnly(p);
  assertCanvas(p);
  console.log("OK", path.relative(ASSETS, p));
}

for (const w of ["sword.png", "shield.png"]) {
  const p = path.join(ASSETS, "gear", w);
  if (!fs.existsSync(p)) continue;
  stripBgOnly(p);
  const png = PNG.sync.read(fs.readFileSync(p));
  if (png.width !== CANVAS || png.height !== CANVAS) {
    console.warn(
      "WARN",
      path.relative(ASSETS, p),
      `${png.width}x${png.height} (expected ${CANVAS}x${CANVAS}) — regenerate or normalize separately`
    );
    continue;
  }
  console.log("OK", path.relative(ASSETS, p));
}

console.log("All rig layers are", CANVAS + "x" + CANVAS);
