/*
 * Copyright 2019, 2020 Uppsala University Library
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

import se.uu.ub.cora.clientdata.converter.javatojson.DataToJsonConverterFactory;
import se.uu.ub.cora.clientdata.converter.jsontojava.JsonToDataConverterFactory;
import se.uu.ub.cora.javaclient.apptoken.AppTokenClientFactory;
import se.uu.ub.cora.javaclient.rest.RestClientFactory;

public class CoraClientDependencies {

	public final AppTokenClientFactory appTokenClientFactory;
	public final RestClientFactory restClientFactory;
	public final DataToJsonConverterFactory dataToJsonConverterFactory;
	public final JsonToDataConverterFactory jsonToDataConverterFactory;
	public final String userId;
	public final String appToken;
	public final String authToken;

	public CoraClientDependencies(AppTokenClientFactory appTokenClientFactory,
			RestClientFactory restClientFactory,
			DataToJsonConverterFactory dataToJsonConverterFactory,
			JsonToDataConverterFactory jsonToDataConverterFactory, String userId, String appToken) {
		this.appTokenClientFactory = appTokenClientFactory;
		this.restClientFactory = restClientFactory;
		this.dataToJsonConverterFactory = dataToJsonConverterFactory;
		this.jsonToDataConverterFactory = jsonToDataConverterFactory;
		this.userId = userId;
		this.appToken = appToken;
		authToken = null;
	}

	public CoraClientDependencies(AppTokenClientFactory appTokenClientFactory,
			RestClientFactory restClientFactory,
			DataToJsonConverterFactory dataToJsonConverterFactory,
			JsonToDataConverterFactory jsonToDataConverterFactory, String userId, String appToken,
			String authToken) {
		this.appTokenClientFactory = appTokenClientFactory;
		this.restClientFactory = restClientFactory;
		this.dataToJsonConverterFactory = dataToJsonConverterFactory;
		this.jsonToDataConverterFactory = jsonToDataConverterFactory;
		this.userId = userId;
		this.appToken = appToken;
		this.authToken = authToken;
	}

}
