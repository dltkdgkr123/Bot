@echo off
cd /d "%~dp0.."
call scripts\common.bat

java -jar %JAR_PATH% ^
  --scenario like_v2_massive_users ^
  --url %BASE_URL% ^
  --threads 100 ^
  --rpt 10000 ^
  --burst false

pause
