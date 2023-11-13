/*
 * Copyright 2018 Uppsala University Library
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
package se.uu.ub.cora.javaclient.token;

import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.javaclient.AppTokenCredentials;
import se.uu.ub.cora.javaclient.AuthTokenCredentials;
import se.uu.ub.cora.javaclient.token.internal.TokenClientImp;

public class TokenClientFactoryImp implements TokenClientFactory {

	private String appTokenVerifierUrl;

	public TokenClientFactoryImp(String appTokenVerifierUrl) {
		this.appTokenVerifierUrl = appTokenVerifierUrl;
	}

	@Override
	public TokenClient factorUsingUserIdAndAppToken(String userId, String appToken) {
		AppTokenCredentials credentials = new AppTokenCredentials(appTokenVerifierUrl, userId,
				appToken);
		return TokenClientImp.usingHttpHandlerFactoryAndAppToken(createHttpHandler(), credentials);
	}

	@Override
	public TokenClient factorUsingAuthToken(String authToken) {
		AuthTokenCredentials credentials = new AuthTokenCredentials(appTokenVerifierUrl, authToken);
		return TokenClientImp.usingHttpHandlerFactoryAndAuthToken(createHttpHandler(), credentials);
	}

	private HttpHandlerFactoryImp createHttpHandler() {
		return new HttpHandlerFactoryImp();
	}

	public String onlyForTestGetAppTokenVerifierUrl() {
		// needed for test
		return appTokenVerifierUrl;
	}

}
