<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<%@ taglib uri="/bbNG"    prefix="bbNG"%>
<%@ taglib uri="/bbData"  prefix="bbData"%>
<%@ taglib uri="/bbUI" prefix="bbUI" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<bbNG:learningSystemPage title="Timetabling Groups" authentication="Y" entitlement="course.content.VIEW">

    <bbNG:pageHeader instructions="Timetabling Activites">
        <bbNG:pageTitleBar title="Timetabling Activities"/>
        <bbNG:breadcrumbBar>
            <bbNG:breadcrumb title="Timetabling Activities"/>
        </bbNG:breadcrumbBar>
    </bbNG:pageHeader>

    <bbNG:actionControlBar>
        <bbNG:actionButton id="audit_log" title="Audit Log" primary="true" url="${auditLog}" />
        <bbNG:actionButton id="merged_courses" title="Merged Courses" primary="true" url="${mergedCourses}" />
    </bbNG:actionControlBar>

    <p>The following is a list of Timetabling activities which have been mapped
        to this course.</p>
   
    <bbNG:inventoryList emptyMsg="There are no Timetabling activities for this course."
                        className="uk.ac.ed.learn9.bb.timetabling.data.Activity"
                        collection="${activities}"
                        description="This following is a list of activities for this course"
        objectVar="activity"
        >
        
        <bbNG:listElement name="moduleId" label="Module ID">
            ${fn:escapeXml(activity.module.timetablingCourseCode)}
        </bbNG:listElement>
        <bbNG:listElement name="name" isRowHeader="true" label="Name">
            ${fn:escapeXml(activity.activityName)}
        </bbNG:listElement>
        <bbNG:listElement name="template" label="Template">
            <c:if test="${not empty activity.template}">
            ${fn:escapeXml(activity.template.templateName)}
            </c:if>
        </bbNG:listElement>
        <bbNG:listElement name="type" label="Type">
            <c:if test="${not empty activity.type}">
            ${fn:escapeXml(activity.type.typeName)}
            </c:if>
        </bbNG:listElement>
        <bbNG:listElement name="set_size" label="Set Size">
            <c:if test="${not empty activity.template}">
            ${fn:length(activity.template.activities)}
            </c:if>
        </bbNG:listElement>
        <bbNG:listElement name="webCtActive" label="WebCT Active">
            ${fn:escapeXml(activity.module.webCtActive)}
        </bbNG:listElement>
        <bbNG:listElement name="for_vle" label="For VLE">
            <c:if test="${activity.template.userText5} not empty">
            ${fn:escapeXml(activity.template.userText5)}
            </c:if>
        </bbNG:listElement>
        <bbNG:listElement name="jta_parent" label="JTA Parent">
            <c:if test="${not empty activity.isJtaParent}">
            ${activity.isJtaParent}
            </c:if>
        </bbNG:listElement>
        <bbNG:listElement name="jta_child" label="JTA Child">
            <c:if test="${not empty activity.isJtaChild}">
            ${activity.isJtaChild}
            </c:if>
        </bbNG:listElement>
    </bbNG:inventoryList>

</bbNG:learningSystemPage>
