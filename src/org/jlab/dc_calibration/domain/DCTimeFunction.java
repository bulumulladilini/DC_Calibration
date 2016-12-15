/*  +__^_________,_________,_____,________^-.-------------------,
 *  | |||||||||   `--------'     |          |                   O
 *  `+-------------USMC----------^----------|___________________|
 *    `\_,---------,---------,--------------'
 *      / X MK X /'|       /'
 *     / X MK X /  `\    /'
 *    / X MK X /`-------'
 *   / X MK X /
 *  / X MK X /
 * (________(                @author m.c.kunkel
 *  `------'
*/
package org.jlab.dc_calibration.domain;

import static org.jlab.dc_calibration.domain.Constants.beta;
import static org.jlab.dc_calibration.domain.Constants.rad2deg;
import static org.jlab.dc_calibration.domain.Constants.wpdist;

public class DCTimeFunction {

	private int superlayer;
	private double thetaDeg;
	private double doca;
	private double[] par;

	public DCTimeFunction(int superlayer, double thetaDeg, double doca, double[] par) {
		this.superlayer = superlayer;
		this.thetaDeg = thetaDeg;
		this.doca = doca;
		this.par = par;
	}

	public double linearFit() {
		double dMax = 2 * wpdist[superlayer - 1];
		double x = doca * dMax;
		double v0Par = par[0];
		return x / v0Par;
	}

	public double nonLinearFit() {
		double dMax = 2 * wpdist[superlayer - 1];
		// constant to avoid repeated calc. (see above main())
		double x = doca * dMax;
		double v0Par = par[0];
		double deltanm = par[1];
		double tMax = par[2];
		double distbeta = par[3]; // 8/3/16: initial value given by Mac is 0.050 cm.
		// Assume a functional form (time =
		// First, calculate n
		double nPar = (1.0 + (deltanm - 1.0) * Math.pow(0.615, deltanm)) / (1.0 - Math.pow(0.615, deltanm));

		// now, calculate m
		double mPar = nPar + deltanm;// Actually it should be named deltamn
		                             // and should be + in between
		                             // //7/21/16
		// determine b from the constraint that the time = tmax at dist=dmax
		double b = (tMax - dMax / v0Par) / (1.0 - mPar / nPar);

		// determine a from the requirement that the derivative at
		// d=dmax equal the derivative at d=0
		double a = -b * mPar / nPar; // From one of the constraints
		double alpha = thetaDeg; // = 0.0; //Local angle in degrees.
		double cos30minusalpha = Math.cos((30. - alpha) / rad2deg); // =Math.cos(Math.toRadians(30.-alpha));
		double xhat = x / dMax;
		double dmaxalpha = dMax * cos30minusalpha;

		// now calculate the dist to time function for theta = 'alpha' deg.
		// Assume a functional form with the SAME POWERS N and M and
		// coefficient a but a new coefficient 'balpha' to replace b.
		// Calculate balpha from the constraint that the value
		// of the function at dmax*cos30minusalpha is equal to tmax

		// parameter balpha (function of the 30 degree paramters a,n,m)
		double balpha = (tMax - dmaxalpha / v0Par - a * Math.pow(cos30minusalpha, nPar)) / Math.pow(cos30minusalpha, mPar);

		// now calculate function
		double xhatPowN = Math.pow(xhat, nPar), xhatPowM = Math.pow(xhat, mPar);
		double term1 = x / v0Par, term2 = a * xhatPowN, term3 = balpha * xhatPowM;
		double calcTime = term1 + term2 + term3;

		// //where x is trkdoca
		double deltatime_beta = (Math.sqrt(x * x + Math.pow(distbeta * Math.pow(beta, 2), 2)) - x) / v0Par;
		calcTime = calcTime + deltatime_beta;

		return calcTime;
	}

}