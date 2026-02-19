#!/bin/bash
# Script to audit access control rules

# Ensure we are at the repo root
if [ ! -f "gradlew" ]; then
    echo "Error: gradlew not found. Please run this script from the repository root."
    exit 1
fi

echo "Running Audit Access Rules Test..."
./gradlew :backend:test --tests sgc.seguranca.acesso.AuditAccessRulesTest

if [ $? -eq 0 ]; then
    echo "Audit complete. Report generated at backend/etc/scripts/access-audit-output.md"
else
    echo "Audit failed."
    exit 1
fi
