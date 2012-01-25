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

package com.gummagames.payments.googlebilling;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.gummagames.payments.KeyInitializationException;
import com.gummagames.payments.PaidProduct;
import com.gummagames.payments.PurchaseManager;

class Auth {

	static String LOGTAG = "GUMMAPAYMENTS";
	private static final String KEY_FACTORY_ALGORITHM = "RSA";
	private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
	private static final SecureRandom RANDOM = new SecureRandom();
	
	static boolean strictCheck = false;

	static private PublicKey publicKey = null;

	/**
	 * Get the current public key value
	 * 
	 * @return
	 */
	static PublicKey getPublicKey() {
		if (publicKey == null) {
			publicKey = generatePublicKey(PurchaseManager.getConfig().getPublicKey());
			if(publicKey == null){
				String message = "You must provide the Google Market Public Key to the PurchaseManager config at initialization";
				if(strictCheck){
					throw new KeyInitializationException(message);
				}
				else{
					Log.w(LOGTAG, message);
				}
			}
		}
		return publicKey;
	}

	void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	/**
	 * Decodes the market JSON without performing client signature verification
	 * @param signedData
	 * @return
	 */
	public static ArrayList<PaidProduct> decodePurchasesWithoutVerification(String signedData) {
		return PaidProduct.parseAndroidMarketOrder(signedData, false);	
	}

	/**
	 * Verify the purchase signatures
	 * 
	 * @param signedData
	 * @param signature
	 * @return
	 */
	public static ArrayList<PaidProduct> clientVerifyPurchase(String signedData, String signature) {
		boolean verified = verify(getPublicKey(), signedData, signature);
		if(!verified){
			return null;
		}
		else{		
			return PaidProduct.parseAndroidMarketOrder(signedData, verified);
		}
	}

	/**
	 * Verifies that the signature from the server matches the computed
	 * signature on the data. Returns true if the data is correctly signed.
	 * 
	 * @param publicKey
	 *            public key associated with the developer account
	 * @param signedData
	 *            signed data from server
	 * @param signature
	 *            server signature
	 * @return true if the data and signature match
	 */
	public static boolean verify(PublicKey publicKey, String signedData, String signature) {
		if (Consts.DEBUG) {
			Log.i(LOGTAG, "signature: " + signature);
		}
		if(publicKey == null){
			if(!PurchaseManager.getConfig().isDebug()){
				return false;
			}
			else{
				return true;
			}
		}
		
		Signature sig;
		try {
			sig = Signature.getInstance(SIGNATURE_ALGORITHM);
			sig.initVerify(publicKey);
			sig.update(signedData.getBytes());
			if (!sig.verify(Base64.decode(signature, Base64.DEFAULT))) {
				Log.e(LOGTAG, "Signature verification failed.");
				return false;
			}
			return true;
		} catch (IllegalArgumentException e) {
			Log.e(LOGTAG, "Invalid key specification.");
		} catch (SignatureException e) {
			Log.e(LOGTAG, "Signature exception.");
		} catch (Exception e) {
			Log.e(LOGTAG, "Base64 decoding failed.");
		}
		return false;
	}

	/**
	 * Generates a PublicKey instance from a string containing the
	 * Base64-encoded public key.
	 * 
	 * @param encodedPublicKey
	 *            Base64-encoded public key
	 * @throws IllegalArgumentException
	 *             if encodedPublicKey is invalid
	 */
	public static PublicKey generatePublicKey(String encodedPublicKey) {
		if(TextUtils.isEmpty(encodedPublicKey)){
			return null;
		}
		
		try {
			byte[] decodedKey = Base64.decode(encodedPublicKey, Base64.DEFAULT);
			KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
			return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeySpecException e) {
			Log.e(LOGTAG, "Invalid key specification.");
			throw new IllegalArgumentException(e);
		} catch (Exception e) {
			Log.e(LOGTAG, "Base64 decoding failed.");
			throw new IllegalArgumentException(e);
		}
	}

}
