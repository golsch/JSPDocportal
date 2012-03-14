<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ page pageEncoding="UTF-8" %>

<c:set  var="baseURL" value="${applicationScope.WebApplicationBaseURL}"/>
<fmt:message var="pageTitle" key="WF.institution" /> 
<stripes:layout-render name="../../../WEB-INF/layout/default.jsp" pageTitle = "${pageTitle}">
	<stripes:layout-component name="contents">

<!--  debug handling -->
<c:choose>
   <c:when test="${not empty param.debug}">
      <c:set var="debug" value="true" />
   </c:when>
   <c:otherwise>
      <c:set var="debug" value="false" />
   </c:otherwise>
</c:choose>

<!--  handle task ending parameters -->
<c:if test="${not empty param.endTask}">
	<mcr:endTask success="success" processID="${param.processID}" 	taskName="${param.endTask}" transition="${param.transition}"/>
</c:if>

<!--  task management part -->

<mcr:getWorkflowTaskBeanList var="myTaskList" mode="activeTasks" 
							 workflowTypes="institution"  varTotalSize="total1" />
<mcr:getWorkflowTaskBeanList var="myProcessList" mode="initiatedProcesses" 
							 workflowTypes="institution" varTotalSize="total2" />
<h2><fmt:message key="WF.institution" /></h2>
<table>
	<tr>
		<td><img title="" alt="" src="${baseURL}images/greenArrow.gif"></td>
		<td>
			<a target="_self" href="${baseURL}nav?path=~institutionbegin"><fmt:message key="WF.institution.StartWorkflow" /></a>
		</td>
	</tr>
	<tr />
	<tr>
		<td><img title="" alt="" src="${baseURL}images/greenArrow.gif"></td>
			<td><fmt:message key="WF.institution.SearchInstitutionToEdit" /> </td>
	</tr>
	<tr>
		<td />
		<td>
			<%--<c:url var="url" value="${WebApplicationBaseURL}editor/searchmasks/SearchMask_InstitutionEdit.xml">
				<c:param name="XSL.editor.source.new" value="true" />
				<c:param name="XSL.editor.cancel.url" value="${WebApplicationBaseURL}" />
				<c:param name="lang" value="${requestScope.lang}" />
			</c:url>
			<c:import url="${url}" />  --%>
			<mcr:includeEditor editorPath="editor/searchmasks/SearchMask_InstitutionEdit.xml"/>      
			<br/>
		</td>
	</tr>
</table>
	<c:out value="${task.workflowProcessType}" />
<c:choose>
	<c:when test="${empty myTaskList && empty myProcessList}">
      <fmt:message key="WF.common.EmptyWorkflow" />   
      <hr/>
      <mcr:includeWebContent file="workflow/institution_introtext.html"/>
   	  <br />&nbsp;<br />
	</c:when>
	<c:otherwise>
		<br />&nbsp;<br />
		<h3><fmt:message key="WF.common.MyTasks" /></h3>   
    	<table cellspacing="3">       
			<c:forEach var="task" items="${myTaskList}">
				<tr>
					<td class="task">
			        	<c:set var="task" scope="request" value="${task}" />
					   	<c:import url="/content/workflow/${task.workflowProcessType}/getTasks.jsp" />
			    	</td>
		    	</tr>       
			</c:forEach>
    	    <c:if test="${empty myTaskList}">
			  	<tr>
		  			<td class="task">
			           <fmt:message key="WF.common.NoTasks" />
    	    		 </td>
        		</tr>
			</c:if>
		</table>
    	<br />&nbsp;<br />
    	<h3><fmt:message key="WF.common.MyInitiatedProcesses" /></h3>
		<table>
    		<c:forEach var="task" items="${myProcessList}">
				<tr>
					<td class="task">
						<c:set var="task" scope="request" value="${task}" />
						<c:import url="/content/workflow/${task.workflowProcessType}/getTasks.jsp" />
		           </td>
				</tr>
	        </c:forEach>
    	    <c:if test="${empty myProcessList}">
				<tr>
					<td class="task">
			           <fmt:message key="WF.common.NoTasks" />
		           </td>
		 		</tr>
        	</c:if>
		</table>   
	</c:otherwise>
</c:choose>        
</stripes:layout-component>
</stripes:layout-render>