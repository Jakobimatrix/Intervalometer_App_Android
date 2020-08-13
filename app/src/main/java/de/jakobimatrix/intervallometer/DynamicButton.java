package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.microedition.khronos.opengles.GL10;

// We only inherit form DrawableRectangle for the nifty Origin calculation.
// Every drawable function call will throw an exception, so don't use them.
public class DynamicButton extends DrawableRectangle{
    public DynamicButton(Context context, RelativeLayout layout, int x, int y, int width, int height){
        super(context, new Pos3d(x,y,0), width, height);
        this.layout = layout;
        button = new TextView(context);
        layout.addView(button, new
                RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        setWidthAndHeight(width, height);
        place(x, y);
        setCenterOrigin();
    }

    public void delete(){
        layout.removeView(button);
    }

    public void setColor(int c){
        button.setBackgroundColor(c);
    }

    public void place(int x, int y){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) button.getLayoutParams();
        params.setMargins(x + (int) Math.round(translation.x), y + (int) Math.round(translation.y), 0, 0);
        button.setLayoutParams(params);
    }

    public void setWidthAndHeight(int width, int height){
        setWidth(width);
        setHeight(height);
        button.setWidth(width);
        button.setHeight(height);
    }

    public void setBackground(int img_id){
        Drawable drawable = context.getResources().getDrawable(img_id);
        button.setBackground(drawable);
        // this resets width and height according to bg
        setWidthAndHeight(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    }

    @Override
    public float getWidth(){
        return button.getWidth();
    }

    @Override
    public float getHeight(){
        return button.getHeight();
    }

    public void setText(String text){
        button.setText(text);
    }

    // Sadly I have to override the set Origin Methods since drawable origin is center
    // and Gui element origin is top-left
    public void setCenterOrigin(){
        translation = new Pos3d(-width/2f, -height/2f, 0);
        set_origin_cb = new setCenterOrigin();
    }

    public void setTopLeftOrigin(){
        translation = new Pos3d(0, 0, 0);
        set_origin_cb = new DrawableRectangle.setTopLeftOrigin();
    }

    @Override
    public void setTopCenterOrigin(){
        translation = new Pos3d(-width/2f, 0, 0);
        set_origin_cb = new DrawableRectangle.setTopCenterOrigin();
    }
    @Override
    public void setTopRightOrigin(){
        translation = new Pos3d(-width, 0, 0);
        set_origin_cb = new DrawableRectangle.setTopRightOrigin();
    }
    @Override
    public void setLeftCenterOrigin(){
        translation = new Pos3d(0, -height/2f, 0);
        set_origin_cb = new DrawableRectangle.setLeftCenterOrigin();
    }
    @Override
    public void setBotLeftOrigin(){
        translation = new Pos3d(0, -height, 0);
        set_origin_cb = new DrawableRectangle.setBotLeftOrigin();
    }
    @Override
    public void setBotCenterOrigin(){
        translation = new Pos3d(-width/2f, -height, 0);
        set_origin_cb = new DrawableRectangle.setBotCenterOrigin();
    }
    @Override
    public void setBotRightOrigin(){
        translation = new Pos3d(-width, -height, 0);
        set_origin_cb = new DrawableRectangle.setBotRightOrigin();
    }
    @Override
    public void setRightCenterOrigin(){
        translation = new Pos3d(-width, -height/2f, 0);
        set_origin_cb = new DrawableRectangle.setRightCenterOrigin();
    }
    public RelativeLayout layout;
    public TextView button = null;


    // Throw exception for calling public drawable functions.
    // I know this is ugly, but since java is not able to inherit from more than one parent,
    // I can not make DrawableRectangle inherit from Rectangle and Drawable to split the functionality.
    @Override
    protected void drawForRealNow(GL10 gl){
        throw new IllegalArgumentException( "DynamicButton::drawForRealNow: This function should not be called. This is not a drawable!" );
    }
    @Override
    public void setColor(ColorRGBA c){
        throw new IllegalArgumentException( "DynamicButton::setColor: This function should not be called. This is not a drawable!" );
    }
    public void setPosition(final Pos3d p){
        throw new IllegalArgumentException( "DynamicButton::setPosition: This function should not be called. This is not a drawable!" );
    }
    @Override
    public void move(float dx, float dy, float dz){
        throw new IllegalArgumentException( "DynamicButton::move: This function should not be called. This is not a drawable!" );
    }
    @Override
    public void move(Pos3d dp){
        throw new IllegalArgumentException( "DynamicButton::move: This function should not be called. This is not a drawable!" );
    }
    @Override
    public void setRelativePositionToParent(Pos3d p){
        throw new IllegalArgumentException( "DynamicButton::setRelativePositionToParent: This function should not be called. This is not a drawable!" );
    }
    @Override
    public void add2RelativePositionToParent(Pos3d p){
        throw new IllegalArgumentException( "DynamicButton::add2RelativePositionToParent: This function should not be called. This is not a drawable!" );
    }
    @Override
    public Pos3d getRelativePositionToParent(){
        throw new IllegalArgumentException( "DynamicButton::getRelativePositionToParent: This function should not be called. This is not a drawable!" );
    }
    @Override
    public boolean isWithin(Pos3d p){
        throw new IllegalArgumentException( "DynamicButton::isWithin: This function should not be called. This is not a drawable!" );
    }
    @Override
    public boolean hasChildren(){
        throw new IllegalArgumentException( "DynamicButton::hasChildren: This function should not be called. This is not a drawable!" );
    }
    @Override
    public void adChild(de.jakobimatrix.intervallometer.Drawable child){
        throw new IllegalArgumentException( "DynamicButton::adChild: This function should not be called. This is not a drawable!" );
    }
    @Override
    public void adChild(de.jakobimatrix.intervallometer.Drawable child, Pos3d relative_translation_2_parent){
        throw new IllegalArgumentException( "DynamicButton::adChild: This function should not be called. This is not a drawable!" );
    }
    @Override
    public void setColoringMethodFill(){
        throw new IllegalArgumentException( "DynamicButton::setColoringMethodFill: This function should not be called. This is not a drawable!" );
    }
    @Override
    public void setColoringMethodLines(){
        throw new IllegalArgumentException( "DynamicButton::setColoringMethodLines: This function should not be called. This is not a drawable!" );
    }
    @Override
    public void forceReRender(){
        throw new IllegalArgumentException( "DynamicButton::forceReRender: This function should not be called. This is not a drawable!" );
    }
}
