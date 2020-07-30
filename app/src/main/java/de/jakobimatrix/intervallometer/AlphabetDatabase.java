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
        loadDrawablesNumbers();
        loadDrawablesLetters();
        loadDrawablesSpecialCharacters();
        registerCharRatios();
    }

    private void loadDrawablesNumbers(){
        drawables.add(context.getResources().getDrawable(R.drawable.ic_0));
        look_up_drawables.put("0".charAt(0),key++);
        drawables.add(context.getResources().getDrawable(R.drawable.ic_1));
        look_up_drawables.put("1".charAt(0),key++);
        drawables.add(context.getResources().getDrawable(R.drawable.ic_2));
        look_up_drawables.put("2".charAt(0),key++);
        drawables.add(context.getResources().getDrawable(R.drawable.ic_3));
        look_up_drawables.put("3".charAt(0),key++);
        drawables.add(context.getResources().getDrawable(R.drawable.ic_4));
        look_up_drawables.put("4".charAt(0),key++);
        drawables.add(context.getResources().getDrawable(R.drawable.ic_5));
        look_up_drawables.put("5".charAt(0),key++);
        drawables.add(context.getResources().getDrawable(R.drawable.ic_6));
        look_up_drawables.put("6".charAt(0),key++);
        drawables.add(context.getResources().getDrawable(R.drawable.ic_7));
        look_up_drawables.put("7".charAt(0),key++);
        drawables.add(context.getResources().getDrawable(R.drawable.ic_8));
        look_up_drawables.put("8".charAt(0),key++);
        drawables.add(context.getResources().getDrawable(R.drawable.ic_9));
        look_up_drawables.put("9".charAt(0),key++);
    }

    private void loadDrawablesLetters(){
        //look_drawables.add();
        //look_drawables.put("?".charAt(0),key++);
    }

    private void loadDrawablesSpecialCharacters(){
        Integer i = 0;
        drawables.add(context.getResources().getDrawable(R.drawable.ic_dot));
        look_up_drawables.put(".".charAt(0),key++);
        drawables.add(context.getResources().getDrawable(R.drawable.ic_comma));
        look_up_drawables.put(",".charAt(0),key++);
        drawables.add(context.getResources().getDrawable(R.drawable.ic_colon));
        look_up_drawables.put(":".charAt(0),key++);
        drawables.add(context.getResources().getDrawable(R.drawable.ic_minus));
        look_up_drawables.put("-".charAt(0),key++);
        drawables.add(context.getResources().getDrawable(R.drawable.ic_plus));
        look_up_drawables.put("+".charAt(0),key++);
        drawables.add(context.getResources().getDrawable(R.drawable.ic_percent));
        look_up_drawables.put("%".charAt(0),key++);

        // DON'T DELETE THIS! IT ALWAYS SERVES AS UNKNOWN CHAR
        drawables.add(context.getResources().getDrawable(R.drawable.ic_questionmark));
        look_up_drawables.put("?".charAt(0),key++);
    }

    private void registerCharRatios(){
        for(int i = 0; i < drawables.size(); i++){
            double width = drawables.get(i).getIntrinsicWidth();
            double height = drawables.get(i).getIntrinsicHeight();
            double ratio = width/height;
            char_ratio.add(ratio);
        }
    }

    public double getCharRatio(Character c){
        if(look_up_drawables.containsKey(c)){
            return char_ratio.get(look_up_drawables.get(c));
        }
        return char_ratio.get(look_up_drawables.get("?".charAt(0)));
    }

    public Bitmap getBitmap (Character c, int width_pix, int height_pix){
        if(!isInitialized()){
            throw new IllegalArgumentException( "AlphabetDatabase::getBitmap: Thou shall not use me until this.init() has been called." );
        }
        final Character c_ = look_up_drawables.containsKey(c)? c: UNKNOWN_CHAR;
        android.graphics.drawable.Drawable d = drawables.get(look_up_drawables.get(c_));

        // TODO if too slow, we might save each created bitmap and return the saved one if the same bitmap was requested
        return drawableToBitmap(d,width_pix, height_pix);
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

    private Integer key = 0;
    private final static int NUM_NUMBERS = 9;
    private final static int NUM_LETTERS = 0;
    private final static int NUM_SPECIAL_CHARACTERS = 6;

    private final Character UNKNOWN_CHAR = "?".charAt(0);

    ArrayList<Double> char_ratio = new ArrayList<>(NUM_NUMBERS + NUM_LETTERS + NUM_SPECIAL_CHARACTERS);
    ArrayList<android.graphics.drawable.Drawable> drawables = new ArrayList<>(NUM_NUMBERS + NUM_LETTERS + NUM_SPECIAL_CHARACTERS);
    Map<Character, Integer> look_up_drawables = new HashMap<>();
}
