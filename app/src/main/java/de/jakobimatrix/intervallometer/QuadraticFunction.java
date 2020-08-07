package de.jakobimatrix.intervallometer;

import java.util.ArrayList;

public class QuadraticFunction extends Function {

    public QuadraticFunction(double c, double b, double a) {
        super(new ArrayList<Double>(3));
        polynomial.add(c);
        polynomial.add(b);
        polynomial.add(a);
    }

    public QuadraticFunction(final Pos3d p1, final Pos3d p2, final Pos3d p3) {
        super(new ArrayList<Double>(2));
        polynomial.add(p1.y);
        polynomial.add(p2.y);
        polynomial.add(p3.y);
        ArrayList<Pos3d> ps = new ArrayList<Pos3d>(){{
            add(p1);
            add(p2);
            add(p3);
        }};
        setFunctionGivenPoints(ps);
    }

    public QuadraticFunction(ArrayList<Double> poly) {
        super(poly);
    }

    public QuadraticFunction(Function f) {
        super(f);
    }

    public QuadraticFunction() {

    }

    public void setFunctionGivenExtrema(Pos3d extrema, Pos3d p){
        /*
        e := extrema
        y_p = ax_p^2 + bx_p + c     (I)
        y_e = ax_e^2 + bx_e + c     (II)
        0 = 2ax_e + b               (III)
        (I - II)
        y_p - y_e = a(x_p^2 - x_e^2) + b(x_p - x_e)             (IV)
        (III ->)
        b = -2ax_e                  (V)
        (V -> IV)
        y_p - y_e = a(x_p^2 - x_e^2) -2ax_e(x_p - x_e)
        y_p - y_e = a[(x_p^2 - x_e^2) -2x_e(x_p - x_e)]         (VI)
        (VI ->)
        a = (y_p - y_e) / [(x_p^2 - x_e^2) -2x_e(x_p - x_e)]    (VII)
        (V)
        b = -2ax_e
        (I)
        c = y_p - ax_p^2 + bx_p
        */

        double y_p = p.y;
        double x_p = p.x;
        double y_e = extrema.y;
        double x_e = extrema.x;

        double a = (y_p - y_e) / ((x_p*x_p - x_e*x_e) -2*x_e*(x_p - x_e));
        double b = -2*a*x_e;
        double c = y_p - a*x_p*x_p + b*x_p;

        polynomial.clear();
        polynomial.add(c);
        polynomial.add(b);
        polynomial.add(a);
    }

    @Override
    public byte[] toByteStream(int min, int max) {
        //[ FUNC_SYMBOL | NUM_PICS | C | B | A ]
        // f(0) = C
        // f(n) = f(n-1) + A(n-1) + B   (I)
        /*
        https://math.stackexchange.com/questions/561795/how-to-write-a-recursive-equation-for-quadratic-sequence
        C = f(0) = f(min)
        B = f(1)-f(0)                   f(n=0) = ...
        A = f(2)-f(1) - B               f(n=1) = ...
         */
        byte[] num_pics = super.toByteStream(min, max);

        byte[] buffer_c = new byte[4];
        byte[] buffer_b = new byte[4];
        byte[] buffer_a = new byte[4];

        int const_c = (int) Math.round(f(min));
        int const_b = (int) Math.round(f(1) - f(0));
        int const_a = (int) Math.round(f(2) - f(1) - const_b);

        Utility.int2Bytes(const_c, buffer_c);
        Utility.int2Bytes(const_b, buffer_b);
        Utility.int2Bytes(const_a, buffer_a);

        return new byte[]{Globals.NUM_VALUES_F_QUAD,
                num_pics[0], num_pics[1], num_pics[2], num_pics[3],
                buffer_c[0], buffer_c[1], buffer_c[2], buffer_c[3],
                buffer_b[0], buffer_b[1], buffer_b[2], buffer_b[3],
                buffer_a[0], buffer_a[1], buffer_a[2], buffer_a[3]};
    }
}
