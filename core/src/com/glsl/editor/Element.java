package com.glsl.editor;

import com.glsl.editor.scanner.AbsScanner;
import com.wulfazar.theredcollapse.engine.core.texteditor.scanner.AbsScanner;

public class Element {
  public AbsScanner.Kind kind;
  public String text;
  
  public Element(AbsScanner.Kind k, String t) {
    text = t;
    kind = k;
  }

  public int countSpaces() {
    int sum = 0;
    
    for (int i = 0; i < text.length(); i++) {
        char c = text.charAt(i);
        if (c == ' ') {
            sum += 1;
        }else {
        break;
      }
    }
    
    return sum;
  }
}
