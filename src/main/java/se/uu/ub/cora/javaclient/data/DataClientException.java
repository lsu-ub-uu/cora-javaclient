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
package se.uu.ub.cora.javaclient.data;

import java.util.Optional;

public class DataClientException extends RuntimeException {
	private static final long serialVersionUID = -3141384493591308355L;
	private final Integer responseCode;

	private DataClientException(String message) {
		super(message);
		this.responseCode = null;
	}

	private DataClientException(String message, Exception e) {
		super(message, e);
		this.responseCode = null;
	}

	public DataClientException(String message, int responseCode, Exception exception) {
		super(message, exception);
		this.responseCode = responseCode;
	}

	public static DataClientException withMessage(String message) {
		return new DataClientException(message);
	}

	public static DataClientException withMessageAndException(String message, Exception e) {
		return new DataClientException(message, e);
	}

	public Optional<Integer> getResponseCode() {
		return Optional.ofNullable(responseCode);
	}

	public static DataClientException withMessageAndResponseCodeAndException(String message,
			int responseCode, Exception exception) {
		return new DataClientException(message, responseCode, exception);
	}
}