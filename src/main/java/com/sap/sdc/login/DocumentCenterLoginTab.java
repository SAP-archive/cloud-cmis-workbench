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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.chemistry.opencmis.client.SessionParameterMap;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.impl.ClientVersion;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParser;
import org.apache.chemistry.opencmis.workbench.AbstractSpringLoginTab;
import org.apache.chemistry.opencmis.workbench.ClientHelper;
import org.apache.chemistry.opencmis.workbench.WorkbenchScale;
import org.apache.chemistry.opencmis.workbench.model.ClientSession;

import com.sap.sdc.login.DocumentCenterData.Consumer;
import com.sap.sdc.login.DocumentCenterData.Landscape;
import com.sap.sdc.login.DocumentCenterData.Provider;

public class DocumentCenterLoginTab extends AbstractSpringLoginTab {

	private static final long serialVersionUID = 1L;

	private static final String HEADER_CSRF = "X-CSRF-Token";

	public static final String SYSPROP_DEV = ClientSession.WORKBENCH_PREFIX + "dev";
	public static final String SYSPROP_LANDSCAPE = ClientSession.WORKBENCH_PREFIX + "landscape";
	public static final String SYSPROP_PROVIDER = ClientSession.WORKBENCH_PREFIX + "provider";
	public static final String SYSPROP_CONSUMER = ClientSession.WORKBENCH_PREFIX + "consumer";
	public static final String SYSPROP_URL = ClientSession.WORKBENCH_PREFIX + "url";
	public static final String SYSPROP_BINDING = ClientSession.WORKBENCH_PREFIX + "binding";
	public static final String SYSPROP_CONN_TIMEOUT = ClientSession.WORKBENCH_PREFIX + "connecttimeout";
	public static final String SYSPROP_READ_TIMEOUT = ClientSession.WORKBENCH_PREFIX + "readtimeout";
	public static final String SYSPROP_USER = ClientSession.WORKBENCH_PREFIX + "user";
	public static final String SYSPROP_PASSWORD = ClientSession.WORKBENCH_PREFIX + "password";
	public static final String SYSPROP_AUTHENTICATION = ClientSession.WORKBENCH_PREFIX + "authentication";
	public static final String SYSPROP_LANGUAGE = ClientSession.WORKBENCH_PREFIX + "language";

	private static final Landscape CUSTOM_LANDSCAPE = new Landscape("Custom", null);

	private DocumentCenterData documentCenterData;

	private JTextField urlField;
	private JComboBox<Landscape> landscapeBox;
	private JComboBox<Provider> providerBox;
	private JComboBox<Consumer> consumerBox;
	private JRadioButton bindingAtomButton;
	private JRadioButton bindingBrowserButton;
	private JPasswordField passwordField;
	private JRadioButton authenticationBasicButton;
	private JRadioButton authenticationOAuthButton;
	private JRadioButton authenticationOAuthCodeButton;
	private JButton authenticationGetOAuthCodeButton;

	private JTextField usernameField;

	private JTextField languageField;
	private JFormattedTextField connectTimeoutField;
	private JFormattedTextField readTimeoutField;
	private JFormattedTextField maxChildrenField;

	private String homeFolderId;

	public DocumentCenterLoginTab() {
		super();

		documentCenterData = new DocumentCenterData();
		try {
			documentCenterData.read(System.getProperty(SYSPROP_DEV) != null);
		} catch (FileNotFoundException e) {
			// ignore
		}

		createGUI();
	}

