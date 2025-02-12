package com.picupmedia.cameraview.picture;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.picupmedia.cameraview.CameraLogger;
import com.picupmedia.cameraview.PictureResult;

/**
 * Helps with logging.
 */
public abstract class FullPictureRecorder extends PictureRecorder {
    private static final String TAG = FullPictureRecorder.class.getSimpleName();
    protected static final CameraLogger LOG = CameraLogger.create(TAG);

    public FullPictureRecorder(@NonNull PictureResult.Stub stub,
                               @Nullable PictureResultListener listener) {
        super(stub, listener);
    }
}
