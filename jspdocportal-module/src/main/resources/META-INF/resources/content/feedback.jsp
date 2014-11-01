<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml"  %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

<fmt:message var="pageTitle" key="Webpage.feedback" />
<stripes:layout-render name="../WEB-INF/layout/default.jsp" pageTitle="${pageTitle}" layout="2columns">
    <stripes:layout-component name="contents">
<h2>${pageTitle}</h2>

<div class="ur-text">
	<mcr:includeWebContent file="feedback.html" />
</div>
<div>

<stripes:messages/>

<stripes:form beanclass="org.mycore.frontend.jsp.stripes.actions.SendFeedbackAction"
			  id="Formular" enctype="multipart/form-data" acceptcharset="UTF-8"
			  class="form-horizontal">
	
	<stripes:hidden name="returnURL">${actionBean.returnURL}</stripes:hidden>
	<stripes:hidden name="subject">${actionBean.subject}</stripes:hidden>
	<stripes:hidden name="recipient">unigeschichte@uni-rostock.de</stripes:hidden>
	<stripes:hidden name="topicHeader">${actionBean.topicHeader}</stripes:hidden>
	<stripes:hidden name="topicURL">${actionBean.topicURL}</stripes:hidden>			  
			  
  <div class="form-group">
    <label class="col-sm-2 control-label"><fmt:message key="Webpage.feedback.label.recipient" /></label>
    <div class="col-sm-10">
     <p class="form-control-static"><b>${actionBean.recipient}</b></p>
    </div>
  </div>
   <div class="form-group">
    <label class="col-sm-2 control-label"><fmt:message key="Webpage.feedback.label.topic" /></label>
    <div class="col-sm-10">
     <h4 class="form-control-static" style="margin-top:0px">${actionBean.topicHeader}<br />(${actionBean.topicURL})</h4>
    </div>
  </div>
  
  <div class="form-group">
    <label for="inputName" class="col-sm-2 control-label"><fmt:message key="Webpage.feedback.label.senderName" /></label>
    <div class="col-sm-10">
      <stripes:text class="form-control" id="inputName" name="fromName" />
    </div>
  </div>
  <div class="form-group">
    <label for="inputEmail" class="col-sm-2 control-label"><fmt:message key="Webpage.feedback.label.senderEmail" /></label>
    <div class="col-sm-10">
      <stripes:text class="form-control" id="inputEmail" name="fromEmail" />
    </div>
  </div>
  <div class="form-group">
    <label for="inputEmail" class="col-sm-2 control-label"><fmt:message key="Webpage.feedback.label.message" /></label>
    <div class="col-sm-10">
      <stripes:textarea class="form-control" id="inputEmail" rows="10" name="message" />
    </div>
  </div>
  <hr />
   <div class="form-group">
    <label class="col-sm-2 control-label"></label>
    <div class="col-sm-10">
    	<fmt:message key="Webpage.feedback.button.send" var="lblSend" />
      <input name="doSend" class="btn btn-primary" value="${lblSend}" type="submit" /> 
    </div>
  </div>
</stripes:form>
</div>

</stripes:layout-component>
</stripes:layout-render>