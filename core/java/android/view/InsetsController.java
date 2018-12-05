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

package android.view;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.graphics.Rect;
import android.util.ArraySet;
import android.util.SparseArray;
import android.view.SurfaceControl.Transaction;
import android.view.WindowInsets.Type.InsetType;
import android.view.InsetsState.InternalInsetType;

import com.android.internal.annotations.VisibleForTesting;

import java.io.PrintWriter;

/**
 * Implements {@link WindowInsetsController} on the client.
 * @hide
 */
public class InsetsController implements WindowInsetsController {

    private final InsetsState mState = new InsetsState();
    private final Rect mFrame = new Rect();
    private final SparseArray<InsetsSourceConsumer> mSourceConsumers = new SparseArray<>();

    private final SparseArray<InsetsSourceControl> mTmpControlArray = new SparseArray<>();

    void onFrameChanged(Rect frame) {
        mFrame.set(frame);
    }

    public InsetsState getState() {
        return mState;
    }

    public void setState(InsetsState state) {
        mState.set(state);
    }

    /**
     * @see InsetsState#calculateInsets
     */
    WindowInsets calculateInsets(boolean isScreenRound,
            boolean alwaysConsumeNavBar, DisplayCutout cutout) {
        return mState.calculateInsets(mFrame, isScreenRound, alwaysConsumeNavBar, cutout);
    }

    /**
     * Called when the server has dispatched us a new set of inset controls.
     */
    public void onControlsChanged(InsetsSourceControl[] activeControls) {
        if (activeControls != null) {
            for (InsetsSourceControl activeControl : activeControls) {
                mTmpControlArray.put(activeControl.getType(), activeControl);
            }
        }

        // Ensure to update all existing source consumers
        for (int i = mSourceConsumers.size() - 1; i >= 0; i--) {
            final InsetsSourceConsumer consumer = mSourceConsumers.valueAt(i);
            final InsetsSourceControl control = mTmpControlArray.get(consumer.getType());

            // control may be null, but we still need to update the control to null if it got
            // revoked.
            consumer.setControl(control);
        }

        // Ensure to create source consumers if not available yet.
        for (int i = mTmpControlArray.size() - 1; i >= 0; i--) {
            final InsetsSourceControl control = mTmpControlArray.valueAt(i);
            getSourceConsumer(control.getType()).setControl(control);
        }
        mTmpControlArray.clear();
    }

    @Override
    public void show(@InsetType int types) {
        final ArraySet<Integer> internalTypes = InsetsState.toInternalType(types);
        for (int i = internalTypes.size() - 1; i >= 0; i--) {
            getSourceConsumer(internalTypes.valueAt(i)).show();
        }
    }

    @Override
    public void hide(@InsetType int types) {
        final ArraySet<Integer> internalTypes = InsetsState.toInternalType(types);
        for (int i = internalTypes.size() - 1; i >= 0; i--) {
            getSourceConsumer(internalTypes.valueAt(i)).hide();
        }
    }

    @VisibleForTesting
    public @NonNull InsetsSourceConsumer getSourceConsumer(@InternalInsetType int type) {
        InsetsSourceConsumer controller = mSourceConsumers.get(type);
        if (controller != null) {
            return controller;
        }
        controller = new InsetsSourceConsumer(type, mState, Transaction::new);
        mSourceConsumers.put(type, controller);
        return controller;
    }

    void dump(String prefix, PrintWriter pw) {
        pw.println(prefix); pw.println("InsetsController:");
        mState.dump(prefix + "  ", pw);
    }
}
