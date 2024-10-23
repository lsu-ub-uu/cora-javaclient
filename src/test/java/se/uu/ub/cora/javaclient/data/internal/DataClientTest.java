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
package se.uu.ub.cora.javaclient.data.internal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

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
import se.uu.ub.cora.javaclient.data.DataClientException;
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
	private static final RestResponse CREATED_RESPONSE = new RestResponse(201, "Some response text",
			Optional.empty(), Optional.of("someNewId"));;
	private static final RestResponse INTERNAL_ERROR_RESPONSE = new RestResponse(500,
			"ErrorMessageFromRest", Optional.empty(), Optional.empty());
	private ClientDataRecordGroupSpy dataRecordGroup;
	private ClientDataRecordSpy clientDataRecordSpy;
	private JsonToClientDataConverterSpy jsonToDataConverter;
	private ClientDataToJsonConverterFactorySpy dataToJsonConverterFactoryFromProvider;

	@BeforeMethod
	public void beforeMethod() {
		setUpDataToJsonConverter();
		setUpJsonToDataConverter();
		setUpConverterToReturnDataRecord();

		restClient = new RestClientSpy();
		dataClient = new DataClientImp(restClient);

		dataRecordGroup = new ClientDataRecordGroupSpy();
	}

	private void setUpDataToJsonConverter() {
		dataToJsonFactoryCreator = new ClientDataToJsonConverterFactoryCreatorSpy();
		ClientDataToJsonConverterProvider
				.setDataToJsonConverterFactoryCreator(dataToJsonFactoryCreator);

		dataToJsonConverterFactoryFromProvider = new ClientDataToJsonConverterFactorySpy();
		dataToJsonFactoryCreator.MRV.setDefaultReturnValuesSupplier("createFactory",
				() -> dataToJsonConverterFactoryFromProvider);

		ClientDataToJsonConverterSpy dataToJsonConverter = new ClientDataToJsonConverterSpy();
		dataToJsonConverter.MRV.setDefaultReturnValuesSupplier("toJson", () -> "converted json");

		dataToJsonConverterFactoryFromProvider.MRV.setDefaultReturnValuesSupplier(
				"factorUsingConvertible", () -> dataToJsonConverter);
	}

	private void setUpJsonToDataConverter() {
		jsonToDataFactory = new JsonToClientDataConverterFactorySpy();
		JsonToClientDataConverterProvider.setJsonToDataConverterFactory(jsonToDataFactory);
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
			fail("Should throw Exception");
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Could not create record of type: " + RECORD_TYPE
					+ ". Returned error was: " + INTERNAL_ERROR_RESPONSE.responseText());
		}
	}

	@Test
	public void testCreateExceptionOnConvertToJson() throws Exception {
		dataToJsonConverterFactoryFromProvider.MRV.setAlwaysThrowException("factorUsingConvertible",
				new RuntimeException("someErrorConversionToJson"));

		try {
			dataClient.create(RECORD_TYPE, dataRecordGroup);

			fail("Should throw Exception");
		} catch (Exception e) {
			assertTrue(e instanceof DataClientException);
			assertEquals(e.getMessage(), "Could not create record of type: " + RECORD_TYPE
					+ ". Returned error was: someErrorConversionToJson");
		}
	}

	@Test
	public void testCreateExceptionOnConvertToData() throws Exception {
		jsonToDataFactory.MRV.setAlwaysThrowException("factorUsingString",
				new RuntimeException("someErrorConversionToData"));

		restClient.MRV.setDefaultReturnValuesSupplier("createRecordFromJson",
				() -> CREATED_RESPONSE);

		try {
			dataClient.create(RECORD_TYPE, dataRecordGroup);

			fail("Should throw Exception");
		} catch (Exception e) {
			assertTrue(e instanceof DataClientException);
			assertEquals(e.getMessage(), "Could not create record of type: " + RECORD_TYPE
					+ ". Returned error was: someErrorConversionToData");
		}
	}

	@Test
	public void testRead() {
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
			fail("Should throw Exception");
		} catch (Exception e) {
			assertEquals(e.getMessage(),
					"Could not read record of type: " + RECORD_TYPE + " and id: " + RECORD_ID
							+ ". Returned error was: " + INTERNAL_ERROR_RESPONSE.responseText());
		}
	}

	@Test
	public void testReadExceptionOnConvertToData() throws Exception {
		jsonToDataFactory.MRV.setAlwaysThrowException("factorUsingString",
				new RuntimeException("someErrorConversionToData"));

		try {
			dataClient.read(RECORD_TYPE, RECORD_ID);

			fail("Should throw Exception");
		} catch (Exception e) {
			assertTrue(e instanceof DataClientException);
			assertEquals(e.getMessage(), "Could not read record of type: " + RECORD_TYPE
					+ " and id: " + RECORD_ID + ". Returned error was: someErrorConversionToData");
		}
	}

	@Test
	public void testReadList() throws Exception {
		setUpDataListToReturnForConverter();

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
			fail("Should throw Exception");
		} catch (Exception e) {
			assertEquals(e.getMessage(), "Could not list records of type: " + RECORD_TYPE
					+ ". Returned error was: " + INTERNAL_ERROR_RESPONSE.responseText());
		}
	}

	@Test
	public void testReadListExceptionOnConvertToData() throws Exception {
		setUpDataListToReturnForConverter();

		jsonToDataFactory.MRV.setAlwaysThrowException("factorUsingString",
				new RuntimeException("someErrorConversionToData"));

		try {
			dataClient.readList(RECORD_TYPE);

			fail("Should throw Exception");
		} catch (Exception e) {
			assertTrue(e instanceof DataClientException);
			assertEquals(e.getMessage(), "Could not list records of type: " + RECORD_TYPE
					+ ". Returned error was: someErrorConversionToData");
		}
	}

	@Test
	public void testUpdate() throws Exception {

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
			fail("Should throw Exception");
		} catch (Exception e) {
			DataClientException dataClientException = (DataClientException) e;
			assertEquals(dataClientException.getResponseCode().get(), 500);
			assertEquals(dataClientException.getMessage(),
					"Could not update record of type: " + RECORD_TYPE + " and id: " + RECORD_ID
							+ ". Returned error was: " + INTERNAL_ERROR_RESPONSE.responseText());
		}
	}

	@Test
	public void testUpdateExceptionOnConversionToJson() throws Exception {
		dataToJsonConverterFactoryFromProvider.MRV.setAlwaysThrowException("factorUsingConvertible",
				new RuntimeException("someErrorConversionToJson"));

		try {
			dataClient.update(RECORD_TYPE, RECORD_ID, dataRecordGroup);
			fail("Should throw Exception");
		} catch (Exception e) {
			assertTrue(e instanceof DataClientException);
			assertEquals(e.getMessage(), "Could not update record of type: " + RECORD_TYPE
					+ " and id: " + RECORD_ID + ". Returned error was: someErrorConversionToJson");
		}
	}

	@Test
	public void testUpdateExceptionOnConversionToData() throws Exception {
		jsonToDataFactory.MRV.setAlwaysThrowException("factorUsingString",
				new RuntimeException("someErrorConversionToData"));
		try {
			dataClient.update(RECORD_TYPE, RECORD_ID, dataRecordGroup);
			fail("Should throw Exception");
		} catch (Exception e) {
			assertTrue(e instanceof DataClientException);
			assertEquals(e.getMessage(), "Could not update record of type: " + RECORD_TYPE
					+ " and id: " + RECORD_ID + ". Returned error was: someErrorConversionToData");
		}
	}

	@Test
	public void testDelete() throws Exception {
		dataClient.delete(RECORD_TYPE, RECORD_ID);

		restClient.MCR.assertParameters("deleteRecord", 0, RECORD_TYPE, RECORD_ID);

		var restResponse = restClient.MCR.getReturnValue("deleteRecord", 0);
		assertEquals(((RestResponse) restResponse).responseCode(), 200);

	}

	@Test
	public void testDeleteErrorCodeOnOnRestClient() throws Exception {
		restClient.MRV.setDefaultReturnValuesSupplier("deleteRecord",
				() -> INTERNAL_ERROR_RESPONSE);

		try {
			dataClient.delete(RECORD_TYPE, RECORD_ID);
			fail("Should throw Exception");
		} catch (Exception e) {
			assertEquals(e.getMessage(),
					"Could not delete record of type: " + RECORD_TYPE + " and id: " + RECORD_ID
							+ ". Returned error was: " + INTERNAL_ERROR_RESPONSE.responseText());
		}
	}

	@Test
	public void testReadIncommingLinks() throws Exception {
		setUpDataListToReturnForConverter();

		ClientDataList incommingLinks = dataClient.readIncomingLinks(RECORD_TYPE, RECORD_ID);

		restClient.MCR.assertParameters("readIncomingLinksAsJson", 0, RECORD_TYPE, RECORD_ID);

		var restResponse = restClient.MCR.getReturnValue("readIncomingLinksAsJson", 0);
		assertConvertToData(incommingLinks, (RestResponse) restResponse);
	}

	@Test
	public void testReadIncommingLinksErrorCodeOnOnRestClient() throws Exception {
		setUpDataListToReturnForConverter();

		restClient.MRV.setDefaultReturnValuesSupplier("readIncomingLinksAsJson",
				() -> INTERNAL_ERROR_RESPONSE);

		try {
			dataClient.readIncomingLinks(RECORD_TYPE, RECORD_ID);

			fail("Should throw Exception");
		} catch (Exception e) {
			assertEquals(e.getMessage(),
					"Could not read incomming links for type: " + RECORD_TYPE + " and id: "
							+ RECORD_ID + ". Returned error was: "
							+ INTERNAL_ERROR_RESPONSE.responseText());
		}
	}

	@Test
	public void testReadIncommingLinksExceptionOnConvertToData() throws Exception {
		setUpDataListToReturnForConverter();

		jsonToDataFactory.MRV.setAlwaysThrowException("factorUsingString",
				new RuntimeException("someErrorConversionToData"));

		try {
			dataClient.readIncomingLinks(RECORD_TYPE, RECORD_ID);

			fail("Should throw Exception");
		} catch (Exception e) {
			assertTrue(e instanceof DataClientException);
			assertEquals(e.getMessage(), "Could not read incomming links for type: " + RECORD_TYPE
					+ " and id: " + RECORD_ID + ". Returned error was: someErrorConversionToData");
		}
	}

}
