#!/bin/bash
echo "Starting STI Merch System..."
if [ ! -f "bin/MerchSystem.class" ]; then
    echo "System not compiled! Please run ./compile.sh first."
    exit 1
fi
java -cp bin MerchSystem