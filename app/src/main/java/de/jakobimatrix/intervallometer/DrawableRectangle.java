package de.jakobimatrix.intervallometer;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DrawableRectangle extends Drawable {
    public DrawableRectangle(Context context_, Pos3d position_, float width_, float height_) {
        super(context_, position_);
        width = width_;
        height = height_;
        if(width_ < 0 || height_ < 0){
            throw new IllegalArgumentException( "DrawableRectangle: given height,width must be positive! Use the SetXXXOrigin methods for changing perspective.");
        }
    }

    protected void calculateCornerPositions(){
        // top-left
        corner[0] = new Pos3d(width/2f , height/2f, 0);
        // bot-left
        corner[1] = new Pos3d(-width/2f , height/2f, 0);
        // bot-right
        corner[2] = new Pos3d(-width/2f , -height/2f, 0);
        // top-right
        corner[3] = new Pos3d(width/2f , -height/2f, 0);

        for(int i = 0; i < NUM_CORNERS; i++){
            corner[i].add(translation);
            corner[i].rotateZ(rotation);
            corner[i].add(position);
        }
    }

    @Override
    protected void Render() {
        final int num_vertices = (NUM_CORNERS)*COORDS_PER_VERTEX;
        final int num_vertices_ids = (2)*3;// triangle

        short [] vertices_ids = new short[num_vertices_ids];
        float [] vertices = new float[num_vertices];

        calculateCornerPositions();

        int counter = 0;
        for(int i = 0; i < NUM_CORNERS; i++){
            for(int v = 0; v < COORDS_PER_VERTEX; v++){
                vertices[counter++] = (float) corner[i].get(v);
            }
        }

        vertices_ids[0] = (short) 0;
        vertices_ids[1] = (short) 1;
        vertices_ids[2] = (short) 2;

        vertices_ids[3] = (short) 0;
        vertices_ids[4] = (short) 2;
        vertices_ids[5] = (short) 3;

        // set the buffer and the size variables
        index_buffer_size = num_vertices_ids;
        vertex_stride = 0;

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * SIZE_OF_FLOAT);
        vbb.order(ByteOrder.nativeOrder());
        vertex_buffer = vbb.asFloatBuffer();
        vertex_buffer.put(vertices);
        vertex_buffer.position(0);

        ByteBuffer ibb = ByteBuffer.allocateDirect(vertices_ids.length * SIZE_OF_SHORT);
        ibb.order(ByteOrder.nativeOrder());
        index_buffer = ibb.asShortBuffer();
        index_buffer.put(vertices_ids);
        index_buffer.position(0);
    }

    @Override
    public boolean isWithin(Pos3d p) {
        if(Utility.isWithinTriangle(corner[0],corner[1],corner[2], p) ||
                Utility.isWithinTriangle(corner[0],corner[2],corner[3], p)){
            return true;
        }
        return false;
    }

    public interface CallBackOnSetOrigin{
        void setOrigin();
    }

    class setCenterOrigin implements CallBackOnSetOrigin {
        public void setOrigin() {
            setCenterOrigin();
        }
    }

    public void setCenterOrigin(){
        translation = Pos3d.Zero();
        needs_rendering = true;
        set_origin_cb = new setCenterOrigin();
    }

    class setTopLeftOrigin implements CallBackOnSetOrigin {
        public void setOrigin() {
            setTopLeftOrigin();
        }
    }

    public void setTopLeftOrigin(){
        translation = new Pos3d(width/2f, -height/2f, 0);
        needs_rendering = true;
        set_origin_cb = new setTopLeftOrigin();
    }

    class setTopCenterOrigin implements CallBackOnSetOrigin {
        public void setOrigin() {
            setTopCenterOrigin();
        }
    }

    public void setTopCenterOrigin(){
        translation = new Pos3d(0, -height/2f, 0);
        needs_rendering = true;
        set_origin_cb = new setTopCenterOrigin();
    }

    class setTopRightOrigin implements CallBackOnSetOrigin {
        public void setOrigin() {
            setTopRightOrigin();
        }
    }

    public void setTopRightOrigin(){
        translation = new Pos3d(-width/2f, -height/2f, 0);
        needs_rendering = true;
        set_origin_cb = new setTopRightOrigin();
    }

    class setLeftCenterOrigin implements CallBackOnSetOrigin {
        public void setOrigin() {
            setLeftCenterOrigin();
        }
    }

    public void setLeftCenterOrigin(){
        translation = new Pos3d(width/2f, 0, 0);
        needs_rendering = true;
        set_origin_cb = new setLeftCenterOrigin();
    }

    class setBotLeftOrigin implements CallBackOnSetOrigin {
        public void setOrigin() {
            setBotLeftOrigin();
        }
    }

    public void setBotLeftOrigin(){
        translation = new Pos3d(width/2f, height/2f, 0);
        needs_rendering = true;
        set_origin_cb = new setBotLeftOrigin();
    }

    class setBotCenterOrigin implements CallBackOnSetOrigin {
        public void setOrigin() {
            setBotCenterOrigin();
        }
    }

    public void setBotCenterOrigin(){
        translation = new Pos3d(0, height/2f, 0);
        needs_rendering = true;
        set_origin_cb = new setBotCenterOrigin();
    }

    class setBotRightOrigin implements CallBackOnSetOrigin {
        public void setOrigin() {
            setBotRightOrigin();
        }
    }

    public void setBotRightOrigin(){
        translation = new Pos3d(-width/2f, height/2f, 0);
        needs_rendering = true;
        set_origin_cb = new setBotRightOrigin();
    }

    class setRightCenterOrigin implements CallBackOnSetOrigin {
        public void setOrigin() {
            setRightCenterOrigin();
        }
    }

    public void setRightCenterOrigin(){
        translation = new Pos3d(-width/2f, 0, 0);
        needs_rendering = true;
        set_origin_cb = new setRightCenterOrigin();
    }

    public float getWidth(){
        return width;
    }

    public  float getHeight(){
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
        set_origin_cb.setOrigin();
        if(height < 0){
            throw new IllegalArgumentException( "DrawableRectangle::setHeight given height must be positive! Use the SetXXXOrigen methods for changing perspective.");
        }
    }

    public void setWidth(float width){
        this.width = width;
        set_origin_cb.setOrigin();
        if(width < 0){
            throw new IllegalArgumentException( "DrawableRectangle::setWidth given width must be positive! Use the SetXXXOrigen methods for changing perspective.");
        }
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
        needs_rendering = true;
    }

    public double getRotation() {
        return rotation;
    }

    double rotation = 0;

    Pos3d translation = Pos3d.Zero();
    CallBackOnSetOrigin set_origin_cb = new setBotLeftOrigin();

    float width;
    float height;
    final static int NUM_CORNERS = 4;
    Pos3d [] corner = new Pos3d [NUM_CORNERS];
}
