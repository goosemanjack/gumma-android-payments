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

/**
 * Values from the market as response_code extras in the com.android.vending.billing.RESPONSE_CODE broadcast intent. 
 * @author Chris Cole
 *
 */
final public class BillingResponseCode {
	/**
	 * RESULT_OK
	 */
	public static final int OK = 0;
	/**
	 * RESULT_USER_CANCELED
	 */
	public static final int USER_CANCELED = 1;
	/**
	 * RESULT_SERVICE_UNAVAILABLE
	 */
	public static final int SERVICE_UNAVAILABLE = 2;
	/**
	 * RESULT_BILLING_UNAVAILABLE
	 */
	public static final int BILLING_UNAVAILABLE = 3;
	/**
	 * RESULT_ITEM_UNAVAILABLE
	 */
	public static final int ITEM_UNAVAILABLE = 4;
	/**
	 * RESULT_DEVELOPER_ERROR
	 */
	public static final int DEVELOPER_ERROR = 5;
	/**
	 * RESULT_ERROR
	 */
	public static final int ERROR = 6;
	

}
