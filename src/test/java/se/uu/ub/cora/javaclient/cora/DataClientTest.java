/*
 * Copyright 2018, 2020, 2021 Uppsala University Library
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
package se.uu.ub.cora.javaclient.cora;

import static org.testng.Assert.assertSame;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterFactory;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterProvider;
import se.uu.ub.cora.clientdata.spies.ClientDataGroupSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataToJsonConverterFactoryCreatorSpy;
import se.uu.ub.cora.javaclient.cora.internal.DataClientImp;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestClientSpy;

public class DataClientTest {
	private DataClientImp coraClient;
	private RestClientSpy restClient;
	private ClientDataToJsonConverterFactoryCreatorSpy dataToJsonFactoryCreator;

	@BeforeMethod
	public void setUp() {
		restClient = new RestClientSpy();
		coraClient = new DataClientImp(restClient);

		dataToJsonFactoryCreator = new ClientDataToJsonConverterFactoryCreatorSpy();
		ClientDataToJsonConverterProvider
				.setDataToJsonConverterFactoryCreator(dataToJsonFactoryCreator);
	}

	@Test
	public void testInit() throws Exception {
		RestClient restClient2 = coraClient.onlyForTestGetRestClient();
		assertSame(restClient2, restClient);

		ClientDataToJsonConverterFactory dataToJsonConverterFactory = coraClient
				.onlyForTestGetDataToJsonConverterFactory();

	}

	@Test
	public void testCreate() throws Exception {

		coraClient.create("someRecordType", new ClientDataGroupSpy());

		// String readJson = coraClient.read("someType", "someId");
		//
		// restClient.MCR.assertParameters("factor", 0, null);

		// RestClientSpyOld restClient = restClientFactory.factored.get(0);
		// assertEquals(restClientFactory.factored.size(), 1);
		// assertEquals(restClientFactory.usedAuthToken, "someAuthTokenFromSpy");
		// assertEquals(restClient.recordType, "someType");
		// assertEquals(restClient.recordId, "someId");
		// assertEquals(readJson, restClient.restResponse.responseText);
		// assertEquals(restClient.methodCalled, "read");

	}

	// @Test(expectedExceptions = CoraClientException.class, expectedExceptionsMessageRegExp = ""
	// + "Could not read record of type: thisRecordTypeTriggersAnError and id: someRecordId from
	// server using "
	// + "base url: http://localhost:8080/therest/rest/record/. Returned error was: "
	// + "Answer from CoraRestClientSpy read")
	// public void testReadError() throws Exception {
	// coraClient.read(RestClientSpyOld.THIS_RECORD_TYPE_TRIGGERS_AN_ERROR, "someRecordId");
	// }
	//
	// @Test
	// public void testReadAsDataRecord() {
	// ClientDataRecord dataRecord = coraClient
	// .readAsDataRecord("someRecordTypeToBeReturnedAsDataGroup", "someRecordId");
	// assertNotNull(dataRecord);
	//
	// assertCorrectFactoredRestClient();
	// assertTrue(jsonToDataConverterFactory.createForJsonObjectWasCalled);
	//
	// JsonObject jsonSentToConverterFactory = (JsonObject) jsonToDataConverterFactory.jsonValue;
	// String dataGroupPartOfRecordJson = jsonSentToConverterFactory.toJsonFormattedString();
	//
	// String dataGroupPartOfRecord = getExpectedDataGroupJson();
	// assertEquals(dataGroupPartOfRecordJson, dataGroupPartOfRecord);
	//
	// ClientDataGroup clientDataGroupInRecord = dataRecord.getDataGroup();
	// ClientDataGroup dataGroupReturnedFromConverter =
	// jsonToDataConverterFactory.factoredConverter.returnedDataGroup;
	// assertSame(clientDataGroupInRecord, dataGroupReturnedFromConverter);
	//
	// }
	//
	// private void assertCorrectFactoredRestClient() {
	// RestClientSpyOld restClient = restClientFactory.factored.get(0);
	// assertEquals(restClientFactory.factored.size(), 1);
	// assertEquals(restClientFactory.usedAuthToken, "someAuthTokenFromSpy");
	// assertEquals(restClient.recordType, "someRecordTypeToBeReturnedAsDataGroup");
	// assertEquals(restClient.recordId, "someRecordId");
	// }
	//
	// private String getExpectedDataGroupJson() {
	// return
	// "{\"children\":[{\"name\":\"nameInData\",\"value\":\"historicCountry\"},{\"children\":[{\"name\":\"id\",\"value\":\"historicCountryCollection\"},{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"recordType\"},{\"name\":\"linkedRecordId\",\"value\":\"metadataItemCollection\"}],\"name\":\"type\"}],\"name\":\"recordInfo\"},{\"children\":[{\"repeatId\":\"0\",\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"genericCollectionItem\"},{\"name\":\"linkedRecordId\",\"value\":\"gaulHistoricCountryItem\"}],\"name\":\"ref\"},{\"repeatId\":\"1\",\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"genericCollectionItem\"},{\"name\":\"linkedRecordId\",\"value\":\"britainHistoricCountryItem\"}],\"name\":\"ref\"}],\"name\":\"collectionItemReferences\"}],\"name\":\"metadata\",\"attributes\":{\"type\":\"itemCollection\"}}";
	// }
	//
	// @Test
	// public void testReadList() throws Exception {
	// String readListJson = coraClient.readList("someType");
	// RestClientSpyOld restClient = restClientFactory.factored.get(0);
	// assertEquals(restClientFactory.factored.size(), 1);
	// assertEquals(restClientFactory.usedAuthToken, "someAuthTokenFromSpy");
	// assertEquals(restClient.recordType, "someType");
	// assertEquals(readListJson, restClient.returnedAnswer + restClient.methodCalled);
	// assertEquals(restClient.methodCalled, "readList");
	// }
	//
	// @Test
	// public void testReadListAsDataRecords() throws Exception {
	// List<ClientDataRecord> dataRecords = coraClient
	// .readListAsDataRecords("someRecordTypeToBeReturnedAsRecordDataInList");
	//
	// RestClientSpyOld restClient = restClientFactory.factored.get(0);
	// assertEquals(restClientFactory.factored.size(), 1);
	// assertEquals(restClientFactory.usedAuthToken, "someAuthTokenFromSpy");
	// assertEquals(restClient.recordType, "someRecordTypeToBeReturnedAsRecordDataInList");
	// assertEquals(restClient.methodCalled, "readList");
	//
	// JsonToDataConverterSpy converter = jsonToDataConverterFactory.factoredConverters.get(0);
	// assertSame(dataRecords.get(0).getDataGroup(), converter.returnedDataGroup);
	// JsonToDataConverterSpy converter2 = jsonToDataConverterFactory.factoredConverters.get(1);
	// assertSame(dataRecords.get(1).getDataGroup(), converter2.returnedDataGroup);
	//
	// }
	//
	// @Test(expectedExceptions = CoraClientException.class, expectedExceptionsMessageRegExp = ""
	// + "Could not read records of type: thisRecordTypeTriggersAnError from server using "
	// + "base url: http://localhost:8080/therest/rest/record/. Returned error was: "
	// + "Answer from CoraRestClientSpy readList")
	// public void testReadListError() throws Exception {
	// coraClient.readList(RestClientSpyOld.THIS_RECORD_TYPE_TRIGGERS_AN_ERROR);
	// }
	//
	// @Test
	// public void testCreate() throws Exception {
	// String json = "some fake json";
	// String createdJson = coraClient.create("someType", json);
	//
	// assertCorrectDataSentToRestClient(json, createdJson, "create", "someType");
	// }
	//
	// private void assertCorrectDataSentToRestClient(String jsonSentToRestClient,
	// String jsonReturnedFromCreate, String methodCalled, String recordType) {
	// RestClientSpyOld restClient = restClientFactory.factored.get(0);
	// assertEquals(restClientFactory.factored.size(), 1);
	// assertEquals(restClientFactory.usedAuthToken, "someAuthTokenFromSpy");
	// assertEquals(restClient.recordType, recordType);
	// assertEquals(restClient.json, jsonSentToRestClient);
	// assertEquals(jsonReturnedFromCreate, restClient.returnedAnswer + restClient.methodCalled);
	// assertEquals(restClient.methodCalled, methodCalled);
	// }
	//
	// @Test(expectedExceptions = CoraClientException.class, expectedExceptionsMessageRegExp = ""
	// + "Could not create record of type: thisRecordTypeTriggersAnError on server using "
	// + "base url: http://localhost:8080/therest/rest/record/. Returned error was: "
	// + "Answer from CoraRestClientSpy create")
	// public void testCreateError() throws Exception {
	// String json = "some fake json";
	// coraClient.create(RestClientSpyOld.THIS_RECORD_TYPE_TRIGGERS_AN_ERROR, json);
	// }
	//
	// @Test
	// public void testCreateFromClientDataGroup() throws Exception {
	// ClientDataGroup dataGroup = ClientDataGroup.withNameInData("someDataGroup");
	//
	// String createdJson = coraClient.create("someType", dataGroup);
	//
	// assertSame(dataToJsonConverterFactory.clientDataElement, dataGroup);
	// String jsonReturnedFromConverter =
	// dataToJsonConverterFactory.converterSpy.jsonToReturnFromSpy;
	//
	// assertCorrectDataSentToRestClient(jsonReturnedFromConverter, createdJson, "create",
	// "someType");
	//
	// }
	//
	// @Test
	// public void testUpdate() throws Exception {
	// String json = "some fake json";
	// String updatedJson = coraClient.update("someType", "someId", json);
	// RestClientSpyOld restClient = restClientFactory.factored.get(0);
	// assertEquals(restClientFactory.factored.size(), 1);
	// assertEquals(restClientFactory.usedAuthToken, "someAuthTokenFromSpy");
	// assertEquals(restClient.recordType, "someType");
	// assertEquals(restClient.recordId, "someId");
	// assertEquals(restClient.json, json);
	// assertEquals(updatedJson, restClient.returnedAnswer + restClient.methodCalled);
	// assertEquals(restClient.methodCalled, "update");
	// }
	//
	// @Test
	// public void testUpdateFromClientDataGroup() throws Exception {
	// ClientDataGroup dataGroup = ClientDataGroup.withNameInData("someDataGroup");
	//
	// String updatedJson = coraClient.update("someType", "someId", dataGroup);
	//
	// assertSame(dataToJsonConverterFactory.clientDataElement, dataGroup);
	// assertFalse(dataToJsonConverterFactory.includeActionLinks);
	// assertEquals(dataToJsonConverterFactory.methodCalled,
	// "createForClientDataElementIncludingActionLinks");
	//
	// String jsonReturnedFromConverter =
	// dataToJsonConverterFactory.converterSpy.jsonToReturnFromSpy;
	// assertCorrectDataSentToRestClient(jsonReturnedFromConverter, updatedJson, "update",
	// "someType");
	//
	// }
	//
	// @Test(expectedExceptions = CoraClientException.class, expectedExceptionsMessageRegExp = ""
	// + "Could not update record of type: thisRecordTypeTriggersAnError and id: someId "
	// + "on server using "
	// + "base url: http://localhost:8080/therest/rest/record/. Returned error was: "
	// + "Answer from CoraRestClientSpy update")
	// public void testUpdateError() throws Exception {
	// String json = "some fake json";
	// coraClient.update(RestClientSpyOld.THIS_RECORD_TYPE_TRIGGERS_AN_ERROR, "someId", json);
	// }
	//
	// @Test
	// public void testDelete() {
	// String createdJson = coraClient.delete("someType", "someId");
	// RestClientSpyOld restClient = restClientFactory.factored.get(0);
	// assertEquals(restClientFactory.factored.size(), 1);
	// assertEquals(restClientFactory.usedAuthToken, "someAuthTokenFromSpy");
	// assertEquals(restClient.recordType, "someType");
	// assertEquals(restClient.recordId, "someId");
	// assertEquals(createdJson, restClient.returnedAnswer + restClient.methodCalled);
	// assertEquals(restClient.methodCalled, "delete");
	// }
	//
	// @Test(expectedExceptions = CoraClientException.class, expectedExceptionsMessageRegExp = ""
	// + "Could not delete record of type: thisRecordTypeTriggersAnError and id: someId "
	// + "from server using "
	// + "base url: http://localhost:8080/therest/rest/record/. Returned error was: "
	// + "Answer from CoraRestClientSpy delete")
	// public void testDeleteError() throws Exception {
	// coraClient.delete(RestClientSpyOld.THIS_RECORD_TYPE_TRIGGERS_AN_ERROR, "someId");
	// }
	//
	// @Test
	// public void testReadIncomingLinks() throws Exception {
	// String readLinksJson = coraClient.readIncomingLinks("someType", "someId");
	// RestClientSpyOld restClient = restClientFactory.factored.get(0);
	// assertEquals(restClientFactory.factored.size(), 1);
	// assertEquals(restClientFactory.usedAuthToken, "someAuthTokenFromSpy");
	// assertEquals(restClient.recordType, "someType");
	// assertEquals(restClient.recordId, "someId");
	// assertEquals(readLinksJson, restClient.returnedAnswer + restClient.methodCalled);
	// assertEquals(restClient.methodCalled, "readincomingLinks");
	// }
	//
	// @Test(expectedExceptions = CoraClientException.class, expectedExceptionsMessageRegExp = ""
	// + "Could not read incoming links of type: thisRecordTypeTriggersAnError and id: someId "
	// + "from server using "
	// + "base url: http://localhost:8080/therest/rest/record/. Returned error was: "
	// + "Answer from CoraRestClientSpy readincomingLinks")
	// public void testReadincomingLinksError() throws Exception {
	// coraClient.readIncomingLinks(RestClientSpyOld.THIS_RECORD_TYPE_TRIGGERS_AN_ERROR, "someId");
	// }
	//
	// @Test
	// public void testIndexDataRecord() throws Exception {
	//
	// ClientDataRecord clientDataRecord = ClientDataRecord
	// .withClientDataGroup(ClientDataGroup.withNameInData("someDataGroup"));
	// ActionLink actionLink = createActionLinkIndex();
	// clientDataRecord.addActionLink("index", actionLink);
	//
	// String createdJson = coraClient.indexData(clientDataRecord);
	//
	// assertSame(dataToJsonConverterFactory.clientDataElement, actionLink.getBody());
	// String jsonReturnedFromConverter =
	// dataToJsonConverterFactory.converterSpy.jsonToReturnFromSpy;
	//
	// assertCorrectDataSentToRestClient(jsonReturnedFromConverter, createdJson, "create",
	// "workOrder");
	//
	// }
	//
	// private ActionLink createActionLinkIndex() {
	// ClientDataGroup workOrder = createBodyForIndexLink();
	//
	// ActionLink actionLink = ActionLink.withAction(Action.INDEX);
	// actionLink.setBody(workOrder);
	// actionLink.setRequestMethod("POST");
	// actionLink.setURL("http://localhost:8080/systemone/rest/record/workOrder/");
	// return actionLink;
	// }
	//
	// private ClientDataGroup createBodyForIndexLink() {
	// ClientDataGroup workOrder = ClientDataGroup.withNameInData("workOrder");
	// workOrder.addChild(ClientDataAtomic.withNameInDataAndValue("type", "someRecordType"));
	// workOrder.addChild(ClientDataAtomic.withNameInDataAndValue("recordId", "someRecordId"));
	// ClientDataGroup recordType = ClientDataGroup.withNameInData("recordType");
	// recordType.addChild(
	// ClientDataAtomic.withNameInDataAndValue("linkedRecordType", "recordType"));
	// recordType.addChild(ClientDataAtomic.withNameInDataAndValue("linkedRecordId", "demo"));
	// workOrder.addChild(recordType);
	// return workOrder;
	// }
	//
	// @Test(expectedExceptions = CoraClientException.class, expectedExceptionsMessageRegExp = ""
	// + "Could not read index data. No index link found in record.")
	// public void testIndexDataRecordWhenNoIndexLink() {
	//
	// ClientDataRecord clientDataRecord = ClientDataRecord
	// .withClientDataGroup(ClientDataGroup.withNameInData("someDataGroup"));
	//
	// coraClient.indexData(clientDataRecord);
	//
	// }
	//
	// @Test
	// public void testIndexDataRecordUsingRecordTypeAndRecordId() throws Exception {
	// String recordType = "someRecordType";
	// String recordId = "someRecordId";
	// setUpActionLinksToReturn();
	// String responseText = coraClient.indexData(recordType, recordId);
	//
	// String performCommit = "true";
	// assertCorrectExecutionWhenIndexingData(recordType, recordId, responseText, performCommit);
	// }
	//
	// private void assertCorrectExecutionWhenIndexingData(String recordType, String recordId,
	// String responseText, String performCommit) {
	// RestClientSpyOld restClientSpy = restClientFactory.factored.get(0);
	// assertCorrectApptokenAndRestClientCallWhenIndexing(restClientSpy, recordType, recordId);
	//
	// String jsonReturnedFromConverter =
	// dataToJsonConverterFactory.converterSpy.jsonToReturnFromSpy;
	// assertEquals(restClientSpy.json, jsonReturnedFromConverter);
	//
	// assertEquals(responseText, restClientSpy.extendedRestResponse.responseText);
	//
	// ClientDataGroup workOrderDataGroup = (ClientDataGroup)
	// dataToJsonConverterFactory.clientDataElement;
	// assertEquals(workOrderDataGroup.getFirstAtomicValueWithNameInData("performCommit"),
	// performCommit);
	//
	// ActionLink actionLink = (ActionLink) jsonToDataConverterFactory.actionLinksToReturn.get(0);
	// assertSame(workOrderDataGroup, actionLink.getBody());
	// }
	//
	// private void assertCorrectApptokenAndRestClientCallWhenIndexing(RestClientSpyOld
	// restClientSpy,
	// String recordType, String recordId) {
	// AppTokenClientSpy appTokenClient = appTokenClientFactory.factored.get(0);
	// assertNotNull(appTokenClient.returnedAuthToken);
	// assertEquals(appTokenClient.returnedAuthToken, restClientFactory.authToken);
	//
	// assertEquals(restClientSpy.recordTypes.get(0), recordType);
	// assertEquals(restClientSpy.recordIds.get(0), recordId);
	//
	// assertEquals(restClientSpy.recordTypes.get(1), "workOrder");
	// }
	//
	// @Test
	// public void testIndexDataWithoutExplicitCommitRecordUsingRecordTypeAndRecordId()
	// throws Exception {
	// String recordType = "someRecordType";
	// String recordId = "someRecordId";
	//
	// setUpActionLinksToReturn();
	//
	// String responseText = coraClient.indexDataWithoutExplicitCommit(recordType, recordId);
	// assertCorrectExecutionWhenIndexingData(recordType, recordId, responseText, "false");
	// }
	//
	// private void setUpActionLinksToReturn() {
	// List<ClientData> actionLinksToReturn = new ArrayList<>();
	// ActionLink actionLinkIndex = ActionLink.withAction(Action.INDEX);
	// actionLinkIndex.setBody(ClientDataGroup.withNameInData("index"));
	// actionLinksToReturn.add(actionLinkIndex);
	//
	// ActionLink actionLink = ActionLink.withAction(Action.READ);
	// actionLinksToReturn.add(actionLink);
	// jsonToDataConverterFactory.actionLinksToReturn = actionLinksToReturn;
	// }
	//
	// @Test
	// public void testRemoveFromIndex() {
	// String recordType = "someRecordType";
	// String recordId = "someRecordId";
	//
	// String responseText = coraClient.removeFromIndex(recordType, recordId);
	//
	// AppTokenClientSpy appTokenClient = appTokenClientFactory.factored.get(0);
	// assertNotNull(appTokenClient.returnedAuthToken);
	// assertEquals(appTokenClient.returnedAuthToken, restClientFactory.authToken);
	//
	// RestClientSpyOld restClientSpy = restClientFactory.factored.get(0);
	//
	// String jsonReturnedFromConverter =
	// dataToJsonConverterFactory.converterSpy.jsonToReturnFromSpy;
	// ClientDataGroup dataGroupSentToConverter = (ClientDataGroup)
	// dataToJsonConverterFactory.clientDataElement;
	// assertCorrectWorkOrderDataGroupSentToConverter(recordType, recordId,
	// dataGroupSentToConverter);
	//
	// assertEquals(restClientSpy.recordTypes.get(0), "workOrder");
	// assertEquals(restClientSpy.json, jsonReturnedFromConverter);
	// assertEquals(responseText, restClientSpy.extendedRestResponse.responseText);
	// }
	//
	// private void assertCorrectWorkOrderDataGroupSentToConverter(String recordType, String
	// recordId,
	// ClientDataGroup dataGroupSentToConverter) {
	// assertEquals(dataGroupSentToConverter.getNameInData(), "workOrder");
	// assertEquals(dataGroupSentToConverter.getFirstAtomicValueWithNameInData("type"),
	// "removeFromIndex");
	// assertEquals(dataGroupSentToConverter.getFirstAtomicValueWithNameInData("recordId"),
	// recordId);
	// ClientDataGroup recordTypeGroup = dataGroupSentToConverter
	// .getFirstGroupWithNameInData("recordType");
	// assertEquals(recordTypeGroup.getFirstAtomicValueWithNameInData("linkedRecordType"),
	// "recordType");
	// assertEquals(recordTypeGroup.getFirstAtomicValueWithNameInData("linkedRecordId"),
	// recordType);
	// }
	//
	// @Test
	// public void testIndexRecordList() {
	// String jsonFilter = "some fake filter json";
	// String response = coraClient.indexRecordsOfType("someType", jsonFilter);
	//
	// assertEquals(restClientFactory.factored.size(), 1);
	// assertEquals(restClientFactory.usedAuthToken, "someAuthTokenFromSpy");
	//
	// RestClientSpyOld restClient = restClientFactory.factored.get(0);
	// assertEquals(restClient.methodCalled, "batchIndexWithFilterAsJson");
	// assertEquals(restClient.recordType, "someType");
	// assertEquals(restClient.filterAsJson, jsonFilter);
	// assertEquals(response, restClient.returnedAnswer + restClient.methodCalled);
	// }
	//
	// @Test(expectedExceptions = CoraClientException.class, expectedExceptionsMessageRegExp = ""
	// + "Could not index record list of type: thisRecordTypeTriggersAnError on server using "
	// + "base url: http://localhost:8080/therest/rest/record/. Returned error was: "
	// + "Answer from CoraRestClientSpy batchIndexWithFilterAsJson")
	// public void testIndexRecordListError() throws Exception {
	// String jsonFilter = "some fake filter json";
	// coraClient.indexRecordsOfType(RestClientSpyOld.THIS_RECORD_TYPE_TRIGGERS_AN_ERROR,
	// jsonFilter);
	// }

}
