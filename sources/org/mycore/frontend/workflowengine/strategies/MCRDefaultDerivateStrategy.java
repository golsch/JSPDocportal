package org.mycore.frontend.workflowengine.strategies;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.jbpm.context.exe.ContextInstance;
import org.mycore.common.JSPUtils;
import org.mycore.common.MCRException;
import org.mycore.common.MCRUtils;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.frontend.workflowengine.jbpm.MCRWorkflowConstants;


public class MCRDefaultDerivateStrategy extends MCRDerivateStrategy {
	private static Logger logger = Logger.getLogger(MCRDefaultDerivateStrategy.class.getName());

	public boolean removeDerivates(ContextInstance ctxI, String saveDirectory, String backupDirectory){
		try{
			String sderids = (String)ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_ATTACHED_DERIVATES);
			if (sderids == null || sderids.length() == 0) {
				// there exist no derivates
				return true;
			}
			List attachedDerivates = Arrays.asList(sderids.split(","));
			for (Iterator it = attachedDerivates.iterator(); it.hasNext();) {
				String derivateID = (String) it.next();
				if ( derivateID != null && derivateID.length()> 0)
					deleteDerivateObject(ctxI,saveDirectory, backupDirectory, null, derivateID, false);
			}
			ctxI.setVariable(MCRWorkflowConstants.WFM_VAR_ATTACHED_DERIVATES,"");
			return true;
		}catch(MCRException ex){
			logger.error("could not remove derivate", ex);
		}
		return false;
	}
	
	public boolean deleteDerivateFile(ContextInstance ctxI, String documentDirectory, String backupDirectory, String metadataObjectId, String derivateObjectId, boolean mustWorkflowVarBeUpdated, String filename) {
		String derivateFileName = documentDirectory + SEPARATOR + filename;
		File derFile = new File(derivateFileName);
		
		if(derFile.isFile()){
			logger.debug("deleting file " + derFile.getName());
			derFile.delete();
		}else{
			if ( !derFile.exists()) {
				logger.warn(derivateObjectId +  " not exist's - do nothing.");					
			} else {
				logger.warn(derFile.getName() + " is not a file, did not delete it");
				return false;
			}
		}
		
		if(mustWorkflowVarBeUpdated){
			String fcnt = (String)ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_FILECNT);
			try {
				int cnt = Integer.parseInt(fcnt);
				ctxI.setVariable(MCRWorkflowConstants.WFM_VAR_FILECNT, String.valueOf( --cnt) );	
			} catch ( NumberFormatException nonum ) {
				ctxI.setVariable(MCRWorkflowConstants.WFM_VAR_FILECNT, "0");
			}			
		}
		
		return false;
	}
	
	public boolean deleteDerivateObject(ContextInstance ctxI, String documentDirectory, String backupDirectory, String metadataObjectId, String derivateObjectId, boolean mustWorkflowVarBeUpdated){
		try{
			String derivateDirectory = documentDirectory + SEPARATOR + derivateObjectId;
			String derivateFileName = documentDirectory +  SEPARATOR + derivateObjectId + ".xml" ;

			File derDir = new File(derivateDirectory);
			File derFile = new File(derivateFileName);
			
			
			if(derDir.isDirectory()) {
				
				logger.debug("deleting directory " + derDir.getName());
				JSPUtils.recursiveDelete(derDir);
				if(mustWorkflowVarBeUpdated){
					String oldFC = (String)ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_FILECNT);
					int iFC = 0;
					if ( derDir.list() != null )
						iFC = Math.abs(Integer.parseInt(oldFC) - derDir.list().length);
					ctxI.setVariable(MCRWorkflowConstants.WFM_VAR_FILECNT,String.valueOf(iFC)) ;
				}
			}else{
				if (!derDir.exists()) {
					logger.warn(derivateObjectId + " not exist's - do nothing.");					
				} else {
					logger.warn(derDir.getName() + " is not a directory, did not delete it");
					return false;
				}
			}
			if(derFile.isFile()){
				logger.debug("deleting file " + derFile.getName());
				derFile.delete();
			}else{
				if ( !derFile.exists()) {
					logger.warn(derivateObjectId +  " not exist's - do nothing.");					
				} else {
					logger.warn(derFile.getName() + " is not a file, did not delete it");
					return false;
				}
			}
			if(mustWorkflowVarBeUpdated){
				String newDerivates = ((String)ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_ATTACHED_DERIVATES)).replaceAll(derivateObjectId + ",*","");
				ctxI.setVariable(MCRWorkflowConstants.WFM_VAR_ATTACHED_DERIVATES, newDerivates);
				
				String deledetDerIDs = (String)ctxI.getVariable(MCRWorkflowConstants.WFM_VAR_DELETED_DERIVATES);
				deledetDerIDs += "," + derivateObjectId;
				ctxI.setVariable(MCRWorkflowConstants.WFM_VAR_DELETED_DERIVATES, deledetDerIDs);
				
			}
			
		}catch(Exception ex){
			logger.error("problems in deleting", ex);
			return false;
		}
		return true;
	}	
	
	
	public void saveFiles(List files, String dirname, ContextInstance ctxI, String newLabel) throws MCRException {
		logger.debug("!! You Enters the saveFiles-DUMMY implementation, must be implemented in subclasses, for workflow-specific file checks");
		
		// save the files
		boolean bUpdateDerivate=false;
		ArrayList ffname = new ArrayList();
		String mainfile = "";
		for (int i = 0; i < files.size(); i++) {
			FileItem item = (FileItem) (files.get(i));
			String fname = item.getName().trim();
			Matcher mat = filenamePattern.matcher(fname);
			while(mat.find()){
				fname = mat.group(1);
			}
			fname.replace(' ', '_');
			// IE Explorer Bug, the name includes the whole path
			if (fname.indexOf("/") >0)
				fname = fname.substring(fname.lastIndexOf("/")+1);
			if (fname.indexOf("\\") >0 )
				fname = fname.substring(fname.lastIndexOf("\\")+1);

			ffname.add(fname);
			try{
				File fout = new File(dirname, fname);
				InputStream fin = item.getInputStream();				
				FileOutputStream fouts = new FileOutputStream(fout);
				MCRUtils.copyStream(fin, fouts);
				fin.close();
				fouts.flush();
				fouts.close();
				logger.info("Data object stored under " + fout.getName());
			}catch(Exception ex){
				String errMsg = "could not sotre data object " + fname;
				logger.error(errMsg, ex);
				throw new MCRException(errMsg);
			}
		}
					
		MCRDerivate der = new MCRDerivate();
		der.setFromURI(dirname + ".xml");
		
		if (der.getDerivate().getInternals().getMainDoc().equals("#####")) {
			if ((mainfile.length() == 0) && (ffname.size() > 0)) {
				mainfile = (String) ffname.get(0);
				der.getDerivate().getInternals().setMainDoc(mainfile);
				bUpdateDerivate=true;
			}
		}
		if ( newLabel != null && newLabel.length()>0 ) {
			der.setLabel(newLabel);
			bUpdateDerivate=true;
		}		
		try {
			if(bUpdateDerivate){
				byte[] outxml = MCRUtils.getByteArray(der.createXML());
				try {
					FileOutputStream out = new FileOutputStream(dirname	+ ".xml");
					out.write(outxml);
					out.flush();
				} catch (IOException ex) {
					logger.error(ex.getMessage());
					logger.error("Exception while store to file " + dirname
							+ ".xml", ex);
					throw ex;
				}
			}
		} catch (Exception e) {
			String msgErr = "Can't open file " + dirname + ".xml"; 
			logger.error(msgErr, e);
			throw new MCRException(msgErr);
		}
	}
}
