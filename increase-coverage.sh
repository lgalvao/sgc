#!/bin/bash
set -e

echo "Starting coverage collection..."
cd frontend
npm run coverage:unit:collect

echo "Running coverage audit..."
cd ..
node etc/scripts/sgc.js frontend cobertura auditoria
