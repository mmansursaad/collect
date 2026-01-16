/* Copyright (C) 2017 Shobhit
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.yedc.android.fragments;

import static com.yedc.android.injection.DaggerUtils.getComponent;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.zxing.client.android.BeepManager;

import com.yedc.android.R;
import com.yedc.android.utilities.Appearances;
import com.yedc.androidshared.ui.ToastUtils;
import com.yedc.qrcode.BarcodeScannerView;
import com.yedc.qrcode.BarcodeScannerViewContainer;

import java.io.IOException;
import java.util.zip.DataFormatException;

import javax.inject.Inject;

public abstract class BarCodeScannerFragment extends Fragment implements BarcodeScannerView.TorchListener {

    private BarcodeScannerViewContainer barcodeScannerViewContainer;

    private Button switchFlashlightButton;
    private BeepManager beepManager;

    @Inject
    BarcodeScannerViewContainer.Factory barcodeScannerViewFactory;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        getComponent(context).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        beepManager = new BeepManager(getActivity());

        View rootView = inflater.inflate(R.layout.fragment_scan, container, false);
        barcodeScannerViewContainer = rootView.findViewById(R.id.barcode_view);
        barcodeScannerViewContainer.setup(barcodeScannerViewFactory, requireActivity(), getViewLifecycleOwner(), isQrOnly(), getContext().getString(com.yedc.strings.R.string.barcode_scanner_prompt), frontCameraUsed());
        barcodeScannerViewContainer.getBarcodeScannerView().setTorchListener(this);

        switchFlashlightButton = rootView.findViewById(R.id.switch_flashlight);
        switchFlashlightButton.setOnClickListener(v -> switchFlashlight());
        // if the device does not have flashlight in its camera, then remove the switch flashlight button...
        if (!hasFlash() || frontCameraUsed()) {
            switchFlashlightButton.setVisibility(View.GONE);
        }

        barcodeScannerViewContainer.getBarcodeScannerView().getLatestBarcode().observe(getViewLifecycleOwner(), result -> {
            beepManager.playBeepSoundAndVibrate();

            try {
                handleScanningResult(result);
            } catch (IOException | DataFormatException | IllegalArgumentException e) {
                ToastUtils.showShortToast(getString(com.yedc.strings.R.string.invalid_qrcode));
            }
        });

        barcodeScannerViewContainer.getBarcodeScannerView().start();

        return rootView;
    }

    private boolean hasFlash() {
        return getActivity().getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private boolean frontCameraUsed() {
        Bundle bundle = getActivity().getIntent().getExtras();
        return bundle != null && bundle.getBoolean(Appearances.FRONT);
    }

    private void switchFlashlight() {
        if (getString(com.yedc.strings.R.string.turn_on_flashlight).equals(switchFlashlightButton.getText())) {
            barcodeScannerViewContainer.getBarcodeScannerView().setTorchOn(true);
        } else {
            barcodeScannerViewContainer.getBarcodeScannerView().setTorchOn(false);
        }
    }

    @Override
    public void onTorchOn() {
        switchFlashlightButton.setText(com.yedc.strings.R.string.turn_off_flashlight);
    }

    @Override
    public void onTorchOff() {
        switchFlashlightButton.setText(com.yedc.strings.R.string.turn_on_flashlight);
    }

    protected abstract boolean isQrOnly();

    protected abstract void handleScanningResult(String result) throws IOException, DataFormatException;
}
