/*
 * Copyright 2018, 2023 Uppsala University Library
 * Copyright 2023 Olov McKie
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
package se.uu.ub.cora.javaclient.internal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.javaclient.AppTokenCredentials;
import se.uu.ub.cora.javaclient.AuthTokenCredentials;
import se.uu.ub.cora.javaclient.JavaClientAppTokenCredentials;
import se.uu.ub.cora.javaclient.JavaClientAuthTokenCredentials;
import se.uu.ub.cora.javaclient.JavaClientFactory;
import se.uu.ub.cora.javaclient.data.internal.DataClientImp;
import se.uu.ub.cora.javaclient.rest.internal.RestClientImp;
import se.uu.ub.cora.javaclient.token.internal.OneAtATimeScheduler;
import se.uu.ub.cora.javaclient.token.internal.TokenClientImp;

public class JavaClientFactoryTest {

	private JavaClientFactory factory;
	private String baseUrl = "https://someBaseUrl";
	private String authToken = "someAuthToken";
	private String loginUrl = "https://someAptokenUrl";
	private String loginId = "someLoginId";
	private static final String RENEW_URL = "someRenewUrl";
	private String appToken = "someAppToken";
	private JavaClientAppTokenCredentials javaClientAppTokenCredentials = new JavaClientAppTokenCredentials(
			baseUrl, loginUrl, loginId, appToken);
	private JavaClientAuthTokenCredentials javaClientAuthTokenCredentials = new JavaClientAuthTokenCredentials(
			baseUrl, RENEW_URL, authToken);
	private AppTokenCredentials appTokenCredentials = new AppTokenCredentials(loginUrl, loginId,
			appToken);
	private AuthTokenCredentials authTokenCredentials = new AuthTokenCredentials(RENEW_URL,
			authToken, true);

	@BeforeMethod
	public void beforeMethod() {
		factory = new JavaClientFactoryImp();
	}

	@Test
	public void testFactorBaseUrlAddedToRestClient() {
		RestClientImp restClient = (RestClientImp) factory
				.factorRestClientUsingJavaClientAuthTokenCredentials(
						javaClientAuthTokenCredentials);

		assertTrue(restClient instanceof RestClientImp);
		assertEquals(restClient.onlyForTestGetBaseUrl(), baseUrl);
	}

	@Test
	public void testFactorHttpHandlerFactoryCreatedAndAddedToRestClient() {
		RestClientImp restClient = (RestClientImp) factory
				.factorRestClientUsingJavaClientAuthTokenCredentials(
						javaClientAuthTokenCredentials);

		assertTrue(restClient.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactoryImp);
	}

	@Test
	public void testFactorTokenClientCreatedCorrectlyAndAddedToRestClient() {
		RestClientImp restClient = (RestClientImp) factory
				.factorRestClientUsingJavaClientAuthTokenCredentials(
						javaClientAuthTokenCredentials);

		TokenClientImp tokenClient = (TokenClientImp) restClient.onlyForTestGetTokenClient();
		assertTokenClientCredentialsWithoutAuthTokenRenewable(tokenClient);
	}

	private void assertTokenClientCredentialsWithoutAuthTokenRenewable(TokenClientImp tokenClient) {
		assertTrue(tokenClient.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactoryImp);
		assertTrue(tokenClient.onlyForTestGetScheduler() instanceof OneAtATimeScheduler);

		AuthTokenCredentials returnedAuthTokenCredentials = tokenClient
				.onlyForTestGetAuthTokenCredentials();

		assertEquals(returnedAuthTokenCredentials.authToken(), authToken);
		assertEquals(returnedAuthTokenCredentials.authTokenRenewUrl(), RENEW_URL);
		assertFalse(returnedAuthTokenCredentials.tokenIsRenewable());
	}

	@Test
	public void testFactorRestClientUsingAuthTokenWithRenew() {
		JavaClientAuthTokenCredentials javaClientAthTokencCredentialsRenewable = new JavaClientAuthTokenCredentials(
				baseUrl, RENEW_URL, authToken, true);

		RestClientImp restClient = (RestClientImp) factory
				.factorRestClientUsingJavaClientAuthTokenCredentials(
						javaClientAthTokencCredentialsRenewable);

		TokenClientImp tokenClient = (TokenClientImp) restClient.onlyForTestGetTokenClient();
		assertTokenClientCredentials(tokenClient);
	}

	private void assertTokenClientCredentials(TokenClientImp tokenClient) {
		assertTrue(tokenClient.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactoryImp);
		assertTrue(tokenClient.onlyForTestGetScheduler() instanceof OneAtATimeScheduler);

		AuthTokenCredentials returnedAuthTokenCredentials = tokenClient
				.onlyForTestGetAuthTokenCredentials();

		assertEquals(returnedAuthTokenCredentials.authTokenRenewUrl(), RENEW_URL);
		assertEquals(returnedAuthTokenCredentials.authToken(), authToken);
		assertTrue(returnedAuthTokenCredentials.tokenIsRenewable());
	}

	@Test
	public void testFactorAppBaseUrlAddedToRestClient() {
		RestClientImp restClient = (RestClientImp) factory
				.factorRestClientUsingJavaClientAppTokenCredentials(javaClientAppTokenCredentials);

		assertTrue(restClient instanceof RestClientImp);
		assertEquals(restClient.onlyForTestGetBaseUrl(), baseUrl);
	}

	@Test
	public void testFactorAppHttpHandlerFactoryCreatedAndAddedToRestClient() {
		RestClientImp restClient = (RestClientImp) factory
				.factorRestClientUsingJavaClientAppTokenCredentials(javaClientAppTokenCredentials);

		assertTrue(restClient.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactoryImp);
	}

	@Test
	public void testFactorAppTokenClientCreatedCorrectlyAndAddedToRestClient() {
		RestClientImp restClient = (RestClientImp) factory
				.factorRestClientUsingJavaClientAppTokenCredentials(javaClientAppTokenCredentials);

		TokenClientImp tokenClient = (TokenClientImp) restClient.onlyForTestGetTokenClient();
		assertTokenClientUsingAppTokenCredentials(tokenClient);
	}

	private void assertTokenClientUsingAppTokenCredentials(TokenClientImp tokenClient) {
		assertTrue(tokenClient.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactoryImp);
		assertTrue(tokenClient.onlyForTestGetScheduler() instanceof OneAtATimeScheduler);

		AppTokenCredentials returnedAppTokenCredentials = tokenClient
				.onlyForTestGetAppTokenCredentials();

		assertEquals(returnedAppTokenCredentials.loginUrl(), loginUrl);
		assertEquals(returnedAppTokenCredentials.loginId(), loginId);
		assertEquals(returnedAppTokenCredentials.appToken(), appToken);
	}

	@Test
	public void testFactorBaseUrlAddedToRestClient_dataClient() {
		DataClientImp dataClient = (DataClientImp) factory
				.factorDataClientUsingJavaClientAuthTokenCredentials(
						javaClientAuthTokenCredentials);

		assertTrue(dataClient instanceof DataClientImp);
		RestClientImp restClient = (RestClientImp) dataClient.onlyForTestGetRestClient();
		assertEquals(restClient.onlyForTestGetBaseUrl(), baseUrl);
	}

	@Test
	public void testFactorHttpHandlerFactoryCreatedAndAddedToRestClient_dataClient() {
		DataClientImp dataClient = (DataClientImp) factory
				.factorDataClientUsingJavaClientAuthTokenCredentials(
						javaClientAuthTokenCredentials);

		RestClientImp restClient = (RestClientImp) dataClient.onlyForTestGetRestClient();

		assertTrue(restClient.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactoryImp);
	}

	@Test
	public void testFactorTokenClientWithoutAuthTokenRenewableCreatedCorrectlyAndAddedToRestClient_dataClient() {
		DataClientImp dataClient = (DataClientImp) factory
				.factorDataClientUsingJavaClientAuthTokenCredentials(
						javaClientAuthTokenCredentials);

		RestClientImp restClient = (RestClientImp) dataClient.onlyForTestGetRestClient();

		TokenClientImp tokenClient = (TokenClientImp) restClient.onlyForTestGetTokenClient();
		assertTokenClientCredentialsWithoutAuthTokenRenewable(tokenClient);
	}

	@Test
	public void testFactorTokenClientCreatedCorrectlyAndAddedToRestClient_dataClient() {
		JavaClientAuthTokenCredentials authTokenCredentialsWithRenewableAuthToken = new JavaClientAuthTokenCredentials(
				baseUrl, RENEW_URL, authToken, true);

		DataClientImp dataClient = (DataClientImp) factory
				.factorDataClientUsingJavaClientAuthTokenCredentials(
						authTokenCredentialsWithRenewableAuthToken);

		RestClientImp restClient = (RestClientImp) dataClient.onlyForTestGetRestClient();
		TokenClientImp tokenClient = (TokenClientImp) restClient.onlyForTestGetTokenClient();
		assertTokenClientCredentials(tokenClient);
	}

	@Test
	public void testFactorAppBaseUrlAddedToRestClient_dataClient() {
		DataClientImp dataClient = (DataClientImp) factory
				.factorDataClientUsingJavaClientAppTokenCredentials(javaClientAppTokenCredentials);

		assertTrue(dataClient instanceof DataClientImp);
		RestClientImp restClient = (RestClientImp) dataClient.onlyForTestGetRestClient();

		assertTrue(restClient instanceof RestClientImp);
		assertEquals(restClient.onlyForTestGetBaseUrl(), baseUrl);
	}

	@Test
	public void testFactorAppHttpHandlerFactoryCreatedAndAddedToRestClient_dataClient() {
		DataClientImp dataClient = (DataClientImp) factory
				.factorDataClientUsingJavaClientAppTokenCredentials(javaClientAppTokenCredentials);

		RestClientImp restClient = (RestClientImp) dataClient.onlyForTestGetRestClient();

		assertTrue(restClient.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactoryImp);
	}

	@Test
	public void testFactorAppTokenClientCreatedCorrectlyAndAddedToRestClient_dataClient() {
		DataClientImp dataClient = (DataClientImp) factory
				.factorDataClientUsingJavaClientAppTokenCredentials(javaClientAppTokenCredentials);

		RestClientImp restClient = (RestClientImp) dataClient.onlyForTestGetRestClient();

		TokenClientImp tokenClient = (TokenClientImp) restClient.onlyForTestGetTokenClient();
		assertTokenClientUsingAppTokenCredentials(tokenClient);
	}

	@Test
	public void testFactorTokenClientUsingAppTokenCredentials() {

		TokenClientImp tokenClient = (TokenClientImp) factory
				.factorTokenClientUsingAppTokenCredentials(appTokenCredentials);

		assertTokenClientUsingAppTokenCredentials(tokenClient);
	}

	@Test
	public void testFactorTokenClientUsingAuthTokenCredentials() {

		TokenClientImp tokenClient = (TokenClientImp) factory
				.factorTokenClientUsingAuthTokenCredentials(authTokenCredentials);

		assertTokenClientCredentials(tokenClient);
	}
}
