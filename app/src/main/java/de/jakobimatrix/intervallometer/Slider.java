package de.jakobimatrix.intervallometer;

import android.widget.SeekBar;

public class Slider {

    /*!
     * \brief Constructor using default min, max and step width.
     * \param SeekBar The stupid named slider from Android.
     */
    public Slider(SeekBar sb){
        seekbar = sb;
        seekbar.setMax((int) ((max - min)/stepsize));
    }

    /*!
     * \brief Constructor using default step width.
     * If you give me min > max you hurt my feelings and I throw an exception.
     * \param SeekBar The stupid named slider from Android.
     * \param max_ The max value of the slider.
     * \param min_ The min value of the slider.
     * \param step_size The resolution of the slider.
     */
    public Slider(SeekBar sb, double min_, double max_, double step_size){
        if(min_ > max_){
            throw new IllegalArgumentException( "Slider: min > max: " + min_ + " !< " + max_);
        }
        seekbar = sb;
        min = min_;
        max = max_;
        stepsize = step_size;
        seekbar.setMax((int) ((max - min)/stepsize));
    }

    /*!
     * \brief Constructor using default step width.
     *  If you give me min > max you hurt my feelings and I throw an exception.
     * \param SeekBar The stupid named slider from Android.
     * \param max_ The max value of the slider.
     * \param min_ The min value of the slider.
     * \param num_steps The resolution of the slider.
     */
    public Slider(SeekBar sb, double min_, double max_, int num_steps){
        if(min_ > max_){
            throw new IllegalArgumentException( "Slider: min > max: " + min_ + " !< " + max_);
        }
        seekbar = sb;
        min = min_;
        max = max_;
        stepsize = (max-min)/(double) num_steps;;
        seekbar.setMax((int) ((max - min)/stepsize));
    }

    /*!
     * \brief getValue returns the current value of the slider.
     * \return current slider value.
     */
    public double getValue(){
        return min + ( seekbar.getProgress() * stepsize);
    }

    /*!
     * \brief setValue Set the slider to a value.
     * I throw a fucking exception if you give me a value outside [min,max].
     * \return current slider value.
     */
    public void setValue(double val){
        if(val < min || val > max){
            throw new IllegalArgumentException( "Slider::setValue: Given Value is out of range: [" + min + " < " + val + " || " + val + " > " + max);
        }
        seekbar.setProgress((int) ((val - min)/stepsize));
    }

    public SeekBar seekbar;
    private double min = 0;
    private double max = 1;
    private double stepsize = 0.01;
}
