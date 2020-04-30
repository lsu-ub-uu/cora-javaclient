/*
 * Copyright 2018 Uppsala University Library
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
package se.uu.ub.cora.javaclient.doubles;

import se.uu.ub.cora.javaclient.rest.ExtendedRestResponse;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestResponse;

public class RestClientSpy implements RestClient {

	public String returnedAnswer = "Answer from CoraRestClientSpy ";
	public static final String THIS_RECORD_TYPE_TRIGGERS_AN_ERROR = "thisRecordTypeTriggersAnError";
	public String recordType;
	public String recordId;
	public String json;
	public String methodCalled;
	public RestResponse restResponse;
	public ExtendedRestResponse extendedRestResponse;
	private int statusCode = 200;

	@Override
	public RestResponse readRecordAsJson(String recordType, String recordId) {
		if (THIS_RECORD_TYPE_TRIGGERS_AN_ERROR.equals(recordType)) {
			// throw new CoraClientException("Error from RestClientSpy");
			statusCode = 500;
		}
		this.recordType = recordType;
		this.recordId = recordId;
		methodCalled = "read";
		if ("someRecordTypeToBeReturnedAsDataGroup".equals(recordType)) {
			String jsonToReturn = "{\"record\":{\"data\":{\"children\":[{\"name\":\"nameInData\",\"value\":\"historicCountry\"},{\"children\":[{\"name\":\"id\",\"value\":\"historicCountryCollection\"},{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"recordType\"},{\"name\":\"linkedRecordId\",\"value\":\"metadataItemCollection\"}],\"name\":\"type\"}],\"name\":\"recordInfo\"},{\"children\":[{\"repeatId\":\"0\",\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"genericCollectionItem\"},{\"name\":\"linkedRecordId\",\"value\":\"gaulHistoricCountryItem\"}],\"name\":\"ref\"},{\"repeatId\":\"1\",\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"genericCollectionItem\"},{\"name\":\"linkedRecordId\",\"value\":\"britainHistoricCountryItem\"}],\"name\":\"ref\"}],\"name\":\"collectionItemReferences\"}],\"name\":\"metadata\",\"attributes\":{\"type\":\"itemCollection\"}},\"actionLinks\":{\"read\":{\"requestMethod\":\"GET\",\"rel\":\"read\",\"url\":\"https://cora.test.alvin-portal.org/alvin/rest/record/metadataItemCollection/historicCountryCollection\",\"accept\":\"application/vnd.uub.record+json\"}}}}";
			restResponse = new RestResponse(200, jsonToReturn);
			return restResponse;
		}
		restResponse = new RestResponse(statusCode, returnedAnswer + methodCalled);
		return restResponse;
	}

	@Override
	public ExtendedRestResponse createRecordFromJson(String recordType, String json) {
		statusCode = 201;
		if (THIS_RECORD_TYPE_TRIGGERS_AN_ERROR.equals(recordType)) {
			statusCode = 500;
		}
		this.recordType = recordType;
		this.json = json;
		methodCalled = "create";
		RestResponse restResponse = new RestResponse(statusCode, returnedAnswer + methodCalled);
		extendedRestResponse = new ExtendedRestResponse(restResponse);
		return extendedRestResponse;
	}

	@Override
	public RestResponse updateRecordFromJson(String recordType, String recordId, String json) {
		if (THIS_RECORD_TYPE_TRIGGERS_AN_ERROR.equals(recordType)) {
			statusCode = 500;
		}
		this.recordType = recordType;
		this.recordId = recordId;
		this.json = json;
		methodCalled = "update";
		restResponse = new RestResponse(statusCode, returnedAnswer + methodCalled);
		return restResponse;
	}

	@Override
	public RestResponse deleteRecord(String recordType, String recordId) {
		if (THIS_RECORD_TYPE_TRIGGERS_AN_ERROR.equals(recordType)) {
			statusCode = 500;
		}
		this.recordType = recordType;
		this.recordId = recordId;
		methodCalled = "delete";
		restResponse = new RestResponse(statusCode, returnedAnswer + methodCalled);
		return restResponse;
	}

	@Override
	public RestResponse readRecordListAsJson(String recordType) {
		if (THIS_RECORD_TYPE_TRIGGERS_AN_ERROR.equals(recordType)) {
			statusCode = 500;
		}
		this.recordType = recordType;
		methodCalled = "readList";
		restResponse = new RestResponse(statusCode, returnedAnswer + methodCalled);
		return restResponse;
	}

	@Override
	public RestResponse readIncomingLinksAsJson(String recordType, String recordId) {
		if (THIS_RECORD_TYPE_TRIGGERS_AN_ERROR.equals(recordType)) {
			statusCode = 500;
		}
		this.recordType = recordType;
		this.recordId = recordId;
		methodCalled = "readincomingLinks";
		restResponse = new RestResponse(statusCode, returnedAnswer + methodCalled);
		return restResponse;
	}

	@Override
	public RestResponse readRecordListWithFilterAsJson(String recordType, String filter) {
		if (THIS_RECORD_TYPE_TRIGGERS_AN_ERROR.equals(recordType)) {
			statusCode = 500;
		}
		this.recordType = recordType;
		methodCalled = "readListWithFilter";
		restResponse = new RestResponse(statusCode, returnedAnswer + methodCalled);
		return restResponse;
	}

}
