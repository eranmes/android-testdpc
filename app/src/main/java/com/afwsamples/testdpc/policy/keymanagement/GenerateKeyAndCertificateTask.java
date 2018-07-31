/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afwsamples.testdpc.policy.keymanagement;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.security.AttestedKeyPair;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afwsamples.testdpc.DeviceAdminReceiver;
import com.afwsamples.testdpc.R;
import com.afwsamples.testdpc.policy.utils.Attestation;
import com.afwsamples.testdpc.policy.utils.AuthorizationList;
import com.afwsamples.testdpc.policy.utils.CertificateUtils;

import org.bouncycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.x500.X500Principal;


public class GenerateKeyAndCertificateTask extends AsyncTask<Void, Integer, AttestedKeyPair> {
    public static final String TAG = "PolicyManagement";

    final String mAlias;
    final boolean mIsUserSelectable;
    private final byte[] mAttestationChallenge;
    private final int mIdAttestationFlags;
    private final ComponentName mAdminComponentName;
    private final DevicePolicyManager mDevicePolicyManager;
    private final Activity mActivity;

    public GenerateKeyAndCertificateTask(
            String alias,
            boolean isUserSelectable,
            byte[] attestationChallenge,
            int idAttestationFlags,
            Activity activity) {
        mAlias = alias;
        mIsUserSelectable = isUserSelectable;
        mAttestationChallenge = attestationChallenge;
        mIdAttestationFlags = idAttestationFlags;
        mActivity = activity;
        mAdminComponentName = DeviceAdminReceiver.getComponentName(activity);
        mDevicePolicyManager =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    @TargetApi(28)
    @Override
    protected AttestedKeyPair doInBackground(Void... voids) {
        //TODO: IMPLEMENT ME:
        // Generate a key-pair
        // Generate a self-signed certificate associated with it
        // Install it into KeyChain.
        // See the utils.CertificateUtils class.
        return null;
    }

    @Override
    protected void onPostExecute(AttestedKeyPair keyPair) {
        if (keyPair != null) {
            showToast(R.string.key_generation_successful, mAlias);
            showKeyGenerationResult(keyPair);
        } else {
            showToast(R.string.key_generation_failed, mAlias);
        }
    }

    private void showToast(int msgId, String extra) {
        if (mActivity.isFinishing()) {
            return;
        }

        Toast.makeText(
                mActivity,
                mActivity.getResources().getString(msgId) + " " + extra,
                Toast.LENGTH_SHORT);
    }

    @TargetApi(28)
    private void showKeyGenerationResult(AttestedKeyPair keyPair) {
        if (mActivity == null || mActivity.isFinishing() || keyPair == null) {
            return;
        }
        View keyGenResultView =
                mActivity.getLayoutInflater().inflate(R.layout.key_generation_result, null);

        //TODO: IMPLEMENT ME
        List<Certificate> attestationChain = null;
        TextView attestationDetailsView = keyGenResultView.findViewById(R.id.attestation_details);

        if ((attestationChain != null) && (attestationChain.size() > 0)) {
            try {
                StringBuilder attestationDetails = new StringBuilder();

                Attestation attestationRecord =
                        new Attestation((X509Certificate) keyPair.getAttestationRecord().get(0));
                attestationDetails.append(
                        mActivity.getText(R.string.attestation_challenge_description) + "\n");
                attestationDetails.append(
                        new String(attestationRecord.getAttestationChallenge()) + "\n");
                AuthorizationList teeList = attestationRecord.getTeeEnforced();
                if (teeList != null) {
                    attestationDetails.append(
                            mActivity.getText(R.string.device_serial_number_description) + "\n");
                    attestationDetails.append(teeList.getSerialNumber() + "\n");
                    attestationDetails.append(
                            mActivity.getText(R.string.device_imei_description) + "\n");
                    attestationDetails.append(teeList.getImei() + "\n");
                    attestationDetails.append(
                            mActivity.getText(R.string.device_meid_description) + "\n");
                    attestationDetails.append(teeList.getMeid() + "\n");
                }

                Certificate root = attestationChain.get(attestationChain.size() - 1);

                attestationDetails.append(
                        String.format(
                                "%s: %d\n",
                                mActivity.getText(R.string.attestation_chain_length_description),
                                keyPair.getAttestationRecord().size()));

                attestationDetails.append(
                        String.format(
                                "%s\n%s\n",
                                mActivity.getText(R.string.attestation_root_description),
                                ((X509Certificate) root).getSubjectX500Principal().getName()));

                attestationDetailsView.setText(attestationDetails);
            } catch (CertificateParsingException e) {
                Log.e(TAG, "Failed parsing attestation record", e);
                attestationDetailsView.setText("<INVALID>");
            }
        } else {
            attestationDetailsView.setText("<none>");
        }

        new AlertDialog.Builder(mActivity)
                .setTitle(R.string.key_generation_successful)
                .setView(keyGenResultView)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
