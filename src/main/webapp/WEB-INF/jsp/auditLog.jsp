<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<%@ taglib uri="/bbNG"    prefix="bbNG"%>
<%@ taglib uri="/bbData"  prefix="bbData"%>
<%@ taglib uri="/bbUI" prefix="bbUI" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<bbNG:learningSystemPage title="Timetabling Groups" authentication="Y" entitlement="course.content.VIEW">

 <bbNG:pageHeader instructions="Timetabling Groups Audit Log">
   <bbNG:pageTitleBar title="Timetabling Groups"/>
   <bbNG:breadcrumbBar>
     <bbNG:breadcrumb title="Timetabling Groups"/>
   </bbNG:breadcrumbBar>
  </bbNG:pageHeader>
 
  <jsp:include page="/getForm">
      <jsp:param name="content_id" value="${content_id}"/>
      <jsp:param name="formText" value="${formText}"/>
  </jsp:include>

</bbNG:learningSystemPage>
