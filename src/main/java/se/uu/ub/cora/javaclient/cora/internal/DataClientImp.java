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
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverter;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterFactory;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterProvider;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterProvider;
import se.uu.ub.cora.javaclient.cora.CoraClientException;
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
	public ClientDataRecord create(String recordType, ClientDataRecordGroup dataRecordGroup) {
		String json = convertToJson(dataRecordGroup);
		RestResponse createRecordFromJson = restClient.createRecordFromJson(recordType, json);
		return convertToData(createRecordFromJson);
	}

	private String convertToJson(ClientDataRecordGroup dataRecordGroup) {
		ClientDataToJsonConverter converterToJson = createConverterToJson(dataRecordGroup);
		return converterToJson.toJson();
	}

	protected ClientDataToJsonConverter createConverterToJson(
			ClientDataRecordGroup dataRecordGroup) {
		return dataToJsonConverterFactory.factorUsingConvertible(dataRecordGroup);
	}

	private ClientDataRecord convertToData(RestResponse createRecordFromJson) {
		JsonToClientDataConverter converterToData = JsonToClientDataConverterProvider
				.getConverterUsingJsonString(createRecordFromJson.responseText());
		return (ClientDataRecord) converterToData.toInstance();
	}

	@Override
	public ClientDataRecord read(String recordType, String recordId) {
		RestResponse response = restClient.readRecordAsJson(recordType, recordId);
		if (response.responseCode() == 200) {
			return convertToData(response);
		}
		throw new CoraClientException("Blabla");
	}

	// @Override
	// public String update(String recordType, String recordId, String json) {
	// RestClient restClient = setUpRestClientWithAuthToken();
	// return update(restClient, recordType, recordId, json);
	// }

	@Override
	public ClientDataRecord update(String recordType, String recordId,
			ClientDataRecordGroup dataRecordGroup) {
		RestClient restClient = setUpRestClientWithAuthToken();
		return update(restClient, recordType, recordId, dataRecordGroup);
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
