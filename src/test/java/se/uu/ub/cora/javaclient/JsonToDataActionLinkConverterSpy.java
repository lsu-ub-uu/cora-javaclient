package se.uu.ub.cora.javaclient;

import se.uu.ub.cora.clientdata.ActionLink;
import se.uu.ub.cora.clientdata.ClientData;
import se.uu.ub.cora.clientdata.converter.jsontojava.JsonToDataActionLinkConverter;

public class JsonToDataActionLinkConverterSpy implements JsonToDataActionLinkConverter {

	public ClientData actionLinkToReturn = null;

	@Override
	public ClientData toInstance() {
		if (actionLinkToReturn == null) {
			return ActionLink.withAction(Action.READ);

		}
		return actionLinkToReturn;
	}

}
