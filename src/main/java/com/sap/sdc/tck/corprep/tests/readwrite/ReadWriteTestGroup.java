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
package com.sap.sdc.tck.corprep.tests.readwrite;

import java.util.Map;

import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTestGroup;
import org.apache.chemistry.opencmis.tck.tests.crud.ChangeTokenTest;
import org.apache.chemistry.opencmis.tck.tests.crud.ContentRangesTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CopyTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateAndDeleteDocumentTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateAndDeleteFolderTest;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateBigDocument;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateDocumentWithoutContent;
import org.apache.chemistry.opencmis.tck.tests.crud.CreateInvalidTypeTest;
import org.apache.chemistry.opencmis.tck.tests.crud.DeleteTreeTest;
import org.apache.chemistry.opencmis.tck.tests.crud.MoveTest;
import org.apache.chemistry.opencmis.tck.tests.crud.NameCharsetTest;
import org.apache.chemistry.opencmis.tck.tests.crud.OperationContextTest;
import org.apache.chemistry.opencmis.tck.tests.crud.PropertyFilterTest;
import org.apache.chemistry.opencmis.tck.tests.crud.SetAndDeleteContentTest;
import org.apache.chemistry.opencmis.tck.tests.crud.UpdateSmokeTest;
import org.apache.chemistry.opencmis.tck.tests.crud.WhitespaceInNameTest;

public class ReadWriteTestGroup extends AbstractSessionTestGroup {

	@Override
	public void init(Map<String, String> parameters) throws Exception {
		super.init(parameters);

		setName("SAP Document Center - Read-Write Test Group");
		setDescription("SDC read-write tests. Please configure a test folder with write access.");

		// basic OpenCMIS tests
		addTest(new CreateAndDeleteFolderTest());
		addTest(new CreateAndDeleteDocumentTest());
		addTest(new CreateBigDocument());
		addTest(new CreateDocumentWithoutContent());
		addTest(new CreateInvalidTypeTest());
		addTest(new NameCharsetTest());
		addTest(new WhitespaceInNameTest());
		addTest(new PropertyFilterTest());
		addTest(new UpdateSmokeTest());
		addTest(new SetAndDeleteContentTest());
		addTest(new ChangeTokenTest());
		addTest(new ContentRangesTest());
		addTest(new CopyTest());
		addTest(new MoveTest());
		addTest(new DeleteTreeTest());
		addTest(new OperationContextTest());
		
		// SDC tests
	}
}
