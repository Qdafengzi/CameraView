package com.picupmedia.cameraview.engine.options;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.os.Build;
import android.util.Range;

import androidx.annotation.NonNull;

import com.picupmedia.cameraview.CameraOptions;
import com.picupmedia.cameraview.controls.Facing;
import com.picupmedia.cameraview.controls.Flash;
import com.picupmedia.cameraview.controls.Hdr;
import com.picupmedia.cameraview.controls.PictureFormat;
import com.picupmedia.cameraview.controls.WhiteBalance;
import com.picupmedia.cameraview.engine.mappers.Camera1Mapper;
import com.picupmedia.cameraview.internal.CamcorderProfiles;
import com.picupmedia.cameraview.size.AspectRatio;
import com.picupmedia.cameraview.size.Size;
import com.picupmedia.cameraview.utils.XLogger;

import java.util.List;

public class Camera1Options extends CameraOptions {

    public Camera1Options(@NonNull Camera.Parameters params, int cameraId, boolean flipSizes) {
        List<String> strings;
        Camera1Mapper mapper = Camera1Mapper.get();

        // Facing
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0, count = Camera.getNumberOfCameras(); i < count; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            Facing value = mapper.unmapFacing(cameraInfo.facing);
            if (value != null) supportedFacing.add(value);
        }

        // WB
        strings = params.getSupportedWhiteBalance();
        //是否支持白平衡
        supportWhiteBalanceFlag = strings != null && strings.contains(Camera.Parameters.WHITE_BALANCE_AUTO);
        if (strings != null) {
            for (String string : strings) {
                WhiteBalance value = mapper.unmapWhiteBalance(string);
                if (value != null) supportedWhiteBalance.add(value);
            }
        }

        // Flash
        supportedFlash.add(Flash.OFF);
        strings = params.getSupportedFlashModes();
        if (strings != null) {
            for (String string : strings) {
                Flash value = mapper.unmapFlash(string);
                if (value != null) supportedFlash.add(value);
            }
        }

        // Hdr
        supportedHdr.add(Hdr.OFF);
        strings = params.getSupportedSceneModes();
        if (strings != null) {
            for (String string : strings) {
                Hdr value = mapper.unmapHdr(string);
                if (value != null) supportedHdr.add(value);
            }
        }

        // zoom
        zoomSupported = params.isZoomSupported();

        // autofocus
        autoFocusSupported = params.getSupportedFocusModes()
                .contains(Camera.Parameters.FOCUS_MODE_AUTO);

        // Exposure correction
        float step = params.getExposureCompensationStep();
        exposureCorrectionMinValue = (float) params.getMinExposureCompensation() * step;
        exposureCorrectionMaxValue = (float) params.getMaxExposureCompensation() * step;
        exposureCorrectionSupported = params.getMinExposureCompensation() != 0
                || params.getMaxExposureCompensation() != 0;

        // Picture Sizes
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        for (Camera.Size size : sizes) {
            int width = flipSizes ? size.height : size.width;
            int height = flipSizes ? size.width : size.height;
            supportedPictureSizes.add(new Size(width, height));
            supportedPictureAspectRatio.add(AspectRatio.of(width, height));
        }

        // Video Sizes
        // As a safety measure, remove Sizes bigger than CamcorderProfile.highest
        CamcorderProfile profile = CamcorderProfiles.get(cameraId,
                new Size(Integer.MAX_VALUE, Integer.MAX_VALUE));
        Size videoMaxSize = new Size(profile.videoFrameWidth, profile.videoFrameHeight);
        List<Camera.Size> vsizes = params.getSupportedVideoSizes();
        if (vsizes != null) {
            for (Camera.Size size : vsizes) {
                if (size.width <= videoMaxSize.getWidth()
                        && size.height <= videoMaxSize.getHeight()) {
                    int width = flipSizes ? size.height : size.width;
                    int height = flipSizes ? size.width : size.height;
                    supportedVideoSizes.add(new Size(width, height));
                    supportedVideoAspectRatio.add(AspectRatio.of(width, height));
                }
            }
        } else {
            // StackOverflow threads seems to agree that if getSupportedVideoSizes is null,
            // previews can be used.
            List<Camera.Size> fallback = params.getSupportedPreviewSizes();
            for (Camera.Size size : fallback) {
                if (size.width <= videoMaxSize.getWidth()
                        && size.height <= videoMaxSize.getHeight()) {
                    int width = flipSizes ? size.height : size.width;
                    int height = flipSizes ? size.width : size.height;
                    supportedVideoSizes.add(new Size(width, height));
                    supportedVideoAspectRatio.add(AspectRatio.of(width, height));
                }
            }
        }

        // Preview FPS
        previewFrameRateMinValue = Float.MAX_VALUE;
        previewFrameRateMaxValue = -Float.MAX_VALUE;
        List<int[]> fpsRanges = params.getSupportedPreviewFpsRange();
        for (int[] fpsRange : fpsRanges) {
            float lower = (float) fpsRange[0] / 1000F;
            float upper = (float) fpsRange[1] / 1000F;
            previewFrameRateMinValue = Math.min(previewFrameRateMinValue, lower);
            previewFrameRateMaxValue = Math.max(previewFrameRateMaxValue, upper);
        }

        // Picture formats
        supportedPictureFormats.add(PictureFormat.JPEG);

        // Frame processing formats
        supportedFrameProcessingFormats.add(ImageFormat.NV21);

        int maxZoomLevel = params.getMaxZoom();
        List<Integer> zoomRatios = params.getZoomRatios();
        maxZoomRatio = zoomRatios.get(maxZoomLevel) / 100f;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            zoomRange = new Range<>(1f, maxZoomRatio);
        }
        XLogger.d("maxZoomLevel:" + maxZoomLevel + " maxZoomRatio:" + maxZoomRatio);
    }
}
