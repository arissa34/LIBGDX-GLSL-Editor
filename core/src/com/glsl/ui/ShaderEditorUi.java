package com.glsl.ui;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.shaders.AbsBaseShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.glsl.editor.CodeEditor;
import com.glsl.editor.scanner.ScannerEnum;
import com.glsl.shaders.ShaderHelper;
import com.glsl.utils.ScreenUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShaderEditorUi {

    protected Stage stage;
    protected Skin skin;
    protected Window windowVS;
    protected Window windowFS;
    protected Window windowListShader;
    protected Window windowLog;
    protected Table rootVS;
    protected Table rootFS;
    protected Table rootList;
    protected Table rootLog;
    private Label logLabel;
    private List<String> shaderList;
    private ScrollPane shaderScroll;
    private CodeEditor codeVSEditor;
    private CodeEditor codeFSEditor;
    private TextButton runBtn;
    private TextButton clearLogBtn;
    private TextButton compileBtn;
    private TextButton formatBtn;
    private TextButton savetBtn;
    private TextButton createNewBtn;
    private TextButton closeBtn;
    private TextButton encryptBtn;
    private static ShaderProgram shaderProgram;

    public ShaderEditorUi() {
        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            stage = new Stage(new ScreenViewport());
        } else {
            stage = new Stage(new ExtendViewport(1000, 600));
        }
        skin = new Skin(Gdx.files.internal("data/ui/skin/skin.json"));

        initializeUi();
        layoutUi();
        eventsUi();
        hide();
        //show();
    }

    public void initializeUi() {

        windowVS = new Window("Vertex shader", skin);
        windowFS = new Window("Fragment shader", skin);
        windowListShader = new Window("List shader", skin);
        windowLog = new Window("Log", skin);
        rootVS = new Table(skin);
        rootFS = new Table(skin);
        rootList = new Table(skin);
        rootLog = new Table(skin);
        runBtn = new TextButton("Run", skin);
        clearLogBtn = new TextButton("Clear", skin);
        compileBtn = new TextButton("Compile", skin);
        formatBtn = new TextButton("Format", skin);
        savetBtn = new TextButton("Save", skin);
        createNewBtn = new TextButton("Create", skin);
        closeBtn = new TextButton("X", skin);
        encryptBtn = new TextButton("Encrypt", skin);

        logLabel = new Label("", skin);
        shaderList = new List(skin);
        shaderList.setItems(ShaderHelper.get().getListShaders());
        shaderScroll = new ScrollPane(shaderList, skin, "bg");
        shaderScroll.setFlickScroll(true);
        shaderScroll.getCaptureListeners().clear();
        codeVSEditor = new CodeEditor(skin, ScannerEnum.GLSL);
        codeFSEditor = new CodeEditor(skin, ScannerEnum.GLSL);

        Pixmap bckgColor = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGB888);
        bckgColor.setColor(0f, 0f, 0f, 1f);
        bckgColor.fill();
        Drawable drawable = new Image(new Texture(bckgColor)).getDrawable();

        //textEditorFS.getStyle().background = drawable;

        windowVS.setMovable(false);
        windowVS.setResizable(false);
        windowVS.setKeepWithinStage(false);
        windowVS.getTitleTable().add(encryptBtn);
        windowVS.getTitleTable().pad(20);
        windowVS.align(Align.top);
        windowVS.setColor(0.1f, 0.1f, 0.1f, 0.67f);
        windowVS.setHeight(stage.getViewport().getWorldHeight() - ScreenUtils.getHeightScreenPourcent(25) - 64);
        windowVS.setWidth(stage.getViewport().getWorldWidth() / 2 - ScreenUtils.getWidhtScreenPourcent(12) - 31);
        windowVS.setX(ScreenUtils.getWidhtScreenPourcent(12) + 14);
        windowVS.setY(ScreenUtils.getHeightScreenPourcent(25) - 19);

        windowListShader.setMovable(false);
        windowListShader.setResizable(false);
        windowListShader.setKeepWithinStage(false);
        //windowListShader.getTitleTable().add(createNewBtn);
        windowListShader.getTitleTable().pad(20);
        windowListShader.align(Align.top);
        windowListShader.setColor(0.1f, 0.1f, 0.1f, 0.67f);
        windowListShader.setHeight(ScreenUtils.getHeightScreenPourcent(25) - 14 - 37);
        windowListShader.setWidth(stage.getViewport().getWorldWidth() / 2 - ScreenUtils.getWidhtScreenPourcent(12) - ScreenUtils.getWidhtScreenPourcent(24) - 14);
        windowListShader.setX(ScreenUtils.getWidhtScreenPourcent(12) + 14);
        windowListShader.setY(37);

        windowLog.setMovable(false);
        windowLog.setResizable(false);
        windowLog.setKeepWithinStage(false);
        windowLog.getTitleTable().add(clearLogBtn);
        windowLog.getTitleTable().pad(20);
        windowLog.align(Align.topLeft);
        windowLog.setColor(0.1f, 0.1f, 0.1f, 0.67f);
        windowLog.setHeight(ScreenUtils.getHeightScreenPourcent(25) - 14 - 37);
        windowLog.setWidth(stage.getViewport().getWorldWidth() / 2 - ScreenUtils.getWidhtScreenPourcent(12) - 14 - windowListShader.getWidth());
        windowLog.setX(ScreenUtils.getWidhtScreenPourcent(12) + windowListShader.getWidth() + 14);
        windowLog.setY(37);

        windowFS.setMovable(false);
        windowFS.setResizable(false);
        windowFS.setKeepWithinStage(false);
        windowFS.getTitleTable().add(savetBtn).padRight(5);
        windowFS.getTitleTable().add(formatBtn).padRight(5);
        windowFS.getTitleTable().add(compileBtn).padRight(5);
        windowFS.getTitleTable().add(runBtn).padRight(5);
        windowFS.getTitleTable().add(closeBtn);
        windowFS.getTitleTable().pad(20);
        windowFS.setColor(0.1f, 0.1f, 0.1f, 0.67f);
        windowFS.setX(ScreenUtils.getWidhtScreenPourcent(50));
        windowFS.setY(37);
    }

    AbsBaseShader shader;
    public void layoutUi() {

        rootVS.defaults().expand().fill().grow();
        rootFS.defaults().expand().fill().grow();
        rootVS.add(codeVSEditor).row();
        rootFS.add(codeFSEditor).row();

        rootList.add(shaderScroll).fill().expand().row();
        windowListShader.add(rootList)
        .height(ScreenUtils.getHeightScreenPourcent(25) - 14 - 86)
        .width(stage.getViewport().getWorldWidth() / 2 - ScreenUtils.getWidhtScreenPourcent(12) - ScreenUtils.getWidhtScreenPourcent(24) - 30);
        windowListShader.pack();

        rootLog.add(logLabel).expand().fill().pad(10).row();
        windowLog.add(rootLog);

        windowVS.add(rootVS)
                .height(stage.getViewport().getWorldHeight() - ScreenUtils.getHeightScreenPourcent(25) - 64)
                .width(stage.getViewport().getWorldWidth() / 2 - ScreenUtils.getWidhtScreenPourcent(12) - 31);
        windowVS.pack();

        windowFS.add(rootFS)
                .height(stage.getViewport().getWorldHeight() - 120)
                .width(stage.getViewport().getWorldWidth() / 2 - ScreenUtils.getWidhtScreenPourcent(12) - 34);
        windowFS.pack();

        stage.addActor(windowVS);
        stage.addActor(windowFS);
        stage.addActor(windowListShader);
        stage.addActor(windowLog);

        codeVSEditor.setxOff(windowVS.getX());
        codeVSEditor.setyOff(windowVS.getY());
        codeFSEditor.setxOff(windowFS.getX());
        codeFSEditor.setyOff(windowFS.getY());

        loadShader(shaderList.getItems().get(0));
    }

    public void eventsUi() {

        createNewBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });

        shaderList.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                loadShader(shaderList.getSelected());
            }
        });
        savetBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(shader == null){
                    logLabel.setText("No shader selected");
                    return;
                }
                prepareShaderToCompile();
                if(!shaderProgram.isCompiled()){
                    displayErrorCompile();
                    return;
                }
                if(shader.writeShaderInFile(codeVSEditor.getAllText(), codeFSEditor.getAllText()))
                    logLabel.setText("Shader file saved !!");
                else
                    logLabel.setText("Error during saving !!");
            }
        });

        formatBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                codeVSEditor.format();
                codeFSEditor.format();
            }
        });

        runBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                prepareShaderToCompile();
                if(shaderProgram.isCompiled()) {
                    shader.setNewProgram(codeVSEditor.getAllText(), codeFSEditor.getAllText());
                    //PostProcessorManager.get().rebindShaders();
                    logLabel.setText("Shader running !!");
                }else{
                    displayErrorCompile();
                }
            }
        });

        encryptBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(shader == null){
                    logLabel.setText("No shader selected");
                    return;
                }
                if(ShaderHelper.get().getShaderLoader().encrypt(codeVSEditor.getAllText(), codeFSEditor.getAllText(), shader.getVertexFileName(), shader.getFragmentFileName()))
                    logLabel.setText("File encrypted !!");
                else
                    logLabel.setText("Error during encrypting !!");
            }
        });

        closeBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
            }
        });

        clearLogBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                logLabel.setText("");
            }
        });

        compileBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                prepareShaderToCompile();
                if(!shaderProgram.isCompiled()){
                    displayErrorCompile();
                }else{
                    logLabel.setText("Compilation success !!");
                }
            }
        });

        InputMultiplexer inputMultiplexer = (InputMultiplexer) Gdx.input.getInputProcessor();
        inputMultiplexer.addProcessor(stage);
    }

    private void prepareShaderToCompile(){
        shaderProgram = new ShaderProgram(codeVSEditor.getAllText(), codeFSEditor.getAllText());
        codeVSEditor.setLineError(null);
        codeFSEditor.setLineError(null);
    }

    private void displayErrorCompile(){
        String log = shaderProgram.getLog();
        logLabel.setText(log);
        extractLineError(log);
    }

    Array<Integer> listLine = new Array<Integer>();
    public void extractLineError(String log){
        listLine.clear();
        Pattern p = Pattern.compile(Pattern.quote("ERROR: 0:") + "(.*?)" + Pattern.quote(": "));
        if(log.contains("Vertex shader") && log.contains("Fragment shader")){
            String logVS = log.substring(0, log.indexOf("Fragment shader"));
            Matcher m = p.matcher(logVS);
            while (m.find()) {
                listLine.add(Integer.valueOf(m.group(1)));
                Gdx.app.log("", "result VS : "+m.group(1));
            }
            codeVSEditor.setLineError(listLine);
            listLine.clear();
            String logFS = log.substring(log.indexOf("Fragment shader"), log.length());
            m = p.matcher(logFS);
            while (m.find()) {
                listLine.add(Integer.valueOf(m.group(1)));
                Gdx.app.log("", "result FS: "+m.group(1));
            }
            codeFSEditor.setLineError(listLine);
        }else{
            if(log.contains("Vertex shader")){
                Matcher m = p.matcher(log);
                while (m.find()) {
                    listLine.add(Integer.valueOf(m.group(1)));
                    Gdx.app.log("", "result VS : "+m.group(1));
                }
                codeVSEditor.setLineError(listLine);
            }
            if(log.contains("Fragment shader")){
                Matcher m = p.matcher(log);
                while (m.find()) {
                    listLine.add(Integer.valueOf(m.group(1)));
                    Gdx.app.log("", "result FS : "+m.group(1));
                }
                codeFSEditor.setLineError(listLine);
            }
        }
    }

    public void show() {
        windowFS.getCells().get(0).setActor(rootFS);
        windowVS.getCells().get(0).setActor(rootVS);
        windowLog.getCells().get(0).setActor(rootLog);
        windowListShader.getCells().get(0).setActor(rootList);
        InputMultiplexer inputMultiplexer = (InputMultiplexer) Gdx.input.getInputProcessor();
        inputMultiplexer.addProcessor(stage);
    }

    public void hide() {
        windowFS.getCells().get(0).setActor(null);
        windowVS.getCells().get(0).setActor(null);
        windowLog.getCells().get(0).setActor(null);
        windowListShader.getCells().get(0).setActor(null);
        InputMultiplexer inputMultiplexer = (InputMultiplexer) Gdx.input.getInputProcessor();
        inputMultiplexer.removeProcessor(stage);
    }

    protected Table table(Actor... actors) {
        Table table = new Table(skin);
        table.defaults().space(6);
        table.add(actors);
        return table;
    }

    protected void addSeparator(Table actor) {
        actor.add(new Image(skin.newDrawable("white", new Color(0x4e4e4eff)))).height(1).fillX().colspan(2).pad(1, 0, 1, 0).row();
    }

    public void draw(float delta) {
        stage.act();
        stage.draw();
    }

    public void loadShader(String shaderName) {
        logLabel.setText("");
        codeVSEditor.setLineError(null);
        codeFSEditor.setLineError(null);
        shader = ShaderHelper.get().getShader(shaderName);
        if(shader != null && shader.program != null){
            Gdx.app.log("", "== loadShader : "+shaderName);
            codeVSEditor.setText(shader.program.getVertexShaderSource());
            codeFSEditor.setText(shader.program.getFragmentShaderSource());
            encryptBtn.setVisible(shader.getVertexFileName().contains(".glsl"));
        }else if(shader != null){
            String shaderText = Gdx.files.local(ShaderHelper.SHADER_BASE_PATH+"/"+shader.getVertexFileName().replace(":VS", "")).readString();
            if(shader.getVertexFileName().contains(".sglsl")){
                shaderText = ShaderHelper.get().getShaderLoader().deccrypt(shaderText);
            }
            Gdx.app.log("", "shaderText : "+shaderText);
            codeVSEditor.setText(shaderText.substring(0, shaderText.indexOf("[FS]")).replace("[VS]\n",""));
            shaderText = Gdx.files.local(ShaderHelper.SHADER_BASE_PATH+"/"+shader.getFragmentFileName().replace(":FS", "")).readString();
            if(shader.getFragmentFileName().contains(".sglsl")){
                shaderText = ShaderHelper.get().getShaderLoader().deccrypt(shaderText);
            }
            codeFSEditor.setText(shaderText.substring(shaderText.indexOf("[FS]"), shaderText.length()).replace("[FS]\n",""));
            encryptBtn.setVisible(shader.getVertexFileName().contains(".glsl"));
            logLabel.setText("Shader loading error but file loaded");
        }else {
            logLabel.setText("Impossible to load");
            return;
        }
        windowVS.getTitleLabel().setText("Vertex shader : ["+shader.getVertexFileName()+"]");
        windowFS.getTitleLabel().setText("Fragment shader : ["+shader.getFragmentFileName()+"]");
    }

    private static ShaderEditorUi instance;

    public static ShaderEditorUi get() {
        if (instance == null) instance = new ShaderEditorUi();
        return instance;
    }
}
