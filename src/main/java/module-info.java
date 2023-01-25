module se.uu.ub.cora.javaclient {
	requires transitive se.uu.ub.cora.httphandler;
	requires transitive se.uu.ub.cora.clientdata;

	requires java.ws.rs;
	requires se.uu.ub.cora.clientbasicdata;

	exports se.uu.ub.cora.javaclient.apptoken;
	exports se.uu.ub.cora.javaclient.cora;
	exports se.uu.ub.cora.javaclient.rest;
}