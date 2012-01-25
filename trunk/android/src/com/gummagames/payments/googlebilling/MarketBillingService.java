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

package com.gummagames.payments.googlebilling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IMarketBillingService;
import com.gummagames.payments.BillingProvider;
import com.gummagames.payments.BillingResponseCode;
import com.gummagames.payments.IPaidProduct;
import com.gummagames.payments.PaidProduct;
import com.gummagames.payments.PurchaseManager;
import com.gummagames.payments.Util;


/**
 * Google Market Billing service object.  Connects to market billing through proxy stub. 
 * @author Chris Cole
 *
 */
public class MarketBillingService extends Service implements ServiceConnection, BillingProvider {

	public static final String LOGTAG = "GUMMAPAYMENTS";

	
	/**
	 * Constants for billing requests that can be sent to MarketBillingService
	 * 
	 * @author Chris Cole
	 * 
	 */
	static final class BillingRequestType {
		public static final String CheckSupported = "CHECK_BILLING_SUPPORTED";
		public static final String Purchase = "REQUEST_PURCHASE";
		public static final String Info = "GET_PURCHASE_INFORMATION";
		public static final String Confirm = "CONFIRM_NOTIFICATIONS";
		public static final String RestoreTransactions = "RESTORE_TRANSACTIONS";

	}

	// private LinkedList<BillingProvider.BillingResponseListener>
	// mEventListeners;

	/**
	 * The list of requests that are pending while we are waiting for the
	 * connection to the MarketBillingService to be established.
	 */
	private static LinkedList<BillingRequest> mPendingRequests = new LinkedList<BillingRequest>();

	/**
	 * The list of requests that we have sent to Android Market but for which we
	 * have not yet received a response code. The HashMap is indexed by the
	 * request Id that each request receives when it executes.
	 */
	private static HashMap<Long, BillingRequest> mSentRequests = new HashMap<Long, BillingRequest>();

	/**
	 * The base class for all requests that use the MarketBillingService. Each
	 * derived class overrides the run() method to call the appropriate service
	 * interface. If we are already connected to the MarketBillingService, then we
	 * call the run() method directly. Otherwise, we bind to the service and save
	 * the request on a queue to be run later when the service is connected.
	 */
	abstract class BillingRequest {
		private final int mStartId;
		protected long mRequestId;

		public BillingRequest(int startId) {
			mStartId = startId;
		}

		public int getStartId() {
			return mStartId;
		}

		/**
		 * Run the request, starting the connection if necessary.
		 * 
		 * @return true if the request was executed or queued; false if there was an
		 *         error starting the connection
		 */
		public boolean runRequest() {
			if (runIfConnected()) {
				return true;
			}

			if (bindToMarketBillingService()) {
				// Add a pending request to run when the service is connected.
				mPendingRequests.add(this);
				return true;
			}
			return false;
		}

		/**
		 * Try running the request directly if the service is already connected.
		 * 
		 * @return true if the request ran successfully; false if the service is not
		 *         connected or there was an error when trying to use it
		 */
		public boolean runIfConnected() {
			if (Consts.DEBUG) {
				Log.d(TAG, getClass().getSimpleName());
			}
			if (mService != null) {
				try {
					mRequestId = run();
					if (Consts.DEBUG) {
						Log.d(TAG, "request id: " + mRequestId);
					}
					if (mRequestId >= 0) {
						mSentRequests.put(mRequestId, this);
					}
					return true;
				} catch (RemoteException e) {
					onRemoteException(e);
				}catch (NullPointerException e){
					Log.w(TAG, "User has not yet accepted Google Market Terms of Service");
				}
			}
			return false;
		}

		/**
		 * Called when a remote exception occurs while trying to execute the
		 * {@link #run()} method. The derived class can override this to execute
		 * exception-handling code.
		 * 
		 * @param e
		 *          the exception
		 */
		protected void onRemoteException(RemoteException e) {
			Log.w(TAG, "remote billing service crashed");
			mService = null;
		}

		/**
		 * The derived class must implement this method.
		 * 
		 * @throws RemoteException
		 */
		abstract protected long run() throws RemoteException;

		/**
		 * This is called when Android Market sends a response code for this
		 * request.
		 * 
		 * @param responseCode
		 *          the response code
		 */
		protected void responseCodeReceived(int responseCode) {
		}

