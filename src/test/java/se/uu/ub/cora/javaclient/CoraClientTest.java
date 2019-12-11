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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.javaclient.cora.CoraClient;
import se.uu.ub.cora.javaclient.cora.CoraClientException;
import se.uu.ub.cora.javaclient.doubles.AppTokenClientFactorySpy;
import se.uu.ub.cora.javaclient.doubles.RestClientFactorySpy;
import se.uu.ub.cora.javaclient.doubles.RestClientSpy;
import se.uu.ub.cora.json.builder.org.OrgJsonBuilderFactoryAdapter;
import se.uu.ub.cora.json.parser.JsonObject;

public class CoraClientTest {
	private CoraClient coraClient;
	private RestClientFactorySpy restClientFactory;
	private AppTokenClientFactorySpy appTokenClientFactory;
	private String userId = "someUserId";
	private String appToken = "someAppToken";
	private DataToJsonConverterFactorySpy dataToJsonConverterFactory;
	private JsonToDataConverterFactorySpy jsonToDataConverterFactory;

	@BeforeMethod
	public void BeforeMethod() {
		restClientFactory = new RestClientFactorySpy();
		appTokenClientFactory = new AppTokenClientFactorySpy();
		dataToJsonConverterFactory = new DataToJsonConverterFactorySpy();
		jsonToDataConverterFactory = new JsonToDataConverterFactorySpy();
		CoraClientDependencies coraClientDependencies = new CoraClientDependencies(
				appTokenClientFactory, restClientFactory, dataToJsonConverterFactory,
				jsonToDataConverterFactory, userId, appToken);
		coraClient = new CoraClientImp(coraClientDependencies);
	}

	@Test
	public void testInit() throws Exception {
		assertEquals(appTokenClientFactory.factored.size(), 1);
		String usedUserId = appTokenClientFactory.usedUserId.get(0);
		assertEquals(usedUserId, userId);
		String usedAppToken = appTokenClientFactory.usedAppToken.get(0);
		assertEquals(usedAppToken, appToken);
	}

	@Test(expectedExceptions = CoraClientException.class)
	public void testInitErrorWithAuthToken() throws Exception {
		CoraClientDependencies coraClientDependencies = setUpDependenciesWithErrorInUserId();
		CoraClientImp coraClient = new CoraClientImp(coraClientDependencies);
		String json = "some fake json";
		coraClient.create("someType", json);
	}

	private CoraClientDependencies setUpDependenciesWithErrorInUserId() {
		CoraClientDependencies coraClientDependencies = new CoraClientDependencies(
				appTokenClientFactory, restClientFactory, dataToJsonConverterFactory,
				jsonToDataConverterFactory, AppTokenClientFactorySpy.THIS_USER_ID_TRIGGERS_AN_ERROR,
				appToken);
		return coraClientDependencies;
	}

	@Test
	public void testRead() throws Exception {
		String readJson = coraClient.read("someType", "someId");
		RestClientSpy restClient = restClientFactory.factored.get(0);
		assertEquals(restClientFactory.factored.size(), 1);
		assertEquals(restClientFactory.usedAuthToken, "someAuthTokenFromSpy");
		assertEquals(restClient.recordType, "someType");
		assertEquals(restClient.recordId, "someId");
		assertEquals(readJson, restClient.returnedAnswer + restClient.methodCalled);
		assertEquals(restClient.methodCalled, "read");

	}

	@Test(expectedExceptions = CoraClientException.class)
	public void testReadError() throws Exception {
		coraClient.read(RestClientSpy.THIS_RECORD_TYPE_TRIGGERS_AN_ERROR, "someRecordId");
	}

