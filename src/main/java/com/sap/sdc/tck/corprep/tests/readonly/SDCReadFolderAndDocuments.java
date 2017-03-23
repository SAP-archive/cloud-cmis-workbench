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

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.INFO;

import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;

import com.sap.sdc.tck.corprep.tests.AbstractSDCTest;

public class SDCReadFolderAndDocuments extends AbstractSDCTest {
	@Override
	public void init(Map<String, String> parameters) {
		super.init(parameters);
		setName("SDC Read Folder and Documents Test");
		setDescription("Interates over the test folder and checks its children.");
	}

	@Override
	public void run(Session session) {
		Folder testFolder = getReadOnlyTestFolder(session);

		checkChildren(session, testFolder, "Test folder children check");

		boolean foundDocument = false;
		boolean foundFolder = false;

		for (CmisObject child : testFolder.getChildren(SELECT_ALL_NO_CACHE_OC)) {
			addResult(checkObject(session, child, getAllProperties(child),
					"Copied document check. Id: + " + child.getName()));

			if (child instanceof Document) {
				foundDocument = true;
			} else if (child instanceof Folder) {
				foundFolder = true;
			}
		}

		if (!foundDocument) {
			addResult(createResult(INFO, "Test folder does not contain a document!"));
		}

		if (!foundFolder) {
			addResult(createResult(INFO, "Test folder does not contain a folder!"));
		}
	}
}
