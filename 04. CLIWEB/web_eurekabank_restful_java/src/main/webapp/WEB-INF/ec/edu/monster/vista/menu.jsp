<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%--
  Menu view (home after login). Rendered by MenuController (GET /menu).
  Session attributes set by LoginController: token, username, nombre, tipoUsuario.
  Every session value is escaped (c:out / fn:escapeXml).
  Design: Figma "EurekaBank - Plantillas UI", frame "Web · Inicio (Empleado)".
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="activeNav" value="inicio"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Menú | EurekaBank</title>
    <link rel="stylesheet" href="${ctx}/css/eurekabank.css">
    <link rel="stylesheet" href="${ctx}/css/menu.css">
</head>
<body>
<%@ include file="partials/header.jspf" %>

<div class="app-body">
    <%@ include file="partials/sidebar.jspf" %>

    <main class="content">
        <div class="content__title">
            <h1 class="text-heading-xl">Bienvenido, <c:out value="${sessionScope.nombre}"/></h1>
            <p class="text-body">Seleccione una operación</p>
        </div>

        <div class="cards">
            <%-- Not available yet: rendered disabled, without link --%>
            <div class="card card--disabled" aria-disabled="true">
                <span class="card__icon" aria-hidden="true">C</span>
                <span class="card__title">Consultar movimientos</span>
                <span class="card__text">Ver saldo e historial de una cuenta</span>
                <span class="card__soon">Próximamente</span>
            </div>
            <%-- Employee-only screens (AuthFilter): a Cliente sees them disabled --%>
            <c:choose>
                <c:when test="${sessionScope.tipoUsuario eq 'Empleado'}">
                    <a class="card" href="${ctx}/deposito">
                        <span class="card__icon" aria-hidden="true">D</span>
                        <span class="card__title">Depósito</span>
                        <span class="card__text">Registrar un ingreso en una cuenta</span>
                    </a>
                    <a class="card" href="${ctx}/retiro">
                        <span class="card__icon" aria-hidden="true">R</span>
                        <span class="card__title">Retiro</span>
                        <span class="card__text">Registrar una salida de una cuenta</span>
                    </a>
                    <a class="card" href="${ctx}/transferencia">
                        <span class="card__icon" aria-hidden="true">T</span>
                        <span class="card__title">Transferencia</span>
                        <span class="card__text">Mover fondos entre dos cuentas</span>
                    </a>
                </c:when>
                <c:otherwise>
                    <div class="card card--disabled" aria-disabled="true">
                        <span class="card__icon" aria-hidden="true">D</span>
                        <span class="card__title">Depósito</span>
                        <span class="card__text">Registrar un ingreso en una cuenta</span>
                        <span class="card__soon">Próximamente</span>
                    </div>
                    <div class="card card--disabled" aria-disabled="true">
                        <span class="card__icon" aria-hidden="true">R</span>
                        <span class="card__title">Retiro</span>
                        <span class="card__text">Registrar una salida de su cuenta</span>
                        <span class="card__soon">Próximamente</span>
                    </div>
                    <div class="card card--disabled" aria-disabled="true">
                        <span class="card__icon" aria-hidden="true">T</span>
                        <span class="card__title">Transferencia</span>
                        <span class="card__text">Mover fondos desde su cuenta</span>
                        <span class="card__soon">Próximamente</span>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>

        <p class="alert alert--info">Este cliente consume el servicio RESTful desarrollado en Java.</p>
    </main>
</div>
</body>
</html>
