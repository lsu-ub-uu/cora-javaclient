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
import se.uu.ub.cora.javaclient.data.DataClientException;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestResponse;
import se.uu.ub.cora.javaclient.token.TokenClient;

public final class RestClientImp implements RestClient {
	private static final int OK = 200;
	private static final int CREATED = 201;
	private static final int UNAUTHORIZED = 401;
	private static final String APPLICATION_UUB_RECORD_JSON = "application/vnd.cora.record+json";
	private static final String APPLICATION_UUB_RECORD_LIST_JSON = "application/vnd.cora.recordList+json";
	private static final String ACCEPT = "Accept";
	private HttpHandlerFactory httpHandlerFactory;
	private String baseUrl;
	private TokenClient tokenClient;
	private String baseUrlRecord;

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
	public RestResponse createRecordFromJson(String recordType, String json) {
		Supplier<RestResponse> methodToRetry = () -> createRecordFromJson(recordType, json);

		HttpHandler httpHandler = createHttpHandlerForCreate(recordType, json);
		return handleCreateResponseFromHttpHandlerUsingMethodToRetry(httpHandler, methodToRetry);
	}

	private HttpHandler createHttpHandlerForCreate(String recordType, String json) {
		String url = baseUrlRecord + recordType;
		return setUpHttpHandlerForPost(json, url);
	}

	private HttpHandler setUpHttpHandlerForPost(String json, String url) {
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestProperty(ACCEPT, APPLICATION_UUB_RECORD_JSON);
		httpHandler.setRequestProperty("Content-Type", APPLICATION_UUB_RECORD_JSON);
		httpHandler.setRequestMethod("POST");
		httpHandler.setOutput(json);
		return httpHandler;
	}

	private HttpHandler createHttpHandlerWithAuthTokenAndUrl(String url) {
		HttpHandler httpHandler = httpHandlerFactory.factor(url);
		httpHandler.setRequestProperty("authToken", tokenClient.getAuthToken());
		return httpHandler;
	}

	private RestResponse handleCreateResponseFromHttpHandlerUsingMethodToRetry(
			HttpHandler httpHandler, Supplier<RestResponse> methodToRetry) {
		if (responseIsCreated(httpHandler)) {
			return composeResponseForCreated(httpHandler);
		}
		return handleErrorResponses(httpHandler, methodToRetry);
	}

	private boolean responseIsCreated(HttpHandler httpHandler) {
		return httpHandler.getResponseCode() == CREATED;
	}

	private RestResponse composeResponseForCreated(HttpHandler httpHandler) {
		String createdId = extractCreatedIdFromLocationHeader(
				httpHandler.getHeaderField("Location"));
		return new RestResponse(httpHandler.getResponseCode(), httpHandler.getResponseText(),
				Optional.empty(), Optional.of(createdId));
	}

	private String extractCreatedIdFromLocationHeader(String locationHeader) {
		return locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
	}

	private RestResponse handleErrorResponses(HttpHandler httpHandler,
			Supplier<RestResponse> methodToRetry) {
		if (responseIsUnauthorized(httpHandler)) {
			return tryRequestNewAuthTokenAndRetryToCallMethod(httpHandler, methodToRetry);
		}
		return composeResponseForAnyOtherError(httpHandler);
	}

	private boolean responseIsUnauthorized(HttpHandler httpHandler) {
		return httpHandler.getResponseCode() == UNAUTHORIZED;
	}

	private RestResponse tryRequestNewAuthTokenAndRetryToCallMethod(HttpHandler httpHandler,
			Supplier<RestResponse> methodToRetry) {
		try {
			return requestNewAuthTokenAndRetryToCallMethod(methodToRetry);
		} catch (DataClientException e) {
			return composeResponseForAnyOtherError(httpHandler);
		}
	}

	RestResponse requestNewAuthTokenAndRetryToCallMethod(Supplier<RestResponse> methodToRetry) {
		tokenClient.requestNewAuthToken();
		return methodToRetry.get();
	}

	private RestResponse composeResponseForAnyOtherError(HttpHandler httpHandler) {
		return new RestResponse(httpHandler.getResponseCode(), httpHandler.getErrorText(),
				Optional.empty(), Optional.empty());
	}

	@Override
	public RestResponse readRecordAsJson(String recordType, String recordId) {
		HttpHandler httpHandler = createUpHttpHandlerForRead(recordType, recordId);
		Supplier<RestResponse> methodToRetry = () -> readRecordAsJson(recordType, recordId);
		return handleResponseFromHttpHandlerUsingMethodToRetry(httpHandler, methodToRetry);
	}

	private HttpHandler createUpHttpHandlerForRead(String recordType, String recordId) {
		String url = baseUrlRecord + recordType + "/" + recordId;
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestProperty(ACCEPT, APPLICATION_UUB_RECORD_JSON);
		httpHandler.setRequestMethod("GET");
		return httpHandler;
	}

	private RestResponse handleResponseFromHttpHandlerUsingMethodToRetry(HttpHandler httpHandler,
			Supplier<RestResponse> methodToRetry) {
		if (responseIsOk(httpHandler)) {
			return composeResponseForOk(httpHandler);
		}
		return handleErrorResponses(httpHandler, methodToRetry);
	}

	private boolean responseIsOk(HttpHandler httpHandler) {
		return httpHandler.getResponseCode() == OK;
	}

	private RestResponse composeResponseForOk(HttpHandler httpHandler) {
		return new RestResponse(httpHandler.getResponseCode(), httpHandler.getResponseText(),
				Optional.empty(), Optional.empty());
	}

