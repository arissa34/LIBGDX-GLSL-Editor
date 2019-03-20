package com.glsl.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.github.abrarsyed.jastyle.ASFormatter;
import com.github.abrarsyed.jastyle.FormatterHelper;
import com.github.abrarsyed.jastyle.constants.EnumFormatStyle;
import com.glsl.editor.scanner.AbsScanner;
import com.glsl.editor.scanner.ScannerEnum;
import com.glsl.utils.ChronoUtils;

import java.io.StringReader;

public class CodeEditor extends Widget implements InputProcessor {
    static private final char BACKSPACE = '\b';
    static private final char ENTER_DESKTOP = '\r';
    static private final char ENTER_ANDROID = '\n';
    static private final char TAB = '\t';
    static private final char DELETE = 127;
    static private final char BULLET = 149;
    static private final char APPLE = 63;
    ShapeRenderer shape;
    CodeEditorStyle style;
    private final static int GUTTER_PADDING = 10;
    private static final String TAG = "CodeEditor";
    private static final float LINE_PADDING = 2;

    private Array<Line> lines;
    boolean disabled;
    private String text = "";

    private Caret caret;
    private float blinkTime = 0.32f;

    private long lastBlink;
    private boolean cursorOn;
    private Clipboard clipboard; // THE TEXT AREA
    private InputListener inputListener;
    KeyRepeatTask keyRepeatTask = new KeyRepeatTask();
    float keyRepeatInitialTime = 0.4f;
    float keyRepeatTime = 0.1f;
    private Slider scrollbar;
    private Rectangle scissors;
    private Rectangle clipBounds;
    private boolean cursorIsIn = false;
    private float xOff, yOff;
    private ScannerEnum scannerEnum;
    private Skin skin;
    private ActionBuffer actionBuffer;

    class KeyRepeatTask extends Task {
        int keycode;

        public void run() {
            inputListener.keyDown(null, keycode);
        }
    }

    public CodeEditor(Skin skin, ScannerEnum scannerEnum) {
        this.skin = skin;
        this.scannerEnum = scannerEnum;
        style = skin.get(CodeEditorStyle.class);
        lines = new Array<Line>();

        //getFont().getData().setScale(1.5f, 1.5f);

        shape = new ShapeRenderer();
        actionBuffer = new ActionBuffer();

        scrollbar = new Slider(0, 100, 1, true, skin);
        scrollbar.setWidth(16);
        scrollbar.setValue(100);

        this.text = "";
        this.parse(this.text);
        this.clipboard = Gdx.app.getClipboard();

        setWidth(getPrefWidth());
        setHeight(getPrefHeight());

        caret = new Caret(this.lines);
        initializeKeyboard();

        this.scissors = new Rectangle();
        this.clipBounds = new Rectangle(0, 0, 0, 0);
    }

    private void initializeKeyboard() {
        addListener(inputListener = new ClickListener() {

            @Override
            public boolean handle(Event event) {
                if (!FocusEvent.class.isInstance(event) && ((InputEvent) event).getType() == InputEvent.Type.scrolled) {
                    return true;
                } else {
                    return super.handle(event);
                }
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, int amount) {
                return super.scrolled(event, x, y, amount);
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {

                super.enter(event, x, y, pointer, fromActor);
                if (pointer == -1) {
                    cursorIsIn = true;
                    enableInput();
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {

                super.exit(event, x, y, pointer, toActor);
                if (pointer == -1) {
                    cursorIsIn = false;
                    disableInput();
                }
            }

            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                if (disabled) return false;
                if (!super.touchDown(event, x, y, pointer, button)) return false;
                if (pointer == 0 && button != 0) return false;
                caret.clearSelection();
                int row = yToRow(y) + caret.getRowScrollPosition();

                boolean shift = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT) || Gdx.input.isKeyPressed(59) ;
                if(shift){
                    caret.startSelection();
                }
                caret.setCursorPosition(xToCol(x, row), row);
                Stage stage = getStage();
                if (stage != null) stage.setKeyboardFocus(CodeEditor.this);
                //Core.shared().setCurrentCursor(Core.CURSOR_TEXT);
                return true;
            }

            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                onTouchDraged(x, y);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                if(caret.getCol() == caret.getSelectionStartCol() && caret.getRow() == caret.getSelectionStartRow()){
                    caret.clearSelection();
                }
            }

            public boolean keyDown(InputEvent event, int keycode) {
                return onKeyDown(keycode);
            }

            public boolean keyUp(InputEvent event, int keycode) {
                if (disabled) return false;
                keyRepeatTask.cancel();
                return true;
            }

            public boolean keyTyped(InputEvent event, char character) {
                return onKeyTyped(character);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(chrono.stopTimer(ChronoUtils.TimeUnit.MILLISECONDE) < tempoClick){
                    selectWord();
                }
                chrono.startTimer();
                super.clicked(event, x, y);
            }

        });
    }

