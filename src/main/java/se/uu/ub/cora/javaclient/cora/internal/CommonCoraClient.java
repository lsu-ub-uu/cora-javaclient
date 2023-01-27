/*
 * Copyright 2020, 2021 Uppsala University Library
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
package se.uu.ub.cora.javaclient.cora.internal;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.clientdata.ClientDataAtomic;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverter;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterFactory;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterFactory;
import se.uu.ub.cora.javaclient.cora.CoraClientException;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestResponse;
import se.uu.ub.cora.json.parser.JsonArray;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParser;
import se.uu.ub.cora.json.parser.JsonValue;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class CommonCoraClient {

	private static final int OK = 200;
	private static final int CREATED = 201;
	private static final String RETURNED_ERROR_WAS = ". Returned error was: ";
	private static final String SERVER_USING_URL = "server using base url: ";
	static final String FROM = " from ";
	static final String AND_ID = " and id: ";
	protected ClientDataToJsonConverterFactory dataToJsonConverterFactory;
	protected JsonToClientDataConverterFactory jsonToDataConverterFactory;

	protected String create(RestClient restClient, String recordType, String json) {
		ExtendedRestResponse response = restClient.createRecordFromJson(recordType, json);
		possiblyThrowErrorIfNotCreated(restClient, recordType, response);
		return response.responseText;
	}

	void possiblyThrowErrorIfNotCreated(RestClient restClient, String recordType,
			ExtendedRestResponse response) {
		if (statusIsNotCreated(response.statusCode)) {
			String url = restClient.getBaseUrl();
			throw new CoraClientException("Could not create record of type: " + recordType + " on "
					+ SERVER_USING_URL + url + RETURNED_ERROR_WAS + response.responseText);
		}
	}

	private boolean statusIsNotCreated(int statusCode) {
		return statusCode != CREATED;
	}

	protected String create(RestClient restClient, String recordType, ClientDataGroup dataGroup) {
		String json = convertDataGroupToJson(dataGroup);
		return create(restClient, recordType, json);
	}

	protected String convertDataGroupToJson(ClientDataGroup dataGroup) {
		ClientDataToJsonConverter converter = createConverter(dataGroup);
		return converter.toJson();
	}

	protected ClientDataToJsonConverter createConverter(ClientDataGroup dataGroup) {
		return dataToJsonConverterFactory.createForClientDataElement(dataGroup);
	}

	protected String read(RestClient restClient, String recordType, String recordId) {
		RestResponse response = restClient.readRecordAsJson(recordType, recordId);
		possiblyThrowErrorForReadRecordTypeAndIdIfNotOk(restClient, response, recordType, recordId);
		return response.responseText;
	}

	private void possiblyThrowErrorForReadRecordTypeAndIdIfNotOk(RestClient restClient,
			RestResponse response, String recordType, String recordId) {
		possiblyThrowErrorIfNotOk(restClient, response,
				"Could not read record of type: " + recordType + AND_ID + recordId + FROM);
	}

	void possiblyThrowErrorIfNotOk(RestClient restClient, RestResponse response,
			String messageStart) {
		if (statusIsNotOk(response.statusCode)) {
			String url = restClient.getBaseUrl();
			throw new CoraClientException(messageStart + SERVER_USING_URL + url + RETURNED_ERROR_WAS
					+ response.responseText);
		}
	}

	protected boolean statusIsNotOk(int statusCode) {
		return statusCode != OK;
	}

	protected String update(RestClient restClient, String recordType, String recordId,
			String json) {
		RestResponse response = restClient.updateRecordFromJson(recordType, recordId, json);
		possiblyThrowErrorForUpdateRecordTypeAndIdIfNotOk(restClient, response, recordType,
				recordId);
		return response.responseText;
	}

	private void possiblyThrowErrorForUpdateRecordTypeAndIdIfNotOk(RestClient restClient,
			RestResponse response, String recordType, String recordId) {
		possiblyThrowErrorIfNotOk(restClient, response,
				"Could not update record of type: " + recordType + AND_ID + recordId + " on ");
	}

	protected String deleteRecord(RestClient restClient, String recordType, String recordId) {
		RestResponse response = restClient.deleteRecord(recordType, recordId);
		possiblyThrowErrorForDeleteIfNotOk(restClient, response, recordType, recordId);
		return response.responseText;
	}

	private void possiblyThrowErrorForDeleteIfNotOk(RestClient restClient, RestResponse response,
			String recordType, String recordId) {
		possiblyThrowErrorIfNotOk(restClient, response,
				"Could not delete record of type: " + recordType + AND_ID + recordId + FROM);
	}

	protected ClientDataRecord readAsDataRecord(RestClient restClient, String recordType,
			String recordId) {
		String readJson = read(restClient, recordType, recordId);
		JsonObject readJsonObject = createJsonObjectFromResponseText(readJson);
		return convertToDataRecord(readJsonObject);
	}

	JsonObject createJsonObjectFromResponseText(String responseText) {
		JsonParser jsonParser = new OrgJsonParser();
		JsonValue jsonValue = jsonParser.parseString(responseText);
		return (JsonObject) jsonValue;
	}

	ClientDataRecord convertToDataRecord(JsonObject readJsonObject) {
		JsonToDataRecordConverterImp recordConverter = JsonToDataRecordConverterImp
				.usingConverterFactory(jsonToDataConverterFactory);
		return (ClientDataRecord) recordConverter.toInstance(readJsonObject);
	}

	protected String update(RestClient restClient, String recordType, String recordId,
			ClientDataGroup dataGroup) {
		String json = convertDataGroupToJsonWithoutLinks(dataGroup);
		return update(restClient, recordType, recordId, json);
	}

	String convertDataGroupToJsonWithoutLinks(ClientDataGroup dataGroup) {
		ClientDataToJsonConverter converter = createConverterWithoutLinks(dataGroup);
		return converter.toJson();
	}

	private ClientDataToJsonConverter createConverterWithoutLinks(ClientDataGroup dataGroup) {
		return dataToJsonConverterFactory.createForClientDataElementIncludingActionLinks(dataGroup,
				false);
	}

	protected String readList(RestClient restClient, String recordType) {
		RestResponse response = restClient.readRecordListAsJson(recordType);
		possiblyThrowErrorForReadList(restClient, recordType, response);
		return response.responseText;
	}

	private void possiblyThrowErrorForReadList(RestClient restClient, String recordType,
			RestResponse response) {
		if (statusIsNotOk(response.statusCode)) {
			String url = restClient.getBaseUrl();
			throw new CoraClientException("Could not read records of type: " + recordType + FROM
					+ SERVER_USING_URL + url + RETURNED_ERROR_WAS + response.responseText);
		}
	}

	protected List<ClientDataRecord> readListAsDataRecords(RestClient restClient,
			String recordType) {
		String responseText = readList(restClient, recordType);
		JsonArray data = extractDataFromResponse(responseText);

		return convertRecords(data);
	}

	JsonArray extractDataFromResponse(String responseText) {
		JsonParser jsonParser = new OrgJsonParser();
		JsonObject responseObject = (JsonObject) jsonParser.parseString(responseText);

		JsonObject dataList = responseObject.getValueAsJsonObject("dataList");
		return dataList.getValueAsJsonArray("data");
	}

	List<ClientDataRecord> convertRecords(JsonArray data) {
		List<ClientDataRecord> dataRecords = new ArrayList<>();
		for (JsonValue jsonValue : data) {
			convertAndAddRecord(dataRecords, (JsonObject) jsonValue);
		}
		return dataRecords;
	}

	private void convertAndAddRecord(List<ClientDataRecord> dataRecords, JsonObject jsonValue) {
		JsonObject readJsonObject = jsonValue;
		ClientDataRecord dataRecord = convertToDataRecord(readJsonObject);
		dataRecords.add(dataRecord);
	}

	protected String readIncomingLinks(RestClient restClient, String recordType, String recordId) {
		RestResponse response = restClient.readIncomingLinksAsJson(recordType, recordId);
		possiblyThrowErrorForIncomingLinksIfNotOk(restClient, response, recordType, recordId);
		return response.responseText;
	}

	private void possiblyThrowErrorForIncomingLinksIfNotOk(RestClient restClient,
			RestResponse response, String recordType, String recordId) {
		possiblyThrowErrorIfNotOk(restClient, response,
				"Could not read incoming links of type: " + recordType + AND_ID + recordId + FROM);
	}

	protected String indexData(RestClient restClient, ClientDataRecord clientDataRecord,
			boolean explicitCommit) {
		throwErrorIfNoIndexLink(clientDataRecord);
		ClientDataGroup bodyDataGroup = getWorkOrderDataGroup(clientDataRecord, explicitCommit);
		return create(restClient, "workOrder", bodyDataGroup);
	}

	private ClientDataGroup getWorkOrderDataGroup(ClientDataRecord clientDataRecord,
			boolean explicitCommit) {
		ActionLink index = clientDataRecord.getActionLink("index");
		ClientDataGroup bodyDataGroup = index.getBody();
		bodyDataGroup.addChild(ClientDataAtomic.withNameInDataAndValue("performCommit",
				String.valueOf(explicitCommit)));
		return bodyDataGroup;
	}

	private void throwErrorIfNoIndexLink(DataRecord clientDataRecord) {
		if (!clientDataRecord.getActionLinks().containsKey("index")) {
			throw new CoraClientException(
					"Could not read index data. No index link found in record.");
		}
	}

	public ClientDataToJsonConverterFactory getDataToJsonConverterFactory() {
		// needed for test
		return dataToJsonConverterFactory;
	}

	public JsonToClientDataConverterFactory getJsonToDataConverterFactory() {
		// needed for test
		return jsonToDataConverterFactory;
	}

	protected ClientDataGroup createWorkOrderForRemoveFromIndex(String recordType,
			String recordId) {
		ClientDataGroup workOrder = ClientDataGroup.withNameInData("workOrder");
		workOrder.addChild(ClientDataAtomic.withNameInDataAndValue("type", "removeFromIndex"));
		workOrder.addChild(ClientDataAtomic.withNameInDataAndValue("recordId", recordId));

		createAndAddRecordType(recordType, workOrder);
		return workOrder;
	}

	void createAndAddRecordType(String recordType, ClientDataGroup workOrder) {
		ClientDataGroup recordTypeGroup = ClientDataGroup
				.asLinkWithNameInDataAndTypeAndId("recordType", "recordType", recordType);
		workOrder.addChild(recordTypeGroup);
	}

	void possiblyThrowErrorIfNotPossibleToBatchIndex(RestClient restClient, String recordType,
			ExtendedRestResponse response) {
		if (statusIsNotCreated(response.statusCode)) {
			String url = restClient.getBaseUrl();
			throw new CoraClientException("Could not index record list of type: " + recordType
					+ " on " + SERVER_USING_URL + url + RETURNED_ERROR_WAS + response.responseText);
		}
	}

	protected String indexRecordList(RestClient restClient, String recordType,
			String indexSettingsAsJson) {
		ExtendedRestResponse response = restClient.batchIndexWithFilterAsJson(recordType,
				indexSettingsAsJson);
		possiblyThrowErrorIfNotPossibleToBatchIndex(restClient, recordType, response);
		return response.responseText;
	}
}
