package de.jakobimatrix.intervalometer;

import android.util.Log;

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

    protected void setFunctionGivenExtrema(Pos3d extrema, Pos3d p){
        df = null;
        /*
        e := extrema
        y_p = a*x_p^2 + b*x_p + c     (I)
        y_e = a*x_e^2 + b*x_e + c     (II)
        0 = 2*a*x_e + b               (III)
        (I - II)
        y_p - y_e = a*(x_p^2 - x_e^2) + b*(x_p - x_e)             (IV)
        (III ->)
        b = -2*a*x_e                  (V)
        (V -> IV)
        y_p - y_e = a*(x_p^2 - x_e^2) -2*a*x_e(x_p - x_e)
        y_p - y_e = a*[(x_p^2 - x_e^2) -2*x_e*(x_p - x_e)]         (VI)
        (VI ->)
        a = (y_p - y_e) / [(x_p^2 - x_e^2) -2*x_e*(x_p - x_e)]    (VII)
        (V)
        b = -2*a*x_e
        (I)
        c = y_p - a*x_p^2 - b*x_p
        */

        double y_p = p.y;
        double x_p = p.x;
        double y_e = extrema.y;
        double x_e = extrema.x;

        double a = (y_p - y_e) / ((x_p*x_p - x_e*x_e) - 2.0*x_e*(x_p - x_e));
        double b = -2*a*x_e;
        double c = y_p - a*x_p*x_p - b*x_p;

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
        C = f(0) = f(0)
        B = f(1)-f(0)                   f(n=0) = ...
        A = f(2)-f(1) - B               f(n=1) = ...

        C = f(min) = f(min)
        B = f(min + 1)-f(min)             f(n=0) = ...
        A = f(min + 2)-f(min + 1) - B     f(n=1) = ...
         */
        byte[] num_pics = getNumPictures(min, max);

        byte[] buffer_c = new byte[4];
        byte[] buffer_b = new byte[4];
        byte[] buffer_a = new byte[4];

        double[] const_d = {
                f(min),
                f(min + 1) - f(min),
                f(min + 2) - 2*f(min + 1) + f(min)};

        int const_i[] = new int[3];
        for(int i = 0; i < 3; i++){
            const_i[i] = Globals.float2Int4byte(const_d[i]);
        }

        Utility.int2Bytes(const_i[0], buffer_c);
        Utility.int2Bytes(const_i[1], buffer_b);
        Utility.int2Bytes(const_i[2], buffer_a);

        return new byte[]{Globals.SYMBOL_F_QUAD,
                num_pics[0], num_pics[1], num_pics[2], num_pics[3],
                buffer_c[0], buffer_c[1], buffer_c[2], buffer_c[3],
                buffer_b[0], buffer_b[1], buffer_b[2], buffer_b[3],
                buffer_a[0], buffer_a[1], buffer_a[2], buffer_a[3]};
    }
}
