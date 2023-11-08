/*
 * Copyright 2023 Uppsala University Library
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
package se.uu.ub.cora.javaclient.rest;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class RestClientSpy implements RestClient {

	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public RestClientSpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("readRecordAsJson", () -> createRestResponse());
		MRV.setDefaultReturnValuesSupplier("createRecordFromJson", () -> createRestResponse());
		MRV.setDefaultReturnValuesSupplier("updateRecordFromJson", () -> createRestResponse());
		MRV.setDefaultReturnValuesSupplier("deleteRecord", () -> createRestResponse());
		MRV.setDefaultReturnValuesSupplier("readRecordListAsJson", () -> createRestResponse());
		MRV.setDefaultReturnValuesSupplier("readRecordListWithFilterAsJson",
				() -> createRestResponse());
		MRV.setDefaultReturnValuesSupplier("readIncomingLinksAsJson", () -> createRestResponse());
		MRV.setDefaultReturnValuesSupplier("batchIndexWithFilterAsJson",
				() -> createRestResponse());
	}

	private RestResponse createRestResponse() {
		return new RestResponse(200, "", Optional.empty());
	}

	@Override
	public RestResponse readRecordAsJson(String recordType, String recordId) {
		return (RestResponse) MCR.addCallAndReturnFromMRV("recordType", recordType, "recordId",
				recordId);
	}

	@Override
	public RestResponse createRecordFromJson(String recordType, String json) {
		return (RestResponse) MCR.addCallAndReturnFromMRV("recordType", recordType, "json", json);
	}

	@Override
	public RestResponse updateRecordFromJson(String recordType, String recordId, String json) {
		return (RestResponse) MCR.addCallAndReturnFromMRV("recordType", recordType, "recordId",
				recordId, "json", json);
	}

	@Override
	public RestResponse deleteRecord(String recordType, String recordId) {
		return (RestResponse) MCR.addCallAndReturnFromMRV("recordType", recordType, "recordId",
				recordId);
	}

	@Override
	public RestResponse readRecordListAsJson(String recordType) {
		return (RestResponse) MCR.addCallAndReturnFromMRV("recordType", recordType);
	}

	@Override
	public RestResponse readIncomingLinksAsJson(String recordType, String recordId) {
		return (RestResponse) MCR.addCallAndReturnFromMRV("recordType", recordType, "recordId",
				recordId);
	}

	@Override
	public RestResponse readRecordListWithFilterAsJson(String recordType, String filter)
			throws UnsupportedEncodingException {
		return (RestResponse) MCR.addCallAndReturnFromMRV("recordType", recordType, "filter",
				filter);
	}

	@Override
	public RestResponse batchIndexWithFilterAsJson(String recordType, String indexSettingsAsJson) {
		return (RestResponse) MCR.addCallAndReturnFromMRV("recordType", recordType,
				"indexSettingsAsJson", indexSettingsAsJson);
	}

}
