/*
 * Copyright 2018, 2020 Uppsala University Library
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
package se.uu.ub.cora.javaclient.rest.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.javaclient.rest.ExtendedRestResponse;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestResponse;

public final class RestClientImp implements RestClient {
	private static final int CREATED = 201;
	private static final int OK = 200;
	private static final String APPLICATION_UUB_RECORD_JSON = "application/vnd.uub.record+json";
	private static final String ACCEPT = "Accept";
	private HttpHandlerFactory httpHandlerFactory;
	private String baseUrl;
	private String authToken;

	public static RestClientImp usingHttpHandlerFactoryAndBaseUrlAndAuthToken(
			HttpHandlerFactory httpHandlerFactory, String baseUrl, String authToken) {
		return new RestClientImp(httpHandlerFactory, baseUrl, authToken);
	}

	private RestClientImp(HttpHandlerFactory httpHandlerFactory, String baseUrl, String authToken) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.baseUrl = baseUrl + "record/";
		this.authToken = authToken;
	}

	@Override
	public RestResponse readRecordAsJson(String recordType, String recordId) {
		HttpHandler httpHandler = createUpHttpHandlerForRead(recordType, recordId);

		int responseCode = httpHandler.getResponseCode();
		String responseText = responseCodeIsOk(responseCode) ? httpHandler.getResponseText()
				: httpHandler.getErrorText();
		return new RestResponse(responseCode, responseText);
	}

	private HttpHandler createUpHttpHandlerForRead(String recordType, String recordId) {
		String url = baseUrl + recordType + "/" + recordId;
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestMethod("GET");
		return httpHandler;
	}

	private boolean responseCodeIsOk(int responseCode) {
		return responseCode == OK;
	}

	private HttpHandler createHttpHandlerWithAuthTokenAndUrl(String url) {
		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		httpHandler.setRequestProperty("authToken", authToken);
		return httpHandler;
	}

	@Override
	public ExtendedRestResponse createRecordFromJson(String recordType, String json) {
		HttpHandler httpHandler = createHttpHandlerForCreate(recordType, json);

		int responseCode = httpHandler.getResponseCode();
		RestResponse restResponse = createRestResponse(httpHandler, responseCode);

		return responseCodeIsCreated(responseCode)
				? createResponseContainingCreatedId(httpHandler, restResponse)
				: new ExtendedRestResponse(restResponse);
	}

	private HttpHandler createHttpHandlerForCreate(String recordType, String json) {
		String url = baseUrl + recordType;
		return setUpHttpHandlerForPost(json, url);
	}

	private RestResponse createRestResponse(HttpHandler httpHandler, int responseCode) {
		String responseText = getResponseOrErrorText(httpHandler, responseCode);
		return new RestResponse(responseCode, responseText);
	}

	private String getResponseOrErrorText(HttpHandler httpHandler, int responseCode) {
		return responseCodeIsCreated(responseCode) ? httpHandler.getResponseText()
				: httpHandler.getErrorText();
	}

	private boolean responseCodeIsCreated(int responseCode) {
		return CREATED == responseCode;
	}

	private ExtendedRestResponse createResponseContainingCreatedId(HttpHandler httpHandler,
			RestResponse restResponse) {
		String createdId = extractCreatedIdFromLocationHeader(
				httpHandler.getHeaderField("Location"));
		return new ExtendedRestResponse(restResponse, createdId);
	}

	private String extractCreatedIdFromLocationHeader(String locationHeader) {
		return locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
	}

	private HttpHandler setUpHttpHandlerForPost(String json, String url) {
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestProperty(ACCEPT, APPLICATION_UUB_RECORD_JSON);
		httpHandler.setRequestProperty("Content-Type", APPLICATION_UUB_RECORD_JSON);
		httpHandler.setRequestMethod("POST");
		httpHandler.setOutput(json);
		return httpHandler;
	}

	@Override
	public RestResponse updateRecordFromJson(String recordType, String recordId, String json) {
		HttpHandler httpHandler = createHttpHandlerForUpdate(recordType, recordId, json);

		int responseCode = httpHandler.getResponseCode();
		String responseText = responseCodeIsOk(responseCode) ? httpHandler.getResponseText()
				: httpHandler.getErrorText();
		return new RestResponse(responseCode, responseText);
	}

	private HttpHandler createHttpHandlerForUpdate(String recordType, String recordId,
			String json) {
		String url = baseUrl + recordType + "/" + recordId;
		return setUpHttpHandlerForPost(json, url);
	}

	@Override
	public RestResponse deleteRecord(String recordType, String recordId) {
		HttpHandler httpHandler = createHttpHandlerForDelete(recordType, recordId);

		int responseCode = httpHandler.getResponseCode();
		String responseText = responseCodeIsOk(responseCode) ? httpHandler.getResponseText()
				: httpHandler.getErrorText();
		return new RestResponse(responseCode, responseText);
	}

	private HttpHandler createHttpHandlerForDelete(String recordType, String recordId) {
		String url = baseUrl + recordType + "/" + recordId;
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestMethod("DELETE");
		return httpHandler;
	}

	@Override
	public RestResponse readRecordListAsJson(String recordType) {
		String url = baseUrl + recordType;
		return readRecordListUsingUrl(url);

	}

	private RestResponse readRecordListUsingUrl(String url) {
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestMethod("GET");

		int responseCode = httpHandler.getResponseCode();
		String responseText = responseCodeIsOk(responseCode) ? httpHandler.getResponseText()
				: httpHandler.getErrorText();
		return new RestResponse(responseCode, responseText);
	}

	@Override
	public RestResponse readIncomingLinksAsJson(String recordType, String recordId) {
		HttpHandler httpHandler = createHttpHandlerForIncomingLinks(recordType, recordId);

		int responseCode = httpHandler.getResponseCode();
		String responseText = responseCodeIsOk(responseCode) ? httpHandler.getResponseText()
				: httpHandler.getErrorText();

		return new RestResponse(responseCode, responseText);
	}

	private HttpHandler createHttpHandlerForIncomingLinks(String recordType, String recordId) {
		String url = baseUrl + recordType + "/" + recordId + "/incomingLinks";
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestMethod("GET");
		return httpHandler;
	}

	@Override
	public RestResponse readRecordListWithFilterAsJson(String recordType, String filter)
			throws UnsupportedEncodingException {
		String url = baseUrl + recordType + "?filter="
				+ URLEncoder.encode(filter, StandardCharsets.UTF_8.name());
		return readRecordListUsingUrl(url);
	}

	@Override
	public ExtendedRestResponse batchIndexWithFilterAsJson(String recordType, String filterAsJson) {

		HttpHandler httpHandler = createHttpHandlerForIndexBatchJob(recordType, filterAsJson);

		int responseCode = httpHandler.getResponseCode();
		RestResponse restResponse = createRestResponse(httpHandler, responseCode);

		return responseCodeIsCreated(responseCode)
				? createResponseContainingCreatedId(httpHandler, restResponse)
				: new ExtendedRestResponse(restResponse);
	}

	private HttpHandler createHttpHandlerForIndexBatchJob(String recordType, String filterAsJson) {
		String url = baseUrl + "index/" + recordType;
		return setUpHttpHandlerForPost(filterAsJson, url);
	}

	public HttpHandlerFactory getHttpHandlerFactory() {
		// needed for test
		return httpHandlerFactory;
	}

	@Override
	public String getBaseUrl() {
		// needed for test
		return baseUrl;
	}

	public String getAuthToken() {
		// needed for test
		return authToken;
	}

}
