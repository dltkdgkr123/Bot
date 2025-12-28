@echo off
cd /d "%~dp0.."
call scripts\common.bat

java -jar %JAR_PATH% ^
  --scenario ping ^
  --url %BASE_URL% ^
  --threads 10 ^
  --rpt 1000 ^
  --burst false

pause
