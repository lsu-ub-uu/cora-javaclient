/*
 * Copyright 2018 Uppsala University Library
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
package se.uu.ub.cora.javaclient.apptoken;

import se.uu.ub.cora.httphandler.HttpHandlerFactory;
import se.uu.ub.cora.httphandler.HttpHandlerFactoryImp;
import se.uu.ub.cora.javaclient.apptoken.http.AppTokenClientImp;
import se.uu.ub.cora.javaclient.cora.http.AppTokenClientCredentials;

public class AppTokenClientFactoryImp implements AppTokenClientFactory {

	private String appTokenVerifierUrl;

	public AppTokenClientFactoryImp(String appTokenVerifierUrl) {
		this.appTokenVerifierUrl = appTokenVerifierUrl;
	}

	@Override
	public AppTokenClient factor(String userId, String appToken) {
		HttpHandlerFactory httpHandlerFactory = new HttpHandlerFactoryImp();
		AppTokenClientCredentials credentials = new AppTokenClientCredentials(appTokenVerifierUrl,
				userId, appToken);
		return AppTokenClientImp.usingHttpHandlerFactoryAndCredentials(httpHandlerFactory,
				credentials);
	}

	public String getAppTokenVerifierUrl() {
		// needed for test
		return appTokenVerifierUrl;
	}

}
