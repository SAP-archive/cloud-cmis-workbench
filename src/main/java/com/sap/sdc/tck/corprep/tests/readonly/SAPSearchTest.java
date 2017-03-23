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

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;

import com.sap.sdc.tck.corprep.tests.AbstractSDCTest;

public class SAPSearchTest extends AbstractSDCTest {

	@Override
	public void init(Map<String, String> parameters) {
		super.init(parameters);
		setName("SDC Search Test");
		setDescription("Performs searches and checks if the repository doesn't respond with an exception.");
	}

	@Override
	public void run(Session session) {
		if (supportsQuery(session)) {
			Folder folder = getReadOnlyTestFolder(session);
			String searchFolderId = folder.getId().replaceAll("'", "\\'");

			boolean works = true;

			works &= runQueryNoException(session, "SAPSEARCH INTREE '" + searchFolderId + "' FORTYPE cmis:document SEARCHTERM test");
			works &= runQueryNoException(session, "SAPSEARCH INTREE '" + searchFolderId + "' FORTYPE cmis:document SEARCHTERM red blue green");
			works &= runQueryNoException(session, "SAPSEARCH INTREE '" + searchFolderId + "' FORTYPE cmis:document SEARCHTERM \"this is a test phrase\"");

			works &= runQueryNoException(session, "SAPSEARCH INTREE '" + searchFolderId + "' FORTYPE cmis:folder SEARCHTERM test");
			works &= runQueryNoException(session, "SAPSEARCH INTREE '" + searchFolderId + "' FORTYPE cmis:folder SEARCHTERM red blue green");
			works &= runQueryNoException(session, "SAPSEARCH INTREE '" + searchFolderId + "' FORTYPE cmis:folder SEARCHTERM \"this is a test phrase\"");

			if (!works) {
				addResult(createResult(WARNING, "At least one test search failed! Search may not work properly!"));
			}
		} else {
			addResult(createResult(WARNING, "Repository does not support queries! Search will not work!"));
		}
	}
}
