<%@ page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"   %>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>

<fmt:message var="pageTitle" key="Webpage.login.ChangeUserID" /> 
<stripes:layout-render name="../WEB-INF/layout/default.jsp" pageTitle = "${pageTitle}" layout="2columns">
	<stripes:layout-component name="contents">
		
		<!-- available user status  
	 			actionBean.loginStatus = { user.login, user.invalid_password, user.welcome, user.disabled, user.unknown, user.unkwnown_error
          -->
		<div class="ir-box">
	 		<h2><fmt:message key="Webpage.login.ChangeUserID" /></h2>
      		 <p><fmt:message key="Webpage.login.info" /></p>
        
			<stripes:form class="form-horizontal"
				beanclass="org.mycore.frontend.jsp.stripes.actions.MCRLoginAction"
				id="loginForm" enctype="multipart/form-data" acceptcharset="UTF-8">
				<div class="stripesinfo">
					<stripes:errors />
					<stripes:messages />
				</div>
			
			<c:if test="${actionBean.loginOK}">
				<p><fmt:message key="Webpage.login.YouAreLoggedInAs" />:&#160;	<strong><c:out value="${actionBean.userID}"></c:out></strong></p>
			</c:if>
			<c:if test="${not empty actionBean.loginStatus}">
				<div class="alert alert-info" role="alert"><fmt:message key="Webpage.login.status.${actionBean.loginStatus}" >
								<fmt:param value="${actionBean.userName}" /></fmt:message></div>
			</c:if>
            <div class="row">
				<div class="col-xs-12 col-sm-offset-3 col-sm-6 form-horizontal">
					<div class="form-group">
						<label for="inputUserID" class="col-sm-4 control-label"><fmt:message key="Webpage.login.UserLogin" />:</label>
						<div class="col-sm-8">
							<input type="text" id="inputUserID" name="userID" placeholder="User ID"  class="form-control" />
						</div>
					</div>
					<div class="form-group">
						<label for="inputPassword" class="col-sm-4 control-label"><fmt:message key="Webpage.login.Password" />:</label>
						<div class="col-sm-8">
							<input type="password" id="inputPassword" name="password" placeholder="Passwort" class="form-control" />
						</div>
					</div>

					<div class="form-group">
						<div class="col-sm-offset-4 col-sm-8">
							<div class="col-sm-6 text-center">
								<input name="doLogin" class="btn btn-primary" value="<fmt:message key="Webpage.login.Login" />" type="submit" /> 
							</div>
							<c:if test="${actionBean.loginOK}">
								<div class="col-sm-6 text-center">
									<input name="doLogout" class="btn btn-danger" value="<fmt:message key="Webpage.login.Logout" />" type="submit" /> 
								</div>
							</c:if>
						</div>
					</div>
				</div>
        	</div>
				
			<c:if test="${not empty actionBean.nextSteps}">
				<div class="panel panel-default" style="margin-top:64px">
  					<div class="panel-heading"><strong><fmt:message key="Webpage.login.your_options" /></strong></div>
  					<div class="panel-body">
  						<ul>
    						<c:forEach var="nextStep" items="${actionBean.nextSteps}">
    							<c:set var="href"><c:out escapeXml="true" value="${nextStep.url}"/></c:set>
									<li>
										<a href="${href}">${nextStep.label}</a>
									</li>
							</c:forEach>
						</ul>
					</div>
				</div>
			</c:if>
    	
		</stripes:form>
		</div>
	</stripes:layout-component>
</stripes:layout-render>
