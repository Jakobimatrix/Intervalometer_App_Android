package de.jakobimatrix.intervallometer;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
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
     * \brief Vector2ArrayShort transforms a given Vector<Short> into an array
     * \param v The vector
     * \return an array with the elements of v
     */
    final static public short[] Vector2ArrayShort(final Vector<Short> v){
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
    final static public float[] Vector2ArrayFloat(final Vector<Float> v){
        float a[] = new float[v.size()];
        for (int i = 0; i < v.size(); i++) {
            a[i] = v.get(i);
        }
        return a;
    }

    final static public short[] ArrayList2ArrayShort(final ArrayList<Short> v){
        short a[] = new short[v.size()];
        for (int i = 0; i < v.size(); i++) {
            a[i] = v.get(i);
        }
        return a;
    }

    final static public float[] ArrayList2ArrayFloat(final ArrayList<Float> v){
        float a[] = new float[v.size()];
        for (int i = 0; i < v.size(); i++) {
            a[i] = v.get(i);
        }
        return a;
    }

    final static public float GOLDEN_RATIO = 1.6180339887f;

    /*!
     * \brief roundAT Rounds a number mathematically incorrect at rounding_point instead of 0.5
     * \param number The number to be rounded.
     * \param rounding_point The point at which to round up (should be in the range (0, 1)
     * \return The rounded number.
     */
    public final static double roundAT(double number, double rounding_point){
        double number_int = Math.floor(number);
        double number_decimals = number - number_int;
        return (number_decimals < rounding_point)?number_int:number_int + 1.0;
    }

    /*!
     * \brief roundAtDecimal Rounds a number at a given power.
     * E.g.:
     * roundAtDecimal(123.456, 100) -> 100
     * roundAtDecimal(123.456, 10) -> 120
     * roundAtDecimal(123.456, 0.1) -> 123.46
     * \param number The number to be rounded.
     * \param power The power at which to round
     * \return The rounded number.
     */
    public final static double roundAtDecimal(double number, double power){
        return (double) Math.round(number / power) * power;
    }


    /*!
     * \brief cutAtDecimal Cuts a number at a given power.
     * E.g.:
     * cutAtDecimal(123.456, 100) -> 100
     * cutAtDecimal(123.456, 10) -> 120
     * cutAtDecimal(123.456, 0.1) -> 123.4
     * cutAtDecimal(123.456, 0.01) -> 123.45
     * \param number The number to be cut.
     * \param power The power at which to round
     * \return The rounded number.
     */
    public final static double cutAtDecimal(double number, double power){
        return (double) Math.ceil(number / power) * power;
    }

    public final static  String Bytes2String(byte[] buffer, int length){
        String s = "[";
        String supplement = "";
        for(int i = 0; i < length; i++){
            s = s + supplement + String.format("%2s", Integer.toHexString(buffer[i] & 0xFF)).replace(' ', '0');
            supplement = "|";
        }
        s = s + "]";
        return s;
    }

    public final static  void int2Bytes(int i, byte[] buffer){
        buffer[0] = (byte)((i >> 24) & 0xFF) ;
        buffer[1] = (byte)((i >> 16) & 0xFF) ;
        buffer[2] = (byte)((i >> 8) & 0XFF);
        buffer[3] = (byte)((i & 0XFF));
    }

    public final static  String bytes2string(byte[] buffer, int length){
        // in Java int has 4 bytes
        // on ATtiny long has 4 bytes
        String hex = Bytes2String(buffer, length);
        String s = null;
        try {
            s = new String(buffer, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            s = "throw exeption";
        }
        Log.i("read as String:",s);
        Log.i("read as Hex:", hex);

        return hex;
    }

    /*!
     * \brief screen2openGl Given coordinates this function returns the corresponding coordinates in openGL view.
     * \param screen_coordinate The screen coordinates
     * \return Pos3d the converted coordinates
     */
    public final static Pos3d screen2openGl(Pos3d screen_coordinate){
        Pos3d openGL_coordinate = new Pos3d(screen_coordinate);
        final float scaling = Math.min(Globals.screen_width, Globals.screen_height);
        //final float MAGIC_NUMBER = 1.125f;
        // TODO this magic number works only on screens 1080*1920
        final float MAGIC_NUMBER = 2f;
        // TODO this magic number works only on screens 1920*1080
        // I have no idea where I fucked up and I also don't care anymore.
        // I mean this: https://www.khronos.org/registry/OpenGL-Refpages/es1.1/xhtml/glFrustum.xml
        final float factor = (Globals.zoom*MAGIC_NUMBER) / (scaling);
        openGL_coordinate.x = -(screen_coordinate.x - Globals.screen_width/2.f) * factor;
        openGL_coordinate.y = (screen_coordinate.y - Globals.screen_height/2.f) * factor;
        return openGL_coordinate;
    }

    /*!
     * \brief screen2openGl Given coordinates this function returns the corresponding coordinates in openGL view.
     * \param screen_coordinate The screen coordinates
     * \return Pos3d the converted coordinates
     */
    public final static Pos3d openGl2Screen(Pos3d openGL_coordinate){
        Pos3d screen_coordinate = new Pos3d(openGL_coordinate);
        final float scaling = Math.min(Globals.screen_width, Globals.screen_height);
        //final float MAGIC_NUMBER = 1.125f;
        // TODO this magic number works only on screens 1080*1920
        final float MAGIC_NUMBER = 2f;
        // TODO this magic number works only on screens 1920*1080
        // I have no idea where I fucked up and I also don't care anymore.
        // I mean this: https://www.khronos.org/registry/OpenGL-Refpages/es1.1/xhtml/glFrustum.xml
        final float factor = (Globals.zoom*MAGIC_NUMBER) / (scaling);
        screen_coordinate.x = -(openGL_coordinate.x / factor) + Globals.screen_width/2.f;
        screen_coordinate.y = openGL_coordinate.y / factor + Globals.screen_height/2.f;
        return screen_coordinate;
    }

    public final static double screen2OpenGl(int pix){
        return screen2openGl(new Pos3d(pix, 0, 0)).x;
    }

    public final static ViewPort screen2openGl(ViewPort screen){
        Pos3d min = screen2openGl(screen.min);
        Pos3d max = screen2openGl(screen.max);
        return new ViewPort(min, max);
    }


    final static double EPSILON_D = Math.ulp(4.0);
    final static float EPSILON_F = Math.ulp(4.0f);

}
