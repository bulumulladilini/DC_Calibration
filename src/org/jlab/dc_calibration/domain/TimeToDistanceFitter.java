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
import static org.jlab.dc_calibration.domain.Constants.nSectors;
import static org.jlab.dc_calibration.domain.Constants.nTh;
import static org.jlab.dc_calibration.domain.Constants.nThBinsVz;
import static org.jlab.dc_calibration.domain.Constants.parName;
import static org.jlab.dc_calibration.domain.Constants.prevFitPars;
import static org.jlab.dc_calibration.domain.Constants.rad2deg;
import static org.jlab.dc_calibration.domain.Constants.thBins;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzH;
import static org.jlab.dc_calibration.domain.Constants.thEdgeVzL;
import static org.jlab.dc_calibration.domain.Constants.wpdist;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.freehep.math.minuit.FunctionMinimum;
import org.freehep.math.minuit.MnMigrad;
import org.freehep.math.minuit.MnStrategy;
import org.freehep.math.minuit.MnUserParameters;
import org.jlab.dc_calibration.NTuple.NTuple;
import org.jlab.groot.base.TStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataChain;
import org.jlab.io.evio.EvioDataEvent;

public class TimeToDistanceFitter implements ActionListener, Runnable {

	private EvioDataBank bnkHits;
	private EvioDataBank bnkSegs;
	private EvioDataBank bnkSegTrks;
	private EvioDataBank bnkTrks;
	private int nTrks;

	private Map<Coordinate, H1F> hArrWire = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> h1ThSL = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> h1timeSlTh = new HashMap<Coordinate, H1F>();
	// Histograms to get ineff. as fn of trkDoca (NtrkDoca = trkDoca/docaMax)
	private Map<Coordinate, H1F> h1trkDoca2Dar = new HashMap<Coordinate, H1F>(); // #############################################################
	private Map<Coordinate, H1F> h1NtrkDoca2Dar = new HashMap<Coordinate, H1F>();// [3] for all good hits, only bad (matchedHitID == -1) and ratio
	private Map<Coordinate, H1F> h1NtrkDocaP2Dar = new HashMap<Coordinate, H1F>();// ############################################################
	private Map<Coordinate, H1F> h1trkDoca3Dar = new HashMap<Coordinate, H1F>(); // ############################################################
	private Map<Coordinate, H1F> h1NtrkDoca3Dar = new HashMap<Coordinate, H1F>();// [3] for all good hits, only bad (matchedHitID == -1) and ratio
	private Map<Coordinate, H1F> h1NtrkDocaP3Dar = new HashMap<Coordinate, H1F>();// ############################################################
	private Map<Coordinate, H1F> h1trkDoca4Dar = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> h1wire4Dar = new HashMap<Coordinate, H1F>();// no ratio here
	private Map<Coordinate, H1F> h1avgWire4Dar = new HashMap<Coordinate, H1F>();// no ratio here
	private Map<Coordinate, H1F> h1fitChisqProbSeg4Dar = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H2F> h2timeVtrkDoca = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H2F> h2timeVtrkDocaVZ = new HashMap<Coordinate, H2F>();

	private Map<Integer, Integer> layerMapTBHits;
	private Map<Integer, Integer> wireMapTBHits;
	private Map<Integer, Double> timeMapTBHits;
	private Map<Integer, Double> trkDocaMapTBHits;
	private Map<Integer, Integer> gSegmThBinMapTBSegments;
	private Map<Integer, Double> gSegmAvgWireTBSegments;
	private Map<Integer, Double> gFitChisqProbTBSegments;

	private EmbeddedCanvas sector1;
	private EmbeddedCanvas sector2;
	private EmbeddedCanvas sector3;
	private EmbeddedCanvas sector4;
	private EmbeddedCanvas sector5;
	private EmbeddedCanvas sector6;

