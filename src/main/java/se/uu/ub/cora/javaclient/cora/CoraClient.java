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

import java.util.List;

import se.uu.ub.cora.clientdata.ClientDataGroup;
import se.uu.ub.cora.clientdata.ClientDataRecord;
import se.uu.ub.cora.javaclient.rest.RestClient;
import se.uu.ub.cora.javaclient.rest.RestResponse;

/**
 * CoraClient is a java client for handling records in a Cora based system. Methods in this class
 * hides the lower level communication details from the user, in contrast to {@link RestClient}
 * which exposes the communication details to the user of the class.
 */
public interface CoraClient {

	/**
	 * Creates a record using recordType and a string to create from. The result is returned as a
	 * String. A {@link CoraClientException} MUST be thrown if the record could not be created.
	 * 
	 * @param recordType,
	 *            A String, the type of the record to be created
	 * @param json,
	 *            A String, the data to crete the record from
	 * @return A String containing the response text
	 */
	String create(String recordType, String json);

	/**
	 * Creates a record using recordType and a {@link ClientDataGroup} to create from. The result is
	 * returned as a String. A {@link CoraClientException} MUST be thrown if the record could not be
	 * created.
	 * 
	 * @param recordType,
	 *            A String, the type of the record to be created
	 * @param dataGroup,
	 *            A {@link ClientDataGroup}, the data to crete the record from
	 * @return A String containing the response text
	 */
	String create(String recordType, ClientDataGroup dataGroup);

	/**
	 * Reads a record using recordType and recordId. The result is returned as a String. A
	 * {@link CoraClientException} MUST be thrown if the record could not be read.
	 * 
	 * @param recordType,
	 *            A String, the type of the record to read
	 * @param recordId,
	 *            A String, the id of the record to be read
	 * @return A String containing the response text
	 */
	String read(String recordType, String recordId);

	/**
	 * Updates a record using recordType, recordId and a string to update from. The result is
	 * returned as a String. A {@link CoraClientException} MUST be thrown if the record could not be
	 * updated.
	 * 
	 * @param recordType,
	 *            A String, the type of the record to be updated
	 * @param recordId,
	 *            A String, the id of the record to be updated
	 * @param json,
	 *            A String, the data to update the record from
	 * @return A String containing the response text
	 */
	String update(String recordType, String recordId, String json);

	/**
	 * Deletes a record using recordType and recordId. The result is returned as a String. A
	 * {@link CoraClientException} MUST be thrown if the record could not be deleted.
	 * 
	 * @param recordType,
	 *            A String, the type of the record to delete
	 * @param recordId,
	 *            A String, the id of the record to be deleted
	 * @return A String containing the response text
	 */
	String delete(String recordType, String recordId);

	/**
	 * Reads records as a list using recordType. The result is returned as a String. A
	 * {@link CoraClientException} MUST be thrown if records could not be listed.
	 * 
	 * @param recordType,
	 *            A String, the type of the records to read as list
	 * @return A String containing the response text
	 */
	String readList(String recordType);

	/**
	 * Reads incoming links for a record using recordType and recordId. The result is returned as a
	 * String. A {@link CoraClientException} MUST be thrown if incoming links could not be read.
	 * 
	 * @param recordType,
	 *            A String, the type of the record to read the incoming links for
	 * @param recordId,
	 *            A String, the id of the record to read the incoming links for
	 * @return A {@link RestResponse}, containing the response text and response code
	 */
	String readIncomingLinks(String recordType, String recordId);

	/**
	 * Reads a record using recordType and recordId. The result is returned as a
	 * {@link ClientDataRecord} A {@link CoraClientException} MUST be thrown if the record could not
	 * be read.
	 * 
	 * @param recordType,
	 *            A String, the type of the record to read
	 * @param recordId,
	 *            A String, the id of the record to be read
	 * @return A {@link ClientDataRecord} created from the response text
	 */
	ClientDataRecord readAsDataRecord(String recordType, String recordId);

	/**
	 * Updates a record using recordType,recordId and a {@link ClientDataGroup} to update from. The
	 * result is returned as a String. A {@link CoraClientException} MUST be thrown if the record
	 * could not be updated.
	 * 
	 * @param recordType,
	 *            A String, the type of the record to be up dated
	 * @param recordId,
	 *            A String, the id of the record to be updated
	 * @param json,
	 *            A String, the data to update the record from
	 * @return A {@link ClientDataGroup} created from the response text
	 */
	String update(String recordType, String recordId, ClientDataGroup dataGroup);

	/**
	 * Reads records as a list using recordType. The result is returned as a List of
	 * {@link ClientDataRecord}. A {@link CoraClientException} MUST be thrown if records could not
	 * be listed.
	 * 
	 * @param recordType,
	 *            A String, the type of the records to read as list
	 * @return A List of {@link ClientDataRecord} containing the records of the requested recordType
	 */
	List<ClientDataRecord> readListAsDataRecords(String recordType);

	/**
	 * Indexes a {@link ClientDataRecord}, by sending an index order for the record.
	 * 
	 * @param {@link
	 *            ClientDataRecord}, the record to index
	 * 
	 */
	String indexData(ClientDataRecord clientDataRecord);

	/**
	 * Indexes a record, by sending an index order for a recordType and a recordId.
	 * 
	 * @param recordType,
	 *            A String, the type of the record to index
	 * @param recordId,
	 *            A String, the id of the record to index
	 */
	String indexData(String recordType, String recordId);

}
