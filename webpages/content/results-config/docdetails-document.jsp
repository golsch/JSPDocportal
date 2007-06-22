<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml"  prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/lib/mycore-taglibs.jar" prefix="mcr" %>
<fmt:setLocale value='${requestScope.lang}'/>
<fmt:setBundle basename='messages'/>
<c:set var="host" value="local" scope="request" />
<c:set var="WebApplicationBaseURL" value="${applicationScope.WebApplicationBaseURL}" />
<c:set var="mcrid" value="${param.id}" />
<c:set var="offset" value="${param.offset}" />
<c:set var="size" value="${param.size}" />
<c:set var="debug" value="${param.debug}" />
<c:set var="print" value="${param.print}" />
<c:set var="from"  value="${param.fromWForDB}" /> 
<c:set var="path"  value="${param.path}" /> 
<c:set var="style"  value="${param.style}" /> 
<c:set var="type" value="${fn:split(mcrid,'_')[1]}"/>

<c:choose>
 <c:when test="${fn:contains(style,'user')}">
	<mcr:receiveUserAsJdom var="mycoreobject" />
	<c:set var="type" value="user"/>
 </c:when>
 <c:otherwise>
  	<mcr:receiveMcrObjAsJdom var="mycoreobject" mcrid="${mcrid}" fromWForDB="${from}" />
 </c:otherwise>
</c:choose>

<c:choose>
   <c:when test="${requestScope.host}">
      <c:set var="host" value="${requestScope.host}" />
   </c:when>
   <c:otherwise>
      <c:set var="host" value="local" />   
   </c:otherwise>
</c:choose>
<c:choose>
   <c:when test="${param.offset > 0}">
      <c:set var="offset" value="${param.offset}" />
   </c:when>
   <c:otherwise>
      <c:set var="offset" value="0" />
   </c:otherwise>
</c:choose>
<c:choose>
   <c:when test="${param.size > 0}">
      <c:set var="size" value="${param.size}" />
   </c:when>
   <c:otherwise>
     <c:set var="size" value="0" />
   </c:otherwise>
</c:choose>

<c:choose>
 <c:when test="${fn:contains(from,'workflow')}" >
     <c:set var="layout" value="preview" />
 </c:when>
 <c:otherwise>
     <c:set var="layout" value="normal" />
 </c:otherwise> 
</c:choose>

<mcr:checkAccess permission="read" var="readAccess" key="${mcrid}" />
<mcr:docDetails mcrObj="${mycoreobject}" var="docDetails" lang="${requestScope.lang}" style="${style}" />
	
<table width="100%" >
 <tr>
   <td>
     <div class="headline">
		 <fmt:message key="OMD.${type}.title" />:
		 <x:forEach select="$docDetails//metaname[@name='OMD.maintitle']/metavalues/metavalue">
		 	<x:out select="./@text" escapeXml="./@escapeXml" />
		 </x:forEach> 
     </div>
   </td>
   <td width="30">&nbsp;</td>
   <td align="center" >
   	    <nobr>
         <mcr:browseCtrl id="${param.resultid}" offset="${offset}" >
            <c:if test="${!empty(lastHitID)}">
                <a href="${WebApplicationBaseURL}nav?path=${path}&id=${lastHitID}&offset=${offset -1}&resultid=${param.resultid}">&lt;&lt;</a>&#160;&#160;
            </c:if>
            <a href="${WebApplicationBaseURL}servlets/MCRJSPSearchServlet?mode=results&id=${param.resultid}">^</a>&#160;&#160;
            <c:if test="${!empty(nextHitID)}">
                <a href="${WebApplicationBaseURL}nav?path=${path}&id=${nextHitID}&offset=${offset +1}&resultid=${param.resultid}">&gt;&gt;</a>                        
            </c:if>
         </mcr:browseCtrl>
        </nobr>
   </td>
  </tr>

 <tr valign = "bottom" >
  <td>
  <mcr:session var="curUserID" method="get" type="userID"/>
  <x:forEach select="$docDetails">
  	  	<x:set var="currentID" select="./@ID" />
  </x:forEach>

   <c:choose>
   <c:when test="${readAccess or fn:contains(currentID, curUserID)}">
    <table cellspacing="0" cellpadding="0" id="metaData" >
    <x:forEach select="$docDetails//metaname">        
				<x:set var="data" select="." scope="request" />
				<c:set var="type"><x:out select = "./@type" /></c:set>
				<x:choose>
					<x:when select="./@type = 'line'">
						<c:if test="${exist > 0}" >
							<jsp:include page="docdetailitems/docdetailitem-${type}.jsp" /> 
						</c:if>
					    <c:set var="exist" value="0" />  
					</x:when>
					<x:otherwise>
						<x:set var="exist1" select="count(./metavalues)+count(./digitalobjects)" />
						<c:set var ="exist" value="${exist+exist1}" />
						<jsp:include page="docdetailitems/docdetailitem-${type}.jsp" /> 
					</x:otherwise>
				</x:choose>
			
				

       </x:forEach>                 


