<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<%@ taglib uri="/bbNG"    prefix="bbNG"%>
<%@ taglib uri="/bbData"  prefix="bbData"%>
<%@ taglib uri="/bbUI" prefix="bbUI" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<bbNG:learningSystemPage title="Timetabling Groups" authentication="Y" entitlement="course.content.VIEW">

 <bbNG:pageHeader instructions="Timetabling Groups Merged Courses">
   <bbNG:pageTitleBar title="Timetabling Groups"/>
   <bbNG:breadcrumbBar>
     <bbNG:breadcrumb title="Timetabling Groups"/>
   </bbNG:breadcrumbBar>
  </bbNG:pageHeader>
    
    <bbNG:button label="Audit Log" url="${auditLog}"></bbNG:button>
    
    <p>The following is a list of merged courses that feed into this course:</p>
    
    <bbNG:inventoryList emptyMsg="There are no merged child courses of this course."
                        className="uk.ac.ed.learn9.bb.timetabling.data.LearnCourseCode"
                        collection="${mergedCourseCodes}"
                        description="This following is a list of merged child courses of this course."
        objectVar="courseCode"
        >
        <bbNG:listElement name="courseCode" label="Course Code">
            ${fn:escapeXml(courseCode)}
        </bbNG:listElement>
    </bbNG:inventoryList>

</bbNG:learningSystemPage>
