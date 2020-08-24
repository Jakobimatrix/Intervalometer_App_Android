package de.jakobimatrix.intervallometer;

import android.app.Activity;
import android.content.res.Resources;

import java.util.ArrayList;

enum SUPPORTED_FUNCTION{
    LINEAR,
    QUADRATIC_EXTREMA_LEFT,
    QUADRATIC_EXTREMA_RIGHT,
    SIGMOID,
    UNKNOWN
};

public class Function {

    public Function(){ }

    /*!
     * \brief Constructor
     * \param poly The polynom representing the function like:
     * f(x) = poly[0] + poly[1]*x + poly[2]*x^2 + ...
     */
    public Function(ArrayList<Double> poly){
        if(poly.size() > 3){
            throw new IllegalArgumentException( "No polynomial of order 3 or greater supported");
        }
        this.polynomial = poly;
    }

    public Function(Function f){
        this.polynomial.clear();
        // copy
        this.polynomial.addAll(f.polynomial);
    }

    /*!
     * \brief f calculate f(x)
     * \param x
     * \return f(x)
     */
    public double f(double x){
        double fx = 0;
        for(int i = 0; i < polynomial.size(); i++){
            fx += polynomial.get(i) * Math.pow(x,i);
        }
        return fx;
    }

    /*!
     * \brief df Return the coefficients of the derivation of f
     */
    public double df(double x){
        if(df == null){
            ArrayList<Double> df_poly = new ArrayList<Double>();
            for(int i = 1; i < polynomial.size(); i++){
                df_poly.add(polynomial.get(i)*i);
            }
            df = new Function(df_poly);
        }
        return df.f(x);
    }

