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
package se.uu.ub.cora.javaclient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.javaclient.cora.CoraClientException;
import se.uu.ub.cora.javaclient.rest.ExtendedRestResponse;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestResponse;

public final class RestClientImp implements RestClient {
	private static final String RETURNED_ERROR_WAS = ". Returned error was: ";
	private static final String FROM_SERVER_USING_URL = " from server using url: ";
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
		String url = baseUrl + recordType + "/" + recordId;
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestMethod("GET");

		int responseCode = httpHandler.getResponseCode();
		String responseText = responseCodeIsOk(responseCode) ? httpHandler.getResponseText()
				: httpHandler.getErrorText();
		return new RestResponse(responseCode, responseText);
		// throw new CoraClientException("Could not read record of type: " + recordType + " and id:
		// "
		// + recordId + FROM_SERVER_USING_URL + url + RETURNED_ERROR_WAS
		// + httpHandler.getErrorText());
	}

	private boolean responseCodeIsOk(int responseCode) {
		return responseCode == OK;
	}

	private boolean statusIsOk(Status statusType) {
		return statusType == Response.Status.OK;
	}

	private HttpHandler createHttpHandlerWithAuthTokenAndUrl(String url) {
		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		httpHandler.setRequestProperty("authToken", authToken);
		return httpHandler;
	}

	public HttpHandlerFactory getHttpHandlerFactory() {
		// needed for test
		return httpHandlerFactory;
	}

	public String getBaseUrl() {
		// needed for test
		return baseUrl;
	}

	public String getAuthToken() {
		// needed for test
		return authToken;
	}

	@Override
	public ExtendedRestResponse createRecordFromJson(String recordType, String json) {
		String url = baseUrl + recordType;
		HttpHandler httpHandler = setUpHttpHandlerForPost(json, url);
		int responseCode = httpHandler.getResponseCode();

		String responseText = responseCodeIsCreated(responseCode) ? httpHandler.getResponseText()
				: httpHandler.getErrorText();
		RestResponse restResponse = new RestResponse(responseCode, responseText);

		return responseCodeIsCreated(responseCode) ? createCreateResponse(httpHandler, restResponse)
				: new ExtendedRestResponse(restResponse);

		// throw new CoraClientException("Could not create record of type: " + recordType
		// + FROM_SERVER_USING_URL + url + RETURNED_ERROR_WAS + httpHandler.getErrorText());
	}

	private boolean responseCodeIsCreated(int responseCode) {
		return CREATED == responseCode;
	}

	private ExtendedRestResponse createCreateResponse(HttpHandler httpHandler,
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
	public String updateRecordFromJson(String recordType, String recordId, String json) {
		String url = baseUrl + recordType + "/" + recordId;
		HttpHandler httpHandler = setUpHttpHandlerForPost(json, url);
		if (OK == httpHandler.getResponseCode()) {
			return httpHandler.getResponseText();
		}
		throw new CoraClientException("Could not update record of type: " + recordType
				+ " with recordId: " + recordId + " on server using url: " + url
				+ RETURNED_ERROR_WAS + httpHandler.getErrorText());
	}

	@Override
	public String deleteRecord(String recordType, String recordId) {
		String url = baseUrl + recordType + "/" + recordId;
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestMethod("DELETE");

		Status statusType = Response.Status.fromStatusCode(httpHandler.getResponseCode());
		if (statusIsOk(statusType)) {
			return httpHandler.getResponseText();
		}
		throw new CoraClientException("Could not delete record of type: " + recordType + " and id: "
				+ recordId + FROM_SERVER_USING_URL + url + RETURNED_ERROR_WAS
				+ httpHandler.getErrorText());
	}

	@Override
	public RestResponse readRecordListAsJson(String recordType) {
		String url = baseUrl + recordType;
		return readRecordListUsingUrl(url);
		// throw new CoraClientException("Could not read records of type: " + recordType
		// + FROM_SERVER_USING_URL + url + RETURNED_ERROR_WAS + httpHandler.getErrorText());

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
	public String readIncomingLinksAsJson(String recordType, String recordId) {
		String url = baseUrl + recordType + "/" + recordId + "/incomingLinks";
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestMethod("GET");

		Status statusType = Response.Status.fromStatusCode(httpHandler.getResponseCode());
		if (statusIsOk(statusType)) {
			return httpHandler.getResponseText();
		}
		throw new CoraClientException("Could not read incoming links of type: " + recordType
				+ FROM_SERVER_USING_URL + url + RETURNED_ERROR_WAS + httpHandler.getErrorText());

	}

	@Override
	public RestResponse readRecordListWithFilterAsJson(String recordType, String filter)
			throws UnsupportedEncodingException {
		String url = baseUrl + recordType + "?filter="
				+ URLEncoder.encode(filter, StandardCharsets.UTF_8.name());
		return readRecordListUsingUrl(url);
	}

}
