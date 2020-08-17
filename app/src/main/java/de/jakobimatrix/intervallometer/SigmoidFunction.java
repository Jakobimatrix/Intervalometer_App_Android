package de.jakobimatrix.intervallometer;

import java.util.ArrayList;

// This is actually a face sigmoid composed out of two parabolas.
// Also I am overwriting all methods which is not the fine English way buuuuut eh.
public class SigmoidFunction extends LinearFunction{

    // shall not be used
    public SigmoidFunction(ArrayList<Double> poly) {
        super(poly);
        throw new IllegalArgumentException( "Don't use this constructor please" );
    }

    public SigmoidFunction(SigmoidFunction f) {
        super(f); // copy the polyomnomnom
        left_parabola = new QuadraticFunction(f.left_parabola);
        right_parabola = new QuadraticFunction(f.right_parabola);
        max_x = f.max_x;
        min_x = f.min_x;
    }

    public SigmoidFunction(final Pos3d left, final Pos3d right) {
        super(new ArrayList<Double>(2));
        constructSigmoid(left, right);
    }

    public void constructSigmoid(Pos3d left_, Pos3d right_){
        if(left_.x == right_.x){
            left_.x -= 0.0001;
            right_.x += 0.0001;
        }
        Pos3d left = new Pos3d(left_);
        Pos3d right = new Pos3d(right_);
        if(left.x > right.x){
            Pos3d temp = new Pos3d(left);
            left = new Pos3d(right);
            right = temp;
        }
        polynomial.add(left.y);
        polynomial.add(right.y);
        min_x = left.x;
        max_x = right.x;

        // now construct the two parabolas such that left is in the extrema of the left parabola,
        // right in the right parabola extrema and both parabolas and both meet in the center

        Pos3d center = Pos3d.div(Pos3d.sub(right,left),2);
        center.add(left);
        left_parabola.setFunctionGivenExtrema(left, center);
        right_parabola.setFunctionGivenExtrema(right, center);

        left_parabola.df = null;
        right_parabola.df = null;
    }

    double getMid(){
        return (max_x - min_x)/2. + min_x;
    }

    @Override
    public double f(double x){
        if(x < min_x){
            return left_parabola.f(min_x);
        }else if(x > max_x){
            return right_parabola.f(max_x);
        }
        return x < getMid()? left_parabola.f(x):right_parabola.f(x);
    }

    @Override
    public double df(double x){
        if(x <= min_x || x >= max_x){
            return 0;
        }
        if(x < getMid()){
            return left_parabola.df(x);
        }else{
            return right_parabola.df(x);
        }
    }

    @Override
    public double getMin(double left, double right){
        return Math.min(left_parabola.f(min_x), right_parabola.f(max_x));
    }

    @Override
    public double getMax(double left, double right){
        return Math.max(left_parabola.f(min_x), right_parabola.f(max_x));
    }

    @Override
    public void scale (double m){
        throw new IllegalArgumentException( "Don't use scale on a sigmoid. Don't wanna implement it.");
    }

    @Override
    public String toString(){
        return left_parabola.toString() + " " + right_parabola.toString();
    }

    @Override
    public int getOrder(){
        return 2;
    }

    @Override
    public void setFunctionGivenPoints(ArrayList<Pos3d> p){
        if(p.size() != 2){
            throw new IllegalArgumentException("SigmoidFunction::setFunctionGivenPoints: length of argument must be 2");
        }
        constructSigmoid(p.get(0), p.get(1));
    }

    @Override
    public void moveOffsetY(double dy){
        left_parabola.moveOffsetY(dy);
        right_parabola.moveOffsetY(dy);
        polynomial.set(0, polynomial.get(0) + dy);
        polynomial.set(1, polynomial.get(1) + dy);
    }

    @Override
    public void moveOffsetX(double dx) {
        left_parabola.moveOffsetX(dx);
        right_parabola.moveOffsetX(dx);
        min_x += dx;
        max_x += dx;
    }

    @Override
    public byte[] toByteStream(int min, int max) {
        int mid = (max-min)/2 + min;

        byte[] left = left_parabola.toByteStream(min, mid);
        byte[] right = right_parabola.toByteStream(mid, max);

        byte[] ret = new byte[left.length + right.length];
        System.arraycopy(left, 0, ret, 0, left.length);
        System.arraycopy(right, 0, ret, left.length, right.length);

        return ret;
    }

    double min_x;
    double max_x;
    private QuadraticFunction left_parabola = new QuadraticFunction();
    private QuadraticFunction right_parabola = new QuadraticFunction();
}
