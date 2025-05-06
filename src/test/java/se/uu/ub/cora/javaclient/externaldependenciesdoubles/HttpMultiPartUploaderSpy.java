/*
 * Copyright 2017 Uppsala University Library
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

package se.uu.ub.cora.javaclient.externaldependenciesdoubles;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import se.uu.ub.cora.httphandler.HttpMultiPartUploader;

public class HttpMultiPartUploaderSpy implements HttpMultiPartUploader {

	public HttpURLConnection urlConnection;
	public int responseCode = 200;
	public Map<String, String> headerFields = new HashMap<>();
	public boolean doneIsCalled = false;
	public String fieldName;
	public String fileName;
	public InputStream stream;

	public HttpMultiPartUploaderSpy(HttpURLConnection urlConnection) {
		this.urlConnection = urlConnection;
	}

	public static HttpMultiPartUploaderSpy usingURLConnection(HttpURLConnection urlConnection) {
		return new HttpMultiPartUploaderSpy(urlConnection);
	}

	@Override
	public String getResponseText() {
		if (fileName != null && fileName.equals("correctFileAnswer")) {
			return "{\"record\":{\"data\":{\"children\":[{\"children\":[{\"name\":\"id\",\"value\":\"sound:23310139824886\"},{\"children\":[{\"name\":\"linkedRecordType\",\"value\":\"system\"},{\"name\":\"linkedRecordId\",\"value\":\"cora\"}],\"actionLinks\":{\"read\":{\"requestMethod\":\"GET\",\"rel\":\"read\",\"url\":\"http://localhost:8080/therest/rest/record/system/cora\",\"accept\":\"application/vnd.cora.record+json\"}},\"name\":\"dataDivider\"},{\"name\":\"type\",\"value\":\"sound\"},{\"name\":\"createdBy\",\"value\":\"131313\"}],\"name\":\"recordInfo\"},{\"children\":[{\"children\":[{\"name\":\"streamId\",\"value\":\"soundBinary:23310456970967\"},{\"name\":\"filename\",\"value\":\"adele.png\"},{\"name\":\"filesize\",\"value\":\"8\"},{\"name\":\"mimeType\",\"value\":\"application/octet-stream\"}],\"actionLinks\":{\"read\":{\"requestMethod\":\"GET\",\"rel\":\"read\",\"url\":\"http://localhost:8080/therest/rest/record/sound/sound:23310139824886/master\",\"accept\":\"application/octet-stream\"}},\"name\":\"master\"}],\"name\":\"resourceInfo\"}],\"name\":\"binary\",\"attributes\":{\"type\":\"sound\"}},\"actionLinks\":{\"read\":{\"requestMethod\":\"GET\",\"rel\":\"read\",\"url\":\"http://localhost:8080/therest/rest/record/sound/sound:23310139824886\",\"accept\":\"application/vnd.cora.record+json\"},\"upload\":{\"requestMethod\":\"POST\",\"rel\":\"upload\",\"contentType\":\"multipart/form-data\",\"url\":\"http://localhost:8080/therest/rest/record/sound/sound:23310139824886/master\"},\"update\":{\"requestMethod\":\"POST\",\"rel\":\"update\",\"contentType\":\"application/vnd.cora.record+json\",\"url\":\"http://localhost:8080/therest/rest/record/sound/sound:23310139824886\",\"accept\":\"application/vnd.cora.record+json\"},\"delete\":{\"requestMethod\":\"DELETE\",\"rel\":\"delete\",\"url\":\"http://localhost:8080/therest/rest/record/sound/sound:23310139824886\"}}}}";
		}
		return "Everything ok";
	}

	@Override
	public int getResponseCode() {
		return responseCode;
	}

	@Override
	public String getErrorText() {
		return "bad things happened";
	}

	@Override
	public void addFormField(String name, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addFilePart(String fieldName, String fileName, InputStream stream)
			throws IOException {
		this.fieldName = fieldName;
		this.fileName = fileName;
		this.stream = stream;

	}

	@Override
	public void addHeaderField(String name, String value) {
		headerFields.put(name, value);
	}

	@Override
	public void done() throws IOException {
		// TODO Auto-generated method stub
		doneIsCalled = true;
	}

}
