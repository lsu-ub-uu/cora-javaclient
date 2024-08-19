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
package se.uu.ub.cora.javaclient.rest.internal;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Supplier;

import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestResponse;
import se.uu.ub.cora.javaclient.token.TokenClient;

public final class RestClientImp implements RestClient {
	private static final int OK = 200;
	private static final int CREATED = 201;
	private static final int UNAUTHORIZED = 401;
	private static final String APPLICATION_UUB_RECORD_JSON = "application/vnd.uub.record+json";
	private static final String ACCEPT = "Accept";
	private HttpHandlerFactory httpHandlerFactory;
	private String baseUrl;
	private TokenClient tokenClient;
	private String baseUrlRecord;
	// private int waitTimeMillis = 10000;

	public static RestClientImp usingHttpHandlerFactoryAndBaseUrlAndTokenClient(
			HttpHandlerFactory httpHandlerFactory, String baseUrl, TokenClient tokenClient) {
		return new RestClientImp(httpHandlerFactory, baseUrl, tokenClient);
	}

	private RestClientImp(HttpHandlerFactory httpHandlerFactory, String baseUrl,
			TokenClient tokenClient) {
		this.httpHandlerFactory = httpHandlerFactory;
		this.baseUrl = baseUrl;
		this.baseUrlRecord = baseUrl + "record/";
		this.tokenClient = tokenClient;
	}

	@Override
	public RestResponse readRecordAsJson(String recordType, String recordId) {
		HttpHandler httpHandler = createUpHttpHandlerForRead(recordType, recordId);

		return handleResponseFromHttpHandler(httpHandler,
				() -> readRecordAsJson(recordType, recordId));
	}

	private HttpHandler createUpHttpHandlerForRead(String recordType, String recordId) {
		String url = baseUrlRecord + recordType + "/" + recordId;
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestMethod("GET");
		return httpHandler;
	}

	private HttpHandler createHttpHandlerWithAuthTokenAndUrl(String url) {
		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		httpHandler.setRequestProperty("authToken", tokenClient.getAuthToken());
		return httpHandler;
	}

	private RestResponse handleResponseFromHttpHandler(HttpHandler httpHandler,
			Supplier<RestResponse> methodToRetry) {
		int responseCode = httpHandler.getResponseCode();
		if (responseCode == UNAUTHORIZED) {
			return renewAuthTokenAndrecallMethod(methodToRetry);
		}
		return composeRestResponse(httpHandler, responseCode);
	}

	private RestResponse composeRestResponse(HttpHandler httpHandler, int responseCode) {
		if (responseCode == OK) {
			return responseForOk(httpHandler, responseCode);
		}
		return responseForError(httpHandler, responseCode);
	}

	RestResponse renewAuthTokenAndrecallMethod(Supplier<RestResponse> methodToRetry) {
		// SPIKE starts here: Add TimeOut
		// Object lock = new Object();
		// synchronized (lock) {
		// try {
		// lock.wait(waitTimeMillis); // This is safe and won't throw
		// } catch (InterruptedException e) {
		// Thread.currentThread().interrupt(); // Restore the interrupted status
		// }
		// }
		// SPIKE ends here
		tokenClient.possiblyRenewAuthToken();
		return methodToRetry.get();
	}

	private RestResponse responseForOk(HttpHandler httpHandler, int responseCode) {
		return new RestResponse(responseCode, httpHandler.getResponseText(), Optional.empty(),
				Optional.empty());
	}

	private RestResponse responseForError(HttpHandler httpHandler, int responseCode) {
		return new RestResponse(responseCode, httpHandler.getErrorText(), Optional.empty(),
				Optional.empty());
	}

	@Override
	public RestResponse createRecordFromJson(String recordType, String json) {
		HttpHandler httpHandler = createHttpHandlerForCreate(recordType, json);
		return composeCreateRestResponseFromHttpHandler(httpHandler,
				() -> createRecordFromJson(recordType, json));
	}

	private HttpHandler createHttpHandlerForCreate(String recordType, String json) {
		String url = baseUrlRecord + recordType;
		return setUpHttpHandlerForPost(json, url);
	}

	private RestResponse composeCreateRestResponseFromHttpHandler(HttpHandler httpHandler,
			Supplier<RestResponse> methodToRetry) {
		int responseCode = httpHandler.getResponseCode();
		if (responseCode == UNAUTHORIZED) {
			return renewAuthTokenAndrecallMethod(methodToRetry);
		}
		if (responseCode == CREATED) {
			return createResponseContainingCreatedId(httpHandler, responseCode);
		}
		return responseForError(httpHandler, responseCode);
	}

	private RestResponse createResponseContainingCreatedId(HttpHandler httpHandler,
			int responseCode) {
		String createdId = extractCreatedIdFromLocationHeader(
				httpHandler.getHeaderField("Location"));
		return new RestResponse(responseCode, httpHandler.getResponseText(), Optional.empty(),
				Optional.of(createdId));
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

		return handleResponseFromHttpHandler(httpHandler,
				() -> updateRecordFromJson(recordType, recordId, json));
	}

