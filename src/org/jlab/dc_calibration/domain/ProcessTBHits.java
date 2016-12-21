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

import static org.jlab.dc_calibration.domain.TestFile.hArrWire;

import java.util.HashMap;
import java.util.Map;

import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;

public class ProcessTBHits {

	private EvioDataBank bnkHits;
	private int nHits;
	private EvioDataEvent event;
	protected Map<Integer, Integer> layerMapTBHits;
	protected Map<Integer, Integer> wireMapTBHits;
	protected Map<Integer, Double> timeMapTBHits;
	protected Map<Integer, Double> trkDocaMapTBHits;

	public ProcessTBHits(EvioDataEvent event) {
		this.event = event;
		init();
	}

	private void init() {
		if (event.hasBank("TimeBasedTrkg::TBHits")) {
			this.bnkHits = (EvioDataBank) event.getBank("TimeBasedTrkg::TBHits");
			initMaps();
			setNhits();

		} else
			this.nHits = 0;
	}

	private void initMaps() {
		this.layerMapTBHits = new HashMap<Integer, Integer>();
		this.wireMapTBHits = new HashMap<Integer, Integer>();
		this.timeMapTBHits = new HashMap<Integer, Double>();
		this.trkDocaMapTBHits = new HashMap<Integer, Double>();
	}

	private void setNhits() {
		this.nHits = bnkHits.rows();

	}

	protected void processTBhits() {

		for (int j = 0; j < nHits; j++) {
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

}
