package se.uu.ub.cora.javaclient;

import se.uu.ub.cora.clientdata.converter.javatojson.DataToJsonConverter;
import se.uu.ub.cora.json.builder.JsonObjectBuilder;

public class DataToJsonConverterSpy extends ClientDataToJsonConverter {

	public String jsonToReturnFromSpy;

	@Override
	public String toJson() {
		jsonToReturnFromSpy = "some json string returned from spy";
		return jsonToReturnFromSpy;
	}

	@Override
	protected JsonObjectBuilder toJsonObjectBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

}
