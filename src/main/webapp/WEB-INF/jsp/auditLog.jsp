<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<%@ taglib uri="/bbNG"    prefix="bbNG"%>
<%@ taglib uri="/bbData"  prefix="bbData"%>
<%@ taglib uri="/bbUI" prefix="bbUI" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<bbNG:learningSystemPage title="Timetabling Groups" authentication="Y" entitlement="course.content.VIEW">

    <bbNG:pageHeader instructions="Timetabling Groups Audit Log">
        <bbNG:pageTitleBar title="Timetabling Groups"/>
        <bbNG:breadcrumbBar>
            <bbNG:breadcrumb title="Timetabling Groups"/>
        </bbNG:breadcrumbBar>
    </bbNG:pageHeader>

    <c:choose>
        <c:when test="${fn:length(changes) gt 0}">
            <table style="border-collapse: collapse; border: thin solid black; width: 80%;">
                <thead>
                    <tr style="border: thin solid black;">
                        <th>Activity</th>
                        <th>Username</th>
                        <th>Group</th>
                        <th>Action</th>
                        <th>Completed</th>
                        <th>Outcome</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${changes}" var="change">
                        <tr style="border: thin solid black;">
                            <td>${fn:escapeXml(change.activityName)}</td>
                            <td>${fn:escapeXml(change.username)}</td>
                            <td>${fn:escapeXml(change.groupName)}</td>
                            <td>${fn:escapeXml(change.changeType)}</td>
                            <td><c:if test="${not empty change.updateCompleted}"><fmt:formatDate value="${change.updateCompleted}" type="BOTH" dateStyle="MEDIUM" timeStyle="SHORT" /></c:if></td>
                            <td>${change.resultLabel}</td>
                        </c:forEach>
                    </tr>
                </tbody>
            </table>
        </c:when>
        <c:otherwise>
            <p>There are no changes to this course based on Timetabling data.</p>
        </c:otherwise>
    </c:choose>


</bbNG:learningSystemPage>
