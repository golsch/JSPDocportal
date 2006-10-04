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

package org.mycore.frontend.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.parsers.bool.MCRCondition;
import org.mycore.services.fieldquery.MCRQuery;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;
import org.mycore.services.fieldquery.MCRSearchServlet;

/**
 * This servlet executes queries and presents result pages.
 * 
 * @author Anja Schaar
 * 
 */
public class MCRJSPSearchServlet extends MCRSearchServlet {
    private static final long serialVersionUID = 1L;

    private String resultlistType = "simple";
    
    public void doGetPost(MCRServletJob job) throws IOException, ServletException {
        String mode = job.getRequest().getParameter("mode");
        
        if ("resort".equals(mode))
        	resortQuery(job.getRequest(), job.getResponse());
        else if ("refine".equals(mode))
        	refineQuery(job.getRequest(), job.getResponse());
        else if ("renew".equals(mode))
        	renewQuery(job.getRequest(), job.getResponse());
        else {
        	super.doGetPost(job);
        }
    }

    // this calls the editor of the searchmask with the inputfield
    private void refineQuery(HttpServletRequest req, HttpServletResponse res) throws IOException,ServletException {
    	String furl = "/nav?path=";    
		String id = req.getParameter("id");
		String editormask = req.getParameter("mask");
		MCRSession session = MCRSessionMgr.getCurrentSession();
		
		if ( editormask.startsWith("~searchstart-class")) {
			// this comes from the browserclass and we have no searchmask
			String browseuri  = session.BData.getUri();
			furl +=editormask+"&actUriPath="+browseuri;
		} else if ( editormask.startsWith("~searchstart-index")) {
				// this comes from the indexbrowser and we have no searchmask
				furl +=editormask; 
		} else {
		   // we must set the session, cause in the next request for the editor call we need the right 
			// session to get the query from the cache
			furl += editormask+"&sourceid="+id+"&session="+session.getID();
		}
		this.getServletContext().getRequestDispatcher(furl).forward(req, res);
    }
    
    // this calls the editor start address of the searchmask with the inputfield or the classbrowser start adresse
    private void renewQuery(HttpServletRequest req, HttpServletResponse res) throws IOException ,ServletException {
		String editormask = req.getParameter("mask");
    	String furl = "/nav?path="+editormask;    
		this.getServletContext().getRequestDispatcher(furl).forward(req, res);
    }    
    
    //this mode comes from the resort form in the resultlist
    private void resortQuery(HttpServletRequest req, HttpServletResponse res) throws IOException {
    	// the id of the query
		String id = req.getParameter("id");
		Document query = (Document) (getCache(getResortKey()).get(id));
		Document origquery = (Document) (getCache(getQueriesKey()).get(id));
		MCRCondition cond = (MCRCondition) (getCache(getConditionsKey()).get(id));
		
		Element sortBy = new Element("sortBy");
		int i = 1;
		for ( i = 1; i < 4; i++) {
			if (req.getParameter("field" + i) != null && !req.getParameter("field" + i).equals("")) {
				Element sortField = new Element("field");
				sortField.setAttribute("name",req.getParameter("field" + i));
				String order = (req.getParameter("order" + i) != null) ?
						req.getParameter("order" + i) : "ascending" ;
				sortField.setAttribute("order",order);
				sortBy.addContent(sortField);
			} else {break;}
		}
		
		if ( i != 1) {
			query.getRootElement().removeChild("sortBy");
			query.getRootElement().addContent(sortBy);
		}
		
        // Execute query
        long start = System.currentTimeMillis();
        MCRResults result = MCRQueryManager.search(MCRQuery.parseXML(query));
        long qtime = System.currentTimeMillis() - start;
        LOGGER.debug("MCRJSPSearchServlet total query time: " + qtime);

        String npp = query.getRootElement().getAttributeValue("numPerPage", "0");

        // Store query and results in cache
        getCache(getResultsKey()).put(result.getID(), result);
        getCache(getResortKey()).put(result.getID(), query);
        getCache(getQueriesKey()).put(result.getID(), origquery); 
        getCache(getConditionsKey()).put(result.getID(), cond);
        
        // Redirect browser to first results page
        sendRedirect(req, res, result.getID(), npp);

    }
    
    /** 
     *  Forwards the document to the output
     *  @author A.Schaar
     *  @see its from mycore and overwritten here 
     */
    protected void forwardRequest(HttpServletRequest req, HttpServletResponse res, Document jdom) throws IOException, ServletException {
    	if ( "results".equalsIgnoreCase(jdom.getRootElement().getName()) ) {
    		String path = "/nav?path=";

    		//<mcr:results xmlns:mcr="http://www.mycore.org/" id="1iljqgz8zqp6merg8xiel" 
            // Show incoming result document
            if (LOGGER.isDebugEnabled()) {
                XMLOutputter out = new XMLOutputter(org.jdom.output.Format.getPrettyFormat());
                LOGGER.debug(out.outputString(jdom));
            }
    		String id = jdom.getRootElement().getAttributeValue("id");
    		String mask = jdom.getRootElement().getAttributeValue("mask");
    		Document query = (Document) (getCache(getResortKey()).get(id));
    		req.setAttribute("query", query);
    		req.setAttribute("results", jdom);
    		
    		String[] maskarray = mask.split("-");
    		
    		if ( maskarray.length > 0 )
    			 resultlistType = maskarray[1];
    		else resultlistType = "simple";

   			path += "~searchresult-" + resultlistType;
    		this.getServletContext().getRequestDispatcher(path).forward(req, res);	
    	}
    	else {
    		// reload the searchmask with in the query
    		super.forwardRequest(req,res,jdom);
    	}
    }
    
    /** 
     *  Redirect browser to results page
     *  @author A.Schaar
     *  @see its overwritten in jspdocportal 
     */
    protected void sendRedirect( HttpServletRequest req, HttpServletResponse res, String id, String numPerPage) throws IOException {
	    // Redirect browser to first results page
    	MCRSession session = MCRSessionMgr.getCurrentSession();
	    String url = "MCRJSPSearchServlet?mode=results&id=" + id + "&numPerPage=" + numPerPage + "&session="+session.getID();
	    res.sendRedirect(res.encodeRedirectURL(url));
    }
}
