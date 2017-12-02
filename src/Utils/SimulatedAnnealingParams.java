package Utils;

/**
 * Simulated annealing parameters
 */
public class SimulatedAnnealingParams {
    private double startTemperature;        // start temperature
    private double endTemperature;          // end temperature
    private double cool;                    // cooling degree
    private int stepNum;                    // number of times to change

    public SimulatedAnnealingParams() {
        this.startTemperature = 1.0;
        this.endTemperature = 0.0001;
        this.cool = 0.999;
        this.stepNum = 1;
    }

    public SimulatedAnnealingParams(double startTemp, double endTemp, double cool, int stepNum) {
        this.startTemperature = startTemp;
        this.endTemperature = endTemp;
        this.cool = cool;
        this.stepNum = stepNum;
    }

    public double getStartTemperature() {
        return startTemperature;
    }

    public void setStartTemperature(double startTemperature) {
        this.startTemperature = startTemperature;
    }

    public double getEndTemperature() {
        return endTemperature;
    }

    public void setEndTemperature(double endTemperature) {
        this.endTemperature = endTemperature;
    }

    public double getCool() {
        return cool;
    }

    public void setCool(double cool) {
        this.cool = cool;
    }

    public int getStepNum() {
        return stepNum;
    }

    public void setStepNum(int stepNum) {
        this.stepNum = stepNum;
    }
}