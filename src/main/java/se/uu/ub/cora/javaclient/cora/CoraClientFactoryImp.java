/*
 * Copyright 2018, 2019, 2020 Uppsala University Library
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

import se.uu.ub.cora.clientbasicdata.converter.datatojson.BasicClientDataToJsonConverterFactory;
import se.uu.ub.cora.clientbasicdata.converter.jsontodata.JsonToBasicClientDataConverterFactoryImp;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterFactory;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterFactory;
import se.uu.ub.cora.javaclient.apptoken.AppTokenClientFactoryImp;
import se.uu.ub.cora.javaclient.cora.internal.ApptokenBasedClientDependencies;
import se.uu.ub.cora.javaclient.cora.internal.AuthtokenBasedClient;
import se.uu.ub.cora.javaclient.cora.internal.CoraClientImp;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestClientFactoryImp;

public final class CoraClientFactoryImp implements CoraClientFactory {

	private AppTokenClientFactoryImp appTokenClientFactory;
	private RestClientFactoryImp restClientFactory;
	private String appTokenVerifierUrl;
	private String baseUrl;

	public static CoraClientFactoryImp usingAppTokenVerifierUrlAndBaseUrl(
			String appTokenVerifierUrl, String baseUrl) {
		return new CoraClientFactoryImp(appTokenVerifierUrl, baseUrl);
	}

	private CoraClientFactoryImp(String appTokenVerifierUrl, String baseUrl) {
		this.appTokenVerifierUrl = appTokenVerifierUrl;
		this.baseUrl = baseUrl;
		appTokenClientFactory = new AppTokenClientFactoryImp(appTokenVerifierUrl);
		restClientFactory = new RestClientFactoryImp(baseUrl);
	}

	@Override
	public CoraClient factor(String userId, String appToken) {
		// ClientDataToJsonConverterFactory dataToJsonConverterFactory =
		// BasicClientDataToJsonConverterFactory
		// .usingBuilderFactory(jsonBuilderFactory);
		ClientDataToJsonConverterFactory dataToJsonConverterFactory = BasicClientDataToJsonConverterFactory
				.usingBuilderFactory(null);
		JsonToClientDataConverterFactory jsonToDataConverterFactory = new JsonToBasicClientDataConverterFactoryImp();
		ApptokenBasedClientDependencies coraClientDependencies = new ApptokenBasedClientDependencies(
				appTokenClientFactory, restClientFactory, dataToJsonConverterFactory,
				jsonToDataConverterFactory, userId, appToken);

		return new CoraClientImp(coraClientDependencies);
	}

	public String getAppTokenVerifierUrl() {
		// needed for test
		return appTokenVerifierUrl;
	}

	public String getBaseUrl() {
		// needed for test
		return baseUrl;
	}

	@Override
	public CoraClient factorUsingAuthToken(String authToken) {
		RestClient restClient = restClientFactory.factorUsingAuthToken(authToken);
		// ClientDataToJsonConverterFactory dataToJsonConverterFactory = new
		// BasicClientDataToJsonConverterFactory.usingBuilderFactory(
		// null);
		// JsonToClientDataConverterFactory jsonToDataConverterFactory = new
		// JsonToClientDataConverterFactoryImp();
		// return new AuthtokenBasedClient(restClient, dataToJsonConverterFactory,
		// jsonToDataConverterFactory);
		return new AuthtokenBasedClient(restClient, null, null);
	}

}
