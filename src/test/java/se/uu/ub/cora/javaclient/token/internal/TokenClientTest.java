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

import static org.testng.Assert.assertSame;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterProvider;
import se.uu.ub.cora.clientdata.spies.ClientDataAuthenticationSpy;
import se.uu.ub.cora.clientdata.spies.JsonToClientDataConverterFactorySpy;
import se.uu.ub.cora.clientdata.spies.JsonToClientDataConverterSpy;
import se.uu.ub.cora.httphandler.spies.HttpHandlerFactorySpy;
import se.uu.ub.cora.httphandler.spies.HttpHandlerSpy;
import se.uu.ub.cora.javaclient.AppTokenCredentials;
import se.uu.ub.cora.javaclient.AuthTokenCredentials;
import se.uu.ub.cora.javaclient.data.DataClientException;
import se.uu.ub.cora.javaclient.token.TokenClient;

public class TokenClientTest {
	private static final String AUTHTOKEN_RENEW_URL = "someAuthTokenRenewUrl";
	private static final String LOGIN_URL = "http://localhost:8080/login/rest/";
	private static final String CORA_REST_APPTOKEN_ENDPOINT = "apptoken";
	private static final String TOKEN_ZERO = "zero-5849-4e10-9ee9-4b192aef17fd";
	private static final String TOKEN_FIRST = "first-5849-4e10-9ee9-4b192aef17fd";
	private static final String TOKEN_SECOND = "second-5849-4e10-9ee9-4b192aef17fd";
	HttpHandlerFactorySpy httpHandlerFactorySpy;
	HttpHandlerSpy httpHandlerSpy;
	HttpHandlerSpy httpHandlerSpy2;
	private AppTokenCredentials appTokenCredentials = new AppTokenCredentials(LOGIN_URL,
			"someLoginId", "02a89fd5-c768-4209-9ecc-d80bd793b01e");
	private TokenClient tokenClient;
	private AuthTokenCredentials authTokenCredentials = new AuthTokenCredentials(
			AUTHTOKEN_RENEW_URL, TOKEN_ZERO);
	private JsonToClientDataConverterFactorySpy jsonToDataConverterFactory;

	private JsonToClientDataConverterSpy jsonToClientDataConverterSpyFirst;
	private ClientDataAuthenticationSpy clientDataAuthenticationSpyFirst;
	private String authenticationResponseFirst;

	private JsonToClientDataConverterSpy jsonToClientDataConverterSpySecond;
	private ClientDataAuthenticationSpy clientDataAuthenticationSpySecond;
	private String authenticationResponseSecond;

	@BeforeMethod
	public void setUp() {
		setUpConverterFactoryWithTwoAuthenticationSpies();
		setupHttphandlerFactoryWithTwoHttpHandlerSpies();
	}

	private void setUpConverterFactoryWithTwoAuthenticationSpies() {
		clientDataAuthenticationSpyFirst = new ClientDataAuthenticationSpy();
		jsonToClientDataConverterSpyFirst = new JsonToClientDataConverterSpy();
		jsonToClientDataConverterSpyFirst.MRV.setDefaultReturnValuesSupplier("toInstance",
				() -> clientDataAuthenticationSpyFirst);

		clientDataAuthenticationSpySecond = new ClientDataAuthenticationSpy();
		jsonToClientDataConverterSpySecond = new JsonToClientDataConverterSpy();
		jsonToClientDataConverterSpySecond.MRV.setDefaultReturnValuesSupplier("toInstance",
				() -> clientDataAuthenticationSpySecond);

		jsonToDataConverterFactory = new JsonToClientDataConverterFactorySpy();
		List<JsonToClientDataConverterSpy> list = List.of(jsonToClientDataConverterSpyFirst,
				jsonToClientDataConverterSpySecond);
		jsonToDataConverterFactory.MRV.setDefaultReturnValuesSupplier("factorUsingString",
				new ListSupplier<JsonToClientDataConverterSpy>(list));
		JsonToClientDataConverterProvider.setJsonToDataConverterFactory(jsonToDataConverterFactory);
	}

	class ListSupplier<T> implements Supplier<T> {
		private Iterator<T> iterator;

		public ListSupplier(List<T> list) {
			iterator = list.iterator();
		}

		@Override
		public T get() {
			return iterator.next();
		}
	}

	private void setupHttphandlerFactoryWithTwoHttpHandlerSpies() {
		httpHandlerSpy = new HttpHandlerSpy();
		httpHandlerSpy.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 201);
		authenticationResponseFirst = getAuthTokenStringUsingToken(TOKEN_FIRST);
		httpHandlerSpy.MRV.setDefaultReturnValuesSupplier("getResponseText",
				() -> authenticationResponseFirst);

