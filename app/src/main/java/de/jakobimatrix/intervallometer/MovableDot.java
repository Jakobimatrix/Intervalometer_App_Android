package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class MovableDot {

    MovableDot(Context c, pos3d.Pos2d init_pos){
        pos = init_pos;
        setupShader(c);
        setupVertexBuffer();
    }

    private void setupVertexBuffer() {
        //vertex_buffer = com.example.user.squareobjectwithopengl.tools.BufferUtils.newFloatBuffer(todoCoords.length);
        vertex_buffer.put(todoCoords);
        vertex_buffer.position(0);
        IntBuffer buffer = IntBuffer.allocate(1);
        GLES20.glGenBuffers(1,buffer);
        vertex_buffer_id = buffer.get(0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertex_buffer_id);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, todoCoords.length * 4, vertex_buffer, GLES20.GL_STATIC_DRAW);

        vertex_count = todoCoords.length / (COORDS_PER_VERTEX + COLORS_PER_VERTEX);
        vertex_stride = (COORDS_PER_VERTEX + COLORS_PER_VERTEX) * 4;
    }

    private void setupShader(Context c) {
        /*shader = new ShaderProgram(
                com.example.user.squareobjectwithopengl.tools.ShaderUtils.readShaderFileFromFilePath(c, R.raw.simple_vertex_shader),
                com.example.user.squareobjectwithopengl.tools.ShaderUtils.readShaderFileFromFilePath(c, R.raw.simple_fragment_shader)
        );*/
    }

    public void draw(){
        /*shader.begin();
        shader.enableVertexAttribute("a_Position");
        shader.setVertexAttribute("a_Position", COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertex_stride, 0);
        shader.enableVertexAttribute("a_Color");
        shader.setVertexAttribute("a_Color", COLORS_PER_VERTEX, GLES20.GL_FLOAT, false, vertex_stride, COORDS_PER_VERTEX * SIZE_OF_FLOAT);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertex_buffer_id);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertex_count);
        shader.disableVertexAttribute("a_Position");
        shader.disableVertexAttribute("a_Color");
        shader.end();*/
    }

    private pos3d.Pos2d pos;
    private FloatBuffer vertex_buffer;
    private int vertex_buffer_id;
    private int vertex_count;
    private int vertex_stride;

    static final int COORDS_PER_VERTEX = 3;
    static final int COLORS_PER_VERTEX = 4;
    static final int SIZE_OF_FLOAT = 4;
    static final float todoCoords[] = {
            -0.5f, 0.5f, 0, 1f, 0, 0, 1f,
            -0.5f, -0.5f, 0, 0, 1f, 0, 1f,
            0.5f, -0.5f, 0, 0, 0, 1f, 1f,
            -0.5f, 0.5f, 0, 1f, 0, 0, 1f,
            0.5f, -0.5f, 0, 0, 0, 1f, 1f,
            0.5f, 0.5f, 0, 0, 1f, 0, 1f,
    };
}
