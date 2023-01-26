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

package se.uu.ub.cora.javaclient;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import se.uu.ub.cora.javaclient.externaldependenciesdoubles.HttpHandlerFactorySpyOld;
import se.uu.ub.cora.javaclient.externaldependenciesdoubles.HttpHandlerInvalidSpy;
import se.uu.ub.cora.javaclient.externaldependenciesdoubles.HttpHandlerSpyOLD;
import se.uu.ub.cora.javaclient.rest.ExtendedRestResponse;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestResponse;
import se.uu.ub.cora.javaclient.rest.http.RestClientImp;

public class RestClientTest {
	private HttpHandlerFactorySpyOld httpHandlerFactorySpy;
	private RestClient restClient;

	@BeforeMethod
	public void setUp() {
		httpHandlerFactorySpy = new HttpHandlerFactorySpyOld();
		String baseUrl = "http://localhost:8080/therest/rest/";
		String authToken = "someToken";
		restClient = RestClientImp.usingHttpHandlerFactoryAndBaseUrlAndAuthToken(
				httpHandlerFactorySpy, baseUrl, authToken);
	}

	@Test
	public void testReadRecordHttpHandlerSetupCorrectly() {
		restClient.readRecordAsJson("someType", "someId");
		assertEquals(getRequestMethod(), "GET");
		assertEquals(httpHandlerFactorySpy.urlString,
				"http://localhost:8080/therest/rest/record/someType/someId");
		assertEquals(getRequestProperty("authToken"), "someToken");
		assertEquals(getNumberOfRequestProperties(), 1);
	}

	@Test
	public void testReadRecordOk() {
		RestResponse response = restClient.readRecordAsJson("someType", "someId");
		HttpHandlerSpyOLD httpHandler = (HttpHandlerSpyOLD) httpHandlerFactorySpy.factored.get(0);
		assertEquals(response.responseText, httpHandler.returnedResponseText);
		assertEquals(response.statusCode, httpHandler.responseCode);
	}

	// @Test(expectedExceptions = CoraClientException.class, expectedExceptionsMessageRegExp = ""
	// + "Could not read record of type: someType and id: someId from server using "
	// + "url: http://localhost:8080/therest/rest/record/someType/someId. Returned error was: "
	// + "bad things happened")
	@Test
	public void testReadRecordNotOk() {
		httpHandlerFactorySpy.changeFactoryToFactorInvalidHttpHandlers();
		RestResponse response = restClient.readRecordAsJson("someType", "someId");

		HttpHandlerInvalidSpy httpHandler = (HttpHandlerInvalidSpy) httpHandlerFactorySpy.factored
				.get(0);
		assertNotNull(response.responseText);
		assertEquals(response.responseText, httpHandler.returnedErrorText);
		assertEquals(response.statusCode, httpHandler.responseCode);
	}

	@Test
	public void testReadRecordListHttpHandlerSetupCorrectly() {
		restClient.readRecordListAsJson("someType");
		assertEquals(getRequestMethod(), "GET");
		assertEquals(httpHandlerFactorySpy.urlString,
				"http://localhost:8080/therest/rest/record/someType");
		assertEquals(getRequestProperty("authToken"), "someToken");
		assertEquals(getNumberOfRequestProperties(), 1);
	}

	@Test
	public void testReadRecordListOk() {
		RestResponse response = restClient.readRecordListAsJson("someType");
		HttpHandlerSpyOLD httpHandler = (HttpHandlerSpyOLD) httpHandlerFactorySpy.factored.get(0);
		assertEquals(response.responseText, httpHandler.returnedResponseText);
		assertEquals(response.statusCode, httpHandler.responseCode);
	}

	// @Test(expectedExceptions = CoraClientException.class, expectedExceptionsMessageRegExp = ""
	// + "Could not read records of type: someType from server using "
	// + "url: http://localhost:8080/therest/rest/record/someType. Returned error was: "
	// + "bad things happened")
	@Test
	public void testReadRecordListNotOk() {
		httpHandlerFactorySpy.changeFactoryToFactorInvalidHttpHandlers();
		RestResponse response = restClient.readRecordListAsJson("someType");
		HttpHandlerInvalidSpy httpHandler = (HttpHandlerInvalidSpy) httpHandlerFactorySpy.factored
				.get(0);
		assertNotNull(response.responseText);
		assertEquals(response.responseText, httpHandler.returnedErrorText);
		assertEquals(response.statusCode, httpHandler.responseCode);
	}