	private void createGUI() {
		setLayout(new SpringLayout());

		urlField = createTextField(this, "URL:", "Enter the Document Center endpoint URL.");
		// urlField.setEditable(true);
		// urlField.setBorder(BorderFactory.createEmptyBorder());
		// urlField.setOpaque(false);

		Color color = UIManager.getColor("Panel.background");
		urlField.setBackground(new Color(color.getRGB()));
		urlField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (landscapeBox.getSelectedItem() == CUSTOM_LANDSCAPE) {
					urlField.setText(buildURL());
				}
			}
		});

		landscapeBox = createLandscapeBox(this, "Landscape:");
		providerBox = createProviderBox(this, "Provider:");
		consumerBox = createConsumerBox(this, "Consumer:");

		ItemListener itemListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if ((authenticationOAuthButton.isSelected() || authenticationGetOAuthCodeButton.isSelected())
							&& bindingAtomButton.isSelected()) {
						bindingBrowserButton.setSelected(true);
					}
					urlField.setText(buildURL());
				}
			}
		};

		createBindingButtons(this);

		bindingAtomButton.addItemListener(itemListener);
		bindingBrowserButton.addItemListener(itemListener);

		usernameField = createTextField(this, "Username:", "Enter user name.");
		usernameField.setText(System.getProperty(SYSPROP_USER, ""));
		if (usernameField.getText().length() == 0) {
			usernameField.setText(System.getProperty("user.name"));
		}

		passwordField = createPasswordField(this, "Password:", "Enter password.");
		passwordField.setText(System.getProperty(SYSPROP_PASSWORD, ""));

		createAuthenticationButtons(this);

		authenticationBasicButton.addItemListener(itemListener);
		authenticationOAuthButton.addItemListener(itemListener);
		authenticationOAuthCodeButton.addItemListener(itemListener);

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

		makeCompactGrid(12);

		String configProvider = System.getProperty(SYSPROP_PROVIDER);
		if (configProvider != null) {
			providerBox.setSelectedItem(configProvider);
		}

		String configConsumer = System.getProperty(SYSPROP_CONSUMER);
		if (configConsumer != null) {
			consumerBox.setSelectedItem(configConsumer);
		}

		urlField.setText("<host>");
		urlField.setText(buildURL());
	}

	private void createBindingButtons(Container pane) {
		JPanel bindingContainer = new JPanel();
		bindingContainer.setLayout(new BoxLayout(bindingContainer, BoxLayout.LINE_AXIS));
		char bc = System.getProperty(SYSPROP_BINDING, "browser").toLowerCase(Locale.ENGLISH).charAt(0);
		boolean atom = (bc == 'a');
		boolean browser = (bc == 'b');
		bindingAtomButton = new JRadioButton("AtomPub", atom);
		bindingBrowserButton = new JRadioButton("Browser", browser);
		ButtonGroup bindingGroup = new ButtonGroup();
		bindingGroup.add(bindingAtomButton);
		bindingGroup.add(bindingBrowserButton);
		bindingContainer.add(bindingAtomButton);
		bindingContainer.add(Box.createRigidArea(WorkbenchScale.scaleDimension(new Dimension(10, 0))));
		bindingContainer.add(bindingBrowserButton);
		JLabel bindingLabel = new JLabel("Binding:", JLabel.TRAILING);

		pane.add(bindingLabel);
		pane.add(createHelp("<html>Select a binding.<br>"
				+ "The <b>Browser binding</b>  is the default and recommended binding.<br>"
				+ "The <b>AtomPub binding</b> is for legacy clients."));
		pane.add(bindingContainer);
	}

	private JComboBox<Landscape> createLandscapeBox(Container pane, String label) {

		Landscape[] landscapes = new Landscape[documentCenterData.getLandscapes().size() + 1];
		landscapes[0] = CUSTOM_LANDSCAPE;
		for (int i = 1; i < landscapes.length; i++) {
			landscapes[i] = documentCenterData.getLandscapes().get(i - 1);
		}

		JComboBox<Landscape> comboBox = new JComboBox<Landscape>(landscapes);
		comboBox.setMaximumRowCount(landscapes.length);

		String landscapeStr = System.getProperty(SYSPROP_LANDSCAPE, "0");
		int landscape = 0;
		try {
			landscape = Integer.parseInt(landscapeStr);
		} catch (NumberFormatException nfe) {
			// ignore
		}

		comboBox.setSelectedIndex(landscape);

		comboBox.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				return super.getListCellRendererComponent(list,
						value == null ? null : ((Landscape) value).getDisplayName(), index, isSelected, cellHasFocus);
			}
		});

		JLabel textLabel = new JLabel(label, JLabel.TRAILING);
		textLabel.setLabelFor(comboBox);

		pane.add(textLabel);
		pane.add(createHelp("<html>Select a <b>HCP landscape</b> or a <b>custom domain</b> or a <b>custom URL</b>."));
		pane.add(comboBox);

		Color color;
		if (comboBox.getSelectedItem() == CUSTOM_LANDSCAPE) {
			color = UIManager.getColor("TextField.background");
		} else {
			color = UIManager.getColor("Panel.background");
		}
		urlField.setBackground(new Color(color.getRGB()));

		comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if (e.getItem() == CUSTOM_LANDSCAPE) {
						Color color = UIManager.getColor("TextField.background");
						urlField.setBackground(new Color(color.getRGB()));
					} else {
						Color color = UIManager.getColor("Panel.background");
						urlField.setBackground(new Color(color.getRGB()));
						urlField.setText(buildURL());
					}

					updateProviderBox((Landscape) e.getItem());
				}
			}
		});

		return comboBox;
	}

	private <T> JComboBox<T> createEditableComboBox(Container pane, String label, String help) {
		JComboBox<T> comboBox = new JComboBox<T>();
		comboBox.setEditable(true);
		JLabel textLabel = new JLabel(label, JLabel.TRAILING);
		textLabel.setLabelFor(comboBox);

		comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				urlField.setText(buildURL());
			}
		});
		comboBox.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				urlField.setText(buildURL());
			}
		});
		comboBox.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				urlField.setText(buildURL());
			}
		});

		pane.add(textLabel);
		if (help != null) {
			pane.add(createHelp(help));
		}
		pane.add(comboBox);

		return comboBox;
	}

	private JComboBox<Provider> createProviderBox(Container pane, String label) {
		JComboBox<Provider> comboBox = createEditableComboBox(pane, label,
				"<html>Select a predefined or enter a custom <b>provider account</b>.");

		comboBox.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				return super.getListCellRendererComponent(list,
						value == null ? null : ((Provider) value).getDisplayName(), index, isSelected, cellHasFocus);
			}
		});

		comboBox.addItemListener(new ItemListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					Object item = e.getItem();
					if (item instanceof Provider) {
						Provider provider = (Provider) item;
						((JComboBox<Provider>) e.getSource()).setSelectedItem(provider.getAlias());
						updateConsumerBox(provider);
					}
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					updateConsumerBox(null);
				}
			}
		});

		return comboBox;
	}

	private JComboBox<Consumer> createConsumerBox(Container pane, String label) {
		JComboBox<Consumer> comboBox = createEditableComboBox(pane, label,
				"<html>Select a predefined or enter a custom <b>consumer account</b>.");

		comboBox.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				return super.getListCellRendererComponent(list,
						value == null ? null : ((Consumer) value).getDisplayName(), index, isSelected, cellHasFocus);
			}
		});

		comboBox.addItemListener(new ItemListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					Object item = e.getItem();
					if (item instanceof Consumer) {
						Consumer consumer = (Consumer) item;
						((JComboBox<Provider>) e.getSource()).setSelectedItem(consumer.getAccount());
					}
				}
			}
		});

		return comboBox;
	}

	private void updateProviderBox(Landscape landscape) {
		providerBox.removeAllItems();
		for (Provider provider : landscape.getProviders()) {
			providerBox.addItem(provider);
		}
	}

	private void updateConsumerBox(Provider provider) {
		consumerBox.removeAllItems();
		if (provider != null) {
			for (Consumer consumer : provider.getConsumers()) {
				consumerBox.addItem(consumer);
			}
		} else {
			consumerBox.setSelectedItem("");
		}
	}

	private void createAuthenticationButtons(Container pane) {
		JPanel authenticationContainer = new JPanel();
		authenticationContainer.setLayout(new BoxLayout(authenticationContainer, BoxLayout.LINE_AXIS));
		boolean standard = (System.getProperty(SYSPROP_AUTHENTICATION, "standard").toLowerCase(Locale.ENGLISH)
				.equals("standard"));
		boolean oauth = (System.getProperty(SYSPROP_AUTHENTICATION, "").toLowerCase(Locale.ENGLISH).equals("oauth"));
		boolean oauthCode = (System.getProperty(SYSPROP_AUTHENTICATION, "").toLowerCase(Locale.ENGLISH).equals("code"));
		authenticationBasicButton = new JRadioButton("Basic Auth", standard);
		authenticationOAuthButton = new JRadioButton("OAuth 2.0 (Bearer Token)", oauth);
		authenticationOAuthCodeButton = new JRadioButton("OAuth 2.0 (Code)", oauthCode);
		authenticationGetOAuthCodeButton = new JButton("Get OAuth Code");
		authenticationGetOAuthCodeButton
				.setToolTipText("<html>If OAuth is configured, the login page is opened in a web browser."
						+ "<br>Follow the login steps until you see the QR code. Then copy the OAuth code from the URL and enter it into the username field.");
		ButtonGroup authenticationGroup = new ButtonGroup();
		authenticationGroup.add(authenticationBasicButton);
		authenticationGroup.add(authenticationOAuthButton);
		authenticationGroup.add(authenticationOAuthCodeButton);
		authenticationContainer.add(authenticationBasicButton);
		authenticationContainer.add(Box.createRigidArea(WorkbenchScale.scaleDimension(new Dimension(10, 0))));
		authenticationContainer.add(authenticationOAuthButton);
		authenticationContainer.add(Box.createRigidArea(WorkbenchScale.scaleDimension(new Dimension(10, 0))));
		authenticationContainer.add(authenticationOAuthCodeButton);
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE)) {
			authenticationContainer.add(Box.createRigidArea(WorkbenchScale.scaleDimension(new Dimension(10, 0))));
			authenticationContainer.add(authenticationGetOAuthCodeButton);

			authenticationGetOAuthCodeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							try {
								// fetch auth infos
								URL authInfoUrl = buildAuthInfoUrl();
								JSONObject oauthConfig = getOAuthConfigutation(authInfoUrl);
								if (oauthConfig == null) {
									throw new CmisConnectionException(
											"No valid OAuth configuration found at " + authInfoUrl.toString() + " .");
								}

								// build URL
								URL authorizationUrl = buildAuthorizationUrl(oauthConfig);
								if (authorizationUrl == null) {
									throw new CmisConnectionException(
											"No valid authorization URL found at " + authInfoUrl.toString() + " .");
								}

								// open web browser
								Desktop.getDesktop().browse(authorizationUrl.toURI());
							} catch (Exception ex) {
								ClientHelper.showError(DocumentCenterLoginTab.this, ex);
							}
						}
					});

					// prepare for code
					authenticationOAuthCodeButton.setSelected(true);
					usernameField.setText("");
					usernameField.requestFocus();
				}
			});
		}

		authenticationBasicButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					usernameField.setEnabled(true);
					passwordField.setEnabled(true);
				}
			}
		});

		ItemListener EnableBothListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					usernameField.setEnabled(true);
					passwordField.setEnabled(false);
				}
			}
		};

		authenticationOAuthButton.addItemListener(EnableBothListener);
		authenticationOAuthCodeButton.addItemListener(EnableBothListener);

		JLabel authenticatioLabel = new JLabel("Authentication:", JLabel.TRAILING);

		pane.add(authenticatioLabel);
		pane.add(createHelp("<html>Select the authentication method.<br>" + "Basic Auth should always work."));
		pane.add(authenticationContainer);
	}

	private String buildURL() {
		Landscape landscape = (Landscape) landscapeBox.getSelectedItem();

		if (landscape == CUSTOM_LANDSCAPE) {
			String urlText = urlField.getText();
			if (urlText.indexOf('/') == -1) {
				// only a hostname
				urlText = "https://" + urlText + buildPath();
			}

			URL url;
			try {
				url = new URL(urlText);

				String protocol = url.getProtocol().toLowerCase(Locale.ENGLISH);
				if (!"http".equals(protocol) && !"https".equals(protocol)) {
					throw new MalformedURLException("not a HTTP protocol: " + url.getProtocol());
				}

				if (url.getPath().isEmpty() || url.getPath().equals("/")) {
					// path is missing -> add it
					url = new URL(url, buildPath());
				}

				return url.toExternalForm();
			} catch (MalformedURLException e) {
				ClientHelper.showError(null, e);
			}

			return urlText;
		}

		StringBuilder sb = new StringBuilder(128);

		String landscapeUrl = landscape.getUrl();

		if (providerBox.getSelectedItem() != null) {
			landscapeUrl = landscapeUrl.replaceAll("\\{provider\\}", providerBox.getSelectedItem().toString());
		}

		if (consumerBox.getSelectedItem() != null) {
			landscapeUrl = landscapeUrl.replaceAll("\\{consumer\\}", consumerBox.getSelectedItem().toString());
		}

		sb.append(landscapeUrl);
		sb.append(buildPath());

		return sb.toString();
	}

	private String buildPath() {
		if (authenticationOAuthButton.isSelected() || authenticationOAuthCodeButton.isSelected()) {
			return "/mcm/oauth";
		} else {
			if (bindingBrowserButton.isSelected()) {
				return "/mcm/b/json";
			} else {
				return "/mcm/b/atom";
			}
		}
	}

	@Override
	public String getTabTitle() {
		return "SAP Document Center";
	}

	@Override
	public Map<String, String> getSessionParameters() {
		String url = buildURL();

		BindingType binding = BindingType.ATOMPUB;
		if (bindingBrowserButton.isSelected()) {
			binding = BindingType.BROWSER;
		}

		String username = usernameField.getText();
		String password = new String(passwordField.getPassword());

		ClientSession.Authentication authentication = ClientSession.Authentication.STANDARD;
		if (authenticationOAuthButton.isSelected()) {
			authentication = ClientSession.Authentication.OAUTH_BEARER;
		}
		if (authenticationOAuthCodeButton.isSelected()) {
			authentication = ClientSession.Authentication.NONE;
		}

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

		SessionParameterMap parameters = ClientSession.createSessionParameters(url, binding, username, password,
				authentication, true, false, true, HEADER_CSRF, locale, connectTimeout, readTimeout);

		if (authenticationOAuthCodeButton.isSelected()) {
			setOAuthParameters(parameters);
		}

		if (maxChildrenField.getValue() instanceof Number) {
			parameters.put(ClientSession.MAX_FOLDER_CHILDREN, ((Number) maxChildrenField.getValue()).toString());
		}

		return parameters;
	}

	private void setOAuthParameters(SessionParameterMap parameters) {
		URL authInfoUrl = buildAuthInfoUrl();
		JSONObject oauthConfig = getOAuthConfigutation(authInfoUrl);

		// check config
		if (oauthConfig == null) {
			throw new CmisConnectionException("No valid OAuth configuratin found at " + authInfoUrl.toString() + " .");
		}

		// get values
		String tokenEntpoint = getStringFromJSON(oauthConfig, "tokenURL");
		String clientId = getStringFromJSON(oauthConfig, "clientId");
		String clientSecret = getStringFromJSON(oauthConfig, "clientSecret");
		String redirectUrl = getStringFromJSON(oauthConfig, "redirectURL");

		// check code
		String code = usernameField.getText().trim();
		if (code.length() == 0) {
			String hint = "";
			URL authorizationUrl = buildAuthorizationUrl(oauthConfig);
			if (authorizationUrl != null) {
				hint = " Request an OAuth code at " + authorizationUrl.toString() + " .";
			}

			throw new CmisConnectionException("No code provided." + hint);
		}

		// turn OAuth on
		parameters.setOAuthAuthentication(tokenEntpoint, clientId, clientSecret, code, redirectUrl);
	}

	private URL buildAuthInfoUrl() {
		try {
			URL cmisUrl = new URL(buildURL());
			return new URL(cmisUrl.getProtocol(), cmisUrl.getHost(), "/mcm/public/rest/v1/settings/auth");
		} catch (Exception e) {
			throw new CmisConnectionException("Could not build authentication info URL: " + e.toString(), e);
		}
	}

	private URL buildAuthorizationUrl(JSONObject oauthConfig) {
		try {
			String authUrl = getStringFromJSON(oauthConfig, "authURL");
			String clientId = getStringFromJSON(oauthConfig, "clientId");
			String redirectUrl = getStringFromJSON(oauthConfig, "redirectURL");

			if (authUrl == null || clientId == null || redirectUrl == null) {
				return null;
			}

			URL url = new URL(authUrl + "?client_id=" + IOUtils.encodeURL(clientId)
					+ "&response_type=code&scope=cmis_all&redirect_uri=" + IOUtils.encodeURL(redirectUrl));

			return url;
		} catch (Exception e) {
			throw new CmisConnectionException("Could not build authorization URL: " + e.toString(), e);
		}
	}

	@SuppressWarnings("unchecked")
	private JSONObject getOAuthConfigutation(URL authInfoUrl) {
		Reader reader = null;

		try {
			// get the OAuth config from server
			HttpURLConnection conn = (HttpURLConnection) authInfoUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(false);
			conn.setAllowUserInteraction(false);
			conn.setUseCaches(false);
			conn.setRequestProperty("User-Agent", ClientVersion.OPENCMIS_USER_AGENT);
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(30000);

			// connect
			conn.connect();
			int respCode = conn.getResponseCode();
			if (respCode != 200) {
				throw new CmisConnectionException("Could not load authentication data from " + authInfoUrl.toString()
						+ " . Response code: " + respCode);
			}

			// parse response
			reader = new InputStreamReader(conn.getInputStream(), "UTF-8");
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(reader);

			if (obj instanceof List) {
				for (Object authEntry : (List<Object>) obj) {
					if (!(authEntry instanceof JSONObject)) {
						continue;
					}

					Object authList = ((JSONObject) authEntry).get("authentication");
					if (!(authList instanceof List)) {
						continue;
					}

					for (Object authType : (List<Object>) authList) {
						if (!(authType instanceof JSONObject)) {
							continue;
						}

						Object type = ((JSONObject) authType).get("type");
						if (!(type instanceof String)) {
							continue;
						}
						if (!type.toString().equals("oauth")) {
							continue;
						}

						return (JSONObject) authType;
					}
				}
			}

			return null;
		} catch (Exception e) {
			throw new CmisConnectionException(
					"Authentiction data from " + authInfoUrl.toString() + " is invalid: " + e.toString(), e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	private String getStringFromJSON(JSONObject json, String name) {
		if (json.containsKey(name)) {
			return json.get(name).toString();
		}

		return null;
	}

	@Override
	public void afterLogin(Session session) {
		homeFolderId = null;

		List<CmisExtensionElement> extensions = session.getRepositoryInfo().getExtensions();
		if (extensions != null) {
			for (CmisExtensionElement ext : extensions) {
				if ("myDocuments".equals(ext.getName()) || "sharing".equals(ext.getName())
						|| "favorites".equals(ext.getName()) || "recycleBinHome".equals(ext.getName())) {
					if (ext.getValue() != null && ext.getValue().length() > 0) {
						homeFolderId = ext.getValue();
						break;
					}
				}
			}
		}
	}

	@Override
	public String getStartFolderId() {
		return homeFolderId;
	}
}
