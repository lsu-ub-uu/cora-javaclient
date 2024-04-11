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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Optional;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DataClientExceptionTest {
	Exception e;

	@BeforeMethod
	private void beforeMethod() {
		e = new Exception("some message");
	}

	@Test
	public void testInit() {
		DataClientException notAuthenticated = DataClientException.withMessage("message");

		Assert.assertEquals(notAuthenticated.getMessage(), "message");
	}

	@Test
	public void testWithMessageAndException() throws Exception {
		DataClientException exception = DataClientException
				.withMessageAndException("second message", e);

		assertEquals(exception.getMessage(), "second message");
		assertEquals(exception.getCause().getMessage(), "some message");
	}

	@Test
	public void testGetResponseCode_NoneResponseCodeSet() throws Exception {
		DataClientException exception = DataClientException.withMessageAndException("message", e);

		Optional<Integer> responseCode = exception.getResponseCode();

		assertTrue(responseCode.isEmpty());
	}

	@Test
	public void testGetResponseCode_WithResponseCodeAndExceptionSet() throws Exception {
		DataClientException exception = DataClientException
				.withMessageAndResponseCodeAndException("message", 401, e);

		Optional<Integer> responseCode = exception.getResponseCode();

		assertEquals(responseCode.get(), 401);
	}

	@Test
	public void testGetResponseCode_WithOnlyResponseCodeSet() throws Exception {
		DataClientException exception = DataClientException.withMessageAndResponseCode("message",
				401);

		Optional<Integer> responseCode = exception.getResponseCode();

		assertEquals(responseCode.get(), 401);
	}
}
