@echo off
cd /d "%~dp0.."
call scripts\common.bat

java -jar %JAR_PATH% ^
  --scenario ping ^
  --url http://localhost:8080/ping

pause