<%--    <x:forEach select="$docDetails//metaname">
      <x:choose>
         <x:when select="./@type = 'space'">
            <tr>
               <td colspan="2" class="metanone">&nbsp;</td>
            </tr>                
         </x:when>
         <x:when select="./@type = 'line'">
            <c:if test="${exist > 0}" >
              <c:set var="exist" value="0" />              
              <tr>
              <td colspan="2" class="metanone"><hr/></td>
              </tr>                
             </c:if> 
         </x:when>
         <x:when select="./@type = 'table'">
           <x:set var="exist" select="count(./metavalues)" />                  
           <x:set var="nameKey" select="string(./@name)" />
           <tr>
            <td class="metaname">
             <c:if test="${! empty(nameKey)}">
	             <fmt:message key="${nameKey}" />:
             </c:if>
            </td>
            <td class="metavalue">
               <x:set var="colnum" select="count(./metavalues)" />
               <x:set var="rownum" select="count(./metavalues[1]/metavalue)" />
	           <x:set var="rownum2" select="count(./metavalues[2]/metavalue)" />
               <c:if test="${rownum2 > rownum}" >
                  <x:set var="rownum" select="count(./metavalues[2]/metavalue)" />
               </c:if>   
               <table border="0" cellpadding="0" cellspacing="4px">
                   <c:forEach var="i" begin="1" end="${rownum}">
                     <tr>
                      <c:forEach var="j" begin="1" end="${colnum}">                              
                        <td valign="top">
                          <div style="min-width:110px" >
                          <x:set var="ikey" select="string(./metavalues[$j]/@introkey)" />   			                               
					      <x:choose>
                             <x:when select="./metavalues[$j]/@type = 'messagekey'">
                              <x:set var="val" select="string(./metavalues[$j]/metavalue[$i]/@text)" />
                              <c:set var="messagekey" value="${ikey}.${val }" />
   							  <fmt:message key="${messagekey}" />
					 		 </x:when>
					         <x:otherwise>
								<c:if test="${!empty(ikey)}">   			                               					         
			                        <fmt:message key="${ikey}" />&nbsp;
	                            </c:if>    
                                <x:out select="./metavalues[$j]/metavalue[$i]/@text" escapeXml="false" />
							 </x:otherwise>   
						  </x:choose>       
							&nbsp;                          
                          </div>
                         </td>
                        </c:forEach>
                       </tr>
                   </c:forEach>
                </table>
           </td>
          </tr>
         </x:when>                      
         <x:when select="./@type = 'standard'">
           <x:set var="exist" select="count(./metavalues)" />                  
           <x:set var="nameKey" select="string(./@name)" />
           <tr>
               <td class="metaname">
	             <c:if test="${! empty(nameKey)}">
		             <fmt:message key="${nameKey}" />:
	             </c:if>               
			   </td>
               <td class="metavalue">
                  <x:forEach select="./metavalues">
                     <x:set var="separator" select="./@separator" />
                     <x:set var="terminator" select="./@terminator" />
                     <x:if select="string-length(./@introkey) > 0" >
                        <x:set var="introkey" select="string(./@introkey)" />
                        <fmt:message key="${introkey}" />
                     </x:if>
                     <x:forEach select="./metavalue">
                        <x:if select="generate-id(../metavalue[position() = 1]) != generate-id(.)">
                           <x:out select="$separator" escapeXml="false" />
                        </x:if>
                        <x:choose>
                           <x:when select="../@type = 'BooleanValues'">
                              <x:set var="booleanKey" select="concat(./@type,'-',./@text)" />
                              <fmt:message key="OMD.${booleanKey}" />
                           </x:when>
                           <x:when select="../@type = 'AuthorJoin'">
                              <x:set var="authorjoinKey" select="concat(./@type,'-',./@text)" />
                              <x:set var="hrefquery" select="concat($WebApplicationBaseURL,'servlets/MCRJSPSearchServlet?mask=~searchstart-indexcreators&', ./@querytext) " />
                              <a href="${hrefquery}" target="<x:out select="./@target" />"><fmt:message key="OMD.${authorjoinKey}" /></a>                                 
                           </x:when>                                     
                           <x:when select="./@href != ''">
                              <a href="<x:out select="./@href" />" target="<x:out select="./@target" />"><x:out select="./@text" /></a>
                           </x:when>
                           <x:otherwise>
                              <x:out select="./@text" escapeXml="./@escapeXml" />
                           </x:otherwise>
                        </x:choose>
                     </x:forEach>
                   
                     <x:if select="generate-id(../metavalues[position() = last()]) != generate-id(.)">
                        <x:out select="$terminator" escapeXml="false" />
                     </x:if>                               
                  </x:forEach>
                  
				<x:forEach select="./digitalobjects">
				    <c:set var="label"  value="dummy" />
					<table border="0" cellpadding="0" cellspacing="0" width="100%">
					
					<x:forEach select="./digitalobject">
					      <x:set var="actlabel"  select="string(./@derivlabel)" />
				      <x:set var="derivid"  select="string(./@derivid)" />
				      
					  <c:if test="${!fn:contains(label,actlabel)}">
                    <mcr:checkAccess permission="read" var="accessallowed" key="${derivid}" />
						  <tr>										  
							<td align="left" valign="bottom" >
								<div class="derivateHeading">
								   <br/><c:out value="${actlabel}" />
								</div>
							</td>	 
						   <c:if test="${accessallowed and empty(param.print)}">
							<td>
							 <a href="<x:out select="concat($WebApplicationBaseURL,'zip?id=',$derivid)" />"
								class="linkButton"><fmt:message key="OMD.zipgenerate" /></a>&#160;&#160;
							</td>
							<td>
               	                <a href="<x:out select="concat($WebApplicationBaseURL,'file/',$derivid,'/','?hosts=',$host)" />" target="_self"><fmt:message key="OMD.details" />&gt;&gt;</a>&#160;&#160; 
               	            </td>
               	            </c:if>
               	          </tr>   										  
               	      </c:if>   
               	      <c:choose>
					  <c:when test="${accessallowed}">
					  <tr>
						<td align="left" valign="top" colspan="3" >
								  <div class="derivate">
								  <x:set var="URL"	select="concat($WebApplicationBaseURL,'file/',./@derivid,'/',./@derivmain,'?hosts=',$host)" />
						  <x:set var="contentType" select="string(./@contentType)" />
						  <x:set var="size" select="string(./@size)" />
						  <table>
							<tr>
								<td><a href="<x:out select="$URL" />" target="_blank"><x:out select="./@derivmain" /></a>&#160;
								(<c:out value="${size}" /> Bytes)&#160;&#160;</td>
								<c:if test="${fn:contains('gif-jpeg-png', contentType) && size < 100000}">
									<td class="imageInResult"><a href="${URL}"><img	src="${URL}" width="100"></a></td>
								</c:if>
							</tr>
				 		  </table>
						  </div>
						</td>
						  </tr>
					  </c:when>
					  <c:otherwise>
					    <tr>
                   	     <td>
						  <div class="derivate">
                      	     	<x:out select="./@derivmain" />&#160;(<x:out select="./@size" /> Bytes)
                             	     	--- <fmt:message key="OMD.fileaccess.denied" />
                       	  </div>	
                   	     </td>
                       	 </tr>
                        </c:otherwise>
                       </c:choose>
				      <c:set var="label" value="${actlabel}" />	     
					</x:forEach>
				  </table>
				</x:forEach>
               </td>
            </tr>        
         </x:when>
      </x:choose>   
    </x:forEach> --%>
   
   <!-- show link for this page -->
	<c:if test="${!fn:contains(style,'user')}">
    	<tr><td colspan="2" class="metanone">&nbsp;</td></tr>     
	   <tr>
   			<td class="metaname"> <fmt:message key="OMD.selflink" />:</td>
        	<td class="metavalue"><a href="${WebApplicationBaseURL}metadata/${param.id}">
	          	${WebApplicationBaseURL}metadata/${param.id} </a>
			</td>
	   </tr> 
	</c:if>
    
  </table>
   
  </c:when>
  <c:otherwise>
	    <fmt:message key="Webpage.error.NoAccessRight" />  
  </c:otherwise>
  </c:choose>  
 </td>
</tr>
</table>
 