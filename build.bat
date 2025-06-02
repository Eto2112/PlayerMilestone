@echo off
echo Building PlayerMilestone...
mvn clean package
if %ERRORLEVEL% neq 0 (
    echo Build failed! Check the errors above.
) else (
    echo Build completed successfully.
)
PAUSE
EXIT /B 0