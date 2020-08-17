package de.jakobimatrix.intervallometer;

import java.util.ArrayList;

public class QuadraticFunctionExtremaLeft extends QuadraticFunction{

    public QuadraticFunctionExtremaLeft(QuadraticFunctionExtremaLeft f) {
        super(f);
    }

    public QuadraticFunctionExtremaLeft() {

    }

    public QuadraticFunctionExtremaLeft(Pos3d left, Pos3d right) {
        setFunctionGivenExtrema(left, right);
    }

    @Override
    public void setFunctionGivenExtrema(Pos3d extrema, Pos3d p){
        super.setFunctionGivenExtrema(extrema,p);
    }

    @Override
    public void setFunctionGivenPoints(ArrayList<Pos3d> p){
        if(p.size() != 2){
            throw new IllegalArgumentException("QuadraticFunctionExtremaLeft::setFunctionGivenPoints: length of argument must be 2");
        }
        setFunctionGivenExtrema(p.get(0), p.get(1));
    }
}
