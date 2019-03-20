package com.glsl.utils;

import com.badlogic.gdx.Gdx;

public class ScreenUtils {

    public static float getWidhtScreenPourcent(float pourcent){
        return pourcent * Gdx.graphics.getBackBufferWidth() / 100;
    }

    public static float getHeightScreenPourcent(float pourcent){
        return pourcent * Gdx.graphics.getBackBufferHeight() / 100;
    }

    public static float getWidhtPourcent(float pourcent, float width){
        return pourcent * width / 100;
    }

    public static float getHeightPourcent(float pourcent, float height){
        return pourcent * height / 100;
    }
}
