package de.jakobimatrix.intervalometer;

/*
* https://i.stack.imgur.com/8E8XO.gif
* */
// class to hold Information about the font like height relative to the font size and how much it needs to be raised.
public class CharFontInfo {
    CharFontInfo(float real_height_percentage, float elevation_percentage, int resource_id){
        this.real_height_percentage = real_height_percentage;
        this.elevation_percentage = elevation_percentage;
        this.resource_id = resource_id;
    }
    public float real_height_percentage; // [0-1] If the font size is x, tha character height is x*real_height_percentage
    public float elevation_percentage; // about how much of the total font size we have to lift the character
    public int resource_id;

    public float fontSize2OffsetBot(float font_size){
       return elevation_percentage*font_size;
    }

    public float fontSize2CharHeight(float font_size){
        return real_height_percentage*font_size;
    }
}
