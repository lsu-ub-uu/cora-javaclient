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
package se.uu.ub.cora.javaclient.doubles;

import java.util.ArrayList;
import java.util.List;

import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestClientFactory;

public class RestClientFactorySpy implements RestClientFactory {

	public String baseUrl;
	public String authToken;
	public RestClientSpyOld restClientSpy;
	public List<RestClientSpyOld> factored = new ArrayList<>();
	public String usedAuthToken;

	@Override
	public RestClient factorUsingAuthToken(String authToken) {
		this.authToken = authToken;
		this.usedAuthToken = authToken;
		restClientSpy = new RestClientSpyOld();
		factored.add(restClientSpy);
		return restClientSpy;
	}

	@Override
	public String getBaseUrl() {
		return "http://localhost:8080/therest/rest/record/";
	}

}
