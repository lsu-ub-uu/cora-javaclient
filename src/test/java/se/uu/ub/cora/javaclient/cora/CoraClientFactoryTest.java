/*
 * Copyright 2018, 2019 Uppsala University Library
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

import se.uu.ub.cora.clientdata.converter.javatojson.DataToJsonConverterFactory;
import se.uu.ub.cora.clientdata.converter.javatojson.DataToJsonConverterFactoryImp;
import se.uu.ub.cora.clientdata.converter.jsontojava.JsonToDataConverterFactory;
import se.uu.ub.cora.clientdata.converter.jsontojava.JsonToDataConverterFactoryImp;
import se.uu.ub.cora.javaclient.CoraClientImp;
import se.uu.ub.cora.javaclient.CoraClientWithRestClient;
import se.uu.ub.cora.javaclient.RestClientImp;
import se.uu.ub.cora.javaclient.apptoken.AppTokenClientFactoryImp;
import se.uu.ub.cora.javaclient.rest.RestClientFactoryImp;

public class CoraClientFactoryTest {
	// private CoraClientImp coraClient;
	private String appTokenVerifierUrl;
	private String baseUrl;
	private CoraClientFactoryImp clientFactory;

	@BeforeMethod
	public void beforeMethod() {
		appTokenVerifierUrl = "someVerifierUrl";
		baseUrl = "someBaseUrl";
		clientFactory = CoraClientFactoryImp.usingAppTokenVerifierUrlAndBaseUrl(appTokenVerifierUrl,
				baseUrl);
	}

	@Test
	public void testCorrectFactoriesAreSentToCoraClient() throws Exception {
		CoraClientImp coraClient = (CoraClientImp) clientFactory.factor("someUserId",
				"someAppToken");

		AppTokenClientFactoryImp appTokenClientFactory = (AppTokenClientFactoryImp) coraClient
				.getAppTokenClientFactory();
		assertEquals(appTokenClientFactory.getAppTokenVerifierUrl(), appTokenVerifierUrl);

		RestClientFactoryImp restClientFactory = (RestClientFactoryImp) coraClient
				.getRestClientFactory();
		assertEquals(restClientFactory.getBaseUrl(), baseUrl);

		DataToJsonConverterFactory dataToJsonConverterFactory = coraClient
				.getDataToJsonConverterFactory();
		assertTrue(dataToJsonConverterFactory instanceof DataToJsonConverterFactoryImp);

		JsonToDataConverterFactory jsonToDataConverterFactory = coraClient
				.getJsonToDataConverterFactory();
		assertTrue(jsonToDataConverterFactory instanceof JsonToDataConverterFactoryImp);
	}

	@Test
	public void testFactorParametersSentAlong() throws Exception {
		CoraClientImp coraClient = (CoraClientImp) clientFactory.factor("someUserId",
				"someAppToken");
		assertEquals(coraClient.getUserId(), "someUserId");
		assertEquals(coraClient.getAppToken(), "someAppToken");
	}

	@Test
	public void testCorrectFactoriesAreSentToCoraClientWhenUsingAuthToken() throws Exception {
		CoraClientWithRestClient coraClient = (CoraClientWithRestClient) clientFactory
				.factorUsingAuthToken("someAuthTokenToken");

		RestClientImp restClient = (RestClientImp) coraClient.getRestClient();
		assertEquals(restClient.getBaseUrl(), baseUrl + "record/");
		assertEquals(restClient.getAuthToken(), "someAuthTokenToken");

		DataToJsonConverterFactory dataToJsonConverterFactory = coraClient
				.getDataToJsonConverterFactory();
		assertTrue(dataToJsonConverterFactory instanceof DataToJsonConverterFactoryImp);

		JsonToDataConverterFactory jsonToDataConverterFactory = coraClient
				.getJsonToDataConverterFactory();
		assertTrue(jsonToDataConverterFactory instanceof JsonToDataConverterFactoryImp);
	}

	@Test
	public void testGetAppTokenVerifierUrl() throws Exception {
		assertEquals(clientFactory.getAppTokenVerifierUrl(), appTokenVerifierUrl);
	}

	@Test
	public void testGetBaseUrl() throws Exception {
		assertEquals(clientFactory.getBaseUrl(), baseUrl);
	}
}
