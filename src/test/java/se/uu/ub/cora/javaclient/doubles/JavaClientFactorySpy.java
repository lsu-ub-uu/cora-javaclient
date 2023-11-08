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

import se.uu.ub.cora.javaclient.JavaClientAppTokenCredentials;
import se.uu.ub.cora.javaclient.JavaClientAuthTokenCredentials;
import se.uu.ub.cora.javaclient.JavaClientFactory;
import se.uu.ub.cora.javaclient.data.DataClient;
import se.uu.ub.cora.javaclient.data.DataClientSpy;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestClientSpy;
import se.uu.ub.cora.testutils.mcr.MethodCallRecorder;
import se.uu.ub.cora.testutils.mrv.MethodReturnValues;

public class JavaClientFactorySpy implements JavaClientFactory {
	public MethodCallRecorder MCR = new MethodCallRecorder();
	public MethodReturnValues MRV = new MethodReturnValues();

	public JavaClientFactorySpy() {
		MCR.useMRV(MRV);
		MRV.setDefaultReturnValuesSupplier("factorRestClientUsingAuthTokenCredentials",
				RestClientSpy::new);
		MRV.setDefaultReturnValuesSupplier("factorRestClientUsingAppTokenCredentials",
				RestClientSpy::new);
		MRV.setDefaultReturnValuesSupplier("factorDataClientUsingAuthTokenCredentials",
				DataClientSpy::new);
		MRV.setDefaultReturnValuesSupplier("factorDataClientUsingAppTokenCredentials",
				DataClientSpy::new);
	}

	@Override
	public RestClient factorRestClientUsingAuthTokenCredentials(
			JavaClientAuthTokenCredentials authTokenCredentials) {
		return (RestClient) MCR.addCallAndReturnFromMRV("authTokenCredentials",
				authTokenCredentials);
	}

	@Override
	public RestClient factorRestClientUsingAppTokenCredentials(
			JavaClientAppTokenCredentials appTokenCredentials) {
		return (RestClient) MCR.addCallAndReturnFromMRV("appTokenCredentials", appTokenCredentials);
	}

	@Override
	public DataClient factorDataClientUsingAuthTokenCredentials(
			JavaClientAuthTokenCredentials authTokenCredentials) {
		return (DataClient) MCR.addCallAndReturnFromMRV("authTokenCredentials",
				authTokenCredentials);
	}

	@Override
	public DataClient factorDataClientUsingAppTokenCredentials(
			JavaClientAppTokenCredentials appTokenCredentials) {
		return (DataClient) MCR.addCallAndReturnFromMRV("appTokenCredentials", appTokenCredentials);
	}

}
