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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterProvider;
import se.uu.ub.cora.clientdata.spies.ClientActionLinkSpy;
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
	HttpHandlerFactorySpy httpHandlerFactory;
	HttpHandlerSpy httpHandlerSpy;
	HttpHandlerSpy httpHandlerSpy2;
	private AppTokenCredentials appTokenCredentials = new AppTokenCredentials(LOGIN_URL,
			"someLoginId", "02a89fd5-c768-4209-9ecc-d80bd793b01e");
	private TokenClient tokenClient;
	private AuthTokenCredentials authTokenCredentials = new AuthTokenCredentials(
			AUTHTOKEN_RENEW_URL, TOKEN_ZERO, true);
	private AuthTokenCredentials authTokenCredentialsWithoutRenew = new AuthTokenCredentials(
			AUTHTOKEN_RENEW_URL, TOKEN_ZERO);
	private JsonToClientDataConverterFactorySpy jsonToDataConverterFactory;
	private JsonToClientDataConverterSpy jsonToClientDataConverterSpyFirst;
	private ClientDataAuthenticationSpy clientDataAuthenticationSpyFirst;
	private String authenticationResponseFirst;
	private JsonToClientDataConverterSpy jsonToClientDataConverterSpySecond;
	private ClientDataAuthenticationSpy clientDataAuthenticationSpySecond;
	private String authenticationResponseSecond;

	private SchedulerFactorySpy schedulerFactory;
	private ClientActionLinkSpy renewActionFirst;
	private ClientActionLinkSpy renewActionSecond;

	@BeforeMethod
	public void setUp() {
		setUpConverterFactoryWithTwoAuthenticationSpies();
		setupHttphandlerFactoryWithTwoHttpHandlerSpies();
	}

	private void setUpConverterFactoryWithTwoAuthenticationSpies() {
		renewActionFirst = createActionLinkSpyWithSuffix("_first");

		clientDataAuthenticationSpyFirst = new ClientDataAuthenticationSpy();
		clientDataAuthenticationSpyFirst.MRV.setSpecificReturnValuesSupplier("getActionLink",
				() -> Optional.of(renewActionFirst), ClientAction.RENEW);
		clientDataAuthenticationSpyFirst.MRV.setDefaultReturnValuesSupplier("getValidUntil",
				this::generateValidUntil);
		clientDataAuthenticationSpyFirst.MRV.setDefaultReturnValuesSupplier("getRenewUntil",
				this::generateRenewUntil);
		clientDataAuthenticationSpyFirst.MRV.setDefaultReturnValuesSupplier("getToken",
				() -> TOKEN_FIRST);

		jsonToClientDataConverterSpyFirst = new JsonToClientDataConverterSpy();
		jsonToClientDataConverterSpyFirst.MRV.setDefaultReturnValuesSupplier("toInstance",
				() -> clientDataAuthenticationSpyFirst);

		renewActionSecond = createActionLinkSpyWithSuffix("_second");

		clientDataAuthenticationSpySecond = new ClientDataAuthenticationSpy();
		clientDataAuthenticationSpySecond.MRV.setSpecificReturnValuesSupplier("getActionLink",
				() -> Optional.of(renewActionSecond), ClientAction.RENEW);
		clientDataAuthenticationSpySecond.MRV.setDefaultReturnValuesSupplier("getValidUntil",
				this::generateValidUntil);
		clientDataAuthenticationSpySecond.MRV.setDefaultReturnValuesSupplier("getRenewUntil",
				this::generateRenewUntil);
		jsonToClientDataConverterSpySecond = new JsonToClientDataConverterSpy();
		jsonToClientDataConverterSpySecond.MRV.setDefaultReturnValuesSupplier("toInstance",
				() -> clientDataAuthenticationSpySecond);

		jsonToDataConverterFactory = new JsonToClientDataConverterFactorySpy();
		jsonToDataConverterFactory.MRV.setDefaultReturnValuesSupplier("factorUsingString",
				ListSupplier.of(jsonToClientDataConverterSpyFirst,
						jsonToClientDataConverterSpySecond));
		JsonToClientDataConverterProvider.setJsonToDataConverterFactory(jsonToDataConverterFactory);

		schedulerFactory = new SchedulerFactorySpy();
	}

	private ClientActionLinkSpy createActionLinkSpyWithSuffix(String suffix) {
		ClientActionLinkSpy action = new ClientActionLinkSpy();
		action.MRV.setDefaultReturnValuesSupplier("getURL", () -> "urlForSchedulerRenew" + suffix);
		action.MRV.setDefaultReturnValuesSupplier("getRequestMethod",
				() -> "someRequestMethod" + suffix);
		action.MRV.setDefaultReturnValuesSupplier("getAccept",
				() -> "application/vnd.uub.authentication+json" + suffix);
		return action;
	}

	private String generateValidUntil() {
		return String.valueOf(System.currentTimeMillis() + 50000);
	}

	private String generateRenewUntil() {
		return String.valueOf(System.currentTimeMillis() + 200000);
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

		httpHandlerFactory = new HttpHandlerFactorySpy();
		httpHandlerFactory.MRV.setReturnValues("factor", List.of(httpHandlerSpy, httpHandlerSpy2),
				LOGIN_URL + CORA_REST_APPTOKEN_ENDPOINT);
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
		tokenClient = TokenClientImp.usingHttpHandlerFactoryAndSchedulerFactoryAndAuthToken(
				httpHandlerFactory, schedulerFactory, authTokenCredentials);
	}

	private void createClientUsingApptoken() {
		tokenClient = TokenClientImp.usingHttpHandlerFactoryAndSchedulerFactoryAndAppToken(
				httpHandlerFactory, schedulerFactory, appTokenCredentials);
	}

	@Test
	public void testHttpHandlerSetupCorrectlyUsingAppToken() {
		createClientUsingApptoken();

		triggerInitialLoginForApptokenUsingGetAuthTokenMethod();

		String expectedUrl = "http://localhost:8080/login/rest/apptoken";
		httpHandlerFactory.MCR.assertParameters("factor", 0, expectedUrl);

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

	private void triggerInitialLoginForApptokenUsingGetAuthTokenMethod() {
		tokenClient.getAuthToken();
	}

	@Test
	public void testLoginWithAppToken() {
		createClientUsingApptoken();

		String authToken = tokenClient.getAuthToken();

		ClientDataAuthenticationSpy authenticationSpy = assertResponseWhenOneCallToConvertedToAuthentication();
		authenticationSpy.MCR.assertReturn("getToken", 0, authToken);
	}

	private ClientDataAuthenticationSpy assertResponseWhenOneCallToConvertedToAuthentication() {
		jsonToDataConverterFactory.MCR.assertNumberOfCallsToMethod("factorUsingString", 1);
		JsonToClientDataConverterSpy jsonToDataConverter = (JsonToClientDataConverterSpy) jsonToDataConverterFactory.MCR
				.getReturnValue("factorUsingString", 0);
		return (ClientDataAuthenticationSpy) jsonToDataConverter.MCR
				.assertCalledParametersReturn("toInstance");
	}

	private ClientDataAuthenticationSpy assertResponseTwoCallsToConvertedToAuthentication() {
		jsonToDataConverterFactory.MCR.assertNumberOfCallsToMethod("factorUsingString", 2);
		JsonToClientDataConverterSpy jsonToDataConverter = (JsonToClientDataConverterSpy) jsonToDataConverterFactory.MCR
				.getReturnValue("factorUsingString", 1);
		return (ClientDataAuthenticationSpy) jsonToDataConverter.MCR
				.assertCalledParametersReturn("toInstance");
	}

	@Test
	public void testGetAuthTokenTwiceIsOnlyCreatedOnceOnServer() {
		createClientUsingApptoken();
		String authToken = tokenClient.getAuthToken();
		String authToken2 = tokenClient.getAuthToken();

		httpHandlerFactory.MCR.assertNumberOfCallsToMethod("factor", 1);
		httpHandlerSpy.MCR.assertNumberOfCallsToMethod("setOutput", 1);
		clientDataAuthenticationSpyFirst.MCR.assertNumberOfCallsToMethod("getToken", 2);

		assertSame(authToken, authToken2);
	}

	@Test(expectedExceptions = DataClientException.class, expectedExceptionsMessageRegExp = ""
			+ "Could not create authToken. Response code: 400")
	public void testGetAuthTokenNotOk() {
		createClientUsingApptoken();
		httpHandlerSpy.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 400);

		triggerInitialLoginForApptokenUsingGetAuthTokenMethod();
	}

	@Test
	public void testStartTokenClientWithAuthToken() {
		HttpHandlerSpy httpHandler = setupHttpHandlerFactoryWithOneHandlerAndOk();
		createClientUsingAuthToken();

		String authToken = tokenClient.getAuthToken();

		assertRenewAuthTokenRequest(httpHandler);
		ClientDataAuthenticationSpy authenticationSpy = assertResponseWhenOneCallToConvertedToAuthentication();

		authenticationSpy.MCR.assertReturn("getToken", 0, authToken);
	}

	private HttpHandlerSpy setupHttpHandlerFactoryWithOneHandlerAndOk() {
		HttpHandlerSpy httpHandler = new HttpHandlerSpy();
		httpHandler.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 200);
		httpHandler.MRV.setDefaultReturnValuesSupplier("getResponseText",
				() -> authenticationResponseFirst);
		httpHandlerFactory.MRV.setDefaultReturnValuesSupplier("factor", () -> httpHandler);
		return httpHandler;
	}

	private void assertRenewAuthTokenRequest(HttpHandlerSpy httpHandlerSpy) {
		httpHandlerFactory.MCR.assertNumberOfCallsToMethod("factor", 1);
		httpHandlerFactory.MCR.assertParameters("factor", 0, AUTHTOKEN_RENEW_URL);
		httpHandlerSpy.MCR.assertParameters("setRequestMethod", 0, "POST");
		httpHandlerSpy.MCR.assertNumberOfCallsToMethod("setRequestMethod", 1);
		httpHandlerSpy.MCR.assertCalledParameters("setRequestProperty", "authToken",
				authTokenCredentials.authToken());
		httpHandlerSpy.MCR.assertCalledParameters("setRequestProperty", "Accept",
				"application/vnd.uub.authentication+json");
		httpHandlerSpy.MCR.assertNumberOfCallsToMethod("setRequestProperty", 2);
	}

	@Test
	public void testStartTokenClientWithAuthTokenWithoutRenew() {
		tokenClient = TokenClientImp.usingHttpHandlerFactoryAndSchedulerFactoryAndAuthToken(
				httpHandlerFactory, schedulerFactory, authTokenCredentialsWithoutRenew);

		String authToken = tokenClient.getAuthToken();

		httpHandlerFactory.MCR.assertMethodNotCalled("factor");
		assertEquals(authToken, authTokenCredentialsWithoutRenew.authToken());
	}

	@Test(expectedExceptions = DataClientException.class, expectedExceptionsMessageRegExp = ""
			+ "Could not renew authToken due to error. Response code: 500")
	public void testStartTokenClientWithAuthTokenErrorWhileRenewNewAuthToken() {
		setupHttpHandlerFactoryWithOneHandlerAndNotOk();
		createClientUsingAuthToken();

		triggerRenewOfInitialAuthTokenOnServerUsingGetAuthTokenMethod();
	}

	private void triggerRenewOfInitialAuthTokenOnServerUsingGetAuthTokenMethod() {
		tokenClient.getAuthToken();
	}

	private HttpHandlerSpy setupHttpHandlerFactoryWithOneHandlerAndNotOk() {
		HttpHandlerSpy httpHandler = new HttpHandlerSpy();
		httpHandler.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 500);
		httpHandlerFactory.MRV.setDefaultReturnValuesSupplier("factor", () -> httpHandler);
		return httpHandler;
	}

	@Test(expectedExceptions = DataClientException.class, expectedExceptionsMessageRegExp = ""
			+ "AuthToken could not be renew due to missing actionLink on previous call.")
	public void testSchedulerRenewCannotFindRenewActionInAuthentication_withAuthTokenInit() {
		setAutenticationWithoutRenewActionLink();
		createClientUsingAuthToken();

		triggerRenewOfInitialAuthTokenOnServerUsingGetAuthTokenMethod();
	}

	private void setAutenticationWithoutRenewActionLink() {
		clientDataAuthenticationSpyFirst.MRV.setSpecificReturnValuesSupplier("getActionLink",
				Optional::empty, ClientAction.RENEW);
	}

	@Test(expectedExceptions = DataClientException.class, expectedExceptionsMessageRegExp = ""
			+ "AuthToken could not be renew due to missing actionLink on previous call.")
	public void testSchedulerRenewCannotFindRenewActionInAuthentication_withApptokenInit() {
		setAutenticationWithoutRenewActionLink();
		createClientUsingApptoken();

		triggerRenewOfInitialAuthTokenOnServerUsingGetAuthTokenMethod();
	}

	@Test
	public void testScheduleRenewAuthTokenForAuthTokenCredentials_withAuthTokenInit() {
		createClientUsingAuthToken();

		triggerRenewOfInitialAuthTokenOnServerUsingGetAuthTokenMethod();

		SchedulerSpy scheduler = (SchedulerSpy) schedulerFactory.MCR
				.assertCalledParametersReturn("factor");
		assertDelayForRenewScheduler(scheduler);

		Runnable task = (Runnable) scheduler.MCR.getParameterForMethodAndCallNumberAndParameter(
				"scheduleTaskWithDelayInMillis", 0, "task");
		assertNotNull(task);
	}

	private void assertDelayForRenewScheduler(SchedulerSpy scheduler) {
		Long delay = (Long) scheduler.MCR.getParameterForMethodAndCallNumberAndParameter(
				"scheduleTaskWithDelayInMillis", 0, "delayInMillis");
		int margin = 10000;
		int maxExpectedDelay = 50000 - margin;
		int minExpectedDelay = 49500 - margin;
		assertTrue(delay <= maxExpectedDelay);
		assertTrue(delay > minExpectedDelay);
	}

	@Test
	public void testScheduleRenewAuthTokenForAuthTokenCredentials_withAppTokenInit() {
		createClientUsingApptoken();

		triggerRenewOfInitialAuthTokenOnServerUsingGetAuthTokenMethod();

		SchedulerSpy scheduler = (SchedulerSpy) schedulerFactory.MCR
				.assertCalledParametersReturn("factor");
		assertDelayForRenewScheduler(scheduler);

		Runnable task = (Runnable) scheduler.MCR.getParameterForMethodAndCallNumberAndParameter(
				"scheduleTaskWithDelayInMillis", 0, "task");
		assertNotNull(task);
	}

	@Test(expectedExceptions = DataClientException.class, expectedExceptionsMessageRegExp = ""
			+ "The authToken renewal could not be scheduled because it has reached the permitted "
			+ "renewal limit. The current token will remain active for a while but will "
			+ "eventually become unauthorized. Please re-login to continue using this client.")
	public void testDoNotScheduleRenewAuthIfTimeToRenewHasPassedRenewUntil__withAuthTokenInit() {
		setRenewUntilToBeBeforeTimeToRenew();
		createClientUsingAuthToken();

		triggerRenewOfInitialAuthTokenOnServerUsingGetAuthTokenMethod();

	}

	@Test(expectedExceptions = DataClientException.class, expectedExceptionsMessageRegExp = ""
			+ "The authToken renewal could not be scheduled because it has reached the permitted "
			+ "renewal limit. The current token will remain active for a while but will "
			+ "eventually become unauthorized. Please re-login to continue using this client.")
	public void testDoNotScheduleRenewAuthIfTimeToRenewHasPassedRenewUntil__withAppTokenInit() {
		setRenewUntilToBeBeforeTimeToRenew();
		createClientUsingApptoken();

		triggerRenewOfInitialAuthTokenOnServerUsingGetAuthTokenMethod();

	}

	private void setRenewUntilToBeBeforeTimeToRenew() {
		int ensureRenewUntilIsBeforeTimeToRenew = 30000;
		String renewUntil = String
				.valueOf(System.currentTimeMillis() + ensureRenewUntilIsBeforeTimeToRenew);
		clientDataAuthenticationSpyFirst.MRV.setDefaultReturnValuesSupplier("getRenewUntil",
				() -> renewUntil);
	}

	@Test
	public void testSceduledRenewTaskAfterInitialRenewOfProvidedAuthTokenByRunningIt_withAuthTokenInit() {
		HttpHandlerSpy hhs1 = createHttpHandlerSpyForResponse(200, TOKEN_FIRST);
		HttpHandlerSpy hhs2 = createHttpHandlerSpyForResponse(200, TOKEN_SECOND);
		httpHandlerFactory.MRV.setDefaultReturnValuesSupplier("factor",
				ListSupplier.of(hhs1, hhs2));

		createClientUsingAuthToken();

		triggerRenewOfInitialAuthTokenOnServerUsingGetAuthTokenMethod();

		runScheduledTaskNumber(0);

		httpHandlerFactory.MCR.assertNumberOfCallsToMethod("factor", 2);
		httpHandlerFactory.MCR.assertParameters("factor", 1, renewActionFirst.getURL());
		hhs2.MCR.assertParameters("setRequestMethod", 0, renewActionFirst.getRequestMethod());
		hhs2.MCR.assertParameters("setRequestProperty", 0, "Accept", renewActionFirst.getAccept());
		hhs2.MCR.assertParameters("setRequestProperty", 1, "authToken", TOKEN_FIRST);
		hhs2.MCR.assertNumberOfCallsToMethod("setRequestProperty", 2);

		// token after schedule is run
		String authToken = tokenClient.getAuthToken();
		ClientDataAuthenticationSpy authenticationSpy = assertResponseTwoCallsToConvertedToAuthentication();
		authenticationSpy.MCR.assertReturn("getToken", 0, authToken);
	}

	@Test
	public void testSceduledRenewTaskAfterInitialRenewOfProvidedAuthTokenByRunningIt_withAppTokenInit() {
		HttpHandlerSpy hhs1 = createHttpHandlerSpyForResponse(200, TOKEN_FIRST);
		httpHandlerFactory.MRV.setDefaultReturnValuesSupplier("factor", () -> hhs1);

		createClientUsingApptoken();

		triggerRenewOfInitialAuthTokenOnServerUsingGetAuthTokenMethod();

		runScheduledTaskNumber(0);

		httpHandlerFactory.MCR.assertNumberOfCallsToMethod("factor", 2);
		httpHandlerFactory.MCR.assertParameters("factor", 1, renewActionFirst.getURL());
		hhs1.MCR.assertParameters("setRequestMethod", 0, renewActionFirst.getRequestMethod());
		hhs1.MCR.assertParameters("setRequestProperty", 0, "Accept", renewActionFirst.getAccept());
		hhs1.MCR.assertParameters("setRequestProperty", 1, "authToken", TOKEN_FIRST);
		hhs1.MCR.assertNumberOfCallsToMethod("setRequestProperty", 2);

		// token after schedule is run
		String authToken = tokenClient.getAuthToken();
		ClientDataAuthenticationSpy authenticationSpy = assertResponseTwoCallsToConvertedToAuthentication();
		System.out.println(authToken);
		authenticationSpy.MCR.assertReturn("getToken", 0, authToken);
	}

	@Test
	public void testSceduledNewRenewAuthTokenAfterFirstRenewAuthTokenIsOK() {
		HttpHandlerSpy hhs1 = createHttpHandlerSpyForResponse(200, TOKEN_FIRST);
		HttpHandlerSpy hhs2 = createHttpHandlerSpyForResponse(200, TOKEN_SECOND);
		httpHandlerFactory.MRV.setDefaultReturnValuesSupplier("factor",
				ListSupplier.of(hhs1, hhs2));

		createClientUsingAuthToken();

		triggerRenewOfInitialAuthTokenOnServerUsingGetAuthTokenMethod();

		runScheduledTaskNumber(0);

		schedulerFactory.MCR.assertNumberOfCallsToMethod("factor", 2);
		SchedulerSpy scheduler = (SchedulerSpy) schedulerFactory.MCR.getReturnValue("factor", 1);
		scheduler.MCR.assertMethodWasCalled("scheduleTaskWithDelayInMillis");
	}

	@Test(expectedExceptions = DataClientException.class, expectedExceptionsMessageRegExp = ""
			+ "Could not renew authToken due to error. Response code: 500")
	public void testTrySceduledNewRenewAuthTokenAfterFirstRenewAuthTokenIsNotOK() {
		HttpHandlerSpy hhs1 = createHttpHandlerSpyForResponse(200, TOKEN_FIRST);
		HttpHandlerSpy hhs2 = createHttpHandlerSpyForResponse(500, TOKEN_SECOND);
		httpHandlerFactory.MRV.setDefaultReturnValuesSupplier("factor",
				ListSupplier.of(hhs1, hhs2));

		createClientUsingAuthToken();

		triggerRenewOfInitialAuthTokenOnServerUsingGetAuthTokenMethod();

		runScheduledTaskNumber(0);

		schedulerFactory.MCR.assertNumberOfCallsToMethod("factor", 1);
	}

	private void runScheduledTaskNumber(int callNumber) {
		var task = getScheduledRenewTaskUsingCallNumber(callNumber);
		task.run();
	}

	private Runnable getScheduledRenewTaskUsingCallNumber(int callNumber) {
		SchedulerSpy scheduler = (SchedulerSpy) schedulerFactory.MCR.getReturnValue("factor",
				callNumber);
		return (Runnable) scheduler.MCR.getParameterForMethodAndCallNumberAndParameter(
				"scheduleTaskWithDelayInMillis", callNumber, "task");
	}

	private HttpHandlerSpy createHttpHandlerSpyForResponse(int code, String token) {
		String responseText = authenticationResponseFirst = getAuthTokenStringUsingToken(token);
		HttpHandlerSpy hhs = new HttpHandlerSpy();
		hhs.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> code);
		hhs.MRV.setDefaultReturnValuesSupplier("getResponseText", () -> responseText);
		return hhs;
	}

	@Test(expectedExceptions = DataClientException.class, expectedExceptionsMessageRegExp = ""
			+ "Could not request a new authToken due to being initialized without appToken.")
	public void testRequestNewAuthToken_withAuthTokenSetUp() {
		createClientUsingAuthToken();

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

		httpHandlerFactory.MCR.assertNumberOfCallsToMethod("factor", 2);
		httpHandlerSpy.MCR.assertNumberOfCallsToMethod("getResponseCode", 1);
		httpHandlerSpy2.MCR.assertNumberOfCallsToMethod("getResponseCode", 1);

		jsonToDataConverterFactory.MCR.assertParameters("factorUsingString", 0,
				authenticationResponseFirst);
		jsonToDataConverterFactory.MCR.assertParameters("factorUsingString", 1,
				authenticationResponseSecond);
	}

	@Test
	public void testHttpHandlerSetupCorrectlyUsingAuthToken() {
		createClientUsingAuthToken();

		TokenClientImp tokenClientImp = (TokenClientImp) tokenClient;
		assertSame(tokenClientImp.onlyForTestGetHttpHandlerFactory(), httpHandlerFactory);
		assertSame(tokenClientImp.onlyForTestGetSchedulerFactory(), schedulerFactory);
		assertSame(tokenClientImp.onlyForTestGetAuthTokenCredentials(), authTokenCredentials);
	}

	@Test
	public void testOnlyForTestForAppToken() {
		createClientUsingApptoken();

		TokenClientImp tokenClientImp = (TokenClientImp) tokenClient;
		assertSame(tokenClientImp.onlyForTestGetHttpHandlerFactory(), httpHandlerFactory);
		assertSame(tokenClientImp.onlyForTestGetSchedulerFactory(), schedulerFactory);
		assertSame(tokenClientImp.onlyForTestGetAppTokenCredentials(), appTokenCredentials);
	}
}
