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
package se.uu.ub.cora.javaclient;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.javaclient.apptoken.AppTokenClient;
import se.uu.ub.cora.javaclient.apptoken.http.AppTokenClientImp;
import se.uu.ub.cora.javaclient.cora.CoraClientException;
import se.uu.ub.cora.javaclient.cora.http.AppTokenClientCredentials;
import se.uu.ub.cora.javaclient.externaldependenciesdoubles.HttpHandlerFactorySpy;

public class AppTokenClientTest {
	private HttpHandlerFactorySpy httpHandlerFactorySpy;
	private AppTokenClientCredentials appTokenCredentials = new AppTokenClientCredentials(
			"http://localhost:8080/apptokenverifier/", "someUserId",
			"02a89fd5-c768-4209-9ecc-d80bd793b01e");
	private AppTokenClient appTokenClient;

	@BeforeMethod
	public void setUp() {
		httpHandlerFactorySpy = new HttpHandlerFactorySpy();
		httpHandlerFactorySpy.setResponseCode(201);
		appTokenClient = AppTokenClientImp
				.usingHttpHandlerFactoryAndCredentials(httpHandlerFactorySpy, appTokenCredentials);
	}

	@Test
	public void testHttpHandlerSetupCorrectly() throws Exception {
		appTokenClient.getAuthToken();
		assertEquals(httpHandlerFactorySpy.urlString,
				"http://localhost:8080/apptokenverifier/rest/apptoken/someUserId");
		assertEquals(getRequestMethod(), "POST");
		assertEquals(getNumberOfRequestProperties(), 0);
		assertEquals(getOutputString(), appTokenCredentials.appToken);
	}

	@Test
	public void testGetAuthToken() throws Exception {
		String authToken = appTokenClient.getAuthToken();
		assertEquals(authToken, "a1acff95-5849-4e10-9ee9-4b192aef17fd");
	}

	@Test
	public void testGetAuthTokenTwiceIsOnlyCreatedOnceOnServer() throws Exception {
		assertEquals(httpHandlerFactorySpy.factored.size(), 0);

		String authToken = appTokenClient.getAuthToken();
		assertEquals(authToken, "a1acff95-5849-4e10-9ee9-4b192aef17fd");
		assertEquals(httpHandlerFactorySpy.factored.size(), 1);

		String authToken2 = appTokenClient.getAuthToken();
		assertEquals(authToken2, "a1acff95-5849-4e10-9ee9-4b192aef17fd");
		assertEquals(httpHandlerFactorySpy.factored.size(), 1);
	}

	@Test(expectedExceptions = CoraClientException.class, expectedExceptionsMessageRegExp = ""
			+ "Could not create authToken")
	public void testGetAuthTokenNotOk() {
		httpHandlerFactorySpy.changeFactoryToFactorInvalidHttpHandlers();
		appTokenClient.getAuthToken();
	}

	private String getOutputString() {
		return httpHandlerFactorySpy.httpHandlerSpy.outputString;
	}

	private String getRequestMethod() {
		return httpHandlerFactorySpy.httpHandlerSpy.requestMetod;
	}

	private int getNumberOfRequestProperties() {
		return httpHandlerFactorySpy.httpHandlerSpy.requestProperties.size();
	}
}
