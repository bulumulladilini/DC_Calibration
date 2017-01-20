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

public class ProcessTBTracks extends DCTBValid implements iProcessable {

	private EvioDataBank bnkTrks;
	private EvioDataEvent event;
	private int nTrks;

	public ProcessTBTracks(EvioDataEvent event) {
		this.event = event;
		init();
	}

	public int getNTrks() {
		return nTrks;
	}

	private void init() {
		if (this.isValid()) {
			this.bnkTrks = (EvioDataBank) event.getBank("TimeBasedTrkg::TBTracks");
			setNtrks();
		} else
			this.nTrks = 0;
	}

	private void setNtrks() {
		this.nTrks = bnkTrks.rows();

	}

	@Override
	protected boolean isValid() {
		return event.hasBank("TimeBasedTrkg::TBTracks") ? true : false;
	}

	@Override
	public void processEvent(EvioDataEvent event) {
		// TODO Auto-generated method stub

	}

}
