/*
 * Copyright 2020 Uppsala University Library
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
package se.uu.ub.cora.javaclient.rest;

/**
 * ExtendedHttpResponse is used to store information from a HttpResponse.
 */
public class ExtendedRestResponse {

	public final int statusCode;
	public final String responseText;
	public final String createdId;

	/**
	 * Stores status code, response text, and created id.
	 * 
	 * @param restResponse
	 *            A {@link RestResponse} which contains the StatusType and the reseponse text
	 * @param createdId,
	 *            A string representing the id of a created record
	 */
	public ExtendedRestResponse(RestResponse restResponse, String createdId) {
		statusCode = restResponse.statusCode;
		responseText = restResponse.responseText;
		this.createdId = createdId;
	}

	/**
	 * Stores status code, response text and empty created id. This constructor is supposed to be
	 * used if the reseponse was not ok, and there is no created id.
	 * 
	 * @param restResponse
	 *            A {@link RestResponse} which contains the Status code and the reseponse text
	 */
	public ExtendedRestResponse(RestResponse restResponse) {
		statusCode = restResponse.statusCode;
		responseText = restResponse.responseText;
		this.createdId = "";
	}

}
