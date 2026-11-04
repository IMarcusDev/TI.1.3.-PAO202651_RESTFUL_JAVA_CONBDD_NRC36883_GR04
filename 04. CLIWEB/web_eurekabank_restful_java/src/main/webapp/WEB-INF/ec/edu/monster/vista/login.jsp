<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%--
  Login view. Rendered by LoginController (GET/POST /login).
  Request attributes set by the controller after a failed login:
    error    -> message to show
    username -> value typed by the user (the password is never sent back)
  Request attribute set on GET /login?expirada=1 (session ended by the server):
    aviso    -> fixed notice asking to log in again
  Design: Figma "EurekaBank - Plantillas UI", frames "Web · Login" / "Web · Login (error)".
--%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Iniciar sesión | EurekaBank</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/eurekabank.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
</head>
<body>
<main class="login-page">
    <section class="login-card" aria-labelledby="login-title">

        <div class="login-card__top">
            <span class="logo"><span class="logo__mark">EB</span>EurekaBank</span>
            <%-- Service badge: this client consumes the RESTful service written in Java --%>
            <span class="badge" title="Servicio consumido: REST / Java">
                <span class="badge__label">Servicio</span>
                <span class="badge__protocol--rest">REST</span>
                <span class="badge__platform--java">Java</span>
            </span>
        </div>

        <div class="login-card__title">
            <h1 id="login-title" class="text-heading-xl">Iniciar sesión</h1>
            <p class="text-body">Ingrese sus credenciales para continuar</p>
        </div>

        <c:if test="${not empty aviso}">
            <p class="alert alert--info" role="status"><c:out value="${aviso}"/></p>
        </c:if>

        <c:if test="${not empty error}">
            <p class="alert alert--error" role="alert"><c:out value="${error}"/></p>
        </c:if>

        <form class="login-form" method="post" action="${pageContext.request.contextPath}/login">
            <%@ include file="partials/csrf.jspf" %>
            <div class="field">
                <span class="field__label">Tipo de acceso</span>
                <div style="display: flex; gap: 20px; align-items: center; padding: 4px 0;">
                    <label style="display: flex; align-items: center; gap: 6px; cursor: pointer;">
                        <input type="radio" name="tipo" value="empleado" ${empty tipo or tipo eq 'empleado' ? 'checked' : ''} onchange="actualizarTipo('empleado')">
                        <span>Empleado</span>
                    </label>
                    <label style="display: flex; align-items: center; gap: 6px; cursor: pointer;">
                        <input type="radio" name="tipo" value="cliente" ${tipo eq 'cliente' ? 'checked' : ''} onchange="actualizarTipo('cliente')">
                        <span>Cliente</span>
                    </label>
                </div>
            </div>

            <div class="field">
                <label id="username-label" class="field__label" for="username"><c:choose><c:when test="${tipo eq 'cliente'}">Número de cuenta</c:when><c:otherwise>Usuario</c:otherwise></c:choose></label>
                <input class="field__input" type="text" id="username" name="username"
                       value="${fn:escapeXml(username)}"
                       placeholder="${tipo eq 'cliente' ? 'Ingrese su número de cuenta' : 'Ingrese su usuario'}"
                       autocomplete="username" required autofocus>
            </div>

            <div class="field">
                <label class="field__label" for="password">Clave</label>
                <input class="field__input" type="password" id="password" name="password"
                       placeholder="Ingrese su clave" autocomplete="current-password" required>
            </div>

            <button class="btn btn--primary btn--block" type="submit">Ingresar</button>
        </form>

        <script>
            function actualizarTipo(tipo) {
                var label = document.getElementById('username-label');
                var input = document.getElementById('username');
                if (tipo === 'cliente') {
                    if (label) label.textContent = 'Número de cuenta';
                    if (input) input.placeholder = 'Ingrese su número de cuenta';
                } else {
                    if (label) label.textContent = 'Usuario';
                    if (input) input.placeholder = 'Ingrese su usuario';
                }
            }
        </script>

    </section>
</main>
</body>
</html>
