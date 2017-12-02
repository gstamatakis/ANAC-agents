package Utils;

/**
 * Simulated annealing parameters
 */
public class SAparams {
    public double startTemperature;        // start temperature
    public double endTemperature;          // end temperature
    public double cool;                    // cooling degree
    public int stepNum;                    // number of times to change

    public SAparams() {
        startTemperature = 1.0;
        endTemperature = 0.0001;
        cool = 0.999;
        stepNum = 1;
    }

    public SAparams(double startTemp, double endTemp, double cool, int stepNum) {
        startTemperature = startTemp;
        endTemperature = endTemp;
        this.cool = cool;
        this.stepNum = stepNum;
    }
}