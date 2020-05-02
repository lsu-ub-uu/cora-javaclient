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
 * RestResponse is used to store information from a HttpResponse.
 */
public class RestResponse {

	public final int statusCode;
	public final String responseText;

	/**
	 * Stores status code, response text, and created id.
	 * 
	 * @param statusCode
	 *            An int representing the status code from the HttpResponse
	 * @param responseText,
	 *            A string representing the response text from the HttpResponse
	 */
	public RestResponse(int statusCode, String responseText) {
		this.statusCode = statusCode;
		this.responseText = responseText;
	}
}
