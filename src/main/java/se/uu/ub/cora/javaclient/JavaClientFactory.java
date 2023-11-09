/*
 * Copyright 2018, 2020 Uppsala University Library
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
import se.uu.ub.cora.javaclient.rest.RestClient;

/**
 * JavaClientFactory factors java clients.
 */
public interface JavaClientFactory {

	/**
	 * factorRestClientUsingAuthTokenCredentials factors a new RestClient using the provided
	 * {@link JavaClientAuthTokenCredentials}
	 * 
	 * @param authTokenCredentials
	 *            A {@link JavaClientAuthTokenCredentials} with information about the server and
	 *            user to use
	 * @return A {@link RestClient} set up according to the {@link JavaClientAuthTokenCredentials}
	 * 
	 */
	RestClient factorRestClientUsingAuthTokenCredentials(
			JavaClientAuthTokenCredentials authTokenCredentials);

	/**
	 * factorRestClientUsingAppTokenCredentials factors a new RestClient using the provided
	 * {@link JavaClientAppTokenCredentials}
	 * 
	 * @param appTokenCredentials
	 *            A {@link JavaClientAppTokenCredentials} with information about the server and user
	 *            to use
	 * @return A {@link RestClient} set up according to the {@link JavaClientAppTokenCredentials}
	 * 
	 */
	RestClient factorRestClientUsingAppTokenCredentials(
			JavaClientAppTokenCredentials appTokenCredentials);

	/**
	 * factorDataClientUsingAuthTokenCredentials factors a new DataClient using the provided
	 * {@link JavaClientAuthTokenCredentials}
	 * 
	 * @param authTokenCredentials
	 *            A {@link JavaClientAuthTokenCredentials} with information about the server and
	 *            user to use
	 * @return A {@link DataClient} set up according to the {@link JavaClientAuthTokenCredentials}
	 * 
	 */
	DataClient factorDataClientUsingAuthTokenCredentials(
			JavaClientAuthTokenCredentials authTokenCredentials);

	/**
	 * factorDataClientUsingAppTokenCredentials factors a new DataClient using the provided
	 * {@link JavaClientAppTokenCredentials}
	 * 
	 * @param appTokenCredentials
	 *            A {@link JavaClientAppTokenCredentials} with information about the server and user
	 *            to use
	 * @return A {@link DataClient} set up according to the {@link JavaClientAppTokenCredentials}
	 * 
	 */
	DataClient factorDataClientUsingAppTokenCredentials(
			JavaClientAppTokenCredentials appTokenCredentials);

}