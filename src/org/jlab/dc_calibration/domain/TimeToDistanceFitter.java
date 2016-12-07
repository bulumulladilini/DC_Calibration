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
 *  `------'				 @author KPAdhikari
*/
package org.jlab.dc_calibration.domain;

import static org.jlab.dc_calibration.domain.Constants.nHists;
import static org.jlab.dc_calibration.domain.Constants.nLayer;
import static org.jlab.dc_calibration.domain.Constants.nSL;
import static org.jlab.dc_calibration.domain.Constants.nTh;
import static org.jlab.dc_calibration.domain.Constants.nThBinsVz;
import static org.jlab.dc_calibration.domain.Constants.rad2deg;
import static org.jlab.dc_calibration.domain.Constants.thBins;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzH;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzL;
import static org.jlab.dc_calibration.domain.Constants.wpdist;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.freehep.math.minuit.FunctionMinimum;
import org.freehep.math.minuit.MnMigrad;
import org.freehep.math.minuit.MnStrategy;
import org.freehep.math.minuit.MnUserParameters;
import org.jlab.groot.base.TStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataChain;
import org.jlab.io.evio.EvioDataEvent;

public class TimeToDistanceFitter implements ActionListener, Runnable {
	String file;
	EvioDataChain reader = null;
	EvioDataBank bnkHits = null;
	EvioDataBank bnkClust = null;
	EvioDataBank bnkSegs = null;
	EvioDataBank bnkSegTrks = null;
	EvioDataBank bnkCross = null;

	private Map<Coordinate, H1F> hArrWire = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> h1ThSL = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> h1timeSlTh = new HashMap<Coordinate, H1F>();
	// Histograms to get ineff. as fn of trkDoca (NtrkDoca = trkDoca/docaMax)
	private Map<Coordinate, H1F> h1trkDoca2Dar = new HashMap<Coordinate, H1F>(); // #############################################################
	private Map<Coordinate, H1F> h1NtrkDoca2Dar = new HashMap<Coordinate, H1F>();// [3]
	                                                                             // for
	                                                                             // all
	                                                                             // good
	                                                                             // hits,
	                                                                             // only
	                                                                             // bad
	                                                                             // (matchedHitID==
	                                                                             // -1)
	                                                                             // and
	                                                                             // ratio
	private Map<Coordinate, H1F> h1NtrkDocaP2Dar = new HashMap<Coordinate, H1F>();// ############################################################

	private Map<Coordinate, H1F> h1trkDoca3Dar = new HashMap<Coordinate, H1F>(); // ############################################################
	private Map<Coordinate, H1F> h1NtrkDoca3Dar = new HashMap<Coordinate, H1F>();// [3]
	                                                                             // for
	                                                                             // all
	                                                                             // good
	                                                                             // hits,
	                                                                             // only
	                                                                             // bad
	                                                                             // (matchedHitID==
	                                                                             // -1)
	                                                                             // and
	                                                                             // ratio
	private Map<Coordinate, H1F> h1NtrkDocaP3Dar = new HashMap<Coordinate, H1F>();// ############################################################

	private Map<Coordinate, H1F> h1trkDoca4Dar = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> h1wire4Dar = new HashMap<Coordinate, H1F>();// no
	                                                                         // ratio
	                                                                         // here
	private Map<Coordinate, H1F> h1avgWire4Dar = new HashMap<Coordinate, H1F>();// no
	                                                                            // ratio
	                                                                            // here

	private Map<Coordinate, H1F> h1fitChisqProbSeg4Dar = new HashMap<Coordinate, H1F>();

	private Map<Coordinate, H2F> h2timeVtrkDoca = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H2F> h2timeVtrkDocaVZ = new HashMap<Coordinate, H2F>();

	private Map<Integer, Integer> layerMapTBHits = null;
	private Map<Integer, Integer> wireMapTBHits = null;
	private Map<Integer, Double> timeMapTBHits = null;
	private Map<Integer, Double> trkDocaMapTBHits = null;

	private Map<Integer, Integer> gSegmThBinMapTBSegments = null;
	private Map<Integer, Double> gSegmAvgWireTBSegments = null;
	private Map<Integer, Double> gFitChisqProbTBSegments = null;

	public TimeToDistanceFitter(String file) {
		this.file = file;
		this.reader = new EvioDataChain();
		createHists();
	}

	private void createHists() {
		TStyle.createAttributes();
		String hNm = "";
		String hTtl = "";
		for (int i = 0; i < nSL; i++) {
			for (int j = 0; j < nLayer; j++) {
				for (int k = 0; k < nHists; k++) {
					hNm = String.format("wireS%dL%dDb%02d", i + 1, j + 1, k);
					hArrWire.put(new Coordinate(i, j, k), new H1F(hNm, 120, -1.0, 119.0));
					hTtl = String.format("wire (SL=%d, Layer%d, DocaBin=%02d)", i + 1, j + 1, k);
					hArrWire.get(new Coordinate(i, j, k)).setTitleX(hTtl);
					hArrWire.get(new Coordinate(i, j, k)).setLineColor(i + 1);
				}
			}
		}
		for (int i = 0; i < nSL; i++) {
			hNm = String.format("thetaSL%d", i + 1);
			hTtl = "#theta";
			h1ThSL.put(new Coordinate(i), new H1F(hNm, 120, -60.0, 60.0));
			h1ThSL.get(new Coordinate(i)).setTitle(hTtl);
			h1ThSL.get(new Coordinate(0)).setLineColor(i + 1);
		}

		for (int i = 0; i < nSL; i++) {
			for (int k = 0; k < nTh; k++) {
				hNm = String.format("timeSL%dThBn%d", i, k);
				h1timeSlTh.put(new Coordinate(i, k), new H1F(hNm, 200, -10.0, 190.0));
				hTtl = String.format("time (SL=%d, th(%.1f,%.1f)", i + 1, thBins[k], thBins[k + 1]);
				h1timeSlTh.get(new Coordinate(i, k)).setTitleX(hTtl);
				h1timeSlTh.get(new Coordinate(i, k)).setLineColor(i + 1);
			}
		}

		String[] hType = { "all hits", "matchedHitID==-1", "Ratio==Ineff." };// as
		                                                                     // String[];

		for (int i = 0; i < nSL; i++) {
			for (int k = 0; k < 3; k++) { // These are for histos integrated
			                              // over all layers
				hNm = String.format("trkDocaS%dH%d", i + 1, k);
				h1trkDoca2Dar.put(new Coordinate(i, k), new H1F(hNm, 90, -0.9, 0.9));
				hNm = String.format("NtrkDocaS%dH%d", i + 1, k);
				h1NtrkDoca2Dar.put(new Coordinate(i, k), new H1F(hNm, 120, -1.2, 1.2));
				hNm = String.format("NtrkDocaPS%dH%d", i + 1, k);
				h1NtrkDocaP2Dar.put(new Coordinate(i, k), new H1F(hNm, 120, 0.0, 1.2));

				if (k == 0)
					hTtl = String.format("all hits (SL=%d)", i + 1);
				if (k == 1)
					hTtl = String.format("matchedHitID==-1 (SL=%d)", i + 1);
				if (k == 2)
					hTtl = String.format("Ineff. (SL=%d)", i + 1);
				h1trkDoca2Dar.get(new Coordinate(i, k)).setTitle(hTtl);
				h1NtrkDoca2Dar.get(new Coordinate(i, k)).setTitle(hTtl);
				h1NtrkDocaP2Dar.get(new Coordinate(i, k)).setTitle(hTtl);

				h1trkDoca2Dar.get(new Coordinate(i, k)).setLineColor(i + 1);
				h1NtrkDoca2Dar.get(new Coordinate(i, k)).setLineColor(i + 1);
				h1NtrkDocaP2Dar.get(new Coordinate(i, k)).setLineColor(i + 1);

			}
			for (int j = 0; j < nLayer; j++) {
				for (int k = 0; k < 3; k++) { // These are for histos integrated
				                              // over all theta

					hNm = String.format("trkDocaS%dL%dH%d", i + 1, j + 1, k);
					h1trkDoca3Dar.put(new Coordinate(i, j, k), new H1F(hNm, 90, -0.9, 0.9));

					hNm = String.format("NtrkDocaS%dL%dH%d", i + 1, j + 1, k);
					h1NtrkDoca3Dar.put(new Coordinate(i, j, k), new H1F(hNm, 120, -1.2, 1.2));

					hNm = String.format("NtrkDocaPS%dL%dH%d", i + 1, j + 1, k);
					h1NtrkDocaP3Dar.put(new Coordinate(i, j, k), new H1F(hNm, 120, 0.0, 1.2));

					if (k == 0)
						hTtl = String.format("all hits (SL=%d, Layer%d)", i + 1, j + 1);
					if (k == 1)
						hTtl = String.format("matchedHitID==-1 (SL=%d, Layer%d)", i + 1, j + 1);
					if (k == 2)
						hTtl = String.format("Ineff. (SL=%d, Layer%d)", i + 1, j + 1);

					h1trkDoca3Dar.get(new Coordinate(i, j, k)).setTitle(hTtl);
					h1NtrkDoca3Dar.get(new Coordinate(i, j, k)).setTitle(hTtl);
					h1NtrkDocaP3Dar.get(new Coordinate(i, j, k)).setTitle(hTtl);

					h1trkDoca3Dar.get(new Coordinate(i, j, k)).setLineColor(i + 1);
					h1NtrkDoca3Dar.get(new Coordinate(i, j, k)).setLineColor(i + 1);
					h1NtrkDocaP3Dar.get(new Coordinate(i, j, k)).setLineColor(i + 1);

				}

				for (int th = 0; th < nTh; th++) {
					for (int k = 0; k < 3; k++) {
						hNm = String.format("trkDocaS%dL%dTh%02dH%d", i + 1, j + 1, th, k);
						h1trkDoca4Dar.put(new Coordinate(i, j, th, k), new H1F(hNm, 90, -0.9, 0.9));

						if (k == 0)
							hTtl = String.format("all hits (SL=%d, Layer%d, th(%.1f,%.1f))", i + 1, j + 1, thBins[th], thBins[th + 1]);
						if (k == 1)
							hTtl = String.format("matchedHitID==-1 (SL=%d, Layer%d, th(%.1f,%.1f))", i + 1, j + 1, thBins[th],
							        thBins[th + 1]);
						if (k == 2)
							hTtl = String.format("Ineff. (SL=%d, Layer%d, th(%.1f,%.1f))", i + 1, j + 1, thBins[th], thBins[th + 1]);
						h1trkDoca3Dar.get(new Coordinate(i, j, k)).setTitle(hTtl);
						h1trkDoca3Dar.get(new Coordinate(i, j, k)).setLineColor(i + 1);

					}
					for (int k = 0; k < 2; k++) {
						hNm = String.format("wireS%dL%dTh%02dH%d", i + 1, j + 1, th, k);
						h1wire4Dar.put(new Coordinate(i, j, th, k), new H1F(hNm, 120, -1.0, 119.0));

						hTtl = String.format("wire # for %s (SL=%d, Lay%d, th(%.1f,%.1f))", hType[k], i + 1, j + 1, thBins[th],
						        thBins[th + 1]);
						h1wire4Dar.get(new Coordinate(i, j, th, k)).setTitle(hTtl);
						h1wire4Dar.get(new Coordinate(i, j, th, k)).setLineColor(i + 1);

						hNm = String.format("avgWireS%dL%dTh%02dH%d", i + 1, j + 1, th, k);
						h1avgWire4Dar.put(new Coordinate(i, j, th, k), new H1F(hNm, 120, -1.0, 119.0));

						hTtl = String.format("avgWire(SegBnk) for %s (SL=%d, Lay%d, th(%.1f,%.1f))", hType[k], i + 1, j + 1, thBins[th],
						        thBins[th + 1]);
						h1avgWire4Dar.get(new Coordinate(i, j, th, k)).setTitle(hTtl);
						h1avgWire4Dar.get(new Coordinate(i, j, th, k)).setLineColor(i + 1);

						hNm = String.format("fitChisqProbS%dL%dTh%02dH%d", i + 1, j + 1, th, k);
						h1fitChisqProbSeg4Dar.put(new Coordinate(i, j, th, k), new H1F(hNm, 90, -0.1, 0.1));

						hTtl = String.format("fitChisqProbSeg(SegBnk) for %s (SL=%d, Lay%d, th(%.1f,%.1f))", hType[k], i + 1, j + 1,
						        thBins[th], thBins[th + 1]);
						h1fitChisqProbSeg4Dar.get(new Coordinate(i, j, th, k)).setTitle(hTtl);
						h1fitChisqProbSeg4Dar.get(new Coordinate(i, j, th, k)).setLineColor(i + 1);

					}
				}
			}
		}
		int[] thetaBins = { 0, 30 };// as int[];
		for (int i = 0; i < nSL; i++) {
			for (int j = 0; j < 2; j++) { // 2 theta bins +/-1 deg around 0 and
			                              // 30 deg
				hNm = String.format("timeVtrkDocaS%dTh%02d", i, j);
				h2timeVtrkDoca.put(new Coordinate(i, j), new H2F(hNm, 200, 0.0, 1.0, 150, 0.0, 200.0));

				hTtl = String.format("time vs |trkDoca| (SL=%d, th=%02d+/-1.0)", i + 1, thetaBins[j]);
				h2timeVtrkDoca.get(new Coordinate(i, j)).setTitle(hTtl);
			}
		}

		for (int i = 0; i < nSL; i++) {
			for (int j = 0; j < nThBinsVz; j++) { // nThBinsVz theta bins +/-2
				// deg around 0, 10, 20, 30,
				// 40, and 50 degs
				hNm = String.format("timeVtrkDocaS%dTh%02d", i, j);
				h2timeVtrkDocaVZ.put(new Coordinate(i, j), new H2F(hNm, 200, 0.0, 1.0, 150, 0.0, 200.0));

				hTtl = String.format("time vs |trkDoca| (SL=%d, th(%2.1f,%2.1f))", i + 1, thEdgeVzL[j], thEdgeVzH[j]); // Worked
				h2timeVtrkDocaVZ.get(new Coordinate(i, j)).setTitle(hTtl);
			}
		}
	}

	public void processData() {
		reader.addFile(this.file);
		reader.open();
		while (reader.hasEvent()) {// && counter < 100
			EvioDataEvent event = reader.getNextEvent();
			if (event.hasBank("TimeBasedTrkg::TBHits") && event.hasBank("TimeBasedTrkg::TBSegments")
			        && event.hasBank("TimeBasedTrkg::TBSegmentTrajectory")) {
				processTBhits(event);
				processTBSegments(event);
				processTBSegmentTrajectory(event);
			}
		}
	}

	private void processTBhits(EvioDataEvent event) {
		layerMapTBHits = new HashMap<Integer, Integer>();
		wireMapTBHits = new HashMap<Integer, Integer>();
		timeMapTBHits = new HashMap<Integer, Double>();
		trkDocaMapTBHits = new HashMap<Integer, Double>();

		bnkHits = (EvioDataBank) event.getBank("TimeBasedTrkg::TBHits");
		for (int j = 0; j < bnkHits.rows(); j++) {
			layerMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getInt("layer", j));
			wireMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getInt("wire", j));
			timeMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getDouble("time", j));
			trkDocaMapTBHits.put(bnkHits.getInt("id", j), bnkHits.getDouble("trkDoca", j));
			int docaBin = (int) ((bnkHits.getDouble("trkDoca", j) - (-0.8)) / 0.2);
			if (bnkHits.getInt("sector", j) == 1 && (docaBin > -1 && docaBin < 8)) {
				hArrWire.get(new Coordinate(bnkHits.getInt("superlayer", j) - 1, bnkHits.getInt("layer", j) - 1, docaBin))
				        .fill(bnkHits.getInt("wire", j));
			}
		}
	}

	private void processTBSegments(EvioDataEvent event) {

		gSegmThBinMapTBSegments = new HashMap<Integer, Integer>();
		gSegmAvgWireTBSegments = new HashMap<Integer, Double>();
		gFitChisqProbTBSegments = new HashMap<Integer, Double>();

		bnkSegs = (EvioDataBank) event.getBank("TimeBasedTrkg::TBSegments");
		int nHitsInSeg = 0;
		for (int j = 0; j < bnkSegs.rows(); j++) {
			int superlayer = bnkSegs.getInt("superlayer", j);
			gSegmAvgWireTBSegments.put(bnkSegs.getInt("ID", j), bnkSegs.getDouble("avgWire", j));
			gFitChisqProbTBSegments.put(bnkSegs.getInt("ID", j), bnkSegs.getDouble("fitChisqProb", j));

			double thDeg = rad2deg * Math.atan2(bnkSegs.getDouble("fitSlope", j), 1.0);
			h1ThSL.get(new Coordinate(bnkHits.getInt("superlayer", j) - 1)).fill(thDeg);
			for (int h = 1; h <= 12; h++) {
				if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1)
					nHitsInSeg++;
			}
			int thBn = -1;
			int thBnVz = -1;
			for (int th = 0; th < nTh; th++) {
				if (thDeg > thBins[th] && thDeg <= thBins[th + 1])
					thBn = th;
			}
			for (int th = 0; th < nThBinsVz; th++) {
				if (thDeg > thEdgeVzL[th] && thDeg <= thEdgeVzH[th])
					thBnVz = th;
			}
			gSegmThBinMapTBSegments.put(bnkSegs.getInt("ID", j), thBn);
			double thTmp1 = thDeg;
			double thTmp2 = thDeg - 30.0;
			double docaMax = 2.0 * wpdist[superlayer - 1];
			for (int h = 0; h < 12; h++) {
				if (nHitsInSeg > 5)// Saving only those with more than 5 hits
				{
					Double gTime = timeMapTBHits.get(new Integer(bnkSegs.getInt("Hit" + h + "_ID", j)));
					Double gTrkDoca = trkDocaMapTBHits.get(new Integer(bnkSegs.getInt("Hit" + h + "_ID", j)));
					if (gTime == null || gTrkDoca == null)
						continue;
					if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1 && thBn > -1 && thBn < nTh)
						h1timeSlTh.get(new Coordinate(superlayer - 1, thBn)).fill(gTime);
					if (Math.abs(thTmp1) < 1.0 && bnkSegs.getInt("Hit" + h + "_ID", j) > -1)
						h2timeVtrkDoca.get(new Coordinate(superlayer - 1, 0)).fill(Math.abs(gTrkDoca), gTime);
					if (Math.abs(thTmp2) < 1.0 && bnkSegs.getInt("Hit" + h + "_ID", j) > -1)
						h2timeVtrkDoca.get(new Coordinate(superlayer - 1, 1)).fill(Math.abs(gTrkDoca), gTime);
					if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1 && thBnVz > -1 && thBnVz < nThBinsVz) {
						double docaNorm = gTrkDoca / docaMax;
						h2timeVtrkDocaVZ.get(new Coordinate(superlayer - 1, thBnVz)).fill(Math.abs(docaNorm), gTime);
					}
				}
			}
		}

	}

	private void processTBSegmentTrajectory(EvioDataEvent event) {
		bnkSegTrks = (EvioDataBank) event.getBank("TimeBasedTrkg::TBSegmentTrajectory");
		for (int i = 0; i < bnkSegTrks.rows(); i++) {
			// First getting all the values of each variables of the current
			// bank
			int superlayer = bnkSegTrks.getInt("superlayer", i);
			int layer = bnkSegTrks.getInt("layer", i);
			int segmentID = bnkSegTrks.getInt("segmentID", i);
			int matchedHitID = bnkSegTrks.getInt("matchedHitID", i);
			double trkDoca = bnkSegTrks.getDouble("trkDoca", i);
			double dMax = wpdist[superlayer - 1];
			double NtrkDoca = trkDoca / dMax; // 6/12/16
			Integer gWire = wireMapTBHits.get(new Integer(matchedHitID));
			Double gSegmAvgWire = gSegmAvgWireTBSegments.get(new Integer(matchedHitID));
			Double gFitChisqProb = gFitChisqProbTBSegments.get(new Integer(matchedHitID));

			if (gWire == null || gSegmAvgWire == null || gFitChisqProb == null)
				continue;
			h1trkDoca2Dar.get(new Coordinate(superlayer - 1, 0)).fill(trkDoca);
			h1NtrkDoca2Dar.get(new Coordinate(superlayer - 1, 0)).fill(NtrkDoca);
			h1NtrkDocaP2Dar.get(new Coordinate(superlayer - 1, 0)).fill(Math.abs(NtrkDoca));

			if (matchedHitID == -1) {
				h1trkDoca2Dar.get(new Coordinate(superlayer - 1, 1)).fill(trkDoca);
				h1NtrkDoca2Dar.get(new Coordinate(superlayer - 1, 1)).fill(NtrkDoca);
				h1NtrkDocaP2Dar.get(new Coordinate(superlayer - 1, 1)).fill(Math.abs(NtrkDoca));
			}
			if (matchedHitID == -1) {
				h1trkDoca2Dar.get(new Coordinate(superlayer - 1, 2)).fill(trkDoca);
				h1NtrkDoca2Dar.get(new Coordinate(superlayer - 1, 2)).fill(NtrkDoca);
				h1NtrkDocaP2Dar.get(new Coordinate(superlayer - 1, 2)).fill(Math.abs(NtrkDoca));
			}
			h1trkDoca3Dar.get(new Coordinate(superlayer - 1, layer - 1, 0)).fill(trkDoca);
			h1NtrkDoca3Dar.get(new Coordinate(superlayer - 1, layer - 1, 0)).fill(NtrkDoca);
			h1NtrkDocaP3Dar.get(new Coordinate(superlayer - 1, layer - 1, 0)).fill(Math.abs(NtrkDoca));

			if (matchedHitID == -1) {
				h1trkDoca3Dar.get(new Coordinate(superlayer - 1, layer - 1, 1)).fill(trkDoca);
				h1NtrkDoca3Dar.get(new Coordinate(superlayer - 1, layer - 1, 1)).fill(NtrkDoca);
				h1NtrkDocaP3Dar.get(new Coordinate(superlayer - 1, layer - 1, 1)).fill(Math.abs(NtrkDoca));
			}
			if (matchedHitID == -1) {
				h1trkDoca3Dar.get(new Coordinate(superlayer - 1, layer - 1, 2)).fill(trkDoca);
				h1NtrkDoca3Dar.get(new Coordinate(superlayer - 1, layer - 1, 2)).fill(NtrkDoca);
				h1NtrkDocaP3Dar.get(new Coordinate(superlayer - 1, layer - 1, 2)).fill(Math.abs(NtrkDoca));
			}
			int gSegmThBin = gSegmThBinMapTBSegments.get(new Integer(segmentID));

			if (segmentID > -1 && gSegmThBin > -1) {
				h1trkDoca4Dar.get(new Coordinate(superlayer - 1, layer - 1, gSegmThBin, 0)).fill(trkDoca);
				if (matchedHitID == -1)
					h1trkDoca4Dar.get(new Coordinate(superlayer - 1, layer - 1, gSegmThBin, 1)).fill(trkDoca);
				if (matchedHitID == -1)
					h1trkDoca4Dar.get(new Coordinate(superlayer - 1, layer - 1, gSegmThBin, 2)).fill(trkDoca);
				if (matchedHitID > -1)
					h1wire4Dar.get(new Coordinate(superlayer - 1, layer - 1, gSegmThBin, 0)).fill(gWire);
				h1avgWire4Dar.get(new Coordinate(superlayer - 1, layer - 1, gSegmThBin, 0)).fill(gSegmAvgWire);
				h1fitChisqProbSeg4Dar.get(new Coordinate(superlayer - 1, layer - 1, gSegmThBin, 0)).fill(gFitChisqProb);
				if (matchedHitID == -1) {
					h1avgWire4Dar.get(new Coordinate(superlayer - 1, layer - 1, gSegmThBin, 1)).fill(gSegmAvgWire);
					h1fitChisqProbSeg4Dar.get(new Coordinate(superlayer - 1, layer - 1, gSegmThBin, 1)).fill(gFitChisqProb);
				}
			}
		}
	}

	public void drawHistograms() {
		final int nSupLayers = 2;
		final int nFreePars = 5;
		final String parName[] = { "v0", "deltamn", "tmax1", "tmax2", "distbeta" };
		final double prevFitPars[] = { 62.92e-04, 1.35, 137.67, 148.02, 0.055 };
		final double[] pars4FitLine = { prevFitPars[0], prevFitPars[1], prevFitPars[2], prevFitPars[3], prevFitPars[4], 1.0, 0.0, 0.3861 };
		String imgNm;
		EmbeddedCanvas c0 = new EmbeddedCanvas();
		c0.setSize(4 * 400, 3 * 400);
		c0.divide(4, 3);
		GraphErrors[][] profileX = new GraphErrors[nSL][2]; // 2 for 2 theta
		                                                    // bins 0, 30
		                                                    // //h2.getProfileX();
		GraphErrors[][] profileY = new GraphErrors[nSL][2]; // 2 for 2 theta
		                                                    // bins 0, 30
		                                                    // //h2.getProfileX();
		// GraphErrors profileY = h2.getProfileY();
		for (int i = 0; i < nSL; i++) {
			for (int j = 0; j < 2; j++) { // 2 thet bins +/-1 deg around 0 and
			                              // 30 deg
				profileX[i][j] = h2timeVtrkDoca.get(new Coordinate(i, j)).getProfileX();
				profileY[i][j] = h2timeVtrkDoca.get(new Coordinate(i, j)).getProfileY();
				c0.cd(i * 2 + j);
				c0.draw(h2timeVtrkDoca.get(new Coordinate(i, j))); // c0.draw(profileX[i][j],"same");
				c0.cd(i * 2 + j + 4);
				c0.draw(profileX[i][j]); // c0.draw(h2timeVtrkDoca[i][j]);
				c0.cd(i * 2 + j + 8);
				c0.draw(profileY[i][j]); // c0.draw(h2timeVtrkDoca[i][j]);
			}
		}
		imgNm = "src/images/timeVsTrkDoca_and_Profiles.png";
		c0.save(imgNm);

		EmbeddedCanvas c01 = new EmbeddedCanvas();
		c01.setSize(3 * 400, 2 * 400);
		c01.divide(1, 2);
		c01.cd(0);
		c01.draw(h2timeVtrkDoca.get(new Coordinate(0, 0)));
		c01.cd(1);
		c01.draw(profileX[0][0]);
		imgNm = "src/images/timeVsTrkDoca_and_Profiles2.png";
		c01.save(imgNm);

		EmbeddedCanvas c03 = new EmbeddedCanvas();
		c03.setSize(4 * 400, nThBinsVz * 400);
		c03.divide(4, nThBinsVz);

		GraphErrors[][] profileXvz = new GraphErrors[nSL][nThBinsVz];
		for (int i = 0; i < nSL; i++) {
			for (int j = 0; j < nThBinsVz; j++) {
				profileXvz[i][j] = h2timeVtrkDocaVZ.get(new Coordinate(i, j)).getProfileX();
			}
		}
		// Now start minimization
		KrishnaFcn theFCN = new KrishnaFcn(nSupLayers, nThBinsVz, profileXvz);
		MnUserParameters upar = new MnUserParameters();
		double parSteps[] = { 0.00001, 0.001, 0.01, 0.01, 0.0001 };
		double pLow[] = { prevFitPars[0] * 0.4, prevFitPars[1] * 0.0, prevFitPars[2] * 0.4, prevFitPars[3] * 0.4, prevFitPars[4] * 0.0 };
		double pHigh[] = { prevFitPars[0] * 1.6, prevFitPars[1] * 5.0, prevFitPars[2] * 1.6, prevFitPars[3] * 1.6, prevFitPars[4] * 1.6 };
		for (int p = 0; p < nFreePars; p++) {
			upar.add(parName[p], prevFitPars[p], parSteps[p], pLow[p], pHigh[p]);
		}

		upar.setValue(2, 155.0);
		upar.setValue(3, 165.0);
		upar.fix(2);
		upar.fix(3);
		System.out.println("Initial parameters: " + upar);

		System.out.println("start migrad");
		MnMigrad migrad = new MnMigrad(theFCN, upar);
		FunctionMinimum min = migrad.minimize();

		if (!min.isValid()) {
			// try with higher strategy
			System.out.println("FM is invalid, try with strategy = 2.");
			MnMigrad migrad2 = new MnMigrad(theFCN, min.userState(), new MnStrategy(2));
			min = migrad2.minimize();
		}

		MnUserParameters userpar = min.userParameters();
		for (int p = 0; p < nFreePars; p++)
			System.out.println(parName[p] + " = " + userpar.value(parName[p]) + " +/- " + userpar.error(parName[p]));
		double[] fPars = new double[nFreePars], fErrs = new double[nFreePars];

		for (int p = 0; p < nFreePars; p++) {
			fPars[p] = userpar.value(parName[p]);
			fErrs[p] = userpar.error(parName[p]);
		}
		for (int j = 0; j < nThBinsVz; j++) { // Row #
			c03.cd(j * 4 + 0);
			c03.draw(h2timeVtrkDocaVZ.get(new Coordinate(0, j))); // c0.draw(profileX[i][j],"same");
			c03.cd(j * 4 + 1);
			c03.draw(h2timeVtrkDocaVZ.get(new Coordinate(1, j))); // c0.draw(profileX[i][j],"same");
			c03.cd(j * 4 + 2);
			c03.draw(profileXvz[0][j]);
			c03.cd(j * 4 + 3);
			c03.draw(profileXvz[1][j]);
		}
		imgNm = "src/images/timeVsTrkDoca_and_ProfilesVZ.png";
		c03.save(imgNm);

		for (int i = 0; i < 5; i++)
			pars4FitLine[i] = fPars[i];
		pars4FitLine[5] = 1.0;
		pars4FitLine[6] = 0.0;
		pars4FitLine[7] = 0.3861;

		calibFnToDraw_withGROOT[][] myFitLinesGroot = new calibFnToDraw_withGROOT[2][nThBinsVz];
		for (int i = 0; i < nSL; i++) {
			for (int j = 0; j < nThBinsVz; j++) {
				String hNm = String.format("myFitLinesS%dTh%d", i + 1, j);
				System.out.println("debug10 ..");
				myFitLinesGroot[i][j] = new calibFnToDraw_withGROOT(hNm, 0.0, 1.0);
				myFitLinesGroot[i][j].setLineColor(3);
				myFitLinesGroot[i][j].setLineWidth(3);
				myFitLinesGroot[i][j].setLineStyle(4);
				pars4FitLine[5] = 1.0 * (i + 1);
				pars4FitLine[6] = 0.5 * (thEdgeVzL[j] + thEdgeVzH[j]);
				pars4FitLine[7] = 2.0 * wpdist[i];
				myFitLinesGroot[i][j].setParameters(pars4FitLine);
				System.out.println("Groot f(0/0.5/1.0) = " + myFitLinesGroot[i][j].evaluate(0.0) + ", "
				        + myFitLinesGroot[i][j].evaluate(0.5) + ", " + myFitLinesGroot[i][j].evaluate(1.0));

			}
		}
		EmbeddedCanvas c06 = new EmbeddedCanvas();
		c06.setSize(4 * 400, 6 * 400);
		c06.divide(4, 6);
		for (int j = 0; j < nThBinsVz; j++) {
			c06.cd(j * 4 + 0);
			c06.draw(h2timeVtrkDocaVZ.get(new Coordinate(0, j)));
			c06.draw(myFitLinesGroot[0][j], "same");
			c06.cd(j * 4 + 1);
			c06.draw(h2timeVtrkDocaVZ.get(new Coordinate(1, j)));
			c06.draw(myFitLinesGroot[1][j], "same");
			c06.cd(j * 4 + 2);
			c06.draw(profileXvz[0][j]);
			c06.draw(myFitLinesGroot[0][j], "same");
			c06.cd(j * 4 + 3);
			c06.draw(profileXvz[1][j]);
			c06.draw(myFitLinesGroot[1][j], "same");
		}
		imgNm = "src/images/myTestFitFunctionAllThBins_wdGroot.png";
		c06.save(imgNm);

		// 10/4/16: Trying to make plot of residuals for each superlayer
		H1F[] h1Residual = new H1F[nSL];
		for (int i = 0; i < nSL; i++) {
			String hNm = String.format("ResidualS%d", i);
			h1Residual[i] = new H1F(hNm, 200, -1.0, 1.0);
		}
		for (int i = 0; i < nSL; i++) {
			for (int j = 0; j < nThBinsVz; j++) {}
		}
	}

	public void actionPerformed(ActionEvent ev) {
		JFrame frame = new JFrame("JOptionPane showMessageDialog example1");
		JOptionPane.showMessageDialog(frame, "Click OK to start processing the time to distance fitting...");
		processData();

	}

	@Override
	public void run() {
		processData();
	}

	public static void main(String[] args) {
		String fileName;
		// fileName =
		// "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/reconstructedDataR128T0corT2DfromCCDBvarFit08.1.evio";
		fileName = "src/files/recOutfile.evio";
		TimeToDistanceFitter rd = new TimeToDistanceFitter(fileName);

		rd.processData();
		// rd.drawHistograms();

	}

}
