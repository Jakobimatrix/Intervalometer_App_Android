package de.jakobimatrix.intervallometer;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

public class Sphere {
    char color = 0;
    float center[]={0,0,0};
    float R;
    float zoom;
    int detail = 10;
    boolean change = false;
    boolean lockedDetail = true;

    int connectingPointslength;
    // Our vertex buffer.
    private FloatBuffer vertexBuffer;
    // Our index buffer.
    private ShortBuffer indexBuffer;

    public double DegToRad(double Deg){
        return Deg*2*3.14159265359/360;
    }

    public int getDetailByZoom(float zoom){
        if(lockedDetail) return this.detail;
        int e= (int)Math.ceil(50*R/Math.abs(zoom));
        if(e<4) e=4; //ist nur halb richtig, da die Distanz zur Kamera nicht beachtet wird
        return e;
    }

    public Sphere(float R, float x, float y, float z, float zoom, char color){
        this.change = true;
        this.center[0]= x; this.center[1]= y; this.center[2]= z;
        this.R=R;
        this.zoom = zoom;
        this.color = color;
    }

    private void Render(){
        if(this.change) {
            detail = getDetailByZoom(zoom);
            short connectNr = 0;
            List<Short> connectingList = new ArrayList<Short>();
            List<Float> gridPointsList = new ArrayList<Float>();

            for (double Roh = 0; Roh <= 180; Roh = Roh + 180 / detail)//todo Redundanz rausnehmen (Punkte oben und unten gibt es bei jeder Umdrehung)
            {
                for (double Phi = 0; Phi <= 360; Phi = Phi + 360 / detail) {
                    gridPointsList.add((float) (R * Math.sin(DegToRad(Roh)) * Math.cos(DegToRad(Phi))) + center[0]);//X
                    gridPointsList.add((float) (R * Math.sin(DegToRad(Roh)) * Math.sin(DegToRad(Phi))) + center[1]);//Y
                    gridPointsList.add((float) (R * Math.cos(DegToRad(Roh))) + center[2]);//Z
                    connectingList.add(connectNr);//longitude
                    connectNr++;
                    connectingList.add(connectNr);//longitude
                }
            }
            connectingList.remove(connectingList.size() - 1);//die letzte Verbindung wieder wech tun
            connectingList.remove(connectingList.size() - 1);
            for (short i = 0; i <= connectNr - detail - 2; i++) {//latitude
                connectingList.add(i);
                connectingList.add((short) (i + detail + 1));
            }


            short connectingPoints[] = new short[connectingList.size()];
            for (int i = 0; i < connectingList.size(); i++) {
                connectingPoints[i] = connectingList.get(i);
            }
            connectingPointslength = connectingPoints.length;

            float gridPoints[] = new float[gridPointsList.size()];
            for (int i = 0; i < gridPointsList.size(); i++) {
                gridPoints[i] = gridPointsList.get(i);
            }


            // a float is 4 bytes, therefore we multiply the number if
            // vertices with 4.
            ByteBuffer vbb = ByteBuffer.allocateDirect(gridPoints.length * 4);
            vbb.order(ByteOrder.nativeOrder());
            vertexBuffer = vbb.asFloatBuffer();
            vertexBuffer.put(gridPoints);
            vertexBuffer.position(0);

            // short is 2 bytes, therefore we multiply the number if
            // vertices with 2.
            ByteBuffer ibb = ByteBuffer.allocateDirect(connectingPoints.length * 2);
            ibb.order(ByteOrder.nativeOrder());
            indexBuffer = ibb.asShortBuffer();
            indexBuffer.put(connectingPoints);
            indexBuffer.position(0);
        }
        change = false;
    }

    public void draw(GL10 gl){
        Render();
        // Enabled the vertices buffer for writing and to be used during
        // rendering.
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // Specifies the location and data format of an array of vertex
        // coordinates to use when rendering.
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

        if(color==0) gl.glColor4f(1.0f, 0.0f, 0.0f, 1.00f);
        else if(color==1) gl.glColor4f(1.0f, 1.0f, 1.0f, 1.00f);
        else if(color==2) gl.glColor4f(1.0f, 1.0f, 0.0f, 1.00f);
        else gl.glColor4f(1.0f, 0.0f, 1.0f, 1.00f);
        //gl.glDrawElements(GL10.GL_LINES, connectingPointslength, GL10.GL_UNSIGNED_SHORT, indexBuffer);    //You see longitude and latitude
        gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, connectingPointslength, GL10.GL_UNSIGNED_SHORT, indexBuffer); //Surface colored
        //gl.glDrawElements(GL10.GL_TRIANGLES, connectingPointslength, GL10.GL_UNSIGNED_SHORT, indexBuffer); //does not what you want! try it...
        //gl.glDrawElements(GL10.GL_TRIANGLE_FAN, connectingPointslength, GL10.GL_UNSIGNED_SHORT, indexBuffer);//Surface colored

    }
    public void draw(GL10 gl,float R,float x, float y, float z, float zoom){
        this.center[0]= x; this.center[1]= y; this.center[2]= z;
        this.R=R;
        this.zoom = zoom;
        this.change = true;
        draw(gl);
    }
    public void setZoom(float zoom){
        if(zoom != this.zoom) {
            this.zoom = zoom;
            this.change = true;
        }
    }
    public void setR(float R){
        if(this.R != R){
            this.R=R;
            this.change = true;
        }

    }
    public void setXYZ(float x, float y, float z){
        this.center[0]= x; this.center[1]= y; this.center[2]= z;
        this.change = true;
    }
    public void setXYZ(List<Float> xyz){
        this.change = true;
        this.center[0]= xyz.get(0); this.center[1]= xyz.get(1); this.center[2]= xyz.get(2);
    }
    public void setXYZ(float[] xyz){
        this.change = true;
        this.center[0]= xyz[0]; this.center[1]= xyz[1]; this.center[2]= xyz[2];
    }
    public void setDetail(int detail){
        if(this.detail != detail) {
            this.detail = detail;
            lockedDetail = true;
            this.change = true;
        }
    }
}
