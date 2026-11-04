<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%--
  Deposit view. Rendered by DepositoController (GET/POST /deposito).
  Request attribute "vista" selects the state:
    formulario   -> form; optional errores (field -> message), error (server message),
                    cuenta, importe (values typed by the user)
    confirmacion -> confirmation; cuenta, importe (hidden), importeFormateado
    comprobante  -> receipt; comprobante (map: operacion, empleado, cuenta, importe,
                    idMovimiento, nuevoSaldo (optional), fecha, mensaje)
  No employee code field: the RESTful server takes the employee from the token.
  Every dynamic value is escaped (c:out / fn:escapeXml).
  Design: Figma "EurekaBank - Plantillas UI", frames "Web · Transferencia (Empleado)",
  "Web · Confirmación" and "Web · Comprobante".
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="activeNav" value="deposito"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Depósito | EurekaBank</title>
    <link rel="stylesheet" href="${ctx}/css/eurekabank.css">
    <link rel="stylesheet" href="${ctx}/css/menu.css">
    <link rel="stylesheet" href="${ctx}/css/operacion.css">
</head>
<body>
<%@ include file="partials/header.jspf" %>

<div class="app-body">
    <%@ include file="partials/sidebar.jspf" %>

    <main class="content">
        <div class="content__title">
            <h1 class="text-heading-xl">Depósito</h1>
            <p class="text-body">Registrar un ingreso en una cuenta</p>
        </div>

        <c:choose>
            <%-- ===== Receipt (after Post/Redirect/Get) ===== --%>
            <c:when test="${vista eq 'comprobante'}">
                <section class="op-card" aria-labelledby="comprobante-title">
                    <p class="alert alert--success" role="status"><c:out value="${comprobante.mensaje}"/></p>
                    <h2 id="comprobante-title" class="receipt__title">Comprobante</h2>
                    <dl class="receipt">
                        <div class="receipt__row"><dt>Operación</dt><dd><c:out value="${comprobante.operacion}"/></dd></div>
                        <div class="receipt__row"><dt>Empleado</dt><dd><c:out value="${comprobante.empleado}"/></dd></div>
                        <div class="receipt__row"><dt>Cuenta destino</dt><dd><c:out value="${comprobante.cuenta}"/></dd></div>
                        <div class="receipt__row"><dt>Importe</dt><dd><c:out value="${comprobante.importe}"/></dd></div>
                        <div class="receipt__row"><dt>N.º de movimiento</dt><dd><c:out value="${comprobante.idMovimiento}"/></dd></div>
                        <c:if test="${not empty comprobante.nuevoSaldo}">
                            <div class="receipt__row"><dt>Nuevo saldo</dt><dd><c:out value="${comprobante.nuevoSaldo}"/></dd></div>
                        </c:if>
                        <div class="receipt__row"><dt>Fecha</dt><dd><c:out value="${comprobante.fecha}"/></dd></div>
                    </dl>
                    <div class="processed">
                        <span class="processed__caption">Procesado por:</span>
                        <span class="badge" title="Servicio consumido: REST / Java">
                            <span class="badge__label">Servicio</span>
                            <span class="badge__protocol--rest">REST</span>
                            <span class="badge__platform--java">Java</span>
                        </span>
                    </div>
                    <div class="op-actions">
                        <a class="btn btn--secondary" href="${ctx}/deposito">Nueva operación</a>
                        <a class="btn btn--primary" href="${ctx}/menu">Volver al menú</a>
                    </div>
                </section>
            </c:when>

            <%-- ===== Confirmation (no JavaScript: a dialog-like card) ===== --%>
            <c:when test="${vista eq 'confirmacion'}">
                <section class="op-dialog" aria-labelledby="confirmar-title">
                    <h2 id="confirmar-title" class="op-dialog__title">Confirmar depósito</h2>
                    <p class="op-dialog__text">
                        ¿Depositar <strong><c:out value="${importeFormateado}"/></strong>
                        en la cuenta <strong><c:out value="${cuenta}"/></strong>?
                    </p>
                    <%-- Hidden values are validated again by the controller --%>
                    <form class="op-form" method="post" action="${ctx}/deposito">
                        <%@ include file="partials/csrf.jspf" %>
                        <input type="hidden" name="cuenta" value="${fn:escapeXml(cuenta)}">
                        <input type="hidden" name="importe" value="${fn:escapeXml(importe)}">
                        <div class="op-actions">
                            <button class="btn btn--secondary" type="submit" name="paso" value="editar">Volver</button>
                            <button class="btn btn--primary" type="submit" name="paso" value="confirmar">Confirmar</button>
                        </div>
                    </form>
                </section>
            </c:when>

            <%-- ===== Form ===== --%>
            <c:otherwise>
                <form class="op-card" method="post" action="${ctx}/deposito">
                    <%@ include file="partials/csrf.jspf" %>
                    <c:if test="${not empty error}">
                        <p class="alert alert--error" role="alert"><c:out value="${error}"/></p>
                    </c:if>

                    <div class="field">
                        <label class="field__label" for="cuenta">Cuenta destino</label>
                        <input class="field__input${not empty errores.cuenta ? ' field__input--error' : ''}"
                               type="text" id="cuenta" name="cuenta" maxlength="30"
                               value="${fn:escapeXml(cuenta)}" placeholder="Ingrese la cuenta de destino"
                               autocomplete="off" required
                               <c:if test="${not empty errores.cuenta}">aria-invalid="true" aria-describedby="cuenta-error"</c:if>>
                        <c:if test="${not empty errores.cuenta}">
                            <span id="cuenta-error" class="field__message field__message--error"><c:out value="${errores.cuenta}"/></span>
                        </c:if>
                    </div>

                    <div class="field">
                        <label class="field__label" for="importe">Importe</label>
                        <input class="field__input${not empty errores.importe ? ' field__input--error' : ''}"
                               type="text" id="importe" name="importe" inputmode="decimal"
                               value="${fn:escapeXml(importe)}" placeholder="0.00" autocomplete="off" required
                               aria-describedby="importe-message"
                               <c:if test="${not empty errores.importe}">aria-invalid="true"</c:if>>
                        <c:choose>
                            <c:when test="${not empty errores.importe}">
                                <span id="importe-message" class="field__message field__message--error"><c:out value="${errores.importe}"/></span>
                            </c:when>
                            <c:otherwise>
                                <span id="importe-message" class="field__message">Mayor que 0, máximo 2 decimales</span>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="op-actions">
                        <a class="btn btn--secondary" href="${ctx}/menu">Cancelar</a>
                        <button class="btn btn--primary" type="submit" name="paso" value="revisar">Revisar</button>
                    </div>
                </form>
            </c:otherwise>
        </c:choose>
    </main>
</div>
</body>
</html>
