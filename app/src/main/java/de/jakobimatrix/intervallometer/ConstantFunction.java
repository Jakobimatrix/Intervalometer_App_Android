package de.jakobimatrix.intervallometer;

import java.util.ArrayList;

public class ConstantFunction extends Function {

    public ConstantFunction(double c) {
        super(new ArrayList<Double>(1));
        polynomial.add(c);
    }

    public ConstantFunction(Pos3d p) {
        super(new ArrayList<Double>(1));
        polynomial.add(p.y);
    }

    public ConstantFunction(ArrayList<Double> poly) {
        super(poly);
    }

    public ConstantFunction(Function f) {
        super(f);
    }

    @Override
    public byte[] toByteStream(int min, int max) {
        //[ FUNC_SYMBOL | NUM_PICS | C ]
        // f(n) = f(n-1); f(0) = C;
        byte[] num_pics = super.toByteStream(min, max);
        byte[] buffer_c = new byte[4];
        Utility.int2Bytes((int) Math.round(f(min)), buffer_c);

        return new byte[]{Globals.NUM_VALUES_F_CONST, num_pics[0], num_pics[1], num_pics[2], num_pics[3], buffer_c[0], buffer_c[1], buffer_c[2], buffer_c[3]};
    }
}
