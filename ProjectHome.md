# Gumma Payments #
## In App Payment Library for Android ##
The In-App Payment SDK Google _should_ have released for Android.

```
BillingConfig config = new BillingConfig();
config.setPublicKey("[MARKET PUBLIC KEY]");
config.setDebug(DEBUG);

PurchaseManager.initialize(this, config);
PurchaseManager.attachBillingResponseListener(
  new BillingProvider.BillingResponseListener() {
    @Override
    public void onBillingSupported(boolean supported) {
      Log.i("PAYMENTS", "Is billing supported?  " + supported);
    }
    @Override
    public void onPurchaseSuccess(PaidProduct product, int responseCode) {
      Log.i("PAYMENTS", "YIPPEE!!! I just made some coin!");
    }

    @Override
    public void onPurchaseFailed(PaidProduct product, int responseCode) {
      Log.i("PAYMENTS", "Cheap-o wouldn't pay");
    }
  });

PurchaseManager.buyItem(product_sku);


```

Try 20 lines of code to get Android in-app payments working instead of wading through the 2500 lines in the Dungeons Sample.


## What Is It? ##

This library simplifies your interaction with in-app payment (IAP) systems from Android applications.  The Google Market Billing services is complicated and tedious to set up.  With the growing popularity of the Kindle Fire and Amazon's Android App Store, your apps will need to be flexible enough to use multiple providers.  This library provides decoupling from and auto-detection of available IAP providers.

## Debug and Test Your Payment Flow ##

A major failing of the Android Market Billing Service out of the box is that it cannot be used to debug and test payment flow until you export a release build of your app and upload the app to the Google Android Market. The **Gumma Payments** library provides a stub payment interface to test your purchase flow in an AVD and with debug builds.