	private HttpHandler createHttpHandlerForUpdate(String recordType, String recordId,
			String json) {
		String url = baseUrlRecord + recordType + "/" + recordId;
		return setUpHttpHandlerForPost(json, url);
	}

	@Override
	public RestResponse deleteRecord(String recordType, String recordId) {
		HttpHandler httpHandler = createHttpHandlerForDelete(recordType, recordId);

		return handleResponseFromHttpHandler(httpHandler, () -> deleteRecord(recordType, recordId));
	}

	private HttpHandler createHttpHandlerForDelete(String recordType, String recordId) {
		String url = baseUrlRecord + recordType + "/" + recordId;
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestMethod("DELETE");
		return httpHandler;
	}

	@Override
	public RestResponse readRecordListAsJson(String recordType) {
		String url = baseUrlRecord + recordType;
		return readRecordListUsingUrl(url, recordType);

	}

	private RestResponse readRecordListUsingUrl(String url, String recordType) {
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestMethod("GET");

		return handleResponseFromHttpHandler(httpHandler, () -> readRecordListAsJson(recordType));
	}

	@Override
	public RestResponse readIncomingLinksAsJson(String recordType, String recordId) {
		HttpHandler httpHandler = createHttpHandlerForIncomingLinks(recordType, recordId);

		return handleResponseFromHttpHandler(httpHandler,
				() -> readIncomingLinksAsJson(recordType, recordId));
	}

	private HttpHandler createHttpHandlerForIncomingLinks(String recordType, String recordId) {
		String url = baseUrlRecord + recordType + "/" + recordId + "/incomingLinks";
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestMethod("GET");
		return httpHandler;
	}

	@Override
	public RestResponse readRecordListWithFilterAsJson(String recordType, String filter) {
		String url = baseUrlRecord + recordType + "?filter="
				+ URLEncoder.encode(filter, StandardCharsets.UTF_8);
		return readRecordListUsingUrl(url, recordType);
	}

	@Override
	public RestResponse batchIndexWithFilterAsJson(String recordType, String indexSettingsAsJson) {
		HttpHandler httpHandler = createHttpHandlerForIndexBatchJob(recordType,
				indexSettingsAsJson);

		return composeCreateRestResponseFromHttpHandler(httpHandler,
				() -> batchIndexWithFilterAsJson(recordType, indexSettingsAsJson));
	}

	private HttpHandler createHttpHandlerForIndexBatchJob(String recordType,
			String indexSettingsAsJson) {
		String url = baseUrlRecord + "index/" + recordType;

		return setUpHttpHandlerForPost(indexSettingsAsJson, url);
	}

	public String onlyForTestGetBaseUrl() {
		return baseUrl;
	}

	public HttpHandlerFactory onlyForTestGetHttpHandlerFactory() {
		return httpHandlerFactory;
	}

	public TokenClient onlyForTestGetTokenClient() {
		return tokenClient;
	}

	@Override
	public RestResponse searchRecordWithSearchCriteriaAsJson(String searchId, String json) {
		return searchRecord(searchId, json);
	}

	private RestResponse searchRecord(String searchId, String json) {
		String url = setUrlForSearch(searchId, json);
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestMethod("GET");

		return handleResponseFromHttpHandler(httpHandler,
				() -> searchRecordWithSearchCriteriaAsJson(searchId, json));
	}

	private String setUrlForSearch(String searchId, String json) {
		return baseUrlRecord + "searchResult/" + searchId + "?searchData="
				+ URLEncoder.encode(json, StandardCharsets.UTF_8);
	}

	@Override
	public RestResponse validateRecordAsJson(String json) {
		String url = baseUrlRecord + "workOrder";
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		setHttpHandlerValidateWorkOrder(httpHandler, json);
		return handleResponseFromHttpHandler(httpHandler, () -> validateRecordAsJson(json));
	}

	protected void setHttpHandlerValidateWorkOrder(HttpHandler httpHandler, String json) {
		httpHandler.setRequestMethod("POST");
		httpHandler.setRequestProperty("Accept", APPLICATION_UUB_RECORD_JSON);
		httpHandler.setRequestProperty("Content-Type", "application/vnd.uub.workorder+json");
		httpHandler.setOutput(json);
	}

	@Override
	public RestResponse download(String type, String id, String representation) {
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(
				baseUrl + "record/" + type + "/" + id + "/" + representation);
		httpHandler.setRequestMethod("GET");

		int responseCode = httpHandler.getResponseCode();
		if (responseCode == 200) {
			InputStream responseBinary = httpHandler.getResponseBinary();
			return new RestResponse(responseCode, "", Optional.of(responseBinary),
					Optional.empty());
		}
		String errorMessage = httpHandler.getErrorText();
		return new RestResponse(responseCode, errorMessage, Optional.empty(), Optional.empty());
	}
}
