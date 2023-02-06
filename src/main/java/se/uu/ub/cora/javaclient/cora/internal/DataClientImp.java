/*
 * Copyright 2018, 2019, 2020, 2021 Uppsala University Library
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

import java.util.List;

import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverter;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterFactory;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterProvider;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterProvider;
import se.uu.ub.cora.javaclient.cora.DataClient;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestClientFactory;
import se.uu.ub.cora.javaclient.rest.RestResponse;

public class DataClientImp implements DataClient {

	protected ClientDataToJsonConverterFactory dataToJsonConverterFactory;
	private RestClientFactory restClientFactory;
	// private AppTokenClient appTokenClient;
	// private AppTokenClientFactory appTokenClientFactory;
	private String userId;
	private String appToken;
	private RestClient restClient;

	// public CoraClientImp(ApptokenBasedClientDependencies coraClientDependencies) {
	// this.appTokenClientFactory = coraClientDependencies.appTokenClientFactory;
	// this.restClientFactory = coraClientDependencies.restClientFactory;
	// this.dataToJsonConverterFactory = coraClientDependencies.dataToJsonConverterFactory;
	// this.jsonToDataConverterFactory = coraClientDependencies.jsonToDataConverterFactory;
	// this.userId = coraClientDependencies.userId;
	// this.appToken = coraClientDependencies.appToken;
	// appTokenClient = appTokenClientFactory.factor(userId, appToken);
	// }

	public DataClientImp(RestClient restClient) {
		this.restClient = restClient;
		dataToJsonConverterFactory = ClientDataToJsonConverterProvider.createImplementingFactory();
	}

	@Override
	public ClientDataRecord create(String recordType, ClientDataGroup dataGroup) {
		ClientDataToJsonConverter converterToJson = createConverter(dataGroup);
		String json = converterToJson.toJson();
		RestResponse createRecordFromJson = restClient.createRecordFromJson(recordType, json);
		JsonToClientDataConverter converterToData = JsonToClientDataConverterProvider
				.getConverterUsingJsonString(createRecordFromJson.responseText());
		return (ClientDataRecord) converterToData.toInstance();
	}

	protected ClientDataToJsonConverter createConverter(ClientDataGroup dataGroup) {
		return dataToJsonConverterFactory.factorUsingConvertible(dataGroup);
	}
	// @Override
	// public String create(String recordType, String json) {
	// return setUpRestClientAndCreateRecord(recordType, json);
	// }

	// private String setUpRestClientAndCreateRecord(String recordType, String json) {
	// // RestClient restClient = setUpRestClientWithAuthToken();
	// RestResponse response = restClient.createRecordFromJson(recordType, json);
	// possiblyThrowErrorIfNotCreated(restClient, recordType, response);
	// return response.responseText();
	// }

	private RestClient setUpRestClientWithAuthToken() {
		// // String authToken = appTokenClient.getAuthToken();
		// // return restClientFactory.factorUsingAuthToken(authToken);
		return null;
	}

	// @Override
	// public String read(String recordType, String recordId) {
	// // RestClient restClient = setUpRestClientWithAuthToken();
	// return read(restClient, recordType, recordId);
	// }

	@Override
	public ClientDataRecord readAsDataRecord(String recordType, String recordId) {
		RestClient restClient = setUpRestClientWithAuthToken();
		return readAsDataRecord(restClient, recordType, recordId);
	}

	// @Override
	// public String update(String recordType, String recordId, String json) {
	// RestClient restClient = setUpRestClientWithAuthToken();
	// return update(restClient, recordType, recordId, json);
	// }

	@Override
	public ClientDataRecord update(String recordType, String recordId, ClientDataGroup dataGroup) {
		RestClient restClient = setUpRestClientWithAuthToken();
		return update(restClient, recordType, recordId, dataGroup);
	}

	@Override
	public void delete(String recordType, String recordId) {
		RestClient restClient = setUpRestClientWithAuthToken();
		return deleteRecord(restClient, recordType, recordId);
	}

	// @Override
	// public String readList(String recordType) {
	// RestClient restClient = setUpRestClientWithAuthToken();
	// return readList(restClient, recordType);
	// }

	@Override
	public List<ClientDataRecord> readListAsDataRecords(String recordType) {
		RestClient restClient = setUpRestClientWithAuthToken();
		return readListAsDataRecords(restClient, recordType);
	}

	@Override
	public String readIncomingLinks(String recordType, String recordId) {
		RestClient restClient = setUpRestClientWithAuthToken();
		return readIncomingLinks(restClient, recordType, recordId);
	}

	@Override
	public String indexData(ClientDataRecord clientDataRecord) {
		RestClient restClient = setUpRestClientWithAuthToken();
		return indexData(restClient, clientDataRecord, true);
	}

	@Override
	public String indexData(String recordType, String recordId) {
		RestClient restClient = setUpRestClientWithAuthToken();
		ClientDataRecord clientDataRecord = readAsDataRecord(restClient, recordType, recordId);
		return indexData(restClient, clientDataRecord, true);
	}

	@Override
	public String removeFromIndex(String recordType, String recordId) {
		ClientDataGroup workOrder = createWorkOrderForRemoveFromIndex(recordType, recordId);
		return create("workOrder", workOrder);
	}

	// public AppTokenClientFactory getAppTokenClientFactory() {
	// // needed for test
	// return appTokenClientFactory;
	// }

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

	@Override
	public String indexDataWithoutExplicitCommit(String recordType, String recordId) {
		RestClient restClient = setUpRestClientWithAuthToken();
		ClientDataRecord clientDataRecord = readAsDataRecord(restClient, recordType, recordId);
		return indexData(restClient, clientDataRecord, false);
	}

	@Override
	public String indexRecordsOfType(String recordType, String indexSettings) {
		RestClient restClient = setUpRestClientWithAuthToken();
		return indexRecordList(restClient, recordType, indexSettings);
	}

	public RestClient onlyForTestGetRestClient() {
		return restClient;
	}

	// public ClientDataToJsonConverterFactory onlyForTestGetDataToJsonConverterFactory() {
	// return dataToJsonConverterFactory;
	// }

}