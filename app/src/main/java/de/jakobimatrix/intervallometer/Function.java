package de.jakobimatrix.intervallometer;

import java.util.ArrayList;

public class Function {

    /*!
     * \brief Constructor
     * \param poly The polynom representing the function like:
     * f(x) = poly[0] + poly[1]*x + poly[2]*x^^2 + ...
     */
    public Function(ArrayList<Double> poly){
        this.polynomial = poly;
    }

    public Function(Function f){
        this.polynomial.addAll(f.polynomial);
    }

    public double f(double x){
        double fx = 0;
        for(int i = 0; i < polynomial.size(); i++){
            fx += polynomial.get(i) * Math.pow(x,i);
        }
        return fx;
    }

    public ArrayList<Double> df(){
        ArrayList<Double> df = new ArrayList<Double>(polynomial.size()-1);
        for(int i = 1; i < polynomial.size(); i++){
            df.add(polynomial.get(i)*i);
        }
        return df;
    }

    void setCoef(int i, double coef){
        int dif = polynomial.size()-i;
        if(dif > 0){
            polynomial.set(i, coef);
        }else{
            for(int j = 0; j < dif; j++){
                polynomial.add(0.0);
            }
            polynomial.add(coef);
        }
    }

    public double getMin(double left, double right){
        // I am lazy here since I know that I only need up to second order
        switch (polynomial.size()){
            case 1: // constant min = max
                return polynomial.get(0);
            case 2: // left or right value
                if(polynomial.get(1)>0){
                    return f(left);
                }else{
                    return f(right);
                }
            case 3: // left right or extrema
                double l = f(left);
                double r = f(right);
                if(polynomial.get(2)>0){
                    ArrayList<Double> df = df();
                    // 0 = ax + b
                    double x = -df.get(0)/df.get(1);
                    l = Math.min(l,x);
                }
                return Math.min(l,r);
            default:
                throw new IllegalArgumentException( "No polynomial of order 3 or greater supported");
        }
    }

    public double getMax(double left, double right){
        Function f = Function.scale(this, -1.0);
        return -f.getMin(left, right);
    }

    public void scale (double m){
        for(int i = 0; i < polynomial.size(); i++){
            polynomial.set(i, polynomial.get(i)*m);
        }
    }

    public final static Function scale(Function f, double m){
        Function f2 = new Function(new ArrayList<Double>(f.polynomial));
        f2.scale(m);
        return f2;
    }

    public String toString(){
        String s = "";
        for(int i = 0; i < polynomial.size(); i++){
            s += polynomial.get(i) + "*x^" + i + " + ";
        }
        return s;
    }

    public ArrayList<Double> polynomial = new ArrayList<>();
}
