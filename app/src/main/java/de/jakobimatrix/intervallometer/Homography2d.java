package de.jakobimatrix.intervallometer;

public class Homography2d {

    public void setIdentity(){
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                h[i*3 + j] = (i==j)?1:0;
            }
        }
    }

    public double [] h = new double[9];

    public Pos3d transform(Pos3d p){
        Pos3d p_ = new Pos3d(p);
        p_.x = h[0]*p.x + h[1] * p.y + h[2];
        p_.y = h[3]*p.x + h[4] * p.y + h[5];
        return p_;
    }

    public void calculateHomography2DNoRotation(Pos3d p1, Pos3d p1_, Pos3d p2, Pos3d p2_){
        /*
         *|x_|   |f1 0 tx|   |x|
         *|y_| = |0 f2 ty| * |y|
         *|1 |   |0 0  1|    |1|
         *
         * // 4 points given
         * x1_ = f1*x1 + tx (I)
         * y1_ = f2*y1 + tx (II)
         * x2_ = f1*x2 + tx (III)
         * y2_ = f2*y2 + tx (IV)
         *
         * (I) - (III)
         * x1_ - x2_ = f1*x1 - f1*x2
         * x1_ - x2_ = f1*(x1 - x2)
         * -> f1 = (x1_ - x2_) / (x1 - x2) (V)
         * (II) - (IV)
         * -> f2 = (y1_ - y2_) / (y1 - y2) (VI)
         *
         * (V)->(I)
         * -> tx = x1_ - f1*x1
         * ->
         * f2 = y1_/y1 - ty = y2_/y2 - ty
         *
         */
        setIdentity();
        h[0] = (p1_.x - p2_.x) / (p1.x - p2.x);
        h[4] = (p1_.y - p2_.y) / (p1.y - p2.y);
        h[2] = p1_.x - h[0]*p1.x;
        h[5] = p1_.y - h[4]*p1.y;
    }

    public Homography2d invert(){
        /*
         * |a b c|1 0 0|
         * |d e f|0 1 0|
         * |0 0 1|0 0 1|
         * -->
         * |a b    c   |1 0   0|
         * |a ae/d af/d|0 a/d 0|
         * |0 0    1   |0 0   1|
         * -->
         * |a b      c     |1  0   0|
         * |0 ae/d-b af/d-c|-1 a/d 0|
         * |0 0      1     |0  0   1|
         * -->
         * |a/b 1      c/b   |1/b  0   0|
         * |0   ae/d-b af/d-c|-1   a/d 0|
         * |0   0      1     |0    0   1|
         * X:=ae/d-b
         * |Xa/b X Xc/b  |X/b  0   0|
         * |0    X af/d-c|-1   a/d 0|
         * |0    0 1     |0    0   1|
         * Y:=af/d-c
         * |Xa/b 0 Xc/b-Y|X/b-1  -a/d 0|
         * |0    X Y     |-1      a/d 0|
         * |0    0 1     |0       0   1|
         * -->
         * |Xa/b 0 Xc/b-Y|X/b-1  -a/d    0|
         * |0    X/Y 1   |-1/Y    a/(dY) 0|
         * |0    0   1   |0       0      1|
         * -->
         * |Xa/b 0 Xc/b-Y|X/b-1  -a/d    0 |
         * |0    X/Y 0   |-1/Y    a/(dY) -1|
         * |0    0   1   |0       0      1 |
         * Z:=Xc/b-Y
         * |Xa/(bZ) 0   1|X/(bZ)-1/Z  -a/(dZ) 0 |
         * |0       X/Y 0|-1/Y         a/(dY) -1|
         * |0       0   1|0            0      1 |
         * -->
         * |Xa/(bZ) 0   0|X/(bZ)-1/Z  -a/(dZ) -1|
         * |0       X/Y 0|-1/Y         a/(dY) -1|
         * |0       0   1|0            0      1 |
         * -->
         * |1 0 0|1/(a)-b/(Xa)  -b/(Xd) -bZ/(aX)|
         * |0 1 0|-X             a/(dX) -1/X    |
         * |0 0 1|0              0      1       |
         */

        final double a = h[0];
        final double b = h[1];
        final double c = h[2];
        final double d = h[3];
        final double e = h[4];
        final double f = h[5];
        final double X = a*e/d-b;
        final double Y = a*f/d-c;
        final double Z = X*c/b-Y;
        Homography2d inv = new Homography2d();
        inv.setIdentity();
        inv.h[0] = 1.0/(a)-b/(X*a);
        inv.h[1] = -b/(X*d);
        inv.h[2] = -b*Z/(X*a);
        inv.h[3] = -X;
        inv.h[4] = a/(d*X);
        inv.h[5] = -1.0/X;
        return inv;
    }
}
