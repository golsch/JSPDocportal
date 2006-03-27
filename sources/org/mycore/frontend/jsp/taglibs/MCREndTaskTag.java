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
package org.mycore.frontend.jsp.taglibs;

// Imported java classes
import java.io.IOException;

import javax.servlet.jsp.JspException;
import org.apache.log4j.Logger;

import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowEngineManagerFactory;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowEngineManagerInterface;

/**
 * 
 * @author Heiko Helmbrecht
 * @version $Revision$ $Date$
 */

public class MCREndTaskTag extends MCRSimpleTagSupport {
	private static Logger LOGGER = Logger.getLogger(MCREndTaskTag.class.getName());
	private static MCRWorkflowEngineManagerInterface WFI = MCRWorkflowEngineManagerFactory.getDefaultImpl();
	
	private String success;
	private long processID;
	private String taskName;
	
	public void setProcessID(long processID) {
		this.processID = processID;
	}

	public void setSuccess(String success) {
		this.success = success;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	

	public void doTag() throws JspException, IOException {
		try{
			getJspContext().setAttribute(success, new Boolean(WFI.endTask(processID,taskName)), getScope("page"));
		}catch(Exception e){
			LOGGER.error("stacktrace", e);
		}
	}
}

