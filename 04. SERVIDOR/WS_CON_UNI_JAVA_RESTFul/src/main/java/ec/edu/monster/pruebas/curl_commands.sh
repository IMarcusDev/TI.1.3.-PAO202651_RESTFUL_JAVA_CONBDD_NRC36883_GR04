# Login
curl -X POST "http://localhost:8080/WS_CON_UNI_JAVA_RESTFul/api/Login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "msosa",
    "password": "msosa"
  }'

# Movimientos
curl -X POST "http://localhost:8080/WS_CON_UNI_JAVA_RESTFul/api/Movimiento/deposito" \
  -H "Content-Type: application/json" \
  -d '{
    "token": "123",
    "cuentaOrigen": "1",
    "cuentaDestino": "123123",
    "valor": 100
  }'

curl -X POST "http://localhost:8080/WS_CON_UNI_JAVA_RESTFul/api/Movimiento/retiro" \
  -H "Content-Type: application/json" \
  -d '{
    "token": "123",
    "cuentaOrigen": "123123",
    "cuentaDestino": "123123",
    "valor": 50
  }'

curl -X POST "http://localhost:8080/WS_CON_UNI_JAVA_RESTFul/api/Movimiento/transferencia" \
  -H "Content-Type: application/json" \
  -d '{
    "token": "123",
    "cuentaOrigen": "2",
    "cuentaDestino": "123123",
    "valor": 25
  }'