	@Test
	public void testReadAsDataRecord() {
		ClientDataRecord dataRecord = coraClient
				.readAsDataRecord("someRecordTypeToBeReturnedAsDataGroup", "someRecordId");
		assertNotNull(dataRecord);

		assertCorrectFactoredRestClient();
		assertTrue(jsonToDataConverterFactory.createForJsonObjectWasCalled);

		JsonObject jsonSentToConverterFactory = (JsonObject) jsonToDataConverterFactory.jsonValue;
		String dataGroupPartOfRecordJson = jsonSentToConverterFactory.toJsonFormattedString();

		String dataGroupPartOfRecord = getExpectedDataGroupJson();
		assertEquals(dataGroupPartOfRecordJson, dataGroupPartOfRecord);

		ClientDataGroup clientDataGroupInRecord = dataRecord.getClientDataGroup();
		ClientDataGroup dataGroupReturnedFromConverter = jsonToDataConverterFactory.factoredConverter.dataGroup;
		assertSame(clientDataGroupInRecord, dataGroupReturnedFromConverter);

	}

	private void assertCorrectFactoredRestClient() {
		RestClientSpy restClient = restClientFactory.factored.get(0);
		assertEquals(restClientFactory.factored.size(), 1);
		assertEquals(restClientFactory.usedAuthToken, "someAuthTokenFromSpy");
		assertEquals(restClient.recordType, "someRecordTypeToBeReturnedAsDataGroup");
		assertEquals(restClient.recordId, "someRecordId");
	}

	private String getExpectedDataGroupJson() {
		return "{\"children\":[{\"name\":\"nameInData\",\"value\":\"historicCountry\"},{\"children\":[{\"name\":\"id\",\"value\":\"historicCountryCollection\"},{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"recordType\"},{\"name\":\"linkedRecordId\",\"value\":\"metadataItemCollection\"}],\"name\":\"type\"}],\"name\":\"recordInfo\"},{\"children\":[{\"repeatId\":\"0\",\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"genericCollectionItem\"},{\"name\":\"linkedRecordId\",\"value\":\"gaulHistoricCountryItem\"}],\"name\":\"ref\"},{\"repeatId\":\"1\",\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"genericCollectionItem\"},{\"name\":\"linkedRecordId\",\"value\":\"britainHistoricCountryItem\"}],\"name\":\"ref\"}],\"name\":\"collectionItemReferences\"}],\"name\":\"metadata\",\"attributes\":{\"type\":\"itemCollection\"}}";
	}

	@Test
	public void testReadList() throws Exception {
		String readListJson = coraClient.readList("someType");
		RestClientSpy restClient = restClientFactory.factored.get(0);
		assertEquals(restClientFactory.factored.size(), 1);
		assertEquals(restClientFactory.usedAuthToken, "someAuthTokenFromSpy");
		assertEquals(restClient.recordType, "someType");
		assertEquals(readListJson, restClient.returnedAnswer + restClient.methodCalled);
		assertEquals(restClient.methodCalled, "readList");
	}

	@Test(expectedExceptions = CoraClientException.class)
	public void testReadListError() throws Exception {
		coraClient.readList(RestClientSpy.THIS_RECORD_TYPE_TRIGGERS_AN_ERROR);
	}

	@Test
	public void testCreate() throws Exception {
		String json = "some fake json";
		String createdJson = coraClient.create("someType", json);

		assertCorrectDataSentToRestClient(json, createdJson, "create");
	}

	private void assertCorrectDataSentToRestClient(String jsonSentToRestClient,
			String jsonReturnedFromCreate, String methodCalled) {
		RestClientSpy restClient = restClientFactory.factored.get(0);
		assertEquals(restClientFactory.factored.size(), 1);
		assertEquals(restClientFactory.usedAuthToken, "someAuthTokenFromSpy");
		assertEquals(restClient.recordType, "someType");
		assertEquals(restClient.json, jsonSentToRestClient);
		assertEquals(jsonReturnedFromCreate, restClient.returnedAnswer + restClient.methodCalled);
		assertEquals(restClient.methodCalled, methodCalled);
	}

	@Test(expectedExceptions = CoraClientException.class)
	public void testCreateError() throws Exception {
		String json = "some fake json";
		coraClient.create(RestClientSpy.THIS_RECORD_TYPE_TRIGGERS_AN_ERROR, json);
	}

