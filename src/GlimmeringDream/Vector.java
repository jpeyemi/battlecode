package GlimmeringDream;

public class Vector {
    double x;
    double y;
    
    Vector(double xComp, double yComp) {
        x = xComp;
        y = yComp;
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public double dot(Vector other) {
        return this.x * other.x + this.y + other.y;
    }

    public double getAngle(Vector other) {
        return Math.acos((this.dot(other) / (this.magnitude() * other.magnitude())));
    }
}
