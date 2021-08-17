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

package se.uu.ub.cora.javaclient.cora.http;

import java.util.List;

import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.converter.javatojson.DataToJsonConverterFactory;
import se.uu.ub.cora.clientdata.converter.jsontojava.JsonToDataConverterFactory;
import se.uu.ub.cora.javaclient.cora.CoraClient;
import se.uu.ub.cora.javaclient.rest.RestClient;

public class AuthtokenBasedClient extends CommonCoraClient implements CoraClient {

	static final String FROM = " from ";
	static final String AND_ID = " and id: ";
	static final String RETURNED_ERROR_WAS = ". Returned error was: ";
	static final String SERVER_USING_URL = "server using base url: ";

	RestClient restClient;

	public AuthtokenBasedClient(RestClient restClient,
			DataToJsonConverterFactory dataToJsonConverterFactory,
			JsonToDataConverterFactory jsonToDataConverterFactory) {
		this.restClient = restClient;
		this.dataToJsonConverterFactory = dataToJsonConverterFactory;
		this.jsonToDataConverterFactory = jsonToDataConverterFactory;
	}

	@Override
	public String create(String recordType, String json) {
		return create(restClient, recordType, json);
	}

	@Override
	public String create(String recordType, ClientDataGroup dataGroup) {
		return create(restClient, recordType, dataGroup);
	}

	@Override
	public String read(String recordType, String recordId) {
		return read(restClient, recordType, recordId);
	}

	@Override
	public ClientDataRecord readAsDataRecord(String recordType, String recordId) {
		return readAsDataRecord(restClient, recordType, recordId);
	}

	@Override
	public String update(String recordType, String recordId, String json) {
		return update(restClient, recordType, recordId, json);
	}

	@Override
	public String update(String recordType, String recordId, ClientDataGroup dataGroup) {
		return update(restClient, recordType, recordId, dataGroup);
	}

	@Override
	public String delete(String recordType, String recordId) {
		return deleteRecord(restClient, recordType, recordId);
	}

	@Override
	public String readList(String recordType) {
		return readList(restClient, recordType);
	}

	@Override
	public List<ClientDataRecord> readListAsDataRecords(String recordType) {
		return readListAsDataRecords(restClient, recordType);
	}

	@Override
	public String readIncomingLinks(String recordType, String recordId) {
		return readIncomingLinks(restClient, recordType, recordId);
	}

	@Override
	public String indexData(ClientDataRecord clientDataRecord) {
		return indexData(restClient, clientDataRecord);
	}

	@Override
	public String indexData(String recordType, String recordId) {
		ClientDataRecord dataRecord = readAsDataRecord(recordType, recordId);
		return indexData(dataRecord);
	}

	public RestClient getRestClient() {
		return restClient;
	}

	@Override
	public String removeFromIndex(String recordType, String recordId) {
		ClientDataGroup workOrder = createWorkOrderForRemoveFromIndex(recordType, recordId);
		return create("workOrder", workOrder);
	}

}
