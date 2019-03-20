package com.glsl.editor.scanner;

public enum ScannerEnum {

    GLSL {
        @Override
        public AbsScanner getScanner(String text) {
            return new GlslScanner(text);
        }
    },
    ;

    public abstract AbsScanner getScanner(String text);
}
