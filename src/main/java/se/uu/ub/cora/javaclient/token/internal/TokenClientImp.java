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

import java.util.Optional;

import se.uu.ub.cora.clientdata.ClientAction;
import se.uu.ub.cora.clientdata.ClientActionLink;
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
	private AppTokenCredentials appTokenCredentials;
	private AuthTokenCredentials authTokenCredentials;
	private ClientDataAuthentication authentication;
	private SchedulerFactory schedulerFactory;

	public static TokenClientImp usingHttpHandlerFactoryAndSchedulerFactoryAndAppToken(
			HttpHandlerFactory httpHandlerFactory, SchedulerFactory schedulerFactory,
			AppTokenCredentials credentials) {
		return new TokenClientImp(httpHandlerFactory, schedulerFactory, credentials);
	}

	TokenClientImp(HttpHandlerFactory httpHandlerFactory, SchedulerFactory schedulerFactory,
			AppTokenCredentials credentials) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.schedulerFactory = schedulerFactory;
		this.appTokenCredentials = credentials;
		this.loginUrl = credentials.loginUrl() + CORA_REST_APPTOKEN_ENDPOINT;
		this.loginId = credentials.loginId();
		this.appToken = credentials.appToken();
	}

	public static TokenClient usingHttpHandlerFactoryAndSchedulerFactoryAndAuthToken(
			HttpHandlerFactory httpHandlerFactory, SchedulerFactory schedulerFactory,
			AuthTokenCredentials credentials) {
		return new TokenClientImp(httpHandlerFactory, schedulerFactory, credentials);
	}

	TokenClientImp(HttpHandlerFactory httpHandlerFactory, SchedulerFactory schedulerFactory,
			AuthTokenCredentials credentials) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.schedulerFactory = schedulerFactory;
		this.authTokenCredentials = credentials;
	}

	@Override
	public String getAuthToken() {
		if (authenticationNeedsToBeFetched()) {
			authentication = loginUsingAppTokenOrRenewProvidedAuthToken();
			scheduleRenewOfAuthentication();
		}
		return authentication.getToken();
	}

	private boolean authenticationNeedsToBeFetched() {
		return null == authentication;
	}

	private ClientDataAuthentication loginUsingAppTokenOrRenewProvidedAuthToken() {
		if (startedWithAppToken()) {
			return logInWithAppToken();
		}
		return renewAuthTokenToTakeControllOverRenew(authTokenCredentials);
	}

	private boolean startedWithAppToken() {
		return null != appToken;
	}

	public ClientDataAuthentication renewAuthTokenToTakeControllOverRenew(
			AuthTokenCredentials credentials) {
		HttpHandler httpHandler = createHttpHandlerForInitialRenewOfProvidedAuthToken(credentials);
		return renewAndPossiblyGetAuthentication(httpHandler);
	}

	private HttpHandler createHttpHandlerForInitialRenewOfProvidedAuthToken(
			AuthTokenCredentials credentials) {
		HttpHandler httpHandler = httpHandlerFactory.factor(credentials.authTokenRenewUrl());
		httpHandler.setRequestMethod("POST");
		httpHandler.setRequestProperty("Accept", "application/vnd.uub.authentication+json");
		httpHandler.setRequestProperty("authToken", credentials.authToken());
		return httpHandler;
	}

	private ClientDataAuthentication renewAndPossiblyGetAuthentication(HttpHandler httpHandler) {
		if (OK == httpHandler.getResponseCode()) {
			return getAuthenticationFromAnswer(httpHandler);
		}
		throw DataClientException
				.withMessage("Could not renew authToken due to error. Response code: "
						+ httpHandler.getResponseCode());
	}

	private ClientDataAuthentication logInWithAppToken() {
		// TODO: flagga för att förhindra mer än ett anrop efter en ny token samtidigt
		HttpHandler httpHandler = callLoginUsingLoginIdAndAppToken(loginId, appToken);
		return possiblyGetAuthenticationFromLoginAnswer(httpHandler);
	}

	@Override
	public void requestNewAuthToken() {
		// TODO: stop running wait to renew if one exists.
		// TODO: flagga för att förhindra mer än ett anrop efter en ny token samtidigt
		if (appTokenDoesNotExist()) {
			throw DataClientException.withMessage(
					"Could not request a new authToken due to being initialized without appToken.");
		}
		authentication = logInWithAppToken();
	}

	private boolean appTokenDoesNotExist() {
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

	private ClientDataAuthentication possiblyGetAuthenticationFromLoginAnswer(
			HttpHandler httpHandler) {
		if (CREATED == httpHandler.getResponseCode()) {
			return getAuthenticationFromAnswer(httpHandler);
		}
		throw DataClientException.withMessage(
				"Could not create authToken. Response code: " + httpHandler.getResponseCode());
	}

	private ClientDataAuthentication getAuthenticationFromAnswer(HttpHandler httpHandler) {
		String responseText = httpHandler.getResponseText();
		return convertResponseTextToAuthentication(responseText);
	}

	private ClientDataAuthentication convertResponseTextToAuthentication(String responseText) {
		JsonToClientDataConverter converterUsingJsonString = JsonToClientDataConverterProvider
				.getConverterUsingJsonString(responseText);
		return (ClientDataAuthentication) converterUsingJsonString.toInstance();
	}

	private void scheduleRenewOfAuthentication() {
		Optional<ClientActionLink> actionLink = authentication.getActionLink(ClientAction.RENEW);
		// TODO: isPresent is not tested, should throw error or log in again if we have an
		// apptoken
		if (actionLink.isPresent()) {
			ClientActionLink renewAction = actionLink.get();
			Scheduler scheduler = schedulerFactory.factor();
			long delay = calculateDelay();
			scheduler.scheduleTaskWithDelayInMillis(renewToken(renewAction), delay);
		}
	}

	private Runnable renewToken(ClientActionLink renewAction) {
		return () -> {
			HttpHandler httpHandler = httpHandlerFactory.factor(renewAction.getURL());
			httpHandler.setRequestMethod(renewAction.getRequestMethod());
			httpHandler.setRequestProperty("Accept", renewAction.getAccept());
			httpHandler.setRequestProperty("authToken", authentication.getToken());
			authentication = renewAndPossiblyGetAuthentication(httpHandler);
			// TODO: next scheduling of renew is not tested
			// scheduleRenewOfAuthentication();
		};
	}

	private long calculateDelay() {
		String validUntil = authentication.getValidUntil();
		long validUntilLong = Long.parseLong(validUntil);
		long now = System.currentTimeMillis();
		int margin = 10000;
		return validUntilLong - now - margin;
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
