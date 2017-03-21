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
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.UIManager;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.workbench.AbstractSpringLoginTab;
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

		landscapeBox = createLandscapeBox(this, "Landscape:");
		providerBox = createProviderBox(this, "Provider:");
		consumerBox = createConsumerBox(this, "Consumer:");

		ItemListener itemListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if (authenticationOAuthButton.isSelected() && bindingAtomButton.isSelected()) {
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

		String landscapeStr = System.getProperty(SYSPROP_LANDSCAPE, landscapes.length > 1 ? "1" : "0");
		int landscape = landscapes.length > 1 ? 1 : 0;
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
		authenticationBasicButton = new JRadioButton("Basic Auth", standard);
		authenticationOAuthButton = new JRadioButton("OAuth 2.0 (Bearer Token)", oauth);
		ButtonGroup authenticationGroup = new ButtonGroup();
		authenticationGroup.add(authenticationBasicButton);
		authenticationGroup.add(authenticationOAuthButton);
		authenticationContainer.add(authenticationBasicButton);
		authenticationContainer.add(Box.createRigidArea(WorkbenchScale.scaleDimension(new Dimension(10, 0))));
		authenticationContainer.add(authenticationOAuthButton);
		JLabel authenticatioLabel = new JLabel("Authentication:", JLabel.TRAILING);

		pane.add(authenticatioLabel);
		pane.add(createHelp("<html>Select the authentication method.<br>" + "Basic Auth should always work."));
		pane.add(authenticationContainer);
	}

	private String buildURL() {
		Landscape landscape = (Landscape) landscapeBox.getSelectedItem();

		if (landscape == CUSTOM_LANDSCAPE) {
			return urlField.getText();
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

		if (authenticationOAuthButton.isSelected()) {
			sb.append("/mcm/oauth");
		} else {
			sb.append("/mcm/b/");

			if (bindingBrowserButton.isSelected()) {
				sb.append("json");
			} else {
				sb.append("atom");
			}
		}

		return sb.toString();
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

		Map<String, String> parameters = ClientSession.createSessionParameters(url, binding, username, password,
				authentication, true, false, true, HEADER_CSRF, locale, connectTimeout, readTimeout);

		if (maxChildrenField.getValue() instanceof Number) {
			parameters.put(ClientSession.MAX_FOLDER_CHILDREN, ((Number) maxChildrenField.getValue()).toString());
		}

		return parameters;
	}

	@Override
	public void afterLogin(Session session) {
		homeFolderId = null;

		List<CmisExtensionElement> extensions = session.getRepositoryInfo().getExtensions();
		if (extensions != null) {
			for (CmisExtensionElement ext : extensions) {
				if ("myDocuments".equals(ext.getName()) || "sharing".equals(ext.getName())) {
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