	@Override
	public RestResponse updateRecordFromJson(String recordType, String recordId, String json) {
		HttpHandler httpHandler = createHttpHandlerForUpdate(recordType, recordId, json);
		Supplier<RestResponse> methodToRetry = () -> updateRecordFromJson(recordType, recordId,
				json);
		return handleResponseFromHttpHandlerUsingMethodToRetry(httpHandler, methodToRetry);
	}

	private HttpHandler createHttpHandlerForUpdate(String recordType, String recordId,
			String json) {
		String url = baseUrlRecord + recordType + "/" + recordId;
		return setUpHttpHandlerForPost(json, url);
	}

	@Override
	public RestResponse deleteRecord(String recordType, String recordId) {
		HttpHandler httpHandler = createHttpHandlerForDelete(recordType, recordId);
		Supplier<RestResponse> methodToRetry = () -> deleteRecord(recordType, recordId);
		return handleResponseFromHttpHandlerUsingMethodToRetry(httpHandler, methodToRetry);
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
		httpHandler.setRequestProperty(ACCEPT, APPLICATION_UUB_RECORD_LIST_JSON);
		// TODO: this should be a method with url as this is called from
		// readRecordListWithFilterAsJson aswell
		Supplier<RestResponse> methodToRetry = () -> readRecordListAsJson(recordType);
		return handleResponseFromHttpHandlerUsingMethodToRetry(httpHandler, methodToRetry);
	}

	@Override
	public RestResponse readIncomingLinksAsJson(String recordType, String recordId) {
		HttpHandler httpHandler = createHttpHandlerForIncomingLinks(recordType, recordId);

		return handleResponseFromHttpHandlerUsingMethodToRetry(httpHandler,
				() -> readIncomingLinksAsJson(recordType, recordId));
	}

	private HttpHandler createHttpHandlerForIncomingLinks(String recordType, String recordId) {
		String url = baseUrlRecord + recordType + "/" + recordId + "/incomingLinks";
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestMethod("GET");
		httpHandler.setRequestProperty(ACCEPT, APPLICATION_UUB_RECORD_LIST_JSON);
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
		Supplier<RestResponse> methodToRetry = () -> batchIndexWithFilterAsJson(recordType,
				indexSettingsAsJson);
		return handleCreateResponseFromHttpHandlerUsingMethodToRetry(httpHandler, methodToRetry);
	}

	private HttpHandler createHttpHandlerForIndexBatchJob(String recordType,
			String indexSettingsAsJson) {
		String url = baseUrlRecord + "index/" + recordType;
		return setUpHttpHandlerForPost(indexSettingsAsJson, url);
	}

	@Override
	public RestResponse searchRecordWithSearchCriteriaAsJson(String searchId, String json) {
		HttpHandler httpHandler = createHttpHandlerForSearch(searchId, json);
		Supplier<RestResponse> methodToRetry = () -> searchRecordWithSearchCriteriaAsJson(searchId,
				json);
		return handleResponseFromHttpHandlerUsingMethodToRetry(httpHandler, methodToRetry);
	}

	private HttpHandler createHttpHandlerForSearch(String searchId, String json) {
		String url = baseUrlRecord + "searchResult/" + searchId + "?searchData="
				+ URLEncoder.encode(json, StandardCharsets.UTF_8);
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestMethod("GET");
		httpHandler.setRequestProperty(ACCEPT, APPLICATION_UUB_RECORD_LIST_JSON);
		return httpHandler;
	}

	@Override
	public RestResponse validateRecordAsJson(String json) {
		HttpHandler httpHandler = createHttpHandlerForValidate(json);
		Supplier<RestResponse> methodToRetry = () -> validateRecordAsJson(json);
		return handleResponseFromHttpHandlerUsingMethodToRetry(httpHandler, methodToRetry);
	}

	private HttpHandler createHttpHandlerForValidate(String json) {
		String url = baseUrlRecord + "workOrder";
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestMethod("POST");
		httpHandler.setRequestProperty(ACCEPT, APPLICATION_UUB_RECORD_JSON);
		httpHandler.setRequestProperty("Content-Type", "application/vnd.cora.workorder+json");
		httpHandler.setOutput(json);
		return httpHandler;
	}

	@Override
	public RestResponse download(String type, String id, String representation) {
		HttpHandler httpHandler = createHttpHandlerForDownload(type, id, representation);
		Supplier<RestResponse> methodToRetry = () -> download(type, id, representation);
		return handlDownloadResponseFromHttpHandlerUsingMethodToRetry(httpHandler, methodToRetry);
	}

	private HttpHandler createHttpHandlerForDownload(String type, String id,
			String representation) {
		String url = baseUrl + "record/" + type + "/" + id + "/" + representation;
		HttpHandler httpHandler = createHttpHandlerWithAuthTokenAndUrl(url);
		httpHandler.setRequestMethod("GET");
		return httpHandler;
	}

	private RestResponse handlDownloadResponseFromHttpHandlerUsingMethodToRetry(
			HttpHandler httpHandler, Supplier<RestResponse> methodToRetry) {
		if (responseIsOk(httpHandler)) {
			return composeDownloadResponseForOk(httpHandler);
		}
		return handleErrorResponses(httpHandler, methodToRetry);
	}

	private RestResponse composeDownloadResponseForOk(HttpHandler httpHandler) {
		InputStream responseBinary = httpHandler.getResponseBinary();
		return new RestResponse(httpHandler.getResponseCode(), "", Optional.of(responseBinary),
				Optional.empty());
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
}
