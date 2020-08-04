package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

public class Texture extends DrawableRectangle {

    public Texture(Context context, Pos3d position, float width, float height) {
        super(context, position, width, height);
    }

    @Override
    public void clean(){
        if(bitmap != null){
            bitmap.recycle();
        }
        super.clean();
    }

    public void setBitmap(Bitmap bitmap){
        if(this.bitmap != null){
            this.bitmap.recycle();
        }
        this.bitmap = bitmap;
        need_texture_reload = true;
    }

    public void loadGLTexture(GL10 gl) {
        // generate one texture pointer
        gl.glGenTextures(1, textures, 0);
        // ...and bind it to our array
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        // create nearest filtered texture
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        //Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
//		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
//		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);

        // Use Android GLUtils to specify a two-dimensional texture image from our bitmap
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        need_texture_reload = false;
    }

    @Override
    public void forceReRender(){
        super.forceReRender();
        need_texture_reload = true;
    }

    @Override
    protected void Render() {
        final int num_vertices = (NUM_CORNERS)*COORDS_PER_VERTEX;

        float [] vertices = new float[num_vertices];

        calculateCornerPositions();
        int counter = 0;

        Integer corner_order[] = {2,1,3,0}; // Don't ask, just go with it.
        //Integer corner_order[] = {3,0,2,1}; // This would mirror vertically
        //Integer corner_order[] = {1,0,2,3}; // <-
        //Integer corner_order[] = {0,1,3,2}; // ->
        for(int i = 0; i < NUM_CORNERS; i++){
            for(int v = 0; v < COORDS_PER_VERTEX; v++){
                vertices[counter++] = (float) corner[corner_order[i]].get(v);
            }
        }

        float texture[] = {
                // Mapping coordinates for the vertices
                0.0f, 1.0f,		// top left		(V2)
                0.0f, 0.0f,		// bottom left	(V1)
                1.0f, 1.0f,		// top right	(V4)
                1.0f, 0.0f		// bottom right	(V3)
        };

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * SIZE_OF_FLOAT);
        vbb.order(ByteOrder.nativeOrder());
        vertex_buffer = vbb.asFloatBuffer();
        vertex_buffer.put(vertices);
        vertex_buffer.position(0);

        ByteBuffer tbb = ByteBuffer.allocateDirect(texture.length * SIZE_OF_FLOAT);
        tbb.order(ByteOrder.nativeOrder());
        texture_buffer = tbb.asFloatBuffer();
        texture_buffer.put(texture);
        texture_buffer.position(0);
    }

    @Override
    protected void drawForRealNow(GL10 gl){
        if(need_texture_reload){
            loadGLTexture(gl);
        }

        // enable texture
        gl.glEnable(GL10.GL_TEXTURE_2D);

        // Point to our buffers
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        // Bind the generated texture using the pointer generated in loadGLTexture.
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        // Point to our vertex buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertex_buffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texture_buffer);

        // Draw the vertices as triangle strip
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, NUM_VERTICES);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glDisable(GL10.GL_TEXTURE_2D);
    }

    @Override
    public boolean isWithin(Pos3d p) {
        return false;
    }

    AlphabetDatabase charToBitmapConverter = AlphabetDatabase.getInstance();
    private final static int NUM_VERTICES = 4;
    private FloatBuffer texture_buffer;

    private Bitmap bitmap = null;
    private int[] textures = new int[1];
    private boolean need_texture_reload = true;
}
