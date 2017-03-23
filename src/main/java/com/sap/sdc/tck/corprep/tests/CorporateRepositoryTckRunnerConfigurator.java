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

import org.apache.chemistry.opencmis.tck.runner.AbstractRunner;
import org.apache.chemistry.opencmis.workbench.AbstractTckRunnerConfigurator;

import com.sap.sdc.tck.corprep.tests.readonly.ReadOnlyTestGroup;
import com.sap.sdc.tck.corprep.tests.readwrite.ReadWriteTestGroup;

public class CorporateRepositoryTckRunnerConfigurator extends AbstractTckRunnerConfigurator {

	@Override
	public void configureRunner(AbstractRunner runner) throws Exception {
		runner.addGroup(new ReadOnlyTestGroup());
		runner.addGroup(new ReadWriteTestGroup());
		runner.loadDefaultTckGroups();
	}

}