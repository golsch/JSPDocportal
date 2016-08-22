/*
 * $RCSfile$
 * $Revision: 29729 $ $Date: 2014-04-23 11:28:51 +0200 (Mi, 23 Apr 2014) $
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
 */
package org.mycore.frontend.jsp.stripes.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.log4j.Logger;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.MCRUserInformation;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.frontend.jsp.MCRHibernateTransactionWrapper;
import org.mycore.frontend.jsp.stripes.actions.util.MCRLoginNextStep;
import org.mycore.frontend.jsp.user.MCRExternalUserLogin;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.services.i18n.MCRTranslation;
import org.mycore.user2.MCRRole;
import org.mycore.user2.MCRRoleManager;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

/**
 * This class handles the Login into the system.
 * 
 * The actionBean context exposes the following variables:
 * 
 * userID - the userID; loginOK - boolean, true if successfully logged-in.
 * loginStatus - result of the login-process as string userName - the full name
 * of logged-in user nextSteps - list of MCRLoginNextStep (fields: url, label
 * for next actions ...)
 * 
 * @author Robert
 *
 */
@UrlBinding("/login.action")
public class MCRLoginAction extends MCRAbstractStripesAction implements ActionBean {
	private static Logger LOGGER = Logger.getLogger(MCRLoginAction.class);
	private static String classNameExtUserLogin = MCRConfiguration.instance().getString("MCR.Application.ExternalUserLogin.Class", "").trim();

	ForwardResolution fwdResolution = new ForwardResolution("/content/login.jsp");

	private String userID;
	private String password;
	private boolean loginOK;
	private String loginStatus = "user.login";
	private String userName;
	private List<MCRLoginNextStep> nextSteps = new ArrayList<>();

