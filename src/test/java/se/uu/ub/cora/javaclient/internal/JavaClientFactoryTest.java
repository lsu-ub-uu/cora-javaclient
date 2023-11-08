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
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.javaclient.JavaClientAppTokenCredentials;
import se.uu.ub.cora.javaclient.JavaClientAuthTokenCredentials;
import se.uu.ub.cora.javaclient.JavaClientFactory;
import se.uu.ub.cora.javaclient.data.internal.DataClientImp;
import se.uu.ub.cora.javaclient.rest.internal.RestClientImp;
import se.uu.ub.cora.javaclient.token.internal.AppTokenCredentials;
import se.uu.ub.cora.javaclient.token.internal.AuthTokenCredentials;
import se.uu.ub.cora.javaclient.token.internal.TokenClientImp;

public class JavaClientFactoryTest {

	private JavaClientFactory factory;
	private String baseUrl = "someBaseUrl";
	private String authToken = "someAuthToken";
	private String appTokenUrl = "someAptokenUrl";
	private String userId = "someUserId";
	private String appToken = "someAppToken";
	private JavaClientAppTokenCredentials appTokenCredentials = new JavaClientAppTokenCredentials(
			baseUrl, appTokenUrl, userId, appToken);
	private JavaClientAuthTokenCredentials authTokenCredentials = new JavaClientAuthTokenCredentials(
			baseUrl, appTokenUrl, authToken);

	@BeforeMethod
	public void beforeMethod() {
		factory = new JavaClientFactoryImp();
	}

	@Test
	public void testFactorBaseUrlAddedToRestClient() throws Exception {
		RestClientImp restClient = (RestClientImp) factory
				.factorRestClientUsingAuthTokenCredentials(authTokenCredentials);

		assertTrue(restClient instanceof RestClientImp);
		assertEquals(restClient.onlyForTestGetBaseUrl(), baseUrl);
	}

	@Test
	public void testFactorHttpHandlerFactoryCreatedAndAddedToRestClient() throws Exception {
		RestClientImp restClient = (RestClientImp) factory
				.factorRestClientUsingAuthTokenCredentials(authTokenCredentials);

		assertTrue(restClient.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactoryImp);
	}

	@Test
	public void testFactorTokenClientCreatedCorrectlyAndAddedToRestClient() throws Exception {
		RestClientImp restClient = (RestClientImp) factory
				.factorRestClientUsingAuthTokenCredentials(authTokenCredentials);

		TokenClientImp tokenClient = (TokenClientImp) restClient.onlyForTestGetTokenClient();
		assertTrue(tokenClient.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactoryImp);

		AuthTokenCredentials authTokenCredentials = tokenClient
				.onlyForTestGetAuthTokenCredentials();

		assertEquals(authTokenCredentials.appTokenVerifierUrl(), appTokenUrl);
		assertEquals(authTokenCredentials.authToken(), authToken);
	}

	@Test
	public void testFactorAppBaseUrlAddedToRestClient() throws Exception {
		RestClientImp restClient = (RestClientImp) factory
				.factorRestClientUsingAppTokenCredentials(appTokenCredentials);

		assertTrue(restClient instanceof RestClientImp);
		assertEquals(restClient.onlyForTestGetBaseUrl(), baseUrl);
	}

	@Test
	public void testFactorAppHttpHandlerFactoryCreatedAndAddedToRestClient() throws Exception {
		RestClientImp restClient = (RestClientImp) factory
				.factorRestClientUsingAppTokenCredentials(appTokenCredentials);

		assertTrue(restClient.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactoryImp);
	}

	@Test
	public void testFactorAppTokenClientCreatedCorrectlyAndAddedToRestClient() throws Exception {
		RestClientImp restClient = (RestClientImp) factory
				.factorRestClientUsingAppTokenCredentials(appTokenCredentials);

		TokenClientImp tokenClient = (TokenClientImp) restClient.onlyForTestGetTokenClient();
		assertTrue(tokenClient.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactoryImp);

		AppTokenCredentials appTokenCredentials = tokenClient.onlyForTestGetAppTokenCredentials();

		assertEquals(appTokenCredentials.appTokenVerifierUrl(), appTokenUrl);
		assertEquals(appTokenCredentials.userId(), userId);
		assertEquals(appTokenCredentials.appToken(), appToken);
	}

