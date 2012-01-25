//  Copyright 2011-2012 Gumma Corp.
//  
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//  
//     http://www.apache.org/licenses/LICENSE-2.0
//  
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//     See the License for the specific language governing permissions and
//     limitations under the License.

package com.gummagames.payments;


import java.util.Random;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Utility
 * @author Chris Cole
 *
 */
public class Util {
	private static Context appContext = null;
	static Random randomGenerator = new Random();

	public static void setAppContext(Context appContext) {
		Util.appContext = appContext;
	}

	/**
	 * A static reference to the ApplicationContext
	 * 
	 * @return
	 */
	public static Context getAppContext() {
		if (appContext == null) {
			// appContext = MyApplication.getAppContext();
		}
		return appContext;
	}


	/**
	 * Checks if we have a valid Internet Connection on the device.
	 * 
	 * @return True if device has internet connectivity
	 * 
	 *         Code modified from: http://www.androidsnippets.org/snippets/131/
	 */
	static public boolean isInternetActive() {

		ConnectivityManager connectivity = (ConnectivityManager) appContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo info = (NetworkInfo) connectivity.getActiveNetworkInfo();

		if (info == null || !info.isConnected()) {
			return false;
		}
		if (info.isRoaming()) {
			return false;
		}
		return true;
	}
	
    /**
     * Generate a random number used only once.
     * @return
     */
    public static long generateNonce() {
        long nonce = randomGenerator.nextLong();
        //sKnownNonces.add(nonce);
        return nonce;
    }

    /*
    public static void removeNonce(long nonce) {
        sKnownNonces.remove(nonce);
    }

    public static boolean isNonceKnown(long nonce) {
        return sKnownNonces.contains(nonce);
    }
*/	

}