	@DefaultHandler
	public Resolution defaultRes() {
		HttpServletRequest request = (HttpServletRequest) getContext().getRequest();
		if ("true".equals(request.getParameter("logout"))) {
			return doLogout();
		} else {
			MCRSession mcrSession = MCRServlet.getSession(request);
			MCRUserInformation mcrUserInfo = mcrSession.getUserInformation();
			if (mcrUserInfo != null && !mcrUserInfo.getUserID().equals("gast") && !mcrUserInfo.getUserID().equals("gast")) {
				loginStatus = "user.welcome";
				loginOK = true;
		        try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()){
		            updateData(mcrSession);
		        }
			}
		}
		return fwdResolution;
	}

	public Resolution doLogout() {
		HttpServletRequest request = (HttpServletRequest) getContext().getRequest();
		MCRSession session = MCRServlet.getSession(request);
		String uid = session.getUserInformation().getUserID();
		LOGGER.debug("Log out user " + uid);
		session.setUserInformation(MCRSystemUserInformation.getGuestInstance());
		return fwdResolution;
	}

	public Resolution doLogin() {
		boolean extLoginOk = false;
		boolean mcrLoginOK = false;

		HttpServletRequest request = (HttpServletRequest) getContext().getRequest();
		MCRSession mcrSession = MCRServlet.getSession(request);
	    try (MCRHibernateTransactionWrapper mtw = new MCRHibernateTransactionWrapper()){
			String oldUserID = mcrSession.getUserInformation().getUserID();

			if (userID != null) {
				userID = (userID.trim().length() == 0) ? null : userID.trim();
			}
			if (password != null) {
				password = (password.trim().length() == 0) ? password : password.trim();
			}
			MCRUser mcrUser = null;

			if (userID == null && password == null && !"gast|guest".contains(oldUserID)) {
				loginOK = true;
				loginStatus = "user.incomplete";
				userID = oldUserID;
				updateData(mcrSession);
				return fwdResolution;
			}

			if (userID == null || password == null) {
				loginOK = false;
				LOGGER.debug("ID or Password cannot be empty");
				loginStatus = "user.incomplete";
				return fwdResolution;
			}

			LOGGER.debug("Trying to log in user " + userID);
			if (oldUserID.equals(userID)) {
				LOGGER.debug("User " + userName + " with ID " + userID + " is allready logged in");
				loginOK = true;
				loginStatus = "user.exists";
				return fwdResolution;
			}

			MCRExternalUserLogin extLogin = null;
			if (classNameExtUserLogin.length() > 0) {
				try {
					@SuppressWarnings("unchecked")
					Class<MCRExternalUserLogin> c = (Class<MCRExternalUserLogin>) Class.forName(classNameExtUserLogin);
					extLogin = (MCRExternalUserLogin) c.newInstance();
				} catch (Exception e) {
					// ExceptionClassNotFoundException, IllegalAccessException,
					// InstantiationException
					// do nothing
					LOGGER.debug("Could not load MCRExternalUserLogin: " + classNameExtUserLogin, e);
				}
			}

			if (extLogin != null) {
				// check userID und PW against external user management system
				extLoginOk = extLogin.loginUser(userID, password);
			}

			if (extLoginOk) {
				String mcrUserID = extLogin.retrieveMyCoReUserID(userID, password);
				String mcrPWD = extLogin.retrieveMyCoRePassword(userID, password);
				if (MCRUserManager.exists(mcrUserID)) {
					mcrUser = MCRUserManager.getUser(mcrUserID);
					mcrLoginOK = loginInMyCore(mcrUserID, mcrPWD, mcrSession);
				}
			} else {
				LOGGER.info("No External User Login - check for MyCoRe User");
				mcrLoginOK = loginInMyCore(userID, password, mcrSession);
			}

			// interprete the results
			if (!extLoginOk && mcrLoginOK) {
				loginOK = true;
				loginStatus = "user.welcome";
			}
			if (extLoginOk && mcrLoginOK) {
				// the user exists in external system and MyCoRe -> everything
				// is OK

				mcrSession.setUserInformation(MCRSystemUserInformation.getSuperUserInstance()); // "root;"
				extLogin.updateUserData(userID, "", mcrUser);
				mcrSession.setUserInformation(mcrUser);

				loginOK = true;
				loginStatus = "user.welcome";
			}

			if (extLoginOk && !mcrLoginOK) {
				// the user is regcognized as member of the institution
				// -> login as member (special account with extended read
				// rights)
				String mcrUserID = MCRConfiguration.instance().getString("MCR.Application.ExternalUserLogin.DefaultUser.uid", "gast").trim();
				String mcrPWD = MCRConfiguration.instance().getString("MCR.Application.ExternalUserLogin.DefaultUser.pwd", "gast").trim();
				String oldStatus = loginStatus;
				loginInMyCore(mcrUserID, mcrPWD, mcrSession);
				if ((oldStatus != null) && oldStatus.equals("user.disabled")) {
					loginStatus = "user.disabled_member";
				} else {
					loginStatus = "user.member";
				}
				loginOK = true;
			}
			if (!extLoginOk && !mcrLoginOK) {
				// the user is not allowed
				loginStatus = "user.unknown";
				loginOK = false;
			}
			
			updateData(mcrSession);
			return fwdResolution;
		}
	}

	private boolean loginInMyCore(String mcrUserID, String mcrPassword, MCRSession mcrSession) {
		boolean result = false;
		try {
			MCRUser mcrUser = MCRUserManager.login(mcrUserID, mcrPassword);
			if (mcrUser != null) {
				result = true;
				mcrSession.setUserInformation(mcrUser);
				loginStatus = "user.welcome";
				LOGGER.debug("user " + userID + " logged in ");
			} else {
				if (userID != null) {
					loginStatus = "user.invalid_password";
				}
			}
			updateData(mcrSession);
		} catch (MCRException e) {
			result = false;
			if (e.getMessage().equals("user can't be found in the database")) {
				loginStatus = "user.unknown";
			} else if (e.getMessage().equals("Login denied. User is disabled.")) {
				loginStatus = "user.disabled";
			} else {
				loginStatus = "user.unkwnown_error";
				LOGGER.debug("user.unkwnown_error" + e.getMessage());
			}
		}
		LOGGER.info(loginStatus);
		return result;
	}

	/**
	 * sets userName and nextSteps variables
	 * 
	 * @param mcrSession
	 */
	private void updateData(MCRSession mcrSession) {
		nextSteps.clear();
		userName = "";

		if (loginOK) {
			StringBuffer name = new StringBuffer();
			ResourceBundle messages = MCRTranslation.getResourceBundle("messages", new Locale(mcrSession.getCurrentLanguage()));
			MCRUser mcrUser = MCRUserManager.getCurrentUser();
			userID = mcrUser.getUserID();
			if ("female".equals(mcrUser.getAttributes().get("sex"))) {
				// Frau
				name.append(messages.getString("Editor.Person.gender.female"));
			} else {
				// Herr
				name.append(messages.getString("Editor.Person.gender.male"));
			}
			name.append(" ");
			name.append(mcrUser.getRealName());
			userName = name.toString();

			for (String groupID : mcrUser.getSystemRoleIDs()) {
				MCRRole mcrgroup = MCRRoleManager.getRole(groupID);
				String link = MCRConfiguration.instance().getString("MCR.Application.Login.StartLink." + groupID, "").trim();
				nextSteps.add(new MCRLoginNextStep(MCRFrontendUtil.getBaseURL() + link, mcrgroup.getLabel().getText() + " (" + mcrgroup.getName()+")"));
			}
		}
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isLoginOK() {
		return loginOK;
	}

	public String getLoginStatus() {
		return loginStatus;
	}

	public String getUserName() {
		return userName;
	}

	public List<MCRLoginNextStep> getNextSteps() {
		return nextSteps;
	}
}