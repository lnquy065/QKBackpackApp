package com.lnquy065.uvzone;

import android.graphics.Color;

/**
 * Created by LN Quy on 20/04/2018.
 */

public class CO2 {

    public static final int CO2_MEDIUM = 1;
    public static final  int CO2_HIGH = 2;
    public static final int CO2_VERYHIGH = 3;

    private static int UVC_MEDIUM = 0x32f4e842;
    private static int UVC_HIGH = 0x32FFA500;
    private static int UVC_VERYHIGH = 0x32FF0000;

    public static int getUVLevel(int uv) {
        if (uv >= 700 && uv <= 2500) return CO2_MEDIUM;
        if (uv > 2500 && uv <=5000) return CO2_HIGH;
        if (uv > 500 ) return CO2_VERYHIGH;
        return 0;
    }

    public static  int getUVSolidColor(int uv) {
        switch (UV.getUVLevel(uv)) {
            case UV.UV_MEDIUM: return UVC_MEDIUM;
            case UV.UV_HIGH: return UVC_HIGH;
            case UV.UV_VERYHIGH: return UVC_VERYHIGH;
        }
        return 0;
    }

    public static  int getUVStrokeColor(int uv) {
        switch (UV.getUVLevel(uv)) {
            case UV.UV_MEDIUM: return Color.YELLOW;
            case UV.UV_HIGH: return Color.YELLOW;
            case UV.UV_VERYHIGH: return Color.RED;
        }
        return 0;
    }
}
