package org.mycore.activiti.workflows.create_object_simple;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class MCRActivitiCreateObjectDelegate implements JavaDelegate {
	  
	  public void execute(DelegateExecution execution) throws Exception {
	    String var = (String) execution.getVariable("input");
	    if(var!=null){
	    var = var.toUpperCase();
	    execution.setVariable("input", var);
	  }
	  }
	  
	}