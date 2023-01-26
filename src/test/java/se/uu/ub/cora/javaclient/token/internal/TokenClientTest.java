/*
 * Copyright 2018 Uppsala University Library
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
package se.uu.ub.cora.javaclient.token.internal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.httphandler.spies.HttpHandlerFactorySpy;
import se.uu.ub.cora.httphandler.spies.HttpHandlerSpy;
import se.uu.ub.cora.javaclient.cora.CoraClientException;
import se.uu.ub.cora.javaclient.token.TokenClient;

public class TokenClientTest {
	private static final String EXAMPLE_AUTHTOKEN = "a1acff95-5849-4e10-9ee9-4b192aef17fd";
	HttpHandlerFactorySpy httpHandlerFactorySpy;
	HttpHandlerSpy httpHandlerSpy;
	private AppTokenCredentials appTokenCredentials = new AppTokenCredentials(
			"http://localhost:8080/apptokenverifier/", "someUserId",
			"02a89fd5-c768-4209-9ecc-d80bd793b01e");
	private TokenClient tokenClient;
	private AuthTokenCredentials authTokenCredentials = new AuthTokenCredentials(
			"http://localhost:8080/apptokenverifier/", EXAMPLE_AUTHTOKEN);

	@BeforeMethod
	public void setUp() {
		httpHandlerFactorySpy = new HttpHandlerFactorySpy();
		httpHandlerSpy = new HttpHandlerSpy();
		httpHandlerSpy.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 201);
		httpHandlerSpy.MRV.setDefaultReturnValuesSupplier("getResponseText",
				() -> "{\"children\":[{\"name\":\"id\",\"value\":\"" + EXAMPLE_AUTHTOKEN + "\"}"
						+ ",{\"name\":\"validForNoSeconds\",\"value\":\"600\"}],\"name\":\"authToken\"}");

		httpHandlerFactorySpy.MRV.setDefaultReturnValuesSupplier("factor", () -> httpHandlerSpy);
	}

	private void createClientUsingAuthToken() {
		tokenClient = TokenClientImp.usingHttpHandlerFactoryAndAuthToken(httpHandlerFactorySpy,
				authTokenCredentials);
	}

	private void createClientUsingApptoken() {
		tokenClient = TokenClientImp.usingHttpHandlerFactoryAndAppToken(httpHandlerFactorySpy,
				appTokenCredentials);
	}

	@Test
	public void testHttpHandlerSetupCorrectly() throws Exception {
		createClientUsingApptoken();
		tokenClient.getAuthToken();

		String expectedUrl = "http://localhost:8080/apptokenverifier/rest/apptoken/someUserId";
		httpHandlerFactorySpy.MCR.assertParameters("factor", 0, expectedUrl);

		HttpHandlerSpy httpHandlerSpy = (HttpHandlerSpy) httpHandlerFactorySpy.MCR
				.getReturnValue("factor", 0);
		httpHandlerSpy.MCR.assertParameters("setRequestMethod", 0, "POST");
		httpHandlerSpy.MCR.assertNumberOfCallsToMethod("setRequestMethod", 1);

		httpHandlerSpy.MCR.assertParameters("setOutput", 0, appTokenCredentials.appToken());
	}

	@Test
	public void testGetAuthToken() throws Exception {
		createClientUsingApptoken();
		String authToken = tokenClient.getAuthToken();
		assertEquals(authToken, EXAMPLE_AUTHTOKEN);
	}

	@Test
	public void testGetAuthTokenTwiceIsOnlyCreatedOnceOnServer() throws Exception {
		createClientUsingApptoken();
		String authToken = tokenClient.getAuthToken();
		String authToken2 = tokenClient.getAuthToken();

		httpHandlerFactorySpy.MCR.assertNumberOfCallsToMethod("factor", 1);
		HttpHandlerSpy httpHandlerSpy = (HttpHandlerSpy) httpHandlerFactorySpy.MCR
				.getReturnValue("factor", 0);
		httpHandlerSpy.MCR.assertNumberOfCallsToMethod("setOutput", 1);

		assertSame(authToken, authToken2);
	}

	@Test(expectedExceptions = CoraClientException.class, expectedExceptionsMessageRegExp = ""
			+ "Could not create authToken")
	public void testGetAuthTokenNotOk() {
		createClientUsingApptoken();
		httpHandlerSpy.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 400);

		tokenClient.getAuthToken();
	}

	@Test
	public void testHttpHandlerSetupCorrectlyUsingAuthToken() throws Exception {
		createClientUsingAuthToken();

		assertSame(((TokenClientImp) tokenClient).onlyForTestGetHttpHandlerFactory(),
				httpHandlerFactorySpy);
		httpHandlerFactorySpy.MCR.assertNumberOfCallsToMethod("factor", 0);

	}

	@Test
	public void testGetAuthTokenUsingAuthToken() throws Exception {
		createClientUsingAuthToken();

		String authToken = tokenClient.getAuthToken();
		assertEquals(authToken, EXAMPLE_AUTHTOKEN);
	}
}
