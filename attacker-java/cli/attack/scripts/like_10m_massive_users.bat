@echo off
cd /d "%~dp0.."
call scripts\common.bat

java -jar %JAR_PATH% ^
  --scenario like_massive_users ^
  --url %BASE_URL% ^
  --threads 100 ^
  --rpt 1000 ^
  --burst false

pause
