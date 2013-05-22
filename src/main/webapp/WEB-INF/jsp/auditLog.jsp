<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<%@ taglib uri="/bbNG"    prefix="bbNG"%>
<%@ taglib uri="/bbData"  prefix="bbData"%>
<%@ taglib uri="/bbUI" prefix="bbUI" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<bbNG:learningSystemPage title="Timetabling Groups" authentication="Y" entitlement="course.content.VIEW">

 <bbNG:pageHeader instructions="Timetabling Groups Audit Log">
   <bbNG:pageTitleBar title="Timetabling Groups"/>
   <bbNG:breadcrumbBar>
     <bbNG:breadcrumb title="Timetabling Groups"/>
   </bbNG:breadcrumbBar>
  </bbNG:pageHeader>
 
    
    <table>
        <thead>
            <tr>
                <th>Username</th>
                <th>Activity</th>
                <th>Group</th>
                <th>Action</th>
                <th>Completed</th>
                <th>Outcome</th>
            </tr>
        </thead>
        <tbody>
  <c:forEach items="${changes}" var="change">
            <tr>
                <td>${fn:escapeXml(change.getUsername())}</td>
                <td>${fn:escapeXml(change.getActivityName())}</td>>
                <td>${fn:escapeXml(change.getGroupName())}</td>
                <td>${fn:escapeXml(change.getChangeType())}</td>
                <td><!-- FIXME --></td>
                <td><!-- FIXME --></td>
  </c:forEach>
            </tr>
        </tbody>
    </table>

</bbNG:learningSystemPage>
