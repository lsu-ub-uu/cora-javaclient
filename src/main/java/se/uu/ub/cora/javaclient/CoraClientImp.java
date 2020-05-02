/*
 * Copyright 2018, 2019 Uppsala University Library
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

import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.converter.javatojson.DataToJsonConverter;
import se.uu.ub.cora.clientdata.converter.javatojson.DataToJsonConverterFactory;
import se.uu.ub.cora.clientdata.converter.jsontojava.JsonToDataConverterFactory;
import se.uu.ub.cora.clientdata.converter.jsontojava.JsonToDataRecordConverterImp;
import se.uu.ub.cora.javaclient.apptoken.AppTokenClient;
import se.uu.ub.cora.javaclient.apptoken.AppTokenClientFactory;
import se.uu.ub.cora.javaclient.cora.CoraClient;
import se.uu.ub.cora.javaclient.cora.CoraClientException;
import se.uu.ub.cora.javaclient.rest.ExtendedRestResponse;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestClientFactory;
import se.uu.ub.cora.javaclient.rest.RestResponse;
import se.uu.ub.cora.json.builder.JsonBuilderFactory;
import se.uu.ub.cora.json.builder.org.OrgJsonBuilderFactoryAdapter;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParser;
import se.uu.ub.cora.json.parser.JsonValue;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class CoraClientImp implements CoraClient {

	private static final int OK = 200;
	private static final int CREATED = 201;
	private static final String FROM = " from ";
	private static final String AND_ID = " and id: ";
	private static final String RETURNED_ERROR_WAS = ". Returned error was: ";
	private static final String SERVER_USING_URL = "server using base url: ";

	private RestClientFactory restClientFactory;
	private AppTokenClient appTokenClient;
	private AppTokenClientFactory appTokenClientFactory;
	private String userId;
	private String appToken;
	private DataToJsonConverterFactory dataToJsonConverterFactory;
	private JsonToDataConverterFactory jsonToDataConverterFactory;

	public CoraClientImp(CoraClientDependencies coraClientDependencies) {
		this.appTokenClientFactory = coraClientDependencies.appTokenClientFactory;
		this.restClientFactory = coraClientDependencies.restClientFactory;
		this.dataToJsonConverterFactory = coraClientDependencies.dataToJsonConverterFactory;
		this.jsonToDataConverterFactory = coraClientDependencies.jsonToDataConverterFactory;
		this.userId = coraClientDependencies.userId;
		this.appToken = coraClientDependencies.appToken;
		appTokenClient = appTokenClientFactory.factor(userId, appToken);
	}

	@Override
	public String create(String recordType, String json) {
		RestClient restClient = setUpRestClientWithAuthToken();
		ExtendedRestResponse response = restClient.createRecordFromJson(recordType, json);
		possiblyThrowErrorIfNotCreated(recordType, response);
		return response.responseText;
	}

	private RestClient setUpRestClientWithAuthToken() {
		String authToken = appTokenClient.getAuthToken();
		return restClientFactory.factorUsingAuthToken(authToken);
	}

	private void possiblyThrowErrorIfNotCreated(String recordType, ExtendedRestResponse response) {
		if (statusIsNotCreated(response.statusCode)) {
			String url = restClientFactory.getBaseUrl();
			throw new CoraClientException("Could not create record of type: " + recordType + " on "
					+ SERVER_USING_URL + url + RETURNED_ERROR_WAS + response.responseText);
		}
	}

	private boolean statusIsNotCreated(int statusCode) {
		return statusCode != CREATED;
	}

	@Override
	public String create(String recordType, ClientDataGroup dataGroup) {
		String json = convertDataGroupToJson(dataGroup);
		return create(recordType, json);
	}

	private String convertDataGroupToJson(ClientDataGroup dataGroup) {
		DataToJsonConverter converter = createConverter(dataGroup);
		return converter.toJson();
	}

	private DataToJsonConverter createConverter(ClientDataGroup dataGroup) {
		JsonBuilderFactory factory = new OrgJsonBuilderFactoryAdapter();
		return dataToJsonConverterFactory.createForClientDataElement(factory, dataGroup);
	}

	@Override
	public String read(String recordType, String recordId) {
		RestClient restClient = setUpRestClientWithAuthToken();
		RestResponse response = restClient.readRecordAsJson(recordType, recordId);
		possiblyThrowErrorForReadRecordTypeAndIdIfNotOk(response, recordType, recordId);
		return response.responseText;
	}

	private void possiblyThrowErrorForReadRecordTypeAndIdIfNotOk(RestResponse response,
			String recordType, String recordId) {
		possiblyThrowErrorIfNotOk(response,
				"Could not read record of type: " + recordType + AND_ID + recordId + FROM);
	}

	@Override
	public ClientDataRecord readAsDataRecord(String recordType, String recordId) {
		String readJson = read(recordType, recordId);
		JsonObject readJsonObject = createJsonObjectFromResponseText(readJson);
		return convertToDataRecord(readJsonObject);
	}

	private JsonObject createJsonObjectFromResponseText(String responseText) {
		JsonParser jsonParser = new OrgJsonParser();
		JsonValue jsonValue = jsonParser.parseString(responseText);
		return (JsonObject) jsonValue;
	}

	private ClientDataRecord convertToDataRecord(JsonObject readJsonObject) {
		JsonToDataRecordConverterImp recordConverter = JsonToDataRecordConverterImp
				.usingConverterFactory(jsonToDataConverterFactory);
		return (ClientDataRecord) recordConverter.toInstance(readJsonObject);
	}

	@Override
	public String update(String recordType, String recordId, String json) {
		RestClient restClient = setUpRestClientWithAuthToken();
		RestResponse response = restClient.updateRecordFromJson(recordType, recordId, json);
		possiblyThrowErrorForUpdateRecordTypeAndIdIfNotOk(response, recordType, recordId);
		return response.responseText;
	}

	private void possiblyThrowErrorForUpdateRecordTypeAndIdIfNotOk(RestResponse response,
			String recordType, String recordId) {
		possiblyThrowErrorIfNotOk(response,
				"Could not update record of type: " + recordType + AND_ID + recordId + " on ");
	}

	private void possiblyThrowErrorIfNotOk(RestResponse response, String messageStart) {
		if (statusIsNotOk(response.statusCode)) {
			String url = restClientFactory.getBaseUrl();
			throw new CoraClientException(messageStart + SERVER_USING_URL + url + RETURNED_ERROR_WAS
					+ response.responseText);
		}
	}

	private boolean statusIsNotOk(int statusCode) {
		return statusCode != OK;
	}

	@Override
	public String update(String recordType, String recordId, ClientDataGroup dataGroup) {
		String json = convertDataGroupToJsonWithoutLinks(dataGroup);
		return update(recordType, recordId, json);
	}

	private String convertDataGroupToJsonWithoutLinks(ClientDataGroup dataGroup) {
		DataToJsonConverter converter = createConverterWithoutLinks(dataGroup);
		return converter.toJson();
	}

	private DataToJsonConverter createConverterWithoutLinks(ClientDataGroup dataGroup) {
		JsonBuilderFactory factory = new OrgJsonBuilderFactoryAdapter();
		return dataToJsonConverterFactory.createForClientDataElementIncludingActionLinks(factory,
				dataGroup, false);
	}

	@Override
	public String delete(String recordType, String recordId) {
		RestClient restClient = setUpRestClientWithAuthToken();
		RestResponse response = restClient.deleteRecord(recordType, recordId);
		possiblyThrowErrorForDeleteIfNotOk(response, recordType, recordId);
		return response.responseText;
	}

	private void possiblyThrowErrorForDeleteIfNotOk(RestResponse response, String recordType,
			String recordId) {
		possiblyThrowErrorIfNotOk(response,
				"Could not delete record of type: " + recordType + AND_ID + recordId + FROM);
	}

	@Override
	public String readList(String recordType) {
		RestClient restClient = setUpRestClientWithAuthToken();
		RestResponse response = restClient.readRecordListAsJson(recordType);
		possiblyThrowErrorForReadList(recordType, response);
		return response.responseText;
	}

	private void possiblyThrowErrorForReadList(String recordType, RestResponse response) {
		if (statusIsNotOk(response.statusCode)) {
			String url = restClientFactory.getBaseUrl();
			throw new CoraClientException("Could not read records of type: " + recordType + FROM
					+ SERVER_USING_URL + url + RETURNED_ERROR_WAS + response.responseText);
		}
	}

	@Override
	public String readIncomingLinks(String recordType, String recordId) {
		RestClient restClient = setUpRestClientWithAuthToken();
		RestResponse response = restClient.readIncomingLinksAsJson(recordType, recordId);
		possiblyThrowErrorForIncomingLinksIfNotOk(response, recordType, recordId);
		return response.responseText;
	}

	private void possiblyThrowErrorForIncomingLinksIfNotOk(RestResponse response, String recordType,
			String recordId) {
		possiblyThrowErrorIfNotOk(response,
				"Could not read incoming links of type: " + recordType + AND_ID + recordId + FROM);
	}

	public AppTokenClientFactory getAppTokenClientFactory() {
		// needed for test
		return appTokenClientFactory;
	}

	public RestClientFactory getRestClientFactory() {
		// needed for test
		return restClientFactory;
	}

	public String getUserId() {
		// needed for test
		return userId;
	}

	public String getAppToken() {
		// needed for test
		return appToken;
	}

	public DataToJsonConverterFactory getDataToJsonConverterFactory() {
		// needed for test
		return dataToJsonConverterFactory;
	}

	public JsonToDataConverterFactory getJsonToDataConverterFactory() {
		// needed for test
		return jsonToDataConverterFactory;
	}

}
