/*
 * Copyright 2018, 2020, 2021, 2023 Uppsala University Library
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Optional;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientDataList;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterProvider;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterProvider;
import se.uu.ub.cora.clientdata.spies.ClientDataListSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordGroupSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataRecordSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataToJsonConverterFactoryCreatorSpy;
import se.uu.ub.cora.clientdata.spies.ClientDataToJsonConverterFactorySpy;
import se.uu.ub.cora.clientdata.spies.ClientDataToJsonConverterSpy;
import se.uu.ub.cora.clientdata.spies.JsonToClientDataConverterFactorySpy;
import se.uu.ub.cora.clientdata.spies.JsonToClientDataConverterSpy;
import se.uu.ub.cora.javaclient.cora.internal.DataClientImp;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestClientSpy;
import se.uu.ub.cora.javaclient.rest.RestResponse;

public class DataClientTest {
	private static final String RECORD_TYPE = "someRecordType";
	private static final String RECORD_ID = "someRecordId";
	private DataClientImp dataClient;
	private RestClientSpy restClient;
	private ClientDataToJsonConverterFactoryCreatorSpy dataToJsonFactoryCreator;
	private JsonToClientDataConverterFactorySpy jsonToDataFactory;
	private static final RestResponse OK_RESPONSE = new RestResponse(200, "", Optional.empty());
	private static final RestResponse CREATED_RESPONSE = new RestResponse(201, "",
			Optional.of("someNewId"));;
	private static final RestResponse INTERNAL_ERROR_RESPONSE = new RestResponse(500,
			"ErrorMessageFromRest", Optional.empty());
	private ClientDataRecordGroupSpy dataRecordGroup;
	private ClientDataRecordSpy clientDataRecordSpy;
	private JsonToClientDataConverterSpy jsonToDataConverter;
	private ClientDataToJsonConverterFactorySpy dataToJsonConverterFactoryFromProvider;

	@BeforeMethod
	public void beforeMethod() {
		dataToJsonFactoryCreator = new ClientDataToJsonConverterFactoryCreatorSpy();
		ClientDataToJsonConverterProvider
				.setDataToJsonConverterFactoryCreator(dataToJsonFactoryCreator);

		dataToJsonConverterFactoryFromProvider = new ClientDataToJsonConverterFactorySpy();
		dataToJsonFactoryCreator.MRV.setDefaultReturnValuesSupplier("createFactory",
				() -> dataToJsonConverterFactoryFromProvider);

		jsonToDataFactory = new JsonToClientDataConverterFactorySpy();
		JsonToClientDataConverterProvider.setJsonToDataConverterFactory(jsonToDataFactory);
		setUpConverterToReturnDataRecord();

		restClient = new RestClientSpy();
		dataClient = new DataClientImp(restClient);

		dataRecordGroup = new ClientDataRecordGroupSpy();
	}

	@Test
	public void testInit() throws Exception {
		RestClient restClient2 = dataClient.onlyForTestGetRestClient();
		assertSame(restClient2, restClient);

		dataToJsonFactoryCreator.MCR.assertParameters("createFactory", 0);
	}

	@Test
	public void testCreate() throws Exception {
		restClient.MRV.setDefaultReturnValuesSupplier("createRecordFromJson",
				() -> CREATED_RESPONSE);

		ClientDataRecord createdDataRecord = dataClient.create(RECORD_TYPE, dataRecordGroup);

		String json = assertDataGroupConvertedToJsonByProviderReturnJsonString(dataRecordGroup);
		RestResponse createResponse = assertCreateRecord(json);
		assertConvertToData(createdDataRecord, createResponse);

	}

	private void setUpConverterToReturnDataRecord() {
		clientDataRecordSpy = new ClientDataRecordSpy();
		serUpDataTypetoReturnForAConverter(clientDataRecordSpy);
	}

	private void serUpDataTypetoReturnForAConverter(ClientConvertible clientConvertible) {
		jsonToDataConverter = new JsonToClientDataConverterSpy();

		jsonToDataConverter.MRV.setDefaultReturnValuesSupplier("toInstance",
				() -> clientConvertible);
		jsonToDataFactory.MRV.setDefaultReturnValuesSupplier("factorUsingString",
				() -> jsonToDataConverter);
	}

	private RestResponse assertCreateRecord(String json) {
		restClient.MCR.assertParameters("createRecordFromJson", 0, RECORD_TYPE, json);

		RestResponse createdResponse = (RestResponse) restClient.MCR
				.getReturnValue("createRecordFromJson", 0);
		return createdResponse;
	}

	private void assertConvertToData(ClientConvertible createdDataRecord,
			RestResponse createdResponse) {
		String createdJson = createdResponse.responseText();

		jsonToDataFactory.MCR.assertParameters("factorUsingString", 0, createdJson);

		JsonToClientDataConverterSpy jsonToClientConverter = (JsonToClientDataConverterSpy) jsonToDataFactory.MCR
				.getReturnValue("factorUsingString", 0);
		jsonToClientConverter.MCR.assertReturn("toInstance", 0, createdDataRecord);
	}

	private String assertDataGroupConvertedToJsonByProviderReturnJsonString(
			ClientDataRecordGroup dataRecordGroup) {
		ClientDataToJsonConverterFactorySpy converterFactorySpy = getConverterFactory();

		converterFactorySpy.MCR.assertParameters("factorUsingConvertible", 0, dataRecordGroup);
		ClientDataToJsonConverterSpy converterSpy = (ClientDataToJsonConverterSpy) converterFactorySpy.MCR
				.getReturnValue("factorUsingConvertible", 0);
		return (String) converterSpy.MCR.getReturnValue("toJson", 0);
	}

	private ClientDataToJsonConverterFactorySpy getConverterFactory() {
		return (ClientDataToJsonConverterFactorySpy) dataToJsonFactoryCreator.MCR
				.getReturnValue("createFactory", 0);
	}

	@Test
	public void testCreateErrorCodeOnRestClient() throws Exception {
		restClient.MRV.setDefaultReturnValuesSupplier("createRecordFromJson",
				() -> INTERNAL_ERROR_RESPONSE);
		try {
			dataClient.create(RECORD_TYPE, dataRecordGroup);
			ensureItFails();
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Could not create record of type: " + RECORD_TYPE
					+ ". Returned error was: " + INTERNAL_ERROR_RESPONSE.responseText());
		}
	}

	@Test
	public void testCreateExceptionOnConvertToJson() throws Exception {
		dataToJsonConverterFactoryFromProvider.MRV.setAlwaysThrowException("factorUsingConvertible",
				new RuntimeException("SomeErrorMessageFromException"));

		try {
			dataClient.create(RECORD_TYPE, dataRecordGroup);

			ensureItFails();
		} catch (Exception e) {
			assertTrue(e instanceof CoraClientException);
			assertEquals(e.getMessage(), "Could not create record of type: " + RECORD_TYPE
					+ ". Returned error was: SomeErrorMessageFromException");
		}
	}

	@Test
	public void testCreateExceptionOnConvertToData() throws Exception {
		jsonToDataFactory.MRV.setAlwaysThrowException("factorUsingString",
				new RuntimeException("SomeErrorMessageFromException"));

		restClient.MRV.setDefaultReturnValuesSupplier("createRecordFromJson",
				() -> CREATED_RESPONSE);

		try {
			dataClient.create(RECORD_TYPE, dataRecordGroup);

			ensureItFails();
		} catch (Exception e) {
			assertTrue(e instanceof CoraClientException);
			assertEquals(e.getMessage(), "Could not create record of type: " + RECORD_TYPE
					+ ". Returned error was: SomeErrorMessageFromException");
		}
	}

	private void ensureItFails() {
		assertFalse(true);
	}

	@Test
	public void testRead() {
		restClient.MRV.setDefaultReturnValuesSupplier("readRecordAsJson", () -> OK_RESPONSE);

		ClientDataRecord record = dataClient.read(RECORD_TYPE, RECORD_ID);

		restClient.MCR.assertParameters("readRecordAsJson", 0, RECORD_TYPE, RECORD_ID);

		var restResponse = restClient.MCR.getReturnValue("readRecordAsJson", 0);
		assertConvertToData(record, (RestResponse) restResponse);
	}

	@Test
	public void testReadErrorCodeOnOnRestClient() throws Exception {
		restClient.MRV.setDefaultReturnValuesSupplier("readRecordAsJson",
				() -> INTERNAL_ERROR_RESPONSE);
		try {
			dataClient.read(RECORD_TYPE, RECORD_ID);
			ensureItFails();
		} catch (Exception e) {
			assertEquals(e.getMessage(),
					"Could not read record of type: " + RECORD_TYPE + " and id: " + RECORD_ID
							+ ". Returned error was: " + INTERNAL_ERROR_RESPONSE.responseText());
		}
	}

	@Test
	public void testReadExceptionOnConvertToData() throws Exception {
		jsonToDataFactory.MRV.setAlwaysThrowException("factorUsingString",
				new RuntimeException("SomeErrorMessageFromException"));

		restClient.MRV.setDefaultReturnValuesSupplier("readRecordAsJson", () -> OK_RESPONSE);

		try {
			dataClient.read(RECORD_TYPE, RECORD_ID);

			ensureItFails();
		} catch (Exception e) {
			assertTrue(e instanceof CoraClientException);
			assertEquals(e.getMessage(),
					"Could not read record of type: " + RECORD_TYPE + " and id: " + RECORD_ID
							+ ". Returned error was: SomeErrorMessageFromException");
		}
	}

	@Test
	public void testReadList() throws Exception {
		setUpDataListToReturnForConverter();

		restClient.MRV.setDefaultReturnValuesSupplier("readRecordListAsJson", () -> OK_RESPONSE);

		ClientDataList recordList = dataClient.readList(RECORD_TYPE);

		restClient.MCR.assertParameters("readRecordListAsJson", 0, RECORD_TYPE);

		var restResponse = restClient.MCR.getReturnValue("readRecordListAsJson", 0);
		assertConvertToData(recordList, (RestResponse) restResponse);
	}

	private void setUpDataListToReturnForConverter() {
		ClientDataListSpy clientDataList = new ClientDataListSpy();
		serUpDataTypetoReturnForAConverter(clientDataList);
	}

	@Test
	public void testReadListErrorCodeOnOnRestClient() throws Exception {
		setUpDataListToReturnForConverter();

		restClient.MRV.setDefaultReturnValuesSupplier("readRecordListAsJson",
				() -> INTERNAL_ERROR_RESPONSE);

		try {
			dataClient.readList(RECORD_TYPE);
			ensureItFails();
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Could not list records of type: " + RECORD_TYPE
					+ ". Returned error was: " + INTERNAL_ERROR_RESPONSE.responseText());
		}
	}

	@Test
	public void testReadListExceptionOnConvertToData() throws Exception {
		setUpDataListToReturnForConverter();

		jsonToDataFactory.MRV.setAlwaysThrowException("factorUsingString",
				new RuntimeException("SomeErrorMessageFromException"));

		restClient.MRV.setDefaultReturnValuesSupplier("readRecordAsJson", () -> OK_RESPONSE);

		try {
			dataClient.readList(RECORD_TYPE);

			ensureItFails();
		} catch (Exception e) {
			assertTrue(e instanceof CoraClientException);
			assertEquals(e.getMessage(), "Could not list records of type: " + RECORD_TYPE
					+ ". Returned error was: SomeErrorMessageFromException");
		}
	}

	@Test
	public void testUpdate() throws Exception {

		restClient.MRV.setDefaultReturnValuesSupplier("updateRecordFromJson", () -> OK_RESPONSE);

		ClientDataRecord record = dataClient.update(RECORD_TYPE, RECORD_ID, dataRecordGroup);

		String json = assertDataGroupConvertedToJsonByProviderReturnJsonString(dataRecordGroup);
		restClient.MCR.assertParameters("updateRecordFromJson", 0, RECORD_TYPE, RECORD_ID, json);

		var restResponse = restClient.MCR.getReturnValue("updateRecordFromJson", 0);
		assertConvertToData(record, (RestResponse) restResponse);

	}

	@Test
	public void testUpdateErrorCodeOnOnRestClient() throws Exception {
		restClient.MRV.setDefaultReturnValuesSupplier("updateRecordFromJson",
				() -> INTERNAL_ERROR_RESPONSE);

		try {
			dataClient.update(RECORD_TYPE, RECORD_ID, dataRecordGroup);
			ensureItFails();
		} catch (Exception e) {
			assertEquals(e.getMessage(),
					"Could not update record of type: " + RECORD_TYPE + " and id: " + RECORD_ID
							+ ". Returned error was: " + INTERNAL_ERROR_RESPONSE.responseText());
		}
	}

	// TODO: Continue with exception for converter on update
	// TODO:Convert for DataList from JsonToData

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
