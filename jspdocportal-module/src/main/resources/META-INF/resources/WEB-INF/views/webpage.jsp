<%@ page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" uri="http://www.mycore.org/jspdocportal/base.tld"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>

<fmt:message var="pageTitle" key="Webpage.title.${fn:replace(actionBean.path, '/', '.')}" />

<stripes:layout-render name="/WEB-INF/layout/default.jsp" pageTitle="${pageTitle}" layout="${layout}">
  <stripes:layout-component name="main_part">
    <div class="container">
    <c:if test="${not empty actionBean.info}">
      <div class="row">
        <div class="col col-md-8">
          <div class="ir-box">
            <mcr:includeWebcontent id="${fn:replace(actionBean.path, '/', '.')}" file="${actionBean.path}.html" />
          </div>
        </div>
        <div class="col col-md-4">
          <div class="row">
            <c:forEach var="id" items="${fn:split(actionBean.info,',')}">
              <div class="col">
                <div class="ir-box ir-box-bordered">
                  <mcr:includeWebcontent id="${id}" file="${fn:replace(id, '.', '/')}.html" />
                </div>
              </div>
            </c:forEach>
          </div>
        </div>
      </div>
    </c:if>
    <c:if test="${empty actionBean.info}">
      <div class="row">
        <c:if test="${empty requestScope['org.mycore.navigation.side.path']}">
          <div class="col ir-content-main">
              <div class="ir-box">
                <mcr:includeWebcontent id="${fn:replace(actionBean.path, '/', '.')}" file="${actionBean.path}.html" />
              </div>
          </div>
       </c:if>
        <c:if test="${not empty requestScope['org.mycore.navigation.side.path']}">
          <div class="col col-md-3 ir-content-side" id="left-side-nav">
              <mcr:outputNavigation mode="side" id="${fn:substringBefore(requestScope['org.mycore.navigation.side.path'], '.')}"></mcr:outputNavigation>
              <c:if test="${not empty actionBean.infoBox}">
                <mcr:includeWebcontent id="${fn:replace(actionBean.infoBox, '/', '.')}" file="${actionBean.infoBox}.html" />
              </c:if>
          </div>
          <div class="col col-md-9 ir-content-main">
              <div class="ir-box">
                <mcr:includeWebcontent id="${fn:replace(actionBean.path, '/', '.')}" file="${actionBean.path}.html" />
              </div>
          </div>
       </c:if>
      </div>
    </c:if>
    </div>
  </stripes:layout-component>
</stripes:layout-render>
