package de.jakobimatrix.intervallometer;

// class to hold r g b a values
public class ColorRGBA {
    public float r;
    public float g;
    public float b;
    public float a;

    ColorRGBA(){
        r = 0;
        g = 0;
        b = 0;
        a = 0;
    }

    ColorRGBA(float r_, float g_, float b_, float a_){
        r=r_;
        g=g_;
        b=b_;
        a=a_;
    }

    ColorRGBA(float r, float g, float b){
        this(r,g,b,1f);
    }

    ColorRGBA(double r, double g, double b, double a){
        this((float) r, (float) g, (float) b, (float) a);
    }

    ColorRGBA(double r, double g, double b){
        this((float) r, (float) g, (float) b, 1f);
    }

    ColorRGBA(String hex){
        setFromHex(hex);
    }

    public final static ColorRGBA red(){
        return new ColorRGBA(1,0,0,1);
    }

    public final static ColorRGBA green(){
        return new ColorRGBA(0,1,0,1);
    }

    public final static ColorRGBA blue(){
        return new ColorRGBA(0,0,1,1);
    }

    public final static ColorRGBA black(){
        return new ColorRGBA(0,0,0,1);
    }

    public final static ColorRGBA white(){
        return new ColorRGBA(1,1,1,1);
    }

    void setFromHex(String hex){
        final int length = hex.length();
        if(length == 7 || length == 9){
            float r_ = (float) Integer.valueOf( hex.substring( 1, 3 ), 16 );
            float g_ = (float) Integer.valueOf( hex.substring( 3, 5 ), 16 );
            float b_ = (float) Integer.valueOf( hex.substring( 5, 7 ), 16 );
            float a_ = 1f;
            if(length == 9){
                a_ = (float) Integer.valueOf( hex.substring( 7, 9 ), 16 );
            }
            r = r_ / 255f;
            g = g_ / 255f;
            b = b_ / 255f;
            a = a_ / 255f;
        }else{
            throw new IllegalArgumentException("Given string " + hex + " must be 7 pr 9 characters long and look like this: #XXXXXX[XX] where X is between 0 and F" );
        }
    }

    public final static ColorRGBA TRANSPARENT = new ColorRGBA(0,0,0,0);
    // todo if api level 24 use Color class?
}
