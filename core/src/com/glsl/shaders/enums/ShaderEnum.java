package com.glsl.shaders.enums;

import com.badlogic.gdx.graphics.g3d.shaders.AbsBaseShader;

import java.util.ArrayList;

public enum ShaderEnum {

    BlurHorizontalShader{
        @Override
        public AbsBaseShader getShader() {
            return new BlurHorizontalShader();
        }
    },
    BlurVerticalShader{
        @Override
        public AbsBaseShader getShader() {
            return new BlurVerticalShader();
        }
    },
    BrightFilterShader{
        @Override
        public AbsBaseShader getShader() {
            return new BrightFilterShader();
        }
    },
    CombineShader{
        @Override
        public AbsBaseShader getShader() {
            return new CombineShader();
        }
    },
    ContrastShader{
        @Override
        public AbsBaseShader getShader() {
            return new ContrastShader();
        }
    },
    CopyShader{
        @Override
        public AbsBaseShader getShader() {
            return new CopyShader();
        }
    },
    DepthMap2dShader{
        @Override
        public AbsBaseShader getShader() {
            return new DepthMap2dShader();
        }
    },
    DepthMapShader{
        @Override
        public AbsBaseShader getShader() {
            return new DepthMapShader();
        }
    },
    DepthOfFieldShader{
        @Override
        public AbsBaseShader getShader() {
            return new DepthOfFieldShader();
        }
    },
    ExampleBasicShader{
        @Override
        public AbsBaseShader getShader() {
            return new ExampleBasicShader();
        }
    },
    FogShader{
        @Override
        public AbsBaseShader getShader() {
            return new FogShader();
        }
    },
    MotionBlurShader{
        @Override
        public AbsBaseShader getShader() {
            return new MotionBlurShader();
        }
    },
    MotionRadialBlurShader{
        @Override
        public AbsBaseShader getShader() {
            return new MotionRadialBlurShader();
        }
    },
    OverlayColorShader{ // TODO REVOIR CEST QUOI CE PUTAIN DE SHADER ???!
        @Override
        public AbsBaseShader getShader() {
            return new OverlayColorShader();
        }
    },
    Pixelation2dShader{
        @Override
        public AbsBaseShader getShader() {
            return new Pixelation2dShader();
        }
    },
    PixelationShader{
        @Override
        public AbsBaseShader getShader() {
            return new PixelationShader();
        }
    },
    RadialBlurShader{
        @Override
        public AbsBaseShader getShader() {
            return new RadialBlurShader();
        }
    },
    SelectionOutlineShader{
        @Override
        public AbsBaseShader getShader() {
            return new SelectionOutlineShader();
        }
    },
    SkyboxShader{
        @Override
        public AbsBaseShader getShader() {
            return new SkyboxShader();
        }
    },
    SkyShader{
        @Override
        public AbsBaseShader getShader() {
            return new SkyShader();
        }
    },
    ThresholdShader{
        @Override
        public AbsBaseShader getShader() {
            return new ThresholdShader();
        }
    },


    ;

    public abstract AbsBaseShader getShader();

    public static ArrayList<String> getList(){
        ArrayList<String> list = new ArrayList<String>();
        for(ShaderEnum shaderEnum : values()){
            list.add(shaderEnum.name());
        }
        return list;
    }
}
