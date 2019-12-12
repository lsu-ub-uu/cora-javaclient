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

import se.uu.ub.cora.clientdata.ClientDataElement;
import se.uu.ub.cora.clientdata.converter.javatojson.DataToJsonConverter;
import se.uu.ub.cora.clientdata.converter.javatojson.DataToJsonConverterFactory;
import se.uu.ub.cora.json.builder.JsonBuilderFactory;

public class DataToJsonConverterFactorySpy implements DataToJsonConverterFactory {

	public JsonBuilderFactory factory;
	public ClientDataElement clientDataElement;
	public DataToJsonConverterSpy converterSpy;
	public boolean includeActionLinks = true;
	public String methodCalled = "";

	@Override
	public DataToJsonConverter createForClientDataElement(JsonBuilderFactory factory,
			ClientDataElement clientDataElement) {
		this.factory = factory;
		this.clientDataElement = clientDataElement;
		methodCalled = "createForClientDataElement";
		converterSpy = new DataToJsonConverterSpy();
		return converterSpy;
	}

	@Override
	public DataToJsonConverter createForClientDataElementIncludingActionLinks(
			JsonBuilderFactory factory, ClientDataElement clientDataElement,
			boolean includeActionLinks) {
		this.factory = factory;
		this.clientDataElement = clientDataElement;
		this.includeActionLinks = includeActionLinks;
		methodCalled = "createForClientDataElementIncludingActionLinks";
		converterSpy = new DataToJsonConverterSpy();
		return converterSpy;
	}

}
