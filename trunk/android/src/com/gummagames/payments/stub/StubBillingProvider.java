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

package com.gummagames.payments.stub;

import java.util.LinkedList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.gummagames.payments.BillingProvider;
import com.gummagames.payments.BillingResponseCode;
import com.gummagames.payments.IPaidProduct;
import com.gummagames.payments.PaidProduct;

/**
 * Stub billing provider for development use when normal market billing is
 * unavailable, like when running on a Virtual Device
 * 
 * @author Chris Cole
 * 
 */
public class StubBillingProvider implements BillingProvider {

	public static final String LOGTAG = "GUMMAPAYMENTS";

	private LinkedList<BillingProvider.BillingResponseListener> mEventListeners = new LinkedList<BillingProvider.BillingResponseListener>();

	private static StubBillingProvider mInstance = null;

	/**
	 * Singleton instance
	 * 
	 * @return
	 */
	static public StubBillingProvider instance() {
		if (mInstance == null) {
			mInstance = new StubBillingProvider();
		}
		return mInstance;
	}

	public void setActivityContext(Context launchingContext) {
		if (launchingContext != null && launchingContext instanceof Activity) { // a
																																						// !=
																																						// null){
																																						// //
																																						// launchingContext.getClass().isAssignableFrom(Activity.class)){
			mLaunchingActivityContext = launchingContext;
		}
	}

	public Activity myActivity() {
		return (Activity) mLaunchingActivityContext;
	}

	/**
	 * Activity specific context, if available
	 */
	Context mLaunchingActivityContext = null;

	public long buyItem(String item) {
		Log.d(LOGTAG, "Buy product id: " + (item == null ? "null" : item));

		PaidProduct prod = new PaidProduct(item, null);

		return buyItem(prod);

	}

	public long buyItem(final IPaidProduct product) {
		if (myActivity() == null) {
			return 0;
		}

		if (product.getProductID() == null) {
			throw new RuntimeException("Product ID (item) not defined in buy request");
		}

		final StubPaymentDialog dialog = new StubPaymentDialog(myActivity(), "Payment Simulator");
		dialog.setProductId(product.getProductID());
		dialog.show();

		View.OnClickListener buyListener, failListener;

		buyListener = new View.OnClickListener() {
			// @Override
			public void onClick(View view) {
				onPurchaseSuccess(product.getProductID(), 1, 0, null);
				dialog.dismiss();
			}
		};

		failListener = new View.OnClickListener() {
			// @Override
			public void onClick(View view) {
				onPurchaseFailed(product.getProductID(), 1, 0, null);
				dialog.dismiss();
			}
		};
		dialog.attachButtonListeners(buyListener, failListener);

		return 0;
	}

	// @Override
	public void attachBillingResponseListener(BillingResponseListener listener) {
		mEventListeners.add(listener);
	}

	public void onPurchaseSuccess(String itemId, int quantity, long purchaseTime,
			String developerPayload) {

		int purchaseResponseCode = BillingResponseCode.OK;
		PaidProduct pp = new PaidProduct(itemId, developerPayload);

		for (BillingProvider.BillingResponseListener listener : mEventListeners) {

			if (purchaseResponseCode == BillingResponseCode.OK) {
				listener.onPurchaseSuccess(pp, purchaseResponseCode);
			} else {
				listener.onPurchaseFailed(pp, purchaseResponseCode);
			}
		}

	}

	public void onPurchaseFailed(String itemId, int quantity, long purchaseTime,
			String developerPayload) {

		int purchaseResponseCode = BillingResponseCode.SERVICE_UNAVAILABLE;
		PaidProduct pp = new PaidProduct(itemId, developerPayload);

		for (BillingProvider.BillingResponseListener listener : mEventListeners) {

			if (purchaseResponseCode == BillingResponseCode.OK) {
				listener.onPurchaseSuccess(pp, purchaseResponseCode);
			} else {
				listener.onPurchaseFailed(pp, purchaseResponseCode);
			}
		}

	}

}
