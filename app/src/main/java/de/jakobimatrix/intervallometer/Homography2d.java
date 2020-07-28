package de.jakobimatrix.intervallometer;

public class Homography2d {

    public void setIdentity(){
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                h[i*3 + j] = (i==j)?1:0;
                h_inv[i*3 + j] = (i==j)?1:0;
            }
        }
    }

    public double [] h = new double[9];
    public double [] h_inv = new double[9];

    public Pos3d transform(final Pos3d p){
        Pos3d p_ = new Pos3d(p);
        p_.x = h[0]*p.x + h[1] * p.y + h[2];
        p_.y = h[3]*p.x + h[4] * p.y + h[5];
        return p_;
    }

    public Pos3d invTransform(final Pos3d p){
        Pos3d p_ = new Pos3d(p);
        p_.x = h_inv[0]*p.x + h_inv[1] * p.y + h_inv[2];
        p_.y = h_inv[3]*p.x + h_inv[4] * p.y + h_inv[5];
        return p_;
    }

    public void calculateHomography2DNoRotation(final Pos3d p1, final Pos3d p1_, final Pos3d p2, final Pos3d p2_){
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

        h_inv[0] = (p1.x - p2.x) / (p1_.x - p2_.x);
        h_inv[4] = (p1.y - p2.y) / (p1_.y - p2_.y);
        h_inv[2] = p1.x - h_inv[0]*p1_.x;
        h_inv[5] = p1.y - h_inv[4]*p1_.y;
    }

    public Homography2d invert(){
        Homography2d inv = new Homography2d();
        for(int i = 0; i < 9; i++){
            inv.h[i] = h_inv[i];
            inv.h_inv[i] = h[i];
        }
        return inv;
    }

    public String toString(){
        return new String(h.toString());
    }
}
