/*
 * Copyright 2018, 2020, 2023 Uppsala University Library
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

import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.javaclient.JavaClientAppTokenCredentials;
import se.uu.ub.cora.javaclient.JavaClientAuthTokenCredentials;
import se.uu.ub.cora.javaclient.JavaClientFactory;
import se.uu.ub.cora.javaclient.data.DataClient;
import se.uu.ub.cora.javaclient.data.internal.DataClientImp;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.internal.RestClientImp;
import se.uu.ub.cora.javaclient.token.TokenClient;
import se.uu.ub.cora.javaclient.token.internal.AppTokenCredentials;
import se.uu.ub.cora.javaclient.token.internal.AuthTokenCredentials;
import se.uu.ub.cora.javaclient.token.internal.TokenClientImp;

public class JavaClientFactoryImp implements JavaClientFactory {

	@Override
	public RestClient factorRestClientUsingAuthTokenCredentials(
			JavaClientAuthTokenCredentials authTokenCredentials) {
		TokenClient tokenClient = createTokenClientForAuthToken(authTokenCredentials.appTokenUrl(),
				authTokenCredentials.authToken());
		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();

		return RestClientImp.usingHttpHandlerFactoryAndBaseUrlAndTokenClient(httpHandlerFactory,
				authTokenCredentials.baseUrl(), tokenClient);
	}

	private TokenClient createTokenClientForAuthToken(String appTokenUrl, String authToken) {
		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();
		AuthTokenCredentials authTokenCredentials = new AuthTokenCredentials(appTokenUrl,
				authToken);
		return TokenClientImp.usingHttpHandlerFactoryAndAuthToken(httpHandlerFactory,
				authTokenCredentials);
	}

	@Override
	public RestClient factorRestClientUsingAppTokenCredentials(
			JavaClientAppTokenCredentials appTokenCredentials) {
		TokenClient tokenClient = createTokenClientForUserIdAndAppToken(
				appTokenCredentials.appTokenUrl(), appTokenCredentials.userId(),
				appTokenCredentials.appToken());
		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();
		return RestClientImp.usingHttpHandlerFactoryAndBaseUrlAndTokenClient(httpHandlerFactory,
				appTokenCredentials.baseUrl(), tokenClient);
	}

	private TokenClient createTokenClientForUserIdAndAppToken(String appTokenUrl, String userId,
			String appToken) {
		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();
		AppTokenCredentials appTokenCredentials = new AppTokenCredentials(appTokenUrl, userId,
				appToken);
		return TokenClientImp.usingHttpHandlerFactoryAndAppToken(httpHandlerFactory,
				appTokenCredentials);
	}

	@Override
	public DataClient factorDataClientUsingAuthTokenCredentials(
			JavaClientAuthTokenCredentials authTokenCredentials) {
		RestClient restClient = factorRestClientUsingAuthTokenCredentials(authTokenCredentials);
		return new DataClientImp(restClient);
	}

	@Override
	public DataClient factorDataClientUsingAppTokenCredentials(
			JavaClientAppTokenCredentials appTokenCredentials) {
		RestClient restClient = factorRestClientUsingAppTokenCredentials(appTokenCredentials);
		return new DataClientImp(restClient);
	}
}
