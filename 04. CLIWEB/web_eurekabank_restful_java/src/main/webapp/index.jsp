<%-- session="false": no session is created here, so the redirect URL
     never carries a ;jsessionid=... (session ids must not appear in URLs) --%>
<%@ page pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Welcome page: the application starts at the login screen.
     c:redirect adds the context path to "/login" automatically. --%>
<c:redirect url="/login"/>
