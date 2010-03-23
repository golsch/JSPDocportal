package org.mycore.frontend.workflowengine.jbpm.publication;

import org.jbpm.graph.exe.ExecutionContext;
import org.mycore.common.MCRException;
import org.mycore.frontend.workflowengine.jbpm.MCRAbstractAction;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowConstants;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowManager;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowManagerFactory;

public class MCRCreateDocumentAction extends MCRAbstractAction {
	
	
	private static final long serialVersionUID = 1L;
	private static MCRWorkflowManager WFM = MCRWorkflowManagerFactory.getImpl("publication");

	public void executeAction(ExecutionContext executionContext) {
		logger.debug("creating empty document");
		String createdDocID = WFM.createEmptyMetadataObject(executionContext.getContextInstance());
		if(createdDocID != null && !createdDocID.equals("")){
			executionContext.setVariable(MCRWorkflowConstants.WFM_VAR_METADATA_OBJECT_IDS, createdDocID);
			executionContext.setVariable(MCRWorkflowConstants.WFM_VAR_ATTACHED_DERIVATES, "");
			executionContext.setVariable(MCRWorkflowConstants.WFM_VAR_DELETED_DERIVATES, "");
		}else{
			String errMsg = "could not create a docID for dissertation";
			logger.error(errMsg);
			throw new MCRException(errMsg);
		}
	}

}