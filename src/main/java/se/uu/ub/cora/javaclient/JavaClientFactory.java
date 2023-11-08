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
 * RestClientFactory factors RestClient. The RestClientFactory implementation is expected to be
 * instantiated with a base url.
 */
public interface JavaClientFactory {

	/**
	 * Factors a RestClient using an authToken
	 * 
	 * @param authTokenCredentials
	 *            TODO
	 * @param authToken,
	 *            a String to use as an authToken
	 * 
	 * @return {@link RestClient}
	 */
	RestClient factorRestClientUsingAuthTokenCredentials(
			JavaClientAuthTokenCredentials authTokenCredentials);

	/**
	 * factorUsingUserIdAndAppToken factors a RestClient using the specified userId and appToken
	 * 
	 * @param appTokenCredentials
	 *            TODO
	 * @param userId,
	 *            a String with the userId
	 * @param appToken,
	 *            a String with a valid appToken
	 * @return {@link RestClient}
	 */
	RestClient factorRestClientUsingAppTokenCredentials(
			JavaClientAppTokenCredentials appTokenCredentials);

	DataClient factorDataClientUsingAuthTokenCredentials(
			JavaClientAuthTokenCredentials authTokenCredentials);

	DataClient factorDataClientUsingAppTokenCredentials(
			JavaClientAppTokenCredentials appTokenCredentials);

}