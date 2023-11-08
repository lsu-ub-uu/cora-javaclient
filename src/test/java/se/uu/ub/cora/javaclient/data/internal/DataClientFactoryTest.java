/*
 * Copyright 2018, 2019, 2023 Uppsala University Library
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
package se.uu.ub.cora.javaclient.data.internal;

import static org.testng.Assert.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterProvider;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterProvider;
import se.uu.ub.cora.javaclient.data.internal.DataClientFactoryImp;
import se.uu.ub.cora.javaclient.data.internal.DataClientImp;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestClientSpy;

public class DataClientFactoryTest {
	private DataClientFactoryImp clientFactory;
	private RestClientSpy restClientSpy;

	@BeforeMethod
	public void beforeMethod() {
		ClientDataToJsonConverterProvider.setDataToJsonConverterFactoryCreator(null);
		JsonToClientDataConverterProvider.setJsonToDataConverterFactory(null);

		restClientSpy = new RestClientSpy();
		clientFactory = new DataClientFactoryImp();
	}

	@Test
	public void testFactorUsingRestClient() throws Exception {
		DataClientImp dataClient = (DataClientImp) clientFactory
				.factorUsingRestClient(restClientSpy);

		RestClient restClient = dataClient.onlyForTestGetRestClient();
		assertSame(restClientSpy, restClient);
	}

}
