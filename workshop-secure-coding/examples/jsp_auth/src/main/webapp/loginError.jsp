<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.oracle.jsc.jsp_auth.AttackLogger" %>
<% String uname = request.getParameter("j_username");
   pageContext.setAttribute("uname",uname);
%>
<html>
<head>
    <title>Login Error</title>
</head>
<body>
    <c:url var="url" value="/index.jsp"/>
    <h2>Invalid user name or password.</h2>

    <c:if test="${AttackLogger.isKnown(uname)}">
        <c:set var="knownUserLogged" value="${AttackLogger.log(uname)}"/>
    </c:if>

    <p>Please enter a user name or password that is authorized to access this 
    application. For this application, this means a user that has been created in the 
    <code>file</code> realm and has been assigned to the <em>group</em> of 
    <code>loginUser</code>.  Click here to <a href="${url}">Try Again</a></p>
</body>
</html>