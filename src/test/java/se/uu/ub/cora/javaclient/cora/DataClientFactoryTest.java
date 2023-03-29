/*
 * Copyright 2018, 2019, 2023 Uppsala University Library
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
package se.uu.ub.cora.javaclient.cora;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterProvider;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterProvider;
import se.uu.ub.cora.javaclient.cora.internal.DataClientImp;
import se.uu.ub.cora.javaclient.doubles.RestClientFactorySpy;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestClientFactoryImp;

public class DataClientFactoryTest {
	private static final String APP_TOKEN = "someAppToken";
	private static final String USER_ID = "someUserId";
	private String appTokenVerifierUrl;
	private String baseUrl;
	private DataClientFactoryImp clientFactory;
	private RestClientFactorySpy restClientFactorySpy;

	@BeforeMethod
	public void beforeMethod() {
		ClientDataToJsonConverterProvider.setDataToJsonConverterFactoryCreator(null);
		JsonToClientDataConverterProvider.setJsonToDataConverterFactory(null);
		restClientFactorySpy = new RestClientFactorySpy();

		appTokenVerifierUrl = "someVerifierUrl";
		baseUrl = "someBaseUrl";
		clientFactory = DataClientFactoryImp.usingAppTokenVerifierUrlAndBaseUrl(appTokenVerifierUrl,
				baseUrl);
	}

	@Test
	public void testInit() throws Exception {
		RestClientFactoryImp restClientFactory = (RestClientFactoryImp) clientFactory
				.onlyForTestGetRestClientFactory();

		assertTrue(restClientFactory instanceof RestClientFactoryImp);
		assertEquals(restClientFactory.onlyForTestGetBaseUrl(), baseUrl);
		assertEquals(restClientFactory.onlyForTestGetAppTokenVerifierUrl(), appTokenVerifierUrl);
	}

	@Test
	public void testFactorUsingUserIdAndAppToken() throws Exception {
		clientFactory.onlyForTestSetRestClientFactory(restClientFactorySpy);

		DataClientImp dataClient = (DataClientImp) clientFactory
				.factorUsingUserIdAndAppToken(USER_ID, APP_TOKEN);

		restClientFactorySpy.MCR.assertParameters("factorUsingUserIdAndAppToken", 0, USER_ID,
				APP_TOKEN);
		RestClient restClient = dataClient.onlyForTestGetRestClient();
		restClientFactorySpy.MCR.assertReturn("factorUsingUserIdAndAppToken", 0, restClient);
	}

	@Test
	public void testFactorUsingAuthToken() throws Exception {
		clientFactory.onlyForTestSetRestClientFactory(restClientFactorySpy);

		DataClientImp dataClient = (DataClientImp) clientFactory
				.factorUsingAuthToken("someAuthToken");

		restClientFactorySpy.MCR.assertParameters("factorUsingAuthToken", 0, "someAuthToken");

		RestClient restClient = dataClient.onlyForTestGetRestClient();
		restClientFactorySpy.MCR.assertReturn("factorUsingAuthToken", 0, restClient);
	}

	@Test
	public void testGetAppTokenVerifierUrl() throws Exception {
		assertEquals(clientFactory.onlyForTestGetAppTokenVerifierUrl(), appTokenVerifierUrl);
	}

	@Test
	public void testGetBaseUrl() throws Exception {
		assertEquals(clientFactory.onlyForTestGetBaseUrl(), baseUrl);
	}
}
