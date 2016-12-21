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

public class ProcessTBSegmentTrajectory {
	private EvioDataBank bnkSegs;
	private int nSegs;

	public ProcessTBSegmentTrajectory(EvioDataEvent event) {
		this.bnkSegs = (EvioDataBank) event.getBank("TimeBasedTrkg::TBSegments");
		init();
	}

	public int getNsegs() {
		return nSegs;
	}

	private void init() {
		if (bnkSegs != null)
			setNTrks();
		else
			this.nSegs = 0;
	}

	private void setNTrks() {
		this.nSegs = bnkSegs.rows();

	}
}
