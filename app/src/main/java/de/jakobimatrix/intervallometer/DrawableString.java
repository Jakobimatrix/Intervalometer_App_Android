package de.jakobimatrix.intervallometer;

import android.content.Context;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

public class DrawableString extends Drawable {
    public DrawableString(Context context_, Pos3d position_, float font_size, int font_height_pix, String value) {
        super(context_, position_);
        this.font_size = font_size;
        this.font_height_pix = font_height_pix;
        setString(value);
    }

    @Override
    protected void Render() {
        for(DrawableChar dc : string){
            dc.Render();
        }
    }

    @Override
    public void draw(GL10 gl){
        for(DrawableChar dc : string){
            dc.draw(gl);
        }
    }

    @Override
    public boolean isWithin(Pos3d p) {
        return false;
    }

    @Override
    public void move(Pos3d p){
        super.move(p);
        for(DrawableChar dc : string){
            dc.move(p);
        }
    }

    @Override
    public void setPosition(Pos3d p){
        Pos3d dp = Pos3d.sub(p, getPosition());
        move(dp);
    }

    public void setString(String value){
        Pos3d offset = Pos3d.Zero();
        Pos3d add = Pos3d.Zero();
        Pos3d margin_right = new Pos3d(CHAR_DIST_PERCENT*font_size,0,0);
        string.clear();
        for(int i = 0; i < value.length(); i++){
            DrawableChar dc = new DrawableChar(context, getPosition(), font_size , value.charAt(i), font_height_pix);
            dc.setBotLeftOrigin();
            Pos3d origin = new Pos3d(dc.getOrigin());
            dc.setCustomOrigin(Pos3d.add(origin, offset));
            dc.setRotation(rotation);
            string.add(dc);
            // calc offset for next char
            add.x = dc.width;
            offset.add(add);
            offset.add(margin_right);
        }
        needs_rendering = true;
    }

    public double getWidth(){
        if(string.size() == 0){
            return 0;
        }
        DrawableChar dc = string.get(string.size()-1);
        return dc.getPosition().x + dc.getWidth() - getPosition().x;
    }

    public void setRotation(float rot_rad){
        rotation = rot_rad;
        for(DrawableChar dc : string){
            dc.setRotation(rot_rad);
        }
    }

    float font_size;
    int font_height_pix;
    float rotation = 0;

    final static float CHAR_DIST_PERCENT = 0.1f;

    ArrayList<DrawableChar> string = new ArrayList<>();
}
