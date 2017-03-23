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
package com.sap.sdc.tck.corprep.tests;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.UNEXPECTED_EXCEPTION;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.tck.impl.TestParameters;
import org.apache.chemistry.opencmis.tck.tests.query.AbstractQueryTest;

public abstract class AbstractSDCTest extends AbstractQueryTest {

	protected Folder getReadOnlyTestFolder(Session session) {
		String testFolderPath = getParameters().get(TestParameters.DEFAULT_TEST_FOLDER_PARENT);
		if (testFolderPath == null) {
			testFolderPath = TestParameters.DEFAULT_TEST_FOLDER_PARENT_VALUE;
		}

		Folder folder = null;
		try {
			CmisObject cmisObject = session.getObjectByPath(testFolderPath, SELECT_ALL_NO_CACHE_OC);
			if (!(cmisObject instanceof Folder)) {
				addResult(createResult(FAILURE, "Test folder is actually not a folder! Path: " + testFolderPath, true));
			}

			folder = (Folder) cmisObject;
		} catch (CmisBaseException e) {
			addResult(createResult(UNEXPECTED_EXCEPTION,
					"Test folder could not be retrieved! Exception: " + e.getMessage(), e, true));
		}

		return folder;
	}

	protected Document findADocument(Folder folder) {
		for (CmisObject child : folder.getChildren()) {
			if (child instanceof Document) {
				return (Document) child;
			}
		}

		return null;
	}

	protected Folder findAFolder(Folder folder) {
		for (CmisObject child : folder.getChildren()) {
			if (child instanceof Folder) {
				return (Folder) child;
			}
		}

		return null;
	}

	protected boolean runQueryNoException(Session session, String stmt) {
		try {
			for (QueryResult qr : session.query(stmt, false)) {
				qr.getPropertyByQueryName("cmis:name");
			}

			return true;
		} catch (Exception e) {
			addResult(createResult(FAILURE, "The query '" + stmt + "' failed with this exception: " + e.toString(), e,
					false));
			return false;
		}
	}

	protected QueryResult runQuerySingleHit(Session session, String stmt) {
		try {
			QueryResult result = null;
			int count = 0;

			for (QueryResult qr : session.query(stmt, false)) {
				count++;
				result = qr;
			}

			if (count != 1) {
				addResult(createResult(FAILURE, "The query '" + stmt
						+ "' should return exactly one result, but it returned " + count + " results!"));
				return null;
			}

			return result;
		} catch (Exception e) {
			addResult(createResult(FAILURE, "The query '" + stmt + "' failed with this exception: " + e.toString(), e,
					false));
			return null;
		}
	}
}
