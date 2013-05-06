<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%-- 
    Document   : view
    Created on : 28-May-2012, 14:15:57
    Author     : rgood
--%>
<%@ taglib uri="/bbNG"    prefix="bbNG"%>
<%@ taglib uri="/bbData"  prefix="bbData"%>
<%@ taglib uri="/bbUI" prefix="bbUI" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<bbNG:learningSystemPage title="${pageTitle}" authentication="Y" entitlement="course.content.VIEW">

 <bbNG:pageHeader instructions="${pageDesc}">
   <bbNG:pageTitleBar title="${pageTitle}"/>
   <bbNG:breadcrumbBar>
     <bbNG:breadcrumb title="${pageTitle}"/>
   </bbNG:breadcrumbBar>
  </bbNG:pageHeader>
 
  <jsp:include page="/getForm">
      <jsp:param name="content_id" value="${content_id}"/>
      <jsp:param name="formText" value="${formText}"/>
  </jsp:include>

</bbNG:learningSystemPage>
