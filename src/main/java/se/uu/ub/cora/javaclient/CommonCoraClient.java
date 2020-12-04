/*
 * Copyright 2020 Uppsala University Library
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
import se.uu.ub.cora.clientdata.converter.javatojson.DataToJsonConverter;
import se.uu.ub.cora.clientdata.converter.javatojson.DataToJsonConverterFactory;
import se.uu.ub.cora.javaclient.cora.CoraClientException;
import se.uu.ub.cora.javaclient.rest.ExtendedRestResponse;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.json.builder.JsonBuilderFactory;
import se.uu.ub.cora.json.builder.org.OrgJsonBuilderFactoryAdapter;

public class CommonCoraClient {

	private static final int CREATED = 201;
	private static final String RETURNED_ERROR_WAS = ". Returned error was: ";
	private static final String SERVER_USING_URL = "server using base url: ";
	protected DataToJsonConverterFactory dataToJsonConverterFactory;

	protected String createRecord(RestClient restClient, String recordType, String json) {
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

	protected String createRecord(RestClient restClient, String recordType,
			ClientDataGroup dataGroup) {
		String json = convertDataGroupToJson(dataGroup);
		return createRecord(restClient, recordType, json);
	}

	private String convertDataGroupToJson(ClientDataGroup dataGroup) {
		DataToJsonConverter converter = createConverter(dataGroup);
		return converter.toJson();
	}

	private DataToJsonConverter createConverter(ClientDataGroup dataGroup) {
		JsonBuilderFactory factory = new OrgJsonBuilderFactoryAdapter();
		return dataToJsonConverterFactory.createForClientDataElement(factory, dataGroup);
	}

}
