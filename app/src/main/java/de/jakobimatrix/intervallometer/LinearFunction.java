package de.jakobimatrix.intervallometer;

import java.util.ArrayList;

public class LinearFunction extends Function {

    public LinearFunction(double c, double b) {
        super(new ArrayList<Double>(2));
        polynomial.add(c);
        polynomial.add(b);
    }

    public LinearFunction(final Pos3d p1, final Pos3d p2) {
        super(new ArrayList<Double>(2));
        polynomial.add(p1.y);
        polynomial.add(p2.y);
        ArrayList<Pos3d> ps = new ArrayList<Pos3d>(){{
            add(p1);
            add(p2);
        }};
        setFunctionGivenPoints(ps);
    }

    public LinearFunction(ArrayList<Double> poly) {
        super(poly);
    }

    public LinearFunction(Function f) {
        super(f);
    }

    @Override
    public byte[] toByteStream(int min, int max) {
        //[ FUNC_SYMBOL | NUM_PICS | C | B ]
        // f(0) = C
        // f(n) = f(n-1) + B
        byte[] num_pics = getNumPictures(min, max);
        byte[] buffer_b = new byte[4];
        byte[] buffer_c = new byte[4];

        double const_c = f(min);
        double const_b = f(min + 1) - f(min);

        Utility.int2Bytes(Globals.float2Int4byte(const_b), buffer_b);
        Utility.int2Bytes(Globals.float2Int4byte(const_c), buffer_c);

        return new byte[]{Globals.SYMBOL_F_LIN,
                num_pics[0], num_pics[1], num_pics[2], num_pics[3],
                buffer_c[0], buffer_c[1], buffer_c[2], buffer_c[3],
                buffer_b[0], buffer_b[1], buffer_b[2], buffer_b[3]};
    }
}