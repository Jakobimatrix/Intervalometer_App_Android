package de.jakobimatrix.intervalometer;

import android.content.Context;

// class to render a char based on a bitmap using the openGL texture feature
public class DrawableChar extends Texture {

    /*!
     * \brief DrawableChar constructor
     * \param context We all need some context sometimes.
     * \param position The position in openGL coordinates where to display the char.
     * \param font_height the height of the character in openGL scaling
     * \param c The character to be displayed.
     * \param font_height_pix How many pixel are needed into the height (width will be calculated according to the char.
     */
    public DrawableChar(Context context, Pos3d position, float font_height , Character c,  int font_height_pix) {
        super(context, position, 0, 0);
        setChar(c, font_height_pix);
        setHeightWidthOffset(font_height);
        setBotLeftOrigin(); // must be set after height and width
    }

    /*!
     * \brief setHeightWidthOffset given a height, calculate the width of the character.
     * \param font_height In openGL scale
     */
    private void setHeightWidthOffset(float font_height){
        double ratio = alphabet_database.getCharRatio(c);
        CharFontInfo info = alphabet_database.getCharInfo(c);
        height = info.fontSize2CharHeight(font_height);
        width = (float) (ratio*height);
        setRelativePositionToParent(new Pos3d(0, info.fontSize2OffsetBot(font_height), 0));
    }

    /*!
     * \brief setChar set a char to be displayed, gimme the number of pixels in height
     * \param font_height In openGL scale
     */
    private void setChar(Character c,  int font_height_px){
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