	@Test
	public void testFactorBaseUrlAddedToRestClient_dataClient() throws Exception {
		DataClientImp dataClient = (DataClientImp) factory
				.factorDataClientUsingAuthTokenCredentials(authTokenCredentials);

		assertTrue(dataClient instanceof DataClientImp);
		RestClientImp restClient = (RestClientImp) dataClient.onlyForTestGetRestClient();
		assertEquals(restClient.onlyForTestGetBaseUrl(), baseUrl);
	}

	@Test
	public void testFactorHttpHandlerFactoryCreatedAndAddedToRestClient_dataClient()
			throws Exception {
		DataClientImp dataClient = (DataClientImp) factory
				.factorDataClientUsingAuthTokenCredentials(authTokenCredentials);

		RestClientImp restClient = (RestClientImp) dataClient.onlyForTestGetRestClient();

		assertTrue(restClient.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactoryImp);
	}

	@Test
	public void testFactorTokenClientCreatedCorrectlyAndAddedToRestClient_dataClient()
			throws Exception {
		DataClientImp dataClient = (DataClientImp) factory
				.factorDataClientUsingAuthTokenCredentials(authTokenCredentials);

		RestClientImp restClient = (RestClientImp) dataClient.onlyForTestGetRestClient();

		TokenClientImp tokenClient = (TokenClientImp) restClient.onlyForTestGetTokenClient();
		assertTrue(tokenClient.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactoryImp);

		AuthTokenCredentials authTokenCredentials = tokenClient
				.onlyForTestGetAuthTokenCredentials();

		assertEquals(authTokenCredentials.appTokenVerifierUrl(), appTokenUrl);
		assertEquals(authTokenCredentials.authToken(), authToken);
	}

	@Test
	public void testFactorAppBaseUrlAddedToRestClient_dataClient() throws Exception {
		DataClientImp dataClient = (DataClientImp) factory
				.factorDataClientUsingAppTokenCredentials(appTokenCredentials);

		assertTrue(dataClient instanceof DataClientImp);
		RestClientImp restClient = (RestClientImp) dataClient.onlyForTestGetRestClient();

		assertTrue(restClient instanceof RestClientImp);
		assertEquals(restClient.onlyForTestGetBaseUrl(), baseUrl);
	}

	@Test
	public void testFactorAppHttpHandlerFactoryCreatedAndAddedToRestClient_dataClient()
			throws Exception {
		DataClientImp dataClient = (DataClientImp) factory
				.factorDataClientUsingAppTokenCredentials(appTokenCredentials);

		RestClientImp restClient = (RestClientImp) dataClient.onlyForTestGetRestClient();

		assertTrue(restClient.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactoryImp);
	}

	@Test
	public void testFactorAppTokenClientCreatedCorrectlyAndAddedToRestClient_dataClient()
			throws Exception {
		DataClientImp dataClient = (DataClientImp) factory
				.factorDataClientUsingAppTokenCredentials(appTokenCredentials);

		RestClientImp restClient = (RestClientImp) dataClient.onlyForTestGetRestClient();

		TokenClientImp tokenClient = (TokenClientImp) restClient.onlyForTestGetTokenClient();
		assertTrue(tokenClient.onlyForTestGetHttpHandlerFactory() instanceof HttpHandlerFactoryImp);

		AppTokenCredentials appTokenCredentials = tokenClient.onlyForTestGetAppTokenCredentials();

		assertEquals(appTokenCredentials.appTokenVerifierUrl(), appTokenUrl);
		assertEquals(appTokenCredentials.userId(), userId);
		assertEquals(appTokenCredentials.appToken(), appToken);
	}

}
