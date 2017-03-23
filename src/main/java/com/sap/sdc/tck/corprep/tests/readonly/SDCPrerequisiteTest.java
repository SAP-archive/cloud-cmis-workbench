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
package com.sap.sdc.tck.corprep.tests.readonly;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

public class SDCPrerequisiteTest extends AbstractSessionTest {

	@Override
	public void init(Map<String, String> parameters) {
		super.init(parameters);
		setName("SDC Prerequisite Test");
		setDescription("Checks if the repository provides all basic capabilities.");
	}

	@Override
	public void run(Session session) {
		RepositoryInfo ri = getRepositoryInfo(session);

		if (ri.getCapabilities() == null) {
			addResult(createResult(FAILURE, "Capabilities are not set!"));
		}

		if (ri.getCmisVersion() != CmisVersion.CMIS_1_1) {
			addResult(createResult(WARNING,
					"This is not CMIS 1.1 repository! It is strongly recommended to support CMIS 1.1."));
		}
	}
}
