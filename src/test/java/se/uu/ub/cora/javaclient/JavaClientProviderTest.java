/*
 * Copyright 2023 Uppsala University Library
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

import static org.testng.Assert.assertTrue;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.javaclient.data.DataClientFactorySpy;
import se.uu.ub.cora.javaclient.data.DataClientSpy;
import se.uu.ub.cora.javaclient.data.internal.DataClientImp;
import se.uu.ub.cora.javaclient.doubles.RestClientFactorySpy;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.internal.RestClientImp;

public class JavaClientProviderTest {

	private static final String SOME_USER_ID = "someUserId";
	private static final String SOME_APP_TOKEN = "someAppToken";
	private static final String SOME_AUTH_TOKEN = "someAuthToken";
	private static final String SOME_APP_TOKEN_VERIFIER_URL = "someAppTokenVerifierUrl";
	private static final String SOME_BASE_URL = "someBaseUrl";

	@AfterMethod
	private void afterMethod() {
		JavaClientProvider.onlyForTestSetDataClientFactory(null);
		JavaClientProvider.onlyForTestSetRestClientFactory(null);
	}

	@Test
	public void testGetRestClientUsingBaseUrlAndApptokenUrlAndAuthToken() throws Exception {
		RestClientImp restClient = (RestClientImp) JavaClientProvider
				.getRestClientUsingBaseUrlAndApptokenUrlAndAuthToken(SOME_BASE_URL,
						SOME_APP_TOKEN_VERIFIER_URL, SOME_AUTH_TOKEN);
		assertTrue(restClient instanceof RestClientImp);
	}

	@Test
	public void testOnlyForTestSetRestClientFactory() throws Exception {
		RestClientFactorySpy restClientFactory = new RestClientFactorySpy();
		JavaClientProvider.onlyForTestSetRestClientFactory(restClientFactory);

		RestClient restClient = JavaClientProvider
				.getRestClientUsingBaseUrlAndApptokenUrlAndAuthToken(SOME_BASE_URL,
						SOME_APP_TOKEN_VERIFIER_URL, SOME_AUTH_TOKEN);

		restClientFactory.MCR.assertParameters(
				"factorUsingBaseUrlAndAppTokenVerifierUrlAndAuthToken", 0, SOME_BASE_URL,
				SOME_APP_TOKEN_VERIFIER_URL, SOME_AUTH_TOKEN);
		restClientFactory.MCR.assertReturn("factorUsingBaseUrlAndAppTokenVerifierUrlAndAuthToken",
				0, restClient);
	}

	@Test
	public void testGetRestClientUsingBaseUrlAndApptokenUrlAndUserIdAndAppToken() throws Exception {
		RestClientImp restClient = (RestClientImp) JavaClientProvider
				.getRestClientUsingBaseUrlAndApptokenUrlAndUserIdAndAppToken(SOME_BASE_URL,
						SOME_APP_TOKEN_VERIFIER_URL, SOME_USER_ID, SOME_APP_TOKEN);
		assertTrue(restClient instanceof RestClientImp);
	}

	@Test
	public void testGetRestClientUsingBaseUrlAndApptokenUrlAndUserIdAndAppTokenPassedParameteres()
			throws Exception {
		RestClientFactorySpy restClientFactory = new RestClientFactorySpy();
		JavaClientProvider.onlyForTestSetRestClientFactory(restClientFactory);

		RestClient restClient = JavaClientProvider
				.getRestClientUsingBaseUrlAndApptokenUrlAndUserIdAndAppToken(SOME_BASE_URL,
						SOME_APP_TOKEN_VERIFIER_URL, SOME_USER_ID, SOME_APP_TOKEN);

		restClientFactory.MCR.assertParameters(
				"factorUsingBaseUrlAndAppTokenUrlAndUserIdAndAppToken", 0, SOME_BASE_URL,
				SOME_APP_TOKEN_VERIFIER_URL, SOME_USER_ID, SOME_APP_TOKEN);
		restClientFactory.MCR.assertReturn("factorUsingBaseUrlAndAppTokenUrlAndUserIdAndAppToken",
				0, restClient);
	}

	@Test
	public void testGetDataClientUsingBaseUrlAndApptokenUrlAndAuthToken() throws Exception {
		// RestClientFactorySpy restClientFactory = new RestClientFactorySpy();
		// JavaClientProvider.onlyForTestSetRestClientFactory(restClientFactory);

		DataClientImp dataClient = (DataClientImp) JavaClientProvider
				.getDataClientUsingBaseUrlAndApptokenUrlAndAuthToken(SOME_BASE_URL,
						SOME_APP_TOKEN_VERIFIER_URL, SOME_AUTH_TOKEN);
		assertTrue(dataClient instanceof DataClientImp);
		assertTrue(dataClient.onlyForTestGetRestClient() instanceof RestClientImp);
	}

	@Test
	public void testGetDataClientUsingBaseUrlAndApptokenUrlAndAuthTokenPassedOn() throws Exception {
		RestClientFactorySpy restClientFactory = new RestClientFactorySpy();
		JavaClientProvider.onlyForTestSetRestClientFactory(restClientFactory);

		DataClientFactorySpy dataClientFactory = new DataClientFactorySpy();
		JavaClientProvider.onlyForTestSetDataClientFactory(dataClientFactory);

		DataClientSpy dataClient = (DataClientSpy) JavaClientProvider
				.getDataClientUsingBaseUrlAndApptokenUrlAndAuthToken(SOME_BASE_URL,
						SOME_APP_TOKEN_VERIFIER_URL, SOME_AUTH_TOKEN);
		dataClientFactory.MCR.assertParameters("factorUsingRestClient", 0, restClientFactory.MCR
				.getReturnValue("factorUsingBaseUrlAndAppTokenVerifierUrlAndAuthToken", 0));
	}

	// @Test
	// public void testOnlyForTestSetCoraClientFactory() throws Exception {
	// DataClientFactory dataClientFactory = new DataClientFactorySpy();
	// JavaClientProvider.onlyForTestSetDataClientFactory(dataClientFactory);
	//
	// DataClient dataClient = JavaClientProvider
	// .getDataClientUsingApptokenVerifierUrlAndBaseUrlAndAuthToken(
	// SOME_APP_TOKEN_VERIFIER_URL, SOME_BASE_URL, SOME_AUTH_TOKEN);
	// assertTrue(dataClient instanceof DataClientSpy);
	//
	// }

}
