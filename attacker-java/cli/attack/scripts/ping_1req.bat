@echo off
cd /d "%~dp0.."
call scripts\common.bat

java -jar %JAR_PATH% ^
  --scenario ping ^
  --url %BASE_URL%/ping

pause
