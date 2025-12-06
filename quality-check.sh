#!/bin/bash

# Quality Check Script for SGC Project

function show_help {
    echo "Usage: ./quality-check.sh [OPTION]"
    echo "Options:"
    echo "  all       Run all quality checks (Backend + Frontend)"
    echo "  backend   Run backend quality checks only"
    echo "  frontend  Run frontend quality checks only"
    echo "  fast      Run fast quality checks (Tests + Coverage only)"
    echo "  help      Show this help message"
}

if [ -z "$1" ]; then
    show_help
    exit 1
fi

case "$1" in
    all)
        echo "Running all quality checks..."
        ./gradlew qualityCheckAll
        ;;
    backend)
        echo "Running backend quality checks..."
        ./gradlew backendQualityCheck
        ;;
    frontend)
        echo "Running frontend quality checks..."
        ./gradlew frontendQualityCheck
        ;;
    fast)
        echo "Running fast quality checks..."
        ./gradlew qualityCheckFast
        ;;
    help)
        show_help
        ;;
    *)
        echo "Invalid option: $1"
        show_help
        exit 1
        ;;
esac
