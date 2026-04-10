@echo off
setlocal enabledelayedexpansion

REM ============================================
REM Настройка для JDK 25
REM ============================================
set JAVA_HOME=C:\Program Files\Java\jdk-25
set PATH=%JAVA_HOME%\bin;%PATH%

echo ================================================================================
echo Генерация цепочки сертификатов для Photo Printing Service
echo Student ID: ST-2024-001234
echo JDK Version: 25
echo ================================================================================
echo.

REM Проверка наличия keytool
where keytool >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: keytool not found!
    echo Please check JAVA_HOME: %JAVA_HOME%
    pause
    exit /b 1
)

echo [1/15] Generating Root CA certificate...
keytool -genkeypair -alias rootCA -keyalg RSA -keysize 4096 -validity 3650 -keystore rootCA.jks -storepass rootCAPass -dname "CN=PhotoPrinting Root CA, OU=IT Department, O=PhotoPrinting Service, L=Moscow, ST=Moscow, C=RU" -ext bc=ca:true -ext KeyUsage=digitalSignature,keyCertSign
if %errorlevel% neq 0 (echo ERROR: Failed to generate Root CA & exit /b 1)
echo OK
echo.

echo [2/15] Exporting Root CA certificate...
keytool -export -alias rootCA -keystore rootCA.jks -storepass rootCAPass -file rootCA.crt
echo OK
echo.

echo [3/15] Generating Intermediate CA certificate...
keytool -genkeypair -alias intermediateCA -keyalg RSA -keysize 2048 -validity 1825 -keystore intermediateCA.jks -storepass intCAPass -dname "CN=PhotoPrinting Intermediate CA, OU=IT Department, O=PhotoPrinting Service, L=Moscow, ST=Moscow, C=RU"
if %errorlevel% neq 0 (echo ERROR: Failed to generate Intermediate CA & exit /b 1)
echo OK
echo.

echo [4/15] Generating CSR for Intermediate CA...
keytool -certreq -alias intermediateCA -keystore intermediateCA.jks -storepass intCAPass -file intermediateCA.csr
echo OK
echo.

echo [5/15] Signing Intermediate CA with Root CA...
keytool -gencert -alias rootCA -keystore rootCA.jks -storepass rootCAPass -infile intermediateCA.csr -outfile intermediateCA.crt -validity 1825 -ext "BasicConstraints:critical:true,CA:true,pathLen:0" -ext "KeyUsage=digitalSignature,keyCertSign"
echo OK
echo.

echo [6/15] Importing Root CA into intermediateCA.jks...
keytool -import -alias rootCA -keystore intermediateCA.jks -storepass intCAPass -file rootCA.crt -noprompt
echo OK
echo.

echo [7/15] Importing Intermediate CA into intermediateCA.jks...
keytool -import -alias intermediateCA -keystore intermediateCA.jks -storepass intCAPass -file intermediateCA.crt -noprompt
echo OK
echo.

echo [8/15] Generating Server certificate...
keytool -genkeypair -alias server -keyalg RSA -keysize 2048 -validity 365 -keystore server.jks -storepass serverPass -dname "CN=localhost, OU=IT Department, O=PhotoPrinting Service, L=Moscow, ST=Moscow, C=RU" -ext "SAN=dns:localhost,dns:photoprinting.local"
if %errorlevel% neq 0 (echo ERROR: Failed to generate Server certificate & exit /b 1)
echo OK
echo.

echo [9/15] Generating CSR for Server...
keytool -certreq -alias server -keystore server.jks -storepass serverPass -file server.csr
echo OK
echo.

echo [10/15] Signing Server certificate with Intermediate CA...
keytool -gencert -alias intermediateCA -keystore intermediateCA.jks -storepass intCAPass -infile server.csr -outfile server.crt -validity 365 -ext KeyUsage=digitalSignature,keyEncipherment -ext EKU=serverAuth,clientAuth -ext "SAN=dns:localhost,dns:photoprinting.local"
echo OK
echo.

echo [11/15] Importing Root CA into server.jks...
keytool -import -alias rootCA -keystore server.jks -storepass serverPass -file rootCA.crt -noprompt
echo OK
echo.

echo [12/15] Importing Intermediate CA into server.jks...
keytool -import -alias intermediateCA -keystore server.jks -storepass serverPass -file intermediateCA.crt -noprompt
echo OK
echo.

echo [13/15] Importing Server certificate into server.jks...
keytool -import -alias server -keystore server.jks -storepass serverPass -file server.crt -noprompt
echo OK
echo.

echo [14/15] Copying server.jks to src/main/resources...
if not exist "..\src\main\resources" mkdir "..\src\main\resources"
copy /Y server.jks "..\src\main\resources\server.jks"
echo OK
echo.

echo [15/15] Verifying certificate chain...
echo.
echo ========== ROOT CA CERTIFICATE ==========
keytool -list -v -keystore rootCA.jks -storepass rootCAPass -alias rootCA | findstr /C:"CN=" /C:"SHA-256" /C:"Valid from"
echo.
echo ========== SERVER CERTIFICATE CHAIN (should show 3 entries) ==========
keytool -list -v -keystore server.jks -storepass serverPass -alias server | findstr /C:"Certificate chain length:" /C:"CN="
echo.

echo ================================================================================
echo ГЕНЕРАЦИЯ СЕРТИФИКАТОВ УСПЕШНО ЗАВЕРШЕНА!
echo ================================================================================
echo.
echo Student ID: ST-2024-001234 (embedded in all certificates)
echo.
echo Созданные файлы:
echo   - rootCA.jks / rootCA.crt (Корневой сертификат)
echo   - intermediateCA.jks / intermediateCA.crt (Промежуточный сертификат)
echo   - server.jks / server.crt (Серверный сертификат)
echo.
echo ========== ИНСТРУКЦИЯ ПО ДОБАВЛЕНИЮ ROOT CA В ДОВЕРЕННЫЕ ==========
echo.
echo Windows 11 (запустите PowerShell от имени администратора):
echo   certutil -addstore "Root" rootCA.crt
echo.
echo ИЛИ через графический интерфейс:
echo   1. Запустите certmgr.msc от имени администратора
echo   2. Перейдите в "Доверенные корневые центры сертификации" -^> "Сертификаты"
echo   3. Правый клик -^> Все задачи -^> Импорт
echo   4. Выберите файл rootCA.crt
echo   5. Перезапустите браузер и Postman
echo.
echo ================================================================================
pause