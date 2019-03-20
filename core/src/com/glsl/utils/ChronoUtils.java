package com.glsl.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Pool;

public class ChronoUtils implements Pool.Poolable {

    public static final String TAG = ChronoUtils.class.getSimpleName();

    public enum TimeUnit{
        NANO(1), MICROSECONDE(1000), MILLISECONDE(1000000), SECOND(1000000000), MINUTE(1000000000); // to long (like my c***)

        public int value;
        TimeUnit(int value) {
            this.value = value;
        }
    }

    private long createdMillis;

    public ChronoUtils(){};

    public ChronoUtils startTimer(){
        createdMillis = System.nanoTime();
        return this;
    }

    public long getTimeEllapsed(TimeUnit timeUnit){
        long nowMillis = System.nanoTime();
        if(timeUnit == TimeUnit.MINUTE) timeUnit.value *= 60;
        if(timeUnit.value == 0) return 0;
        return ((nowMillis - createdMillis) / timeUnit.value);
    }

    public long stopTimer(){return stopTimer(null, false);}
    public long stopTimer(TimeUnit timeUnit){return stopTimer(timeUnit, false);}
    public long stopTimer(TimeUnit timeUnit, boolean log){
        if(timeUnit == null) timeUnit = TimeUnit.MINUTE;
        long t = getTimeEllapsed(timeUnit);
        if(log){
            Gdx.app.log(TAG,"==>  stopTimer : "+ t + " "+timeUnit.name().toLowerCase());
        }
        createdMillis = 0;
        return t;
    }

    @Override
    public void reset() {
        createdMillis = 0;
    }
}