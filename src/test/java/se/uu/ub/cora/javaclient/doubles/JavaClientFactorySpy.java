/*
 * Copyright 2018, 2020, 2023 Uppsala University Library
 * Copyright 2023 Olov McKie
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

import se.uu.ub.cora.javaclient.AppTokenCredentials;
import se.uu.ub.cora.javaclient.AuthTokenCredentials;
import se.uu.ub.cora.javaclient.JavaClientAppTokenCredentials;
import se.uu.ub.cora.javaclient.JavaClientAuthTokenCredentials;
import se.uu.ub.cora.javaclient.JavaClientFactory;
import se.uu.ub.cora.javaclient.TokenClientSpy;
import se.uu.ub.cora.javaclient.data.DataClient;
import se.uu.ub.cora.javaclient.data.DataClientSpy;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestClientSpy;
import se.uu.ub.cora.javaclient.token.TokenClient;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class JavaClientFactorySpy implements JavaClientFactory {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public JavaClientFactorySpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("factorRestClientUsingJavaClientAuthTokenCredentials",
				RestClientSpy::new);
		MRV.setDefaultReturnValuesSupplier("factorRestClientUsingJavaClientAppTokenCredentials",
				RestClientSpy::new);
		MRV.setDefaultReturnValuesSupplier("factorDataClientUsingJavaClientAuthTokenCredentials",
				DataClientSpy::new);
		MRV.setDefaultReturnValuesSupplier("factorDataClientUsingJavaClientAppTokenCredentials",
				DataClientSpy::new);
		MRV.setDefaultReturnValuesSupplier("factorTokenClientUsingAppTokenCredentials",
				TokenClientSpy::new);
		MRV.setDefaultReturnValuesSupplier("factorTokenClientUsingAuthTokenCredentials",
				TokenClientSpy::new);
	}

	@Override
	public RestClient factorRestClientUsingJavaClientAuthTokenCredentials(
			JavaClientAuthTokenCredentials javaClientAuthTokenCredentials) {
		return (RestClient) MCR.addCallAndReturnFromMRV("javaClientAuthTokenCredentials",
				javaClientAuthTokenCredentials);
	}

	@Override
	public RestClient factorRestClientUsingJavaClientAppTokenCredentials(
			JavaClientAppTokenCredentials javaClientAppTokenCredentials) {
		return (RestClient) MCR.addCallAndReturnFromMRV("javaClientAppTokenCredentials",
				javaClientAppTokenCredentials);
	}

	@Override
	public DataClient factorDataClientUsingJavaClientAuthTokenCredentials(
			JavaClientAuthTokenCredentials javaClientAuthTokenCredentials) {
		return (DataClient) MCR.addCallAndReturnFromMRV("javaClientAuthTokenCredentials",
				javaClientAuthTokenCredentials);
	}

	@Override
	public DataClient factorDataClientUsingJavaClientAppTokenCredentials(
			JavaClientAppTokenCredentials javaClientAppTokenCredentials) {
		return (DataClient) MCR.addCallAndReturnFromMRV("javaClientAppTokenCredentials",
				javaClientAppTokenCredentials);
	}

	@Override
	public TokenClient factorTokenClientUsingAppTokenCredentials(
			AppTokenCredentials appTokenCredentials) {
		return (TokenClient) MCR.addCallAndReturnFromMRV("appTokenCredentials",
				appTokenCredentials);
	}

	@Override
	public TokenClient factorTokenClientUsingAuthTokenCredentials(
			AuthTokenCredentials authTokenCredentials) {
		return (TokenClient) MCR.addCallAndReturnFromMRV("authTokenCredentials",
				authTokenCredentials);
	}

}
