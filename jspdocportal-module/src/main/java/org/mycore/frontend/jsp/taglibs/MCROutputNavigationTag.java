/*
 * $RCSfile$
 * $Revision: 16360 $ $Date: 2010-01-06 00:54:02 +0100 (Mi, 06 Jan 2010) $
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

package org.mycore.frontend.jsp.taglibs;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.jsp.MCRHibernateTransactionWrapper;
import org.mycore.frontend.jsp.navigation.model.NavigationItem;
import org.mycore.frontend.jsp.navigation.model.NavigationObject;

/**
 * <p>
 * Tag that renders the navigation.
 * </p>
 * <p>
 * It receives the following attribute:
 * </p>
 * <ul>
 * <li>id: The id of the navigation, that should be printed</li>
 * <li>mode: The mode the is used for the output. Valid values are:
 * <ul>
 * <li>left</li>
 * <li>top</li>
 * <li>toc</li>
 * <li>breadcrumbs</li>
 * </ul>
 * </li>
 * <li>expanded: Should the whole navigation tree be printed or just</li>
 * </ul>
 * 
 * 
 * @author Robert Stephan
 * 
 * 
 */
public class MCROutputNavigationTag extends MCRAbstractNavigationTag {
    private static final List<String> MODES = Arrays
            .asList(new String[] { "left", "side", "top", "breadcrumbs", "toc", "navbar", "mobile", "top-dropdown" });

    private static final String INDENT = "\n       ";

    private String cssClass;

    private String mode;

    private static Logger LOGGER = LogManager.getLogger(MCROutputNavigationTag.class);

    public void doTag() throws JspException, IOException {
        init(mode);
        if (!MODES.contains(mode)) {
            LOGGER.warn("The attribute mode has to be one of these values: " + MODES);
            return;
        }
        JspWriter out = getJspContext().getOut();

        if (mode.equals("left")) {
            printLeftNav(path, nav, cssClass, out);
        }
        
        if (mode.equals("side")) {
        	out.append("\n<nav class=\"ir-nav-side\">");
            printSideNav(path, nav, cssClass, out);
            out.append("\n</nav>");
        }
        
        if (mode.equals("toc")) {
            NavigationItem eNav = findNavItem(nav, path);
            printTOC(eNav,out);
        }

        if (mode.equals("top")) {
            printTopNav(path, nav,out);
        }

        if (mode.equals("top-dropdown")) {
            printTopDropdownNav(path, nav, out);
        }

        if (mode.equals("navbar")) {
            printNavbar(nav, out);
        }

        if (mode.equals("breadcrumbs")) {
            NavigationItem eNav = findNavItem(nav, path);
            printBreadcrumbs(eNav, out);
        }
    }

