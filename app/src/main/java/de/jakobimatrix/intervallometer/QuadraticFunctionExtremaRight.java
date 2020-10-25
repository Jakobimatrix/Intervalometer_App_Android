package de.jakobimatrix.intervallometer;

import java.util.ArrayList;

public class QuadraticFunctionExtremaRight extends QuadraticFunction {

    public QuadraticFunctionExtremaRight(QuadraticFunctionExtremaRight f) {
        super(f);
    }

    public QuadraticFunctionExtremaRight() {

    }

    public QuadraticFunctionExtremaRight(Pos3d left, Pos3d right) {
        setFunctionGivenExtrema(left, right);
    }

    @Override
    public void setFunctionGivenExtrema(Pos3d p, Pos3d extrema){
        super.setFunctionGivenExtrema(extrema,p);
    }

    @Override
    public void setFunctionGivenPoints(ArrayList<Pos3d> p){
        if(p.size() != 2){
            throw new IllegalArgumentException("QuadraticFunctionExtremaRight::setFunctionGivenPoints: length of argument must be 2");
        }
        setFunctionGivenExtrema(p.get(0), p.get(1));
    }
}
