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

import se.uu.ub.cora.clientdata.converter.javatojson.DataToJsonConverterFactory;
import se.uu.ub.cora.clientdata.converter.javatojson.DataToJsonConverterFactoryImp;
import se.uu.ub.cora.clientdata.converter.jsontojava.JsonToDataConverterFactory;
import se.uu.ub.cora.clientdata.converter.jsontojava.JsonToDataConverterFactoryImp;
import se.uu.ub.cora.javaclient.CoraClientDependencies;
import se.uu.ub.cora.javaclient.CoraClientImp;
import se.uu.ub.cora.javaclient.RestClientCoraClient;
import se.uu.ub.cora.javaclient.apptoken.AppTokenClientFactoryImp;
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
		DataToJsonConverterFactory dataToJsonConverterFactory = new DataToJsonConverterFactoryImp();
		JsonToDataConverterFactory jsonToDataConverterFactory = new JsonToDataConverterFactoryImp();
		CoraClientDependencies coraClientDependencies = new CoraClientDependencies(
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
		DataToJsonConverterFactory dataToJsonConverterFactory = new DataToJsonConverterFactoryImp();
		JsonToDataConverterFactory jsonToDataConverterFactory = new JsonToDataConverterFactoryImp();
		return new RestClientCoraClient(restClient, dataToJsonConverterFactory,
				jsonToDataConverterFactory);
	}

}
