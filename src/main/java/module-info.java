module se.uu.ub.cora.javaclient {
	requires transitive se.uu.ub.cora.httphandler;
	requires transitive se.uu.ub.cora.clientdata;

	requires java.ws.rs;

	exports se.uu.ub.cora.javaclient;
	exports se.uu.ub.cora.javaclient.data;
	exports se.uu.ub.cora.javaclient.rest;
	exports se.uu.ub.cora.javaclient.token;
}