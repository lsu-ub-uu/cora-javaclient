/*
 * Copyright 2018 Uppsala University Library
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

/**
 * TokenClientFactory creates TokenClients from either the combination of loginId and appToken or a
 * valid authToken.
 */
public interface TokenClientFactory {
	/**
	 * factorUsingLoginIdAndAppToken creates a new {@link TokenClient} for the given loginId and
	 * appToken.
	 * 
	 * @param loginId
	 *            String with a valid loginId
	 * @param appToken
	 *            A String with a valid appToken
	 * @return A newly created TokenClient
	 */
	TokenClient factorUsingLoginIdAndAppToken(String loginId, String appToken);

	/**
	 * factorUsingAuthToken creates a new {@link TokenClient} for the given authToken.
	 * 
	 * @param authToken
	 *            A String with a valid authToken
	 * @return A newly created TokenClient
	 */
	TokenClient factorUsingAuthToken(String authToken);
}