    /**
     * sets the current mode
     * 
     * @param mode
     *            - the current mode. Allowed values are: left, top,
     *            breadcrumbs, toc
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    /**
     * prints the navigation items as left side main navigation can be called
     * recursively
     * 
     * @param path
     *            - the navigation path (separated by ".")
     * @param currentNode
     *            - the current navigation item
     * @param out
     *            - the JSPOutputWriter
     */
    private void printLeftNav(String[] currentPath, NavigationObject currentNode, String cssClass, JspWriter out) {
        try (MCRHibernateTransactionWrapper htw = new MCRHibernateTransactionWrapper()) {
            List<NavigationItem> printableElements = printableItems(currentNode);
            if (printableElements.isEmpty()) {
                return;
            }
            int level = currentNode.getLevel();
            StringBuffer indentBuffer = new StringBuffer(INDENT);

            for (int j = 0; j < level; j++) {
                indentBuffer.append("    ");
            }
            String indent = indentBuffer.toString();
            if (cssClass != null) {
                out.append(indent).append("  <ul class=\"" + cssClass + "\">");
            } else {
                out.append(indent).append("  <ul>");
            }
            for (NavigationItem el : printableElements) {
                String id = el.getId();
                boolean active = currentPath.length > 0 && currentPath[0].equals(id);

                String msg = retrieveI18N(el.getI18n());
                out.append(indent)
                        .append(" <li id=\"" + retrieveNavPath(el) + "\"" + (active ? " class=\"active\"" : "") + ">");

                out.append(indent);
                String href = el.getHref();
                if (!href.startsWith("http")) {
                    href = MCRFrontendUtil.getBaseURL() + href;
                }
                out.append(" <a target=\"_self\" href=\"" + href + "\">" + msg + "</a>");
                if (expanded || active) {
                    String[] subpath = path;
                    if (path.length > 0) {
                        subpath = Arrays.copyOfRange(path, 1, path.length);
                    }
                    printLeftNav(subpath, el, null, out);
                }
                out.append(indent).append(" </li>");
            }

            out.append(indent).append(" </ul>");
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
    }
    
    /**
     * prints the navigation items as left side main navigation can be called
     * recursively
     * 
     * @param path
     *            - the navigation path (separated by ".")
     * @param currentNode
     *            - the current navigation item
     * @param out
     *            - the JSPOutputWriter
     *
     *
     */
  
    private void printSideNav(String[] currentPath, NavigationObject currentNode, String cssClass, JspWriter out) {
        try (MCRHibernateTransactionWrapper htw = new MCRHibernateTransactionWrapper()) {
            List<NavigationItem> printableElements = printableItems(currentNode);
            if (printableElements.isEmpty()) {
                return;
            }
            int level = currentNode.getLevel();
            StringBuffer indentBuffer = new StringBuffer(INDENT);

            for (int j = 0; j < level; j++) {
                indentBuffer.append("    ");
            }
            String indent = indentBuffer.toString();
            if (cssClass != null) {
                out.append(indent).append("  <ul class=\"" + cssClass + "\">");
            } else {
                out.append(indent).append("  <ul class=\"nav flex-column\">");
            }
            for (NavigationItem el : printableElements) {
                String id = el.getId();
                boolean active = currentPath.length == 1 && currentPath[0].equals(id);
                boolean doExpand = currentPath.length > 0 && currentPath[0].equals(id) && printableItems(el).size()>0 || expanded;

                String msg = retrieveI18N(el.getI18n());
                out.append(indent)
                        .append(" <li id=\"" + retrieveNavPath(el) + "\" class=\"nav-item" + (doExpand ? " ir-nav-item-expanded" : "") + "\">");

                out.append(indent);
                String href = el.getHref();
                if (!href.startsWith("http")) {
                    href = MCRFrontendUtil.getBaseURL() + href;
                }
                out.append(" <a target=\"_self\" class=\"nav-link" + (active ? " active" : "")+ "\" href=\"" + href + "\">" + msg + "</a>");
                if (doExpand) {
                    String[] subpath = path;
                    if (path.length > 0) {
                        subpath = Arrays.copyOfRange(path, 1, path.length);
                    }
                    printSideNav(subpath, el, null, out);
                }
                out.append(indent).append(" </li>");
            }

            out.append(indent).append(" </ul>");
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
    }

    /**
     * prints top nav (horizontal navigation) (only with direct sub items of the
     * given navigation item
     * 
     * @param currentNode
     *            - the current navigation item
     * @param out
     *            - the JSPOutputWriter
     */
    private void printTopNav(String[] currentPath, NavigationObject currentNode, JspWriter out) {
        if (currentNode != null) {

            List<NavigationItem> printableElements = printableItems(currentNode);

            if (!printableElements.isEmpty()) {
                try {
                    if (cssClass != null) {
                        out.append("  <ul class=\"" + cssClass + "\">");
                    } else {
                        out.append("  <ul>");
                    }
                    for (NavigationItem el : printableElements) {
                        boolean active = currentPath.length > 0 && currentPath[0].equals(el.getId());
                        String msg = retrieveI18N(el.getI18n());
                        out.append(INDENT).append("    <li class=\"nav-item\">");
                        String href = el.getHref();
                        if (!href.startsWith("http")) {
                            href = MCRFrontendUtil.getBaseURL() + href;
                        }
                        out.append(INDENT).append("    <a target=\"_self\" class=\"nav-link" + (active ? " active" : "") +"\" href=\"" + href + "\">" + msg + "</a>");
                        out.append(INDENT).append("   </li>");
                    }

                    if (getJspBody() != null) {
                        getJspBody().invoke(out);
                    }
                    out.append(INDENT).append("</ul>");
                    out.flush();
                }

                catch (IOException | JspException e) {
                    LOGGER.error(e);
                }
            }
        }

    }

    /**
     * prints top nav (horizontal navigation) (only with direct sub items of the
     * given navigation item
     * 
     * @param currentNode
     *            - the current navigation item
     * @param out
     *            - the JSPOutputWriter
     */
    private void printTopDropdownNav(String[] currentPath, NavigationObject currentNode, JspWriter out) {
        if (currentNode != null) {
            List<NavigationItem> printableElements = printableItems(currentNode);
            if (!printableElements.isEmpty()) {
                try {
                    if (cssClass != null) {
                        out.append("<ul class=\"" + cssClass + "\">");
                    } else {
                        out.append("<ul>");
                    }
                    int dropdownCounter = 0;
                    for (NavigationItem el : printableElements) {
                        boolean active = currentPath.length > 0 && currentPath[0].equals(el.getId());
                        String msg = retrieveI18N(el.getI18n());
                        List<NavigationItem> printableElementsTmp = printableItems(el);
                        if (printableElementsTmp.size() > 0) {
                            String dropdownId = Integer.toString(dropdownCounter);
                            out.append(INDENT).append("<li class=\"dropdown nav-item\">");
                            out.append(INDENT).append("<a class=\"nav-link dropdown-toggle\" id=\"navbarDropdown" + dropdownId + "\" role=\"button\" data-toggle=\"dropdown\" href=\"#\" aria-haspopup=\"true\" aria-expanded=\"false\">" + msg + "</a>");
                            out.append(INDENT).append("<div class=\"dropdown-menu\" aria-labelledby=\"navbarDropdown" + dropdownId + "\">");
                            for (NavigationItem elTmp : printableElementsTmp) {
                                String msgTmp = retrieveI18N(elTmp.getI18n());
                                String href = elTmp.getHref();
                                if (!href.startsWith("http")) {
                                    href = MCRFrontendUtil.getBaseURL() + href;
                                }
                                out.append(INDENT).append("<a target=\"_self\" class=\"dropdown-item\" href=\"" + href + "\">" + msgTmp + "</a>");
                            }
                            out.append(INDENT).append("</div>");
                            out.append(INDENT).append("</li>");
                            dropdownCounter++;
                        } else {
                            String href = el.getHref();
                            if (!href.startsWith("http")) {
                                href = MCRFrontendUtil.getBaseURL() + href;
                            }
                            out.append(INDENT).append("<li class=\"nav-item\">");
                            out.append(INDENT).append("<a target=\"_self\" class=\"nav-link" + (active ? " active" : "") + "\" href=\"" + href + "\">" + msg + "</a>");
                            out.append(INDENT).append("</li>");
                        }
                    }
                    if (getJspBody() != null) {
                        getJspBody().invoke(out);
                    }
                    out.append(INDENT).append("</ul>");
                    out.flush();
                }
                catch (IOException | JspException e) {
                    LOGGER.error(e);
                }
            }
        }
    }

    /**
     * prints main navigation (horizontal navigation) (only with direct sub items of the
     * given navigation item
     * 
     * @param currentNode
     *            - the current navigation item
     * @param out
     *            - the JSPOutputWriter
     */
    private void printNavbar(NavigationObject currentNode, JspWriter out) {
        printTopNav(path, currentNode, out);
    }

    /**
     * prints the navigation items as left side main navigation can be called
     * recursively
     * 
     * @param path
     *            - the navigation path (separated by ".")
     * @param currentNode
     *            - the current navigation item
     * @param out
     *            - the JSPOutputWriter
     */
    private void printTOC(NavigationObject currentNode, JspWriter out) {
        if (currentNode == null) {
            LOGGER.error("No navigation item found for navigation: " + id + ", path: " + currentPath);
            return;
        }
        List<NavigationItem> printableElements = printableItems(currentNode);
        if (!printableElements.isEmpty()) {
            try {
                if (cssClass != null) {
                    out.append(INDENT).append("<ul class=\"" + cssClass + "\">");
                } else {
                    out.append(INDENT).append("<ul>");
                }
                for (NavigationItem el : printableElements) {
                    String msg = retrieveI18N(el.getI18n());
                    out.append(INDENT).append("<li>");
                    String href = el.getHref();
                    if (!href.startsWith("http")) {
                        href = MCRFrontendUtil.getBaseURL() + href;
                    }
                    out.append(INDENT).append("<a target=\"_self\" href=\"" + href + "\">" + msg + "</a>");
                    if (expanded) {
                        printTOC(el, out);
                    }
                    out.append(INDENT).append("</li>");
                }
                out.append(INDENT).append("</ul>");
                out.flush();
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
    }

    /**
     * prints a breadcrumb navigation for the given node by retrieving its
     * parents.
     * 
     * @param currentNode
     *            - the current navigation item
     * @param out
     *            - the JSPOutputWriter
     */

    private void printBreadcrumbs(NavigationItem currentNode, JspWriter out) {
        if (currentNode == null) {
            return;
        }

        StringBuffer sbOut = new StringBuffer();
        sbOut.append(INDENT).append("</ul>");
        NavigationObject c = currentNode;
        while (c instanceof NavigationItem) {
            String href = ((NavigationItem) c).getHref();
            String msg = retrieveI18N(((NavigationItem) c).getI18n());
            if (!href.startsWith("http")) {
                href = MCRFrontendUtil.getBaseURL() + href;
            }
            if (c == currentNode) {
                sbOut.insert(0, INDENT + "   </li>");
                sbOut.insert(0, INDENT + "      <span>" + msg + "</span>");
                sbOut.insert(0, INDENT + "   <li>");
            } else {
                sbOut.insert(0, INDENT + "   </li>");
                sbOut.insert(0, INDENT + "      <a target=\"_self\" href=\"" + href + "\">" + msg + "</a>");
                sbOut.insert(0, INDENT + "   <li>");
            }
            c = c.getParent();
        }
        sbOut.insert(0, INDENT + "   </li>");
        sbOut.insert(0, INDENT + "      <a target=\"_self\" href=\"" + MCRFrontendUtil.getBaseURL() + "\">"
                + retrieveI18N("Nav.Start") + "</a>");
        sbOut.insert(0, INDENT + "   <li>");

        if (cssClass != null) {
            sbOut.insert(0, INDENT + "<ul class=\"" + cssClass + "\">");
        } else {
            sbOut.insert(0, INDENT + "<ul>");
        }

        try {
            out.append(sbOut.toString());
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

}
