/*
 * Copyright 2019, 2020 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.uu.ub.cora.javaclient;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterFactory;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterFactory;
import se.uu.ub.cora.javaclient.apptoken.AppTokenClientFactory;
import se.uu.ub.cora.javaclient.cora.http.ApptokenBasedClientDependencies;
import se.uu.ub.cora.javaclient.doubles.AppTokenClientFactorySpy;
import se.uu.ub.cora.javaclient.doubles.RestClientFactorySpy;
import se.uu.ub.cora.javaclient.rest.RestClientFactory;

public class ApptokenBasedClientDependenciesTest {

	private ApptokenBasedClientDependencies dependencies;
	private AppTokenClientFactory appTokenClientFactory;
	private RestClientFactory restClientFactory;
	private ClientDataToJsonConverterFactory dataToJsonConverterFactory;
	private JsonToClientDataConverterFactory jsonToDataConverterFactory;

	@BeforeMethod
	public void setUp() {
		appTokenClientFactory = new AppTokenClientFactorySpy();
		restClientFactory = new RestClientFactorySpy();
		dataToJsonConverterFactory = new DataToJsonConverterFactorySpy();
		jsonToDataConverterFactory = new JsonToDataConverterFactorySpy();
		String userId = "someUserId";
		String appToken = "someApptoken";
		dependencies = new ApptokenBasedClientDependencies(appTokenClientFactory, restClientFactory,
				dataToJsonConverterFactory, jsonToDataConverterFactory, userId, appToken);

	}

	@Test
	public void testDependencies() {
		assertEquals(dependencies.appTokenClientFactory, appTokenClientFactory);
		assertEquals(dependencies.restClientFactory, restClientFactory);
		assertEquals(dependencies.dataToJsonConverterFactory, dataToJsonConverterFactory);
		assertEquals(dependencies.jsonToDataConverterFactory, jsonToDataConverterFactory);
		assertEquals(dependencies.userId, "someUserId");
		assertEquals(dependencies.appToken, "someApptoken");
	}

	@Test
	public void testDependenciesWithAuthToken() {
		String authToken = "345345-345345-34565748";

		dependencies = new ApptokenBasedClientDependencies(appTokenClientFactory, restClientFactory,
				dataToJsonConverterFactory, jsonToDataConverterFactory, "someUserId",
				"someApptoken", "345345-345345-34565748");

		assertEquals(dependencies.appTokenClientFactory, appTokenClientFactory);
		assertEquals(dependencies.restClientFactory, restClientFactory);
		assertEquals(dependencies.dataToJsonConverterFactory, dataToJsonConverterFactory);
		assertEquals(dependencies.jsonToDataConverterFactory, jsonToDataConverterFactory);
		assertEquals(dependencies.userId, "someUserId");
		assertEquals(dependencies.appToken, "someApptoken");
		assertEquals(dependencies.authToken, authToken);
	}
}
