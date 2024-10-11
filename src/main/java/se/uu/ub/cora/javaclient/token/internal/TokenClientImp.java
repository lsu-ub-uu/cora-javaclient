/*
 * Copyright 2018, 2024 Uppsala University Library
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
import se.uu.ub.cora.javaclient.AppTokenCredentials;
import se.uu.ub.cora.javaclient.AuthTokenCredentials;
import se.uu.ub.cora.javaclient.data.DataClientException;
import se.uu.ub.cora.javaclient.token.TokenClient;

public final class TokenClientImp implements TokenClient {

	private static final String CORA_REST_APPTOKEN_ENDPOINT = "apptoken";
	private static final int CREATED = 201;
	private static final int DISTANCE_TO_START_OF_TOKEN = 24;
	private static final String NEW_LINE = "\n";
	private HttpHandlerFactory httpHandlerFactory;
	private String loginUrl;
	private String loginId;
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
		this.loginUrl = credentials.loginUrl() + CORA_REST_APPTOKEN_ENDPOINT;
		this.loginId = credentials.loginId();
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
		HttpHandler httpHandler = createHttpHandler();
		createAuthTokenUsingHttpHandler(loginId, appToken, httpHandler);
		authToken = possiblyGetAuthTokenFromAnswer(httpHandler);
	}

	@Override
	public void requestNewAuthToken() {
		if (appTokenDoNotExist()) {
			throw DataClientException.withMessage(
					"Could not request a new authToken due to being initialized without appToken.");
		}
		fetchAuthTokenFromServer();
	}

	private boolean appTokenDoNotExist() {
		return appToken == null;
	}

	private HttpHandler createHttpHandler() {
		return httpHandlerFactory.factor(loginUrl);
	}

	private void createAuthTokenUsingHttpHandler(String loginId, String appToken,
			HttpHandler httpHandler) {
		httpHandler.setRequestMethod("POST");
		httpHandler.setRequestProperty("Content-Type", "application/vnd.uub.login");
		httpHandler.setOutput(loginId + NEW_LINE + appToken);
	}

	private String possiblyGetAuthTokenFromAnswer(HttpHandler httpHandler) {
		if (CREATED == httpHandler.getResponseCode()) {
			return getAuthToken(httpHandler);
		}
		throw DataClientException.withMessage(
				"Could not create authToken. Response code: " + httpHandler.getResponseCode());
	}

	private String getAuthToken(HttpHandler httpHandler) {
		String responseText = httpHandler.getResponseText();
		return extractCreatedTokenFromResponseText(responseText);
	}

	private String extractCreatedTokenFromResponseText(String responseText) {
		int idIndex = responseText.lastIndexOf("\"name\":\"token\"") + DISTANCE_TO_START_OF_TOKEN;
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
