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

import static org.jlab.dc_calibration.domain.Constants.nSL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jlab.groot.base.TStyle;
import org.jlab.groot.data.H1F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataChain;
import org.jlab.io.evio.EvioDataEvent;

public class TestFile {

	protected static Map<Coordinate, H1F> hArrWire = new HashMap<Coordinate, H1F>();
	private EmbeddedCanvas test;
	private EvioDataBank bnkHits;

	private ArrayList<String> fileArray;
	private EvioDataChain reader;
	protected Map<Integer, Integer> layerMapTBHits;
	protected Map<Integer, Integer> wireMapTBHits;
	protected Map<Integer, Double> timeMapTBHits;
	protected Map<Integer, Double> trkDocaMapTBHits;

	public TestFile(ArrayList<String> files) {
		this.fileArray = files;
		this.reader = new EvioDataChain();
		createHists();
	}

	private void createHists() {
		TStyle.createAttributes();
		String hNm = "";
		String hTtl = "";
		for (int i = 0; i < nSL; i++) {
			hNm = String.format("wireS%d", i + 1);
			hArrWire.put(new Coordinate(i), new H1F(hNm, 120, -1.0, 119.0));
			hTtl = String.format("wire (SL=%d)", i + 1);
			hArrWire.get(new Coordinate(i)).setTitleX(hTtl);
			hArrWire.get(new Coordinate(i)).setLineColor(i + 1);
		}
	}

	private void createCanvas() {
		test = new EmbeddedCanvas();
		test.setSize(4 * 400, 6 * 400);
		test.divide(2, 3);
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
			ProcessTBHits tbHits = new ProcessTBHits(event);
			ProcessTBTracks tbTracks = new ProcessTBTracks(event);

			if (event.hasBank("TimeBasedTrkg::TBHits") && event.hasBank("TimeBasedTrkg::TBSegments")) {// && event.hasBank("TimeBasedTrkg::TBSegmentTrajectory") &&

				if (tbTracks.getNTrks() > 0) {
					tbHits.processTBhits();
					// processTBhits(event);
				}
			}
		}
		System.out.println(
		        "processed " + counter + " Events with TimeBasedTrkg::TBSegmentTrajectory entries from a total of " + icounter + " events");
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
			if (docaBin > -1 && docaBin < 8) {
				hArrWire.get(new Coordinate(bnkHits.getInt("superlayer", j) - 1)).fill((float) bnkHits.getInt("wire", j));
			}
		}
	}

	protected void drawHistograms() {
		createCanvas();

		int canvasPlace = 0;
		for (int i = 0; i < nSL; i++) {

			test.cd(canvasPlace);
			test.draw(hArrWire.get(new Coordinate(i)));
			System.out.println("onn canvas " + canvasPlace);
			canvasPlace++;

		}

		test.save("src/images/test.png");

	}

	public static void main(String[] args) {

		ArrayList<String> fileArray = new ArrayList<String>();

		// fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_1.evio");
		// fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_10.evio");
		// fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_2.evio");
		// fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_4.evio");

		fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_1.evio");
		// fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_10.evio");
		// fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_2.evio");
		// fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_4.evio");

		TestFile rd = new TestFile(fileArray);
		rd.processData();
		rd.drawHistograms();

	}
}
