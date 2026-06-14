#!/usr/bin/env node
const fs = require("fs");
const path = require("path");

const ROOT = path.join(__dirname, "..", "..");
const sharedContent = path.join(ROOT, "shared", "content");
const sharedAssets = path.join(ROOT, "shared", "assets");
const androidAssets = path.join(ROOT, "android", "app", "src", "main", "assets");

function copyDir(src, dest) {
  if (!fs.existsSync(src)) return;
  fs.mkdirSync(dest, { recursive: true });
  for (const ent of fs.readdirSync(src, { withFileTypes: true })) {
    const s = path.join(src, ent.name);
    const d = path.join(dest, ent.name);
    if (ent.isDirectory()) copyDir(s, d);
    else fs.copyFileSync(s, d);
  }
}

fs.mkdirSync(androidAssets, { recursive: true });
if (fs.existsSync(sharedContent)) copyDir(sharedContent, path.join(androidAssets, "content"));
if (fs.existsSync(sharedAssets)) copyDir(sharedAssets, androidAssets);
console.log("Synced shared -> android assets");