    /*!
     * \brief getMin calculates Min(f(x)) x[left, right]
     * \param left The left border
     * \param right the right border
     */
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
                    if(df == null){
                        df(0); // will save df
                    }
                    // 0 = ax + b
                    double x = -df.polynomial.get(0)/df.polynomial.get(1);
                    l = Math.min(l,f(x));
                }
                return Math.min(l,r);
            default:
                throw new IllegalArgumentException( "No polynomial of order 3 or greater supported");
        }
    }

    /*!
     * \brief getMax calculates Max(f(x)) x[left, right]
     * \param left The left border
     * \param right the right border
     */
    public double getMax(double left, double right){
        scale(-1.0);
        double max = -getMin(left, right);
        scale(-1.0);
        return max;
    }

    /*!
     * \brief scale scale the function m*f(x)
     * \param m the scale factor
     */
    public void scale (double m){
        for(int i = 0; i < polynomial.size(); i++){
            polynomial.set(i, polynomial.get(i)*m);
        }
        if(df != null){
            df.scale(m);
        }
    }

    /*!
     * \brief toString
     * \return "f(x) = poly[0] + poly[1]*x + poly[2]*x^2 + ..."
     */
    public String toString(){
        String s = "f(x) = ";
        for(int i = 0; i < polynomial.size(); i++){
            s += polynomial.get(i) + "*x^" + i + " + ";
        }
        return s;
    }

    /*!
     * \brief getOrder
     * \return the number of coefficients.
     */
    public int getOrder(){
        return polynomial.size();
    }

    /*!
     * \brief setFunctionGivenPoints calculate the function given n points
     * supports 1, 2 or 3 points.
     * \param p the points
     */
    public void setFunctionGivenPoints(ArrayList<Pos3d> p){
        polynomial.clear();
        df = null;
        switch (p.size()){
            case 1: // constant y = c
                polynomial.add(p.get(0).y);
                break;
            case 2:  // linear y = bx + c
                // y1 = b*x1 + c
                // y2 = b*x2 + c
                // y1 - y2 = b*(x1 - x2)
                // b = (y1 - y2)/(x1 - x2)
                // c = y1 - b*x1
            {
                double x1 = p.get(0).x;
                double y1 = p.get(0).y;
                double x2 = p.get(1).x;
                double y2 = p.get(1).y;
                double b = (y1 - y2) / (x1 - x2);
                double c = y1 - b * x1;
                polynomial.add(c);
                polynomial.add(b);
            }
                break;
            case 3: //quadratic y = ax^2 + bx + c
                // https://stackoverflow.com/questions/717762/how-to-calculate-the-vertex-of-a-parabola-given-three-points
                // Todo maybe use lin algebra? http://ejml.org/wiki/index.php?title=Main_Page
                // or https://commons.apache.org/proper/commons-math/
            {
                double x1 = p.get(0).x;
                double y1 = p.get(0).y;
                double x2 = p.get(1).x;
                double y2 = p.get(1).y;
                double x3 = p.get(2).x;
                double y3 = p.get(2).y;
                double denom = (x1 - x2) * (x1 - x3) * (x2 - x3);
                double a = (x3 * (y2 - y1) + x2 * (y1 - y3) + x1 * (y3 - y2)) / denom;
                double b = (x3 * x3 * (y1 - y2) + x2 * x2 * (y3 - y1) + x1 * x1 * (y2 - y3)) / denom;
                double c = (x2 * x3 * (x2 - x3) * y1 + x3 * x1 * (x3 - x1) * y2 + x1 * x2 * (x1 - x2) * y3) / denom;
                polynomial.add(c);
                polynomial.add(b);
                polynomial.add(a);
            }
                break;
            default:
                throw new IllegalArgumentException( "Function::setFunctionGivenPoints: I only support 1,2 or 3 Points.");
        }
    }

    /*!
     * \brief moveOffsetY move the function along y axis about dy
     * \param dy shift
     */
    public void moveOffsetY(double dy){
        polynomial.set(0, polynomial.get(0)+dy);
    }

    public void moveOffsetX(double dx) {
        switch (getOrder()){
            case 1: // y = c do nothing
                return;
            case 2: // y = bx + c
                //dy = b*dx
                polynomial.set(0, polynomial.get(0) - polynomial.get(1)*dx);
                break;
            case 3: // quadratic
                // just make 3 points, shift them and calculate new quadratic
                Pos3d p1 = new Pos3d(-100 + dx, f(-100),0);
                Pos3d p2 = new Pos3d(0 + dx, f(0),0);
                Pos3d p3 = new Pos3d(100 + dx, f(100),0);
                ArrayList<Pos3d> points = new ArrayList<>(3);
                points.add(p1);
                points.add(p2);
                points.add(p3);
                setFunctionGivenPoints(points);
                break;
            default:
                throw new IllegalArgumentException( "Function::moveOffsetX: I only support 1,2 or 3 Points.");
        }

        if(df != null){
            df.moveOffsetX(dx);
        }
    }


    protected byte[] toByteStream(int min, int max){
        throw new IllegalArgumentException( "Function::toByteStream: this method is 'abstract', don't use it." );
    }

    protected byte[] getNumPictures(int min, int max){
        byte[] num_pics = new byte[4];
        Utility.int2Bytes(max-min, num_pics);
        return num_pics;
    }


    public static SUPPORTED_FUNCTION FunctionString2Enum(Activity activity, String function_class){
        if(activity.getString(R.string.linear_function).equals(function_class)){
            return SUPPORTED_FUNCTION.LINEAR;
        }
        if(activity.getString(R.string.quadratic_function_left).equals(function_class)){
            return SUPPORTED_FUNCTION.QUADRATIC_EXTREMA_LEFT;
        }
        if(activity.getString(R.string.quadratic_function_right).equals(function_class)){
            return SUPPORTED_FUNCTION.QUADRATIC_EXTREMA_RIGHT;
        }
        if(activity.getString(R.string.sigmoid_function).equals(function_class)){
            return SUPPORTED_FUNCTION.SIGMOID;
        }
        return SUPPORTED_FUNCTION.UNKNOWN;
    }

    public static Function create(Pos3d left, Pos3d right, SUPPORTED_FUNCTION sf){
        switch (sf){
            case LINEAR:
                return new LinearFunction(left,right);
            case QUADRATIC_EXTREMA_LEFT:
                return new QuadraticFunctionExtremaLeft(left,right);
            case QUADRATIC_EXTREMA_RIGHT:
                return new QuadraticFunctionExtremaRight(left,right);
            case SIGMOID:
                return new SigmoidFunction(left, right);
            case UNKNOWN:
                return null;
            default:
                // Why Java not able to see, that ENUM has no default?!
                throw new IllegalStateException("Unexpected value: " + sf);
        }
    }

    public static String FunctionEnum2String(Activity activity, SUPPORTED_FUNCTION function_class){
        switch (function_class){
            case LINEAR:
                return activity.getString(R.string.linear_function);
            case QUADRATIC_EXTREMA_LEFT:
                return activity.getString(R.string.quadratic_function_left);
            case QUADRATIC_EXTREMA_RIGHT:
                return activity.getString(R.string.quadratic_function_right);
            case SIGMOID:
                return activity.getString(R.string.sigmoid_function);
            case UNKNOWN:
                return activity.getString(R.string.unknown_function);
        }
        return "";// this never happens
    }

    public static SUPPORTED_FUNCTION FunctionClass2Enum(Object unknown_function_class){
        // sigmoid must be checked for LinearFunction since all sigmoids are linear functions (see inheritance)
        if (unknown_function_class instanceof de.jakobimatrix.intervallometer.SigmoidFunction) {
            return SUPPORTED_FUNCTION.SIGMOID;
        }
        if (unknown_function_class instanceof de.jakobimatrix.intervallometer.LinearFunction ||
                unknown_function_class instanceof de.jakobimatrix.intervallometer.ConstantFunction) {
            return SUPPORTED_FUNCTION.LINEAR;
        }
        if (unknown_function_class instanceof de.jakobimatrix.intervallometer.QuadraticFunctionExtremaRight) {
            return SUPPORTED_FUNCTION.QUADRATIC_EXTREMA_RIGHT;
        }
        if (unknown_function_class instanceof de.jakobimatrix.intervallometer.QuadraticFunctionExtremaLeft) {
            return SUPPORTED_FUNCTION.QUADRATIC_EXTREMA_LEFT;
        }
        return SUPPORTED_FUNCTION.UNKNOWN;
    }

    // f(x) = poly[0] + poly[1]*x + poly[2]*x^2 + ...
    protected ArrayList<Double> polynomial = new ArrayList<>();
    protected Function df = null;
}
