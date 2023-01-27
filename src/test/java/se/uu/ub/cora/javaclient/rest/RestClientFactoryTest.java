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
package se.uu.ub.cora.javaclient.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.javaclient.rest.internal.RestClientImp;

public class RestClientFactoryTest {

	private RestClientFactory factory;
	private String baseUrl = "someBaseUrl";
	private String authToken = "";

	@BeforeMethod
	public void beforeMethod() {
		factory = new RestClientFactoryImp(baseUrl);
	}

	@Test
	public void testFactor() throws Exception {
		RestClient coraRestClient = factory.factorUsingAuthToken(authToken);
		assertTrue(coraRestClient instanceof RestClientImp);
	}

	@Test
	public void testFactorAddedDependenciesIsOk() throws Exception {
		RestClientImp coraRestClient = (RestClientImp) factory.factorUsingAuthToken(authToken);
		HttpHandlerFactory handlerFactory = coraRestClient.onlyForTestGetHttpHandlerFactory();
		assertTrue(handlerFactory instanceof HttpHandlerFactoryImp);
	}

	@Test
	public void testInputsSentOnToClient() throws Exception {
		RestClientImp coraRestClient = (RestClientImp) factory
				.factorUsingAuthToken("someAuthToken");
		assertEquals(coraRestClient.getBaseUrl(), "someBaseUrlrecord/");
		assertEquals(coraRestClient.onlyForTestGetAuthToken(), "someAuthToken");
	}

	@Test
	public void testInputsSentOnToClientForSure() throws Exception {
		factory = new RestClientFactoryImp("someBaseUrl2");
		RestClientImp coraRestClient = (RestClientImp) factory
				.factorUsingAuthToken("someAuthToken2");
		assertEquals(coraRestClient.getBaseUrl(), "someBaseUrl2record/");
		assertEquals(coraRestClient.onlyForTestGetAuthToken(), "someAuthToken2");
	}
}
