package com.afollestad.polar.ui.base;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.polar.R;
import com.afollestad.polar.config.Config;
import com.afollestad.polar.util.Utils;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class BaseDonateActivity extends BaseThemedActivity implements BillingProcessor.IBillingHandler {

    private BillingProcessor bp;

    public void purchase(String donateId) {
        if (bp != null)
            bp.purchase(this, donateId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Config.get().donationEnabled() &&
                BillingProcessor.isIabServiceAvailable(this)) {
            bp = new BillingProcessor(this, Config.get().donationLicenseKey(), this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bp != null) {
            bp.release();
            bp = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (bp == null || !bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        Toast.makeText(this, R.string.thank_you_donation, Toast.LENGTH_SHORT).show();
        if (bp != null)
            bp.consumePurchase(productId);
    }

    @Override
    public void onPurchaseHistoryRestored() {
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        String msg;
        switch (errorCode) {
            case 1:
                return; // USER_CANCELLED
            case 2:
                msg = "Billing service unavailable.";
                break;
            case 3:
                msg = "Billing API version unavailable.";
                break;
            case 4:
                msg = "The requested item is unavailable.";
                break;
            case 5:
                msg = "Developer error: invalid arguments provided to the API.";
                break;
            case 6:
                msg = "Fatal biling error.";
                break;
            case 7:
                msg = "Item is already owned.";
                break;
            case 8:
                msg = "Failed to consume this item since it has not yet been purchased.";
                break;
            default:
                msg = "Unknown billing error, error code " + errorCode;
                break;
        }
        Utils.showError(this, new Exception(msg));
    }

    @Override
    public void onBillingInitialized() {
        Log.d("BaseDonationActivity", "onBillingInitialized()");
    }
}
