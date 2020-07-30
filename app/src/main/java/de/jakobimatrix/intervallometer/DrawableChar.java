package de.jakobimatrix.intervallometer;

import android.content.Context;

public class DrawableChar extends Texture {
    public DrawableChar(Context context_, Pos3d position_, float height_ ,Character c,  int height_pix) {
        super(context_, position_, 0, height_, 0, height_pix);
        setWidth();
        setChar(c, height_px);
    }

    private void setWidth(){
        double ratio = alphabet_database.getCharRatio(c);
        width_px = (int) Math.round(ratio*height_px);
        width = (float) (ratio*height);
    }

    public void setChar(Character c,  int height_px){
        if(this.c != c || this.height_px != height_px){
            this.height_px = height_px;
            setWidth();
            setBitmap(charToBitmapConverter.getBitmap(c, width_px, height_px));
        }
    }

    Character c;
    AlphabetDatabase alphabet_database = AlphabetDatabase.getInstance();
}
