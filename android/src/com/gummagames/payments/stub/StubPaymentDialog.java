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


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gummagames.payments.R;


public class StubPaymentDialog extends Dialog  {
	
	private String productId;
	
	
	public String getProductId() {
		return productId;
	}


	public void setProductId(String productId) {
		this.productId = productId;
	}


	public StubPaymentDialog(Activity owner, String title){
		super(owner);
		this.setOwnerActivity(owner);
		this.setTitle(title);
		
	}
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stub_payment);
		
		TextView tv = (TextView)findViewById(R.id.textItemId);
		tv.setText(String.format("Product ID: %s", getProductId()));

	}
	
	public void attachButtonListeners(View.OnClickListener buyListener, View.OnClickListener failListener){
		
		final Button buyBtn = (Button) findViewById(R.id.BtnBuy);
		final Button failBtn = (Button) findViewById(R.id.BtnFail);
		
		if(buyBtn != null){
			buyBtn.setOnClickListener(buyListener);
		}
		if(failBtn != null){
			failBtn.setOnClickListener(failListener);
		}
		
		
	}
	

}
