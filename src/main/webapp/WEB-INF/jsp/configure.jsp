<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<%@ taglib uri="/bbNG"    prefix="bbNG"%>
<%@ taglib uri="/bbData"  prefix="bbData"%>
<%@ taglib uri="/bbUI" prefix="bbUI" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<bbNG:learningSystemPage title="Timetabling Groups" authentication="Y" entitlement="course.content.VIEW">

    <bbNG:pageHeader instructions="Timetabling Groups Configuration">
        <bbNG:pageTitleBar title="Timetabling Groups"/>
        <bbNG:breadcrumbBar>
            <bbNG:breadcrumb title="Timetabling Groups"/>
        </bbNG:breadcrumbBar>
    </bbNG:pageHeader>

    <form action="" method="post">
        <p><input type="submit" name="command" value="Run Synchronisation" /></p>
    </form>

    <c:choose>
        <c:when test="${fn:length(runs) gt 0}">
            <table style="border-collapse: collapse; border: thin solid black; width: 75%;">
                <thead>
                    <tr style="border: thin solid black;">
                        <th>ID</th>
                        <th>Start</th>
                        <th>End</th>
                        <th>Result</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${runs}" var="run">
                        <tr style="border: thin solid black;">
                            <td>${fn:escapeXml(run.runId)}</td>
                            <td><fmt:formatDate value="${run.startTime}" type="BOTH" dateStyle="MEDIUM" timeStyle="SHORT" /></td>
                            <td><c:if test="${not empty run.endTime}"><fmt:formatDate value="${run.endTime}" type="BOTH" dateStyle="MEDIUM" timeStyle="SHORT" /></c:if></td>
                            <td><c:if test="${not empty run.result}">${run.result}</c:if></td>
                        </c:forEach>
                    </tr>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <p>There are no synchronisation runs to display.</p>
        </c:otherwise>
    </c:choose>

</bbNG:learningSystemPage>
