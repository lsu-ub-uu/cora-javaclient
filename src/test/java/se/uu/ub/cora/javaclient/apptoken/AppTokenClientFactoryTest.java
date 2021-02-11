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
package se.uu.ub.cora.javaclient.apptoken;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.javaclient.apptoken.http.AppTokenClientImp;

public class AppTokenClientFactoryTest {

	private AppTokenClientFactory factory;
	private String appTokenVerifierUrl = "someBaseUrl/";
	private AppTokenClientImp appTokenClient;

	@BeforeMethod
	public void beforeMethod() {
		factory = new AppTokenClientFactoryImp(appTokenVerifierUrl);
		String userId = "someUserId";
		String appToken = "someAppToken";
		appTokenClient = (AppTokenClientImp) factory.factor(userId, appToken);
	}

	@Test
	public void testFactor() throws Exception {
		assertTrue(appTokenClient instanceof AppTokenClientImp);
	}

	@Test
	public void testFactorAddedDependenciesIsOk() throws Exception {
		HttpHandlerFactory handlerFactory = appTokenClient.getHttpHandlerFactory();
		assertTrue(handlerFactory instanceof HttpHandlerFactoryImp);
	}

	@Test
	public void testInputsSentOnToClient() throws Exception {
		assertEquals(appTokenClient.getAppTokenVerifierUrl(), "someBaseUrl/rest/apptoken/");
		assertEquals(appTokenClient.getUserId(), "someUserId");
		assertEquals(appTokenClient.getAppToken(), "someAppToken");
	}

	@Test
	public void testInputsSentOnToClientForSure() throws Exception {
		factory = new AppTokenClientFactoryImp("someBaseUrl2/");
		AppTokenClientImp appTokenClient = (AppTokenClientImp) factory.factor("", "");
		assertEquals(appTokenClient.getAppTokenVerifierUrl(), "someBaseUrl2/rest/apptoken/");
	}
}
