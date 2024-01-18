package com.picupmedia.cameraview.filters.custom;

import android.opengl.GLES20;

import androidx.annotation.NonNull;

import com.picupmedia.cameraview.filter.BaseFilter;
import com.picupmedia.cameraview.utils.XLogger;
import com.otaliastudios.opengl.core.Egloo;

import java.nio.FloatBuffer;

public class ImageLevelFilter extends BaseFilter {

    public static final String LEVELS_FRAGMET_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
            "varying highp vec2 "+DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME+";\n" +
                    " \n" +
                    " uniform samplerExternalOES sTexture;\n" +
                    " uniform mediump vec3 levelMinimum;\n" +
                    " uniform mediump vec3 levelMiddle;\n" +
                    " uniform mediump vec3 levelMaximum;\n" +
                    " uniform mediump vec3 minOutput;\n" +
                    " uniform mediump vec3 maxOutput;\n" +
                    " \n" +
                    " void main()\n" +
                    " {\n" +
                    "     mediump vec4 textureColor = texture2D(sTexture, "+DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME+");\n" +
                    "     \n" +
                    "     gl_FragColor = vec4( mix(minOutput, maxOutput, pow(min(max(textureColor.rgb -levelMinimum, vec3(0.0)) / (levelMaximum - levelMinimum  ), vec3(1.0)), 1.0 /levelMiddle)) , textureColor.a);\n" +
                    " }\n";

    @NonNull
    @Override
    public String getFragmentShader() {
        return LEVELS_FRAGMET_SHADER;
    }


    private int minLocation;
    private float[] min;
    private int midLocation;
    private float[] mid;
    private int maxLocation;
    private float[] max;
    private int minOutputLocation;
    private float[] minOutput;
    private int maxOutputLocation;
    private float[] maxOutput;

    public ImageLevelFilter() {
        this(new float[]{0.0f, 0.0f, 0.0f}, new float[]{1.0f, 1.0f, 1.0f}, new float[]{1.0f, 1.0f, 1.0f}, new float[]{0.0f, 0.0f, 0.0f}, new float[]{1.0f, 1.0f, 1.0f});
    }

    private ImageLevelFilter(final float[] min, final float[] mid, final float[] max, final float[] minOUt, final float[] maxOut) {
        this.min = min;
        this.mid = mid;
        this.max = max;
        minOutput = minOUt;
        maxOutput = maxOut;
    }

    @Override
    public void onCreate(int programHandle) {
        super.onCreate(programHandle);
        minLocation = GLES20.glGetUniformLocation(programHandle, "levelMinimum");
        Egloo.checkGlProgramLocation(minLocation, "levelMinimum");
        midLocation = GLES20.glGetUniformLocation(programHandle, "levelMiddle");
        Egloo.checkGlProgramLocation(midLocation, "levelMiddle");
        maxLocation = GLES20.glGetUniformLocation(programHandle, "levelMaximum");
        Egloo.checkGlProgramLocation(maxLocation, "levelMaximum");
        minOutputLocation = GLES20.glGetUniformLocation(programHandle, "minOutput");
        Egloo.checkGlProgramLocation(minOutputLocation, "minOutput");
        maxOutputLocation = GLES20.glGetUniformLocation(programHandle, "maxOutput");
        Egloo.checkGlProgramLocation(maxOutputLocation, "maxOutput");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        minLocation = -1;
        midLocation = -1;
        maxLocation = -1;
        minOutputLocation = -1;
        maxOutputLocation = -1;
    }

    @Override
    protected void onPreDraw(long timestampUs, @NonNull float[] transformMatrix) {
        try {
            super.onPreDraw(timestampUs, transformMatrix);
            updateUniforms();
        } catch (Exception e) {
            XLogger.e("image level error:"+e.getMessage());
        }
    }

    @NonNull
    @Override
    protected BaseFilter onCopy() {
        XLogger.d("image level onCopy");
        if (min.length == 0 && mid.length == 0 && max.length == 0) {
            return new ImageLevelFilter();
        }
        return new ImageLevelFilter(min,mid,max,minOutput,maxOutput);
    }


    public void updateUniforms() {
        GLES20.glUniform3fv(minLocation, 1, FloatBuffer.wrap(min));
        Egloo.checkGlError("glUniform3fv");
        GLES20.glUniform3fv(midLocation, 1, FloatBuffer.wrap(mid));
        Egloo.checkGlError("glUniform3fv");
        GLES20.glUniform3fv(maxLocation, 1, FloatBuffer.wrap(max));
        Egloo.checkGlError("glUniform3fv");
        GLES20.glUniform3fv(minOutputLocation, 1, FloatBuffer.wrap(minOutput));
        Egloo.checkGlError("glUniform3fv");
        GLES20.glUniform3fv(maxOutputLocation, 1, FloatBuffer.wrap(maxOutput));
        Egloo.checkGlError("glUniform3fv");
    }

    public void setMin(float min, float mid, float max, float minOut, float maxOut) {
        setRedMin(min, mid, max, minOut, maxOut);
        setGreenMin(min, mid, max, minOut, maxOut);
        setBlueMin(min, mid, max, minOut, maxOut);
    }

    public void setMin(float min, float mid, float max) {
        setMin(min, mid, max, 0.0f, 1.0f);
    }

    public void setRedMin(float min, float mid, float max, float minOut, float maxOut) {
        this.min[0] = min;
        this.mid[0] = mid;
        this.max[0] = max;
        minOutput[0] = minOut;
        maxOutput[0] = maxOut;
        updateUniforms();
    }

    public void setRedMin(float min, float mid, float max) {
        setRedMin(min, mid, max, 0, 1);
    }

    public void setGreenMin(float min, float mid, float max, float minOut, float maxOut) {
        this.min[1] = min;
        this.mid[1] = mid;
        this.max[1] = max;
        minOutput[1] = minOut;
        maxOutput[1] = maxOut;
        updateUniforms();
    }

    public void setGreenMin(float min, float mid, float max) {
        setGreenMin(min, mid, max, 0, 1);
    }

    public void setBlueMin(float min, float mid, float max, float minOut, float maxOut) {
        this.min[2] = min;
        this.mid[2] = mid;
        this.max[2] = max;
        minOutput[2] = minOut;
        maxOutput[2] = maxOut;
        updateUniforms();
    }

    public void setBlueMin(float min, float mid, float max) {
        setBlueMin(min, mid, max, 0, 1);
    }


}
