package de.jakobimatrix.intervallometer;

import android.content.Context;

public class Manipulator extends MovableDot implements Comparable<Manipulator> {
    public Manipulator(Context context_, Pos3d position_, float radius) {
        super(context_, position_, radius);
    }

    /*
    @return  a negative integer, zero, or a positive integer as this object
     *          is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Manipulator comp) {
        if(getPosition().x > comp.getPosition().x){
            return 1;
        }

        if(getPosition().x < comp.getPosition().x){
            return -1;
        }
        return 0;
    }
}
