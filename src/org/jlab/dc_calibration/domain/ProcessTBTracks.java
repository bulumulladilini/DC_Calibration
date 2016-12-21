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

import org.jlab.io.evio.EvioDataBank;
import org.jlab.io.evio.EvioDataEvent;

public class ProcessTBTracks {

	private EvioDataBank bnkTrks;
	private int nTrks;

	public ProcessTBTracks(EvioDataEvent event) {
		this.bnkTrks = (EvioDataBank) event.getBank("TimeBasedTrkg::TBTracks");
		init();
	}

	public int getNTrks() {
		return nTrks;
	}

	private void init() {
		if (bnkTrks != null)
			setNtrks();
		else
			this.nTrks = 0;
	}

	private void setNtrks() {
		this.nTrks = bnkTrks.rows();

	}

}
