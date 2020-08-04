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
        for (Map.Entry<Character, CharInfo> entry : LOOK_UP_CHAR_2_INFO.entrySet()) {
            Character c = entry.getKey();
            CharInfo info = entry.getValue();
            android.graphics.drawable.Drawable d = context.getResources().getDrawable(info.resource_id);
            drawables.add(d);
            float width = d.getIntrinsicWidth();
            float height = d.getIntrinsicHeight();
            float ratio = width/height;
            char_ratio.add(ratio);
            look_up_drawables.put(c, i++);
        }
    }

    public double getCharRatio(Character c){
        final Character c_ = validateCharacter(c);
        return char_ratio.get(look_up_drawables.get(c_));
    }

    public Bitmap getBitmap (Character c, int height_pix){
        if(!isInitialized()){
            throw new IllegalArgumentException( "AlphabetDatabase::getBitmap: Thou shall not use me until this.init() has been called." );
        }
        final Character c_ = validateCharacter(c);
        android.graphics.drawable.Drawable d = drawables.get(look_up_drawables.get(c_));
        float ratio =  char_ratio.get(look_up_drawables.get(c_));
        float height_percent = LOOK_UP_CHAR_2_INFO.get(c_).real_height_percentage;
        float true_height = height_pix*height_percent;
        int width_pix = (int) Math.ceil(ratio*true_height);

        // TODO if too slow, we might save each created bitmap and return the saved one if the same bitmap was requested
        return drawableToBitmap(d,width_pix, (int) Math.ceil(true_height));
    }

    private Character validateCharacter(Character c){
        return look_up_drawables.containsKey(c)? c: UNKNOWN_CHAR;
    }

    public CharInfo getCharInfo(Character c){
        return LOOK_UP_CHAR_2_INFO.get(validateCharacter(c));
    }

    final static public java.util.Set<Character> getAllSupportedCharacters(){
        return LOOK_UP_CHAR_2_INFO.keySet();
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

        return bitmap;
    }

    private static Context context = null;

    ArrayList<Float> char_ratio = new ArrayList<>(LOOK_UP_CHAR_2_INFO.size());
    ArrayList<android.graphics.drawable.Drawable> drawables = new ArrayList<>(LOOK_UP_CHAR_2_INFO.size());
    Map<Character, Integer> look_up_drawables = new HashMap<>();

    // font size info
    private static final float CHAR_INFO_DOT_HEIGHT = 5f/49f;
    private static final float CHAR_INFO_DOT_ELEVATION = 13f/49f;
    private static final float CHAR_INFO_COMMA_HEIGHT = 9f/49f;
    private static final float CHAR_INFO_COMMA_ELEVATION = 9f/49f;
    private static final float CHAR_INFO_COLON_HEIGHT = 19f/49f;
    private static final float CHAR_INFO_COLON_ELEVATION = 20f/49f;
    private static final float CHAR_INFO_MINUS_HEIGHT = 5f/49f;
    private static final float CHAR_INFO_MINUS_ELEVATION = 26f/49f;
    private static final float CHAR_INFO_PLUS_HEIGHT = 15f/49f;
    private static final float CHAR_INFO_PLUS_ELEVATION = 22f/49f;
    // 0-9, A, B, C ... b, d, f...
    private static final float CHAR_INFO_FIRST_SECOND_FLOOR_HEIGHT = 36f/49f;
    private static final float CHAR_INFO_FIRST_SECOND_FLOOR_ELEVATION = 13f/49f;

    private static final Character UNKNOWN_CHAR = "?".charAt(0);
    private static final Map<Character, CharInfo> LOOK_UP_CHAR_2_INFO = new HashMap<Character,CharInfo>()
    {{  put('0', new CharInfo(CHAR_INFO_FIRST_SECOND_FLOOR_HEIGHT, CHAR_INFO_FIRST_SECOND_FLOOR_ELEVATION, R.drawable.ic_0));
        put('1', new CharInfo(CHAR_INFO_FIRST_SECOND_FLOOR_HEIGHT, CHAR_INFO_FIRST_SECOND_FLOOR_ELEVATION, R.drawable.ic_1));
        put('2', new CharInfo(CHAR_INFO_FIRST_SECOND_FLOOR_HEIGHT, CHAR_INFO_FIRST_SECOND_FLOOR_ELEVATION, R.drawable.ic_2));
        put('3', new CharInfo(CHAR_INFO_FIRST_SECOND_FLOOR_HEIGHT, CHAR_INFO_FIRST_SECOND_FLOOR_ELEVATION, R.drawable.ic_3));
        put('4', new CharInfo(CHAR_INFO_FIRST_SECOND_FLOOR_HEIGHT, CHAR_INFO_FIRST_SECOND_FLOOR_ELEVATION, R.drawable.ic_4));
        put('5', new CharInfo(CHAR_INFO_FIRST_SECOND_FLOOR_HEIGHT, CHAR_INFO_FIRST_SECOND_FLOOR_ELEVATION, R.drawable.ic_5));
        put('6', new CharInfo(CHAR_INFO_FIRST_SECOND_FLOOR_HEIGHT, CHAR_INFO_FIRST_SECOND_FLOOR_ELEVATION, R.drawable.ic_6));
        put('7', new CharInfo(CHAR_INFO_FIRST_SECOND_FLOOR_HEIGHT, CHAR_INFO_FIRST_SECOND_FLOOR_ELEVATION, R.drawable.ic_7));
        put('8', new CharInfo(CHAR_INFO_FIRST_SECOND_FLOOR_HEIGHT, CHAR_INFO_FIRST_SECOND_FLOOR_ELEVATION, R.drawable.ic_8));
        put('9', new CharInfo(CHAR_INFO_FIRST_SECOND_FLOOR_HEIGHT, CHAR_INFO_FIRST_SECOND_FLOOR_ELEVATION, R.drawable.ic_9));
        put('.', new CharInfo(CHAR_INFO_DOT_HEIGHT, CHAR_INFO_DOT_ELEVATION, R.drawable.ic_dot));
        put(',', new CharInfo(CHAR_INFO_COMMA_HEIGHT, CHAR_INFO_COMMA_ELEVATION, R.drawable.ic_comma));
        put(':', new CharInfo(CHAR_INFO_COLON_HEIGHT, CHAR_INFO_COLON_ELEVATION, R.drawable.ic_colon));
        put(UNKNOWN_CHAR, new CharInfo(CHAR_INFO_FIRST_SECOND_FLOOR_HEIGHT, CHAR_INFO_FIRST_SECOND_FLOOR_ELEVATION, R.drawable.ic_questionmark));
        put('-', new CharInfo(CHAR_INFO_MINUS_HEIGHT, CHAR_INFO_MINUS_ELEVATION, R.drawable.ic_minus));
        put('+', new CharInfo(CHAR_INFO_PLUS_HEIGHT, CHAR_INFO_PLUS_ELEVATION, R.drawable.ic_plus));
        put('%', new CharInfo(CHAR_INFO_FIRST_SECOND_FLOOR_HEIGHT, CHAR_INFO_FIRST_SECOND_FLOOR_ELEVATION, R.drawable.ic_percent));
    }};

}
