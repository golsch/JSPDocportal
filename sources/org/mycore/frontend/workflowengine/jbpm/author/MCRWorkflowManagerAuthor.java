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
package org.mycore.frontend.workflowengine.jbpm.author;

// Imported java classes
import org.apache.log4j.Logger;
import org.jbpm.context.exe.ContextInstance;
import org.jdom.Element;
import org.mycore.common.JSPUtils;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowAccessRuleEditorUtils;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowConstants;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowManager;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowProcess;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowProcessManager;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowUtils;
import org.mycore.frontend.workflowengine.strategies.MCRAuthorMetadataStrategy;
import org.mycore.frontend.workflowengine.strategies.MCRMetadataStrategy;
import org.mycore.frontend.workflowengine.strategies.MCRWorkflowDirectoryManager;
import org.mycore.services.fieldquery.MCRResults;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserMgr;

/**
 * This class holds methods to manage the workflow file system of MyCoRe.
 * 
 * @author Heiko Helmbrecht, Anja Schaar
 * @version $Revision$ $Date$
 */

public class MCRWorkflowManagerAuthor extends MCRWorkflowManager {

	private static Logger logger = Logger
			.getLogger(MCRWorkflowManagerAuthor.class.getName());

	private static MCRWorkflowManager singleton;

	protected MCRWorkflowManagerAuthor() throws Exception {
		super("author", "author");
		metadataStrategy = new MCRAuthorMetadataStrategy("author");
	}

