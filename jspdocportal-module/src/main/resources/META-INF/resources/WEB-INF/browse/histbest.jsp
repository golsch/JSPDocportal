<%@ page pageEncoding="UTF-8" contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c"       uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="x"       uri="http://java.sun.com/jsp/jstl/xml"%>
<%@ taglib prefix="fmt"     uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn"      uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="mcr" 	uri="http://www.mycore.org/jspdocportal/base.tld"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
	
<%@ taglib prefix="search" tagdir="/WEB-INF/tags/search"%>

<fmt:message var="pageTitle" key="Webpage.browse.title.${actionBean.result.mask}" />
<stripes:layout-render name="/WEB-INF/layout/default.jsp" pageTitle="${pageTitle}" layout="2columns">
	<stripes:layout-component name="html_header">
		<meta name="mcr:search.id" content="${actionBean.result.id}" />
	</stripes:layout-component>
	<stripes:layout-component name="left_side">
		<div class="ir-box ir-box-bordered">
			<div class="main_navigation">
				<mcr:outputNavigation id="left" cssClass="nav ir-sidenav" expanded="true" mode="left" />
			</div>
			<div class="main_navigation">
				<mcr:outputNavigation id="publish" cssClass="nav ir-sidenav" expanded="false" mode="left" />
			</div>
		</div>
		
		
		<div class="ir-box ir-box-bordered" style="margin-top:36px">
		<h3>Filter und Facetten</h3>
			<div class="panel panel-default">
				<div class="panel-heading">
					<form class="form-horizontal" onsubmit="return false;">
						<div class="form-group">
    						<div class="col-sm-12">
    							<select id="filterField" name="filterField" class="form-control input-sm">
  									<option value="ir.creator_all"><fmt:message key="Browse.Filter.histbest.ir.creator_all"/></option>
  									<option value="ir.title_all"><fmt:message key="Browse.Filter.histbest.ir.title_all"/></option>
  									<option value="ir.pubyear_start"><fmt:message key="Browse.Filter.histbest.ir.pubyear_start"/></option>
  									<option value="ir.pubyear_end"><fmt:message key="Browse.Filter.histbest.ir.pubyear_end" /></option>
 								</select>
 							</div>
   						</div>
  						<div class="form-group">	
   							<div class="col-sm-9">
								<input class="form-control input-sm" id="filterValue" name="filterValue" style="width:100%" placeholder="Wert" type="text">
   							</div>
  							<div class="col-sm-3">
  								<script type="text/javascript">
  									function changeFilterIncludeURL() {
  											window.location=$("meta[name='mcr:baseurl']").attr("content")
  										 			    + "browse/histbest?_search="
  										           		+ $("meta[name='mcr:search.id']").attr("content")
  											       		+ "&_add-filter="
  											       		+ encodeURIComponent("+" + $("#filterField option:selected").val()+":"+$("#filterValue").val());
  										}
  										function changeFilterExcludeURL() {
  											window.location=$("meta[name='mcr:baseurl']").attr("content")
  												       + "browse/histbest?_search="
  										        	   + $("meta[name='mcr:search.id']").attr("content")
  											       	   + "&_add-filter="
  											       	   + encodeURIComponent("-" + $("#filterField option:selected").val()+":"+$("#filterValue").val());
  										}
  										</script>
  										
  										<button id="filterInclude" class="btn btn-sm btn-primary" onclick="changeFilterIncludeURL();">
											<span class="glyphicon glyphicon-plus"></span>
										</button> 	
          							</div>
  								</div>
  					</form>
  				</div>
  			</div>
 	
  			<div class="row" style="margin-bottom:24px;">
  				<div class="col-sm-12">
					<c:forEach var="fq" items="${actionBean.result.filterQueries}">
						<c:if test="${not fn:contains(fq, '.facet:')}">
  							<c:url var="url" value="${WebApplicationBaseURL}browse/histbest">
  								<c:param name="_search" value="${actionBean.result.id}" />
  								<c:param name="_remove-filter" value="${fq}" />
							</c:url>
							<c:set var="c"><fmt:message key="Browse.Filter.histbest.${fn:substringBefore(fn:substring(fq, 1, -1),':')}"/>: ${actionBean.calcFacetOutputString(fn:substringBefore(fn:substring(fq, 1, -1),':'), fn:substringAfter(fn:substring(fq, 1, -1),':'))}</c:set>
							<a class="btn btn-sm btn-default ir-btn-facet" style="display:block;text-align:left;white-space:normal;margin-bottom:3px;color:black;width:100%" href="${url}">
								<span class="glyphicon glyphicon-remove pull-right" style="margin-top:3px; color:darkred;"></span>
								${c}										
							</a>
						</c:if>
					</c:forEach>
				</div>
			</div>
						
			<search:result-facets result="${actionBean.result}" mask="histbest" />
		</div> 
  	</stripes:layout-component>
  	
    <stripes:layout-component name="contents">
    	<div class="row ir-box">
    		<div class="col-xs-12">
				<h2>${pageTitle}</h2>
			</div>
		</div>
		<div class="row ir-box">
			<div class="col-xs-12">
			<search:result-sorter result="${actionBean.result}" 
			                      fields="score,ir.pubyear_start,modified,ir.creator.result,ir.title.result" mask="histbest"/>
			<search:result-browser result="${actionBean.result}">
				<c:set var="doctype" value="${fn:substringBefore(fn:substringAfter(mcrid, '_'),'_')}" />
				<c:choose>
					<c:when test="${(doctype eq 'document') or (doctype eq 'bundle')}">
						<search:result-entry-document entry="${entry}" url="${url}" />
					</c:when>
					<c:otherwise>
						<search:result-entry entry="${entry}" url="${url}" />
					</c:otherwise>
				</c:choose>
			</search:result-browser>
			</div>
		</div>
	</stripes:layout-component>
</stripes:layout-render>