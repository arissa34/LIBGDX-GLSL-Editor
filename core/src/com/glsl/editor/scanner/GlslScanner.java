package com.glsl.editor.scanner;

import com.glsl.editor.CharacterIterator;
import com.glsl.editor.KeywordList;
import com.wulfazar.theredcollapse.engine.core.texteditor.CharacterIterator;
import com.wulfazar.theredcollapse.engine.core.texteditor.KeywordList;

import java.io.Reader;

/**
 * A simple Java source file parser for syntax highlighting.
 *
 * @author Matthias Mann
 */
public class GlslScanner extends AbsScanner {

    private static final KeywordList KEYWORD_LIST = new KeywordList(
            "function", "for", "while", "void", "if", "else", "null",
            "try", "catch", "do", "instanceof", "return", "throw", "typeof",
            "this", "new", "switch", "case", "break", "with", "prototype", "true", "false"
    );
    private static final KeywordList PRIMITIVE = new KeywordList(
            "bool", "int", "double",
            "bvec2", "bvec3", "bvec4",
            "ivec2", "ivec3", "ivec4",
            "float", "vec2", "vec3", "vec4",
            "sampler2D", "samplerCube",
            "mat2", "mat3", "mat4");
    private static final KeywordList GLSL_WORD = new KeywordList(
            "gl_Position", "gl_FragColor", "gl_FragCoord",
            "gl_PointSize", "gl_FrontFacing", "gl_PointCoord", "gl_PointSize",
            "GL_ES", "GL_OES_standard_derivatives"
    );

    private static final KeywordList GLSL_QUALIFIERS = new KeywordList(
            "#define", "precision", "#extension", "#ifdef", "#endif", "#else", "struct",
            "#undef", "#ifndef", "error", "pragma", "#version", "line",
            "uniform","const", "attribute", "varying", "highp", "mediump", "lowp"
    );
    private static final KeywordList GLSL_FUNCTION = new KeywordList(
            "radians", "degrees", "sin", "cos", "tan", "asin", "acos", "atan",
             "pow", "exp", "log", "exp2", "log2", "sqrt", "inversesqrt", "abs", "sign",
            "floor", "ceil", "fract", "mod", "min", "max", "clamp", "mix", "step", "smoothstep",
             "lenght", "distance", "dot", "cross", "normalize", "faceforward", "reflect", "refract",
            "matrixCompMult", "lessThan", "lessThanEqual", "greaterThan", "greaterThanEqual", "equal", "notEqual", "any",
            "all", "not", "texture2DLod", "texture2DProjLod", "texture2DProjLod", "textureCubeLod",
            "texture2D", "texture2DProj", "texture2DProj", "textureCube"
    );

    public GlslScanner(CharSequence cs) {
        super(cs);
    }

    public GlslScanner(Reader r) {
        super(r);
    }

    /**
     * Scans for the next token.
     * Read errors result in EOF.
     * <p>
     * Use {@link #getString()} to retrieve the string for the parsed token.
     *
     * @return the next token.
     */
    public Kind scan() {
        iterator.clear();
        if (inMultiLineComment) {
            return scanMultiLineComment(false);
        }
        int ch = iterator.next();
        switch (ch) {
            case CharacterIterator.EOF:
                return Kind.EOF;
            case '\n':
                return Kind.NEWLINE;
            case '\"':
            case '\'':
                scanString(ch);
                return Kind.STRING;
            case '/':
                switch (iterator.peek()) {
                    case '/':
                        iterator.advanceToEOL();
                        return Kind.COMMENT;
                    case '*':
                        inMultiLineComment = true;
                        iterator.next(); // skip '*'
                        return scanMultiLineComment(true);
                }
                // fall through
            default:
                return scanNormal(ch);
        }
    }

    private void scanString(int endMarker) {
        for (; ; ) {
            int ch = iterator.next();
            if (ch == '\\') {
                iterator.next();
            } else if (ch == endMarker || ch == '\n' || ch == '\r') {
                return;
            }
        }
    }

    private Kind scanMultiLineComment(boolean start) {
        int ch = iterator.next();
        if (!start && ch == '\n') {
            return Kind.NEWLINE;
        }
        if (ch == '@') {
            iterator.advanceIdentifier();
            return Kind.COMMENT_TAG;
        }
        for (; ; ) {
            if (ch < 0 || (ch == '*' && iterator.peek() == '/')) {
                iterator.next();
                inMultiLineComment = false;
                return Kind.COMMENT;
            }
            if (ch == '\n') {
                iterator.pushback();
                return Kind.COMMENT;
            }
            if (ch == '@') {
                iterator.pushback();
                return Kind.COMMENT;
            }
            ch = iterator.next();
        }
    }

    private Kind scanNormal(int ch) {
        for (; ; ) {
            switch (ch) {
                case '\n':
                case '\"':
                case '\'':
                case CharacterIterator.EOF:
                    iterator.pushback();
                    return Kind.NORMAL;
                case '/':
                    if (iterator.check("/*")) {
                        iterator.pushback();
                        return Kind.NORMAL;
                    }
                    break;
                default:
                    if (Character.isJavaIdentifierStart(ch) || ch == '#') {
                        iterator.setMarker(true);
                        iterator.advanceIdentifier();
                        if (iterator.isKeyword(KEYWORD_LIST)) {
                            if (iterator.isMarkerAtStart()) {
                                return Kind.KEYWORD;
                            }
                            iterator.rewindToMarker();
                            return Kind.NORMAL;
                        } else if (iterator.isKeyword(GLSL_WORD)) {
                            if (iterator.isMarkerAtStart()) {
                                return Kind.SPECIAL_WORD_4;
                            }
                            iterator.rewindToMarker();
                            return Kind.NORMAL;
                        } else if (iterator.isKeyword(GLSL_QUALIFIERS)) {
                            if (iterator.isMarkerAtStart()) {
                                return Kind.SPECIAL_WORD_3;
                            }
                            iterator.rewindToMarker();
                            return Kind.NORMAL;
                        } else if (iterator.isKeyword(PRIMITIVE)) {
                            if (iterator.isMarkerAtStart()) {
                                return Kind.SPECIAL_WORD_PRIMITIVE;
                            }
                            iterator.rewindToMarker();
                            return Kind.NORMAL;
                        } else if (iterator.isKeyword(GLSL_FUNCTION)) {
                            if (iterator.isMarkerAtStart()) {
                                return Kind.SPECIAL_WORD_FUNCTION;
                            }
                            iterator.rewindToMarker();
                            return Kind.NORMAL;
                        } else if (Character.isDigit(ch)) {
                            return Kind.NUMBER;
                        }
                    }
                    break;
            }
            ch = iterator.next();
        }
    }

}
