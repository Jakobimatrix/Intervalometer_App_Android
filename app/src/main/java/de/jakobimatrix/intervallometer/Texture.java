package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Texture extends Drawable {

    public Texture(Context context_, Pos3d position_,float width, float height, int width_px, int height_px) {
        super(context_, position_);
        this.height_px = height_px;
        this.width_px = width_px;
        this.width = width;
        this.height = height;
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
    protected void Render() {
        float vertices[] = {
                (float) position.x, (float) position.y, (float) position.z,		// V1 - bottom left
                (float) position.x,  height,  (float) position.z,		// V2 - top left
                width, (float) position.y,  (float) position.z,		// V3 - bottom right
                width,  height,  (float) position.z			// V4 - top right
        };
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
        // bind the previously generated texture
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        // Point to our buffers
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        // Set the face rotation
        gl.glFrontFace(GL10.GL_CW);

        // Point to our vertex buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertex_buffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texture_buffer);

        // Draw the vertices as triangle strip
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, NUM_VERTICES);

        //Disable the client state before leaving
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
    }

    @Override
    public boolean isWithin(Pos3d p) {
        return false;
    }

    AlphabetDatabase charToBitmapConverter = AlphabetDatabase.getInstance();
    private final static int NUM_VERTICES = 4;
    private FloatBuffer texture_buffer;
    int height_px; // resolution y
    int width_px;  // resolution x
    float height; // openGL y
    float width;  // openGL x
    private Bitmap bitmap = null;
    private int[] textures = new int[1];
    private boolean need_texture_reload = true;
}
