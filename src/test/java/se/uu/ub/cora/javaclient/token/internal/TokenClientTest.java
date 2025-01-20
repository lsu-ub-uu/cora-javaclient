/*
 * Copyright 2018, 2025 Uppsala University Library
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

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterProvider;
import se.uu.ub.cora.clientdata.spies.JsonToClientDataConverterFactorySpy;
import se.uu.ub.cora.clientdata.spies.JsonToClientDataConverterSpy;
import se.uu.ub.cora.httphandler.spies.HttpHandlerFactorySpy;
import se.uu.ub.cora.httphandler.spies.HttpHandlerSpy;
import se.uu.ub.cora.javaclient.AppTokenCredentials;
import se.uu.ub.cora.javaclient.AuthTokenCredentials;
import se.uu.ub.cora.javaclient.data.DataClientException;
import se.uu.ub.cora.javaclient.token.TokenClient;

public class TokenClientTest {
	private static final String EXAMPLE_AUTHTOKEN_FIRST = "first-5849-4e10-9ee9-4b192aef17fd";
	private static final String EXAMPLE_AUTHTOKEN_SECOND = "second-5849-4e10-9ee9-4b192aef17fd";
	HttpHandlerFactorySpy httpHandlerFactorySpy;
	HttpHandlerSpy httpHandlerSpy;
	HttpHandlerSpy httpHandlerSpy2;
	private AppTokenCredentials appTokenCredentials = new AppTokenCredentials(
			"http://localhost:8080/login/rest/", "someLoginId",
			"02a89fd5-c768-4209-9ecc-d80bd793b01e");
	private TokenClient tokenClient;
	private AuthTokenCredentials authTokenCredentials = new AuthTokenCredentials(
			"http://localhost:8080/login/rest/", EXAMPLE_AUTHTOKEN_FIRST);
	private JsonToClientDataConverterFactorySpy jsonToDataConverterFactory;
	private JsonToClientDataConverterSpy jsonToClientDataConverterSpy;

	@BeforeMethod
	public void setUp() {
		// new ClientDataAuthTokenSpy();
		// jsonToClientDataConverterSpy = new JsonToClientDataConverterSpy();
		// jsonToClientDataConverterSpy.MRV.setDefaultReturnValuesSupplier("toInstance", );

		jsonToDataConverterFactory = new JsonToClientDataConverterFactorySpy();
		JsonToClientDataConverterProvider.setJsonToDataConverterFactory(jsonToDataConverterFactory);

		httpHandlerFactorySpy = new HttpHandlerFactorySpy();
		httpHandlerSpy = new HttpHandlerSpy();
		httpHandlerSpy.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 201);
		httpHandlerSpy2 = new HttpHandlerSpy();
		httpHandlerSpy2.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 201);

		String authTokenFirst = getAuthTokenStringUsingToken(EXAMPLE_AUTHTOKEN_FIRST);
		String authTokenSecond = getAuthTokenStringUsingToken(EXAMPLE_AUTHTOKEN_SECOND);

		httpHandlerSpy.MRV.setDefaultReturnValuesSupplier("getResponseText", () -> authTokenFirst);
		httpHandlerSpy2.MRV.setDefaultReturnValuesSupplier("getResponseText",
				() -> authTokenSecond);

		httpHandlerFactorySpy.MRV.setReturnValues("factor",
				List.of(httpHandlerSpy, httpHandlerSpy2),
				"http://localhost:8080/login/rest/apptoken");
	}

	private String getAuthTokenStringUsingToken(String token) {
		String authTokenFirstAsJson = """
				{
					"data":
						{"children":[
							{"name":"token", "value":"%s"},
							{"name": "validUntil", "value": "300"},
							{"name": "renewUntil", "value": "200"},
							{"name": "userId", "value": "someUserId"},
							{"name": "loginId", "value": "someLoginId"},
							{"name": "firstName", "value": "someFirstName"},
							{"name": "lastName", "value": "someLastName"}
						],
						"name":"authToken"},
					"actionLinks": {
						"renew": {
							"requestMethod": "POST",
							"rel": "renew",
							"url": "http://localhost:38180/login/rest/authToken/someTokenId",
							"accept": "application/vnd.uub.authToken+json"
						},
						"delete": {
							"requestMethod": "DELETE",
							"rel": "delete",
							"url": "http://localhost:38180/login/rest/authToken/someTokenId"
						}
					}
				}""";
		return authTokenFirstAsJson.formatted(token);
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
	public void testHttpHandlerSetupCorrectly() {
		createClientUsingApptoken();
		tokenClient.getAuthToken();

		String expectedUrl = "http://localhost:8080/login/rest/apptoken";
		httpHandlerFactorySpy.MCR.assertParameters("factor", 0, expectedUrl);

		httpHandlerSpy.MCR.assertParameters("setRequestMethod", 0, "POST");
		httpHandlerSpy.MCR.assertParameters("setRequestProperty", 0, "Content-Type",
				"application/vnd.uub.login");
		httpHandlerSpy.MCR.assertNumberOfCallsToMethod("setRequestMethod", 1);

		httpHandlerSpy.MCR.assertParameters("setOutput", 0,
				appTokenCredentials.loginId() + "\n" + appTokenCredentials.appToken());
	}

	@Test
	public void testGetAuthToken() {
		createClientUsingApptoken();

		String authToken = tokenClient.getAuthToken();

		var authTokenAsJson = httpHandlerSpy.MCR.getReturnValue("getResponseText", 0);
		JsonToClientDataConverterSpy jsonToDataConverter = (JsonToClientDataConverterSpy) jsonToDataConverterFactory.MCR
				.assertCalledParametersReturn("factorUsingString", authTokenAsJson);
		jsonToDataConverter.MCR.assertCalledParametersReturn("toInstance");
		// assertEquals(authToken, EXAMPLE_AUTHTOKEN_FIRST);
	}

	@Test
	public void testGetAuthTokenTwiceIsOnlyCreatedOnceOnServer() {
		createClientUsingApptoken();
		String authToken = tokenClient.getAuthToken();
		String authToken2 = tokenClient.getAuthToken();

		httpHandlerFactorySpy.MCR.assertNumberOfCallsToMethod("factor", 1);
		httpHandlerSpy.MCR.assertNumberOfCallsToMethod("setOutput", 1);

		assertSame(authToken, authToken2);
	}

	@Test(expectedExceptions = DataClientException.class, expectedExceptionsMessageRegExp = ""
			+ "Could not create authToken. Response code: 400")
	public void testGetAuthTokenNotOk() {
		createClientUsingApptoken();
		httpHandlerSpy.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 400);

		tokenClient.getAuthToken();
	}

	@Test
	public void testHttpHandlerSetupCorrectlyUsingAuthToken() {
		createClientUsingAuthToken();

		assertSame(((TokenClientImp) tokenClient).onlyForTestGetHttpHandlerFactory(),
				httpHandlerFactorySpy);
		httpHandlerFactorySpy.MCR.assertNumberOfCallsToMethod("factor", 0);

	}

	@Test
	public void testGetAuthTokenUsingAuthToken() {
		createClientUsingAuthToken();

		String authToken = tokenClient.getAuthToken();
		assertEquals(authToken, EXAMPLE_AUTHTOKEN_FIRST);
	}

	@Test
	public void testRequestNewAuthToken_withAppTokenSetUp() {
		createClientUsingApptoken();

		String authToken = tokenClient.getAuthToken();
		assertEquals(authToken, EXAMPLE_AUTHTOKEN_FIRST);

		tokenClient.requestNewAuthToken();
		authToken = tokenClient.getAuthToken();
		assertEquals(authToken, EXAMPLE_AUTHTOKEN_SECOND);

		httpHandlerSpy.MCR.assertNumberOfCallsToMethod("getResponseCode", 1);
		httpHandlerSpy2.MCR.assertNumberOfCallsToMethod("getResponseCode", 1);
	}

	@Test(expectedExceptions = DataClientException.class, expectedExceptionsMessageRegExp = ""
			+ "Could not request a new authToken due to being initialized without appToken.")
	public void testRequestNewAuthToken_withAuthTokenSetUp() {
		createClientUsingAuthToken();

		String authToken = tokenClient.getAuthToken();
		assertEquals(authToken, EXAMPLE_AUTHTOKEN_FIRST);

		tokenClient.requestNewAuthToken();
	}

	// @Test
	// public void testCallRenewAuthToken() {
	// createClientUsingAuthToken();
	//
	// tokenClient.getAuthToken();
	//
	// }

}
