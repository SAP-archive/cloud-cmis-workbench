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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentCenterData {
	private static final String SDC_FILE = "sdc.json";
	private static final String SDC_DEV_FILE = "sdc-dev.json";
	private static final String SDC_PATH = "/sap";

	private static final Logger LOG = LoggerFactory.getLogger(DocumentCenterData.class);

	private List<Landscape> landscapes;

	@SuppressWarnings("unchecked")
	public void read(boolean dev) throws FileNotFoundException {
		landscapes = new ArrayList<DocumentCenterData.Landscape>();

		String sdcFilename = dev ? SDC_DEV_FILE : SDC_FILE;

		InputStream stream = null;
		File sdcFile = null;

		sdcFile = new File(System.getProperty("user.home"), sdcFilename);
		if (sdcFile.isFile()) {
			stream = new FileInputStream(sdcFile);
		}

		if (stream == null) {
			sdcFile = new File(System.getProperty("user.dir"), sdcFilename);
			if (sdcFile.isFile()) {
				stream = new FileInputStream(sdcFile);
			}
		}

		if (stream == null) {
			stream = DocumentCenterData.class.getResourceAsStream("/" + sdcFilename);
		}

		if (stream == null) {
			stream = DocumentCenterData.class.getResourceAsStream(SDC_PATH + "/" + sdcFilename);
		}

		if (stream == null) {
			throw new FileNotFoundException("SDC file not found!");
		}

		try {
			JSONParser parser = new JSONParser();
			Object landscapeList = parser.parse(new InputStreamReader(new BufferedInputStream(stream), "UTF-8"));

			if (!(landscapeList instanceof List)) {
				LOG.error("Cannot read SDC data: Unexpected JSON.");
				return;
			}

			// landscapes
			for (Object landscapeObj : (List<Object>) landscapeList) {
				if (!(landscapeObj instanceof Map)) {
					continue;
				}

				Map<String, Object> landscapeJson = (Map<String, Object>) landscapeObj;

				String landscapeName = getString(landscapeJson, "name", "<unknown>");
				String landscapeUrl = getString(landscapeJson, "url",
						"https://{provider}-{tenant}.<data-center>.hana.ondemand.com");

				Landscape landscape = new Landscape(landscapeName, landscapeUrl);
				landscapes.add(landscape);

				// providers
				Object providersJson = landscapeJson.get("providers");
				if (providersJson instanceof List) {
					for (Object providerObj : (List<Object>) providersJson) {
						if (!(providerObj instanceof Map)) {
							continue;
						}

						Map<String, Object> providerJson = (Map<String, Object>) providerObj;

						String providerName = getString(providerJson, "name", "<unknown>");
						String providerAlias = getString(providerJson, "alias", "<unknown>");

						Provider provider = new Provider(providerName, providerAlias);
						landscape.getProviders().add(provider);

						// consumers
						Object consumersJson = providerJson.get("consumers");
						if (consumersJson instanceof List) {
							for (Object consumerObj : (List<Object>) consumersJson) {
								if (!(consumerObj instanceof Map)) {
									continue;
								}

								Map<String, Object> consumerJson = (Map<String, Object>) consumerObj;

								String consumerName = getString(consumerJson, "name", "<unknown>");
								String consumerAccount = getString(consumerJson, "account", "<unknown>");

								Consumer consumer = new Consumer(consumerName, consumerAccount);
								provider.getConsumers().add(consumer);
							}
						}
					}
				}
			}

		} catch (Exception e) {
			LOG.error("Cannot read SDC data: {}", e.toString(), e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	private String getString(Map<String, Object> json, String key, String def) {
		Object obj = json.get(key);
		if (obj == null) {
			return def;
		} else {
			return obj.toString();
		}
	}

	public List<Landscape> getLandscapes() {
		return landscapes;
	}

	@Override
	public String toString() {
		return (landscapes == null ? "[]" : landscapes.toString());
	}

	static class Landscape {
		private String name;
		private String url;
		private List<Provider> providers;

		public Landscape(String name, String url) {
			this.name = name;
			this.url = url;
			this.providers = new ArrayList<DocumentCenterData.Provider>();
		}

		public String getName() {
			return name;
		}

		public String getUrl() {
			return url;
		}

		public List<Provider> getProviders() {
			return providers;
		}

		public String getDisplayName() {
			return name;
		}

		@Override
		public String toString() {
			return getDisplayName() + " Providers: " + providers.toString();
		}
	}

	static class Provider {
		private String name;
		private String alias;
		private List<Consumer> consumers;

		public Provider(String name, String alias) {
			this.name = name;
			this.alias = alias;
			this.consumers = new ArrayList<DocumentCenterData.Consumer>();
		}

		public String getName() {
			return name;
		}

		public String getAlias() {
			return alias;
		}

		public List<Consumer> getConsumers() {
			return consumers;
		}

		public String getDisplayName() {
			return name + " (" + alias + ")";
		}

		@Override
		public String toString() {
			return getDisplayName() + " Consumers: " + consumers.toString();
		}
	}

	static class Consumer {
		private String name;
		private String account;

		public Consumer(String name, String account) {
			this.name = name;
			this.account = account;
		}

		public String getName() {
			return name;
		}

		public String getAccount() {
			return account;
		}

		public String getDisplayName() {
			return name + " (" + account + ")";
		}

		@Override
		public String toString() {
			return getDisplayName();
		}
	}
}
