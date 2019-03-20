/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.glsl.shaders.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.glsl.security.EncryptException;
import com.glsl.security.EncryptUtils;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;

public class ShaderLoader {
    public FileHandle root;

    public ObjectMap<String, ObjectMap<String, String>> snippets = new ObjectMap<String, ObjectMap<String, String>>();
    private Array<String> includes = new Array<String>();

    public ShaderLoader(FileHandle root) {
        this.root = root;
    }

    public ShaderProgram load(String vertex, String fragment) {
        StringBuilder out = new StringBuilder();
        load(out, vertex);
        vertex = out.toString();
        includes.clear();
        out.setLength(0);
        load(out, fragment);
        fragment = out.toString();
        includes.clear();
        return new ShaderProgram(vertex, fragment);
    }

    public String load(final String name) {
        StringBuilder out = new StringBuilder();
        load(out, name);
        includes.clear();
        return out.toString();
    }

    protected void load(final StringBuilder out, final String name) {
        final int idx = name.lastIndexOf(':');
        final String fileName = idx < 0 ? name : name.substring(0, idx);
        final String snipName = idx < 0 || (idx >= name.length() - 1) ? "" : name.substring(idx + 1);
        ObjectMap<String, String> snips = snippets.get(fileName, null);
        if (snips == null) {
            snips = parse(root.child(fileName));
            snippets.put(fileName, snips);
        }
        String result = snips.get(snipName, null);
        if (result == null)
            throw new GdxRuntimeException("No snippet [" + snipName + "] in file " + root.child(fileName).path());
        parse(out, fileName, result);
    }

    protected void parse(final StringBuilder out, final String currentFile, final String code) {
        String[] lines = code.split("\n");
        int idx, jdx;
        for (final String line : lines) {
            if (((idx = line.indexOf("#include")) == 0) && ((idx = line.indexOf("\"", idx)) > 0)
                    && ((jdx = line.indexOf("\"", ++idx)) > idx)) {
                String name = line.substring(idx, jdx);
                if (name.length() > 0) {
                    if (name.charAt(0) == ':') name = currentFile + name;
                    if (!includes.contains(name, false)) {
                        includes.add(name);
                        load(out, name);
                    }
                }
            } else
                out.append(line.trim()).append("\r\n");
        }
    }

    final static StringBuilder stringBuilder = new StringBuilder();

