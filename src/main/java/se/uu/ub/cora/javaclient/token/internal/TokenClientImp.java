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
	private Scheduler scheduler;

	public static TokenClientImp usingHttpHandlerFactoryAndSchedulerAndAppToken(
			HttpHandlerFactory httpHandlerFactory, Scheduler scheduler,
			AppTokenCredentials credentials) {
		return new TokenClientImp(httpHandlerFactory, scheduler, credentials);
	}

	TokenClientImp(HttpHandlerFactory httpHandlerFactory, Scheduler scheduler,
			AppTokenCredentials credentials) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.scheduler = scheduler;

		this.appTokenCredentials = credentials;
		this.loginUrl = credentials.loginUrl() + CORA_REST_APPTOKEN_ENDPOINT;
		this.loginId = credentials.loginId();
		this.appToken = credentials.appToken();
	}

	public static TokenClient usingHttpHandlerFactoryAndSchedulerAndAuthToken(
			HttpHandlerFactory httpHandlerFactory, Scheduler scheduler,
			AuthTokenCredentials credentials) {
		return new TokenClientImp(httpHandlerFactory, scheduler, credentials);
	}

	TokenClientImp(HttpHandlerFactory httpHandlerFactory, Scheduler scheduler,
			AuthTokenCredentials credentials) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.scheduler = scheduler;
		this.authTokenCredentials = credentials;
	}

	@Override
	public synchronized String getAuthToken() {
		if (isStartedUsingAuthTokenButIsNotRenewable()) {
			return authTokenCredentials.authToken();
		}
		if (authenticationNeedsToBeFetched()) {
			authentication = loginUsingAppTokenOrRenewProvidedAuthToken();
			scheduleRenewOfAuthentication();
		}
		return authentication.getToken();
	}

	private boolean isStartedUsingAuthTokenButIsNotRenewable() {
		return null != authTokenCredentials && !authTokenCredentials.tokenIsRenewable();
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

	record HttpHandlerSpec(String url, String method, String accept, String authToken) {
	}

	private ClientDataAuthentication renewAuthTokenToTakeControllOverRenew(
			AuthTokenCredentials credentials) {
		HttpHandlerSpec httpHandlerSpec = new HttpHandlerSpec(credentials.authTokenRenewUrl(),
				"POST", "application/vnd.uub.authentication+json", credentials.authToken());
		HttpHandler httpHandler = createHttpHandlerRequestUsingSpec(httpHandlerSpec);
		return possiblyGetAuthenticationFromRenewAuthTokenAnswer(httpHandler);
	}

	private HttpHandler createHttpHandlerRequestUsingSpec(HttpHandlerSpec httpHandlerSpec) {
		HttpHandler httpHandler = httpHandlerFactory.factor(httpHandlerSpec.url());
		httpHandler.setRequestMethod(httpHandlerSpec.method());
		httpHandler.setRequestProperty("Accept", httpHandlerSpec.accept());
		httpHandler.setRequestProperty("authToken", httpHandlerSpec.authToken);
		return httpHandler;
	}

	private ClientDataAuthentication possiblyGetAuthenticationFromRenewAuthTokenAnswer(
			HttpHandler httpHandler) {
		if (OK == httpHandler.getResponseCode()) {
			return getAuthenticationFromAnswer(httpHandler);
		}
		throw DataClientException
				.withMessage("Could not renew authToken due to error. Response code: "
						+ httpHandler.getResponseCode());
	}

	private ClientDataAuthentication logInWithAppToken() {
		HttpHandler httpHandler = callLoginUsingLoginIdAndAppToken(loginId, appToken);
		return possiblyGetAuthenticationFromLoginAnswer(httpHandler);
	}

	@Override
	public synchronized void requestNewAuthToken() {
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
		Optional<ClientActionLink> renewActionLink = authentication
				.getActionLink(ClientAction.RENEW);
		if (renewActionLink.isPresent()) {
			startScheduleUsingActionLink(renewActionLink.get());
		} else {
			// OBS: This branch SHOULD NOT happen in this case. Something is very wrong if Renew
			// actionLink is missing on this stage.
			throw DataClientException.withMessage(
					"AuthToken could not be renew due to missing actionLink on previous call.");
		}
	}

	private void startScheduleUsingActionLink(ClientActionLink actionLink) {
		ClientActionLink renewAction = actionLink;
		long delayToRenew = calculateDelayToRenew();
		long timeNow = System.currentTimeMillis();
		long renewUntilLong = Long.parseLong(authentication.getRenewUntil());
		if (timeNow + delayToRenew > renewUntilLong) {
			throw DataClientException.withMessage(
					"The authToken renewal could not be scheduled because it has reached the "
							+ "permitted renewal limit. The current token will remain "
							+ "active for a while but will eventually become unauthorized. "
							+ "Please re-login to continue using this client.");
		}
		scheduler.scheduleTaskWithDelayInMillis(renewToken(renewAction), delayToRenew);
	}

	private Runnable renewToken(ClientActionLink renewAction) {
		return () -> {
			HttpHandlerSpec httpHandlerSpec = new HttpHandlerSpec(renewAction.getURL(),
					renewAction.getRequestMethod(), renewAction.getAccept(),
					authentication.getToken());
			HttpHandler httpHandler = createHttpHandlerRequestUsingSpec(httpHandlerSpec);
			authentication = possiblyGetAuthenticationFromRenewAuthTokenAnswer(httpHandler);
			scheduleRenewOfAuthentication();
		};
	}

	private long calculateDelayToRenew() {
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

	public Scheduler onlyForTestGetScheduler() {
		return scheduler;
	}
}
