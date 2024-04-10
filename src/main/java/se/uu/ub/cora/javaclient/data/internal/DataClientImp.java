/*
 * Copyright 2018, 2019, 2020, 2021, 2023 Uppsala University Library
 * Copyright 2023 Olov McKie
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

import java.text.MessageFormat;

import se.uu.ub.cora.clientdata.ClientConvertible;
import se.uu.ub.cora.clientdata.ClientDataList;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverter;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterFactory;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterProvider;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterProvider;
import se.uu.ub.cora.javaclient.data.DataClient;
import se.uu.ub.cora.javaclient.data.DataClientException;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestResponse;

public class DataClientImp implements DataClient {

	private static final int RESPONSE_CODE_OK = 200;
	private static final int RESPONSE_CODE_CREATED = 201;
	private static final String ERROR_MESSAGE_CREATE = "Could not create record of type: {0}. "
			+ "Returned error was: {1}";
	private static final String ERROR_MESSAGE_READ = "Could not read record of type: {0} and "
			+ "id: {1}. Returned error was: {2}";
	private static final String ERROR_MESSAGE_READ_LIST = "Could not list records of type: {0}. "
			+ "Returned error was: {1}";
	private static final String ERROR_MESSAGE_READ_INCOMMING_LINKS = "Could not read incomming "
			+ "links for type: {0} and id: {1}. " + "Returned error was: {2}";
	private static final String ERROR_MESSAGE_UPDATE = "Could not update record of type: {0} and "
			+ "id: {1}. Returned error was: {2}";
	private static final String ERROR_MESSAGE_DELETE = "Could not delete record of type: {0} and "
			+ "id: {1}. Returned error was: {2}";
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

	private DataClientException createErrorUsingRecordTypeAndMessage(String recordType,
			String message) {
		return DataClientException.withMessage(MessageFormat.format(ERROR_MESSAGE_CREATE, recordType, message));
	}

	private void rethrowIfClientException(Exception e) {
		if (e instanceof DataClientException) {
			throw (DataClientException) e;
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

	private DataClientException readErrorUsingRecordTypeAndMessage(String recordType,
			String recordId, String message) {
		return DataClientException.withMessage(MessageFormat.format(ERROR_MESSAGE_READ, recordType, recordId, message));
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

	private DataClientException readListErrorUsingRecordTypeAndMessage(String recordType,
			String message) {
		return DataClientException.withMessage(MessageFormat.format(ERROR_MESSAGE_READ_LIST, recordType, message));
	}

	@Override
	public ClientDataRecord update(String recordType, String recordId,
			ClientDataRecordGroup dataRecordGroup) {
		try {
			return tryToUpdate(recordType, recordId, dataRecordGroup);
		} catch (Exception e) {
			rethrowIfClientException(e);
			throw createErrorMessageForUpdate(recordType, recordId, e.getMessage());
		}
	}

	private DataClientException createErrorMessageForUpdate(String recordType, String recordId,
			String message) {
		return DataClientException.withMessage(MessageFormat.format(ERROR_MESSAGE_UPDATE, recordType, recordId, message));
	}

	private ClientDataRecord tryToUpdate(String recordType, String recordId,
			ClientDataRecordGroup dataRecordGroup) {
		String json = convertToJson(dataRecordGroup);
		RestResponse response = restClient.updateRecordFromJson(recordType, recordId, json);
		throwErrorIfNotUpdate(recordType, recordId, response);
		return (ClientDataRecord) convertToData(response);
	}

	private void throwErrorIfNotUpdate(String recordType, String recordId, RestResponse response) {
		if (response.responseCode() != RESPONSE_CODE_OK) {
			throw createErrorMessageForUpdate(recordType, recordId, response.responseText());
		}
	}

	@Override
	public void delete(String recordType, String recordId) {
		RestResponse response = restClient.deleteRecord(recordType, recordId);
		throwErrorIfNotDelete(recordType, recordId, response);
	}

	private void throwErrorIfNotDelete(String recordType, String recordId, RestResponse response) {
		if (response.responseCode() != RESPONSE_CODE_OK) {
			throw createErrorMessageForDelete(recordType, recordId, response.responseText());
		}
	}

	private DataClientException createErrorMessageForDelete(String recordType, String recordId,
			String message) {
		return DataClientException.withMessage(MessageFormat.format(ERROR_MESSAGE_DELETE, recordType, recordId, message));
	}

	@Override
	public ClientDataList readIncomingLinks(String recordType, String recordId) {
		try {
			return tryToReadIncommingLinks(recordType, recordId);
		} catch (Exception e) {
			rethrowIfClientException(e);
			throw createErrorMessageForIncommingLinks(recordType, recordId, e.getMessage());
		}
	}

	private ClientDataList tryToReadIncommingLinks(String recordType, String recordId) {
		RestResponse response = restClient.readIncomingLinksAsJson(recordType, recordId);
		throwErrorIfNotReadIncommingLinks(recordType, recordId, response);
		return (ClientDataList) convertToData(response);
	}

	private void throwErrorIfNotReadIncommingLinks(String recordType, String recordId,
			RestResponse response) {
		if (response.responseCode() != RESPONSE_CODE_OK) {
			throw createErrorMessageForIncommingLinks(recordType, recordId,
					response.responseText());
		}
	}

	private DataClientException createErrorMessageForIncommingLinks(String recordType,
			String recordId, String message) {
		return DataClientException.withMessage(MessageFormat.format(ERROR_MESSAGE_READ_INCOMMING_LINKS,
				recordType, recordId, message));
	}

	public RestClient onlyForTestGetRestClient() {
		return restClient;
	}

}
