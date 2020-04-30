package se.uu.ub.cora.javaclient.rest;

public interface RestClientFactory {

	RestClient factorUsingAuthToken(String authToken);

	String getBaseUrl();

}