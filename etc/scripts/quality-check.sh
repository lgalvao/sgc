#!/bin/bash
set -e

function show_help {
    echo "Usage: ./quality-check.sh [OPTION]"
    echo "Options:"
    echo "  all       Run all quality checks (Backend + Frontend)"
    echo "  backend   Run backend quality checks only"
    echo "  frontend  Run frontend quality checks only"
    echo "  fast      Run fast quality checks (Tests + Coverage only)"
    echo "  help      Show this help message"
}

ACTION=$1

if [ -z "$ACTION" ]; then
    show_help
    exit 1
fi

GRADLEW="./gradlew"

if [ ! -f "$GRADLEW" ]; then
    echo "Error: $GRADLEW not found in the current directory."
    exit 1
fi

case $ACTION in
    all)
        echo "Running all quality checks..."
        $GRADLEW qualityCheckAll
        ;;
    backend)
        echo "Running backend quality checks..."
        $GRADLEW backendQualityCheck
        ;;
    frontend)
        echo "Running frontend quality checks..."
        $GRADLEW frontendQualityCheck
        ;;
    fast)
        echo "Running fast quality checks..."
        $GRADLEW qualityCheckFast
        ;;
    help)
        show_help
        ;;
    *)
        echo "Invalid option: $ACTION"
        show_help
        exit 1
        ;;
esac
