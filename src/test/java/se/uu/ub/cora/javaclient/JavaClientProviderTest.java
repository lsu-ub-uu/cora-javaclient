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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.javaclient.data.DataClientSpy;
import se.uu.ub.cora.javaclient.data.internal.DataClientImp;
import se.uu.ub.cora.javaclient.doubles.JavaClientFactorySpy;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.internal.RestClientImp;
import se.uu.ub.cora.javaclient.token.TokenClient;
import se.uu.ub.cora.javaclient.token.internal.TokenClientImp;

public class JavaClientProviderTest {

	private static final String SOME_USER_ID = "someLoginId";
	private static final String SOME_APP_TOKEN = "someAppToken";
	private static final String SOME_AUTH_TOKEN = "someAuthToken";
	private static final String SOME_APP_TOKEN_VERIFIER_URL = "someAppTokenVerifierUrl";
	private static final String SOME_BASE_URL = "someBaseUrl";
	private JavaClientAppTokenCredentials javaClientAppTokenCredentials = new JavaClientAppTokenCredentials(
			SOME_BASE_URL, SOME_APP_TOKEN_VERIFIER_URL, SOME_USER_ID, SOME_APP_TOKEN);
	private JavaClientAuthTokenCredentials javaClientAuthTokenCredentials = new JavaClientAuthTokenCredentials(
			SOME_BASE_URL, SOME_APP_TOKEN_VERIFIER_URL, SOME_AUTH_TOKEN);
	private JavaClientFactorySpy javaClientFactory;
	private AppTokenCredentials appTokenCredentials = new AppTokenCredentials(
			SOME_APP_TOKEN_VERIFIER_URL, SOME_USER_ID, SOME_APP_TOKEN);
	private AuthTokenCredentials authTokenCredentials = new AuthTokenCredentials(
			SOME_APP_TOKEN_VERIFIER_URL, SOME_AUTH_TOKEN);

