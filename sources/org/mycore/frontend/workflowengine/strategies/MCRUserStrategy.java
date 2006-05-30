package org.mycore.frontend.workflowengine.strategies;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.mycore.common.MCRUtils;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowProcess;

public abstract class MCRUserStrategy {
	private static Logger logger = Logger.getLogger(MCRUserStrategy.class.getName());
	
	public static final String VALID_PREFIX = "isValid-";
	public static final String VARIABLE_PREFIX = "createdDocID";

	
	/**
	 * returns the documentType for a given metadata strategy
	 * 
	 * @return
	 */
	public abstract String getDocumentType();
	
	/**
     * The method stores the data in a working directory dependent of the
     * type.
     * 
     * @param outxml
     *            the prepared JDOM object
     * @param ID
     *            MCRObject ID of the MCRObject/MCRDerivate/MCRUser
     * @param fullname
     *            the file name where the JDOM was stored.
     */	
	 public abstract void storeMetadata(byte[] outxml,  String ID, String fullname)  throws Exception;	
	
	/**
	 * sets a workflow-process-variable with the name
	 * 	valid-{mcrid} to boolean isValid
	 * 
	 * @param mcrid
	 * @param isValid
	 */
	public abstract void setMetadataValid(String mcrid, boolean isValid, MCRWorkflowProcess wfp);
	
	/**
	 * returns the boolean value of the valid-Flag that was set
	 * via <code>setMetadataValidFlag</code>
	 * @param mcrid
	 * @return true|false
	 */
	public abstract boolean isMetadataValid(String mcrid, MCRWorkflowProcess wfp);
	
	/**
	 * sets some workflow variables with any information from a documents metadata
	 * 		can be used in every workflow type completely different according to the needs
	 * @param mcrid
	 * @param metadata
	 */
	public abstract void setWorkflowVariablesFromMetadata(MCRWorkflowProcess wfp, Element metadata);
	
	/**
	 * is publishing a user metadata object to the database
	 * @param mcrobjid
	 * @param directory
	 * @return
	 */
	public abstract boolean commitUserObject(String mcrobjid, String directory);
	
	/**
	 * is removing a user metadata object from the filesystem
	 * @param mcrobjid
	 * @param directory
	 * @return
	 */
	public abstract boolean removeUserObject(String mcrobjid, String directory);
	
	protected boolean backupMetadataObject(String inputFile, String backupDirectory){
		try{
			File fInputFile = new File(inputFile);
			File fBackupDirectory = new File(backupDirectory);
			MCRUtils.copyStream(new FileInputStream(fInputFile), new FileOutputStream(new File(fBackupDirectory.getAbsolutePath() + File.separator + fInputFile.getName())));
		}catch (Exception ex) {
			logger.error("could not backup Metadata Object", ex);
			return false;
		}
		return true;
		
	}
}
