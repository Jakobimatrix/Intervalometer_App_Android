package de.jakobimatrix.intervallometer;

import java.util.Vector;

public class Utility {
    private Utility(){}

    // Part of isWithinTriangle()
    // Looks like a cross product, but I don't care as long as it works.
    private final static double sign (Pos3d p1, Pos3d p2, Pos3d p3)
    {
        return (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y);
    }

    /*!
     * \brief isWithinTriangle Check if a given pose is inside a given triangle.
     * https://stackoverflow.com/questions/2049582/how-to-determine-if-a-point-is-in-a-2d-triangle
     * \param t1, t2, t3 the edge coordinates of the triangle.
     * \param p The pose to be checked.
     * \return True if the pose is withing the triangle, false otherwise.
     */
    public static final boolean isWithinTriangle(Pos3d t1, Pos3d t2, Pos3d t3, Pos3d p){
        double d1, d2, d3;
        boolean has_neg, has_pos;

        d1 = sign(p, t1, t2);
        d2 = sign(p, t2, t3);
        d3 = sign(p, t3, t1);

        has_neg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        has_pos = (d1 > 0) || (d2 > 0) || (d3 > 0);

        return !(has_neg && has_pos);
    }

    /*!
     * \brief screen2OpenGL Using homography to transform from screen coordinates to OpenGL coordinates
     * THERE IS NO CHECK IF GIVEN ARRAy HAS ENOUGH ELEMENTS!
     * \param homography A 4*4 homography matrix in row mayor.
     * \param pos_screen the to be transformed pose in screen coordinates.
     * \return The position in openGL coordinates.
     */
    public static  final Pos3d screen2OpenGL(double[] homography, Pos3d pos_screen){
        double [] pos_screen_vector = {pos_screen.x, pos_screen.y, pos_screen.z, 1};
        double [] pos_gl_vector = {0,0,0,0};
        for(int y = 0; y < 4; y++){
            for(int x = 0; x < 4; x++){
                pos_gl_vector[y] += homography[y*4 + x]*pos_screen_vector[x];
            }
        }
        return new Pos3d(pos_gl_vector[0], pos_gl_vector[1], pos_gl_vector[2]);
    }

    /*!
     * \brief Vector2ArrayShort transforms a given Vector<Short> into an array
     * \param v The vector
     * \return an array with the elements of v
     */
    final static short[] Vector2ArrayShort(final Vector<Short> v){
        short a[] = new short[v.size()];
        for (int i = 0; i < v.size(); i++) {
            a[i] = v.get(i);
        }
        return a;
    }

    /*!
     * \brief Vector2ArrayFloat transforms a given Vector<Float> into an array
     * \param v The vector
     * \return an array with the elements of v
     */
    final static float[] Vector2ArrayFloat(final Vector<Float> v){
        float a[] = new float[v.size()];
        for (int i = 0; i < v.size(); i++) {
            a[i] = v.get(i);
        }
        return a;
    }
}
