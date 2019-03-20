package com.glsl.editor;

import com.badlogic.gdx.utils.Array;

public class ActionBuffer {

    public enum ActionEnum{
        TYPE, REMOVE;
    }

    public class Action{
        public ActionEnum type;
        public int row;
        public int col;
        public String text;

        public Action(String text, ActionEnum type, int row, int col) {
            this.type = type;
            this.row = row;
            this.col = col;
            this.text = text;
        }
    }

    private Array<Action> listAction;
    private int bufferIndex = 0;
    private int bufferSizeMax;

    public ActionBuffer(){
        this(20);
    }

    public ActionBuffer(int bufferSizeMax){
        this.bufferSizeMax = bufferSizeMax;
        listAction = new Array<Action>(bufferSizeMax);
    }

    private void addAction(Action action) {
        if(isBufferFull()){
            listAction.removeIndex(0);
            bufferIndex--;
        }
        if(listAction.size > bufferIndex){
            for(int i = listAction.size-1 ; i >= bufferIndex && i>=0; i--){
                //Gdx.app.log("", "bufer remove : "+i);
                listAction.removeIndex(i);
            }
        }
        listAction.add(action);
        bufferIndex++;
        //Gdx.app.log("", "==> addAction bufer real size : "+listAction.size+ " bufferIndex : "+bufferIndex);
    }

    public void addTypeAction(String ins, int row, int col){
        Action action = new Action(ins, ActionEnum.TYPE, row, col);
        addAction(action);
        //Gdx.app.log("", "ADD STRING");
    }

    public void addDeleteAction(String ins, int row, int col){
       Action action = new Action(ins, ActionEnum.REMOVE, row, col);
        addAction(action);
        //Gdx.app.log("", "ADD DELETE");
    }

    public boolean bufferNotEmpty(){
        return bufferIndex > 0;
    }

    public boolean hasNextAction(){
        //Gdx.app.log("", "==> hasNextAction bufer real size : "+listAction.size+ " bufferIndex : "+bufferIndex);
        return bufferIndex < listAction.size;
    }

    private boolean isBufferFull(){
        return listAction.size == bufferSizeMax;
    }

    public Action getLastAction(){
        if(!bufferNotEmpty()) return null;
        bufferIndex--;
        Action action = listAction.get(bufferIndex);
        //Gdx.app.log("", "==> getLastAction bufer real size : "+listAction.size+ " bufferIndex : "+bufferIndex);
        return action;
    }

    public Action getNextAction(){
        if(!hasNextAction()) return null;
        Action action = listAction.get(bufferIndex);
        bufferIndex++;
        //Gdx.app.log("", "==> getNextAction bufer real size : "+listAction.size+ " bufferIndex : "+bufferIndex);
        return action;
    }
}
