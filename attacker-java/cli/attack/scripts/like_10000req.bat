@echo off
cd /d "%~dp0.."
call scripts\common.bat

java -jar %JAR_PATH% ^
  --scenario like ^
  --url %BASE_URL%/post/like ^
  --threads 10 ^
  --rpt 1000 ^
  --burst false

pause
