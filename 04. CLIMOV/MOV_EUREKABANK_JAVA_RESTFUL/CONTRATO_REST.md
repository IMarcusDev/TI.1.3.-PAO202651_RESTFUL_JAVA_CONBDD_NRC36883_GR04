# Contrato REST que consume el cliente móvil

El cliente móvil implementa las mismas operaciones del ERS (RNF-001) y del ECUD que el cliente SOAP (`TI.1.1/04. CLIMOV/MOV_EUREKABANK_JAVA_SOAP/CONTRATO_SOAP.md`), expuestas como recursos REST. Este documento fija las rutas, los campos del cuerpo y los campos de respuesta que lee el cliente, para que el servidor los publique igual.

- URL base: `http://<IP>:8080/WS_CON_UNI_JAVA_RESTFul/api/` (`@ApplicationPath("api")`), que se configura en `gradle.properties` (`servidor=...`).
- Las rutas usan los nombres de operación del ERS (los mismos que en SOAP). Así no chocan con las rutas anteriores del servidor (`Movimiento/deposito`, `retiro`, `transferencia` y `POST Login`), que siguen existiendo con su cuerpo original (`cuentaOrigen`, `cuentaDestino`, `valor`).
- Todas las operaciones son `POST` con cuerpo JSON (`Content-Type: application/json`). El token viaja como campo `token` dentro del cuerpo, igual que en los servidores actuales.
- Todas las respuestas llevan `estado` y `mensaje`. `estado` puede ser texto (`"OK"`/`"ERROR"`) o el número del enum (`0` = OK, `1` = ERROR), como lo serializa .NET sin `JsonStringEnumConverter`.
- El cliente lee los campos por nombre sin distinguir mayúsculas (`numeroCuenta` o `NumeroCuenta`) y sin importar el nivel de anidamiento.
- Se puede responder con cualquier código HTTP (por ejemplo 401 o 400) siempre que el cuerpo traiga `estado` y `mensaje`. Un 4xx/5xx sin ese cuerpo se muestra como "El servidor no pudo procesar la solicitud (HTTP nnn)".

## Recurso `Login`

| Ruta | Cuerpo | Campos de respuesta si `estado = OK` |
|---|---|---|
| `POST Login/loginEmpleado` | `usuario`, `clave` | `token`, `perfil` (`EMPLEADO`), `nombre` |
| `POST Login/loginCliente` | `cuenta`, `clave` | `token`, `perfil` (`CLIENTE`), `nombre` (titular) |
| `POST Login/logout` | `token` | — |

## Recurso `Movimiento`

| Ruta | Cuerpo | Campos de respuesta si `estado = OK` |
|---|---|---|
| `POST Movimiento/consultarMovimientos` | `token`, `cuenta` | `cuenta`: { `numeroCuenta`, `titular`, `moneda`, `saldo`, `estadoCuenta` }. `movimientos`: arreglo de { `numeroMovimiento`, `fecha`, `tipoMovimiento`, `accion` (`INGRESO`/`SALIDA`), `importe`, `cuentaReferencia` } |
| `POST Movimiento/registrarDeposito` | `token`, `cuenta`, `importe` | `numeroMovimiento`, `nuevoSaldo` |
| `POST Movimiento/registrarRetiro` | `token`, `cuenta`, `importe`, `clave` | `numeroMovimiento`, `nuevoSaldo` |
| `POST Movimiento/registrarTransferencia` | `token`, `cuentaOrigen`, `cuentaDestino`, `importe`, `clave` | `numeroMovimiento`, `nuevoSaldo` (de la cuenta de origen) |

Notas para el servidor:

- `importe` llega como número JSON (`120.5`); conviene recibirlo como `BigDecimal`/`decimal`. `saldo`, `importe` y `nuevoSaldo` pueden responderse como número o como texto.
- Cuando opera el cliente, `cuenta`/`cuentaOrigen` es la cuenta de su sesión y `clave` llega vacía (ECUD3 paso 6). En un depósito no se envía `clave`.
- El estado de la cuenta se llama `estadoCuenta`, no `estado`, para no confundirlo con el estado de la respuesta.
- `fecha` puede enviarse como ISO 8601 (`2026-10-31T16:40:00`) o ya formateada (`31/10/2026 16:40`).
- Si el token no es válido, el `mensaje` debe contener la palabra "Token" (ECUD4: "Token inválido o sesión expirada"). Así el cliente lo reconoce, borra la sesión y vuelve al inicio de sesión.
- Para un error de negocio, el cliente muestra el `mensaje` tal como llega ("Saldo insuficiente", "Clave de cuenta incorrecta", etc.). Si el mensaje contiene "clave", el cliente borra el campo de clave.

## Ejemplo de respuesta de `Movimiento/consultarMovimientos`

```json
{
  "estado": "OK",
  "mensaje": "Consulta de movimientos exitosa",
  "cuenta": {
    "numeroCuenta": "00100001",
    "titular": "Ana Pérez",
    "moneda": "USD",
    "saldo": 379.50,
    "estadoCuenta": "ACTIVO"
  },
  "movimientos": [
    {
      "numeroMovimiento": 1,
      "fecha": "2026-10-30T09:15:00",
      "tipoMovimiento": "Depósito",
      "accion": "INGRESO",
      "importe": 500.00,
      "cuentaReferencia": null
    }
  ]
}
```

Las pruebas de `src/main/java/ec/edu/monster/pruebas/repositorios` usan estas mismas respuestas y comprueban la ruta y el cuerpo JSON que envía el cliente.