    private ChronoUtils chrono = new ChronoUtils();
    private final float tempoClick = 300;

    public void onTouchDraged(float x, float y) {
        lastBlink = 0;
        cursorOn = false;

        int row = yToRow(y) + caret.getRowScrollPosition();
        int col = xToCol(x, row);

        int rd = row - caret.getRow();

        boolean moveLeft = col - caret.getCol() <= 0;
        caret.setCursorPosition(col, row);

        if (moveLeft) {
            updateScrollInLeftDirectionForCol();
        } else {
            updateScrollInRightDirectionForCol();
        }

        if (rd == 0) {
        } else if (rd < 0) {
            updateScrollInDownDirectionForRow();
        } else {
            updateScrollInUpDirectionForRow();
        }

        caret.startSelection();
    }

    public String getAllText() {
        String out = "";
        for (int i = 0; i < this.lines.size; i++) {
            Line line = this.lines.get(i);
            out += line.getCachedFullText();
            if (i != this.lines.size - 1) {
                out += '\n';
            }
        }

        return out;
    }

    public String buildStringFromLines() {
        String s = "";
        for (int i = 0; i < this.lines.size; i++) {
            Line line = lines.get(i);
            s += line.getCachedFullText();
            if (i != this.lines.size - 1) {
                s += "\n";
            }
        }
        return s;
    }

