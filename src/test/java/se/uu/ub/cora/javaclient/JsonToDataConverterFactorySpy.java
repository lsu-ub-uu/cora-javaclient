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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.uu.ub.cora.clientdata.ClientData;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverter;
import se.uu.ub.cora.clientdata.converter.JsonToClientDataConverterFactory;
import se.uu.ub.cora.clientdata.converter.jsontojava.JsonToDataActionLinkConverter;
import se.uu.ub.cora.json.parser.JsonValue;

public class JsonToDataConverterFactorySpy implements JsonToClientDataConverterFactory {

	public boolean createForJsonObjectWasCalled = false;
	public JsonValue jsonValue;
	public JsonToDataConverterSpy factoredConverter;
	public List<JsonToDataConverterSpy> factoredConverters = new ArrayList<>();
	public List<ClientData> actionLinksToReturn = Collections.emptyList();
	private int numOfCallsTocreateJsonToDataActionLink = 0;

	@Override
	public JsonToClientDataConverter createForJsonObject(JsonValue jsonValue) {
		this.jsonValue = jsonValue;
		createForJsonObjectWasCalled = true;
		factoredConverter = new JsonToDataConverterSpy();
		factoredConverters.add(factoredConverter);
		return factoredConverter;
	}

	@Override
	public JsonToClientDataConverter createForJsonString(String json) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonToDataActionLinkConverter createActionLinksConverterForJsonString(String json) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonToDataActionLinkConverter createJsonToDataActionLinkConverterForJsonObject(
			JsonValue jsonValue) {
		JsonToDataActionLinkConverterSpy jsonToDataActionLinkConverterSpy = new JsonToDataActionLinkConverterSpy();
		if (!actionLinksToReturn.isEmpty()) {
			jsonToDataActionLinkConverterSpy.actionLinkToReturn = actionLinksToReturn
					.get(numOfCallsTocreateJsonToDataActionLink);
		}
		numOfCallsTocreateJsonToDataActionLink++;
		return jsonToDataActionLinkConverterSpy;
	}

}
