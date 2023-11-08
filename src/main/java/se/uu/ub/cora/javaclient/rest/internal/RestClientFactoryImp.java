/*
 * Copyright 2018, 2020, 2023 Uppsala University Library
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
package se.uu.ub.cora.javaclient.rest.internal;

import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestClientFactory;
import se.uu.ub.cora.javaclient.token.TokenClient;
import se.uu.ub.cora.javaclient.token.internal.AppTokenCredentials;
import se.uu.ub.cora.javaclient.token.internal.AuthTokenCredentials;
import se.uu.ub.cora.javaclient.token.internal.TokenClientImp;

public class RestClientFactoryImp implements RestClientFactory {

	@Override
	public RestClient factorUsingBaseUrlAndAppTokenVerifierUrlAndAuthToken(String baseUrl,
			String appTokenUrl, String authToken) {
		TokenClient tokenClient = createTokenClientForAuthToken(appTokenUrl, authToken);
		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();

		return RestClientImp.usingHttpHandlerFactoryAndBaseUrlAndTokenClient(httpHandlerFactory,
				baseUrl, tokenClient);
	}

	private TokenClient createTokenClientForAuthToken(String appTokenUrl, String authToken) {
		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();
		AuthTokenCredentials authTokenCredentials = new AuthTokenCredentials(appTokenUrl,
				authToken);
		return TokenClientImp.usingHttpHandlerFactoryAndAuthToken(httpHandlerFactory,
				authTokenCredentials);
	}

	@Override
	public RestClient factorUsingBaseUrlAndAppTokenUrlAndUserIdAndAppToken(String baseUrl,
			String appTokenUrl, String userId, String appToken) {
		TokenClient tokenClient = createTokenClientForUserIdAndAppToken(appTokenUrl, userId,
				appToken);
		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();
		return RestClientImp.usingHttpHandlerFactoryAndBaseUrlAndTokenClient(httpHandlerFactory,
				baseUrl, tokenClient);
	}

	private TokenClient createTokenClientForUserIdAndAppToken(String appTokenUrl, String userId,
			String appToken) {
		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();
		AppTokenCredentials appTokenCredentials = new AppTokenCredentials(appTokenUrl, userId,
				appToken);
		return TokenClientImp.usingHttpHandlerFactoryAndAppToken(httpHandlerFactory,
				appTokenCredentials);
	}
}
