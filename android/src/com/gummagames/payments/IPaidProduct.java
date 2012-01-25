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
 * Interface for paid products used by billing system
 * @author Chris Cole
 *
 */
public interface IPaidProduct {
	
	/**
	 * Unique identifier for the product
	 * @return
	 */
	String getProductID();
	
	/**
	 * Display name for the product in current locale
	 */
	String getProductName();	
	
	/**
	 * Extra data that might be used in the billing system.
	 * For Google market this is for DEVELOPER_DATA
	 * @param data
	 */
	String getProductExtraData();
	
	/**
	 * Extra data that might be used in the billing system.
	 * For Google market this is for DEVELOPER_DATA
	 * @param data
	 */
	void setProductExtraData(String data);
	
	/**
	 * Product price in USD cents.
	 * i.e. 100 = $1.00
	 * @return
	 */
	int getProductPriceCents();

}
