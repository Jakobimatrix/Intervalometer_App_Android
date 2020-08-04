package de.jakobimatrix.intervallometer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// This stores all drawables and generates if requested the bitmaps
// Its also a singleton
public class AlphabetDatabase {
    private static AlphabetDatabase instance = null;

    protected AlphabetDatabase(){ }

    public static synchronized AlphabetDatabase getInstance() {
        if(null == instance){
            instance = new AlphabetDatabase();
        }
        return instance;
    }

    public void init(Context context){
        this.context = context;
        loadDrawables();
    }

    public boolean isInitialized(){
        return context != null;
    }

    private void loadDrawables(){
        int i = 0;
        for (Map.Entry<Character, Integer> entry : LOOK_UP_CHAR_2_RESOURCE.entrySet()) {
            Character c = entry.getKey();
            Integer id = entry.getValue();
            android.graphics.drawable.Drawable d = context.getResources().getDrawable(id);
            drawables.add(d);
            double width = d.getIntrinsicWidth();
            double height = d.getIntrinsicHeight();
            double ratio = width/height;
            char_ratio.add(ratio);
            look_up_drawables.put(c, i++);
        }
    }

    public double getCharRatio(Character c){
        final Character c_ = look_up_drawables.containsKey(c)? c: UNKNOWN_CHAR;
        return char_ratio.get(look_up_drawables.get(c_));
    }

    public Bitmap getBitmap (Character c, int height_pix){
        if(!isInitialized()){
            throw new IllegalArgumentException( "AlphabetDatabase::getBitmap: Thou shall not use me until this.init() has been called." );
        }
        final Character c_ = look_up_drawables.containsKey(c)? c: UNKNOWN_CHAR;
        android.graphics.drawable.Drawable d = drawables.get(look_up_drawables.get(c_));
        int width_pix = (int) Math.ceil(char_ratio.get(look_up_drawables.get(c_))*height_pix);

        // TODO if too slow, we might save each created bitmap and return the saved one if the same bitmap was requested
        return drawableToBitmap(d,width_pix, height_pix);
    }

    final static public java.util.Set<Character> getAllSupportedCharacters(){
        return LOOK_UP_CHAR_2_RESOURCE.keySet();
    }

    // https://stackoverflow.com/questions/24389043/bitmapfactory-decoderesource-returns-null-for-shape-defined-in-xml-drawable
    public static Bitmap drawableToBitmap (android.graphics.drawable.Drawable drawable, int res_pix_x, int res_pix_y) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        // TODO Bitmap.Config.ARGB_8888 this is color 32 bit
        // TODO USE ALPHA_8 ?? instead
        Bitmap bitmap = Bitmap.createBitmap(res_pix_x, res_pix_y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        double c =  canvas.getWidth();
        double d = canvas.getHeight();

        return bitmap;
    }

    private static Context context = null;

    ArrayList<Double> char_ratio = new ArrayList<>(LOOK_UP_CHAR_2_RESOURCE.size());
    ArrayList<android.graphics.drawable.Drawable> drawables = new ArrayList<>(LOOK_UP_CHAR_2_RESOURCE.size());
    Map<Character, Integer> look_up_drawables = new HashMap<>();

    private static final Character UNKNOWN_CHAR = "?".charAt(0);
    private static final Map<Character, Integer> LOOK_UP_CHAR_2_RESOURCE = new HashMap<Character,Integer>()
    {{  put('0', R.drawable.ic_0);
        put('1', R.drawable.ic_1);
        put('2', R.drawable.ic_2);
        put('3', R.drawable.ic_3);
        put('4', R.drawable.ic_4);
        put('5', R.drawable.ic_5);
        put('6', R.drawable.ic_6);
        put('7', R.drawable.ic_7);
        put('8', R.drawable.ic_8);
        put('9', R.drawable.ic_9);
        put('.', R.drawable.ic_dot);
        put(',', R.drawable.ic_comma);
        put(':', R.drawable.ic_colon);
        put(UNKNOWN_CHAR, R.drawable.ic_questionmark);
        put('-', R.drawable.ic_minus);
        put('+', R.drawable.ic_plus);
        put('%', R.drawable.ic_percent);
    }};

}
