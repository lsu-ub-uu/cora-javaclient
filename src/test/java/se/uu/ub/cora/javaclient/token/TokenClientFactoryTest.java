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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.javaclient.AppTokenCredentials;
import se.uu.ub.cora.javaclient.AuthTokenCredentials;
import se.uu.ub.cora.javaclient.token.internal.TokenClientImp;

public class TokenClientFactoryTest {

	private TokenClientFactory factory;
	private String appTokenVerifierUrl = "someBaseUrl/";
	private TokenClientImp tokenClient;
	private String userId = "someUserId";
	private String appToken = "someAppToken";
	private String authToken = "someAuthToken";

	@BeforeMethod
	public void beforeMethod() {
		factory = new TokenClientFactoryImp(appTokenVerifierUrl);
	}

	private void factorUsingAppToken(String userId, String appToken) {
		tokenClient = (TokenClientImp) factory.factorUsingUserIdAndAppToken(userId, appToken);
	}

	private void factorUsingAuthToken(String authToken) {
		tokenClient = (TokenClientImp) factory.factorUsingAuthToken(authToken);
	}

	@Test
	public void testFactorAppToken() throws Exception {
		factorUsingAppToken(userId, appToken);

		assertTrue(tokenClient instanceof TokenClientImp);
	}

	@Test
	public void testFactorAppTokenAddedDependenciesIsOk() throws Exception {
		factorUsingAppToken(userId, appToken);

		HttpHandlerFactory handlerFactory = tokenClient.onlyForTestGetHttpHandlerFactory();
		assertTrue(handlerFactory instanceof HttpHandlerFactoryImp);

		AppTokenCredentials appTokenCredentials = tokenClient.onlyForTestGetAppTokenCredentials();
		assertEquals(appTokenCredentials.appTokenVerifierUrl(), appTokenVerifierUrl);
		assertEquals(appTokenCredentials.userId(), userId);
		assertEquals(appTokenCredentials.appToken(), appToken);
	}

	@Test
	public void testFactorAuthToken() throws Exception {
		factorUsingAuthToken(authToken);

		assertTrue(tokenClient instanceof TokenClientImp);
	}

	@Test
	public void testFactorAuthTokenAddedDependenciesIsOk() throws Exception {
		factorUsingAuthToken(authToken);

		HttpHandlerFactory handlerFactory = tokenClient.onlyForTestGetHttpHandlerFactory();
		assertTrue(handlerFactory instanceof HttpHandlerFactoryImp);

		AuthTokenCredentials authTokenCredentials = tokenClient
				.onlyForTestGetAuthTokenCredentials();
		assertEquals(authTokenCredentials.appTokenVerifierUrl(), appTokenVerifierUrl);
		assertEquals(authTokenCredentials.authToken(), authToken);
	}
}
