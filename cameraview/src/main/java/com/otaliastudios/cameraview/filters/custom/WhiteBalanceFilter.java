package com.otaliastudios.cameraview.filters.custom;

import android.opengl.GLES20;

import androidx.annotation.NonNull;

import com.otaliastudios.cameraview.filter.BaseFilter;
import com.otaliastudios.opengl.core.Egloo;

public class WhiteBalanceFilter extends BaseFilter {

    public static final String WHITE_BALANCE_FRAGMENT_SHADER = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "varying highp vec2 " + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + ";\n" +
            " \n" +
            "uniform lowp float temperature;\n" +
            "uniform lowp float tint;\n" +
            "\n" +
            "const lowp vec3 warmFilter = vec3(0.93, 0.54, 0.0);\n" +
            "\n" +
            "const mediump mat3 RGBtoYIQ = mat3(0.299, 0.587, 0.114, 0.596, -0.274, -0.322, 0.212, -0.523, 0.311);\n" +
            "const mediump mat3 YIQtoRGB = mat3(1.0, 0.956, 0.621, 1.0, -0.272, -0.647, 1.0, -1.105, 1.702);\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "	lowp vec4 source = texture2D(sTexture," + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + ");\n" +
            "	\n" +
            "	mediump vec3 yiq = RGBtoYIQ * source.rgb; //adjusting tint\n" +
            "	yiq.b = clamp(yiq.b + tint*0.5226*0.1, -0.5226, 0.5226);\n" +
            "	lowp vec3 rgb = YIQtoRGB * yiq;\n" +
            "\n" +
            "	lowp vec3 processed = vec3(\n" +
            "		(rgb.r < 0.5 ? (2.0 * rgb.r * warmFilter.r) : (1.0 - 2.0 * (1.0 - rgb.r) * (1.0 - warmFilter.r))), //adjusting temperature\n" +
            "		(rgb.g < 0.5 ? (2.0 * rgb.g * warmFilter.g) : (1.0 - 2.0 * (1.0 - rgb.g) * (1.0 - warmFilter.g))), \n" +
            "		(rgb.b < 0.5 ? (2.0 * rgb.b * warmFilter.b) : (1.0 - 2.0 * (1.0 - rgb.b) * (1.0 - warmFilter.b))));\n" +
            "\n" +
            "	gl_FragColor = vec4(mix(rgb, processed, temperature), source.a);\n" +
            "}";

    private int temperatureLocation;
    private float temperature;
    private int tintLocation;
    private float tint;

    public WhiteBalanceFilter() {
        this(5000.0f, 0.0f);
    }

    public WhiteBalanceFilter(final float temperature, final float tint) {
        this.temperature = temperature;
        this.tint = tint;
    }

    @Override
    public void onCreate(int programHandle) {
        super.onCreate(programHandle);
        temperatureLocation = GLES20.glGetUniformLocation(programHandle, "temperature");
        Egloo.checkGlProgramLocation(temperatureLocation, "temperature");
        tintLocation = GLES20.glGetUniformLocation(programHandle, "tint");
        Egloo.checkGlProgramLocation(tintLocation, "tint");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        temperatureLocation = -1;
        tintLocation = -1;
    }

    public void setTemperature(final float temperature) {
        this.temperature = temperature;
        setFloat(temperatureLocation, this.temperature < 5000 ? (float) (0.0004 * (this.temperature - 5000.0)) : (float) (0.00006 * (this.temperature - 5000.0)));
    }


    public void setTint(final float tint) {
        this.tint = tint;
        setFloat(tintLocation, (float) (this.tint / 100.0));
    }

    private void setFloat(int location, float floatValue) {
        GLES20.glUniform1f(location, floatValue);
        Egloo.checkGlError("glUniform1f");
    }


    @NonNull
    @Override
    public String getFragmentShader() {
        return WHITE_BALANCE_FRAGMENT_SHADER;
    }

    @NonNull
    @Override
    protected BaseFilter onCopy() {
        if (temperature > 0 && tint > 0) {
            return new WhiteBalanceFilter(temperature, tint);
        }
        return new WhiteBalanceFilter();

    }

    @Override
    protected void onPreDraw(long timestampUs, @NonNull float[] transformMatrix) {
        super.onPreDraw(timestampUs, transformMatrix);
        setTemperature(temperature);
        setTint(tint);
    }
}
