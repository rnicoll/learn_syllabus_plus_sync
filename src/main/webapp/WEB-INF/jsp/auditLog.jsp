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
    
    <bbNG:button label="Merged Courses" url="${mergedCourses}"></bbNG:button>

    <p>The following is a list of outstanding changes to student/group enrolments
        on this course.</p>
    
    <bbNG:inventoryList emptyMsg="There are no changes to this course based on Timetabling data."
                        className="uk.ac.ed.learn9.bb.timetabling.data.EnrolmentChangePart"
                        collection="${pendingChanges}"
                        description="This following is a list of pending changes to student/group enrolments on this
        course"
        objectVar="change"
        >
        <bbNG:listElement name="activityName" label="Activity">
            ${fn:escapeXml(change.activityName)}
        </bbNG:listElement>
        <bbNG:listElement name="username" isRowHeader="true" label="Username">
            ${fn:escapeXml(change.username)}
        </bbNG:listElement>
        <bbNG:listElement name="groupName" label="Group">
            ${fn:escapeXml(change.groupName)}
        </bbNG:listElement>
        <bbNG:listElement name="changeType" label="Change Type">
            ${fn:escapeXml(change.changeLabel)}
        </bbNG:listElement>
        <bbNG:listElement name="result" label="Status">
            ${fn:escapeXml(change.resultLabel)}
        </bbNG:listElement>
    </bbNG:inventoryList>

    <p>The following is a list of completed/failed changes to student/group
        enrolments on this course, driven by Timetabling.</p>
    
    <bbNG:inventoryList emptyMsg="There are no changes to this course based on Timetabling data."
                        className="uk.ac.ed.learn9.bb.timetabling.data.EnrolmentChangePart"
                        collection="${completeChanges}"
                        description="This following is a list of changes to student/group enrolments on this
        course"
        objectVar="change"
        >
        <bbNG:listElement name="activityName" label="Activity">
            ${fn:escapeXml(change.activityName)}
        </bbNG:listElement>
        <bbNG:listElement name="username" isRowHeader="true" label="Username">
            ${fn:escapeXml(change.username)}
        </bbNG:listElement>
        <bbNG:listElement name="groupName" label="Group">
            ${fn:escapeXml(change.groupName)}
        </bbNG:listElement>
        <bbNG:listElement name="changeType" label="Change Type">
            ${fn:escapeXml(change.changeLabel)}
        </bbNG:listElement>
        <bbNG:listElement name="result" label="Status">
            ${fn:escapeXml(change.resultLabel)}
        </bbNG:listElement>
        <bbNG:listElement name="updateCompleted" label="Update Completed">
            <c:if test="${not empty change.updateCompleted}"><fmt:formatDate value="${change.updateCompleted}" type="BOTH" dateStyle="MEDIUM" timeStyle="SHORT" /></c:if>
        </bbNG:listElement>
    </bbNG:inventoryList>

</bbNG:learningSystemPage>
