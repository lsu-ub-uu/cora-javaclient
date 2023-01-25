/*
 * Copyright 2019 Uppsala University Library
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

import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverter;
import se.uu.ub.cora.clientdata.converter.ClientDataToJsonConverterFactory;
import se.uu.ub.cora.clientdata.converter.javatojson.Convertible;

public class DataToJsonConverterFactorySpy implements ClientDataToJsonConverterFactory {

	public Convertible clientDataElement;
	public DataToJsonConverterSpy converterSpy;
	public boolean includeActionLinks = true;
	public String methodCalled = "";

	@Override
	public ClientDataToJsonConverter createForClientDataElement(Convertible clientDataElement) {
		this.clientDataElement = clientDataElement;
		methodCalled = "createForClientDataElement";
		converterSpy = new DataToJsonConverterSpy();
		return converterSpy;
	}

	@Override
	public ClientDataToJsonConverter createForClientDataElementIncludingActionLinks(
			Convertible clientDataElement, boolean includeActionLinks) {
		this.clientDataElement = clientDataElement;
		this.includeActionLinks = includeActionLinks;
		methodCalled = "createForClientDataElementIncludingActionLinks";
		converterSpy = new DataToJsonConverterSpy();
		return converterSpy;
	}

}
