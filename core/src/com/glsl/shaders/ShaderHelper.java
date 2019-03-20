package com.glsl.shaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.shaders.AbsBaseShader;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.glsl.shaders.enums.ShaderEnum;
import com.glsl.shaders.loader.ShaderLoader;

public class ShaderHelper {

    private static final String TAG = ShaderHelper.class.getSimpleName();
    public static final String SHADER_BASE_PATH = "data/shader";

    // ===== SINGLETON
    private static ShaderHelper instance;

    public static ShaderHelper get() {
        if (instance == null) instance = new ShaderHelper();
        return instance;
    }
    // ===== SINGLETON

    public ShaderHelper(){
        listAllShaders();
    }

    private ShaderLoader loader;
    private ObjectMap<String, AbsBaseShader> listShaders = new ObjectMap<String, AbsBaseShader>();
    private Array<String> listShadersName;

    public ShaderLoader getShaderLoader(){
        if(loader == null){
            loader = new ShaderLoader(Gdx.files.internal(SHADER_BASE_PATH));
        }
        return loader;
    }

    private void listAllShaders(){
        for(ShaderEnum shaderEnum : ShaderEnum.values()){
            listShaders.put(shaderEnum.name(), null);
        }
    }

    public AbsBaseShader getShader(String nameShader){
        if(listShaders.containsKey(nameShader)){
            return ShaderEnum.valueOf(nameShader).getShader();
        }else {
            throw new RuntimeException("RuntimeException no shader : "+nameShader);
        }
    }

    public Array<String> getListShaders(){
        if(listShadersName == null){
            listShadersName = new Array<String>();
            for(String name : listShaders.keys().toArray()){
                if(name.contains("Shader")) listShadersName.add(name);
            }
        }
        return listShadersName;
    }
}
