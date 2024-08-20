/*
 * Copyright 2015, 2016, 2018 Uppsala University Library
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.httphandler.spies.HttpHandlerFactorySpy;
import se.uu.ub.cora.httphandler.spies.HttpHandlerSpy;
import se.uu.ub.cora.javaclient.TokenClientSpy;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestResponse;

public class RestClientTest {
	private static final String SOME_REPRESENTATION = "someRepresentation";
	private static final String JSON_RECORD = "{\"name\":\"value\"}";
	private static final String FILTER = "{\"name\":\"filter\",\"children\":[{\"name\":\"part\",\"children\":["
			+ "{\"name\":\"key\",\"value\":\"idFromLogin\"},"
			+ "{\"name\":\"value\",\"value\":\"someId\"}],\"repeatId\":\"0\"}]}";
	private static final int ERROR_CODE = 500;
	private static final int CREATED_CODE = 201;
	private static final int OK_CODE = 200;
	private static final String SOME_ID = "someId";
	private static final String SOME_TYPE = "someType";
	private HttpHandlerFactorySpy httpHandlerFactorySpy;
	private String baseUrl;
	private TokenClientSpy tokenClient;
	private RestClient restClient;
	private HttpHandlerSpy httpHandlerSpy_first;
	private HttpHandlerSpy httpHandlerSpy_second;

	@BeforeMethod
	public void setUp() {
		baseUrl = "http://localhost:8080/therest/rest/";

		httpHandlerSpy_first = new HttpHandlerSpy();
		httpHandlerSpy_second = new HttpHandlerSpy();
		httpHandlerFactorySpy = new HttpHandlerFactorySpy();
		setUpHttpHandler();

		tokenClient = new TokenClientSpy();
		tokenClient.MRV.setDefaultReturnValuesSupplier("getAuthToken", () -> "someToken");
		restClient = RestClientImp.usingHttpHandlerFactoryAndBaseUrlAndTokenClient(
				httpHandlerFactorySpy, baseUrl, tokenClient);
	}

	private void setUpHttpHandler() {
		String readUrl = baseUrl + "record/" + SOME_TYPE + "/" + SOME_ID;
		String readListUrl = baseUrl + "record/" + SOME_TYPE;
		String workOrderUrl = baseUrl + "record/workOrder";
		String incommingLinksUrl = baseUrl + "record/" + SOME_TYPE + "/" + SOME_ID
				+ "/incomingLinks";
		String batchIndexUrl = baseUrl + "record/index/recordTypeToIndex";
		String downloadUrl = baseUrl + "record/" + SOME_TYPE + "/" + SOME_ID + "/"
				+ SOME_REPRESENTATION;
		String searchUrl = "http://localhost:8080/therest/rest/record/searchResult/someSearchId?searchData=%7B%7D";

		setReturnValuesForHttpHandler(readUrl);
		setReturnValuesForHttpHandler(readListUrl);
		setReturnValuesForHttpHandler(workOrderUrl);
		setReturnValuesForHttpHandler(incommingLinksUrl);
		setReturnValuesForHttpHandler(batchIndexUrl);
		setReturnValuesForHttpHandler(downloadUrl);
		setReturnValuesForHttpHandler(searchUrl);
	}

	private void setReturnValuesForHttpHandler(String url) {
		httpHandlerFactorySpy.MRV.setReturnValues("factor",
				List.of(httpHandlerSpy_first, httpHandlerSpy_second), url);
	}

	@Test
	public void testInit() throws Exception {
		RestClientImp restClientImp = (RestClientImp) restClient;

		assertSame(restClientImp.onlyForTestGetHttpHandlerFactory(), httpHandlerFactorySpy);
		assertSame(restClientImp.onlyForTestGetBaseUrl(), baseUrl);
		assertSame(restClientImp.onlyForTestGetTokenClient(), tokenClient);
	}

	@Test
	public void testReadRecordHttpHandlerSetupCorrectly() {
		restClient.readRecordAsJson(SOME_TYPE, SOME_ID);

		httpHandlerFactorySpy.MCR.assertParameters("factor", 0,
				"http://localhost:8080/therest/rest/record/someType/someId");
		httpHandlerSpy_first.MCR.assertParameters("setRequestMethod", 0, "GET");
		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 0, "authToken",
				tokenClient.getAuthToken());
		httpHandlerSpy_first.MCR.assertNumberOfCallsToMethod("setRequestProperty", 1);
	}

	@Test
	public void testReadRecordReturnsInformationFromHttpHandlerOnCode200() {
		RestResponse response = restClient.readRecordAsJson(SOME_TYPE, SOME_ID);

		assertResponseOK(response);
	}

	@Test
	public void testReadRecordReturnsInformationFromHttpHandlerOnCodeOtherThan200() {
		setHttpHandlerToReturnErrorResponseCode();

		RestResponse response = restClient.readRecordAsJson(SOME_TYPE, SOME_ID);

		assertResponseOnError(response);
	}

	@Test
	public void testReadRecordListHttpHandlerSetupCorrectly() {
		restClient.readRecordListAsJson(SOME_TYPE);

		httpHandlerFactorySpy.MCR.assertParameters("factor", 0,
				"http://localhost:8080/therest/rest/record/someType");
		httpHandlerSpy_first.MCR.assertParameters("setRequestMethod", 0, "GET");
		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 0, "authToken",
				tokenClient.getAuthToken());
		httpHandlerSpy_first.MCR.assertNumberOfCallsToMethod("setRequestProperty", 1);
	}

	@Test
	public void testReadRecordListReturnsInformationFromHttpHandlerOnCode200() {
		RestResponse response = restClient.readRecordListAsJson(SOME_TYPE);

		assertResponseOK(response);
	}

	@Test
	public void testReadRecordListReturnsInformationFromHttpHandlerOnCodeOtherThan200() {
		setHttpHandlerToReturnErrorResponseCode();

		RestResponse response = restClient.readRecordListAsJson(SOME_TYPE);

		assertResponseOnError(response);
	}

	@Test
	public void testReadRecordListWithFilterHttpHandlerSetupCorrectly() {
		String readListWithFilterUrl = setUpHttpHandlerForReadListWithFilterUrl();

		restClient.readRecordListWithFilterAsJson(SOME_TYPE, FILTER);

		httpHandlerFactorySpy.MCR.assertParameters("factor", 0, readListWithFilterUrl);
		httpHandlerSpy_first.MCR.assertParameters("setRequestMethod", 0, "GET");
		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 0, "authToken",
				tokenClient.getAuthToken());
		httpHandlerSpy_first.MCR.assertNumberOfCallsToMethod("setRequestProperty", 1);
	}

	private String setUpHttpHandlerForReadListWithFilterUrl() {
		String encodedJson = URLEncoder.encode(FILTER, StandardCharsets.UTF_8);
		String readListWithFilterUrl = "http://localhost:8080/therest/rest/record/someType?filter="
				+ encodedJson;
		setReturnValuesForHttpHandler(readListWithFilterUrl);
		return readListWithFilterUrl;
	}

	@Test
	public void testReadRecordListWithFilterOk() {
		setUpHttpHandlerForReadListWithFilterUrl();

		RestResponse response = restClient.readRecordListWithFilterAsJson(SOME_TYPE, FILTER);

		assertResponseOK(response);
	}

	@Test
	public void testReadRecordListWithFilterNotOk() {
		setUpHttpHandlerForReadListWithFilterUrl();
		setHttpHandlerToReturnErrorResponseCode();

		RestResponse response = restClient.readRecordListWithFilterAsJson(SOME_TYPE, FILTER);

		assertResponseOnError(response);
	}

	@Test
	public void testCreateRecordHttpHandlerSetupCorrectly() throws Exception {
		setHttpHandlerToReturnCreatedResponseCodeAndLocation();

		restClient.createRecordFromJson(SOME_TYPE, JSON_RECORD);

		httpHandlerFactorySpy.MCR.assertParameters("factor", 0,
				"http://localhost:8080/therest/rest/record/someType");
		httpHandlerSpy_first.MCR.assertParameters("setRequestMethod", 0, "POST");
		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 0, "authToken",
				tokenClient.getAuthToken());
		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 1, "Accept",
				"application/vnd.uub.record+json");
		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 2, "Content-Type",
				"application/vnd.uub.record+json");
		httpHandlerSpy_first.MCR.assertNumberOfCallsToMethod("setRequestProperty", 3);

		httpHandlerSpy_first.MCR.assertParameters("setOutput", 0, JSON_RECORD);
	}

	@Test
	public void testCreateRecordOk() throws Exception {
		setHttpHandlerToReturnCreatedResponseCodeAndLocation();

		RestResponse response = restClient.createRecordFromJson(SOME_TYPE, JSON_RECORD);

		assertResponseCreatedOK(response);
	}

	@Test
	public void testCreateRecordNotOk() throws Exception {
		setHttpHandlerToReturnErrorResponseCode();

		RestResponse response = restClient.createRecordFromJson(SOME_TYPE, JSON_RECORD);

		assertResponseOnError(response);
	}

	@Test
	public void testUpdateRecordHttpHandlerSetupCorrectly() throws Exception {
		restClient.updateRecordFromJson(SOME_TYPE, SOME_ID, JSON_RECORD);

		httpHandlerFactorySpy.MCR.assertParameters("factor", 0,
				"http://localhost:8080/therest/rest/record/someType/" + SOME_ID);
		httpHandlerSpy_first.MCR.assertParameters("setRequestMethod", 0, "POST");
		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 0, "authToken",
				tokenClient.getAuthToken());
		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 1, "Accept",
				"application/vnd.uub.record+json");
		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 2, "Content-Type",
				"application/vnd.uub.record+json");
		httpHandlerSpy_first.MCR.assertNumberOfCallsToMethod("setRequestProperty", 3);

		httpHandlerSpy_first.MCR.assertParameters("setOutput", 0, JSON_RECORD);
	}

	@Test
	public void testUpdateRecordOk() {
		RestResponse response = restClient.updateRecordFromJson(SOME_TYPE, SOME_ID, JSON_RECORD);

		assertResponseOK(response);
	}

	@Test
	public void testUpdateRecordNotOk() throws Exception {
		setHttpHandlerToReturnErrorResponseCode();

		RestResponse response = restClient.updateRecordFromJson(SOME_TYPE, SOME_ID, JSON_RECORD);

		assertResponseOnError(response);
	}

	@Test
	public void testDeleteRecordHttpHandlerSetupCorrectly() throws Exception {
		restClient.deleteRecord(SOME_TYPE, SOME_ID);

		httpHandlerFactorySpy.MCR.assertParameters("factor", 0,
				"http://localhost:8080/therest/rest/record/someType/" + SOME_ID);
		httpHandlerSpy_first.MCR.assertParameters("setRequestMethod", 0, "DELETE");
		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 0, "authToken",
				tokenClient.getAuthToken());
		httpHandlerSpy_first.MCR.assertNumberOfCallsToMethod("setRequestProperty", 1);
	}

	@Test
	public void testDeleteRecordOk() throws Exception {
		RestResponse response = restClient.deleteRecord(SOME_TYPE, SOME_ID);

		assertResponseOK(response);
	}

	@Test
	public void testDeleteRecordNotOk() {
		setHttpHandlerToReturnErrorResponseCode();

		RestResponse response = restClient.deleteRecord(SOME_TYPE, SOME_ID);

		assertResponseOnError(response);
	}

	@Test
	public void testReadIncomingLinksHttpHandlerSetupCorrectly() {
		restClient.readIncomingLinksAsJson(SOME_TYPE, SOME_ID);

		httpHandlerFactorySpy.MCR.assertParameters("factor", 0,
				"http://localhost:8080/therest/rest/record/someType/" + SOME_ID + "/incomingLinks");
		httpHandlerSpy_first.MCR.assertParameters("setRequestMethod", 0, "GET");
		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 0, "authToken",
				tokenClient.getAuthToken());
		httpHandlerSpy_first.MCR.assertNumberOfCallsToMethod("setRequestProperty", 1);
	}

	@Test
	public void testReadIncomingLinksOk() {
		RestResponse response = restClient.readIncomingLinksAsJson(SOME_TYPE, SOME_ID);

		assertResponseOK(response);
	}

	@Test
	public void testReadIncomingLinksNotOk() {
		setHttpHandlerToReturnErrorResponseCode();

		RestResponse response = restClient.readIncomingLinksAsJson(SOME_TYPE, SOME_ID);

		assertResponseOnError(response);
	}

	@Test
	public void testBatchIndexWithFilterHttpHandlerSetupCorrectly() {
		setHttpHandlerToReturnCreatedResponseCodeAndLocation();

		restClient.batchIndexWithFilterAsJson("recordTypeToIndex", FILTER);

		httpHandlerFactorySpy.MCR.assertNumberOfCallsToMethod("factor", 1);

		httpHandlerFactorySpy.MCR.assertParameters("factor", 0,
				"http://localhost:8080/therest/rest/record/index/recordTypeToIndex");
		httpHandlerSpy_first.MCR.assertParameters("setRequestMethod", 0, "POST");
		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 0, "authToken",
				tokenClient.getAuthToken());
		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 1, "Accept",
				"application/vnd.uub.record+json");
		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 2, "Content-Type",
				"application/vnd.uub.record+json");
		httpHandlerSpy_first.MCR.assertNumberOfCallsToMethod("setRequestProperty", 3);

		httpHandlerSpy_first.MCR.assertParameters("setOutput", 0, FILTER);
	}

	@Test
	public void testBatchIndexWithFilterOk() {
		setHttpHandlerToReturnCreatedResponseCodeAndLocation();

		RestResponse response = restClient.batchIndexWithFilterAsJson("recordTypeToIndex", FILTER);

		assertResponseCreatedOK(response);
	}

	@Test
	public void testBatchIndexWithFilterNotOk() {
		setHttpHandlerToReturnErrorResponseCode();

		RestResponse response = restClient.batchIndexWithFilterAsJson("recordTypeToIndex", FILTER);

		assertResponseOnError(response);
	}

	private void setHttpHandlerToReturnCreatedResponseCodeAndLocation() {
		httpHandlerSpy_first.MRV.setDefaultReturnValuesSupplier("getResponseCode",
				() -> CREATED_CODE);
		httpHandlerSpy_first.MRV.setSpecificReturnValuesSupplier("getHeaderField",
				() -> "http://some.place/rest/record/type/" + SOME_ID, "Location");
	}

	private void setHttpHandlerToReturnErrorResponseCode() {
		httpHandlerSpy_first.MRV.setDefaultReturnValuesSupplier("getResponseCode",
				() -> ERROR_CODE);
	}

	private void assertResponseOK(RestResponse response) {
		httpHandlerSpy_first.MCR.assertReturn("getResponseCode", 0, response.responseCode());
		httpHandlerSpy_first.MCR.assertReturn("getResponseText", 0, response.responseText());
		assertEquals(response.responseCode(), OK_CODE);
		assertTrue(response.createdId().isEmpty());
	}

	private void assertResponseCreatedOK(RestResponse response) {
		httpHandlerSpy_first.MCR.assertReturn("getResponseCode", 0, response.responseCode());
		httpHandlerSpy_first.MCR.assertReturn("getResponseText", 0, response.responseText());
		assertEquals(response.responseCode(), CREATED_CODE);
		assertEquals(response.createdId().get(), SOME_ID);
	}

	private void assertResponseOnError(RestResponse response) {
		httpHandlerSpy_first.MCR.assertReturn("getResponseCode", 0, response.responseCode());
		httpHandlerSpy_first.MCR.assertReturn("getErrorText", 0, response.responseText());
		assertEquals(response.responseCode(), ERROR_CODE);
		assertTrue(response.createdId().isEmpty());
	}

	@Test
	public void testSearchRecordOK() throws Exception {
		String json = "{\"name\":\"search\",\"children\":[{\"name\":\"include\",\"children\":["
				+ "{\"name\":\"includePart\",\"children\":[{\"name\":\"text\",\"value\":\"\"}]}]}]}";
		String searchId = "someSearchId";

		setUpHttpHandlerForSearchUrl(searchId, json);

		RestResponse response = restClient.searchRecordWithSearchCriteriaAsJson(searchId, json);

		String jsonEncoded = URLEncoder.encode(json, StandardCharsets.UTF_8.name());
		httpHandlerFactorySpy.MCR.assertParameters("factor", 0,
				baseUrl + "record/searchResult/" + searchId + "?searchData=" + jsonEncoded);
		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 0, "authToken",
				tokenClient.getAuthToken());
		httpHandlerSpy_first.MCR.assertParameters("setRequestMethod", 0, "GET");
		assertResponseOK(response);
	}

	@Test
	public void testSearchRecordNotOk() throws Exception {
		String json = "{\"name\":\"search\",\"children\":[{\"name\":\"include\",\"children\":["
				+ "{\"name\":\"includePart\",\"children\":[{\"name\":\"text\",\"value\":\"\"}]}]}]}";
		String searchId = "someSearchId";
		setUpHttpHandlerForSearchUrl(searchId, json);
		setHttpHandlerToReturnErrorResponseCode();

		RestResponse response = restClient.searchRecordWithSearchCriteriaAsJson(searchId, json);

		assertResponseOnError(response);
	}

	private String setUpHttpHandlerForSearchUrl(String searchId, String json) {
		String encodedSearchData = URLEncoder.encode(json, StandardCharsets.UTF_8);
		String searchUrl = baseUrl + "record/searchResult/" + searchId + "?searchData="
				+ encodedSearchData;
		setReturnValuesForHttpHandler(searchUrl);
		return searchUrl;
	}

	@Test
	public void testValidateRecordOK() throws Exception {
		String json = "someJson";
		RestResponse response = restClient.validateRecordAsJson(json);

		httpHandlerFactorySpy.MCR.assertParameters("factor", 0, baseUrl + "record/workOrder");

		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 0, "authToken",
				tokenClient.getAuthToken());
		httpHandlerSpy_first.MCR.assertParameters("setRequestMethod", 0, "POST");
		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 1, "Accept",
				"application/vnd.uub.record+json");
		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 2, "Content-Type",
				"application/vnd.uub.workorder+json");
		httpHandlerSpy_first.MCR.assertNumberOfCallsToMethod("setRequestProperty", 3);
		httpHandlerSpy_first.MCR.assertParameters("setOutput", 0, json);

		assertResponseOK(response);
	}

	@Test
	public void testValidateRecordNotOk() throws Exception {
		setHttpHandlerToReturnErrorResponseCode();
		String json = "someJson";
		RestResponse response = restClient.validateRecordAsJson(json);

		assertResponseOnError(response);
	}

	@Test
	public void testDownloadOk() throws Exception {
		RestResponse response = restClient.download(SOME_TYPE, SOME_ID, SOME_REPRESENTATION);

		httpHandlerFactorySpy.MCR.assertParameters("factor", 0,
				baseUrl + "record/" + SOME_TYPE + "/" + SOME_ID + "/" + SOME_REPRESENTATION);

		httpHandlerSpy_first.MCR.assertParameters("setRequestProperty", 0, "authToken",
				tokenClient.getAuthToken());
		httpHandlerSpy_first.MCR.assertParameters("setRequestMethod", 0, "GET");

		assertResponseBinaryOK(response);
	}

	private void assertResponseBinaryOK(RestResponse response) {
		httpHandlerSpy_first.MCR.assertReturn("getResponseCode", 0, response.responseCode());
		httpHandlerSpy_first.MCR.assertReturn("getResponseBinary", 0,
				response.responseBinary().get());
		assertEquals(response.responseCode(), OK_CODE);
		assertEquals(response.responseText(), "");
		assertTrue(response.createdId().isEmpty());
	}

	@Test
	public void testDownloadNotOk() throws Exception {
		httpHandlerSpy_first.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 500);

		RestResponse response = restClient.download(SOME_TYPE, SOME_ID, SOME_REPRESENTATION);

		assertResponseOnError(response);
	}

	@Test
	public void testRequestNewAuthTokenIfUnauthorized_AllMethodsThatUsesIt() throws Exception {
		testRequestNewAuthTokenIsCalledByMethod(
				() -> restClient.createRecordFromJson(SOME_TYPE, JSON_RECORD));
		testRequestNewAuthTokenIsCalledByMethod(
				() -> restClient.readRecordAsJson(SOME_TYPE, SOME_ID));
		testRequestNewAuthTokenIsCalledByMethod(() -> restClient.readRecordListAsJson(SOME_TYPE));
		testRequestNewAuthTokenIsCalledByMethod(
				() -> restClient.searchRecordWithSearchCriteriaAsJson("someSearchId", "{}"));
		testRequestNewAuthTokenIsCalledByMethod(
				() -> restClient.updateRecordFromJson(SOME_TYPE, SOME_ID, JSON_RECORD));
		testRequestNewAuthTokenIsCalledByMethod(() -> restClient.validateRecordAsJson(JSON_RECORD));
		testRequestNewAuthTokenIsCalledByMethod(() -> restClient.deleteRecord(SOME_TYPE, SOME_ID));
		testRequestNewAuthTokenIsCalledByMethod(
				() -> restClient.download(SOME_TYPE, SOME_ID, SOME_REPRESENTATION));
		testRequestNewAuthTokenIsCalledByMethod(
				() -> restClient.batchIndexWithFilterAsJson("recordTypeToIndex", FILTER));
		testRequestNewAuthTokenIsCalledByMethod(
				() -> restClient.readIncomingLinksAsJson(SOME_TYPE, SOME_ID));
	}

	private void testRequestNewAuthTokenIsCalledByMethod(Supplier<RestResponse> method) {
		setUnauthorizedToFirstHttpHandler();

		RestResponse restResponse = method.get();

		assertRequestNewAuthTokenWhenUnauthorized(restResponse);
		setUp();
	}

	private void setUnauthorizedToFirstHttpHandler() {
		httpHandlerSpy_first.MRV.setAlwaysThrowException(FILTER,
				new RuntimeException("spyException"));
		httpHandlerSpy_first.MRV.setDefaultReturnValuesSupplier("getResponseCode", () -> 401);
	}

	private void assertRequestNewAuthTokenWhenUnauthorized(RestResponse restResponse) {
		tokenClient.MCR.assertMethodWasCalled("requestNewAuthToken");
		httpHandlerSpy_second.MCR.assertMethodWasCalled("getResponseCode");
		assertEquals(restResponse.responseCode(), 200);
	}

}
