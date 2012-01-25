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

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

/**
 * Basic Paid Product implementation
 * 
 * @author Chris Cole
 * 
 */
public class PaidProduct implements IPaidProduct {

	/**
	 * Constants of the JSON object keys used in google Android market
	 * 
	 */
	static class MarketJson {
		public static final String productId = "productId";
		public static final String orders = "orders";
		public static final String nonce = "nonce";
		public static final String purchaseState = "purchaseState";
		public static final String packageName = "packageName";
		public static final String purchaseTime = "purchaseTime";
		public static final String orderId = "orderId";
		public static final String notificationId = "notificationId";
		public static final String developerPayload = "developerPayload";
	}

	public PaidProduct() {
	}

	public PaidProduct(String productID, String extraData) {
		setProductID(productID);
		setProductExtraData(extraData);
	}

	static public ArrayList<PaidProduct> parseAndroidMarketOrder(String jsonMarketData) {
		return parseAndroidMarketOrder(jsonMarketData, false);
	}

	static public ArrayList<PaidProduct> parseAndroidMarketOrder(String jsonMarketData, boolean verified) {
		if (TextUtils.isEmpty(jsonMarketData)) {
			return null;
		}

		JSONObject jobj;
		JSONArray jOrders;
		int tranCount = 0;

		try {
			jobj = new JSONObject(jsonMarketData);
			jOrders = jobj.optJSONArray(MarketJson.orders);
			if (jOrders != null) {
				tranCount = jOrders.length();
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		ArrayList<PaidProduct> prods = new ArrayList<PaidProduct>();

		try {
			for (int i = 0; i < tranCount; i++) {
				JSONObject jord = jOrders.getJSONObject(i);
				PaidProduct pp = new PaidProduct();
				if(pp.loadMarketJson(jord)){
					pp.setVerified(verified);
					prods.add(pp);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return prods;
	}

	/**
	 * product information in the format returned by the Google Android Market
	 * 
	 * @param jsonMarketData
	 */
	public PaidProduct(String jsonMarketData) {
		loadMarketJson(jsonMarketData);
	}

	public boolean loadMarketJson(String json) {
		try {
			JSONObject jdata = new JSONObject(json);
			return loadMarketJson(jdata);
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Initializes this object with JSON in the Android Market format
	 * @param json
	 */
	public boolean loadMarketJson(JSONObject json) {
		int resp;
		boolean debug = true;
		try {
			resp = json.getInt(MarketJson.purchaseState);
			this.purchaseState = PurchaseState.valueOf(resp);
			this.setProductID(json.getString(MarketJson.productId));
			String pkg = json.optString(MarketJson.packageName); //whats this for?
			
			//this.setProductName(json.optString(MarketJson.packageName))/
			this.purchaseTime = json.optLong(MarketJson.purchaseTime);
			this.notificationID = json.optString(MarketJson.notificationId);
			
			if(debug){
				Log.d("JSON", json.toString(4));
			}
			
			
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Initialize with values
	 * 
	 * @param productID
	 * @param name
	 * @param price
	 *            price in pennies USD denominated
	 * @param extraData
	 */
	public PaidProduct(String productID, String name, int price, String extraData) {
		this(productID, extraData);
		setProductPriceCents(price);
		setProductName(name);
	}

	/**
	 * State of the purchase. Can track purchased, cancelled, or refunded
	 */
	public PurchaseState purchaseState;
	public String notificationID;
	public String orderId;
	public long purchaseTime;

	private boolean verified = false;

	/**
	 * True when the signature has been verified
	 * 
	 * @return
	 */
	boolean isVerified() {
		return verified;
	}

	void setVerified(boolean verified) {
		this.verified = verified;
	}

	private String productID;

	private String productName;

	private String productExtraData;

	private int productPriceCents;

	public String getProductID() {
		return productID;
	}

	public void setProductID(String productID) {
		this.productID = productID;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	/**
	 * Developer payload extra data
	 */
	public String getProductExtraData() {
		return productExtraData;
	}

	/**
	 * Developer payload extra data
	 */
	public void setProductExtraData(String productExtraData) {
		this.productExtraData = productExtraData;
	}

	public int getProductPriceCents() {
		return productPriceCents;
	}

	public void setProductPriceCents(int productPriceCents) {
		this.productPriceCents = productPriceCents;
	}

}