		protected Bundle makeRequestBundle(String method) {
			Bundle request = new Bundle();
			request.putString(Consts.BILLING_REQUEST_METHOD, method);
			request.putInt(Consts.BILLING_REQUEST_API_VERSION, 1);
			request.putString(Consts.BILLING_REQUEST_PACKAGE_NAME, getPackageName());
			return request;
		}

		protected void logResponseCode(String method, Bundle response) {
			int responseCode = response.getInt(Consts.BILLING_RESPONSE_RESPONSE_CODE);
			if (Consts.DEBUG) {
				Log.e(TAG, method + " received " + responseCode);
			}
		}
	}

	/**
	 * Wrapper class that checks if in-app billing is supported.
	 */
	class CheckBillingSupported extends BillingRequest {
		public CheckBillingSupported() {
			// This object is never created as a side effect of starting this
			// service so we pass -1 as the startId to indicate that we should
			// not stop this service after executing this request.
			super(-1);
		}

		@Override
		protected long run() throws RemoteException {
			Bundle request = makeRequestBundle("CHECK_BILLING_SUPPORTED");
			Bundle response = mService.sendBillingRequest(request);
			int responseCode = response.getInt(Consts.BILLING_RESPONSE_RESPONSE_CODE);
			if (Consts.DEBUG) {
				Log.i(TAG, "CheckBillingSupported response code: " + responseCode);
			}
			boolean billingSupported = (responseCode == BillingResponseCode.OK);
			ResponseHandler.checkBillingSupportedResponse(billingSupported);
			return Consts.BILLING_RESPONSE_INVALID_REQUEST_ID;
		}
	}

	/**
	 * Wrapper class that requests a purchase.
	 */
	class RequestPurchase extends BillingRequest {
		public final String mProductId;
		public final String mDeveloperPayload;

		public RequestPurchase(String itemId) {
			this(itemId, null);
		}

		public RequestPurchase(String itemId, String developerPayload) {
			// This object is never created as a side effect of starting this
			// service so we pass -1 as the startId to indicate that we should
			// not stop this service after executing this request.
			super(-1);
			mProductId = itemId;
			
			//Reset if null to avoid NPE from MarketBillingService
			//   http://code.google.com/p/marketbilling/issues/detail?id=25#c6
			
			if(developerPayload == null){
				developerPayload = "";
			}
			mDeveloperPayload = developerPayload;
		}

		@Override
		protected long run() throws RemoteException {
			Bundle request = makeRequestBundle("REQUEST_PURCHASE");
			
			Log.i(LOGTAG, "Running google market payment with productID: " + mProductId);
			
			request.putString(Consts.BILLING_REQUEST_ITEM_ID, mProductId);
			// Note that the developer payload is optional.
			if (mDeveloperPayload != null) {
				request.putString(Consts.BILLING_REQUEST_DEVELOPER_PAYLOAD, mDeveloperPayload);
			}
			Bundle response = mService.sendBillingRequest(request);
			/*
			 * PendingIntent pendingIntent = response
			 * .getParcelable(Consts.BILLING_RESPONSE_PURCHASE_INTENT); if
			 * (pendingIntent == null) { Log.e(TAG, "Error with requestPurchase");
			 * return Consts.BILLING_RESPONSE_INVALID_REQUEST_ID; }
			 * 
			 * Intent intent = new Intent();
			 * ResponseHandler.buyPageIntentResponse(pendingIntent, intent); return
			 * response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID,
			 * Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
			 */

			PendingIntent pendingIntent = response.getParcelable(Consts.BILLING_RESPONSE_PURCHASE_INTENT);
			if (pendingIntent == null) {
				Log.e(TAG, "Error with requestPurchase");
				return Consts.BILLING_RESPONSE_INVALID_REQUEST_ID;
			}

			Intent intent = new Intent();
			ResponseHandler.buyPageIntentResponse(pendingIntent, intent);
			return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID,
					Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);

		}

