package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class DrawableFunction extends Drawable {
    public DrawableFunction(Context context_, Pos3d position_, Function f, double min_x, double max_x, Homography2d system_2_open_gl) {
        super(context_, position_);
        this.f = f;
        this.max_x = max_x;
        this.min_x = min_x;
        this.f2openGL = system_2_open_gl;
    }

    @Override
    protected void Render() {
        final int num_points = num_samples *2;
        final int num_vertices = (num_samples)*COORDS_PER_VERTEX;
        final int num_triangles = num_points - 2;
        final int num_vertices_ids = (num_triangles)*3;// triangle

        ArrayList<Short> vertices_ids_v = new ArrayList<>(num_vertices_ids);
        ArrayList<Float> vertices_v = new ArrayList<Float>(num_vertices);

        Function df = new Function(f.df());

        /*
        * We want to sample num_samples of that function. To get a line with a line_thickness
        * We actually need one point above and one point below the actual sample.
        * But if we do this, the thickness will change depending on the gradient.
        * So we need to sample like this
        * + /
        *  / +
        * Such that the gradiant between + + is -1/grad of function in sample point and also
        * that the distance of each + is line_thickness/2 away from the sample point.
        * Behold calculus:
        * df(x) =: a
        * -1/a =: g
        * line_thickness/2.0 =: d
        * Calculate the needed transformation with pythagoras:
        * dy^2 + dx^2 = d^2 (I)
        * we know the formula for the gradient approximation:
        * g = dy/dx (II)
        * dx*g = dy (III)
        * (III) in (I)
        * dx^2*g^2 + dx^2 = d^2
        * -> dx = d/sqrt(g^2 + 1) (VI)
        * (VI) in (III)
        * -> dy = dx*g
        * */
        double d = line_thickness/2.;

        double x = min_x;
        double eq_step = (max_x-min_x) / num_samples;
        short vertex_id = 0;
        for(int i = 0; i < num_samples; i++){
            double y = f.f(x);
            double g = -1./df.f(x);
            double dx = d/Math.sqrt(g*g + 1.);
            double dy = dx*g;
            if(df.f(x) < 0.000001){
                dx = 0.;
                dy = d;
            }

            Pos3d func_pos_over = new Pos3d(x + dx, y + dy, GRID_ELEVATION_Z);
            Pos3d func_pos_under = new Pos3d(x - dx, y - dy, GRID_ELEVATION_Z);
            Pos3d draw_pos_over = f2openGL.transform(func_pos_over);
            draw_pos_over.add(position);
            Pos3d draw_pos_under = f2openGL.transform(func_pos_under);
            draw_pos_under.add(position);

            vertices_v.add((float) draw_pos_over.x);
            vertices_v.add((float) draw_pos_over.y);
            vertices_v.add((float) draw_pos_over.z);
            vertices_v.add((float) draw_pos_under.x);
            vertices_v.add((float) draw_pos_under.y);
            vertices_v.add((float) draw_pos_under.z);

            if(i < num_samples-1) { // not the last two pints
                vertices_ids_v.add(vertex_id); // over
                vertices_ids_v.add((short) (vertex_id + 1)); // under
                vertices_ids_v.add((short) (vertex_id + 2)); // next over

                vertices_ids_v.add((short) (vertex_id + 1)); //  under
                vertices_ids_v.add((short) (vertex_id + 3)); // next under
                vertices_ids_v.add((short) (vertex_id + 2)); // next over
            }
            vertex_id += 2;
            // This is not really equidistant for changing gradients but it is not as if someone will notice this on that small screen anyway...
            x += eq_step;
        }

        // convert to arrays
        final short vertices_ids_a[] = Utility.ArrayList2ArrayShort(vertices_ids_v);
        final float vertices_a[] = Utility.ArrayList2ArrayFloat(vertices_v);

        // set the buffer and the size variables
        index_buffer_size = vertices_ids_v.size();
        vertex_stride = 0;

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices_a.length * SIZE_OF_FLOAT);
        vbb.order(ByteOrder.nativeOrder());
        vertex_buffer = vbb.asFloatBuffer();
        vertex_buffer.put(vertices_a);
        vertex_buffer.position(0);

        ByteBuffer ibb = ByteBuffer.allocateDirect(vertices_ids_a.length * SIZE_OF_SHORT);
        ibb.order(ByteOrder.nativeOrder());
        index_buffer = ibb.asShortBuffer();
        index_buffer.put(vertices_ids_a);
        index_buffer.position(0);
    }

    @Override
    public boolean isWithin(Pos3d p) {
        return false;
    }

    public void setMax(double max_x) {
        this.max_x = max_x;
        needs_rendering = true;
    }

    public void setMin(double min_x) {
        this.min_x = min_x;
        needs_rendering = true;
    }

    public void setHomography(Homography2d system_2_open_gl){
        this.f2openGL = system_2_open_gl;
        needs_rendering = true;
    }

    public Function getFunction(){
        return f;
    }

    Function f;
    // function coordinates
    double min_x;
    double max_x;

    int num_samples = DEFAULT_NUM_SAMPLES;
    float line_thickness = DEFAULT_LINE_THICKNESS;

    Homography2d f2openGL;
    Homography2d openGL2F;

    final static int DEFAULT_NUM_SAMPLES = 30;
    final static float DEFAULT_LINE_THICKNESS = 0.075f;
    final static double GRID_ELEVATION_Z = 0.01;
}
