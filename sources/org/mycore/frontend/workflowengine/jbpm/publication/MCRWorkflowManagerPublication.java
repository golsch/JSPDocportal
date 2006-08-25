/**
 * $RCSfile$
 * $Revision$ $Date$
 *
 * This file is part of ** M y C o R e **
 * Visit our homepage at http://www.mycore.de/ for details.
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
 * along with this program, normally in the file license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 *
 **/

// package
package org.mycore.frontend.workflowengine.jbpm.publication;

// Imported java classes
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jbpm.context.exe.ContextInstance;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.mycore.common.JSPUtils;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.classifications.MCRCategoryItem;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowAccessRuleEditorUtils;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowConstants;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowManager;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowProcess;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowProcessManager;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowUtils;
import org.mycore.frontend.workflowengine.strategies.MCRMetadataStrategy;
import org.mycore.frontend.workflowengine.strategies.MCRWorkflowDirectoryManager;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserMgr;

/**
 * This class holds methods to manage the workflow file system of MyCoRe.
 * 
 * @author Heiko Helmbrecht, Anja Schaar
 * @version $Revision$ $Date$
 */

public class MCRWorkflowManagerPublication extends MCRWorkflowManager{
	
	
	private static Logger logger = Logger.getLogger(MCRWorkflowManagerPublication.class.getName());
	private static MCRWorkflowManager singleton;
	
