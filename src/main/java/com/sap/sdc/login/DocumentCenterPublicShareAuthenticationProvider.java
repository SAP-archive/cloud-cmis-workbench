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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.StandardAuthenticationProvider;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;

public class DocumentCenterPublicShareAuthenticationProvider extends StandardAuthenticationProvider {

	private static final long serialVersionUID = 1L;

	public static final String MDOCS_SHARE_ID = "com.sap.mcm.share.id";
	public static final String MDOCS_SHARE_PWD = "com.sap.mcm.share.password";

	private String shareId;
	private String sharePassword;

	public DocumentCenterPublicShareAuthenticationProvider() {
		super();
	}

	@Override
	public void setSession(BindingSession session) {
		super.setSession(session);

		shareId = IOUtils.encodeURL((String) session.get(MDOCS_SHARE_ID));
		sharePassword = IOUtils.encodeURL((String) session.get(MDOCS_SHARE_PWD));
	}

	@Override
	public Map<String, List<String>> getHTTPHeaders(String url) {
		Map<String, List<String>> httpHeaders = super.getHTTPHeaders(url);
		if (httpHeaders == null) {
			httpHeaders = new HashMap<String, List<String>>();
		}

		httpHeaders.put("x-public-link", Collections.singletonList(shareId));

		if (sharePassword != null) {
			httpHeaders.put("x-pwd", Collections.singletonList(sharePassword));
		}

		return httpHeaders;
	}
}