	@Test
	public void testReadRecordListWithFilterHttpHandlerSetupCorrectly()
			throws UnsupportedEncodingException {
		String filterAsJson = "{\"name\":\"filter\",\"children\":[{\"name\":\"part\",\"children\":[{\"name\":\"key\",\"value\":\"idFromLogin\"},{\"name\":\"value\",\"value\":\"someId\"}],\"repeatId\":\"0\"}]}";

		restClient.readRecordListWithFilterAsJson("someType", filterAsJson);
		assertEquals(getRequestMethod(), "GET");

		String encodedJson = URLEncoder.encode(filterAsJson, "UTF-8");
		assertEquals(httpHandlerFactorySpy.urlString,
				"http://localhost:8080/therest/rest/record/someType?filter=" + encodedJson);

		assertEquals(getRequestProperty("authToken"), "someToken");
		assertEquals(getNumberOfRequestProperties(), 1);
	}

	@Test
	public void testReadRecordListWithFilterOk() throws UnsupportedEncodingException {
		String filterAsJson = "{\"name\":\"filter\",\"children\":[{\"name\":\"part\",\"children\":[{\"name\":\"key\",\"value\":\"idFromLogin\"},{\"name\":\"value\",\"value\":\"someId\"}],\"repeatId\":\"0\"}]}";
		RestResponse response = restClient.readRecordListWithFilterAsJson("someType", filterAsJson);
		HttpHandlerSpyOLD httpHandler = (HttpHandlerSpyOLD) httpHandlerFactorySpy.factored.get(0);
		assertEquals(response.responseText, httpHandler.returnedResponseText);
		assertEquals(response.statusCode, httpHandler.responseCode);
	}

	@Test
	public void testReadRecordListWithFilterNotOk() throws UnsupportedEncodingException {
		httpHandlerFactorySpy.changeFactoryToFactorInvalidHttpHandlers();
		String filterAsJson = "{\"name\":\"filter\",\"children\":[{\"name\":\"part\",\"children\":[{\"name\":\"key\",\"value\":\"idFromLogin\"},{\"name\":\"value\",\"value\":\"someId\"}],\"repeatId\":\"0\"}]}";
		RestResponse response = restClient.readRecordListWithFilterAsJson("someType", filterAsJson);
		HttpHandlerInvalidSpy httpHandler = (HttpHandlerInvalidSpy) httpHandlerFactorySpy.factored
				.get(0);
		assertNotNull(response.responseText);
		assertEquals(response.responseText, httpHandler.returnedErrorText);
		assertEquals(response.statusCode, httpHandler.responseCode);
	}

	@Test
	public void testCreateRecordHttpHandlerSetupCorrectly() throws Exception {
		httpHandlerFactorySpy.setResponseCode(201);

		String json = "{\"name\":\"value\"}";
		restClient.createRecordFromJson("someType", json);

		assertEquals(getRequestMethod(), "POST");
		assertEquals(httpHandlerFactorySpy.urlString,
				"http://localhost:8080/therest/rest/record/someType");
		assertEquals(getRequestProperty("authToken"), "someToken");
		assertEquals(getRequestProperty("Accept"), "application/vnd.uub.record+json");
		assertEquals(getRequestProperty("Content-Type"), "application/vnd.uub.record+json");
		assertEquals(getNumberOfRequestProperties(), 3);

		assertEquals(getOutputString(), "{\"name\":\"value\"}");
	}

	@Test
	public void testCreateRecordOk() throws Exception {
		httpHandlerFactorySpy.setResponseCode(201);
		String json = "{\"name\":\"value\"}";
		ExtendedRestResponse response = restClient.createRecordFromJson("someType", json);
		HttpHandlerSpyOLD httpHandler = (HttpHandlerSpyOLD) httpHandlerFactorySpy.factored.get(0);
		assertEquals(response.responseText, httpHandler.returnedResponseText);
		assertEquals(response.statusCode, httpHandler.responseCode);
	}

