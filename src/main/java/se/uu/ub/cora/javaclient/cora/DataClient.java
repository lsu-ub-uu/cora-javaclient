/*
 * Copyright 2018, 2019, 2020 Uppsala University Library
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
package se.uu.ub.cora.javaclient.cora;

import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataList;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.clientdata.ClientDataRecordGroup;
import se.uu.ub.cora.javaclient.rest.RestClient;

/**
 * DataClient is a java client for handling records in a Cora based system. Methods in this class
 * works with ClientData objects and hides the lower level communication details from the user, in
 * contrast to {@link RestClient} which works with Strings and exposes the communication details to
 * the user of the class.
 */
public interface DataClient {

	/**
	 * Creates a record using recordType and a {@link ClientDataGroup} to create from. The result is
	 * returned as a String. A {@link CoraClientException} MUST be thrown if the record could not be
	 * created.
	 * 
	 * @param recordType
	 *            A String, the type of the record to be created
	 * @param dataRecordGroup
	 *            A {@link ClientDataRecordGroup}, the data to crete the record from
	 * @return A String containing the response text
	 */
	ClientDataRecord create(String recordType, ClientDataRecordGroup dataRecordGroup);

	/**
	 * Reads a record using recordType and recordId. The result is returned as a
	 * {@link ClientDataRecord}. If the record cannot be read a {@link CoraClientException} MUST be
	 * thrown .
	 * 
	 * @param recordType
	 *            A String, the type of the record to read
	 * @param recordId
	 *            A String, the id of the record to be read
	 * @return A {@link ClientDataRecord} created from the response text
	 */
	ClientDataRecord read(String recordType, String recordId);

	/**
	 * Reads records as a list using recordType. The result is returned as a List of
	 * {@link ClientDataRecord}. A {@link CoraClientException} MUST be thrown if records could not
	 * be listed.
	 * 
	 * @param recordType
	 *            A String, the type of the records to read as list
	 * @return A {@link ClientDataList} containing a list with records of the requested type
	 */
	ClientDataList readList(String recordType);

	/**
	 * Updates a record using recordType,recordId and a {@link ClientDataGroup} to update from. The
	 * result is returned as a String. A {@link CoraClientException} MUST be thrown if the record
	 * could not be updated.
	 * 
	 * @param recordType
	 *            A String, the type of the record to be up dated
	 * @param recordId
	 *            A String, the id of the record to be updated
	 * @param dataRecordGroup
	 *            The ClientDataRecordGroup to be updated to
	 * @return A {@link ClientDataGroup} created from the response text
	 */
	ClientDataRecord update(String recordType, String recordId,
			ClientDataRecordGroup dataRecordGroup);

	/**
	 * Deletes a record using recordType and recordId. The result is returned as a String. A
	 * {@link CoraClientException} MUST be thrown if the record could not be deleted.
	 * 
	 * @param recordType
	 *            A String, the type of the record to delete
	 * @param recordId
	 *            A String, the id of the record to be deleted
	 * @return A String containing the response text
	 */
	void delete(String recordType, String recordId);

	/**
	 * Reads incoming links for a record using recordType and recordId. The result is returned as a
	 * String. A {@link CoraClientException} MUST be thrown if incoming links could not be read.
	 * 
	 * @param recordType
	 *            A String, the type of the record to read the incoming links for
	 * @param recordId
	 *            A String, the id of the record to read the incoming links for
	 * @return A {@link ClientDataList}, containing the response text and response code
	 */
	ClientDataList readIncomingLinks(String recordType, String recordId);

	// /**
	// * Indexes a {@link ClientDataRecord}, by sending an index order for the record.
	// *
	// * @param {@link
	// * ClientDataRecord}, the record to index
	// *
	// */
	// String indexData(ClientDataRecord clientDataRecord);
	//
	// /**
	// * Indexes a record by sending an index order for a recordtype and a recordId. When using this
	// * method, no explicit commit is made, which is useful when multiple calls are made within a
	// * short period of time. * @param recordType, A String, the type of the record to index
	// *
	// * @param recordId
	// * A String, the id of the record to index
	// */
	// String indexDataWithoutExplicitCommit(String recordType, String recordId);
	//
	// /**
	// * Removes a record from index, by sending recordType and a recordId.
	// *
	// * @param recordType
	// * A String, the type of the record to remove from index
	// * @param recordId
	// * A String, the id of the record to remove from index
	// */
	// String removeFromIndex(String recordType, String recordId);
	//
	// /**
	// * Indexes, by sending an batch index order for a recordType. The list of records to be
	// indexed
	// * may be limited by a filter.
	// *
	// * @param recordType
	// * A String, the type of the records to index
	// * @param settingsAsJson
	// * A String, with Json specifying a index settings, including a filter that is
	// * applied to the list before indexing
	// * @return A String with the json representation of the newly created indexbatchjob
	// */
	// String indexRecordsOfType(String recordType, String settingsAsJson);

}