		@Override
		protected void responseCodeReceived(int responseCode) {
			ResponseHandler.responseCodeReceived(MarketBillingService.this, this, responseCode);
		}
	}

	/**
	 * Wrapper class that confirms a list of notifications to the server.
	 */
	class ConfirmNotifications extends BillingRequest {
		final String[] mNotifyIds;

		public ConfirmNotifications(int startId, String[] notifyIds) {
			super(startId);
			mNotifyIds = notifyIds;
		}

		@Override
		protected long run() throws RemoteException {
			Bundle request = makeRequestBundle("CONFIRM_NOTIFICATIONS");
			request.putStringArray(Consts.BILLING_REQUEST_NOTIFY_IDS, mNotifyIds);
			Bundle response = mService.sendBillingRequest(request);
			logResponseCode("confirmNotifications", response);
			return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID,
					Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
		}
	}

	/**
	 * Wrapper class that sends a GET_PURCHASE_INFORMATION message to the server.
	 */
	class GetPurchaseInformation extends BillingRequest {
		long mNonce;
		final String[] mNotifyIds;

		public GetPurchaseInformation(int startId, String[] notifyIds) {
			super(startId);
			mNotifyIds = notifyIds;
		}

		@Override
		protected long run() throws RemoteException {
			mNonce = Util.generateNonce(); // Security.generateNonce();

			Bundle request = makeRequestBundle("GET_PURCHASE_INFORMATION");
			request.putLong(Consts.BILLING_REQUEST_NONCE, mNonce);
			request.putStringArray(Consts.BILLING_REQUEST_NOTIFY_IDS, mNotifyIds);
			Bundle response = mService.sendBillingRequest(request);
			logResponseCode("getPurchaseInformation", response);
			return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID,
					Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
		}

		@Override
		protected void onRemoteException(RemoteException e) {
			super.onRemoteException(e);

			// Security.removeNonce(mNonce);
		}
	}

	/**
	 * Wrapper class that sends a RESTORE_TRANSACTIONS message to the server.
	 */
	class RestoreTransactions extends BillingRequest {
		long mNonce;

		public RestoreTransactions() {
			// This object is never created as a side effect of starting this
			// service so we pass -1 as the startId to indicate that we should
			// not stop this service after executing this request.
			super(-1);
		}

		@Override
		protected long run() throws RemoteException {
			mNonce = Util.generateNonce();

			Bundle request = makeRequestBundle("RESTORE_TRANSACTIONS");
			request.putLong(Consts.BILLING_REQUEST_NONCE, mNonce);
			Bundle response = mService.sendBillingRequest(request);
			logResponseCode("restoreTransactions", response);
			return response.getLong(Consts.BILLING_RESPONSE_REQUEST_ID,
					Consts.BILLING_RESPONSE_INVALID_REQUEST_ID);
		}

		@Override
		protected void onRemoteException(RemoteException e) {
			super.onRemoteException(e);
			// Security.removeNonce(mNonce);
		}

		@Override
		protected void responseCodeReceived(int responseCode) {
			ResponseHandler.responseCodeReceived(MarketBillingService.this, this, responseCode);
		}
	}

	/*
	 * ******************************************* BEGIN IMPLE
	 * *******************************
	 */

	private static MarketBillingService mInstance = null;

	/**
	 * Singleton instance
	 * 
	 * @return
	 */
	static public MarketBillingService instance() {
		if (mInstance == null) {
			mInstance = new MarketBillingService();
		}
		return mInstance;
	}

	final static boolean debugLog = true;

	private static final String TAG = "BillingService";

	/** The service connection to the remote MarketBillingService. */
	private static IMarketBillingService mService;

	public static IMarketBillingService marketServiceStub() {
		return mService;
	}

	/**
	 * Helper method for building market requests. Accepts values
	 * 
	 * @param method
	 * @return
	 */
	protected Bundle buildRequestBundle(String method) {
		Bundle request = new Bundle();
		request.putString(Consts.BILLING_REQUEST_METHOD, method);
		request.putInt(Consts.BILLING_REQUEST_API_VERSION, 1);
		request.putString(Consts.BILLING_REQUEST_PACKAGE_NAME, getPackageName());
		return request;
	}

	public MarketBillingService() {
		super();
	}

	private boolean mContextBound = false;

	public void setContext(Context context) {
		if (!mContextBound) {
			try {
				attachBaseContext(context);
				mContextBound = true;
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
	}

	public void setActivityContext(Context launchingContext) {
		if (launchingContext != null && launchingContext.getClass().equals(Activity.class)) {
			mLaunchingActivityContext = launchingContext;
		}
	}

	/**
	 * Activity specific context, if available
	 */
	Context mLaunchingActivityContext = null;

	/**
	 * Gets the most isolated context available. First the Activity, then the
	 * application
	 * 
	 * @return
	 */
	public Context getAppropriateContext() {
		if (mLaunchingActivityContext != null) {
			return mLaunchingActivityContext;
		} else {
			return this.getBaseContext();
		}
	}

	@Override
	public void onCreate() {
		this.bindToMarketBillingService();
	}

	/**
	 * We don't support binding to this service, only starting the service.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		handleCommand(intent, startId);
	}

	/**
	 * The {@link MarketBillingReceiver} sends messages to this service using
	 * intents. Each intent has an action and some extra arguments specific to
	 * that action.
	 * 
	 * @param intent
	 *          the intent containing one of the supported actions
	 * @param startId
	 *          an identifier for the invocation instance of this service
	 */
	public void handleCommand(Intent intent, int startId) {
		String action = intent.getAction();
		if (debugLog) {
			Log.i(TAG, "handleCommand() action: " + action);
		}

		if (Consts.ACTION_CONFIRM_NOTIFICATION.equals(action)) {
			String[] notifyIds = intent.getStringArrayExtra(Consts.NOTIFICATION_ID);
			confirmNotifications(startId, notifyIds);
		} else if (Consts.ACTION_GET_PURCHASE_INFORMATION.equals(action)) {
			String notifyId = intent.getStringExtra(Consts.NOTIFICATION_ID);
			getPurchaseInformation(startId, new String[] { notifyId });
		} else if (Consts.ACTION_PURCHASE_STATE_CHANGED.equals(action)) {
			String signedData = intent.getStringExtra(Consts.INAPP_SIGNED_DATA);
			String signature = intent.getStringExtra(Consts.INAPP_SIGNATURE);
			purchaseStateChanged(startId, signedData, signature);
		} else if (Consts.ACTION_RESPONSE_CODE.equals(action)) {
			long requestId = intent.getLongExtra(Consts.INAPP_REQUEST_ID, -1);
			int responseCode = intent.getIntExtra(Consts.INAPP_RESPONSE_CODE, BillingResponseCode.ERROR);
			// ResponseCode responseCode = ResponseCode.valueOf(responseCodeIndex);
			checkResponseCode(requestId, responseCode);
		}

	}

	private void checkResponseCode(long requestId, int responseCode) {
		Log.d(TAG, requestId + ": " + responseCode);

	}

	private void purchaseStateChanged(int startId, String signedData, String signature) {
		ArrayList<PaidProduct> purchases;
		if (PurchaseManager.getConfig().verifySignatureOnClient()) {
			purchases = Auth.clientVerifyPurchase(signedData, signature);
		} else {
			purchases = Auth.decodePurchasesWithoutVerification(signedData);
		}

		if (purchases == null) {
			return;
		}

		ArrayList<String> notifyList = new ArrayList<String>();
		for (PaidProduct prod : purchases) {
			if (prod.notificationID != null) {
				notifyList.add(prod.notificationID);
			}
			ResponseHandler.purchaseResponse(this, prod.purchaseState, prod.getProductID(), prod.orderId,
					prod.purchaseTime, prod.getProductExtraData());
		}
		if (!notifyList.isEmpty()) {
			String[] notifyIds = notifyList.toArray(new String[notifyList.size()]);
			confirmNotifications(startId, notifyIds);
		}
	}

	private boolean getPurchaseInformation(int startId, String[] notifyIds) {
		return new GetPurchaseInformation(startId, notifyIds).runRequest();
	}

	private boolean confirmNotifications(int startId, String[] notifyIds) {
		return new ConfirmNotifications(startId, notifyIds).runRequest();
	}

	/**
	 * Binds to the MarketBillingService and returns true if the bind succeeded.
	 * 
	 * @return true if the bind succeeded; false otherwise
	 */
	boolean bindToMarketBillingService() {
		try {
			if (debugLog) {
				Log.i(TAG, "binding to Market billing service");
			}
			boolean bindResult = bindService(new Intent(Consts.MARKET_BILLING_SERVICE_ACTION), this, // ServiceConnection.
					Context.BIND_AUTO_CREATE);

			if (bindResult) {
				return true;
			} else {
				Log.e(TAG, "Could not bind to service.");
			}
		} catch (SecurityException e) {
			Log.e(TAG, "Security exception: " + e);
		}
		return false;
	}

	/**
	 * Attempts to buy an item from the market
	 */
	public long buyItem(String item) {
		return buyItem(new PaidProduct(item, null));
	}

	/**
	 * Attempts to buy an item from the market Requests that the given item be
	 * offered to the user for purchase. When the purchase succeeds (or is
	 * canceled) the {@link BillingReceiver} receives an intent with the action
	 * {@link Consts#ACTION_NOTIFY}. Returns false if there was an error trying to
	 * connect to Android Market.
	 * 
	 * @param productId
	 *          an identifier for the item being offered for purchase
	 * @param developerPayload
	 *          a payload that is associated with a given purchase, if null, no
	 *          payload is sent
	 * @return false if there was an error connecting to Android Market
	 */
	public long buyItem(IPaidProduct product) {
		
		Log.d(LOGTAG, "Google buy item: " + product.getProductID());

		boolean result = new RequestPurchase(product.getProductID(), product.getProductExtraData())
				.runRequest();

		if (!result) {
			return 0;
		}
		return 1l;

	}

	/**
	 * Runs any pending requests that are waiting for a connection to the service
	 * to be established. This runs in the main UI thread.
	 */
	private void runPendingRequests() {
		int maxStartId = -1;
		BillingRequest request;
		while ((request = mPendingRequests.peek()) != null) {
			if (request.runIfConnected()) {
				// Remove the request
				mPendingRequests.remove();

				// Remember the largest startId, which is the most recent
				// request to start this service.
				if (maxStartId < request.getStartId()) {
					maxStartId = request.getStartId();
				}
			} else {
				// The service crashed, so restart it. Note that this leaves
				// the current request on the queue.
				bindToMarketBillingService();
				return;
			}
		}
	}

	/**
	 * This is called when we are connected to the MarketBillingService. This runs
	 * in the main UI thread.
	 */
	public void onServiceConnected(ComponentName name, IBinder service) {
		mService = IMarketBillingService.Stub.asInterface(service);
		if (debugLog) {
			Log.d(TAG, "Billing service connected");
		}

		runPendingRequests();
	}

	/**
	 * This is called when we are disconnected from the MarketBillingService.
	 */
	public void onServiceDisconnected(ComponentName name) {
		Log.w(TAG, "Billing service disconnected");
		mService = null;
	}

	/**
	 * Unbinds from the MarketBillingService. Call this when the application
	 * terminates to avoid leaking a ServiceConnection.
	 */
	public void unbind() {
		try {
			unbindService(this);
		} catch (IllegalArgumentException e) {
			// This might happen if the service was disconnected
		}
	}

	public boolean isBillingSupported() {

		boolean oldBindTest = true;

		if (oldBindTest) {
			return new CheckBillingSupported().runRequest();
		} else {

			boolean bound = true;
			if (mService == null) {
				bound = this.bindToMarketBillingService();
			}

			if (!bound) {
				return false;
			}

			if (mService == null) {
				try {
					Thread.sleep(100);
					if (mService == null) {
						bound = this.bindToMarketBillingService();
					}
					if (mService == null) {
						Thread.sleep(100);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (mService == null) {
					return false;
				}

			}

			Bundle response = execServiceRequest(BillingRequestType.CheckSupported);
			String val = response.getString(Consts.BILLING_RESPONSE_RESPONSE_CODE);

			boolean haskey = response.containsKey(Consts.BILLING_RESPONSE_RESPONSE_CODE);

			int vi = response.getInt(Consts.BILLING_RESPONSE_RESPONSE_CODE);

			if ("RESULT_OK".equals(val)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Execute a service request internally
	 * 
	 * @param checksupported
	 * @return
	 */
	protected Bundle execServiceRequest(String billingRequestType) {
		Bundle req = buildRequestBundle(billingRequestType);
		Bundle response = null;
		try {
			response = mService.sendBillingRequest(req);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (response == null) {
			response = new Bundle();
		}
		return response;
	}

	private PurchaseObserver mPurchaseObserver = null;

	PurchaseObserver getPurchaseObserver() {
		if (mPurchaseObserver == null) {
			mPurchaseObserver = new PurchaseObserver(this.getAppropriateContext());
			// register with Handler
			ResponseHandler.register(mPurchaseObserver);
		}
		return mPurchaseObserver;
	}

	public void attachBillingResponseListener(BillingResponseListener listener) {
		getPurchaseObserver().attachBillingResponseListener(listener);
	}

	/**
	 * Tests to see if the underlying market billing service is properly
	 * initialized
	 * 
	 * @return
	 */
	public boolean isValid() {
		return (mService != null);
	}

}
