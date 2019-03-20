package com.glsl.editor.scanner;

import com.badlogic.gdx.graphics.Color;
import com.glsl.editor.CharacterIterator;

import java.io.Reader;

public abstract class AbsScanner {


    public enum Kind {
        /** End of file - this token has no text */
        EOF,
        /** End of line - this token has no text */
        NEWLINE,
        /** Normal text - does not include line breaks */
        NORMAL (Color.WHITE),
        /** A keyword */
        KEYWORD (Color.TAN),
        /** A string or character constant */
        STRING (new Color(142.0f / 255.0f, 198.0f / 255.0f, 95.0f / 255.0f, 1.0f)),
        /** A comment - multi line comments are split up and {@link } tokens are inserted */
        COMMENT (new Color(95.0f / 255.0f, 190.0f / 255.0f, 96.0f / 255.0f, 1.0f)),
        /** A javadoc tag inside a comment */
        COMMENT_TAG,
        NUMBER (new Color(127.0f / 255.0f, 197.0f / 255.0f, 120.0f / 255.0f, 1.0f)),
        /** Special word */
        SPECIAL_WORD_FUNCTION (Color.MAGENTA),
        SPECIAL_WORD_PRIMITIVE (new Color(252.0f / 255.0f, 128.0f / 255.0f, 58.0f / 255.0f, 1.0f)),
        SPECIAL_WORD_3 (new Color(252.0f / 255.0f, 98.0f / 255.0f, 98.0f / 255.0f, 1.0f)),
        SPECIAL_WORD_4 (new Color(0f / 255.0f, 153.0f / 255.0f, 204.0f / 255.0f, 1.0f));

        Color color;
        Kind(Color color) {
            this.color = color;
        }
        Kind() {
            this.color = Color.WHITE;
        }

        public Color getColor() {
            return color;
        }
    }

    public AbsScanner(CharSequence cs) {
        this.iterator = new CharacterIterator(cs);
    }

    public AbsScanner(Reader r) {
        this.iterator = new CharacterIterator(r);
    }

    protected CharacterIterator iterator;
    protected boolean inMultiLineComment;

    public abstract Kind scan();
    /**
     * Returns the string for the last token returned by {@link #scan() }
     *
     * @return the string for the last token
     */
    public String getString() {
        return iterator.getString();
    }

}
