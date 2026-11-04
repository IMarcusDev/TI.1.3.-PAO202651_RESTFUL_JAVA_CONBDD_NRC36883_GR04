<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%--
  Movements consultation view. Rendered by MovimientosController (GET/POST /movimientos).
  Employee can type any account; Cliente has their own account fixed without input.
  Every dynamic value is escaped (c:out / fn:escapeXml).
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="activeNav" value="movimientos"/>
<c:set var="esCliente" value="${sessionScope.tipoUsuario eq 'Cliente'}"/>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Consultar movimientos | EurekaBank</title>
    <link rel="stylesheet" href="${ctx}/css/eurekabank.css">
    <link rel="stylesheet" href="${ctx}/css/menu.css">
    <link rel="stylesheet" href="${ctx}/css/operacion.css">
    <link rel="stylesheet" href="${ctx}/css/movimientos.css">
</head>
<body>
<%@ include file="partials/header.jspf" %>

<div class="app-body">
    <%@ include file="partials/sidebar.jspf" %>

    <main class="content">
        <div class="content__title">
            <h1 class="text-heading-xl">Consultar movimientos</h1>
            <p class="text-body">Ver saldo e historial de transacciones de una cuenta</p>
        </div>

        <div class="movimientos-container">
            <c:if test="${not empty error}">
                <p class="alert alert--error" role="alert"><c:out value="${error}"/></p>
            </c:if>

            <%-- Search Form --%>
            <form class="op-card" method="post" action="${ctx}/movimientos">
                <%@ include file="partials/csrf.jspf" %>
                <c:choose>
                    <c:when test="${esCliente}">
                        <div class="field">
                            <span class="field__label">Su cuenta</span>
                            <p class="text-body" style="font-size: 16px; font-weight: 600; color: var(--brand-900); margin: 4px 0 0 0;">
                                <c:out value="${sessionScope.cuenta}"/>
                            </p>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="field">
                            <label class="field__label" for="cuenta">Número de cuenta</label>
                            <input class="field__input" type="text" id="cuenta" name="cuenta" maxlength="30"
                                   value="${fn:escapeXml(cuenta)}" placeholder="Ingrese el número de cuenta"
                                   autocomplete="off" required autofocus>
                        </div>
                    </c:otherwise>
                </c:choose>

                <div class="op-actions">
                    <button class="btn btn--primary" type="submit">Consultar</button>
                </div>
            </form>

            <%-- Account details and movements table --%>
            <c:if test="${not empty historial and not empty historial.cuenta}">
                <section class="op-card" aria-labelledby="cuenta-title">
                    <h2 id="cuenta-title" class="receipt__title">Datos de la cuenta</h2>
                    <dl class="receipt">
                        <div class="receipt__row"><dt>Número de cuenta</dt><dd><c:out value="${historial.cuenta.numero}"/></dd></div>
                        <div class="receipt__row"><dt>Titular</dt><dd><c:out value="${historial.cuenta.titular}"/></dd></div>
                        <div class="receipt__row"><dt>Moneda</dt><dd><c:out value="${historial.cuenta.moneda}"/></dd></div>
                        <div class="receipt__row"><dt>Saldo</dt><dd><c:out value="${historial.cuenta.saldoFormateado}"/></dd></div>
                        <div class="receipt__row"><dt>Estado</dt><dd><c:out value="${historial.cuenta.estado}"/></dd></div>
                    </dl>
                </section>

                <section aria-labelledby="movimientos-title">
                    <h2 id="movimientos-title" class="receipt__title" style="margin-bottom: 12px;">Movimientos registrados</h2>
                    <c:choose>
                        <c:when test="${historial.sinMovimientos()}">
                            <p class="alert alert--info">La cuenta no tiene movimientos</p>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="movimientos-table">
                                    <thead>
                                        <tr>
                                            <th>N.º</th>
                                            <th>Fecha</th>
                                            <th>Tipo</th>
                                            <th>Acción</th>
                                            <th class="text-right">Importe</th>
                                            <th>Cuenta referencia</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="m" items="${historial.movimientos}">
                                            <tr>
                                                <td><c:out value="${m.numero}"/></td>
                                                <td><c:out value="${m.fecha}"/></td>
                                                <td><c:out value="${m.tipo}"/></td>
                                                <td>
                                                    <span class="${m.esIngreso ? 'movimiento--ingreso' : 'movimiento--egreso'}">
                                                        <c:out value="${m.accion}"/>
                                                    </span>
                                                </td>
                                                <td class="text-right"><c:out value="${m.importeFormateado}"/></td>
                                                <td><c:out value="${m.referencia}"/></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>

                <div class="processed">
                    <span class="processed__caption">Procesado por:</span>
                    <span class="badge" title="Servicio consumido: REST / Java">
                        <span class="badge__label">Servicio</span>
                        <span class="badge__protocol--rest">REST</span>
                        <span class="badge__platform--java">Java</span>
                    </span>
                </div>
            </c:if>
        </div>
    </main>
</div>
</body>
</html>
