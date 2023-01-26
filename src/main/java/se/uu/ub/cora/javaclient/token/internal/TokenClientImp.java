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
package se.uu.ub.cora.javaclient.token.internal;

import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.javaclient.cora.CoraClientException;
import se.uu.ub.cora.javaclient.token.TokenClient;

public final class TokenClientImp implements TokenClient {

	private static final String CORA_REST_APPTOKEN_ENDPOINT = "rest/apptoken/";
	private static final int CREATED = 201;
	private static final int DISTANCE_TO_START_OF_TOKEN = 21;
	private HttpHandlerFactory httpHandlerFactory;
	private String appTokenVerifierUrl;
	private String userId;
	private String appToken;
	private String authToken;
	private AppTokenCredentials appTokenCredentials;
	private AuthTokenCredentials authTokenCredentials;

	public static TokenClientImp usingHttpHandlerFactoryAndAppToken(
			HttpHandlerFactory httpHandlerFactory, AppTokenCredentials credentials) {
		return new TokenClientImp(httpHandlerFactory, credentials);
	}

	public static TokenClient usingHttpHandlerFactoryAndAuthToken(
			HttpHandlerFactory httpHandlerFactory, AuthTokenCredentials credentials) {
		return new TokenClientImp(httpHandlerFactory, credentials);
	}

	public TokenClientImp(HttpHandlerFactory httpHandlerFactory, AppTokenCredentials credentials) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.appTokenCredentials = credentials;
		this.appTokenVerifierUrl = credentials.appTokenVerifierUrl() + CORA_REST_APPTOKEN_ENDPOINT;
		this.userId = credentials.userId();
		this.appToken = credentials.appToken();
	}

	public TokenClientImp(HttpHandlerFactory httpHandlerFactory, AuthTokenCredentials credentials) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.authTokenCredentials = credentials;
		this.authToken = credentials.authToken();
	}

	@Override
	public String getAuthToken() {
		if (authTokenNeedsToBeFetched()) {
			fetchAuthTokenFromServer();
		}
		return authToken;
	}

	private boolean authTokenNeedsToBeFetched() {
		return null == authToken;
	}

	private void fetchAuthTokenFromServer() {
		HttpHandler httpHandler = createHttpHandler(userId);
		createAuthTokenUsingHttpHandler(appToken, httpHandler);
		authToken = possiblyGetAuthTokenFromAnswer(httpHandler);
	}

	private HttpHandler createHttpHandler(String userId) {
		return httpHandlerFactory.factor(appTokenVerifierUrl + userId);
	}

	private void createAuthTokenUsingHttpHandler(String appToken, HttpHandler httpHandler) {
		httpHandler.setRequestMethod("POST");
		httpHandler.setOutput(appToken);
	}

	private String possiblyGetAuthTokenFromAnswer(HttpHandler httpHandler) {
		if (CREATED == httpHandler.getResponseCode()) {
			return getAuthToken(httpHandler);
		}
		throw new CoraClientException("Could not create authToken");
	}

	private String getAuthToken(HttpHandler httpHandler) {
		String responseText = httpHandler.getResponseText();
		return extractCreatedTokenFromResponseText(responseText);
	}

	private String extractCreatedTokenFromResponseText(String responseText) {
		int idIndex = responseText.lastIndexOf("\"name\":\"id\"") + DISTANCE_TO_START_OF_TOKEN;
		return responseText.substring(idIndex, responseText.indexOf('"', idIndex));
	}

	public HttpHandlerFactory onlyForTestGetHttpHandlerFactory() {
		return httpHandlerFactory;
	}

	public AppTokenCredentials onlyForTestGetAppTokenCredentials() {
		return appTokenCredentials;
	}

	public AuthTokenCredentials onlyForTestGetAuthTokenCredentials() {
		return authTokenCredentials;
	}
}
