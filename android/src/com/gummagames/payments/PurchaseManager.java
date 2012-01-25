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

import com.gummagames.payments.BillingProvider.BillingResponseListener;
import com.gummagames.payments.googlebilling.MarketBillingService;
import com.gummagames.payments.stub.StubBillingProvider;

/**
 * Wrapper class to abstract and handle in-app purchases.
 * This is the main payment control calss
 * 
 * @author Chris Cole
 * 
 */
public class PurchaseManager {
	
	/**
	 * Hook to the configuration
	 */
	private static BillingConfig config = null;
	


	/**
	 * Configuration for the PurchaseManager
	 * @return
	 */
	public static BillingConfig getConfig() {
		return config;
	}
	public static void setConfig(BillingConfig config) {
		PurchaseManager.config = config;
	}

	private static BillingProvider billingSvc = null;
	
	/**
	 * Initialize the PurchaseManager for use.
	 * @param launchingActivityContext The Activity that is invoking the purchase system
	 * @param configuration Configuration to use for PurchaseManager
	 */
	public static void initialize(Context launchingActivityContext, BillingConfig configuration) {
		
		if (Util.getAppContext() == null) {
			Util.setAppContext(launchingActivityContext.getApplicationContext());
		}
		PurchaseManager.setConfig(configuration);
		
		boolean debug = configuration.isDebug();

		if (configuration.useGoogleMarket() && isGoogleMarketEnabled(launchingActivityContext)) {
			billingSvc = MarketBillingService.instance();
			//Fallback to stub if general failure happens
			if(debug && !((MarketBillingService)billingSvc).isValid()){
				billingSvc = StubBillingProvider.instance();
			}
			
		}
		else if(debug){
			billingSvc = StubBillingProvider.instance();
		}
		if(billingSvc != null){
			billingSvc.setActivityContext(launchingActivityContext);
		}
	}

	static public void attachBillingResponseListener(BillingResponseListener listener) {
		billingSvc.attachBillingResponseListener(listener);
	}

	/**
	 * Billing service types that are supported
	 * 
	 * @author Chris Cole
	 * 
	 */
	static public enum BillingService {
		None, GoogleMarket, Paypal, Amazon
	}

	/**
	 * Currently configured billing service
	 */
	private static BillingService currentBillingService;

	/**
	 * Currently configured billing service
	 * 
	 * @return
	 */
	public static BillingService getCurrentBillingService() {
		return currentBillingService;
	}

	/**
	 * Tests to see if some form of billing is active
	 * 
	 * @return
	 */
	public static BillingService availableBilling() {
		if (!Util.isInternetActive()) {
			return BillingService.None;
		}
		if (isGoogleMarketEnabled()) {
			return BillingService.GoogleMarket;
		}
		return BillingService.None;
	}

	static public boolean isGoogleMarketEnabled() {
		return isGoogleMarketEnabled(Util.getAppContext());
	}

	static public boolean isGoogleMarketEnabled(Context context) {
		if (context == null) {
			return false;
		}
		if (!Util.isInternetActive()) {
			return false;
		}
		MarketBillingService.instance().setContext(context);
		return MarketBillingService.instance().isBillingSupported();
	}

	public static long buyItem(String productID) {
		return billingSvc.buyItem(productID);
	}

	public static long buyItem(IPaidProduct product) {
		return billingSvc.buyItem(product);
	}

	/**
	 * Process a purchase request with the given listener
	 * 
	 * @param product
	 * @param listener
	 * @return
	 */
	public static long buyItem(IPaidProduct product, BillingResponseListener listener) {
		attachBillingResponseListener(listener);
		return billingSvc.buyItem(product);
	}

}
