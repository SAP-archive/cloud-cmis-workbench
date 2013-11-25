/**
 * Copyright 2013, SAP AG
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
package com.sap.mcm;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.UIManager;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.spi.AuthenticationProvider;
import org.apache.chemistry.opencmis.workbench.AbstractSpringLoginTab;
import org.apache.chemistry.opencmis.workbench.model.ClientSession;

public class MobileDocsLoginTab extends AbstractSpringLoginTab {

    private static final long serialVersionUID = 1L;

    public static final String SYSPROP_LANDSCAPES = ClientSession.WORKBENCH_PREFIX + "landscapes";
    public static final String SYSPROP_TENANT = ClientSession.WORKBENCH_PREFIX + "tenant";
    public static final String SYSPROP_URL = ClientSession.WORKBENCH_PREFIX + "url";
    public static final String SYSPROP_BINDING = ClientSession.WORKBENCH_PREFIX + "binding";
    public static final String SYSPROP_USER = ClientSession.WORKBENCH_PREFIX + "user";
    public static final String SYSPROP_PASSWORD = ClientSession.WORKBENCH_PREFIX + "password";

    private static final String CUSTOM_TEXT = "<custom URL>";

    private JTextField urlField;
    private JComboBox landscapeBox;
    private JTextField tenandField;
    private JRadioButton bindingAtomButton;
    private JRadioButton bindingBrowserButton;
    private JTextField usernameField;
    private JPasswordField passwordField;

    private MobileDocsAuthenticationProvider authenticationProvider;

    public MobileDocsLoginTab() {
        super();
        createGUI();
    }

    private void createGUI() {
        setLayout(new SpringLayout());

        urlField = createTextField(this, "URL:");

        Color color = UIManager.getColor("Panel.background");
        urlField.setBackground(new Color(color.getRGB()));

        landscapeBox = createLandscapeBox(this, "Landscape:");
        landscapeBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (e.getItem().equals(CUSTOM_TEXT)) {
                        Color color = UIManager.getColor("TextField.background");
                        urlField.setBackground(new Color(color.getRGB()));
                    } else {
                        Color color = UIManager.getColor("Panel.background");
                        urlField.setBackground(new Color(color.getRGB()));
                        urlField.setText(buildURL());
                    }
                }
            }
        });

        tenandField = createTextField(this, "Customer account:");
        tenandField.setText(System.getProperty(SYSPROP_TENANT, ""));

        tenandField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                urlField.setText(buildURL());
            }
        });
        tenandField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                urlField.setText(buildURL());
            }
        });

        createBindingButtons(this);

        ItemListener itemListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    urlField.setText(buildURL());
                }
            }
        };

        bindingAtomButton.addItemListener(itemListener);
        bindingBrowserButton.addItemListener(itemListener);

        usernameField = createTextField(this, "Username:");
        usernameField.setText(System.getProperty(SYSPROP_USER, ""));
        if (usernameField.getText().length() == 0) {
            usernameField.setText(System.getProperty("user.name"));
        }

        passwordField = createPasswordField(this, "Password:");
        passwordField.setText(System.getProperty(SYSPROP_PASSWORD, ""));

        makeCompactGrid(this, 6, 2, 5, 10, 5, 5);

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
        bindingContainer.add(Box.createRigidArea(new Dimension(10, 0)));
        bindingContainer.add(bindingBrowserButton);
        JLabel bindingLabel = new JLabel("Binding:", JLabel.TRAILING);

        pane.add(bindingLabel);
        pane.add(bindingContainer);
    }

    private JComboBox createLandscapeBox(Container pane, String label) {
        ArrayList<String> landscapes = new ArrayList<String>();
        String landscapesProperty = System.getProperty(SYSPROP_LANDSCAPES);
        if (landscapesProperty != null && landscapesProperty.trim().length() > 0) {
            for (String s : landscapesProperty.split(",")) {
                landscapes.add(s);
            }
        } else {
            landscapes.add("smd.hana.ondemand.com");
            landscapes.add("smd.us1.hana.ondemand.com");
            landscapes.add(CUSTOM_TEXT);
        }

        JComboBox comboBox = new JComboBox(landscapes.toArray(new String[landscapes.size()]));
        JLabel textLabel = new JLabel(label, JLabel.TRAILING);
        textLabel.setLabelFor(comboBox);

        pane.add(textLabel);
        pane.add(comboBox);

        return comboBox;
    }

    private String buildURL() {
        String tenant = tenandField.getText().trim();
        String landscape = (String) landscapeBox.getSelectedItem();

        if (landscape.equals(CUSTOM_TEXT)) {
            return urlField.getText();
        }

        StringBuilder sb = new StringBuilder();

        if (landscape.startsWith("localhost")) {
            sb.append("http://");
            sb.append(landscape);
        } else {
            sb.append("https://");
            if (tenant.length() == 0) {
                sb.append(landscape);
            } else {
                int x = landscape.indexOf('.');
                if (x > 0) {
                    sb.append(landscape.substring(0, x));
                    sb.append('-');
                    sb.append(tenant);
                    sb.append(landscape.substring(x));
                } else {
                    sb.append(landscape);
                }
            }
        }

        sb.append("/mcm/b/");

        if (bindingBrowserButton.isSelected()) {
            sb.append("json");
        } else {
            sb.append("atom");
        }

        return sb.toString();
    }

    @Override
    public String getTabTitle() {
        return "SAP Mobile Documents";
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

        Map<String, String> parameters = ClientSession.createSessionParameters(url, binding, username, password,
                ClientSession.Authentication.STANDARD, true, false, true);

        return parameters;
    }

    @Override
    public AuthenticationProvider getAuthenticationProvider() {
        return authenticationProvider;
    }

    @Override
    public void beforeLoadRepositories() {
        authenticationProvider = new MobileDocsAuthenticationProvider();
    }

    @Override
    public void afterLogin(Session session) {
        List<CmisExtensionElement> extensions = session.getRepositoryInfo().getExtensions();
        if (extensions != null) {
            for (CmisExtensionElement ext : extensions) {
                if (ext.getName().equals("token")) {
                    authenticationProvider.setToken(ext.getValue());
                }
            }
        }
    }
}