	protected MCRWorkflowManagerPublication() throws Exception {
		super("document", "publication");
		this.derivateStrategy = new MCRDocumentDerivateStrategy();
	}

	
	/**
	 * Returns the disshab workflow manager singleton.
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static synchronized MCRWorkflowManager instance() throws Exception {
		if (singleton == null)
			singleton = new MCRWorkflowManagerPublication();
		return singleton;
	}
	
	
	
	public long initWorkflowProcess(String initiator, String transitionName) throws MCRException {
			MCRWorkflowProcess wfp = createWorkflowProcess(workflowProcessType);
			try{
				wfp.initialize(initiator);
				wfp.save();
				MCRUser user = MCRUserMgr.instance().retrieveUser(initiator);
				String email = user.getUserContact().getEmail();
				if(email != null && !email.equals("")){
					wfp.setStringVariable(MCRWorkflowConstants.WFM_VAR_INITIATOREMAIL, email);
				}
				String salutation = user.getUserContact().getSalutation();
				if(salutation != null && !salutation.equals("")){
					wfp.setStringVariable(MCRWorkflowConstants.WFM_VAR_INITIATORSALUTATION, salutation);
				}
				wfp.setStringVariable(MCRWorkflowConstants.WFM_VAR_FILECNT, "0");
				
				wfp.endTask("initialization", initiator, transitionName);
				
				return wfp.getProcessInstanceID();
			}catch(MCRException ex){
				logger.error("MCRWorkflow Error, could not initialize the workflow process", ex);
				throw new MCRException("MCRWorkflow Error, could not initialize the workflow process");
			}finally{
				if(wfp != null)
					wfp.close();
			}				
	}
	
	
	public long initWorkflowProcessForEditing(String initiator, String mcrid ){
		if (mcrid != null && MCRObject.existInDatastore(mcrid)) {
			// Store Object in Workflow - Filesystem
			MCRObject mob = new MCRObject();
			mob.receiveFromDatastore(mcrid);
			String type = mob.getId().getTypeId();
			Document job = mob.createXML();
			String pubType="";
            Iterator it = job.getDescendants( new ElementFilter("type"));
            if ( it.hasNext() )    {
			       Element el = (Element) it.next();
			       pubType = el.getAttributeValue("categid");
		           if ( pubType.indexOf(".")>0)
		        	   pubType = pubType.substring(0,pubType.indexOf("."));
			}			
            
			String atachedDerivates = JSPUtils.saveToDirectory(mob, MCRWorkflowDirectoryManager.getWorkflowDirectory(type));
			
			long processID = initWorkflowProcess(initiator, "go2processEditInitialized");
			
			MCRWorkflowProcess wfp = MCRWorkflowProcessManager.getInstance().getWorkflowProcess(processID);
			String urn = this.identifierStrategy.getUrnFromDocument(mcrid);			
            wfp.setStringVariable(MCRWorkflowConstants.WFM_VAR_METADATA_PUBLICATIONTYPE,pubType);
			wfp.setStringVariable(MCRWorkflowConstants.WFM_VAR_METADATA_OBJECT_IDS, mcrid);
			wfp.setStringVariable(MCRWorkflowConstants.WFM_VAR_RESERVATED_URN, urn);	
			wfp.setStringVariable(MCRWorkflowConstants.WFM_VAR_ATTACHED_DERIVATES, atachedDerivates);
			wfp.setStringVariable(MCRWorkflowConstants.WFM_VAR_DELETED_DERIVATES, "");

			int filecnt =  (atachedDerivates.split("_derivate_")).length;
			wfp.setStringVariable("fileCnt", String.valueOf(filecnt));
			
			
			MCRWorkflowAccessRuleEditorUtils.setWorkflowVariablesForAccessRuleEditor(mcrid, wfp.getContextInstance());
		
			setWorkflowVariablesFromMetadata(mcrid, mob.createXML().getRootElement().getChild("metadata"), wfp.getContextInstance());
			setMetadataValid(mcrid, true, wfp.getContextInstance());
			wfp.close();
			return processID;

		} else {
			return -1;
		}
	}
	
	public String checkDecisionNode(String decisionNode, ContextInstance ctxI) {
		if(decisionNode.equals("canDocumentBeSubmitted")){
			if(checkSubmitVariables(ctxI)){
				return "documentCanBeSubmitted";
			}else{
				return "documentCantBeSubmitted";
			}
		}else if(decisionNode.equals("canDocumentBeCommitted")){
			if(checkSubmitVariables(ctxI)){
				return "go2wasCommitmentSuccessful";
			}else{
				return "go2sendBackToDocumentCreated";
			}
		}
		return null;
	}

	private boolean checkSubmitVariables(ContextInstance ctxI){
		try{
//			String authorID = (String) ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_AUTHOR_IDS);
			String reservatedURN = (String) ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_RESERVATED_URN);
			String createdDocID = (String) ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_METADATA_OBJECT_IDS);
			String attachedDerivates = (String) ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_ATTACHED_DERIVATES);
			if(		!MCRWorkflowUtils.isEmpty(reservatedURN) 
				&& 	!MCRWorkflowUtils.isEmpty(createdDocID)
				&&	!MCRWorkflowUtils.isEmpty(attachedDerivates)	){
				
				String strDocValid = (String) ctxI.getVariable(MCRMetadataStrategy.VALID_PREFIX + createdDocID );
				if(strDocValid != null ){
					if(strDocValid.equals("true") ){
						return true;
					}
				}
			}
		}catch(MCRException ex){
			logger.error("catched error", ex);
		}finally{

		}			
		return false;		
	}	
	
		
	public String createEmptyMetadataObject(ContextInstance ctxI){
		
		try{
			MCRObjectID nextFreeId = getNextFreeID(metadataStrategy.getDocumentType());
			String initiator = (String) ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_INITIATOR);
			String publicationType = (String) ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_METADATA_PUBLICATIONTYPE);			
			String saveDirectory = MCRWorkflowDirectoryManager.getWorkflowDirectory(mainDocumentType);
			Map identifiers = new HashMap();
			identifiers.put(MCRWorkflowConstants.KEY_IDENTIFER_TYPE_URN, (String) ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_RESERVATED_URN));
			if(metadataStrategy.createEmptyMetadataObject(false,null,null, 
					nextFreeId, initiator, identifiers, publicationType, saveDirectory) ){
				
				permissionStrategy.setPermissions(nextFreeId.toString(), initiator,	getWorkflowProcessType(), ctxI, MCRWorkflowConstants.PERMISSION_MODE_DEFAULT );
				return nextFreeId.toString();
			}
		}catch(MCRException ex){
			logger.error("could not create empty metadata object", ex);
		}finally{
		
		}
		return null;
	}	
	
	public void saveFiles(List files, String dirname, long pid, String newLabel) throws MCRException {		
		MCRWorkflowProcess wfp = MCRWorkflowProcessManager.getInstance().getWorkflowProcess(pid);
		try{
			derivateStrategy.saveFiles(files, dirname, wfp.getContextInstance(), newLabel);
		}catch(MCRException ex){
			
		}finally{
			wfp.close();
		}
	}
	
	public boolean commitWorkflowObject(ContextInstance ctxI){
		boolean bSuccess = false;
		
		try{
			String documentID = (String) ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_METADATA_OBJECT_IDS);
			String documentType = new MCRObjectID(documentID).getTypeId();
			bSuccess = metadataStrategy.commitMetadataObject(documentID, MCRWorkflowDirectoryManager.getWorkflowDirectory(documentType));
			
			List deletedDerIDs = Arrays.asList(((String) ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_DELETED_DERIVATES)).split(","));
			for (Iterator it = deletedDerIDs.iterator(); it.hasNext();) {
				String derivateID = (String) it.next();
				if ( derivateID != null && derivateID.length() > 0 ) {
					bSuccess &= derivateStrategy.deleteDeletedDerivates(derivateID);
				}
			}
			
			List derivateIDs = Arrays.asList(((String)ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_ATTACHED_DERIVATES)).split(","));
			for (Iterator it = derivateIDs.iterator(); it.hasNext();) {
				String derivateID = (String) it.next();
				if ( derivateID != null && derivateID.length() > 0 ) {
					bSuccess &= derivateStrategy.commitDerivateObject(derivateID, MCRWorkflowDirectoryManager.getWorkflowDirectory(documentType));
					permissionStrategy.setPermissions(derivateID, null,	workflowProcessType, ctxI, MCRWorkflowConstants.PERMISSION_MODE_PUBLISH);
				}
			}
			
			// readrule wird auf true gesetzt
			permissionStrategy.setPermissions(documentID, null,	workflowProcessType, ctxI, MCRWorkflowConstants.PERMISSION_MODE_PUBLISH);
			bSuccess = true;
		}catch(MCRException ex){
			logger.error("an error occurred", ex);
			ctxI.setVariable("varnameERROR", ex.getMessage());						
		}finally{
		
		}		
		return bSuccess;
	}
	
	public boolean removeWorkflowFiles(ContextInstance ctxI){
		boolean bSuccess = false;
		String workflowDirectory = MCRWorkflowDirectoryManager.getWorkflowDirectory(mainDocumentType);
		try{
			bSuccess = metadataStrategy.removeMetadataFiles(ctxI, workflowDirectory, deleteDir);
			bSuccess &= derivateStrategy.removeDerivates(ctxI,workflowDirectory, deleteDir);
		}catch(MCRException ex){
			logger.error("could not delete workflow files", ex);
			ctxI.setVariable("varnameERROR", ex.getMessage());						
		}finally{
		
		}
		return bSuccess;
	}
	
	public void setWorkflowVariablesFromMetadata(String mcrid, Element metadata, ContextInstance ctxI){
		try{
			StringBuffer sbTitle = new StringBuffer("");
			Iterator it = metadata.getDescendants(new ElementFilter("title"));
			if( it.hasNext()){
				//nur din ersten Titelsatz!
				Element title = (Element)it.next();
				sbTitle.append(title.getText());				
			}
			ctxI.setVariable("wfo-title", sbTitle.toString());	
			
			String publicationType = (String) ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_METADATA_PUBLICATIONTYPE);			
			if ( publicationType != null) {
				String clid = MCRConfiguration.instance().getString("MCR.ClassificationID.Type");
				MCRCategoryItem clItem = MCRCategoryItem.getCategoryItem(clid,publicationType);
				String label = clItem.getDescription(MCRSessionMgr.getCurrentSession().getCurrentLanguage());
				ctxI.setVariable("wfo-type", label);
			}
			
			
		}catch(MCRException ex){
			logger.error("catched error", ex);
		}finally{
			
		}			
	}
	
}