	private EmbeddedCanvas sector1Profiles;
	private EmbeddedCanvas sector2Profiles;
	private EmbeddedCanvas sector3Profiles;
	private EmbeddedCanvas sector4Profiles;
	private EmbeddedCanvas sector5Profiles;
	private EmbeddedCanvas sector6Profiles;

	private Map<Coordinate, GraphErrors> htime2DisDocaProfile = new HashMap<Coordinate, GraphErrors>();
	private Map<Coordinate, DCFitFunction> mapOfFitFunctions = new HashMap<Coordinate, DCFitFunction>();
	private Map<Coordinate, MnUserParameters> mapOfFitParameters = new HashMap<Coordinate, MnUserParameters>();
	private Map<Coordinate, double[]> mapOfUserFitParameters = new HashMap<Coordinate, double[]>();
	private Map<Coordinate, DCFitDrawer> mapOfFitLines = new HashMap<Coordinate, DCFitDrawer>();

	private H2F testHist;
	private boolean acceptorder = false;
	private boolean isLinearFit;

	private ArrayList<String> fileArray;
	private EvioDataChain reader;
	private OrderOfAction OAInstance;
	private DCTabbedPane dcTabbedPane;

	// MK testing
	private NTuple nTupletimeVtrkDocaVZ;
	double[] tupleVars;

	public TimeToDistanceFitter(ArrayList<String> files, boolean isLinearFit) {
		this.fileArray = files;
		this.reader = new EvioDataChain();
		this.dcTabbedPane = new DCTabbedPane("PooperDooper");
		this.isLinearFit = isLinearFit;
		this.nTupletimeVtrkDocaVZ = new NTuple("testData", "Sector:SuperLayer:ThetaBin:Doca:Time");
		this.tupleVars = new double[5];
		createHists();
	}

	public TimeToDistanceFitter(OrderOfAction OAInstance, ArrayList<String> files, boolean isLinearFit) {
		this.fileArray = files;
		this.OAInstance = OAInstance;
		this.reader = new EvioDataChain();
		this.dcTabbedPane = new DCTabbedPane("PooperDooper");
		this.isLinearFit = isLinearFit;
		createHists();
	}

