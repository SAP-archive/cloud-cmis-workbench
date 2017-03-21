/**
 * Copyright 2013-2017, SAP SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sap.sdc.login;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import javax.swing.JFormattedTextField;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.apache.chemistry.opencmis.client.SessionParameterMap;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.spi.AuthenticationProvider;
import org.apache.chemistry.opencmis.workbench.AbstractSpringLoginTab;
import org.apache.chemistry.opencmis.workbench.model.ClientSession;

public class DocumentCenterPublicShareLoginTab extends AbstractSpringLoginTab {

	private static final long serialVersionUID = 1L;

	private static final String HEADER_CSRF = "X-CSRF-Token";

	public static final String SYSPROP_URL = ClientSession.WORKBENCH_PREFIX + "share.url";
	public static final String SYSPROP_SHARE_PASSWORD = ClientSession.WORKBENCH_PREFIX + "share.password";
	public static final String SYSPROP_CONN_TIMEOUT = ClientSession.WORKBENCH_PREFIX + "connecttimeout";
	public static final String SYSPROP_READ_TIMEOUT = ClientSession.WORKBENCH_PREFIX + "readtimeout";
	public static final String SYSPROP_LANGUAGE = ClientSession.WORKBENCH_PREFIX + "language";

	private JTextField urlField;
	private JPasswordField passwordField;

	private JTextField languageField;
	private JFormattedTextField connectTimeoutField;
	private JFormattedTextField readTimeoutField;
	private JFormattedTextField maxChildrenField;

	private DocumentCenterPublicShareAuthenticationProvider authenticationProvider;

	public DocumentCenterPublicShareLoginTab() {
		super();
		createGUI();
	}

	private void createGUI() {
		setLayout(new SpringLayout());

		urlField = createTextField(this, "Share URL:", "<html>Enter the <b>link to the share</b>.");
		urlField.setText(System.getProperty(SYSPROP_URL, ""));

		passwordField = createPasswordField(this, "Password:",
				"<html>Enter the <b>share password</b>.<br>If the share has no password, leave this field blank.");
		passwordField.setText(System.getProperty(SYSPROP_SHARE_PASSWORD, ""));

		languageField = createTextField(this, "Language:", "<html>Enter a <b>ISO 639 language</b> code.<br>"
				+ "The language code is sent to the server but it may not be used by the server.");
		languageField.setText(System.getProperty(SYSPROP_LANGUAGE, Locale.getDefault().getLanguage()));

		connectTimeoutField = createIntegerField(this, "Connect timeout (secs):",
				"<html>Enter the <b>connect timeout</b> in seconds.<br>This is the time the client waits to connect to the server.");
		try {
			connectTimeoutField.setValue(Long.parseLong(System.getProperty(SYSPROP_CONN_TIMEOUT, "30")));
		} catch (NumberFormatException e) {
			connectTimeoutField.setValue(30);
		}

		readTimeoutField = createIntegerField(this, "Read timeout (secs):",
				"<html>Enter the <b>read timeout</b> in seconds.<br>This is the time the client waits for a response from the server.");
		try {
			readTimeoutField.setValue(Long.parseLong(System.getProperty(SYSPROP_READ_TIMEOUT, "600")));
		} catch (NumberFormatException e) {
			readTimeoutField.setValue(600);
		}

		maxChildrenField = createIntegerField(this, "Max children:",
				"<html><b>Maximum number of children</b> that should be loaded.<br>"
						+ "Enter '0' to disable getChildren calls.");
		try {
			maxChildrenField.setValue(Long.parseLong(System.getProperty(ClientSession.MAX_FOLDER_CHILDREN, "1000")));
		} catch (NumberFormatException e) {
			maxChildrenField.setValue(1000);
		}

		makeCompactGrid(6);
	}

	private String getShareId() throws MalformedURLException {
		URL shareUrl = new URL(urlField.getText().trim());

		String shareId = shareUrl.getQuery();
		if (shareId == null) {
			throw new CmisConnectionException("No share ID!");
		}

		int shareIdIdx = shareId.indexOf("shr=");
		if (shareIdIdx == -1) {
			throw new CmisConnectionException("No share ID!");
		}

		int nextParamIdx = shareId.indexOf('&', shareIdIdx);
		if (nextParamIdx == -1) {
			shareId = shareId.substring(shareIdIdx + 4);
		} else {
			shareId = shareId.substring(shareIdIdx + 4, nextParamIdx);
		}

		return shareId;
	}

	@Override
	public String getTabTitle() {
		return "SAP Document Center Public Share";
	}

	@Override
	public Map<String, String> getSessionParameters() {

		URL shareUrl = null;
		String share = null;
		try {
			shareUrl = new URL(urlField.getText().trim());
			share = getShareId();
		} catch (MalformedURLException e) {
			throw new CmisConnectionException("Invalid URL: " + urlField.getText() + " (" + e.getMessage() + ")", e);
		}

		String port = "";
		if (shareUrl.getPort() > -1) {
			port = ":" + String.valueOf(shareUrl.getPort());
		}
		String url = shareUrl.getProtocol() + "://" + shareUrl.getHost() + port + "/mcm/public/json";

		Locale locale = null;
		String language = languageField.getText().trim();
		if (language.length() > 1 && language.charAt(0) != '-' && language.charAt(0) != '_') {
			int sep1 = language.indexOf('-');
			int sep2 = language.indexOf('_');

			if (sep1 > 0) {
				locale = new Locale(language.substring(0, sep1), language.substring(sep1 + 1));
			} else if (sep2 > 0) {
				locale = new Locale(language.substring(0, sep2), language.substring(sep2 + 1));
			} else {
				locale = new Locale(language);
			}
		}

		long connectTimeout = 0;
		if (connectTimeoutField.getValue() instanceof Number) {
			connectTimeout = ((Number) connectTimeoutField.getValue()).longValue() * 1000;
			if (connectTimeout < 0) {
				connectTimeoutField.setValue(0);
				connectTimeout = 0;
			}
		}

		long readTimeout = 0;
		if (readTimeoutField.getValue() instanceof Number) {
			readTimeout = ((Number) readTimeoutField.getValue()).longValue() * 1000;
			if (readTimeout < 0) {
				readTimeoutField.setValue(0);
				readTimeout = 0;
			}
		}

		SessionParameterMap parameters = ClientSession.createSessionParameters(url, BindingType.BROWSER, null, null,
				ClientSession.Authentication.NONE, true, false, true, HEADER_CSRF, locale, connectTimeout, readTimeout);

		parameters.put(DocumentCenterPublicShareAuthenticationProvider.MDOCS_SHARE_ID, share);

		char[] password = passwordField.getPassword();
		if (password != null && password.length > 0) {
			parameters.put(DocumentCenterPublicShareAuthenticationProvider.MDOCS_SHARE_PWD, new String(password));
		}

		if (maxChildrenField.getValue() instanceof Number) {
			parameters.put(ClientSession.MAX_FOLDER_CHILDREN, ((Number) maxChildrenField.getValue()).toString());
		}
		return parameters;
	}

	@Override
	public AuthenticationProvider getAuthenticationProvider() {
		return authenticationProvider;
	}

	@Override
	public void beforeLoadRepositories() {
		authenticationProvider = new DocumentCenterPublicShareAuthenticationProvider();
	}

	@Override
	public String getStartFolderId() {
		String share = null;
		try {
			share = getShareId();
		} catch (MalformedURLException e) {
			return null;
		}

		return share;
	}
}
