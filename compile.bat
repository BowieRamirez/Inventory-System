@echo off
echo Compiling STI Merch System...
if not exist "bin" mkdir bin
javac -d bin src\*.java
if %errorlevel% equ 0 (
    echo Compilation successful!
    echo Compiled files are in the 'bin' directory.
) else (
    echo Compilation failed!
    pause
)