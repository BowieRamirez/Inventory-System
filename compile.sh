#!/bin/bash
echo "Compiling STI Merch System..."
if [ ! -d "bin" ]; then
    mkdir bin
fi
javac -d bin src/*.java
if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Compiled files are in the 'bin' directory."
else
    echo "Compilation failed!"
    exit 1
fi