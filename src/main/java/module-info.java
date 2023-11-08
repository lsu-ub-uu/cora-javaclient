module se.uu.ub.cora.javaclient {
	requires transitive se.uu.ub.cora.httphandler;
	requires transitive se.uu.ub.cora.clientdata;

	requires java.ws.rs;
	requires se.uu.ub.cora.clientbasicdata;

	exports se.uu.ub.cora.javaclient.token;
	exports se.uu.ub.cora.javaclient.data;
	exports se.uu.ub.cora.javaclient.rest;
}