    protected boolean onKeyTyped(char character) {
        if (disabled) return false;
        Stage stage = getStage();
        if (stage != null && stage.getKeyboardFocus() == this) {
            boolean ctrl = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT) || Gdx.input.isKeyPressed(APPLE);
            if(ctrl){
                return false;
            }
            if (character == TAB) {
                caret.clearSelection();
                insertText("\t");
                //caret.incCol(2);
                caret.incCol();
                actionBuffer.addTypeAction(String.valueOf("\t"), caret.getRow(), caret.getCol());
            } else if (character == DELETE) {
                deleteRight(false);
            } else if (character == BACKSPACE) {
                delete(false);
            } else if (character == ENTER_DESKTOP) {
                insertText("\n");
                caret.incRow();
                int spaces = caret.getPrevPadding();
                caret.setCol(0);
                actionBuffer.addTypeAction(String.valueOf("\n"), caret.getRow(), caret.getCol());
                for (int i = 0; i < spaces; i++) {
                    insertText(" ");
                }
                caret.setCol(spaces);
                updateScrollInDownDirectionForRow();

            } else if (containsCharacter(character)) {
                insertText(String.valueOf(character));
                actionBuffer.addTypeAction(String.valueOf(character), caret.getRow(), caret.getCol()+1);
                caret.incCol(1);
                updateScrollInLeftDirectionForCol();
            } else {
                return false;
            }

            return true;
        }
        return false;
    }

    public boolean containsCharacter(char character) {
        return getFont().getData().getGlyph(character) != null;
    }

    protected boolean onKeyDown(int keycode) {
        if (disabled) return false;
        lastBlink = 0;
        cursorOn = false;
        Stage stage = getStage();

        //Gdx.app.log("", "keycode : "+keycode);

        if (stage != null && stage.getKeyboardFocus() == this) {

            boolean repeat = false;
            boolean ctrl = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT) || Gdx.input.isKeyPressed(APPLE);
            boolean shift = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT) || Gdx.input.isKeyPressed(59) ; //59 SHIFT AZERTY ON MAC

            if (ctrl) {
                if(shift){
                    if (keycode == 51) {//keycode == Keys.Z //TODO AZERTY MAPPING
                        crtlShiftZ();
                        return true;
                    }
                }
                if (keycode == 50) {//keycode == Keys.V //TODO AZERTY MAPPING
                    paste();
                    return true;
                }
                if (keycode == 31) {//keycode == Keys.C || keycode == Keys.INSERT //TODO AZERTY MAPPING
                    copy();
                    return true;
                }
                if (keycode == 52) {//keycode == Keys.X || keycode == Keys.DEL //TODO AZERTY MAPPING
                    cut();
                    return true;
                }

                if (keycode == 45) {//keycode == Keys.A //TODO AZERTY MAPPING
                    caret.clearSelection();
                    caret.selectAll();
                    return true;
                }

                if (keycode == 32) {//keycode == Keys.D //TODO AZERTY MAPPING
                    duplicate();
                    return true;
                }

                if (keycode == 51) {//keycode == Keys.Z //TODO AZERTY MAPPING
                    crtlZ();
                    return true;
                }

            }

            if (keycode == Keys.PAGE_UP) {
                pageUp();
                updateScrollInUpDirectionForRow();
                repeat = true;
            }

            if (keycode == Keys.PAGE_DOWN) {
                pageDown();
                updateScrollInDownDirectionForRow();
                repeat = true;
            }

            if (keycode == Keys.LEFT) {
                if (shift) {
                    caret.startSelection();
                }
                if (ctrl) {
                    caret.moveByWordInLeft();
                } else {
                    if (caret.haveSelection() && !shift) {
                        caret.clearSelection();
                    } else {
                        caret.moveOneCharLeft();
                    }
                }

                updateScrollInRightDirectionForCol();

                repeat = true;
            }

            if (keycode == Keys.RIGHT) {
                if (shift) {
                    caret.startSelection();
                }
                if (ctrl) {
                    caret.moveByWordInRight();
                } else {
                    if (caret.haveSelection() && !shift) {
                        caret.clearSelection();
                    } else {
                        caret.moveOneCharRight();
                    }
                }

                updateScrollInLeftDirectionForCol();
                repeat = true;
            }

            if (keycode == Keys.UP && caret.getRow() > 0) {
                if (shift) {
                    caret.startSelection();
                } else {
                    caret.clearSelection();
                }
                caret.moveRowUp();
                updateScrollInRightDirectionForCol();
                updateScrollInUpDirectionForRow();
                repeat = true;
            }

            if (keycode == Keys.DOWN && caret.getRow() < this.lines.size - 1) {
                if (shift) {
                    caret.startSelection();
                } else {
                    caret.clearSelection();
                }
                caret.moveRowDown();
                updateScrollInRightDirectionForCol();
                updateScrollInDownDirectionForRow();
                repeat = true;
            }

            if (keycode == Keys.HOME) {
                if (shift) {
                    caret.startSelection();
                }
                caret.setColHome();
                updateScrollInRightDirectionForCol();
            }

            if (keycode == Keys.END) {
                if (shift) {
                    caret.startSelection();
                }
                caret.setColEnd();
                updateScrollInLeftDirectionForCol();
            }

            if (repeat && (!keyRepeatTask.isScheduled() || keyRepeatTask.keycode != keycode)) {
                keyRepeatTask.keycode = keycode;
                keyRepeatTask.cancel();
                Timer.schedule(keyRepeatTask, keyRepeatInitialTime, keyRepeatTime);
            }

            return true;
        } else {
            return false;
        }
    }

    private void pageUp() {
        int mv = caret.getRow() - 1 - visibleLinesCount();
        if (mv < 0) {
            mv = 0;
        }
        caret.clearSelection();
        caret.setRow(mv);
    }

    private void pageDown() {
        int mv = caret.getRow() - 1 + visibleLinesCount();
        if (mv > this.lines.size - 1) {
            mv = this.lines.size - 1;
        }
        caret.clearSelection();
        caret.setRow(mv);
    }

    private void updateScrollInUpDirectionForRow() {
        if (caret.getRow() < caret.getRowScrollPosition()) {
            caret.setRowScrollPosition(caret.getRow());
        }
    }

    private void updateScrollInDownDirectionForRow() {
        if (caret.getRow() >= caret.getRowScrollPosition() + visibleLinesCount()) {
            caret.setRowScrollPosition(caret.getRow() + 1 - visibleLinesCount());
        }
    }

    private void updateScrollInRightDirectionForCol() {
        if (caret.getCol() < caret.getColScrollPosition()) {
            caret.setColScrollPosition(caret.getCol());
        }
    }

    private void updateScrollInLeftDirectionForCol() {
        if (caret.getCol() > visibleCharsCount() - 2) {
            caret.setColScrollPosition(caret.getCol() - visibleCharsCount() + 1);
        } else {
            caret.setColScrollPosition(0);
        }
    }

    public void insertText(String ins) {
        if (caret.haveSelection()) {
            delete(false);
        }

        String lineText = getAllText();
        int pos = caret.getCaretPosition();

        String finalText = lineText.substring(0, pos) + ins;
        if (pos < lineText.length()) {
            finalText += lineText.substring(pos, lineText.length());
        }

        parse(finalText);
    }

    private void selectWord() {
        caret.clearSelection();
        caret.moveByWordInLeft();
        caret.startSelection();
        caret.moveByWordInRight();
        caret.setRowScrollPosition(caret.getRowScrollPosition());
        caret.setColScrollPosition(caret.getColScrollPosition());
    }

    private void deleteRight(boolean isFromBuffer) {
        remove(1, isFromBuffer);
    }

    private void delete(boolean isFromBuffer) {
        remove(-1, isFromBuffer);
    }

    private void remove(int i, boolean isFromBuffer) {
        String lineText = getAllText();
        if (lineText.length() == 0) {
            return;
        }
        int pos = Math.max(0, caret.getCaretPosition());

        if (caret.haveSelection()) {
            int startPos = caret.getSelectionCaretPosition();

            int from = Math.min(pos, startPos);
            int to = Math.max(pos, startPos);

            String textToRemove = lineText.substring(from, to);
            String finalText = lineText.substring(0, from);
            if (pos < lineText.length()) {
                finalText += lineText.substring(to, lineText.length());
            }
            caret.moveToSelectionStart();
            caret.clearSelection();
            if(!isFromBuffer) actionBuffer.addDeleteAction(textToRemove, caret.getRow(), caret.getCol());

            parse(finalText);
        } else {
            String finalText = null;
            if (i == -1) {
                String textToRemove = lineText.substring(pos-1, pos);
                finalText = lineText.substring(0, pos + i);
                if (pos < lineText.length()) {
                    finalText += lineText.substring(pos, lineText.length());
                }
                caret.moveOneCharLeft();
                if(!isFromBuffer) actionBuffer.addDeleteAction(textToRemove, caret.getRow(), caret.getCol());
            } else {
                finalText = lineText.substring(0, pos);
                if (pos + 1 < lineText.length()) {
                    finalText += lineText.substring(pos + i, lineText.length());
                }
                //caret.moveOneCharRight();
            }

            parse(finalText);
        }

        this.updateScrollInUpDirectionForRow();
    }

    private void duplicate(){
        if (caret.haveSelection()) {
            duplicateSelection();
        }else{
            duplicateLine();
        }
    }

    private void duplicateSelection(){
        int pos = caret.getCaretPosition();
        if (caret.haveSelection()) {
            int startPos = caret.getSelectionCaretPosition();
            int from = Math.min(pos, startPos);
            int to = Math.max(pos, startPos);
            String copyText = getAllText().substring(from, to);
            caret.clearSelection();
            insertText(copyText);
            actionBuffer.addTypeAction(copyText, caret.getRow(), caret.getCol());
            caret.moveForwardByCharCount(copyText.length());
        }
    }

    private void duplicateLine(){
        caret.setCursorPosition(0, caret.getRow());
        caret.startSelection();
        caret.setCursorPosition(caret.getCurrentLine().textLenght(), caret.getRow());
        int pos = caret.getCaretPosition();
        if (caret.haveSelection()) {
            int startPos = caret.getSelectionCaretPosition();
            int from = Math.min(pos, startPos);
            int to = Math.max(pos, startPos);
            String copyText = getAllText().substring(from, to);
            caret.clearSelection();
            insertText("\n"+copyText);
            actionBuffer.addTypeAction("\n"+copyText, caret.getRow(), caret.getCol());
            caret.moveRowDown();
        }
    }

    private void crtlZ(){
        if(actionBuffer.bufferNotEmpty()){
            caret.clearSelection();
            ActionBuffer.Action action = actionBuffer.getLastAction();
            caret.setCursorPosition(action.col, action.row);
            if(action.type == ActionBuffer.ActionEnum.TYPE){
                if(action.text.length() > 1){
                    caret.startSelection();
                    caret.moveForwardByCharCount(action.text.length());
                }
                delete(true);
            }else if(action.type == ActionBuffer.ActionEnum.REMOVE){
                insertText(action.text);
                caret.moveForwardByCharCount(action.text.length());
            }
        }
    }

    private void crtlShiftZ(){
        if(actionBuffer.hasNextAction()){
            ActionBuffer.Action action = actionBuffer.getNextAction();
            caret.setCursorPosition(action.col, action.row);
            if(action.type == ActionBuffer.ActionEnum.REMOVE){
                if(action.text.length() > 1){
                    caret.startSelection();
                    caret.moveForwardByCharCount(action.text.length());
                }
                delete(true);
            }else if(action.type == ActionBuffer.ActionEnum.TYPE){
                insertText(action.text);
                caret.moveForwardByCharCount(action.text.length());
            }
        }
    }

    private void cut() {
        if (caret.haveSelection()) {
            copy();
            delete(false);
        }
    }

    private void copy() {
        String lineText = getAllText();
        int pos = caret.getCaretPosition();

        if (caret.haveSelection()) {
            int startPos = caret.getSelectionCaretPosition();

            int from = Math.min(pos, startPos);
            int to = Math.max(pos, startPos);

            String copyText = lineText.substring(from, to);
            clipboard.setContents(copyText);
        }
    }

    private void paste() {
        String content = clipboard.getContents();

        if (content != null) {
            insertText(content);
            actionBuffer.addTypeAction(content, caret.getRow(), caret.getCol());
            caret.moveForwardByCharCount(content.length());
            caret.clearSelection();
        }
    }

    public int xToCol(float x, int row) {
        x = (int) ((x - gutterWidth()));
        caret.setCursorPosition(0, row);
        Line line = caret.getLineForRow(row);
        if(line == null) return 0;
        line.calculateGlyphLine(getFont());
        int n = line.glyphPositions.size-1;
        int col = n;
        float[] glyphPositions = line.glyphPositions.items;
        for (int i = 0; i < n; i++) {
            if (glyphPositions[i] > x && i>0) {
                if (glyphPositions[i] - x <= x - glyphPositions[i - 1]){
                    col = i;
                    break;
                }
                col = i - 1;
                break;
            }
        }
        int c = col;
        if (c < 0) {
            c = 0;
        }
        return c;
    }

    public int yToRow(float y) {
        int r = (int) Math.floor((getHeight() - y - GUTTER_PADDING) / getLineHeight());
        if (r < 0) {
            r = 0;
        }
        return r;
    }

    public float getLineHeight() {
        return getFont().getLineHeight() + LINE_PADDING;// ;
    }

    private int gutterWidth() {
        return 40;
    }

    private int visibleLinesCount() {
        return (int) (this.getHeight() / getLineHeight());
    }

    private int visibleCharsCount() {
        return (int) ((this.getWidth() - gutterWidth() - GUTTER_PADDING) / getFont().getSpaceXadvance());
    }

    @Override
    public void draw(Batch renderBatch, float parentAlpha) {
        Stage stage = getStage();
        boolean focused = stage != null && stage.getKeyboardFocus() == this;

        final BitmapFont font = getFont();
        final Drawable cursorPatch = style.cursor;

        Color color = getColor();
        float sx = 0;//getX();
        float sy = 0;//getY();

        float width = getWidth();
        float height = getHeight();

        int fromLine = caret.getRowScrollPosition();
        int toLine = Math.min(fromLine + visibleLinesCount(), this.lines.size);

        int fromChar = caret.getColScrollPosition();
        int toChar = fromChar + visibleCharsCount();


        float size = 0;
        for (int i = 0; i < caret.getColScrollPosition(); i++) {
            bounds.setText(font, caret.getChar(i) + "");
            size += bounds.width;
        }
        int xOffset = (int) size;

        renderBatch.end();

        shape.setProjectionMatrix(renderBatch.getProjectionMatrix());

        Gdx.gl.glEnable(GL20.GL_BLEND);
        /*** RENDER BACKGROUND EDITOR ****/
        shape.begin(ShapeType.Filled);
        shape.setColor(0.1f, 0.1f, 0.1f, .65f);
        shape.rect(xOff, yOff, width, height);
        shape.end();

        /*** RENDER BACKGROUND LINES COUNT ****/
        shape.begin(ShapeType.Filled);
        shape.setColor(0.25f, 0.25f, 0.25f, .65f);
        shape.rect(xOff, yOff, gutterWidth() + GUTTER_PADDING / 2, height);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    
    /*shape.begin(ShapeType.Line);
    shape.setColor(0.3f, 0.3f, 0.3f, 1);
    shape.rect(xOff, yOff, width, height);
    shape.end();*/

        clipBounds.set(sx + gutterWidth() + GUTTER_PADDING, sy, width - (gutterWidth() + GUTTER_PADDING), height);
        ScissorStack.calculateScissors(stage.getCamera(), renderBatch.getTransformMatrix(), clipBounds, scissors);
        ScissorStack.pushScissors(scissors);

        if (caret.haveSelection()) {
            int cursorRowStart = Math.min(caret.getSelectionStartRow(), caret.getRow());
            int cursorRowEnd = Math.max(caret.getRow(), caret.getSelectionStartRow());

            int cursorColEnd = caret.getCol();
            int cursorColStart = caret.getSelectionStartCol();
            if (caret.getRow() < caret.getSelectionStartRow()) {
                cursorColEnd = caret.getSelectionStartCol();
                cursorColStart = caret.getCol();
            }

            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            shape.begin(ShapeType.Filled);
            shape.setColor(1.0f, 1.0f, 1.0f, 0.1f);

            if (cursorRowStart == cursorRowEnd && !caret.haveSelection()) {
                shape.rect(xOff,
                        (yOff + height) - (caret.getRow() + 1 - caret.getRowScrollPosition()) * getLineHeight()
                        , width, getLineHeight());
            } else {
                int rowCount = Math.abs(cursorRowStart - cursorRowEnd) - 1;

                Line line = caret.getLineForRow(cursorRowStart);
                line.calculateGlyphLine(getFont());
                size = line.getWidthToTheCursor(cursorColStart);

                shape.rect(xOff + gutterWidth() + GUTTER_PADDING + size,
                        (yOff + height) - (cursorRowStart + 1 - caret.getRowScrollPosition()) * getLineHeight(),
                        width - gutterWidth() - GUTTER_PADDING - size,
                        getLineHeight()
                );

                for (int i = 0; i < rowCount; i++) {
                    shape.rect(xOff + gutterWidth() + GUTTER_PADDING,
                            (yOff + height) - (cursorRowStart + i + 2 - caret.getRowScrollPosition()) * getLineHeight(),
                            width - gutterWidth() - GUTTER_PADDING,
                            getLineHeight()
                    );
                }

                line = caret.getLineForRow(cursorRowEnd);
                line.calculateGlyphLine(getFont());
                size = line.getWidthToTheCursor(cursorColEnd);
                shape.rect(xOff + gutterWidth() + GUTTER_PADDING,
                        (yOff + height) - (cursorRowEnd + 1 - caret.getRowScrollPosition()) * getLineHeight(),
                        (size),
                        getLineHeight()
                );
            }

            shape.end();

            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        if (focused && !caret.haveSelection()) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shape.begin(ShapeType.Filled);
            shape.setColor(1.0f, 1.0f, 1.0f, 0.1f);
            shape.rect(xOff,
                    (yOff + height) - (caret.getRow() + 1 - caret.getRowScrollPosition()) * getLineHeight()
                    , width, getLineHeight());
            shape.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        renderBatch.begin();
        renderBatch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        /*** RENDER LINES ****/
        for (int y = 0; y < toLine - fromLine; y++) {
            Line line = this.lines.get(fromLine + y);
            float linePosY = (height + font.getDescent()) - y * getLineHeight();
            float lineElementX = 0;

            for (int x = 0; x < line.size(); x++) {
                Element elem = line.get(x);
                bounds.setText(font, elem.text);
                font.setColor(elem.kind.getColor());
                font.draw(renderBatch, elem.text, gutterWidth() + GUTTER_PADDING + lineElementX , linePosY);

                lineElementX += bounds.width;
                //if (lineElementX > toChar) {
                //  break;
                //}
            }
        }
        /*** RENDER CURSOR ****/
        if (focused && !disabled) {
            blink();
            if (cursorOn && cursorPatch != null) {

                Line line = caret.getCurrentLine();
                line.calculateGlyphLine(getFont());
                size = line.getWidthToTheCursor(caret.getCol());
                cursorPatch.draw(renderBatch,
                        gutterWidth() + GUTTER_PADDING + size +font.getData().cursorX ,
                        (height) - (caret.getRow() - caret.getRowScrollPosition() + 1) * getLineHeight(),
                        cursorPatch.getMinWidth(),
                        getLineHeight());
            }
        }

        renderBatch.flush();
        ScissorStack.popScissors();

        /*** RENDER LINES COUNT ****/
        for (int y = 0; y < toLine - fromLine; y++) {
            int line = fromLine + y + 1;
            String lineNumberString = Integer.toString(line);
            float linePosY = (height + font.getDescent()) - y * getLineHeight();
            if (listLineError.contains(line, true)) {
                font.setColor(Color.RED);
                lineNumberString += " >";
                bounds.setText(font, lineNumberString);
            } else {
                font.setColor(Color.WHITE);
                bounds.setText(font, lineNumberString);
            }
            font.draw(renderBatch, lineNumberString, gutterWidth() - bounds.width, linePosY);
        }

        // font.setColor(Color.WHITE);
        // font.draw(renderBatch, "Row " + caret.getRow() + " Col " + caret.getCol() +
        // " Char " + String.valueOf(caret.getCurrentChar()) +
        // " Scroll Col: " + caret.getColScrollPosition() +
        // " Scroll Row: " + caret.getRowScrollPosition() +
        // " Caret position: " + caret.getCaretPosition(),
        //         gutterWidth() + GUTTER_PADDING ,
        //         getLineHeight() + GUTTER_PADDING
        // );

        //scrollbar.setPosition(sx + getWidth(), sy);
        //scrollbar.setHeight(height);

        //scrollbar.setRange(0, lines.size()); //- visibleLinesCount()
        //int scroll = (int) (scrollbar.getMaxValue() - scrollbar.getValue());
        //caret.setRowScrollPosition(scroll);
        //scrollbar.draw(renderBatch, parentAlpha);
    }

    private Array<Integer> listLineError = new Array<Integer>();

    public void setLineError(Array<Integer> list) {
        listLineError.clear();
        if(list != null) listLineError.addAll(list.toArray());
    }

    GlyphLayout bounds = new GlyphLayout();

    @Override
    public void act(float delta) {
        super.act(delta);
        //scrollbar.act(Gdx.graphics.getDeltaTime());
    }

    private BitmapFont getFont() {
        //Skin skin = CoreHelper.get().getDefaultSkin();
        //return skin.getFont("default-font");
        Skin skin = new Skin(Gdx.files.internal("data/ui/skin/skin.json"));
        BitmapFont font = skin.getFont("default");
        checkTabExistInFont(font);
        return font;
    }

    private void checkTabExistInFont(BitmapFont font ){
        if(!font.getData().hasGlyph('\t')){
            BitmapFont.Glyph glyphSpace = font.getData().getGlyph(' ');
            BitmapFont.Glyph glyph = new BitmapFont.Glyph();
            glyph.width = glyphSpace.width * 5;
            glyph.srcX = glyphSpace.srcX;
            glyph.srcY = glyphSpace.srcY;
            glyph.height = glyphSpace.height;
            glyph.xoffset = glyphSpace.xoffset;
            glyph.yoffset = glyphSpace.yoffset;
            glyph.xadvance = glyphSpace.xadvance * 5;
            glyph.kerning = glyphSpace.kerning;
            glyph.fixedWidth = glyphSpace.fixedWidth;
            font.getData().setGlyph('\t', glyph);
        }
    }

    private void blink() {
        long time = TimeUtils.nanoTime();
        if ((time - lastBlink) / 1000000000.0f > blinkTime) {
            cursorOn = !cursorOn;
            lastBlink = time;
        }
    }

    public void setText(String string) {
        caret.clearSelection();
        caret.setCursorPosition(0, 0);
        this.text = formatText(string);
        parse(this.text);
    }

    public void format(){
        String text = getAllText();
        parse(formatText(text));
    }

    private String formatText(String textToFormat) {
        ASFormatter formatter = new ASFormatter();
        formatter.setFormattingStyle(EnumFormatStyle.KR);
        formatter.setDeleteEmptyLinesMode(true);
        return FormatterHelper.format(new StringReader(textToFormat), formatter);
    }

    public void parse(String text) {
        this.lines.clear();
        AbsScanner js = scannerEnum.getScanner(text);
        AbsScanner.Kind kind;

        Line line = new Line();
        this.lines.add(line);
        while ((kind = js.scan()) != AbsScanner.Kind.EOF) {
            if (kind == AbsScanner.Kind.NEWLINE) {
                line = new Line();
                this.lines.add(line);
            } else {
                line.add(new Element(kind, js.getString()));
            }
        }

        for (Line row : lines) {
            row.buildString();
        }
        scrollbar.setValue(lines.size);
    }

    public void disableInput() {
        disabled = true;
        InputMultiplexer inputMultiplexer = (InputMultiplexer) Gdx.input.getInputProcessor();
        inputMultiplexer.removeProcessor(this);
    }

    public void enableInput() {
        disabled = false;
        InputMultiplexer inputMultiplexer = (InputMultiplexer) Gdx.input.getInputProcessor();
        inputMultiplexer.addProcessor(this);
    }

    static public class CodeEditorStyle {
        public BitmapFont font;
        public Color fontColor, focusedFontColor, disabledFontColor;
        /**
         * Optional.
         */
        public Drawable background, focusedBackground, disabledBackground, cursor, selection;
        /**
         * Optional.
         */
        public BitmapFont messageFont;
        /**
         * Optional.
         */
        public Color messageFontColor;

        public CodeEditorStyle() {
        }

        public CodeEditorStyle(BitmapFont font, Color fontColor, Drawable cursor, Drawable selection, Drawable background) {
            this.background = background;
            this.cursor = cursor;
            this.font = font;
            this.fontColor = fontColor;
            this.selection = selection;
        }

        public CodeEditorStyle(CodeEditorStyle style) {
            this.messageFont = style.messageFont;
            if (style.messageFontColor != null)
                this.messageFontColor = new Color(style.messageFontColor);
            this.background = style.background;
            this.focusedBackground = style.focusedBackground;
            this.disabledBackground = style.disabledBackground;
            this.cursor = style.cursor;
            this.font = style.font;
            if (style.fontColor != null) this.fontColor = new Color(style.fontColor);
            if (style.focusedFontColor != null)
                this.focusedFontColor = new Color(style.focusedFontColor);
            if (style.disabledFontColor != null)
                this.disabledFontColor = new Color(style.disabledFontColor);
            this.selection = style.selection;
        }
    }

    public void addToStage(Stage stage) {
        stage.addActor(this);
        stage.addActor(this.scrollbar);
    }

    public void setxOff(float xOff) {
        this.xOff = xOff;
    }

    public void setyOff(float yOff) {
        this.yOff = yOff;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        if (!cursorIsIn) return false;
        if (amount > 0) {
            if (caret.getRow() > 0) {
                caret.clearSelection();
                caret.moveRowUp();
                updateScrollInRightDirectionForCol();
                updateScrollInUpDirectionForRow();
            }
        } else if (amount < 0) {

            if (caret.getRow() < this.lines.size - 1) {
                caret.clearSelection();
                caret.moveRowDown();
                updateScrollInRightDirectionForCol();
                updateScrollInDownDirectionForRow();
            }
        }
        return false;
    }
}