    protected ObjectMap<String, String> parse(final FileHandle file) {
        ObjectMap<String, String> result = new ObjectMap<String, String>();

        BufferedReader reader = file.reader(1024);
        ;
        if (file.path().contains(".sglsl")) {
            try {
                String textDecrypt = EncryptUtils.decrypt(getClass().getPackage().getName() + ".KAKA", file);
                reader = new BufferedReader(new CharArrayReader(textDecrypt.toCharArray()));
                //Gdx.app.log("", "DECRYPT : "+textDecrypt);
            } catch (EncryptException e) {
                e.printStackTrace();
            }
        }

        String line;
        String snipName = "";
        stringBuilder.setLength(0);
        int idx;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.length() > 3 && line.charAt(0) == '[' && (idx = line.indexOf(']')) > 1) {
                    if (snipName.length() > 0 || stringBuilder.length() > 0)
                        result.put(snipName, stringBuilder.toString());
                    stringBuilder.setLength(0);
                    snipName = line.substring(1, idx);
                } else
                    stringBuilder.append(line.trim()).append("\r\n");
            }
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }
        if (snipName.length() > 0 || stringBuilder.length() > 0)
            result.put(snipName, stringBuilder.toString());

        return result;
    }

    @Override
    public String toString() {
        stringBuilder.setLength(0);
        for (final ObjectMap.Entry<String, ObjectMap<String, String>> entry : snippets.entries()) {
            stringBuilder.append(entry.key).append(": {");
            for (final String snipname : entry.value.keys())
                stringBuilder.append(snipname).append(", ");
            stringBuilder.append("}\n");
        }
        return stringBuilder.toString();
    }

    public boolean writeShaderInFile(String vertexText, String fragmentText, String vertexName, String fragmentName) {

        String vertexPath = root.path() + "/" + vertexName.replace(":VS", "");
        String fragmentPath = root.path() + "/" + fragmentName.replace(":FS", "");

        if (vertexPath.equals(fragmentPath)) {
            if (!vertexPath.contains(".sglsl")) {
                FileHandle file = Gdx.files.local(vertexPath);
                file.writeString("[VS]\n" + vertexText + "\n[FS]\n" + fragmentText, false);
            } else {
                try {
                    String text = "[VS]\n" + vertexText + "\n[FS]\n" + fragmentText;
                    EncryptUtils.encrypt(getClass().getPackage().getName() + ".KAKA", text, Gdx.files.local(vertexPath.replace(".glsl", ".sglsl")));
                } catch (EncryptException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        } else {
            // try {
            //     EncryptUtils.encrypt(getClass().getPackage().getName()+".KAKA", Gdx.files.local(fragmentPath), Gdx.files.local(fragmentPath.replace(".glsl", ".sglsl")));
            // } catch (EncryptException e) {
            //     e.printStackTrace();
            //     return false;
            // }
            if (vertexPath.contains(".sglsl")) {
                try {
                    String currentFSText = EncryptUtils.decrypt(getClass().getPackage().getName() + ".KAKA", Gdx.files.local(vertexPath));
                    String fs = currentFSText.substring(currentFSText.indexOf("[FS]"), currentFSText.length());
                    String text = "[VS]\n" + vertexText + "\n" + fs;
                    EncryptUtils.encrypt(getClass().getPackage().getName() + ".KAKA", text, Gdx.files.local(vertexPath));
                } catch (EncryptException e) {
                    e.printStackTrace();
                }
            } else {
                FileHandle fileVS = Gdx.files.local(vertexPath);
                String currentFSText = fileVS.readString();
                String fs = currentFSText.substring(currentFSText.indexOf("[FS]"), currentFSText.length());
                fileVS.writeString("[VS]\n" + vertexText + "\n" + fs, false);
            }

            if (fragmentPath.contains(".sglsl")) {
                try {
                    String currentVSText = EncryptUtils.decrypt(getClass().getPackage().getName() + ".KAKA", Gdx.files.local(fragmentPath));
                    String vs = currentVSText.substring(0, currentVSText.indexOf("[FS]"));
                    String text = vs + "\n" + "[FS]\n" + fragmentText;
                    EncryptUtils.encrypt(getClass().getPackage().getName() + ".KAKA", text, Gdx.files.local(fragmentPath));
                } catch (EncryptException e) {
                    e.printStackTrace();
                }
            } else {
                FileHandle fileFS = Gdx.files.local(fragmentPath);
                String currentVSText = fileFS.readString();
                String vs = currentVSText.substring(0, currentVSText.indexOf("[FS]"));
                fileFS.writeString(vs + "\n" + "[FS]\n" + fragmentText, false);
            }
        }
        return true;
    }

    public boolean encrypt(String vertexText, String fragmentText, String vertexName, String fragmentName){
        String vertexPath = root.path() + "/" + vertexName.replace(":VS", "");
        String fragmentPath = root.path() + "/" + fragmentName.replace(":FS", "");

        if (vertexPath.equals(fragmentPath)) {
            FileHandle file = Gdx.files.local(vertexPath);
            file.writeString("[VS]\n" + vertexText + "\n[FS]\n" + fragmentText, false);
            try {
                String text = "[VS]\n" + vertexText + "\n[FS]\n" + fragmentText;
                EncryptUtils.encrypt(getClass().getPackage().getName() + ".KAKA", text, Gdx.files.local(vertexPath.replace(".glsl", ".sglsl")));

            } catch (EncryptException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }else {
            return false;
        }
    }

    public String deccrypt(String shaderText){
        try {
            return EncryptUtils.decrypt(getClass().getPackage().getName() + ".KAKA", shaderText);
        } catch (EncryptException e) {
            e.printStackTrace();
        }
        return null;
    }
}
