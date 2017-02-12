package com.afollestad.polar.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.polar.BuildConfig;
import com.afollestad.polar.R;
import com.afollestad.polar.config.Config;
import com.afollestad.polar.dialogs.ProgressDialogFragment;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;

import java.util.Random;

/**
 * @author Aidan Follestad (afollestad)
 */
public class LicensingUtils {

    private final static String KEY_SALT = "[licensing-salt]";
    private final static String KEY_VALID = "[license-valid]";

    public interface LicensingCallback {
        void onLicensingResult(boolean allow, int reason);

        void onLicensingError(int errorCode);
    }

    @SuppressWarnings("PointlessBooleanExpression")
    private static void LOG(String message, Object... args) {
        if (!BuildConfig.DEBUG) return;
        if (args != null)
            Log.d("PolarLicensing", String.format(message, args));
        else Log.d("PolarLicensing", message);
    }

    private LicensingUtils() {
    }

    private static byte[] mSalt;
    private static LicenseCheckerCallback mLicenseCheckerCallback;
    private static LicenseChecker mChecker;
    private static ProgressDialogFragment mProgress;

    private static void generateSalt(Context context) {
        mSalt = new byte[20];
        final Random randomGenerator = new Random();
        for (int i = 0; i < 20; ++i)
            mSalt[i] = (byte) (randomGenerator.nextInt(600) - 300);
        final String saltStr = getSaltString();
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(KEY_SALT, saltStr).commit();
        if (BuildConfig.DEBUG)
            LOG("Generated new licensing salt: %s", saltStr);
    }

    private static String getSaltString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mSalt.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(Byte.toString(mSalt[i]));
        }
        return sb.toString();
    }

    private static byte[] bytesFromString(String string) {
        final String[] split = string.split(" ");
        final byte[] data = new byte[split.length];
        for (int i = 0; i < split.length; i++)
            data[i] = Byte.parseByte(split[i]);
        return data;
    }

    private static byte[] getSalt(Context context) {
        if (mSalt == null) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.contains(KEY_SALT)) {
                mSalt = bytesFromString(prefs.getString(KEY_SALT, null));
                LOG("Loaded licensing salt: %s", getSaltString());
            }
            if (mSalt == null) generateSalt(context);
        }
        return mSalt;
    }

    public static boolean check(@NonNull AppCompatActivity context, @NonNull LicensingCallback cb) {
        final String key = Config.get().licensingPublicKey();
        if (key == null || key.trim().isEmpty()) {
            LOG("License checking is disabled.");
            return true;
        } else if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_VALID, false)) {
            LOG("License checking has already been done, and the license check was successful.");
            return true;
        }

        if (BuildConfig.DEBUG) {
            Toast.makeText(context, "License checking is disabled for this debug build.", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (context.getContentResolver() == null) {
            if (!context.isFinishing())
                context.finish();
            return false;
        }
        mProgress = ProgressDialogFragment.show(context, R.string.checking_license);
        final String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        // Library calls this when it's done.
        mLicenseCheckerCallback = new MyLicenseCheckerCallback(context, cb);
        // Construct the LicenseChecker with a policy.
        mChecker = new LicenseChecker(
                context, new ServerManagedPolicy(context,
                new AESObfuscator(getSalt(context), BuildConfig.APPLICATION_ID, deviceId)),
                key);
        mChecker.checkAccess(mLicenseCheckerCallback);
        return false;
    }

    public static void cleanup() {
        if (mChecker != null)
            mChecker.onDestroy();
        mLicenseCheckerCallback = null;
        mSalt = null;
        mLicenseCheckerCallback = null;
    }

    private static class MyLicenseCheckerCallback implements LicenseCheckerCallback {

        private final Context mContext;
        private final LicensingCallback mCb;

        public MyLicenseCheckerCallback(Context context, LicensingCallback callback) {
            mContext = context;
            mCb = callback;
        }

        @Override
        public void allow(int policyReason) {
            mProgress.dismiss();
            mProgress = null;
            if (mCb != null)
                mCb.onLicensingResult(true, policyReason);
            PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                    .putBoolean(KEY_VALID, true).commit();
            Toast.makeText(mContext, R.string.license_valid, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void dontAllow(int policyReason) {
            // Should not allow access. In most cases, the app should assume
            // the user has access unless it encounters this. If it does,
            // the app should inform the user of their unlicensed ways
            // and then either shut down the app or limit the user to a
            // restricted set of features.
            // In this example, we show a dialog that takes the user to Market.
            // If the reason for the lack of license is that the service is
            // unavailable or there is another problem, we display a
            // retry button on the dialog and a different message.
            mProgress.dismiss();
            mProgress = null;
            if (mCb != null)
                mCb.onLicensingResult(false, policyReason);
        }

        @Override
        public void applicationError(int errorCode) {
            // This is a polite way of saying the developer made a mistake
            // while setting up or calling the license checker library.
            // Please examine the error code and fix the error.
            mProgress.dismiss();
            mProgress = null;
            if (mCb != null)
                mCb.onLicensingError(errorCode);
        }
    }
}