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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.QueryStatement;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.tck.CmisTestResult;

import com.sap.sdc.tck.corprep.tests.AbstractSDCTest;

public class SDCSyncTest extends AbstractSDCTest {

	@Override
	public void init(Map<String, String> parameters) {
		super.init(parameters);
		setName("SDC Sync Test");
		setDescription("Checks if sync may work for this repository.");
	}

	@Override
	public void run(Session session) {
		if (supportsQuery(session) && !isFulltextOnly(session)) {

			Folder testFolder = getReadOnlyTestFolder(session);
			Document doc = findADocument(testFolder);
			Folder folder = findAFolder(testFolder);

			if (doc == null) {
				addResult(createResult(WARNING,
						"Test folder does not contain a document, which is required for this test!"));
			} else {
				runObjectIdQuery(session, "SELECT * FROM cmis:document WHERE cmis:objectId = ?", doc.getId());
				runObjectIdQuery(session, "SELECT * FROM cmis:document WHERE cmis:objectId IN (?)", doc.getId());
				runObjectIdQuery(session, "SELECT * FROM cmis:document WHERE cmis:objectId IN (?)", doc.getId(), true);

				QueryStatement stmt = session.createQueryStatement(
						"SELECT * FROM cmis:document WHERE in_tree(?) AND cmis:lastModificationDate >= TIMESTAMP ?");
				stmt.setString(1, testFolder.getId());
				GregorianCalendar dateTime = doc.getLastModificationDate();
				dateTime.add(Calendar.MINUTE, -5);
				stmt.setDateTime(2, dateTime);
				runQueryNoException(session, stmt.toQueryString());
			}

			if (folder == null) {
				addResult(createResult(WARNING,
						"Test folder does not contain a folder, which is required for this test!"));
			} else {
				runObjectIdQuery(session, "SELECT * FROM cmis:folder WHERE cmis:objectId = ?", folder.getId());
				runObjectIdQuery(session, "SELECT * FROM cmis:folder WHERE cmis:objectId IN (?)", folder.getId());
				runObjectIdQuery(session, "SELECT * FROM cmis:folder WHERE cmis:objectId IN (?)", folder.getId(), true);

				QueryStatement stmt = session.createQueryStatement(
						"SELECT * FROM cmis:folder WHERE in_tree(?) AND cmis:lastModificationDate >= TIMESTAMP ?");
				stmt.setString(1, testFolder.getId());
				GregorianCalendar dateTime = folder.getLastModificationDate();
				dateTime.add(Calendar.MINUTE, -5);
				stmt.setDateTime(2, dateTime);
				runQueryNoException(session, stmt.toQueryString());
			}
		} else {
			addResult(createResult(WARNING, "Repository does not support metadata queries! Sync will not work!"));
		}
	}

	private void runObjectIdQuery(Session session, String stmt, String objectId) {
		runObjectIdQuery(session, stmt, objectId, false);
	}

	private void runObjectIdQuery(Session session, String stmt, String objectId, boolean objectIdTwice) {
		QueryStatement queryStmt = session.createQueryStatement(stmt);

		if (objectIdTwice) {
			queryStmt.setString(1, new String[] { objectId, objectId });
		} else {
			queryStmt.setString(1, objectId);
		}

		QueryResult result = runQuerySingleHit(session, queryStmt.toQueryString());

		if (result != null) {
			CmisTestResult failure = createResult(FAILURE,
					"The query '" + queryStmt.toQueryString() + "' returned the wrong object!");
			assertEquals(objectId, result.getPropertyByQueryName("cmis:objectId"), null, failure);
		}
	}
}
