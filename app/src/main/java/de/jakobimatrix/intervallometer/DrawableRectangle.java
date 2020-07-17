package de.jakobimatrix.intervallometer;

import android.content.Context;

import pos3d.Pos3d;

public class DrawableRectangle extends Drawable {
    public DrawableRectangle(Context context_, Pos3d position_, float width, float height) {
        super(context_, position_);
    }

    @Override
    protected void Render() {

    }

    @Override
    public boolean isWithin(Pos3d p) {
        return false;
    }

    float width;
    float height;
    Pos3d rotation = new Pos3d(0,0,0);
}
