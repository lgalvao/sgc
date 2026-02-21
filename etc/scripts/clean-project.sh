#!/bin/bash

# Stops the script if any command fails
set -e

echo "========================================="
echo "🧹 Starting Full Project Cleanup..."
echo "========================================="

# 1. Clean Project Artifacts using Git
echo "🗑️  Removing untracked files and builds (Excluding .env*, test-results, and node_modules)..."
# We prompt for safety, but allow a -y flag to skip confirmation
if [ "$1" != "-y" ]; then
    read -p "Are you sure you want to delete all untracked files and build artifacts? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Cleanup aborted."
        exit 1
    fi
fi

# Do the actual cleaning
git clean -fdx -e ".env*" -e "test-results/" -e "node_modules/" -e "frontend/node_modules/"

echo "✅ Project artifacts cleaned!"

echo ""
echo "========================================="
echo "✅ Project is beautifully clean and ready!"
echo "========================================="