		httpHandlerSpy2 = new HttpHandlerSpy();
		httpHandlerSpy2.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 201);
		authenticationResponseSecond = getAuthTokenStringUsingToken(TOKEN_SECOND);
		httpHandlerSpy2.MRV.setDefaultReturnValuesSupplier("getResponseText",
				() -> authenticationResponseSecond);

		httpHandlerFactorySpy = new HttpHandlerFactorySpy();
		httpHandlerFactorySpy.MRV.setReturnValues("factor",
				List.of(httpHandlerSpy, httpHandlerSpy2), LOGIN_URL + CORA_REST_APPTOKEN_ENDPOINT);

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
		httpHandlerSpy.MCR.assertNumberOfCallsToMethod("setRequestMethod", 1);

		httpHandlerSpy.MCR.assertParameters("setRequestProperty", 0, "Content-Type",
				"application/vnd.uub.login");
		httpHandlerSpy.MCR.assertParameters("setRequestProperty", 1, "Accept",
				"application/vnd.uub.authentication+json");
		httpHandlerSpy.MCR.assertNumberOfCallsToMethod("setRequestProperty", 2);

		httpHandlerSpy.MCR.assertParameters("setOutput", 0,
				appTokenCredentials.loginId() + "\n" + appTokenCredentials.appToken());
	}

	@Test
	public void testStartedWithAuthTokenRenewsTokenToTakeControllOverRenew() {
		createClientUsingApptoken();

		String authToken = tokenClient.getAuthToken();

		ClientDataAuthenticationSpy authenticationSpy = assertReadAnswerAndConvertToClientDataAuthentication(
				httpHandlerSpy);
		authenticationSpy.MCR.assertReturn("getToken", 0, authToken);
	}

	private ClientDataAuthenticationSpy assertReadAnswerAndConvertToClientDataAuthentication(
			HttpHandlerSpy httpHandler) {
		var authTokenAsJson = httpHandler.MCR.getReturnValue("getResponseText", 0);
		JsonToClientDataConverterSpy jsonToDataConverter = (JsonToClientDataConverterSpy) jsonToDataConverterFactory.MCR
				.assertCalledParametersReturn("factorUsingString", authTokenAsJson);
		ClientDataAuthenticationSpy authenticationSpy = (ClientDataAuthenticationSpy) jsonToDataConverter.MCR
				.assertCalledParametersReturn("toInstance");
		return authenticationSpy;
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
	}

	@Test
	public void testStartTokenClientWithAuthToke() {
		HttpHandlerSpy httpHandler = setupHttpHandlerFactoryWithOneHandlerAndOk();
		createClientUsingAuthToken();

		assertRenewAuthTokenRequest(httpHandler);
		ClientDataAuthenticationSpy authenticationSpy = assertReadAnswerAndConvertToClientDataAuthentication(
				httpHandler);

		String authToken = tokenClient.getAuthToken();

		authenticationSpy.MCR.assertReturn("getToken", 0, authToken);
	}

	private HttpHandlerSpy setupHttpHandlerFactoryWithOneHandlerAndOk() {
		HttpHandlerSpy httpHandler = new HttpHandlerSpy();
		httpHandler.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 200);
		String authenticationResponseFirst = getAuthTokenStringUsingToken(TOKEN_FIRST);
		httpHandler.MRV.setDefaultReturnValuesSupplier("getResponseText",
				() -> authenticationResponseFirst);
		httpHandlerFactorySpy.MRV.setDefaultReturnValuesSupplier("factor", () -> httpHandler);
		return httpHandler;
	}

	private void assertRenewAuthTokenRequest(HttpHandlerSpy httpHandlerSpy) {
		httpHandlerFactorySpy.MCR.assertNumberOfCallsToMethod("factor", 1);
		httpHandlerFactorySpy.MCR.assertParameters("factor", 0, AUTHTOKEN_RENEW_URL);
		httpHandlerSpy.MCR.assertParameters("setRequestMethod", 0, "POST");
		httpHandlerSpy.MCR.assertNumberOfCallsToMethod("setRequestMethod", 1);
		httpHandlerSpy.MCR.assertCalledParameters("setRequestProperty", "authToken",
				authTokenCredentials.authToken());
		httpHandlerSpy.MCR.assertCalledParameters("setRequestProperty", "Accept",
				"application/vnd.uub.authentication+json");
		httpHandlerSpy.MCR.assertNumberOfCallsToMethod("setRequestProperty", 2);
	}

	@Test(expectedExceptions = DataClientException.class, expectedExceptionsMessageRegExp = ""
			+ "Could not renew authToken due to error. Response code: 500")
	public void testStartTokenClientWithAuthTokenErrorWhileRenewNewAuthToken() throws Exception {
		setupHttpHandlerFactoryWithOneHandlerAndNotOk();

		createClientUsingAuthToken();
	}

	private HttpHandlerSpy setupHttpHandlerFactoryWithOneHandlerAndNotOk() {
		HttpHandlerSpy httpHandler = new HttpHandlerSpy();
		httpHandler.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 500);
		httpHandlerFactorySpy.MRV.setDefaultReturnValuesSupplier("factor", () -> httpHandler);
		return httpHandler;
	}

	@Test(expectedExceptions = DataClientException.class, expectedExceptionsMessageRegExp = ""
			+ "Could not request a new authToken due to being initialized without appToken.")
	public void testRequestNewAuthToken_withAuthTokenSetUp() {
		createClientUsingAuthToken();

		tokenClient.getAuthToken();

		tokenClient.requestNewAuthToken();
	}

	@Test
	public void testRequestNewAuthToken_withAppTokenSetUp() {
		createClientUsingApptoken();

		String authToken = tokenClient.getAuthToken();
		clientDataAuthenticationSpyFirst.MCR.assertReturn("getToken", 0, authToken);

		tokenClient.requestNewAuthToken();

		String authTokenSecond = tokenClient.getAuthToken();
		clientDataAuthenticationSpySecond.MCR.assertReturn("getToken", 0, authTokenSecond);

		httpHandlerFactorySpy.MCR.assertNumberOfCallsToMethod("factor", 2);
		httpHandlerSpy.MCR.assertNumberOfCallsToMethod("getResponseCode", 1);
		httpHandlerSpy2.MCR.assertNumberOfCallsToMethod("getResponseCode", 1);

		jsonToDataConverterFactory.MCR.assertParameters("factorUsingString", 0,
				authenticationResponseFirst);
		jsonToDataConverterFactory.MCR.assertParameters("factorUsingString", 1,
				authenticationResponseSecond);

	}

}
