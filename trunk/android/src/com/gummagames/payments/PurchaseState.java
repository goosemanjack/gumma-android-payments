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
 * The possible states of an in-app purchase, as defined by Android Market.
 * @author Chris Cole
 *
 */
public enum PurchaseState {
    // Responses to requestPurchase or restoreTransactions.
    PURCHASED,   // User was charged for the order.
    CANCELED,    // The charge failed on the server.
    REFUNDED;    // User received a refund for the order.

    // Converts from an ordinal value to the PurchaseState
    public static PurchaseState valueOf(int index) {
        PurchaseState[] values = PurchaseState.values();
        if (index < 0 || index >= values.length) {
            return CANCELED;
        }
        return values[index];
    }
}
