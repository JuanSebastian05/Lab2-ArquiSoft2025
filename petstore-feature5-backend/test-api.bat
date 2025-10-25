@echo off
echo ================================
echo  PRUEBAS API PETSTORE BACKEND
echo ================================

echo.
echo 1. Probando LOGIN...
echo --------------------------------
curl -X POST "http://localhost:8080/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@marketing.com\",\"password\":\"admin123\"}" ^
  -w "\nStatus: %%{http_code}\n"

echo.
echo.
echo 2. Probando ENDPOINT PROTEGIDO SIN TOKEN (debe fallar)...
echo --------------------------------
curl -X GET "http://localhost:8080/api/auth/me" ^
  -H "Content-Type: application/json" ^
  -w "\nStatus: %%{http_code}\n"

echo.
echo.
echo 3. Para probar CON TOKEN, copia el token del login anterior y ejecuta:
echo curl -X GET "http://localhost:8080/api/auth/me" -H "Authorization: Bearer TU_TOKEN_AQUI"
echo.
echo 4. Para probar VERIFICACION DE TOKEN:
echo curl -X GET "http://localhost:8080/api/auth/verify" -H "Authorization: Bearer TU_TOKEN_AQUI"
echo.
echo ================================
pause
