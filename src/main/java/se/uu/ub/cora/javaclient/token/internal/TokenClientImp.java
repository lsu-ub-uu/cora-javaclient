/*
 * Copyright 2018, 2024, 2025 Uppsala University Library
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

import se.uu.ub.cora.clientdata.ClientDataAuthentication;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterProvider;
import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.javaclient.AppTokenCredentials;
import se.uu.ub.cora.javaclient.AuthTokenCredentials;
import se.uu.ub.cora.javaclient.data.DataClientException;
import se.uu.ub.cora.javaclient.token.TokenClient;

public final class TokenClientImp implements TokenClient {

	private static final int OK = 200;
	private static final String CORA_REST_APPTOKEN_ENDPOINT = "apptoken";
	private static final int CREATED = 201;
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

	TokenClientImp(HttpHandlerFactory httpHandlerFactory, AppTokenCredentials credentials) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.appTokenCredentials = credentials;
		this.loginUrl = credentials.loginUrl() + CORA_REST_APPTOKEN_ENDPOINT;
		this.loginId = credentials.loginId();
		this.appToken = credentials.appToken();
	}

	public static TokenClient usingHttpHandlerFactoryAndAuthToken(
			HttpHandlerFactory httpHandlerFactory, AuthTokenCredentials credentials) {
		return new TokenClientImp(httpHandlerFactory, credentials);
	}

	TokenClientImp(HttpHandlerFactory httpHandlerFactory, AuthTokenCredentials credentials) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.authTokenCredentials = credentials;
		this.authToken = credentials.authToken();
		renewAuthTokenToGetTakeControllOverRenew(credentials);
	}

	private void renewAuthTokenToGetTakeControllOverRenew(AuthTokenCredentials credentials) {
		HttpHandler httpHandler = createHttpHandlerForInitialRenewOfProvidedAuthToken(credentials);
		authToken = possiblyGetAuthTokenFromRenewAnswer(httpHandler);
	}

	private HttpHandler createHttpHandlerForInitialRenewOfProvidedAuthToken(
			AuthTokenCredentials credentials) {
		HttpHandler httpHandler = httpHandlerFactory.factor(credentials.authTokenRenewUrl());
		httpHandler.setRequestMethod("POST");
		httpHandler.setRequestProperty("Accept", "application/vnd.uub.authentication+json");
		httpHandler.setRequestProperty("authToken", credentials.authToken());
		return httpHandler;
	}

	private String possiblyGetAuthTokenFromRenewAnswer(HttpHandler httpHandler) {
		if (OK == httpHandler.getResponseCode()) {
			return readAuthTokenfromAnswer(httpHandler);
		}
		throw DataClientException
				.withMessage("Could not renew authToken due to error. Response code: "
						+ httpHandler.getResponseCode());
	}

	@Override
	public String getAuthToken() {
		if (authTokenNeedsToBeFetched()) {
			logInWithAppToken();
		}
		return authToken;
	}

	private boolean authTokenNeedsToBeFetched() {
		return null == authToken;
	}

	private void logInWithAppToken() {
		// TODO: flagga för att förhindra mer än ett anrop efter en ny token samtidigt
		HttpHandler httpHandler = callLoginUsingLoginIdAndAppToken(loginId, appToken);
		authToken = possiblyGetAuthTokenFromCreateAnswer(httpHandler);
	}

	@Override
	public void requestNewAuthToken() {
		// TODO: stop running wait to renew if one exists.
		// TODO: flagga för att förhindra mer än ett anrop efter en ny token samtidigt
		if (appTokenDoNotExist()) {
			throw DataClientException.withMessage(
					"Could not request a new authToken due to being initialized without appToken.");
		}
		logInWithAppToken();
	}

	private boolean appTokenDoNotExist() {
		return appToken == null;
	}

	private HttpHandler createHttpHandler() {
		return httpHandlerFactory.factor(loginUrl);
	}

	private HttpHandler callLoginUsingLoginIdAndAppToken(String loginId, String appToken) {
		HttpHandler httpHandler = createHttpHandler();
		httpHandler.setRequestMethod("POST");
		httpHandler.setRequestProperty("Content-Type", "application/vnd.uub.login");
		httpHandler.setRequestProperty("Accept", "application/vnd.uub.authentication+json");
		httpHandler.setOutput(loginId + NEW_LINE + appToken);
		return httpHandler;
	}

	private String possiblyGetAuthTokenFromCreateAnswer(HttpHandler httpHandler) {
		if (CREATED == httpHandler.getResponseCode()) {
			return readAuthTokenfromAnswer(httpHandler);
		}
		throw DataClientException.withMessage(
				"Could not create authToken. Response code: " + httpHandler.getResponseCode());
	}

	private String readAuthTokenfromAnswer(HttpHandler httpHandler) {
		String responseText = httpHandler.getResponseText();
		return extractCreatedTokenFromResponseText(responseText);
	}

	private String extractCreatedTokenFromResponseText(String responseText) {
		JsonToClientDataConverter converterUsingJsonString = JsonToClientDataConverterProvider
				.getConverterUsingJsonString(responseText);
		ClientDataAuthentication authentication = (ClientDataAuthentication) converterUsingJsonString
				.toInstance();

		return authentication.getToken();
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

	// private void callSchedule() {
	// ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
	//
	// try (virtualThreadExecutor) {
	// var taskResult = schedule(() ->
	// System.out.println("Running on a scheduled virtual thread!"), 5, ChronoUnit.SECONDS,
	// virtualThreadExecutor);
	//
	// }
	// }

}
