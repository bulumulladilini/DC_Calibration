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

import static org.jlab.dc_calibration.domain.Constants.nHists;
import static org.jlab.dc_calibration.domain.Constants.nLayer;
import static org.jlab.dc_calibration.domain.Constants.nSL;
import static org.jlab.dc_calibration.domain.Constants.nSectors;
import static org.jlab.dc_calibration.domain.Constants.nThBinsVz;
import static org.jlab.dc_calibration.domain.Histograms.h2timeVtrkDocaVZ;
//import static org.jlab.dc_calibration.domain.Histograms.*;
//import static org.jlab.dc_calibration.domain.Constants.*;
import static org.jlab.dc_calibration.domain.Histograms.hArrWire;
import static org.jlab.dc_calibration.domain.Histograms.htrkDoca;

import java.util.ArrayList;

import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.evio.EvioDataChain;
import org.jlab.io.evio.EvioDataEvent;

public class TestFile {

	private ArrayList<EmbeddedCanvas> wires;
	private ArrayList<EmbeddedCanvas> trkDocas;
	private ArrayList<EmbeddedCanvas> trkDocasvsTime;
	private ArrayList<String> fileArray;

	private EvioDataChain reader;
	private InitializeHistograms initializeHistograms = new InitializeHistograms();

	public TestFile(ArrayList<String> files) {
		this.fileArray = files;
		this.reader = new EvioDataChain();
		addToReader();
		initializeHistograms.initTBhists();
		initializeHistograms.initTBSegments();
	}

	private void createCanvas() {
		wires = new ArrayList<EmbeddedCanvas>();
		trkDocas = new ArrayList<EmbeddedCanvas>();
		trkDocasvsTime = new ArrayList<EmbeddedCanvas>();

		for (int i = 0; i < nSectors; i++) {
			wires.add(new EmbeddedCanvas());
			wires.get(i).setSize(4 * 400, 6 * 400);
			wires.get(i).divide(6, 6);
			trkDocas.add(new EmbeddedCanvas());
			trkDocas.get(i).setSize(4 * 400, 6 * 400);
			trkDocas.get(i).divide(3, 2);
			trkDocasvsTime.add(new EmbeddedCanvas());
			trkDocasvsTime.get(i).setSize(4 * 400, 6 * 400);
			trkDocasvsTime.get(i).divide(6, 6);
		}
	}

	private void addToReader() {

		try {
			for (String str : fileArray) {
				this.reader.addFile(str);
			}
			this.reader.open();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	protected void processData() {
		int icounter = 0;
		while (reader.hasEvent()) {// && icounter < 100
			icounter++;
			if (icounter % 200 == 0) {
				System.out.println("Processed " + icounter + " events.");
			}
			EvioDataEvent event = reader.getNextEvent();
			ProcessTBHits tbHits = new ProcessTBHits(event);
			ProcessTBTracks tbTracks = new ProcessTBTracks(event);
			ProcessTBSegments tbSegments = new ProcessTBSegments(event);
			if (tbTracks.getNTrks() > 0) {// && tbHits.getNrows() > 0 && tbSegments.getNrows() > 0
				tbHits.processTBhits();
				tbSegments.processTBSegments(tbHits);
				tbHits.clearMaps();
				tbSegments.clearMaps();
			}
		}
	}

	protected void drawHistograms() {
		createCanvas();
		int canvasPlace;
		for (int i = 0; i < nSL; i++) {
			canvasPlace = 0;
			for (int j = 0; j < nLayer; j++) {
				for (int k = 0; k < nHists; k++) {
					wires.get(i).cd(canvasPlace);
					wires.get(i).draw(hArrWire.get(new Coordinate(i, j, k)));
					canvasPlace++;
				}
			}
			wires.get(i).save("src/images/wires" + (i + 1) + ".png");
		}
		for (int i = 0; i < nSectors; i++) {
			canvasPlace = 0;
			for (int j = 0; j < nSL; j++) {
				trkDocas.get(i).cd(canvasPlace);
				trkDocas.get(i).draw(htrkDoca.get(new Coordinate(i, j)));
				canvasPlace++;
			}
			trkDocas.get(i).save("src/images/trkDocas" + (i + 1) + ".png");
		}

		for (int i = 0; i < nSectors; i++) {
			canvasPlace = 0;
			for (int j = 0; j < nSL; j++) {
				for (int k = 0; k < nThBinsVz; k++) {
					trkDocasvsTime.get(i).cd(canvasPlace);
					trkDocasvsTime.get(i).draw(h2timeVtrkDocaVZ.get(new Coordinate(i, j, k)));
					canvasPlace++;
				}
			}
			trkDocasvsTime.get(i).save("src/images/timeVtrkDocaSector" + (i + 1) + ".png");
		}
	}

	public static void main(String[] args) {

		ArrayList<String> fileArray = new ArrayList<String>();

		fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_1.evio");
		fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_10.evio");
		// fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_2.evio");
		// fileArray.add("/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/pion/cookedFiles/out_out_4.evio");

		// fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_1.evio");
		// fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_10.evio");
		// fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_2.evio");
		// fileArray.add("/Users/michaelkunkel/WORK/CLAS/CLAS12/DC_Calibration/data/Calibration/pion/mergedFiles/cookedFiles/out_out_4.evio");

		TestFile rd = new TestFile(fileArray);
		rd.processData();
		rd.drawHistograms();

	}
}
