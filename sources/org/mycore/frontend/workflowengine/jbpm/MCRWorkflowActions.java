/*
 * $RCSfile$
 * $Revision$ $Date$
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */

package org.mycore.frontend.workflowengine.jbpm;


import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.frontend.editor.MCRRequestParameters;
import org.mycore.frontend.fileupload.MCRUploadHandlerMyCoRe;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.frontend.servlets.MCRServletJob;

/**
 * This class is the superclass of servlets which checks the MCREditorServlet
 * output XML and store the XML in a file or if an error was occured start the
 * editor again.
 * 
 * @author Heiko Helmbrecht
 * @version $Revision$ $Date$
 */
public class MCRWorkflowActions extends MCRServlet {
	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = Logger.getLogger(MCRWorkflowActions.class);
	private static MCRWorkflowEngineManagerInterface WFI = MCRWorkflowEngineManagerFactory.getDefaultImpl();

	/**
     * This method overrides doGetPost of MCRServlet. <br />
     */
    public void doGetPost(MCRServletJob job) throws Exception {
    	MCRSession mcrSession = MCRSessionMgr.getCurrentSession();
    	HttpServletRequest request = job.getRequest();
    	HttpServletResponse response = job.getResponse();
        String lang = mcrSession.getCurrentLanguage();
    	
        MCRRequestParameters parms = new MCRRequestParameters(request);

        String pid = parms.getParameter("processid");
        
        MCRJbpmWorkflowObject wfo = new MCRJbpmWorkflowObject(Long.parseLong(pid));
        //jbpm_variableinstance  initiator, authorID, reservatedURN und createdDocID
        String mcrid = wfo.getStringVariableValue("createdDocID");
        String userid = wfo.getStringVariableValue("initiator");
        String fileCnt = wfo.getStringVariableValue("fileCnt");
        String documentType = wfo.getDocumentType();
        
        String derivateID = parms.getParameter("derivateID");
        String nextPath = parms.getParameter("nextPath");

        if ( nextPath == null || nextPath.length() == 0)        	nextPath = "~workflow-" + documentType;
        
        String todo = parms.getParameter("todo");
       
        if ( "WFAddWorkflowObject".equals(todo) ) {
        	// leeren Editor f�r das Object includieren
        }
        if ( "WFEditWorkflowObject".equals(todo) ) {
        	wfo.setWorkflowStatus(documentType + "Edited");
        	// bef�llten Editor f�r das Object includieren
        	request.setAttribute("isNewEditorSource","false");
        	request.setAttribute("mcrid",mcrid);
        	request.setAttribute("type",documentType);
        	request.setAttribute("step","author");
        	request.setAttribute("nextPath",nextPath);
        	request.getRequestDispatcher("/nav?path=~editor-include").forward(request, response);
        	return;
        	
        }
        if ( "WFCommitWorkflowObject".equals(todo) ) {
        	//Object komplett in die DB schieben
        	boolean bSuccess =false;
    		if ( (  	AI.checkPermission(mcrid, "commitdb")
    	             && AI.checkPermission(derivateID,"deletedb")) ) {    			
    		   	bSuccess = WFI.commitWorkflowObject(documentType, mcrid);
    		}
    		if (bSuccess) {
    			// Object hochgeladen
    			WFI.setCommitStatus( mcrid);
    		}
        }
        if ( "WFDeleteWorkflowObject".equals(todo) ) {
        	//Object aus dem WF l�schen
        }
        if ( "WFAddNewDerivateToWorkflowObject".equals(todo) ) {
        	derivateID = WFI.addNewDerivateToWorkflowObject(mcrid, documentType);
        	if (derivateID != null && derivateID.length()>0) {
	       		int fcnt = Integer.parseInt(fileCnt);           		
	       		wfo.setStringVariableValue("fileCnt", Integer.toString(fcnt+1));
        	}
        	todo = "WFAddNewFileToDerivate";
        	
        }
        if ( "WFEditDerivateFromWorkflowObject".equals(todo) ) {
        	//bef�llten Editor f�r das Derivate includieren
        }
        if ( "WFAddNewFileToDerivate".equals(todo) ) {
        	//leeren upload Editor includieren
			String fuhid = new MCRUploadHandlerMyCoRe( mcrid, derivateID, "new", nextPath).getID();
        	request.setAttribute("isNewEditorSource","true");
        	request.setAttribute("uploadID", fuhid);
        	request.setAttribute("mcrid",mcrid);
        	request.setAttribute("type",documentType);
        	request.setAttribute("step","author");
        	request.setAttribute("nextPath",nextPath);
        	request.setAttribute("mcrid2",derivateID);
        	request.getRequestDispatcher("/nav?path=~editor-include").forward(request, response);        	
        	return;
       	}
        if ( "WFRemoveFileFromDerivate".equals(todo) ) {
        	// ein File aus dem Derivate l�schen
        }        
        if ( "WFRemoveDerivateFromWorkflowObject".equals(todo) ) {
            //Anschliessend muss im WF eventuell ein neuer Status gesetzt werden, denn wenn zB. kein Derivate mehr da ist
            //muss wenn es eine Dissertation ist, der Status wieder zur�ckgesetzt werden
        	boolean bSuccess =false;
    		if ( (  	AI.checkPermission(mcrid, "deletewf")
    	             && AI.checkPermission(derivateID,"deletewf")) ) {    			
    		   	bSuccess = WFI.deleteDerivateObject(documentType, mcrid, derivateID);
    		}
    		
           	if ( bSuccess ) {
           		wfo.setWorkflowStatus(documentType + "DocumentRemoved");
           		int fcnt = Integer.parseInt(fileCnt);           		
           		wfo.setStringVariableValue("fileCnt", Integer.toString(fcnt-1));
           		if ( fcnt <= 0 ) 
           			wfo.setWorkflowStatus(documentType + "noDocuments");
           	}
            request.getRequestDispatcher("/nav?path=" + nextPath).forward(request, response);
        	return;
        }         
    }
    


}
