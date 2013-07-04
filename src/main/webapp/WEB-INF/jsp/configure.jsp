<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<%@ taglib uri="/bbNG"    prefix="bbNG"%>
<%@ taglib uri="/bbData"  prefix="bbData"%>
<%@ taglib uri="/bbUI" prefix="bbUI" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<bbNG:genericPage title="Timetabling Groups" authentication="Y" entitlement="course.content.VIEW">

    <bbNG:pageHeader instructions="Timetabling Groups Configuration">
        <bbNG:pageTitleBar title="Timetabling Groups"/>
        <bbNG:breadcrumbBar>
            <bbNG:breadcrumb title="Timetabling Groups"/>
        </bbNG:breadcrumbBar>
    </bbNG:pageHeader>

    <bbNG:actionControlBar>
        <bbNG:actionButton id="run_sync" title="Run Synchronisation" primary="true" url="${runSynchronisation}" />
    </bbNG:actionControlBar>
    
    <bbNG:inventoryList emptyMsg="There are no synchronisation runs to display."
                        className="uk.ac.ed.learn9.bb.timetabling.data.SynchronisationRun"
                        collection="${runs}"
        objectVar="run"
        >
        <bbNG:listElement name="runId" isRowHeader="true" label="ID">${run.runId}</bbNG:listElement>
        <bbNG:listElement name="startTime" label="Start Time">
            <fmt:formatDate value="${run.startTime}" type="BOTH" dateStyle="MEDIUM" timeStyle="SHORT" />
        </bbNG:listElement>
        <bbNG:listElement name="endTime" label="End Time">
            <c:if test="${not empty run.endTime}"><fmt:formatDate value="${run.endTime}" type="BOTH" dateStyle="MEDIUM" timeStyle="SHORT" /></c:if>
        </bbNG:listElement>
        <bbNG:listElement name="result" label="Result">${fn:escapeXml(run.result)}</bbNG:listElement>
    </bbNG:inventoryList>

</bbNG:genericPage>
