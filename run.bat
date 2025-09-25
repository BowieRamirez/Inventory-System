@echo off
echo Starting STI Merch System...
if not exist "bin\MerchSystem.class" (
    echo System not compiled! Please run compile.bat first.
    pause
    exit /b 1
)
java -cp bin MerchSystem
pause