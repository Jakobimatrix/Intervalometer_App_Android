package de.jakobimatrix.intervallometer;

// A class representing a 3d point. Like there a probably 10^10 Implementations of this.
public class Pos3d {
    public double x;
    public double y;
    public double z;

    /*!
     * \brief get Returns one dimension depending on given i:
     * 0 -> x
     * 1 -> y
     * 2 -> z
     * other -> exception in your face
     * \param i The given i.
     * \return one dimension.
     */
    public double get(int i){
        switch (i){
            case 0: return x;
            case 1: return y;
            case 2: return z;
            default:
                throw new IllegalArgumentException("Pose3d::get(i): i is only allowed to be [0, 1, 2]. Given was " + i);
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

    public Pos3d(final Pos3d p){
        x = p.x;
        y = p.y;
        z = p.z;
    }

    /*!
     * \brief add Add a pose to this->Pos3d
     * \param p The pose to be added.
     */
    public void add(final Pos3d p){
        x += p.x;
        y += p.y;
        z += p.z;
    }

    /*!
     * \brief sub Subtract a pose from this->Pos3d
     * \param p The pose to be subtracted.
     */
    public void sub(final Pos3d p){
        x -= p.x;
        y -= p.y;
        z -= p.z;
    }

    /*!
     * \brief mul Multiply this->Pos3d with a constant (scaling)
     * \param m The scale for multiplication
     */
    public void mul(double m){
        x *= m;
        y *= m;
        z *= m;
    }

    /*!
     * \brief div Divide this->Pos3d with a constant (1/scaling)
     * \param d The inverse scale for multiplication
     */
    public void div(double d){
        mul(1/d);
    }

    /*!
     * \brief add Adds two poses together not changing the input.
     * \param p1 The first pose.
     * \param p2 The second pose.
     * \return p1 + p2;
     */
    public static Pos3d add(final Pos3d p1, final Pos3d p2){
        Pos3d res = new Pos3d(p1);
        res.add(p2);
        return res;
    }

    /*!
     * \brief sub Subtracts two poses from another not changing the input.
     * \param p1 The first pose.
     * \param p2 The second pose.
     * \return p1 - p2;
     */
    public static Pos3d sub(final Pos3d p1, final Pos3d p2){
        Pos3d res = new Pos3d(p1);
        res.sub(p2);
        return res;
    }

    /*!
     * \brief mul Multiply a pose by a factor aka scaling.
     * \param p1 The pose.
     * \param m The scale factor.
     * \return p1*m;
     */
    public static Pos3d mul(final Pos3d p1, double m){
        Pos3d res = new Pos3d(p1);
        res.mul(m);
        return res;
    }

    /*!
     * \brief div Divide a pose by a factor aka inverse scaling.
     * \param p1 The pose.
     * \param d The scale factor.
     * \return p1/d;
     */
    public static Pos3d div(final Pos3d p1, double d){
        Pos3d res = new Pos3d(p1);
        res.div(d);
        return res;
    }

    /*!
     * \brief norm Calculates the norm as if this->Pos3d was a Vector
     * \return norm >= 0;
     */
    public double norm(){
        return Math.sqrt(x*x + y*y + z*z);
    }

    /*!
     * \brief distance calculates the Distance between This->Pos3d and a second point.
     * \param a The second point.
     * \return (this-a).norm();
     */
    public double distance(Pos3d a){
        return distance(a, this);
    }

    /*!
     * \brief distance calculates the Distance between two points
     * \param a The first point.
     * \param b The second point.
     * \return (a-b).norm();
     */
    public static double distance(Pos3d a, Pos3d b){
        Pos3d dif = Pos3d.sub(a,b);
        return dif.norm();
    }

    /*!
     * \brief rotateZ Rotates the point around the z axis around the origin.
     * \param rad the rotation in rad
     */
    public void rotateZ(double rad){
        final double x_ = x;
        final double y_ = y;
        x = Math.cos(rad)*x_ - Math.sin(rad)*y_;
        y = Math.sin(rad)*x_ + Math.cos(rad)*y_;
    }

    static public Pos3d Zero(){
        return new Pos3d(0,0,0);
    }

    public String toString(){
        return "x:" + x + " y:" + y + " z:" + z;
    }
}
