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

import se.uu.ub.cora.javaclient.cora.CoraClientException;
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

	@Override
	public RestResponse readRecordAsJson(String recordType, String recordId) {
		if (THIS_RECORD_TYPE_TRIGGERS_AN_ERROR.equals(recordType)) {
			throw new CoraClientException("Error from RestClientSpy");
		}
		this.recordType = recordType;
		this.recordId = recordId;
		methodCalled = "read";
		if ("someRecordTypeToBeReturnedAsDataGroup".equals(recordType)) {
			String jsonToReturn = "{\"record\":{\"data\":{\"children\":[{\"name\":\"nameInData\",\"value\":\"historicCountry\"},{\"children\":[{\"name\":\"id\",\"value\":\"historicCountryCollection\"},{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"recordType\"},{\"name\":\"linkedRecordId\",\"value\":\"metadataItemCollection\"}],\"name\":\"type\"}],\"name\":\"recordInfo\"},{\"children\":[{\"repeatId\":\"0\",\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"genericCollectionItem\"},{\"name\":\"linkedRecordId\",\"value\":\"gaulHistoricCountryItem\"}],\"name\":\"ref\"},{\"repeatId\":\"1\",\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"genericCollectionItem\"},{\"name\":\"linkedRecordId\",\"value\":\"britainHistoricCountryItem\"}],\"name\":\"ref\"}],\"name\":\"collectionItemReferences\"}],\"name\":\"metadata\",\"attributes\":{\"type\":\"itemCollection\"}},\"actionLinks\":{\"read\":{\"requestMethod\":\"GET\",\"rel\":\"read\",\"url\":\"https://cora.test.alvin-portal.org/alvin/rest/record/metadataItemCollection/historicCountryCollection\",\"accept\":\"application/vnd.uub.record+json\"}}}}";
			return new RestResponse(200, jsonToReturn);
		}
		return new RestResponse(200, returnedAnswer + methodCalled);
	}

	@Override
	public ExtendedRestResponse createRecordFromJson(String recordType, String json) {
		if (THIS_RECORD_TYPE_TRIGGERS_AN_ERROR.equals(recordType)) {
			throw new CoraClientException("Error from RestClientSpy");
		}
		this.recordType = recordType;
		this.json = json;
		methodCalled = "create";
		RestResponse restResponse = new RestResponse(201, returnedAnswer + methodCalled);
		return new ExtendedRestResponse(restResponse);
	}

	@Override
	public String updateRecordFromJson(String recordType, String recordId, String json) {
		if (THIS_RECORD_TYPE_TRIGGERS_AN_ERROR.equals(recordType)) {
			throw new CoraClientException("Error from RestClientSpy");
		}
		this.recordType = recordType;
		this.recordId = recordId;
		this.json = json;
		methodCalled = "update";
		return returnedAnswer + methodCalled;
	}

	@Override
	public String deleteRecord(String recordType, String recordId) {
		if (THIS_RECORD_TYPE_TRIGGERS_AN_ERROR.equals(recordType)) {
			throw new CoraClientException("Error from RestClientSpy");
		}
		this.recordType = recordType;
		this.recordId = recordId;
		methodCalled = "delete";
		return returnedAnswer + methodCalled;
	}

	@Override
	public RestResponse readRecordListAsJson(String recordType) {
		if (THIS_RECORD_TYPE_TRIGGERS_AN_ERROR.equals(recordType)) {
			throw new CoraClientException("Error from RestClientSpy");
		}
		this.recordType = recordType;
		methodCalled = "readList";
		return new RestResponse(200, returnedAnswer + methodCalled);
	}

	@Override
	public String readIncomingLinksAsJson(String recordType, String recordId) {
		if (THIS_RECORD_TYPE_TRIGGERS_AN_ERROR.equals(recordType)) {
			throw new CoraClientException("Error from RestClientSpy");
		}
		this.recordType = recordType;
		this.recordId = recordId;
		methodCalled = "readincomingLinks";
		return returnedAnswer + methodCalled;
	}

	@Override
	public RestResponse readRecordListWithFilterAsJson(String recordType, String filter) {
		// TODO Auto-generated method stub
		return null;
	}

}
