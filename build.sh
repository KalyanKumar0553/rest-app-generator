#!/bin/bash
set -e

echo "Installing dependencies..."
npm install --include=dev --legacy-peer-deps

echo "Building application..."
npm run build

echo "Build completed successfully!"
echo "Output directory: www/"
