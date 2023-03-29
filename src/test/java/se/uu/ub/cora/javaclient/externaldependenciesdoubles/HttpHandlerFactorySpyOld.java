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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.httphandler.HttpHandler;
import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpMultiPartUploader;

public class HttpHandlerFactorySpyOld implements HttpHandlerFactory {

	public String urlString;
	private boolean factorValid = true;
	public HttpHandlerSpyOLD httpHandlerSpy;
	public HttpHandlerInvalidSpy httpHandlerInvalidSpy;

	private int responseCode = 200;
	public HttpMultiPartUploaderSpy httpMultiPartUploaderSpy;
	public HttpMultiPartUploaderInvalidSpy httpMultiPartUploaderInvalidSpy;

	public List<HttpHandler> factored = new ArrayList<>();

	@Override
	public HttpHandler factor(String urlString) {
		this.urlString = urlString;
		try {
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			if (factorValid) {
				httpHandlerSpy = HttpHandlerSpyOLD.usingURLConnection(urlConnection);
				httpHandlerSpy.responseCode = responseCode;
				factored.add(httpHandlerSpy);
				return httpHandlerSpy;
			}
			httpHandlerInvalidSpy = HttpHandlerInvalidSpy.usingURLConnection(urlConnection);
			factored.add(httpHandlerInvalidSpy);
			return httpHandlerInvalidSpy;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public HttpMultiPartUploader factorHttpMultiPartUploader(String urlString) {
		this.urlString = urlString;
		try {
			URL url = new URL(urlString);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			if (factorValid) {
				httpMultiPartUploaderSpy = HttpMultiPartUploaderSpy
						.usingURLConnection(urlConnection);
				httpMultiPartUploaderSpy.responseCode = responseCode;
				return httpMultiPartUploaderSpy;
			}
			httpMultiPartUploaderInvalidSpy = HttpMultiPartUploaderInvalidSpy
					.usingURLConnection(urlConnection);
			return httpMultiPartUploaderInvalidSpy;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void changeFactoryToFactorInvalidHttpHandlers() {
		factorValid = false;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;

	}

}