	// @Test(expectedExceptions = CoraClientException.class, expectedExceptionsMessageRegExp = ""
	// + "Could not create record of type: someType from server using "
	// + "url: http://localhost:8080/therest/rest/record/someType. Returned error was: "
	// + "bad things happened")
	@Test
	public void testCreateRecordNotOk() throws Exception {
		httpHandlerFactorySpy.changeFactoryToFactorInvalidHttpHandlers();
		String json = "{\"name\":\"value\"}";
		ExtendedRestResponse response = restClient.createRecordFromJson("someType", json);
		HttpHandlerInvalidSpy httpHandler = (HttpHandlerInvalidSpy) httpHandlerFactorySpy.factored
				.get(0);
		assertNotNull(response.responseText);
		assertEquals(response.responseText, httpHandler.returnedErrorText);
		assertEquals(response.statusCode, httpHandler.responseCode);
		assertEquals(response.createdId, "");
	}

	@Test
	public void testCreateRecordOkWithCreatedId() {
		httpHandlerFactorySpy.setResponseCode(201);
		String json = "{\"name\":\"value\"}";
		ExtendedRestResponse response = restClient.createRecordFromJson("someType", json);
		HttpHandlerSpyOLD httpHandler = (HttpHandlerSpyOLD) httpHandlerFactorySpy.factored.get(0);
		assertEquals(response.responseText, httpHandler.returnedResponseText);
		assertEquals(response.statusCode, httpHandler.responseCode);

		String returnedHeaderFromSpy = httpHandler.returnedHeaderField;
		assertEquals(response.createdId,
				returnedHeaderFromSpy.substring(returnedHeaderFromSpy.lastIndexOf('/') + 1));
	}

	@Test
	public void testUpdateRecordHttpHandlerSetupCorrectly() throws Exception {
		httpHandlerFactorySpy.setResponseCode(200);

		String json = "{\"name\":\"value\"}";
		restClient.updateRecordFromJson("someType", "someId", json);

		assertEquals(getRequestMethod(), "POST");
		assertEquals(httpHandlerFactorySpy.urlString,
				"http://localhost:8080/therest/rest/record/someType/someId");
		assertEquals(getRequestProperty("authToken"), "someToken");
		assertEquals(getRequestProperty("Accept"), "application/vnd.uub.record+json");
		assertEquals(getRequestProperty("Content-Type"), "application/vnd.uub.record+json");
		assertEquals(getNumberOfRequestProperties(), 3);

		assertEquals(getOutputString(), "{\"name\":\"value\"}");
	}

	@Test
	public void testUpdateRecordOk() {
		String json = "{\"name\":\"value\"}";
		RestResponse response = restClient.updateRecordFromJson("someType", "someId", json);
		HttpHandlerSpyOLD httpHandler = (HttpHandlerSpyOLD) httpHandlerFactorySpy.factored.get(0);
		assertEquals(response.responseText, httpHandler.returnedResponseText);
		assertEquals(response.statusCode, httpHandler.responseCode);
	}

	// @Test(expectedExceptions = CoraClientException.class, expectedExceptionsMessageRegExp = ""
	// + "Could not update record of type: someType with recordId: someId on server using "
	// + "url: http://localhost:8080/therest/rest/record/someType/someId. Returned error was: "
	// + "bad things happened")
	@Test
	public void testUpdateRecordNotOk() throws Exception {
		httpHandlerFactorySpy.changeFactoryToFactorInvalidHttpHandlers();
		String json = "{\"name\":\"value\"}";
		RestResponse response = restClient.updateRecordFromJson("someType", "someId", json);

		HttpHandlerInvalidSpy httpHandler = (HttpHandlerInvalidSpy) httpHandlerFactorySpy.factored
				.get(0);
		assertNotNull(response.responseText);
		assertEquals(response.responseText, httpHandler.returnedErrorText);
		assertEquals(response.statusCode, httpHandler.responseCode);

	}

