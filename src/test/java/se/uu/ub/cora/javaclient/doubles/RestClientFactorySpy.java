/*
 * Copyright 2018, 2020, 2023 Uppsala University Library
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

import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestClientFactory;
import se.uu.ub.cora.javaclient.rest.RestClientSpy;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class RestClientFactorySpy implements RestClientFactory {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public RestClientFactorySpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("factorUsingBaseUrlAndAppTokenVerifierUrlAndAuthToken",
				RestClientSpy::new);
		MRV.setDefaultReturnValuesSupplier("factorUsingBaseUrlAndAppTokenUrlAndUserIdAndAppToken",
				RestClientSpy::new);
	}

	@Override
	public RestClient factorUsingBaseUrlAndAppTokenVerifierUrlAndAuthToken(String baseUrl,
			String appTokenUrl, String authToken) {
		return (RestClient) MCR.addCallAndReturnFromMRV("baseUrl", baseUrl, "appTokenUrl",
				appTokenUrl, "authToken", authToken);
	}

	@Override
	public RestClient factorUsingBaseUrlAndAppTokenUrlAndUserIdAndAppToken(String baseUrl,
			String appTokenUrl, String userId, String appToken) {
		return (RestClient) MCR.addCallAndReturnFromMRV("baseUrl", baseUrl, "appTokenUrl",
				appTokenUrl, "userId", userId, "appToken", appToken);
	}

}