	@BeforeMethod
	private void beforeMethod() {
		javaClientFactory = new JavaClientFactorySpy();
	}

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
				.createRestClientUsingJavaClientAuthTokenCredentials(
						javaClientAuthTokenCredentials);
		assertTrue(restClient instanceof RestClientImp);
	}

	@Test
	public void testOnlyForTestSetjavaClientFactory() throws Exception {
		JavaClientProvider.onlyForTestSetJavaClientFactory(javaClientFactory);

		RestClient restClient = JavaClientProvider
				.createRestClientUsingJavaClientAuthTokenCredentials(
						javaClientAuthTokenCredentials);

		javaClientFactory.MCR.assertParameters(
				"factorRestClientUsingJavaClientAuthTokenCredentials", 0,
				javaClientAuthTokenCredentials);
		javaClientFactory.MCR.assertReturn("factorRestClientUsingJavaClientAuthTokenCredentials", 0,
				restClient);
	}

	@Test
	public void testCreateRestClientUsingBaseUrlAndApptokenUrlAndLoginIdAndAppToken()
			throws Exception {
		RestClientImp restClient = (RestClientImp) JavaClientProvider
				.createRestClientUsingJavaClientAppTokenCredentials(javaClientAppTokenCredentials);
		assertTrue(restClient instanceof RestClientImp);
	}

	@Test
	public void testCreateRestClientUsingBaseUrlAndApptokenUrlAndLoginIdAndAppTokenPassedParameteres()
			throws Exception {
		JavaClientProvider.onlyForTestSetJavaClientFactory(javaClientFactory);

		RestClient restClient = JavaClientProvider
				.createRestClientUsingJavaClientAppTokenCredentials(javaClientAppTokenCredentials);

		javaClientFactory.MCR.assertParameters("factorRestClientUsingJavaClientAppTokenCredentials",
				0, javaClientAppTokenCredentials);
		javaClientFactory.MCR.assertReturn("factorRestClientUsingJavaClientAppTokenCredentials", 0,
				restClient);
	}

	@Test
	public void testCreateDataClientUsingBaseUrlAndApptokenUrlAndAuthToken() throws Exception {
		DataClientImp dataClient = (DataClientImp) JavaClientProvider
				.createDataClientUsingJavaClientAuthTokenCredentials(
						javaClientAuthTokenCredentials);

		assertTrue(dataClient instanceof DataClientImp);
		assertTrue(dataClient.onlyForTestGetRestClient() instanceof RestClientImp);
	}

	@Test
	public void testCreateDataClientUsingBaseUrlAndApptokenUrlAndAuthTokenPassedOn()
			throws Exception {
		JavaClientProvider.onlyForTestSetJavaClientFactory(javaClientFactory);

		DataClientSpy dataClient = (DataClientSpy) JavaClientProvider
				.createDataClientUsingJavaClientAuthTokenCredentials(
						javaClientAuthTokenCredentials);

		javaClientFactory.MCR.assertParameters(
				"factorDataClientUsingJavaClientAuthTokenCredentials", 0,
				javaClientAuthTokenCredentials);
		javaClientFactory.MCR.assertReturn("factorDataClientUsingJavaClientAuthTokenCredentials", 0,
				dataClient);
	}

	@Test
	public void testCreateDataClientUsingBaseUrlAndApptokenUrlAndloginIdAndAppToken()
			throws Exception {
		DataClientImp dataClient = (DataClientImp) JavaClientProvider
				.createDataClientUsingJavaClientAppTokenCredentials(javaClientAppTokenCredentials);
		assertTrue(dataClient instanceof DataClientImp);
		assertTrue(dataClient.onlyForTestGetRestClient() instanceof RestClientImp);
	}

	@Test
	public void testCreateDataClientUsingBaseUrlAndApptokenUrlAndLoginIdAndAppTokenPassedParameteres()
			throws Exception {
		JavaClientProvider.onlyForTestSetJavaClientFactory(javaClientFactory);

		DataClientSpy dataClient = (DataClientSpy) JavaClientProvider
				.createDataClientUsingJavaClientAppTokenCredentials(javaClientAppTokenCredentials);

		javaClientFactory.MCR.assertParameters("factorDataClientUsingJavaClientAppTokenCredentials",
				0, javaClientAppTokenCredentials);
		javaClientFactory.MCR.assertReturn("factorDataClientUsingJavaClientAppTokenCredentials", 0,
				dataClient);
	}

	@Test
	public void testCreateTokenClientUsingAppTokenCredentials() throws Exception {
		JavaClientProvider.onlyForTestSetJavaClientFactory(javaClientFactory);

		TokenClient tokenClient = JavaClientProvider
				.createTokenClientUsingAppTokenCredentials(appTokenCredentials);

		javaClientFactory.MCR.assertParameters("factorTokenClientUsingAppTokenCredentials", 0,
				appTokenCredentials);
		javaClientFactory.MCR.assertReturn("factorTokenClientUsingAppTokenCredentials", 0,
				tokenClient);
	}

	@Test
	public void testCreateTokenClientUsingAppTokenCredentialsRealJavaFactory() throws Exception {
		TokenClient tokenClient = JavaClientProvider
				.createTokenClientUsingAppTokenCredentials(appTokenCredentials);

		assertTrue(tokenClient instanceof TokenClientImp);
	}

	@Test
	public void testCreateTokenClientUsingAuthTokenCredentials() throws Exception {
		JavaClientProvider.onlyForTestSetJavaClientFactory(javaClientFactory);

		TokenClient tokenClient = JavaClientProvider
				.createTokenClientUsingAuthTokenCredentials(authTokenCredentials);

		javaClientFactory.MCR.assertParameters("factorTokenClientUsingAuthTokenCredentials", 0,
				authTokenCredentials);
		javaClientFactory.MCR.assertReturn("factorTokenClientUsingAuthTokenCredentials", 0,
				tokenClient);
	}

	@Test
	public void testCreateTokenClientUsingAuthTokenCredentialsRealJavaFactory() throws Exception {
		TokenClient tokenClient = JavaClientProvider
				.createTokenClientUsingAuthTokenCredentials(authTokenCredentials);

		assertTrue(tokenClient instanceof TokenClientImp);
	}

}
