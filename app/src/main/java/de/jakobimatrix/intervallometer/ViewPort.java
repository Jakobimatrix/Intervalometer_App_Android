package de.jakobimatrix.intervallometer;

// wrapper class Viewport spans a rectangle with bot left and top right point min/max
public class ViewPort {
    ViewPort(Pos3d min, Pos3d max){
        this.max = max;
        this.min = min;
    }

    public ViewPort(ViewPort screen) {
        this.max = screen.max;
        this.min = screen.min;
    }

    public static boolean equals(ViewPort a, ViewPort b) {
        return Pos3d.equals(a.min, b.min) && Pos3d.equals(a.max, b.max);
    }

    public static ViewPort Zero() {
        return new ViewPort(Pos3d.Zero(), Pos3d.Zero());
    }

    public boolean isWithin(ViewPort v){
        if(v.min.x < min.x || v.min.y < min.y || v.max.x > max.x || v.max.y > max.y){
            return false;
        }
        return true;
    }

    public void union(ViewPort v){
        min.x = Math.min(min.x, v.min.x);
        min.y = Math.min(min.y, v.min.y);
        max.x = Math.max(max.x, v.max.x);
        max.y = Math.max(max.y, v.max.y);
    }

    public double width(){
        return max.x - min.x;
    }

    public double widthAbs(){
        return Math.abs(max.x - min.x);
    }

    public double height(){
        return max.y - min.y;
    }

    public double heightAbs(){
        return Math.abs(max.y - min.y);
    }

    public Pos3d diag(){
        return Pos3d.sub(max,min);
    }

    public String toString(){
        return "[min: " + min.toString() + " | max: " + max.toString() + "]";
    }

    public Pos3d min;
    public Pos3d max;
}
