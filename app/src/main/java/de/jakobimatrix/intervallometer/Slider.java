package de.jakobimatrix.intervallometer;

import android.widget.SeekBar;

public class Slider {

    Slider(SeekBar sb){
        seekbar = sb;
        seekbar.setMax((int) ((max - min)/stepsize));
    }

    Slider(SeekBar sb, double min_, double max_){
        seekbar = sb;
        min = min_;
        max = max_;
        seekbar.setMax((int) ((max - min)/stepsize));
    }

    Slider(SeekBar sb, double min_, double max_, double step_size){
        seekbar = sb;
        min = min_;
        max = max_;
        stepsize = step_size;
        seekbar.setMax((int) ((max - min)/stepsize));
    }

    public double getValue(){
        return min + ( seekbar.getProgress() * stepsize);
    }

    public void setValue(double val){
        seekbar.setProgress((int) ((val - min)/stepsize));
    }

    public SeekBar seekbar;
    private double min = 0;
    private double max = 1;
    private double stepsize = 0.01;
}
