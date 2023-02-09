/*
 * Copyright 2018, 2019, 2020, 2023 Uppsala University Library
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

import se.uu.ub.cora.javaclient.cora.internal.DataClientImp;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestClientFactory;
import se.uu.ub.cora.javaclient.rest.RestClientFactoryImp;

public final class DataClientFactoryImp implements CoraClientFactory {
	private RestClientFactory restClientFactory;
	private String appTokenVerifierUrl;
	private String baseUrl;

	public static DataClientFactoryImp usingAppTokenVerifierUrlAndBaseUrl(
			String appTokenVerifierUrl, String baseUrl) {
		return new DataClientFactoryImp(appTokenVerifierUrl, baseUrl);
	}

	private DataClientFactoryImp(String appTokenVerifierUrl, String baseUrl) {
		this.appTokenVerifierUrl = appTokenVerifierUrl;
		this.baseUrl = baseUrl;
		restClientFactory = RestClientFactoryImp.usingBaseUrlAndAppTokenVerifierUrl(baseUrl,
				appTokenVerifierUrl);
	}

	@Override
	public DataClient factorUsingUserIdAndAppToken(String userId, String appToken) {
		RestClient restClient = restClientFactory.factorUsingUserIdAndAppToken(userId, appToken);
		return new DataClientImp(restClient);
	}

	@Override
	public DataClient factorUsingAuthToken(String authToken) {
		RestClient restClient = restClientFactory.factorUsingAuthToken(authToken);
		return new DataClientImp(restClient);
	}

	public String onlyForTestGetBaseUrl() {
		return baseUrl;
	}

	public String onlyForTestGetAppTokenVerifierUrl() {
		return appTokenVerifierUrl;
	}

	public RestClientFactory onlyForTestGetRestClientFactory() {
		return restClientFactory;
	}

	void onlyForTestSetRestClientFactory(RestClientFactory restClientFactory) {
		this.restClientFactory = restClientFactory;
	}

}
