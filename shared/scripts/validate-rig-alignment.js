#!/usr/bin/env node
/** Overlay knight rig parts + sword for alignment check. Output: shared/assets/_debug/rig_preview.png */
const fs = require("fs");
const path = require("path");
const { PNG } = require("pngjs");

const ROOT = path.join(__dirname, "..", "..");
const ASSETS = path.join(ROOT, "shared", "assets");
const OUT = path.join(ASSETS, "_debug");

function loadPng(p) {
  return PNG.sync.read(fs.readFileSync(p));
}

function blit(dst, src, dx, dy) {
  for (let y = 0; y < src.height; y++) {
    for (let x = 0; x < src.width; x++) {
      const si = (y * src.width + x) * 4;
      const a = src.data[si + 3] / 255;
      if (a <= 0) continue;
      const tx = x + dx;
      const ty = y + dy;
      if (tx < 0 || ty < 0 || tx >= dst.width || ty >= dst.height) continue;
      const di = (ty * dst.width + tx) * 4;
      dst.data[di] = Math.round(src.data[si] * a + dst.data[di] * (1 - a));
      dst.data[di + 1] = Math.round(src.data[si + 1] * a + dst.data[di + 1] * (1 - a));
      dst.data[di + 2] = Math.round(src.data[si + 2] * a + dst.data[di + 2] * (1 - a));
      dst.data[di + 3] = Math.round((a + dst.data[di + 3] / 255 * (1 - a)) * 255);
    }
  }
}

function drawCrosshair(png, cx, cy, color) {
  for (let d = -12; d <= 12; d++) {
    setPx(png, cx + d, cy, color);
    setPx(png, cx, cy + d, color);
  }
}

function setPx(png, x, y, [r, g, b, a = 255]) {
  if (x < 0 || y < 0 || x >= png.width || y >= png.height) return;
  const i = (y * png.width + x) * 4;
  png.data[i] = r;
  png.data[i + 1] = g;
  png.data[i + 2] = b;
  png.data[i + 3] = a;
}

const size = 1024;
const out = new PNG({ width: size, height: size });
for (let i = 0; i < out.data.length; i += 4) {
  out.data[i] = 40;
  out.data[i + 1] = 36;
  out.data[i + 2] = 32;
  out.data[i + 3] = 255;
}

const knightRig = path.join(ASSETS, "heroes", "knight", "rig");
const parts = ["leg_left.png", "leg_right.png", "torso.png", "arm_left.png", "head.png", "arm_right.png"];
for (const part of parts) {
  const p = path.join(knightRig, part);
  if (fs.existsSync(p)) blit(out, loadPng(p), 0, 0);
}

const sword = path.join(ASSETS, "gear", "sword.png");
if (fs.existsSync(sword)) {
  const s = loadPng(sword);
  if (s.width === size && s.height === size) blit(out, s, 0, 0);
}

const shield = path.join(ASSETS, "gear", "shield.png");
if (fs.existsSync(shield)) {
  const s = loadPng(shield);
  if (s.width === size && s.height === size) blit(out, s, 0, 0);
}

drawCrosshair(out, 512, 640, [255, 0, 0, 255]);
drawCrosshair(out, Math.round(0.58 * size), Math.round(0.38 * size), [0, 255, 0, 255]);
drawCrosshair(out, Math.round(0.42 * size), Math.round(0.38 * size), [0, 128, 255, 255]);

fs.mkdirSync(OUT, { recursive: true });
const outPath = path.join(OUT, "rig_preview.png");
fs.writeFileSync(outPath, PNG.sync.write(out));
console.log("Wrote", outPath);
console.log("Red=weapon grip (512,640) Green=right arm pivot Blue=left arm pivot");
