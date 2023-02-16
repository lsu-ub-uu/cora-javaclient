package se.uu.ub.cora.javaclient;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.javaclient.cora.DataClient;
import se.uu.ub.cora.javaclient.cora.DataClientFactoryImp;

public class DataClientRealTest {

	private String apptokenUrl = "https://cora.epc.ub.uu.se/systemone/apptokenverifier/rest/";
	private String baseUrl = "https://cora.epc.ub.uu.se/systemone/rest/";

	@Test
	public void testName() throws Exception {
		DataClientFactoryImp dataClientFactory = DataClientFactoryImp
				.usingAppTokenVerifierUrlAndBaseUrl(apptokenUrl, baseUrl);
		DataClient dataClient = dataClientFactory
				.factorUsingAuthToken("25575bbf-5efe-449f-a570-d674b50e5fa3");

		ClientDataRecord read = dataClient.read("demo", "asdf");
		assertEquals(read.getId(), "");
	}

}
