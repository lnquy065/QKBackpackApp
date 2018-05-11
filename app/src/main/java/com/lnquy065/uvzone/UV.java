package com.lnquy065.uvzone;

import android.graphics.Color;

/**
 * Created by LN Quy on 20/04/2018.
 */

public class UV {

    public static final int UV_MEDIUM = 1;
    public static final  int UV_HIGH = 2;
    public static final int UV_VERYHIGH = 3;

    private static int UVC_MEDIUM = 0x32f4e842;
    private static int UVC_HIGH = 0x32FFA500;
    private static int UVC_VERYHIGH = 0x32FF0000;

    public static int getUVLevel(int uv) {
        if (uv >= 3 && uv <= 6) return UV_MEDIUM;
        if (uv > 6 && uv <=11) return UV_HIGH;
        if (uv >= 12 ) return UV_VERYHIGH;
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
