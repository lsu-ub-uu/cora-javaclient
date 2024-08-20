/*
 * Copyright 2018, 2024 Uppsala University Library
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
package se.uu.ub.cora.javaclient.token;

import se.uu.ub.cora.javaclient.JavaClientProvider;
import se.uu.ub.cora.javaclient.data.DataClientException;

/**
 * TokenClient is responsible for always beeing able to return a valid authToken. TokenClients
 * SHOULD be created using {@link JavaClientProvider}.
 */
public interface TokenClient {
	/**
	 * getAuthToken returns a valid authToken.
	 * 
	 * @throws DataClientException
	 *             is thrown if fetching the authToken from server fails
	 * 
	 * @return A String with a valid authToken
	 */
	String getAuthToken();

	/**
	 * renewAuthToken request a new authToken if the token client has been set up with an appToken,
	 * otherwise is an error thrown.
	 * 
	 * @throws DataClientException
	 *             is thrown if the initialized without an appToken or fetching the authToken from
	 *             server fails
	 */
	void requestNewAuthToken();
}
