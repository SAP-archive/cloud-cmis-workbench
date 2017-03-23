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

import java.util.Map;

import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTestGroup;
import org.apache.chemistry.opencmis.tck.tests.basics.RepositoryInfoTest;
import org.apache.chemistry.opencmis.tck.tests.basics.RootFolderTest;
import org.apache.chemistry.opencmis.tck.tests.query.InvalidQueryTest;
import org.apache.chemistry.opencmis.tck.tests.query.QuerySmokeTest;
import org.apache.chemistry.opencmis.tck.tests.types.BaseTypesTest;

public class ReadOnlyTestGroup extends AbstractSessionTestGroup {

	@Override
	public void init(Map<String, String> parameters) throws Exception {
		super.init(parameters);

		setName("SAP Document Center - Read-Only Test Group");
		setDescription("SDC read-only tests. Please configure a test folder with documents and folders.");

		// basic OpenCMIS tests
		addTest(new RepositoryInfoTest());
		addTest(new RootFolderTest());
		addTest(new BaseTypesTest());
		addTest(new QuerySmokeTest());
		addTest(new InvalidQueryTest());

		// SDC tests
		addTest(new SDCPrerequisiteTest());
		addTest(new SDCReadFolderAndDocuments());
		addTest(new SAPSearchTest());
		addTest(new SDCSyncTest());
	}
}
