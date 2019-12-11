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
import se.uu.ub.cora.clientdata.converter.jsontojava.JsonToDataRecordConverter;
import se.uu.ub.cora.javaclient.apptoken.AppTokenClient;
import se.uu.ub.cora.javaclient.apptoken.AppTokenClientFactory;
import se.uu.ub.cora.javaclient.cora.CoraClient;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestClientFactory;
import se.uu.ub.cora.json.builder.JsonBuilderFactory;
import se.uu.ub.cora.json.builder.org.OrgJsonBuilderFactoryAdapter;
import se.uu.ub.cora.json.parser.JsonObject;
import se.uu.ub.cora.json.parser.JsonParser;
import se.uu.ub.cora.json.parser.JsonValue;
import se.uu.ub.cora.json.parser.org.OrgJsonParser;

public class CoraClientImp implements CoraClient {

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
		return restClient.createRecordFromJson(recordType, json);
	}

	private RestClient setUpRestClientWithAuthToken() {
		String authToken = appTokenClient.getAuthToken();
		return restClientFactory.factorUsingAuthToken(authToken);
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
		return restClient.readRecordAsJson(recordType, recordId);
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
		JsonToDataRecordConverter recordConverter = new JsonToDataRecordConverter(readJsonObject,
				jsonToDataConverterFactory);
		return recordConverter.toInstance();
	}

	@Override
	public String update(String recordType, String recordId, String json) {
		RestClient restClient = setUpRestClientWithAuthToken();
		return restClient.updateRecordFromJson(recordType, recordId, json);
	}

	@Override
	public String delete(String recordType, String recordId) {
		RestClient restClient = setUpRestClientWithAuthToken();
		return restClient.deleteRecord(recordType, recordId);
	}

	@Override
	public String readList(String recordType) {
		RestClient restClient = setUpRestClientWithAuthToken();
		return restClient.readRecordListAsJson(recordType);
	}

	@Override
	public String readIncomingLinks(String recordType, String recordId) {
		RestClient restClient = setUpRestClientWithAuthToken();
		return restClient.readIncomingLinksAsJson(recordType, recordId);
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
}