	@Test
	public void testDeleteRecordHttpHandlerSetupCorrectly() throws Exception {
		httpHandlerFactorySpy.setResponseCode(200);

		restClient.deleteRecord("someType", "someId");

		assertEquals(getRequestMethod(), "DELETE");
		assertEquals(httpHandlerFactorySpy.urlString,
				"http://localhost:8080/therest/rest/record/someType/someId");
		assertEquals(getRequestProperty("authToken"), "someToken");
		assertEquals(getNumberOfRequestProperties(), 1);

	}

	@Test
	public void testDeleteRecordOk() throws Exception {
		httpHandlerFactorySpy.setResponseCode(200);
		RestResponse response = restClient.deleteRecord("someType", "someId");
		HttpHandlerSpyOLD httpHandler = (HttpHandlerSpyOLD) httpHandlerFactorySpy.factored.get(0);
		assertEquals(response.responseText, httpHandler.returnedResponseText);
		assertEquals(response.statusCode, httpHandler.responseCode);
	}

	// @Test(expectedExceptions = CoraClientException.class, expectedExceptionsMessageRegExp = ""
	// + "Could not delete record of type: someType and id: someId from server using "
	// + "url: http://localhost:8080/therest/rest/record/someType/someId. Returned error was: "
	// + "bad things happened")
	@Test
	public void testDeleteRecordNotOk() {
		httpHandlerFactorySpy.changeFactoryToFactorInvalidHttpHandlers();
		RestResponse response = restClient.deleteRecord("someType", "someId");
		HttpHandlerInvalidSpy httpHandler = (HttpHandlerInvalidSpy) httpHandlerFactorySpy.factored
				.get(0);
		assertNotNull(response.responseText);
		assertEquals(response.responseText, httpHandler.returnedErrorText);
		assertEquals(response.statusCode, httpHandler.responseCode);
	}

	@Test
	public void testReadIncomingLinksHttpHandlerSetupCorrectly() {
		restClient.readIncomingLinksAsJson("someType", "someId");
		assertEquals(getRequestMethod(), "GET");
		assertEquals(httpHandlerFactorySpy.urlString,
				"http://localhost:8080/therest/rest/record/someType/someId/incomingLinks");
		assertEquals(getRequestProperty("authToken"), "someToken");
		assertEquals(getNumberOfRequestProperties(), 1);
	}

	@Test
	public void testReadIncomingLinksOk() {
		RestResponse response = restClient.readIncomingLinksAsJson("someType", "someId");
		HttpHandlerSpyOLD httpHandler = (HttpHandlerSpyOLD) httpHandlerFactorySpy.factored.get(0);
		assertEquals(response.responseText, httpHandler.returnedResponseText);
		assertEquals(response.statusCode, httpHandler.responseCode);
		// String json = restClient.readIncomingLinksAsJson("someType", "someId");
		// assertEquals(json, "Everything ok");
	}

	// @Test(expectedExceptions = CoraClientException.class, expectedExceptionsMessageRegExp =
	// "Could not read "
	// + "incoming links of type: someType from server using "
	// + "url: http://localhost:8080/therest/rest/record/someType/someId/incomingLinks. "
	// + "Returned error was: bad things happened")
	@Test
	public void testReadIncomingLinksNotOk() {
		httpHandlerFactorySpy.changeFactoryToFactorInvalidHttpHandlers();
		RestResponse response = restClient.readIncomingLinksAsJson("someType", "someId");
		HttpHandlerInvalidSpy httpHandler = (HttpHandlerInvalidSpy) httpHandlerFactorySpy.factored
				.get(0);
		assertNotNull(response.responseText);
		assertEquals(response.responseText, httpHandler.returnedErrorText);
		assertEquals(response.statusCode, httpHandler.responseCode);
	}

