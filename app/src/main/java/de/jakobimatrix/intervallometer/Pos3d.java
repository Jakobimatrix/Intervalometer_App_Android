package de.jakobimatrix.intervallometer;

public class Pos3d {
    public double x;
    public double y;
    public double z;

    public double get(int i){
        switch (i){
            case 0: return x;
            case 1: return y;
            case 2: return z;
            default:
                throw new IllegalArgumentException("In Pose3d.get(i): i is only allowed to be [0, 1, 2]. Given was " + i);
        }
    }

    public Pos3d(double x_, double y_, double z_){
        x = x_;
        y = y_;
        z = z_;
    }

    public Pos3d(float x_, float y_, float z_){
        x = (double) x_;
        y = (double) y_;
        z = (double) z_;
    }

    public Pos3d(int x_, int y_, int z_){
        x = (double) x_;
        y = (double) y_;
        z = (double) z_;
    }

    public Pos3d(short x_, short y_, short z_){
        x = (double) x_;
        y = (double) y_;
        z = (double) z_;
    }

    public Pos3d(Pos3d p){
        x = p.x;
        y = p.y;
        z = p.z;
    }

    public void add(Pos3d p){
        x += p.x;
        y += p.y;
        z += p.z;
    }

    public void sub(Pos3d p){
        x -= p.x;
        y -= p.y;
        z -= p.z;
    }

    public void mul(double m){
        x *= m;
        y *= m;
        z *= m;
    }

    public void div(double d){
        x /= d;
        y /= d;
        z /= d;
    }

    public static Pos3d add(Pos3d p1, Pos3d p2){
        Pos3d res = new Pos3d(p1);
        res.add(p2);
        return res;
    }

    public static Pos3d sub(Pos3d p1, Pos3d p2){
        Pos3d res = new Pos3d(p1);
        res.sub(p2);
        return res;
    }

    public static Pos3d mul(Pos3d p1, double m){
        Pos3d res = new Pos3d(p1);
        res.mul(m);
        return res;
    }

    public static Pos3d div(Pos3d p1, double d){
        Pos3d res = new Pos3d(p1);
        res.div(d);
        return res;
    }

    public double norm(){
        return Math.sqrt(x*x + y*y + z*z);
    }

    public double distance(Pos3d a){
        return distance(a, this);
    }

    public static double distance(Pos3d a, Pos3d b){
        Pos3d dif = Pos3d.sub(a,b);
        return dif.norm();
    }


}
