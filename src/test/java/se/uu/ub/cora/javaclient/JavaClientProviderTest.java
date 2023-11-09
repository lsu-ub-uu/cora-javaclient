/*
 * Copyright 2023 Uppsala University Library
 * Copyright 2023 Olov McKie
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

import static org.testng.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.javaclient.data.DataClientSpy;
import se.uu.ub.cora.javaclient.data.internal.DataClientImp;
import se.uu.ub.cora.javaclient.doubles.JavaClientFactorySpy;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.internal.RestClientImp;

public class JavaClientProviderTest {

	private static final String SOME_USER_ID = "someUserId";
	private static final String SOME_APP_TOKEN = "someAppToken";
	private static final String SOME_AUTH_TOKEN = "someAuthToken";
	private static final String SOME_APP_TOKEN_VERIFIER_URL = "someAppTokenVerifierUrl";
	private static final String SOME_BASE_URL = "someBaseUrl";
	private JavaClientAppTokenCredentials appTokenCredentials = new JavaClientAppTokenCredentials(
			SOME_BASE_URL, SOME_APP_TOKEN_VERIFIER_URL, SOME_USER_ID, SOME_APP_TOKEN);
	private JavaClientAuthTokenCredentials authTokenCredentials = new JavaClientAuthTokenCredentials(
			SOME_BASE_URL, SOME_APP_TOKEN_VERIFIER_URL, SOME_AUTH_TOKEN);

	@AfterMethod
	private void afterMethod() {
		JavaClientProvider.onlyForTestSetJavaClientFactory(null);

	}

	@Test
	public void testPrivateConstructor() throws Exception {
		Constructor<JavaClientProvider> constructor = JavaClientProvider.class
				.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
	}

	@Test(expectedExceptions = InvocationTargetException.class)
	public void testPrivateConstructorInvoke() throws Exception {
		Constructor<JavaClientProvider> constructor = JavaClientProvider.class
				.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	@Test
	public void testCreateRestClientUsingBaseUrlAndApptokenUrlAndAuthToken() throws Exception {
		RestClientImp restClient = (RestClientImp) JavaClientProvider
				.createRestClientUsingAuthTokenCredentials(authTokenCredentials);
		assertTrue(restClient instanceof RestClientImp);
	}

	@Test
	public void testOnlyForTestSetjavaClientFactory() throws Exception {
		JavaClientFactorySpy javaClientFactory = new JavaClientFactorySpy();
		JavaClientProvider.onlyForTestSetJavaClientFactory(javaClientFactory);

		RestClient restClient = JavaClientProvider
				.createRestClientUsingAuthTokenCredentials(authTokenCredentials);

		javaClientFactory.MCR.assertParameters("factorRestClientUsingAuthTokenCredentials", 0,
				authTokenCredentials);
		javaClientFactory.MCR.assertReturn("factorRestClientUsingAuthTokenCredentials", 0,
				restClient);
	}

	@Test
	public void testCreateRestClientUsingBaseUrlAndApptokenUrlAndUserIdAndAppToken()
			throws Exception {
		RestClientImp restClient = (RestClientImp) JavaClientProvider
				.createRestClientUsingAppTokenCredentials(appTokenCredentials);
		assertTrue(restClient instanceof RestClientImp);
	}

	@Test
	public void testCreateRestClientUsingBaseUrlAndApptokenUrlAndUserIdAndAppTokenPassedParameteres()
			throws Exception {
		JavaClientFactorySpy javaClientFactory = new JavaClientFactorySpy();
		JavaClientProvider.onlyForTestSetJavaClientFactory(javaClientFactory);

		RestClient restClient = JavaClientProvider
				.createRestClientUsingAppTokenCredentials(appTokenCredentials);

		javaClientFactory.MCR.assertParameters("factorRestClientUsingAppTokenCredentials", 0,
				appTokenCredentials);
		javaClientFactory.MCR.assertReturn("factorRestClientUsingAppTokenCredentials", 0,
				restClient);
	}

	@Test
	public void testCreateDataClientUsingBaseUrlAndApptokenUrlAndAuthToken() throws Exception {
		DataClientImp dataClient = (DataClientImp) JavaClientProvider
				.createDataClientUsingAuthTokenCredentials(authTokenCredentials);

		assertTrue(dataClient instanceof DataClientImp);
		assertTrue(dataClient.onlyForTestGetRestClient() instanceof RestClientImp);
	}

	@Test
	public void testCreateDataClientUsingBaseUrlAndApptokenUrlAndAuthTokenPassedOn()
			throws Exception {
		JavaClientFactorySpy javaClientFactory = new JavaClientFactorySpy();
		JavaClientProvider.onlyForTestSetJavaClientFactory(javaClientFactory);

		DataClientSpy dataClient = (DataClientSpy) JavaClientProvider
				.createDataClientUsingAuthTokenCredentials(authTokenCredentials);

		javaClientFactory.MCR.assertParameters("factorDataClientUsingAuthTokenCredentials", 0,
				authTokenCredentials);
		javaClientFactory.MCR.assertReturn("factorDataClientUsingAuthTokenCredentials", 0,
				dataClient);
	}

	@Test
	public void testCreateDataClientUsingBaseUrlAndApptokenUrlAndUserIdAndAppToken()
			throws Exception {
		DataClientImp dataClient = (DataClientImp) JavaClientProvider
				.createDataClientUsingAppTokenCredentials(appTokenCredentials);
		assertTrue(dataClient instanceof DataClientImp);
		assertTrue(dataClient.onlyForTestGetRestClient() instanceof RestClientImp);
	}

	@Test
	public void testCreateDataClientUsingBaseUrlAndApptokenUrlAndUserIdAndAppTokenPassedParameteres()
			throws Exception {
		JavaClientFactorySpy javaClientFactory = new JavaClientFactorySpy();
		JavaClientProvider.onlyForTestSetJavaClientFactory(javaClientFactory);

		DataClientSpy dataClient = (DataClientSpy) JavaClientProvider
				.createDataClientUsingAppTokenCredentials(appTokenCredentials);

		javaClientFactory.MCR.assertParameters("factorDataClientUsingAppTokenCredentials", 0,
				appTokenCredentials);
		javaClientFactory.MCR.assertParameters("factorDataClientUsingAppTokenCredentials", 0,
				appTokenCredentials);
		javaClientFactory.MCR.assertReturn("factorDataClientUsingAppTokenCredentials", 0,
				dataClient);
	}

}
