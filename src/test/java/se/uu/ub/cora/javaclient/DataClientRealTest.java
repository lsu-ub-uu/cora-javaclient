/*
 * Copyright 2023 Uppsala University Library
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

public class DataClientRealTest {

	// private String apptokenUrl = "https://localhost:38380/systemone/login/rest/";
	// private String baseUrl = "https://localhost:8080/systemone/rest/";
	// private DataClientFactoryImp dataClientFactory;
	//
	// @BeforeMethod
	// private void beforeMethod() {
	// dataClientFactory = new DataClientFactoryImp();
	// }
	//
	// @Test(enabled = false)
	// public void testRead() throws Exception {
	// DataClient dataClient = dataClientFactory
	// .factorUsingBaseUrlAndAppTokenVerifierUrlAndAuthToken(baseUrl, apptokenUrl,
	// "25575bbf-5efe-449f-a570-d674b50e5fa3");
	//
	// ClientDataRecord read = dataClient.read("demo", "asdf");
	// assertEquals(read.getId(), "asdf");
	// ClientActionLink clientActionLink = read.getActionLink(ClientAction.READ_INCOMING_LINKS)
	// .get();
	// assertEquals(clientActionLink.getURL(),
	// "https://cora.epc.ub.uu.se/systemone/rest/record/demo/asdf/incomingLinks");
	// }
	//
	// @Test(enabled = false)
	// public void testReadList() throws Exception {
	// DataClient dataClient = dataClientFactory.factorUsingRestClient(baseUrl, apptokenUrl,
	// "25575bbf-5efe-449f-a570-d674b50e5fa3");
	//
	// ClientDataList readList = dataClient.readList("demo");
	// assertEquals(readList.getToNo(), "2");
	// ClientDataRecord clientData = (ClientDataRecord) readList.getDataList().get(0);
	// assertEquals(clientData.getId(), "asdf");
	// ClientDataRecord clientData2 = (ClientDataRecord) readList.getDataList().get(1);
	// assertEquals(clientData2.getId(), "sdfsdf");
	//
	// }
	//
	// @Test(enabled = false)
	// public void testUpdate() throws Exception {
	// DataClient dataClient = dataClientFactory
	// .factorUsingBaseUrlAndAppTokenVerifierUrlAndAuthToken(null, null,
	// "25575bbf-5efe-449f-a570-d674b50e5fa3");
	//
	// ClientDataRecord read = dataClient.read("demo", "asdf");
	// ClientDataRecordGroup dataRecordGroup = read.getDataRecordGroup();
	// dataRecordGroup.removeFirstChildWithNameInData("keeptHis");
	// dataRecordGroup
	// .addChild(ClientDataProvider.createAtomicUsingNameInDataAndValue("keeptHis", "3"));
	// dataClient.update("demo", "asdf", dataRecordGroup);
	// }
	//
	// @Test(enabled = false)
	// public void testNameAppToken() throws Exception {
	//
	// DataClient dataClient = dataClientFactory
	// .factorUsingBaseUrlAndAppTokenUrlAndUserIdAndAppToken(null, null,
	// "systemoneAdmin@system.cora.uu.se",
	// "5d3f3ed4-4931-4924-9faa-8eaf5ac6457e");
	// ClientDataRecord read = dataClient.read("demo", "asdf");
	// assertEquals(read.getId(), "asdf");
	// // assertEquals(read.getReadPermissions(), "");
	// }
	//
	// @Test(enabled = false)
	// public void testProvaLite() throws Exception {
	//
	// DataClient dataClient = dataClientFactory
	// .factorUsingBaseUrlAndAppTokenUrlAndUserIdAndAppToken(null, null,
	// "systemoneAdmin@system.cora.uu.se",
	// "5d3f3ed4-4931-4924-9faa-8eaf5ac6457e");
	// ClientDataList readRecordTypes = dataClient.readList("recordType");
	// List<ClientData> listOfRecordTypes = readRecordTypes.getDataList();
	// for (ClientData data : listOfRecordTypes) {
	// ClientDataRecord recordType = (ClientDataRecord) data;
	//
	// ClientDataRecordLink link = (ClientDataRecordLink) recordType.getDataRecordGroup()
	// .getFirstChildWithNameInData("metadataId");
	// System.out.println("metadataId: " + link.getLinkedRecordId());
	// ClientDataRecordLink linkNew = (ClientDataRecordLink) recordType.getDataRecordGroup()
	// .getFirstChildWithNameInData("newMetadataId");
	// System.out.println("new metadataId: " + linkNew.getLinkedRecordId());
	// System.out.println("");
	//
	// ClientDataRecord newMetadataGroupRecord = dataClient.read(linkNew.getLinkedRecordType(),
	// linkNew.getLinkedRecordId());
	// ClientDataRecordGroup newMetadataGroup = newMetadataGroupRecord.getDataRecordGroup();
	// }
	//
	// }
}