	@Test
	public void testCreateFromClientDataGroup() throws Exception {
		ClientDataGroup dataGroup = ClientDataGroup.withNameInData("someDataGroup");

		String createdJson = coraClient.create("someType", dataGroup);

		assertTrue(dataToJsonConverterFactory.factory instanceof OrgJsonBuilderFactoryAdapter);
		assertSame(dataToJsonConverterFactory.clientDataElement, dataGroup);
		String jsonReturnedFromConverter = dataToJsonConverterFactory.converterSpy.jsonToReturnFromSpy;

		assertCorrectDataSentToRestClient(jsonReturnedFromConverter, createdJson, "create");

	}

	@Test
	public void testUpdate() throws Exception {
		String json = "some fake json";
		String updatedJson = coraClient.update("someType", "someId", json);
		RestClientSpy restClient = restClientFactory.factored.get(0);
		assertEquals(restClientFactory.factored.size(), 1);
		assertEquals(restClientFactory.usedAuthToken, "someAuthTokenFromSpy");
		assertEquals(restClient.recordType, "someType");
		assertEquals(restClient.recordId, "someId");
		assertEquals(restClient.json, json);
		assertEquals(updatedJson, restClient.returnedAnswer + restClient.methodCalled);
		assertEquals(restClient.methodCalled, "update");
	}

	@Test
	public void testUpdateFromClientDataGroup() throws Exception {
		ClientDataGroup dataGroup = ClientDataGroup.withNameInData("someDataGroup");

		String updatedJson = coraClient.update("someType", "someId", dataGroup);

		assertTrue(dataToJsonConverterFactory.factory instanceof OrgJsonBuilderFactoryAdapter);
		assertSame(dataToJsonConverterFactory.clientDataElement, dataGroup);
		assertFalse(dataToJsonConverterFactory.includeActionLinks);
		// String jsonReturnedFromConverter =
		// dataToJsonConverterFactory.converterSpy.jsonToReturnFromSpy;
		//
		// assertCorrectDataSentToRestClient(jsonReturnedFromConverter, updatedJson, "update");

	}

	@Test(expectedExceptions = CoraClientException.class)
	public void testUpdateError() throws Exception {
		String json = "some fake json";
		coraClient.update(RestClientSpy.THIS_RECORD_TYPE_TRIGGERS_AN_ERROR, "someId", json);
	}

	@Test
	public void testDelete() {
		String createdJson = coraClient.delete("someType", "someId");
		RestClientSpy restClient = restClientFactory.factored.get(0);
		assertEquals(restClientFactory.factored.size(), 1);
		assertEquals(restClientFactory.usedAuthToken, "someAuthTokenFromSpy");
		assertEquals(restClient.recordType, "someType");
		assertEquals(restClient.recordId, "someId");
		assertEquals(createdJson, restClient.returnedAnswer + restClient.methodCalled);
		assertEquals(restClient.methodCalled, "delete");
	}

	@Test(expectedExceptions = CoraClientException.class)
	public void testDeleteError() throws Exception {
		coraClient.delete(RestClientSpy.THIS_RECORD_TYPE_TRIGGERS_AN_ERROR, "someId");
	}

	@Test
	public void testReadIncomingLinks() throws Exception {
		String readLinksJson = coraClient.readIncomingLinks("someType", "someId");
		RestClientSpy restClient = restClientFactory.factored.get(0);
		assertEquals(restClientFactory.factored.size(), 1);
		assertEquals(restClientFactory.usedAuthToken, "someAuthTokenFromSpy");
		assertEquals(restClient.recordType, "someType");
		assertEquals(restClient.recordId, "someId");
		assertEquals(readLinksJson, restClient.returnedAnswer + restClient.methodCalled);
		assertEquals(restClient.methodCalled, "readincomingLinks");
	}

	@Test(expectedExceptions = CoraClientException.class)
	public void testReadincomingLInksError() throws Exception {
		coraClient.readIncomingLinks(RestClientSpy.THIS_RECORD_TYPE_TRIGGERS_AN_ERROR, "someId");
	}
}
