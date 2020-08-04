package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.util.Log;

public class DrawableChar extends Texture {
    public DrawableChar(Context context, Pos3d position, float height , Character c,  int height_pix) {
        super(context, position, 0, height);
        setChar(c, height_pix);
    }

    private void setWidth(){
        double ratio = alphabet_database.getCharRatio(c);
        width = (float) (ratio*height);
    }

    public void setChar(Character c,  int height_px){
        if(this.c != c || this.height_px != height_px){
            this.c = c;
            this.height_px = height_px;
            setWidth();
            setBitmap(charToBitmapConverter.getBitmap(c, height_px));
        }
    }

    int height_px = 0;
    Character c;
    AlphabetDatabase alphabet_database = AlphabetDatabase.getInstance();
}
