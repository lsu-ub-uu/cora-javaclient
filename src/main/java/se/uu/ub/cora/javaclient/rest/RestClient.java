/*
 * Copyright 2018, 2020 Uppsala University Library
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
package se.uu.ub.cora.javaclient.rest;

import java.io.UnsupportedEncodingException;

import se.uu.ub.cora.javaclient.cora.CoraClient;

/**
 * RestClient is a java client for handling records in a Cora based system. Methods return a rather
 * low level {@link RestResponse} exposing the return codes from the server, when called. In
 * contrast to {@link CoraClient} which hides the communication details from the user of the class.
 */
public interface RestClient {

	/**
	 * Reads a record using recordType and recordId. The result is returned as a responseText in the
	 * {@link RestResponse}
	 * 
	 * @param recordType,
	 *            A String, the type of the record to read
	 * @param recordId,
	 *            A String, the id of the record to be read
	 * @return A {@link RestResponse}, containing the response
	 */
	RestResponse readRecordAsJson(String recordType, String recordId);

	/**
	 * Created a record using recordType and a string to set as output in the httpRequest. The
	 * result is returned as a responseText in the {@link ExtendedRestResponse}
	 * 
	 * @param recordType,
	 *            A String, the type of the record to be created
	 * @param json,
	 *            A String to create the record from
	 * @return A {@link ExtendedRestResponse}, containing the response
	 */
	ExtendedRestResponse createRecordFromJson(String recordType, String json);

	/**
	 * Updates a record using recordType recordId, and a string to set as output in the httpRequest.
	 * The result is returned as a responseText in the {@link RestResponse}
	 * 
	 * @param recordType,
	 *            A String, the type of the record to updated
	 * @param recordId,
	 *            A String, the id of the record to be updated
	 * @param json,
	 *            A String to update the record from
	 * @return A {@link RestResponse}, containing the response
	 */
	RestResponse updateRecordFromJson(String recordType, String recordId, String json);

	/**
	 * Deletes a record using recordType and recordId. The result is returned as a responseText in
	 * the {@link RestResponse}
	 * 
	 * @param recordType,
	 *            A String, the type of the record to delete
	 * @param recordId,
	 *            A String, the id of the record to be delete
	 * @return A {@link RestResponse}, containing the response
	 */
	RestResponse deleteRecord(String recordType, String recordId);

	/**
	 * Read a list of records using recordType. The result is returned as a responseText in the
	 * {@link RestResponse}
	 * 
	 * @param recordType,
	 *            A String, the type of the records to be listed
	 * @return A {@link RestResponse}, containing the response
	 */
	RestResponse readRecordListAsJson(String recordType);

	/**
	 * Reads incoming links for a record using recordType and recordId. The result is returned as a
	 * responseText in the {@link RestResponse}
	 * 
	 * @param recordType,
	 *            A String, the type of the record to read the incoming links for
	 * @param recordId,
	 *            A String, the id of the record to read the incoming links for
	 * @return A {@link RestResponse}, containing the response
	 */
	RestResponse readIncomingLinksAsJson(String recordType, String recordId);

	/**
	 * Read a list of records using recordType and a filter to filter the result. The result is
	 * returned as a responseText in the {@link RestResponse}
	 * 
	 * @param recordType,
	 *            A String, the type of the records to be listed
	 * @param filter,
	 *            A String, a json string to use as a filter to limit the result
	 * @return A {@link RestResponse}, containing the response
	 */
	RestResponse readRecordListWithFilterAsJson(String recordType, String filter)
			throws UnsupportedEncodingException;

	/**
	 * Returns the base url that is expected to be set when the RestClient is instantiated
	 * 
	 * @return A String to use as base url
	 */
	String getBaseUrl();

	/**
	 * Creates an IndexBatchJob for the provided recordType.
	 * 
	 * @param recordType,
	 *            A String, the type of the records to be indexed
	 * 
	 * @param filter
	 *            A JSON-formatted String used to filter the result
	 * @throws UnsupportedEncodingException
	 * 
	 */
	ExtendedRestResponse batchIndexWithFilterAsJson(String recordType, String filterAsJson);

}
