package com.glsl.editor;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.FloatArray;
import com.glsl.editor.scanner.GlslScanner;
import com.wulfazar.theredcollapse.engine.core.texteditor.scanner.GlslScanner;

import java.util.ArrayList;


public class Line extends ArrayList<Element> {

    private String cachedFullText = "";

    public int textLenght() {
        return text().length();
    }

    public final GlyphLayout layout = new GlyphLayout();
    public final FloatArray glyphPositions = new FloatArray();
    float fontOffset;
    public void calculateGlyphLine(BitmapFont font) {
        glyphPositions.clear();
        float x = 0;
        layout.setText(font, text());
        if (layout.runs.size > 0) {
            GlyphLayout.GlyphRun run = layout.runs.first();
            FloatArray xAdvances = run.xAdvances;
            fontOffset = xAdvances.first();
            for (int i = 1, n = xAdvances.size; i < n; i++) {
                glyphPositions.add(x);
                x += xAdvances.get(i);
            }
        } else
            fontOffset = 0;
        glyphPositions.add(x);
    }

    public float getWidthToTheCursor(int col){
        if(col <= 0 ) return 0;
        try{
            return glyphPositions.get(col);
        }catch (Exception e){
            return 0;
        }
    }

    public String text() {
        String s = "";
        for (Element e : this) {
            s += e.text;
        }
        return s;
    }

    public char charAt(int col) {
        try {
            return text().charAt(col);
        } catch (StringIndexOutOfBoundsException e) {
            return ' ';
        }
    }

    public void buildString() {
        setCachedFullText(text());
    }

    public String getCachedFullText() {
        return cachedFullText;
    }

    public void setCachedFullText(String cachedFullText) {
        this.cachedFullText = cachedFullText;
    }

    public int getPadding() {
        int sum = 0;
        for (int i = 0; i < this.size(); i++) {
            Element e = this.get(i);
            if (e.kind == GlslScanner.Kind.NEWLINE) {
                sum += e.countSpaces();
                //continue;
            } else if (e.kind == GlslScanner.Kind.NORMAL) {
                sum += e.countSpaces();
            } else {
                break;
            }
        }

        return sum;
    }
}
