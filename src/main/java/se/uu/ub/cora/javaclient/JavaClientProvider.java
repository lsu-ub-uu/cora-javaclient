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

import se.uu.ub.cora.javaclient.data.DataClient;
import se.uu.ub.cora.javaclient.data.DataClientFactory;
import se.uu.ub.cora.javaclient.data.internal.DataClientFactoryImp;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestClientFactory;
import se.uu.ub.cora.javaclient.rest.internal.RestClientFactoryImp;

public class JavaClientProvider {

	private static RestClientFactory restClientFactory = new RestClientFactoryImp();
	private static DataClientFactory dataClientFactory = new DataClientFactoryImp();
	private static RestClientFactory onlyForTestRestClientFactory;
	private static DataClientFactory onlyForTestDataClientFactory;

	public static RestClient getRestClientUsingBaseUrlAndApptokenUrlAndAuthToken(String baseUrl,
			String appTokenVerifierUrl, String authToken) {
		return getRestClientFactory().factorUsingBaseUrlAndAppTokenVerifierUrlAndAuthToken(baseUrl,
				appTokenVerifierUrl, authToken);
	}

	public static RestClient getRestClientUsingBaseUrlAndApptokenUrlAndUserIdAndAppToken(
			String someBaseUrl, String someAppTokenVerifierUrl, String someUserId,
			String someAppToken) {
		return getRestClientFactory().factorUsingBaseUrlAndAppTokenUrlAndUserIdAndAppToken(
				someBaseUrl, someAppTokenVerifierUrl, someUserId, someAppToken);
	}

	public static DataClient getDataClientUsingBaseUrlAndApptokenUrlAndAuthToken(String baseUrl,
			String appTokenVerifierUrl, String authToken) {
		RestClient restClient = getRestClientUsingBaseUrlAndApptokenUrlAndAuthToken(baseUrl,
				appTokenVerifierUrl, authToken);
		return getDataClientFactory().factorUsingRestClient(restClient);
	}

	private static RestClientFactory getRestClientFactory() {
		if (onlyForTestRestClientFactory == null) {
			return restClientFactory;
		}
		return onlyForTestRestClientFactory;
	}

	private static DataClientFactory getDataClientFactory() {
		if (onlyForTestDataClientFactory == null) {
			return dataClientFactory;
		}
		return onlyForTestDataClientFactory;
	}

	public static void onlyForTestSetRestClientFactory(RestClientFactory restClientFactory) {
		JavaClientProvider.onlyForTestRestClientFactory = restClientFactory;

	}

	public static void onlyForTestSetDataClientFactory(DataClientFactory dataClientFactory) {
		JavaClientProvider.onlyForTestDataClientFactory = dataClientFactory;
	}

}
