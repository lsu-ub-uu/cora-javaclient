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

import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterFactory;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterProvider;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterFactory;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterProvider;
import se.uu.ub.cora.javaclient.apptoken.AppTokenClientFactoryImp;
import se.uu.ub.cora.javaclient.cora.internal.AuthtokenBasedClient;
import se.uu.ub.cora.javaclient.cora.internal.DataClientImp;
import se.uu.ub.cora.javaclient.rest.RestClientFactoryImp;
import se.uu.ub.cora.javaclient.rest.internal.RestClientImp;

public class DataClientFactoryTest {
	private String appTokenVerifierUrl;
	private String baseUrl;
	private DataClientFactoryImp clientFactory;

	@BeforeMethod
	public void beforeMethod() {
		ClientDataToJsonConverterProvider.setDataToJsonConverterFactoryCreator(null);
		JsonToClientDataConverterProvider.setJsonToDataConverterFactory(null);
		appTokenVerifierUrl = "someVerifierUrl";
		baseUrl = "someBaseUrl";
		clientFactory = DataClientFactoryImp.usingAppTokenVerifierUrlAndBaseUrl(appTokenVerifierUrl,
				baseUrl);
	}

	@Test
	public void testCorrectFactoriesAreSentToCoraClient() throws Exception {
		DataClientImp coraClient = (DataClientImp) clientFactory.factor("someUserId",
				"someAppToken");

		AppTokenClientFactoryImp appTokenClientFactory = (AppTokenClientFactoryImp) coraClient
				.getAppTokenClientFactory();
		assertEquals(appTokenClientFactory.onlyForTestGetAppTokenVerifierUrl(), appTokenVerifierUrl);

		RestClientFactoryImp restClientFactory = (RestClientFactoryImp) coraClient
				.getRestClientFactory();
		assertEquals(restClientFactory.getBaseUrl(), baseUrl);

		// ClientDataToJsonConverterFactory dataToJsonConverterFactory = coraClient
		// .getDataToJsonConverterFactory();
		// assertTrue(dataToJsonConverterFactory instanceof ClientDataToJsonConverterFactory);
		//
		// JsonToClientDataConverterFactory jsonToDataConverterFactory = coraClient
		// .getJsonToDataConverterFactory();
		// assertTrue(jsonToDataConverterFactory instanceof JsonToDataConverterFactoryImp);
	}

	@Test
	public void testFactorParametersSentAlong() throws Exception {
		DataClientImp coraClient = (DataClientImp) clientFactory.factor("someUserId",
				"someAppToken");
		assertEquals(coraClient.getUserId(), "someUserId");
		assertEquals(coraClient.getAppToken(), "someAppToken");
	}

	@Test
	public void testCorrectFactoriesAreSentToCoraClientWhenUsingAuthToken() throws Exception {
		AuthtokenBasedClient coraClient = (AuthtokenBasedClient) clientFactory
				.factorUsingAuthToken("someAuthTokenToken");

		RestClientImp restClient = (RestClientImp) coraClient.getRestClient();
		assertEquals(restClient.getBaseUrl(), baseUrl + "record/");
		assertEquals(restClient.onlyForTestGetTokenClient(), "someAuthTokenToken");

		ClientDataToJsonConverterFactory dataToJsonConverterFactory = coraClient
				.getDataToJsonConverterFactory();
		assertTrue(dataToJsonConverterFactory instanceof DataToJsonConverterFactoryImp);

		JsonToClientDataConverterFactory jsonToDataConverterFactory = coraClient
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
