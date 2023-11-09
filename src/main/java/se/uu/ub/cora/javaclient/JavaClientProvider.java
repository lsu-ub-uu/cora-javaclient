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

/**
 * JavaClientProvider provides a means for other classes in the system to create instances of
 * different java clients.
 * <p>
 * JavaClientProvider has a number of static methods that start with createX that is intended to be
 * used to create all instances of java clients in the system.
 * <p>
 * To help with testing is there a metod
 * {@link JavaClientProvider#onlyForTestSetJavaClientFactory(JavaClientFactory)} that makes it
 * possible to change the implementing java clients while testing.
 */
public class JavaClientProvider {

	private JavaClientProvider() {
		// not called
		throw new UnsupportedOperationException();
	}

	private static JavaClientFactory javaClientFactory = new JavaClientFactoryImp();
	private static JavaClientFactory onlyForTestJavaClientFactory;

	/**
	 * createRestClientUsingAuthTokenCredentials creates a {@link RestClient} from a
	 * {@link JavaClientAuthTokenCredentials}
	 * 
	 * @param authTokenCredentials
	 *            A {@link JavaClientAuthTokenCredentials} to use for setting up the created client
	 * @return A {@link RestClient} set up with the information from the provided
	 *         {@link JavaClientAuthTokenCredentials}
	 */
	public static RestClient createRestClientUsingAuthTokenCredentials(
			JavaClientAuthTokenCredentials authTokenCredentials) {
		return getJavaClientFactory()
				.factorRestClientUsingAuthTokenCredentials(authTokenCredentials);
	}

	/**
	 * createRestClientUsingAppTokenCredentials creates a {@link RestClient} from a
	 * {@link JavaClientAppTokenCredentials}
	 * 
	 * @param authTokenCredentials
	 *            A {@link JavaClientAppTokenCredentials} to use for setting up the created client
	 * @return A {@link RestClient} set up with the information from the provided
	 *         {@link JavaClientAppTokenCredentials}
	 */
	public static RestClient createRestClientUsingAppTokenCredentials(
			JavaClientAppTokenCredentials appTokenCredentials) {
		return getJavaClientFactory().factorRestClientUsingAppTokenCredentials(appTokenCredentials);
	}

	/**
	 * createDataClientUsingAuthTokenCredentials creates a {@link DataClient} from a
	 * {@link JavaClientAuthTokenCredentials}
	 * 
	 * @param authTokenCredentials
	 *            A {@link JavaClientAuthTokenCredentials} to use for setting up the created client
	 * @return A {@link DataClient} set up with the information from the provided
	 *         {@link JavaClientAuthTokenCredentials}
	 */
	public static DataClient createDataClientUsingAuthTokenCredentials(
			JavaClientAuthTokenCredentials authTokenCredentials) {
		return getJavaClientFactory()
				.factorDataClientUsingAuthTokenCredentials(authTokenCredentials);
	}

	/**
	 * createDataClientUsingAppTokenCredentials creates a {@link DataClient} from a
	 * {@link JavaClientAppTokenCredentials}
	 * 
	 * @param authTokenCredentials
	 *            A {@link JavaClientAppTokenCredentials} to use for setting up the created client
	 * @return A {@link DataClient} set up with the information from the provided
	 *         {@link JavaClientAppTokenCredentials}
	 */
	public static DataClient createDataClientUsingAppTokenCredentials(
			JavaClientAppTokenCredentials appTokenCredentials) {
		return getJavaClientFactory().factorDataClientUsingAppTokenCredentials(appTokenCredentials);
	}

	private static JavaClientFactory getJavaClientFactory() {
		if (onlyForTestJavaClientFactory == null) {
			return javaClientFactory;
		}
		return onlyForTestJavaClientFactory;
	}

	/**
	 * onlyForTestSetJavaClientFactory sets a JavaClientFactory that will be used to factor java
	 * clients when other classes needs to create new instances. This possibility to set a
	 * JavaClientFactory is provided to enable testing of client creation in other classes and is
	 * not intented to be used in production.
	 * 
	 * @param javaClientFactory
	 *            A JavaClientFactory to use to create java client classes for testing
	 */

	public static void onlyForTestSetJavaClientFactory(JavaClientFactory javaClientFactory) {
		JavaClientProvider.onlyForTestJavaClientFactory = javaClientFactory;
	}
}
