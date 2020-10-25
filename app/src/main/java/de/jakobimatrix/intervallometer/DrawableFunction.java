package de.jakobimatrix.intervallometer;

import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

// clas to display a function within a given view
public class DrawableFunction extends Drawable {
    /*!
     * \brief DrawableFunction Constructor
     * \param context_ someone som context please?
     * \param position_ the BOT LEFT corner of the system in open GL coordinates
     * \param f The function to be displayed
     * \param min_x The minimal value of f(x) to be rendered
     * \param max_x The maximal value of f(x) to be rendered
     * \param system_2_open_gl homography describing the conversation from system coordinates to open gl coordinates.
     */
    public DrawableFunction(Context context_, Pos3d position_, Function f, double min_x, double max_x, Homography2d system_2_open_gl) {
        super(context_, position_);
        this.f = f;
        this.max_x = max_x;
        this.min_x = min_x;
        swapIf();
        this.f2openGL = system_2_open_gl;

        begin = new DrawableCircle(context_, position_, (float) (line_thickness/2.));
        end = new DrawableCircle(context_, position_, (float) (line_thickness/2.));

        adChild(begin);
        adChild(end);

        if(max_x < min_x){
            throw new IllegalArgumentException( "DrawableFunction: Given min > given max");
        }
    }

    @Override
    protected void Render() {
        final int num_points = num_samples *2;
        final int num_vertices = (num_samples)*COORDS_PER_VERTEX;
        final int num_triangles = num_points - 2;
        final int num_vertices_ids = (num_triangles)*3;// triangle

        ArrayList<Short> vertices_ids_v = new ArrayList<>(num_vertices_ids);
        ArrayList<Float> vertices_v = new ArrayList<Float>(num_vertices);


        double x_scale = f2openGL.h[0];
        double y_scale = f2openGL.h[4];

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
        double d = 1;

        double x = min_x;
        double eq_step = (max_x-min_x) / (num_samples-1);
        short vertex_id = 0;
        for(int i = 0; i < num_samples; i++){
            double grad = f.df(x);
            double y = f.f(x);
            double g = -1./grad;
            double dx = d/Math.sqrt(g*g + 1.);
            double dy = dx*g;
            if(Math.abs(grad) < 0.00000001){
                dx = 0.;
                dy = d;
            }
            double vz = (grad > 0)?1:-1;

            Pos3d func_center = new Pos3d(x, y, Globals.FUNCTION_Z_ELEVATION);
            Pos3d func_center_gl = f2openGL.transform(func_center);
            Pos3d func_orth_width_wrong_length_gl = new Pos3d(vz*dx*y_scale,vz*dy*x_scale,0);
            Pos3d func_over_gl_wrong_length = Pos3d.add(func_center_gl, func_orth_width_wrong_length_gl);

            // calculate dx, dy in open GL such that
            // (line_thickness/2)^2 = c^2 = dx^2 + dy^2
            // first norm ->
            // 1 = c^2 = dx_n^2 + dy_n^2 -> c = 1
            Pos3d func_orth_width_gl = Pos3d.sub(func_over_gl_wrong_length, func_center_gl);
            func_orth_width_gl.z = 0;
            func_orth_width_gl.div(func_orth_width_gl.norm());
            // now (line_thickness/2)^2 = [sqrt(line_thickness/2 *c]^2 = [sqrt(line_thickness/2 *dx]^2 * [sqrt(line_thickness/2 *dy]^2
            func_orth_width_gl.mul(line_thickness/2.);

            Pos3d func_gl_over = Pos3d.add(func_center_gl, func_orth_width_gl);
            Pos3d func_gl_under = Pos3d.sub(func_center_gl, func_orth_width_gl);

            vertices_v.add((float) func_gl_over.x);
            vertices_v.add((float) func_gl_over.y);
            vertices_v.add((float) func_gl_over.z);
            vertices_v.add((float) func_gl_under.x);
            vertices_v.add((float) func_gl_under.y);
            vertices_v.add((float) func_gl_under.z);

            if(i < num_samples-1) { // not the last two pints
                vertices_ids_v.add((short) (vertex_id + 2)); // // next over
                vertices_ids_v.add((short) (vertex_id)); // over
                vertices_ids_v.add((short) (vertex_id + 1)); // under

                vertices_ids_v.add((short) (vertex_id + 2)); // next over
                vertices_ids_v.add((short) (vertex_id + 1)); // under
                vertices_ids_v.add((short) (vertex_id + 3)); // next under
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

        //setColoringMethodLines();

        Pos3d begin_pos_f = new Pos3d(min_x, f.f(min_x), Globals.FUNCTION_Z_ELEVATION);
        Pos3d end_pos_f = new Pos3d(max_x, f.f(max_x), Globals.FUNCTION_Z_ELEVATION);
        Pos3d draw_begin_pos = f2openGL.transform(begin_pos_f);
        Pos3d draw_end_pos = f2openGL.transform(end_pos_f);
        Pos3d rel_pos_begin = Pos3d.sub(draw_begin_pos, getPosition());
        Pos3d rel_pos_end = Pos3d.sub(draw_end_pos, getPosition());
        begin.setRelativePositionToParent(rel_pos_begin);
        end.setRelativePositionToParent(rel_pos_end);
    }

    @Override
    public boolean isWithin(Pos3d p) {
        return false;
    }

    /*!
     * \brief setMax
     * \param max_x The maximal value of f(x) to be rendered
     */
    public void setMax(double max_x) {
        this.max_x = max_x;
        swapIf();
        needs_rendering = true;
    }

    /*!
     * \brief moveY move the function in y direction
     * \param dy the length at which to move the function in y direction.
     */
    public void moveY(double dy){
        getFunction().moveOffsetY(dy);
        needs_rendering = true;
    }

    /*!
     * \brief moveX move the function in x direction
     * \param dx the length at which to move the function in x direction.
     */
    public void moveX(double dx){
        getFunction().moveOffsetX(dx);
        //max_x += dx;
        //min_x += dx;
        needs_rendering = true;
    }

    /*!
     * \brief setMin
     * \param min_x The maximal value of f(x) to be rendered
     */
    public void setMin(double min_x) {
        this.min_x = min_x;
        swapIf();
        needs_rendering = true;
    }

    /*!
     * \brief swapIf make sure that max_x > min_x
     */
    private void swapIf(){
        // std::swap anyone? Oh that's right fucking Java can not make a double a reference.
        if(this.min_x > this.max_x){
            double temp = min_x;
            this.min_x = this.max_x;
            this.max_x = temp;
        }
    }

    /*!
     * \brief setHomography set a new homography system 2 openGl
     */
    public void setHomography(Homography2d system_2_open_gl){
        f2openGL = system_2_open_gl;
        needs_rendering = true;
    }

    /*!
     * \brief setThickness set how thick the function shall be displayed.
     * \param thickness in openGL scale
     */
    public void setThickness(float thickness){
        line_thickness = thickness;
        begin.setRadius(thickness/2f);
        end.setRadius(thickness/2f);
    }

    public Function getFunction(){
        return f;
    }

    public void setFunction(final Function f){
        this.f = f;
        needs_rendering = true;
    }

    Function f;
    // function coordinates
    double min_x;
    double max_x;

    int num_samples = DEFAULT_NUM_SAMPLES;
    float line_thickness = DEFAULT_LINE_THICKNESS;

    Homography2d f2openGL;

    DrawableCircle begin;
    DrawableCircle end;

    final static int DEFAULT_NUM_SAMPLES = 30;
    final static float DEFAULT_LINE_THICKNESS = 0.07f;
}
