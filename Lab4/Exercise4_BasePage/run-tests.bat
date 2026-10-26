
@echo off
chcp 65001 >nul

echo ==========================================
echo   RUNNING SELENIUM TESTS - HEADLESS MODE
echo ==========================================

call mvn clean test -Dheadless=true -Dmaven.test.failure.ignore=true %*

echo.
echo ==========================================
echo   GENERATING HTML TEST REPORT
echo ==========================================

call mvn surefire-report:report-only -DlinkXRef=false

echo.
if exist "target\site\surefire-report.html" (
    echo HTML report: target\site\surefire-report.html
    start "" "target\site\surefire-report.html"
) else if exist "target\reports\surefire.html" (
    echo HTML report: target\reports\surefire.html
    start "" "target\reports\surefire.html"
) else (
    echo Report not found. Please check Maven output.
)

if exist "target\screenshots" (
    echo Screenshots folder: target\screenshots
)

echo.
echo Finished.
pause
