/*
 * Copyright 2023 Uppsala University Library
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
package se.uu.ub.cora.javaclient;

import se.uu.ub.cora.javaclient.data.DataClient;
import se.uu.ub.cora.javaclient.internal.JavaClientFactoryImp;
import se.uu.ub.cora.javaclient.rest.RestClient;

public class JavaClientProvider {

	private static JavaClientFactory javaClientFactory = new JavaClientFactoryImp();
	private static JavaClientFactory onlyForTestJavaClientFactory;

	public static RestClient getRestClientUsingAuthTokenCredentials(
			JavaClientAuthTokenCredentials authTokenCredentials) {
		return getJavaClientFactory()
				.factorRestClientUsingAuthTokenCredentials(authTokenCredentials);
	}

	public static RestClient getRestClientUsingAppTokenCredentials(
			JavaClientAppTokenCredentials appTokenCredentials) {
		return getJavaClientFactory().factorRestClientUsingAppTokenCredentials(appTokenCredentials);
	}

	public static DataClient getDataClientUsingAuthTokenCredentials(
			JavaClientAuthTokenCredentials authTokenCredentials) {
		return getJavaClientFactory()
				.factorDataClientUsingAuthTokenCredentials(authTokenCredentials);
	}

	public static DataClient getDataClientUsingAppTokenCredentials(
			JavaClientAppTokenCredentials appTokenCredentials) {
		return getJavaClientFactory().factorDataClientUsingAppTokenCredentials(appTokenCredentials);
	}

	private static JavaClientFactory getJavaClientFactory() {
		if (onlyForTestJavaClientFactory == null) {
			return javaClientFactory;
		}
		return onlyForTestJavaClientFactory;
	}

	public static void onlyForTestSetJavaClientFactory(JavaClientFactory javaClientFactory) {
		JavaClientProvider.onlyForTestJavaClientFactory = javaClientFactory;

	}
}
