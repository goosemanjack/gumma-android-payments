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

import android.content.Context;

/**
 * Provider of in-app billing services, such as Google market 
 * @author Chris Cole
 *
 */
public interface BillingProvider {
		
	/**
	 * Initiate a purchase of an item from the billing service
	 * @param item
	 * @return
	 */
	long buyItem(String item);
	
	/**
	 * Initiate a purchase of an item from the billing service
	 * @param product
	 * @return
	 */
	long buyItem(IPaidProduct product);
	
	
	/**
	 * Attaches appropriate event listener for the billing provider
	 * @param listener
	 */
	void attachBillingResponseListener(BillingResponseListener listener);
	
	/**
	 * Mechanism to set the Activity context of the Activity launching the request for billing
	 * @param launchingContext
	 */
	void setActivityContext(Context launchingContext);
	
	
	/**
	 * Event listener interface for interacting with asynchronous billing system.
	 * The billing provider implementation should fire this events
	 *
	 */
	public interface BillingResponseListener{
		
		/**
		 * Event fired after the billing provider answers if its supported or not
		 * @param supported
		 */
		void onBillingSupported(boolean supported);
		
		/**
		 * Event fired after the billing provider (google market or such) returns control to the app
		 * and it was a successful purchase
		 * @param product
		 * @param responseCode
		 */
		void onPurchaseSuccess(PaidProduct product, int responseCode);

		/**
		 * Event fired after on a failed purchase when the billing provider (google market or such) 
		 * returns control to the app.  Failure doesn't always fire, but can include cancel, refund, etc.
		 * @param product
		 * @param responseCode
		 */
		void onPurchaseFailed(PaidProduct product, int responseCode);

	}

}
