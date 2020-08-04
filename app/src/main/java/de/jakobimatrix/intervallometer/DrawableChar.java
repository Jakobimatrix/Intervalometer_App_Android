package de.jakobimatrix.intervallometer;

import android.content.Context;

public class DrawableChar extends Texture {
    public DrawableChar(Context context, Pos3d position, float font_height , Character c,  int font_height_pix) {
        super(context, position, 0, 0);
        setChar(c, font_height_pix);
        setHeightWidthOffset(font_height);
        setBotLeftOrigin(); // must be set after height and width
    }

    private void setHeightWidthOffset(float font_height){
        double ratio = alphabet_database.getCharRatio(c);
        CharInfo info = alphabet_database.getCharInfo(c);
        height = info.fontSize2CharHeight(font_height);
        width = (float) (ratio*height);
        setRelativePositionToParent(new Pos3d(0, info.fontSize2OffsetBot(font_height), 0));
    }

    public void setChar(Character c,  int font_height_px){
        if(this.c != c || this.font_height_px != font_height_px){
            this.c = c;
            this.font_height_px = font_height_px;
            setBitmap(charToBitmapConverter.getBitmap(c, font_height_px));
        }
    }

    int font_height_px = 0;
    Character c;
    AlphabetDatabase alphabet_database = AlphabetDatabase.getInstance();
}
