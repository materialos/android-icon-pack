package org.materialos.icons.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.polar.R;
import org.materialos.icons.config.Config;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

/**
 * @author Aidan Follestad (afollestad)
 */
public abstract class BaseDonateActivity extends BaseThemedActivity implements BillingProcessor.IBillingHandler {

    private BillingProcessor bp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Config.get().donationEnabled() &&
                BillingProcessor.isIabServiceAvailable(this)) {
            bp = new BillingProcessor(this, Config.get().donationLicenseKey(), this);
        }
    }

    public void purchase(String donateId) {
        if (bp != null)
            bp.purchase(this, donateId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing() && bp != null) {
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
        Toast.makeText(this, getString(R.string.donation_error, error != null ?
                error.getMessage() : "Error code " + errorCode), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBillingInitialized() {
        Log.d("BaseDonationActivity", "onBillingInitialized()");
    }
}