	private void createHists() {
		testHist = new H2F("A test of superlayer6 at thetabin6", 200, 0.0, 1.0, 150, 0.0, 200.0);
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
		for (int i = 0; i < nSectors; i++) {
			for (int j = 0; j < nSL; j++) {
				for (int k = 0; k < nThBinsVz; k++) { // nThBinsVz theta bins +/-2
					// deg around 0, 10, 20, 30,
					// 40, and 50 degs
					hNm = String.format("Sector %d timeVtrkDocaS%dTh%02d", i, j, k);
					h2timeVtrkDocaVZ.put(new Coordinate(i, j, k), new H2F(hNm, 200, 0.0, 1.0, 150, 0.0, 200.0));

					hTtl = String.format("time vs. Doca (SL=%d, th(%2.1f,%2.1f))", j + 1, thEdgeVzL[k], thEdgeVzH[k]); // Worked
					h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).setTitle(hTtl);
					h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).setTitleX("Doca");
					h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).setTitleX("Time");

				}
			}
		}
	}

	private void createCanvas() {

		sector1 = new EmbeddedCanvas();
		sector2 = new EmbeddedCanvas();
		sector3 = new EmbeddedCanvas();
		sector4 = new EmbeddedCanvas();
		sector5 = new EmbeddedCanvas();
		sector6 = new EmbeddedCanvas();

		sector1Profiles = new EmbeddedCanvas();
		sector2Profiles = new EmbeddedCanvas();
		sector3Profiles = new EmbeddedCanvas();
		sector4Profiles = new EmbeddedCanvas();
		sector5Profiles = new EmbeddedCanvas();
		sector6Profiles = new EmbeddedCanvas();

		sector1.setSize(4 * 400, 6 * 400);
		sector1.divide(6, 6);
		sector2.setSize(4 * 400, 6 * 400);
		sector2.divide(6, 6);
		sector3.setSize(4 * 400, 6 * 400);
		sector3.divide(6, 6);
		sector4.setSize(4 * 400, 6 * 400);
		sector4.divide(6, 6);
		sector5.setSize(4 * 400, 6 * 400);
		sector5.divide(6, 6);
		sector6.setSize(4 * 400, 6 * 400);
		sector6.divide(6, 6);

		sector1Profiles.setSize(4 * 400, 6 * 400);
		sector1Profiles.divide(6, 6);
		sector2Profiles.setSize(4 * 400, 6 * 400);
		sector2Profiles.divide(6, 6);
		sector3Profiles.setSize(4 * 400, 6 * 400);
		sector3Profiles.divide(6, 6);
		sector4Profiles.setSize(4 * 400, 6 * 400);
		sector4Profiles.divide(6, 6);
		sector5Profiles.setSize(4 * 400, 6 * 400);
		sector5Profiles.divide(6, 6);
		sector6Profiles.setSize(4 * 400, 6 * 400);
		sector6Profiles.divide(6, 6);

	}

	protected void processData() {
		int counter = 0;
		int icounter = 0;
		for (String str : fileArray) {
			reader.addFile(str);
		}
		reader.open();
		while (reader.hasEvent()) {// && icounter < 100

			icounter++;
			if (icounter % 2000 == 0) {
				System.out.println("Processed " + icounter + " events.");
			}
			EvioDataEvent event = reader.getNextEvent();
			ProcessTBSegmentTrajectory tbSegmentTrajectory = new ProcessTBSegmentTrajectory(event);
			if (tbSegmentTrajectory.getNsegs() > 0) {
				counter++;
			}
			if (event.hasBank("TimeBasedTrkg::TBHits") && event.hasBank("TimeBasedTrkg::TBSegments")) {// && event.hasBank("TimeBasedTrkg::TBSegmentTrajectory") &&
			                                                                                           // event.hasBank("TimeBasedTrkg::TBTracks")
				ProcessTBTracks tbTracks = new ProcessTBTracks(event);
				if (tbTracks.getNTrks() > 0) {
					processTBhits(event);
					processTBSegments(event);
				}

			}

		}
		System.out.println(
		        "processed " + counter + " Events with TimeBasedTrkg::TBSegmentTrajectory entries from a total of " + icounter + " events");
		saveNtuple();
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
			int sector = bnkSegs.getInt("sector", j);
			gSegmAvgWireTBSegments.put(bnkSegs.getInt("ID", j), bnkSegs.getDouble("avgWire", j));
			gFitChisqProbTBSegments.put(bnkSegs.getInt("ID", j), bnkSegs.getDouble("fitChisqProb", j));

			double thDeg = rad2deg * Math.atan2(bnkSegs.getDouble("fitSlope", j), 1.0);
			h1ThSL.get(new Coordinate(bnkSegs.getInt("superlayer", j) - 1)).fill(thDeg);
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
			for (int h = 1; h <= 12; h++) {
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
					if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1 && thBnVz > -1 && thBnVz < nThBinsVz) {// && thBnVz < nThBinsVz
						double docaNorm = gTrkDoca / docaMax;
						h2timeVtrkDocaVZ.get(new Coordinate(sector - 1, superlayer - 1, thBnVz)).fill(Math.abs(docaNorm), gTime);
					}
					// here I will fill a test histogram of superlay6 and thetabin6
					if (bnkSegs.getInt("Hit" + h + "_ID", j) > -1 && thBnVz == 5 && superlayer == 6) {
						double docaNorm = gTrkDoca / docaMax;
						tupleVars[0] = (double) sector;
						tupleVars[1] = (double) superlayer;
						tupleVars[2] = (double) thBnVz;
						tupleVars[3] = Math.abs(docaNorm);
						tupleVars[4] = gTime;
						nTupletimeVtrkDocaVZ.addRow(tupleVars);
						testHist.fill(Math.abs(docaNorm), gTime);
					}

				}
			}
		}

	}

	protected void drawHistograms() {
		createCanvas();
		for (int i = 0; i < nSectors; i++) {
			for (int j = 0; j < nSL; j++) {
				for (int k = 0; k < nThBinsVz; k++) {
					htime2DisDocaProfile.put(new Coordinate(i, j, k), h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getProfileX());
					htime2DisDocaProfile.get(new Coordinate(i, j, k)).setTitle("Sector " + i + " timeVtrkDocaS " + j + " Th" + k);
				}
			}
		}

		// Lets Run the Fitter
		runFitter();
		// Done running fitter
		// lets create lines we just fit
		createFitLines();
		int canvasPlace = 0;
		for (int j = 0; j < nSL; j++) {
			for (int k = 0; k < nThBinsVz; k++) {
				sector1.cd(canvasPlace);
				sector2.cd(canvasPlace);
				sector3.cd(canvasPlace);
				sector4.cd(canvasPlace);
				sector5.cd(canvasPlace);
				sector6.cd(canvasPlace);

				sector1.draw(h2timeVtrkDocaVZ.get(new Coordinate(0, j, k)));
				// sector1.draw(mapOfFitLines.get(new Coordinate(0, j, k)), "same");
				sector2.draw(h2timeVtrkDocaVZ.get(new Coordinate(1, j, k)));
				// sector2.draw(mapOfFitLines.get(new Coordinate(1, j, k)), "same");
				sector3.draw(h2timeVtrkDocaVZ.get(new Coordinate(2, j, k)));
				// sector3.draw(mapOfFitLines.get(new Coordinate(2, j, k)), "same");
				sector4.draw(h2timeVtrkDocaVZ.get(new Coordinate(3, j, k)));
				// sector4.draw(mapOfFitLines.get(new Coordinate(3, j, k)), "same");
				sector5.draw(h2timeVtrkDocaVZ.get(new Coordinate(4, j, k)));
				// sector5.draw(mapOfFitLines.get(new Coordinate(5, j, k)), "same");
				sector6.draw(h2timeVtrkDocaVZ.get(new Coordinate(5, j, k)));
				// sector6.draw(mapOfFitLines.get(new Coordinate(5, j, k)), "same");

				sector1Profiles.cd(canvasPlace);
				sector2Profiles.cd(canvasPlace);
				sector3Profiles.cd(canvasPlace);
				sector4Profiles.cd(canvasPlace);
				sector5Profiles.cd(canvasPlace);
				sector6Profiles.cd(canvasPlace);

				sector1Profiles.draw(h2timeVtrkDocaVZ.get(new Coordinate(0, j, k)).getProfileX());
				sector1Profiles.draw(mapOfFitLines.get(new Coordinate(0, j, k)), "same");
				sector2Profiles.draw(h2timeVtrkDocaVZ.get(new Coordinate(1, j, k)).getProfileX());
				sector2Profiles.draw(mapOfFitLines.get(new Coordinate(1, j, k)), "same");
				sector3Profiles.draw(h2timeVtrkDocaVZ.get(new Coordinate(2, j, k)).getProfileX());
				sector3Profiles.draw(mapOfFitLines.get(new Coordinate(2, j, k)), "same");
				sector4Profiles.draw(h2timeVtrkDocaVZ.get(new Coordinate(3, j, k)).getProfileX());
				sector4Profiles.draw(mapOfFitLines.get(new Coordinate(3, j, k)), "same");
				sector5Profiles.draw(h2timeVtrkDocaVZ.get(new Coordinate(4, j, k)).getProfileX());
				sector5Profiles.draw(mapOfFitLines.get(new Coordinate(5, j, k)), "same");
				sector6Profiles.draw(h2timeVtrkDocaVZ.get(new Coordinate(5, j, k)).getProfileX());
				sector6Profiles.draw(mapOfFitLines.get(new Coordinate(5, j, k)), "same");

				canvasPlace++;

			}
		}
		sector1.save("src/images/sector1.png");
		sector2.save("src/images/sector2.png");
		sector3.save("src/images/sector3.png");
		sector4.save("src/images/sector4.png");
		sector5.save("src/images/sector5.png");
		sector6.save("src/images/sector6.png");

		sector1Profiles.save("src/images/sector1Profiles.png");
		sector2Profiles.save("src/images/sector2Profiles.png");
		sector3Profiles.save("src/images/sector3Profiles.png");
		sector4Profiles.save("src/images/sector4Profiles.png");
		sector5Profiles.save("src/images/sector5Profiles.png");
		sector6Profiles.save("src/images/sector6Profiles.png");
		// lets add the canvas's to the pane and draw it.
		addToPane();

		// this is temp for testHist
		EmbeddedCanvas test = new EmbeddedCanvas();
		test.cd(0);
		test.draw(testHist);
		test.save("src/images/test.png");
	}

	protected void addToPane() {
		dcTabbedPane.addCanvasToPane("Sector 1", sector1);
		dcTabbedPane.addCanvasToPane("Sector 2", sector2);
		dcTabbedPane.addCanvasToPane("Sector 3", sector3);
		dcTabbedPane.addCanvasToPane("Sector 4", sector4);
		dcTabbedPane.addCanvasToPane("Sector 5", sector5);
		dcTabbedPane.addCanvasToPane("Sector 6", sector6);

		dcTabbedPane.showFrame();

	}

	public void runFitter() {

		final int nFreePars = 4;

		// initial guess of tMax for the 6 superlayers (cell sizes are different for each)
		// This is one of the free parameters (par[2], but fixed for now.)
		double tMaxSL[] = { 155.0, 165.0, 300.0, 320.0, 525.0, 550.0 };

		// Now start minimization
		double parSteps[] = { 0.00001, 0.001, 0.01, 0.01, 0.0001 };
		double pLow[] = { prevFitPars[0] * 0.4, prevFitPars[1] * 0.0, prevFitPars[2] * 0.4, prevFitPars[3] * 0.4, prevFitPars[4] * 0.0 };
		double pHigh[] = { prevFitPars[0] * 1.6, prevFitPars[1] * 5.0, prevFitPars[2] * 1.6, prevFitPars[3] * 1.6, prevFitPars[4] * 1.6 };
		Map<Coordinate, MnUserParameters> mapTmpUserFitParameters = new HashMap<Coordinate, MnUserParameters>();

		for (int i = 0; i < nSectors; i++) {
			for (int j = 0; j < nSL; j++) {
				for (int k = 0; k < nThBinsVz; k++) {
					mapOfFitFunctions.put(new Coordinate(i, j, k),
					        new DCFitFunction(h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getProfileX(), j, k, isLinearFit));
					mapOfFitParameters.put(new Coordinate(i, j, k), new MnUserParameters());
					for (int p = 0; p < nFreePars; p++) {
						mapOfFitParameters.get(new Coordinate(i, j, k)).add(parName[p], prevFitPars[p], parSteps[p], pLow[p], pHigh[p]);
					}
					mapOfFitParameters.get(new Coordinate(i, j, k)).setValue(2, tMaxSL[j]);// tMax for SLth superlayer
					mapOfFitParameters.get(new Coordinate(i, j, k)).fix(2);
					MnMigrad migrad =
					        new MnMigrad(mapOfFitFunctions.get(new Coordinate(i, j, k)), mapOfFitParameters.get(new Coordinate(i, j, k)));
					FunctionMinimum min = migrad.minimize();

					if (!min.isValid()) {
						// try with higher strategy
						System.out.println("FM is invalid, try with strategy = 2.");
						MnMigrad migrad2 = new MnMigrad(mapOfFitFunctions.get(new Coordinate(i, j, k)), min.userState(), new MnStrategy(2));
						min = migrad2.minimize();
					}
					mapTmpUserFitParameters.put(new Coordinate(i, j, k), min.userParameters());
					double[] fPars = new double[nFreePars];
					double[] fErrs = new double[nFreePars];
					for (int p = 0; p < nFreePars; p++) {
						fPars[p] = mapTmpUserFitParameters.get(new Coordinate(i, j, k)).value(parName[p]);
						fErrs[p] = mapTmpUserFitParameters.get(new Coordinate(i, j, k)).error(parName[p]);
					}
					mapOfUserFitParameters.put(new Coordinate(i, j, k), fPars);
				} // end of nThBinsVz loop
			} // end of superlayer loop
		} // end of sector loop

	}

	private void createFitLines() {
		for (int i = 0; i < nSectors; i++) {
			for (int j = 0; j < nSL; j++) {
				for (int k = 0; k < nThBinsVz; k++) {
					String title = "Sector " + i + " timeVtrkDocaS " + j + " Th" + k;
					double maxFitValue = h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getDataX(getMaximumFitValue(i, j, k));
					mapOfFitLines.put(new Coordinate(i, j, k), new DCFitDrawer(title, 0.0, 1.0, j, k, isLinearFit));
					mapOfFitLines.get(new Coordinate(i, j, k)).setLineColor(2);
					mapOfFitLines.get(new Coordinate(i, j, k)).setLineWidth(3);
					mapOfFitLines.get(new Coordinate(i, j, k)).setLineStyle(4);
					mapOfFitLines.get(new Coordinate(i, j, k)).setParameters(mapOfUserFitParameters.get(new Coordinate(i, j, k)));
				}
			}
		}

	}

	public int getMaximumFitValue(int i, int j, int k) {
		int maxOutput = 0;
		int nX = h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getXAxis().getNBins();
		int nY = h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getYAxis().getNBins();
		double[][] mybuff = h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)).getContentBuffer();
		for (int iX = 0; iX < nX; iX++) {
			for (int iY = 0; iY < nY; iY++) {
				if (mybuff[iX][iY] != 0.0) {
					maxOutput = iX;
				}
			}
		}
		return maxOutput;

	}

	public void actionPerformed(ActionEvent e) {
		OAInstance.buttonstatus(e);
		acceptorder = OAInstance.isorderOk();
		JFrame frame = new JFrame("JOptionPane showMessageDialog example1");
		if (acceptorder) {
			JOptionPane.showMessageDialog(frame, "Click OK to start processing the time to distance fitting...");
			processData();
			drawHistograms();
			// DCTabbedPane test = new DCTabbedPane();
		} else
			System.out.println("I am red and it is not my turn now ;( ");
	}

	@Override
	public void run() {

		processData();
		drawHistograms();

	}

	private void saveNtuple() {
		nTupletimeVtrkDocaVZ.write("src/files/pionTest.evio");
	}

	public static void main(String[] args) {
		String fileName;
		String fileName2;

		fileName = "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.1.evio";
		// fileName2 =
		// "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.2.evio";
		ArrayList<String> fileArray = new ArrayList<String>();
		// fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/elec/cookedFiles/out_out_1.evio");
		// fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/elec/cookedFiles/out_out_10.evio");
		// fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/elec/cookedFiles/out_out_2.evio");
		// fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/elec/cookedFiles/out_out_3.evio");

		// fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_1.evio");
		// fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_10.evio");
		// fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_2.evio");
		// fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_4.evio");

		fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_1.evio");
		fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_10.evio");
		fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_2.evio");
		fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_4.evio");

		// fileArray.add(fileName);
		// fileArray.add(
		// "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.1.evio");
		// fileArray.add(
		// "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.3.evio");
		//
		// fileArray.add(
		// "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.4.evio");
		//
		// fileArray.add(
		// "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.5.evio");

		TimeToDistanceFitter rd = new TimeToDistanceFitter(fileArray, true);

		rd.processData();
		// System.out.println(rd.getMaximumFitValue(5, 5, 5) + " output");
		rd.drawHistograms();

	}
}