	/**
	 * Returns the disshab workflow manager singleton.
	 * 
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static synchronized MCRWorkflowManager instance() throws Exception {
		if (singleton == null)
			singleton = new MCRWorkflowManagerAuthor();
		return singleton;
	}

	public long initWorkflowProcess(String initiator, String transitionName)
			throws MCRException {
		MCRWorkflowProcess wfp = createWorkflowProcess(workflowProcessType);
		try {
			wfp.initialize(initiator);
			wfp.save();
			
			MCRUser user = MCRUserMgr.instance().retrieveUser(initiator);
			String email = user.getUserContact().getEmail();
			if (email != null && !email.equals("")) {
				wfp.setStringVariable(
						MCRWorkflowConstants.WFM_VAR_INITIATOREMAIL, email);
			}
			String salutation = user.getUserContact().getSalutation();
			if (salutation != null && !salutation.equals("")) {
				wfp.setStringVariable(
						MCRWorkflowConstants.WFM_VAR_INITIATORSALUTATION,
						salutation);
			}
			wfp.endTask("initialization", initiator, transitionName);
			return wfp.getProcessInstanceID();
		} catch (MCRException ex) {
			logger.error("MCRWorkflow Error, could not initialize the workflow process",ex);
			throw new MCRException(	"MCRWorkflow Error, could not initialize the workflow process");
		} finally {
			if (wfp != null)
				wfp.close();
		}
	}

	public String checkDecisionNode(String decisionNode, ContextInstance ctxI) {
		if (decisionNode.equals("canAuthorBeSubmitted")) {
			if (checkSubmitVariables(ctxI)) {
				return "authorCanBeSubmitted";
			} else {
				return "authorCantBeSubmitted";
			}
		}

		if (decisionNode.equals("canAuthorBeCommitted")) {
			if (checkSubmitVariables(ctxI)) {
				return "authorCanBeCommitted";
			} else {
				return "authorCantBeCommitted";
			}
		}

		if (decisionNode.equals("doesAuthorForUserExist")) {
			
			String userid = (String) ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_INITIATOR);
			MCRResults mcrResult = MCRWorkflowUtils.queryMCRForAuthorByUserid(userid);
			logger.debug("Results found hits:" + mcrResult.getNumHits());
			if (mcrResult.getNumHits() > 0) {
				String createdDocID = mcrResult.getHit(0).getID();
				ctxI.setVariable(
				   MCRWorkflowConstants.WFM_VAR_METADATA_OBJECT_IDS,
				   createdDocID);
				// cannot be used in decision handlers - persistence problems with jbpm
				// wfp.setStringVariable(MCRWorkflowConstants.WFM_VAR_METADATA_OBJECT_IDS, createdDocID);
							
				MCRObject mob = new MCRObject();
				mob.receiveFromDatastore(createdDocID);
				ctxI.setVariable(MCRWorkflowConstants.WFM_VAR_WFOBJECT_TITLE, 
						                     createWFOTitlefromMetadata(mob.createXML().getRootElement().getChild("metadata")));
				// cannot be used in decision handlers - persistence problems with jbpm
				// setWorkflowVariablesFromMetadata(createdDocID, mob.createXML()
				//	.getRootElement().getChild("metadata"), processid);

				return "authorForUserExists_yes";
			} else {
				return "authorForUserExists_no";
			}
		}

		if (decisionNode.equals("canChangesBeCommitted")) {
			if (checkSubmitVariables(ctxI)) {
				return "changesCanBeCommitted";
			} else {
				return "changesCantBeCommitted";
			}
		}
		return null;
	}

	private boolean checkSubmitVariables(ContextInstance ctxI) {
		boolean ret = false;
		try {
			String createdDocID = (String) ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_METADATA_OBJECT_IDS);
			if (createdDocID == null)
				createdDocID = (String) ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_METADATA_OBJECT_IDS);
			String strDocValid = (String) ctxI.getVariable(MCRMetadataStrategy.VALID_PREFIX
							+ createdDocID);
			if (strDocValid != null) {
				if (strDocValid.equals("true")) {
					ret = true;
				}
			}
		} catch (MCRException ex) {
			logger.error("catched error", ex);
		}
		return ret;
	}

	public String createEmptyMetadataObject(ContextInstance ctxI) {
		logger.warn("this is an empty method for workflowtype author");
		return null;
	}

public String createNewAuthor(String userid, ContextInstance ctxI,
			boolean isFillInUserData) {
		
	try {
			MCRObjectID author = this.getNextFreeID(this.mainDocumentType);
			author = authorStrategy.createAuthor(userid, author,
					isFillInUserData, false);
	//		setStringVariable(MCRWorkflowConstants.WFM_VAR_METADATA_OBJECT_IDS, author.getId(), processID);
			setDefaultPermissions(author.getId(), userid, ctxI);
			return author.getId();
		} catch (MCRException ex) {
			logger.error("an error occurred", ex);
		} finally {
		}
		
		return null;
	}

	public boolean commitWorkflowObject(ContextInstance ctxI) {
		try {
			String documentID = (String) ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_METADATA_OBJECT_IDS);
			String documentType = new MCRObjectID(documentID).getTypeId();
			if (!metadataStrategy.commitMetadataObject(documentID,
					MCRWorkflowDirectoryManager.getWorkflowDirectory(documentType))) {
				throw new MCRException("error in committing " + documentID);
			}
			permissionStrategy.setPermissions(documentID, null,	workflowProcessType,ctxI, MCRWorkflowConstants.PERMISSION_MODE_PUBLISH);
			return true;
		} catch (MCRException ex) {
			logger.error("an error occurred", ex);
			ctxI.setVariable("varnameERROR", ex.getMessage());			
		} finally {
			
		}
		return false;
	}

	public boolean removeWorkflowFiles(ContextInstance ctxI) {
		
		String workflowDirectory = MCRWorkflowDirectoryManager
				.getWorkflowDirectory(mainDocumentType);
		try {
			metadataStrategy.removeMetadataFiles(ctxI, workflowDirectory, deleteDir);
			return true;
		} catch (MCRException ex) {
			logger.error("could not delete workflow files", ex);
			ctxI.setVariable("varnameERROR", ex.getMessage());			
		} finally {
		
		}
		return false;
	}

	public void setWorkflowVariablesFromMetadata(String mcrid,	Element metadata, ContextInstance ctxI) {
		
		try {
			ctxI.setVariable(MCRWorkflowConstants.WFM_VAR_WFOBJECT_TITLE, createWFOTitlefromMetadata(metadata));		
		} catch (MCRException ex) {
			logger.error("catched error", ex);
		} finally {
		}
	}

	public long initWorkflowProcessForEditing(String initiator, String mcrid ) throws MCRException {
		if (mcrid != null && MCRObject.existInDatastore(mcrid)) {
			// Store Object in Workflow - Filesystem
			MCRObject mob = new MCRObject();
			mob.receiveFromDatastore(mcrid);
			String type = mob.getId().getTypeId();
			JSPUtils.saveToDirectory(mob, MCRWorkflowDirectoryManager.getWorkflowDirectory(type));
			long processID = initWorkflowProcess(initiator,  "go2DisplayAuthorData");
			MCRWorkflowProcess wfp = MCRWorkflowProcessManager.getInstance().getWorkflowProcess(processID);
			try{
			wfp.setStringVariable(MCRWorkflowConstants.WFM_VAR_METADATA_OBJECT_IDS, mcrid);
			
			MCRWorkflowAccessRuleEditorUtils.setWorkflowVariablesForAccessRuleEditor(mcrid, wfp.getContextInstance());
			setWorkflowVariablesFromMetadata(mcrid, mob.createXML().getRootElement().getChild("metadata"), wfp.getContextInstance());
			setMetadataValid(mcrid, true, wfp.getContextInstance());
			return processID;
			}
			catch(Exception e){
				logger.error("catched exception: ",e);
				return -1;
			}
			finally{
				wfp.close();
				
			}

		} else {
			return -1;
		}
	}
	
	private String createWFOTitlefromMetadata(Element metadata){
		Element name = metadata.getChild("names").getChild("name");
		StringBuffer sbTitle = new StringBuffer("");
		String first = name.getChildTextNormalize("firstname");
		String last = name.getChildTextNormalize("surname");
		String academic = name.getChildTextNormalize("academic");
		String prefix = name.getChildTextNormalize("prefix");
		String fullname = name.getChildTextNormalize("fullname");
		if (fullname != null) {
			sbTitle.append(fullname);
		} else {
			if (academic != null) {
				sbTitle.append(academic);
				sbTitle.append(" ");
			}
			if (first != null) {
				sbTitle.append(first);
				sbTitle.append(" ");
			}
			if (prefix != null) {
				sbTitle.append(prefix);
				sbTitle.append(" ");
			}
			if (last != null) {
				sbTitle.append(last);
				sbTitle.append("");
			}
		}
		return sbTitle.toString();
	}
}
