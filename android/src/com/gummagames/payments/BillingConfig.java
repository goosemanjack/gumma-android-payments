/*  Copyright 2011-2012 Gumma Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 */

package com.gummagames.payments;

import android.os.Bundle;

/**
 * Holds configuration settings for the billing system
 * @author Chris Cole
 *
 */
public class BillingConfig {
	
	/**
	 * Public key used by current payment provider
	 */
	private String publicKey;
	
	/**
	 * Bundle to hold additional information
	 */
	private Bundle bundle = null;
	
	/**
	 * Flag for performing client (in-app) verification of encryption
	 */
	private boolean performClientVerification = true;
	
	private boolean debug = false;
	
	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	/**
	 * Add extra information to the extra bundle
	 * @param key
	 * @param value
	 */
	public void addExtra(String key, String value){
		if(bundle == null){
			bundle = new Bundle();
		}
		bundle.putString(key, value);
	}
	/**
	 * Bundle of any extra information
	 * @return
	 */
	public Bundle extra(){
		if(bundle == null){
			bundle = new Bundle();
		}
		return bundle;
	}
	

	/**
	 * Public key used by the market
	 * @return
	 */
	public String getPublicKey() {
		return publicKey;
	}

	/**
	 * Public key used by the market.
	 * In the case of Google Android Market, 
	 * Public Key value is found under the "Edit Profile" link
	 * 
	 * @param publicKey
	 */
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	/**
	 * Indicates of client-side (in-app) signature verification should be used
	 * @return
	 */
	public boolean verifySignatureOnClient() {
		return performClientVerification;
	}

	/**
	 * Sets client (in-app) signature verification on or off
	 * @param performClientVerification
	 */
	public void setPerformClientVerification(boolean verify) {
		this.performClientVerification = verify;
	}
	
	private boolean useGoogleMarket = true;
	
	/**
	 * Call this to enable or disable usage of Google market.
	 * You must call initialize after changing this flag
	 */
	public void disableGoogleMarket(){
		useGoogleMarket = false;
	}
	/**
	 * Call this to enable or disable usage of Google market.
	 * You must call initialize after changing this flag
	 */
	public void enableGoogleMarket(){
		useGoogleMarket = true;
	}
	/**
	 * Call this to enable or disable usage of Google market.
	 * You must call initialize after changing this flag
	 */
	public void setGoogleMarketEnabled(boolean enabled){
		useGoogleMarket = enabled;
	}
	
	public boolean useGoogleMarket(){
		return useGoogleMarket;
	}
	


}
