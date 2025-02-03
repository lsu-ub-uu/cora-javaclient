/*
 * Copyright 2023, 2025 Uppsala University Library
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

/**
 * AuthTokenCredentials contains information needed to start a TokenClient using a currently valid
 * auth token. This implies that TokenClient will keep the authToken renewed, until authToken
 * "renewUntil" is reached.
 * <p>
 * When using this constructor SHOULD tokenIsRenewable be set to true and the renewAuthTokenUrl
 * contain the entire renew url, including the tokenId returned when the token was created.
 * 
 * @param renewAuthTokenUrl
 *            The complete url for the actionLink renew, including tokenId.
 * @param authToken
 *            The token from a valid authenication.
 * @param tokenIsRenewable
 *            a boolean if the token is renewable or not, if the renewAuthTokenUrl contains needed
 *            tokenId
 */
public record AuthTokenCredentials(String authTokenRenewUrl, String authToken,
		boolean tokenIsRenewable) {

	/**
	 * AuthTokenCredentials contains information needed to start a TokenClient using a currently
	 * valid auth token.
	 * <p>
	 * This constructor will set tokenIsRenewable to false leading to the client getting
	 * unauthorized as soon as the token runs out, usually a few minutes.
	 * 
	 * @param renewAuthTokenUrl
	 *            Value for this parameter is not used, but only here to avoid breaking the api.
	 * @param authToken
	 *            The token from a valid authenication.
	 * @deprecated Depricated, use the constructor with tokenIsRenewable instead
	 */
	@Deprecated
	public AuthTokenCredentials(String renewAuthTokenUrl, String authToken) {
		this(renewAuthTokenUrl, authToken, false);
	}
}