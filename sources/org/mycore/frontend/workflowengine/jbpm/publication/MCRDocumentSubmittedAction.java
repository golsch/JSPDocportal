package org.mycore.frontend.workflowengine.jbpm.publication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.exe.ExecutionContext;
import org.mycore.common.MCRException;
import org.mycore.frontend.workflowengine.jbpm.MCRAbstractAction;
import org.mycore.frontend.workflowengine.jbpm.MCRJbpmWorkflowBase;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowConstants;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowManager;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowManagerFactory;
import org.mycore.user.MCRUser;
import org.mycore.user.MCRUserMgr;

public class MCRDocumentSubmittedAction extends MCRAbstractAction{
	
	String lockedVariables;

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(MCRDocumentSubmittedAction.class);

	public void executeAction(ExecutionContext executionContext) throws MCRException {
		logger.debug("locking workflow variables and setting the access control to the editor mode");
		ContextInstance contextInstance = executionContext.getContextInstance();

		List ids = new ArrayList();
		ids.addAll(Arrays.asList(((String)contextInstance.getVariable("attachedDerivates")).split(",")));
		ids.add(contextInstance.getVariable("createdDocID"));

		String initiator = contextInstance.getVariable(MCRWorkflowConstants.WFM_VAR_INITIATOR).toString();
		MCRUser user = MCRUserMgr.instance().retrieveUser(initiator);
		long processID = contextInstance.getProcessInstance().getId();
		String workflowType = MCRJbpmWorkflowBase.getWorkflowProcessType(processID);
		MCRWorkflowManager wfm = MCRWorkflowManagerFactory.getImpl(workflowType);
		
		for (Iterator it = ids.iterator(); it.hasNext();) {
			String id = (String) it.next();
			 						//(mcrid, userid, wftype, mode)
			wfm.permissionStrategy.setPermissions(id, user.getID(), workflowType,contextInstance, MCRWorkflowConstants.PERMISSION_MODE_EDITING);
		}
	}
}
