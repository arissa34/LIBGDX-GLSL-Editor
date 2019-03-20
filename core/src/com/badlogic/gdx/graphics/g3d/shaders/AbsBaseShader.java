package com.badlogic.gdx.graphics.g3d.shaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.glsl.shaders.ShaderHelper;
import com.glsl.shaders.loader.ShaderLoader;

public abstract class AbsBaseShader extends MyBaseShader {

    protected final int u_delta = register(new Uniform("u_delta"));
    protected final int u_mouse = register(new Uniform("u_mouse"));
    protected final int u_resolution = register(new Uniform("u_resolution"));

    protected static float delta = 0f;
    protected static Vector2 mouse = new Vector2();
    protected static Vector2 resolution = new Vector2();

    private ShaderLoader loader;

    public AbsBaseShader() {
        super();
    }

    public void loadShader(){
        loader = ShaderHelper.get().getShaderLoader();
        try{
            program = new ShaderProgram(loader.load(getVertexFileName()), addPrefix() + loader.load(getFragmentFileName()));
            if (!program.isCompiled()) {
                throw new RuntimeException(program.getLog());
            }
            init(program, null);
            init();
        }catch (Exception e){

        }
    }

    public abstract String addPrefix();

    public abstract String getVertexFileName();

    public abstract String getFragmentFileName();

    @Override
    public void init() {
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        program.begin();
        //set(u_delta, delta);
        //set(u_mouse, mouse.set(Gdx.input.getX(), Gdx.input.getY()));
        //set(u_resolution, resolution.set(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight()));
    }

    @Override
    public void end () {
        program.end();
    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        return true;
    }

    public void update(float delta) {
        this.delta = delta;
    }

    public void setNewProgram(String vertexShader, String fragmentShader) {
        program = new ShaderProgram(vertexShader, fragmentShader);
        init(program, null);
        init();
    }

    public boolean writeShaderInFile(String vertexText, String fragmentText){
        return loader.writeShaderInFile(vertexText, fragmentText, getVertexFileName(), getFragmentFileName());
    }

    @Override
    public void dispose () {
        super.dispose();
        if(program != null) {
            program.dispose();
        }
    }

}