	@Test
	public void testBatchIndexWithFilterHttpHandlerSetupCorrectly()
			throws UnsupportedEncodingException {
		httpHandlerFactorySpy.setResponseCode(201);

		String filterAsJson = "{\"name\":\"filter\",\"children\":[{\"name\":\"part\",\"children\":[{\"name\":\"key\",\"value\":\"idFromLogin\"},{\"name\":\"value\",\"value\":\"someId\"}],\"repeatId\":\"0\"}]}";
		restClient.batchIndexWithFilterAsJson("recordTypeToIndex", filterAsJson);
		assertEquals(getRequestMethod(), "POST");

		assertEquals(httpHandlerFactorySpy.urlString,
				"http://localhost:8080/therest/rest/record/index/recordTypeToIndex");

		assertEquals(getRequestProperty("Accept"), "application/vnd.uub.record+json");
		assertEquals(getRequestProperty("Content-Type"), "application/vnd.uub.record+json");

		assertEquals(getRequestProperty("authToken"), "someToken");
		assertEquals(getNumberOfRequestProperties(), 3);

		assertEquals(getOutputString(), filterAsJson);

	}

	@Test
	public void testBatchIndexWithFilterOk() throws UnsupportedEncodingException {
		httpHandlerFactorySpy.setResponseCode(201);

		String filterAsJson = "{\"name\":\"filter\",\"children\":[{\"name\":\"part\",\"children\":[{\"name\":\"key\",\"value\":\"idFromLogin\"},{\"name\":\"value\",\"value\":\"someId\"}],\"repeatId\":\"0\"}]}";
		ExtendedRestResponse response = restClient.batchIndexWithFilterAsJson("recordTypeToIndex",
				filterAsJson);

		HttpHandlerSpyOLD httpHandler = (HttpHandlerSpyOLD) httpHandlerFactorySpy.factored.get(0);
		assertEquals(response.responseText, "indexBatchJobAsJson");
		assertEquals(response.statusCode, httpHandler.responseCode);

	}

	@Test
	public void testBatchIndexWithFilterNotOk() throws UnsupportedEncodingException {
		httpHandlerFactorySpy.changeFactoryToFactorInvalidHttpHandlers();

		String filterAsJson = "{\"name\":\"filter\",\"children\":[{\"name\":\"part\",\"children\":[{\"name\":\"key\",\"value\":\"idFromLogin\"},{\"name\":\"value\",\"value\":\"someId\"}],\"repeatId\":\"0\"}]}";
		ExtendedRestResponse response = restClient.batchIndexWithFilterAsJson("recordTypeToIndex",
				filterAsJson);

		HttpHandlerInvalidSpy httpHandler = (HttpHandlerInvalidSpy) httpHandlerFactorySpy.factored
				.get(0);

		assertNotNull(response.responseText);
		assertEquals(response.responseText, httpHandler.returnedErrorText);
		assertEquals(response.statusCode, httpHandler.responseCode);

	}

	@Test
	public void testBatchIndexWithFilterOkWithCreatedId() throws UnsupportedEncodingException {
		httpHandlerFactorySpy.setResponseCode(201);

		String filterAsJson = "{\"name\":\"filter\",\"children\":[{\"name\":\"part\",\"children\":[{\"name\":\"key\",\"value\":\"idFromLogin\"},{\"name\":\"value\",\"value\":\"someId\"}],\"repeatId\":\"0\"}]}";
		ExtendedRestResponse response = restClient.batchIndexWithFilterAsJson("recordTypeToIndex",
				filterAsJson);

		HttpHandlerSpyOLD httpHandler = (HttpHandlerSpyOLD) httpHandlerFactorySpy.factored.get(0);
		assertEquals(response.responseText, "indexBatchJobAsJson");
		assertEquals(response.statusCode, httpHandler.responseCode);

		String returnedHeaderFromSpy = httpHandler.returnedHeaderField;
		assertEquals(response.createdId,
				returnedHeaderFromSpy.substring(returnedHeaderFromSpy.lastIndexOf('/') + 1));

	}

	private String getOutputString() {
		return httpHandlerFactorySpy.httpHandlerSpy.outputString;
	}

	private String getRequestMethod() {
		return httpHandlerFactorySpy.httpHandlerSpy.requestMetod;
	}

	private int getNumberOfRequestProperties() {
		return httpHandlerFactorySpy.httpHandlerSpy.requestProperties.size();
	}

	private String getRequestProperty(String key) {
		return httpHandlerFactorySpy.httpHandlerSpy.requestProperties.get(key);
	}
}
