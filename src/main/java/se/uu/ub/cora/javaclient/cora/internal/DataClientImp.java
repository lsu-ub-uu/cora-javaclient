/*
 * Copyright 2018, 2019, 2020, 2021, 2023 Uppsala University Library
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

import java.text.MessageFormat;

import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataList;
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
import se.uu.ub.cora.javaclient.rest.RestResponse;

public class DataClientImp implements DataClient {

	private static final int RESPONSE_CODE_OK = 200;
	private static final int RESPONSE_CODE_CREATED = 201;
	private static final String ERROR_MESSAGE_CREATE = "Could not create record of type: {0}. "
			+ "Returned error was: {1}";
	private static final String ERROR_MESSAGE_READ = "Could not read record of type: {0} and id: {1}. "
			+ "Returned error was: {2}";
	private static final String ERROR_MESSAGE_READ_LIST = "Could not list records of type: {0}. "
			+ "Returned error was: {1}";
	private static final String ERROR_MESSAGE_UPDATE = "Could not update record of type: {0} and id: {1}. "
			+ "Returned error was: {2}";
	protected ClientDataToJsonConverterFactory dataToJsonConverterFactory;
	private RestClient restClient;

	public DataClientImp(RestClient restClient) {
		this.restClient = restClient;
		dataToJsonConverterFactory = ClientDataToJsonConverterProvider.createImplementingFactory();
	}

	@Override
	public ClientDataRecord create(String recordType, ClientDataRecordGroup dataRecordGroup) {
		try {
			return tryToCreate(recordType, dataRecordGroup);
		} catch (Exception e) {
			rethrowIfClientException(e);
			throw createErrorUsingRecordTypeAndMessage(recordType, e.getMessage());
		}
	}

	private ClientDataRecord tryToCreate(String recordType, ClientDataRecordGroup dataRecordGroup) {
		String json = convertToJson(dataRecordGroup);
		RestResponse response = restClient.createRecordFromJson(recordType, json);
		throwErrorIfNotCreated(recordType, response);
		return (ClientDataRecord) convertToData(response);
	}

	private void throwErrorIfNotCreated(String recordType, RestResponse response) {
		if (response.responseCode() != RESPONSE_CODE_CREATED) {
			throw createErrorUsingRecordTypeAndMessage(recordType, response.responseText());
		}
	}

	private CoraClientException createErrorUsingRecordTypeAndMessage(String recordType,
			String message) {
		return new CoraClientException(
				MessageFormat.format(ERROR_MESSAGE_CREATE, recordType, message));
	}

	private void rethrowIfClientException(Exception e) {
		if (e instanceof CoraClientException) {
			throw (CoraClientException) e;
		}
	}

	private String convertToJson(ClientDataRecordGroup dataRecordGroup) {
		ClientDataToJsonConverter converterToJson = createConverterToJson(dataRecordGroup);
		return converterToJson.toJson();
	}

	protected ClientDataToJsonConverter createConverterToJson(
			ClientDataRecordGroup dataRecordGroup) {
		return dataToJsonConverterFactory.factorUsingConvertible(dataRecordGroup);
	}

	private ClientConvertible convertToData(RestResponse createRecordFromJson) {
		JsonToClientDataConverter converterToData = JsonToClientDataConverterProvider
				.getConverterUsingJsonString(createRecordFromJson.responseText());
		return converterToData.toInstance();
	}

	@Override
	public ClientDataRecord read(String recordType, String recordId) {
		try {
			return tryToRead(recordType, recordId);
		} catch (Exception e) {
			rethrowIfClientException(e);
			throw readErrorUsingRecordTypeAndMessage(recordType, recordId, e.getMessage());
		}
	}

	private ClientDataRecord tryToRead(String recordType, String recordId) {
		RestResponse response = restClient.readRecordAsJson(recordType, recordId);
		throwErrorIfNotRead(recordType, recordId, response);
		return (ClientDataRecord) convertToData(response);
	}

	private void throwErrorIfNotRead(String recordType, String recordId, RestResponse response) {
		if (response.responseCode() != RESPONSE_CODE_OK) {
			throw readErrorUsingRecordTypeAndMessage(recordType, recordId, response.responseText());
		}
	}

	private CoraClientException readErrorUsingRecordTypeAndMessage(String recordType,
			String recordId, String message) {
		return new CoraClientException(
				MessageFormat.format(ERROR_MESSAGE_READ, recordType, recordId, message));
	}

	@Override
	public ClientDataList readList(String recordType) {
		try {
			return tryToReadList(recordType);
		} catch (Exception e) {
			rethrowIfClientException(e);
			throw readListErrorUsingRecordTypeAndMessage(recordType, e.getMessage());
		}
	}

	private ClientDataList tryToReadList(String recordType) {
		RestResponse response = restClient.readRecordListAsJson(recordType);
		throwErrorIfNotReadList(recordType, response);
		return (ClientDataList) convertToData(response);
	}

	private void throwErrorIfNotReadList(String recordType, RestResponse response) {
		if (response.responseCode() != RESPONSE_CODE_OK) {
			throw readListErrorUsingRecordTypeAndMessage(recordType, response.responseText());
		}
	}

	private CoraClientException readListErrorUsingRecordTypeAndMessage(String recordType,
			String message) {
		return new CoraClientException(
				MessageFormat.format(ERROR_MESSAGE_READ_LIST, recordType, message));
	}

	@Override
	public ClientDataRecord update(String recordType, String recordId,
			ClientDataRecordGroup dataRecordGroup) {
		String json = convertToJson(dataRecordGroup);
		RestResponse response = restClient.updateRecordFromJson(recordType, recordId, json);
		throwErrorIfNotUpdate(recordType, recordId, response);
		return (ClientDataRecord) convertToData(response);
	}

	private void throwErrorIfNotUpdate(String recordType, String recordId, RestResponse response) {
		if (response.responseCode() != RESPONSE_CODE_OK) {
			throw updateErrorUsingRecordTypeAndMessage(recordType, recordId,
					response.responseText());
		}
	}

	private CoraClientException updateErrorUsingRecordTypeAndMessage(String recordType,
			String recordId, String message) {
		return new CoraClientException(
				MessageFormat.format(ERROR_MESSAGE_UPDATE, recordType, recordId, message));
	}

	@Override
	public void delete(String recordType, String recordId) {
		RestClient restClient = setUpRestClientWithAuthToken();
		return deleteRecord(restClient, recordType, recordId);
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

}
