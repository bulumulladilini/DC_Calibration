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
 *  `------'				 @author Olga Cortes
*/
package org.jlab.dc_monitoring;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.jlab.clas.physics.Vector3;
import org.jlab.dc_calibration.domain.Coordinate;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.hipo.HipoDataSource;

public class DCMonitoring {

	private Dimension screensize = null;
	private JFrame frame = null;
	private JTabbedPane tabbedPane = null;

	private Map<Coordinate, H2F> occupanciesByCoordinate = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H2F> occupanciesintrack = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H2F> trkdocasvstime = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H2F> cross_yvsx = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H2F> cross_uyvsux = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H1F> Ntksperevnt = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> Chisqpertrck = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> Residual = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> Momentum = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> Theta = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> Phi = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H2F> thetaVSmomenutm = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H2F> thetaVSphi = new HashMap<Coordinate, H2F>();

	private EmbeddedCanvas can1 = null;
	private EmbeddedCanvas can2 = null;
	private EmbeddedCanvas can3 = null;
	private EmbeddedCanvas can4 = null;
	private EmbeddedCanvas can5 = null;
	private EmbeddedCanvas can6 = null;
	private EmbeddedCanvas can7 = null;
	private EmbeddedCanvas can8 = null;
	private EmbeddedCanvas can9 = null;
	private EmbeddedCanvas can10 = null;
	private EmbeddedCanvas can11 = null;
	private EmbeddedCanvas can12 = null;
	private EmbeddedCanvas can13 = null;
	private EmbeddedCanvas can14 = null;

	private HipoDataSource reader = null;

	public DCMonitoring(String fileName) {
		this.reader = new HipoDataSource();
		reader.open(fileName);
		init();
		processEvent();
		drawPlots();
		addCanvasToPane();
	}

	public DCMonitoring(HipoDataSource reader) {
		this.reader = reader;
		init();
		processEvent();
		drawPlots();
		addCanvasToPane();
	}

	private void setScreenSize() {
		screensize = Toolkit.getDefaultToolkit().getScreenSize();
	}

	private void setJFrame() {
		frame = new JFrame("DC MONITORING");
		frame.setSize((int) (screensize.getHeight() * .75 * 1.618), (int) (screensize.getHeight() * .75));
	}

	private void setJTabbedPane() {
		tabbedPane = new JTabbedPane();
	}

	private void createHistograms() {
		for (int i = 0; i < 6; i++) {
			cross_yvsx.put(new Coordinate(i),
					new H2F("y vs x sector" + (i + 1), "sector" + (i + 1), 180, -180, 180, 120, -140, 140));
			cross_yvsx.get(new Coordinate(i)).setTitleX("x");
			cross_yvsx.get(new Coordinate(i)).setTitleY("y");
			cross_uyvsux.put(new Coordinate(i), new H2F("Uy vs Ux sector" + (i + 1), "", 100, -1, 1, 100, -1, 1));
			cross_uyvsux.get(new Coordinate(i)).setTitleY("Ux");
			cross_uyvsux.get(new Coordinate(i)).setTitleY("Uy");
			Ntksperevnt.put(new Coordinate(i), new H1F("tracks per sector" + (i + 1), "sector" + (i + 1), 20, 0, 20));
			Ntksperevnt.get(new Coordinate(i)).setTitleX("Number of tracks/ evnt");
			Chisqpertrck.put(new Coordinate(i),
					new H1F("Track Chi squared sector " + (i + 1), "sector" + (i + 1), 100, 0, 10));
			Chisqpertrck.get(new Coordinate(i)).setTitleX("Track  Chi squared");
			Residual.put(new Coordinate(i), new H1F("Residual sector" + (i + 1), "sector" + (i + 1), 100, -1, 1));
			Residual.get(new Coordinate(i)).setTitle("Residual sector" + (i + 1));
			Momentum.put(new Coordinate(i), new H1F("momentum sector" + (i + 1), "sector" + (i + 1), 100, 0, 8));
			Momentum.get(new Coordinate(i)).setTitle("momentum sector" + (i + 1));
			Theta.put(new Coordinate(i), new H1F("Theta sector" + (i + 1), "sector" + (i + 1), 100, 0, Math.PI));
			Theta.get(new Coordinate(i)).setTitleX("Theta sector" + (i + 1));
			Phi.put(new Coordinate(i), new H1F("Phi sector" + (i + 1), "sector" + (i + 1), 100, -Math.PI, Math.PI));
			Phi.get(new Coordinate(i)).setTitleX("Phi sector" + (i + 1));
			thetaVSmomenutm.put(new Coordinate(i),
					new H2F("Theta VS p sector" + (i + 1), "sector" + (i + 1), 100, 0, 8, 100, 0, Math.PI));
			thetaVSmomenutm.get(new Coordinate(i)).setTitleX("p sector" + (i + 1));
			thetaVSmomenutm.get(new Coordinate(i)).setTitleY("theta ");
			thetaVSphi.put(new Coordinate(i), new H2F("Theta VS phi sector" + (i + 1), "sector" + (i + 1), 100,
					-Math.PI, Math.PI, 100, 0, Math.PI));
			thetaVSphi.get(new Coordinate(i)).setTitleX("phi sector" + (i + 1));
			thetaVSphi.get(new Coordinate(i)).setTitleY("theta ");
			for (int j = 0; j < 6; j++) {
				occupanciesByCoordinate.put(new Coordinate(i, j),
						new H2F("Occupancy all hits" + i, "", 112, 1, 113, 6, 1, 7));
				occupanciesByCoordinate.get(new Coordinate(i, j)).setTitleX("Wire Sector" + (j + 1));
				occupanciesByCoordinate.get(new Coordinate(i, j)).setTitleY("Layer SL" + (i + 1));
				occupanciesintrack.put(new Coordinate(i, j),
						new H2F("Occupancy used in track" + i, "", 112, 1, 113, 6, 1, 7));
				occupanciesintrack.get(new Coordinate(i, j)).setTitleX("Wire Sector" + (j + 1));
				occupanciesintrack.get(new Coordinate(i, j)).setTitleY("Layer SL" + (i + 1));
				if ((j % 6 == 0) || (j % 6 == 1)) {
					trkdocasvstime.put(new Coordinate(i, j),
							new H2F("TrackDoca vs time" + j, "", 100, 0, 200, 100, 0, 1));
				} else
					trkdocasvstime.put(new Coordinate(i, j),
							new H2F("TrackDoca vs time" + j, "", 100, 0, 600, 100, 0, 2));
				trkdocasvstime.get(new Coordinate(i, j)).setTitleY("time");
				trkdocasvstime.get(new Coordinate(i, j)).setTitleY("TrackDoca");
			}
		}
	}

	private void createCanvas() {
		can1 = new EmbeddedCanvas();
		can5 = new EmbeddedCanvas();
		can2 = new EmbeddedCanvas();
		can3 = new EmbeddedCanvas();
		can4 = new EmbeddedCanvas();
		can6 = new EmbeddedCanvas();
		can7 = new EmbeddedCanvas();
		can8 = new EmbeddedCanvas();
		can9 = new EmbeddedCanvas();
		can10 = new EmbeddedCanvas();
		can11 = new EmbeddedCanvas();
		can12 = new EmbeddedCanvas();
		can13 = new EmbeddedCanvas();
		can14 = new EmbeddedCanvas();
		can1.divide(6, 6);
		can3.divide(2, 3);
		can4.divide(2, 3);
		can5.divide(6, 6);
		can6.divide(2, 2);
		can7.divide(2, 3);
		can8.divide(2, 3);
		can9.divide(2, 3);
		can10.divide(2, 3);
		can11.divide(2, 3);
		can12.divide(2, 3);
		can13.divide(2, 3);
		can14.divide(2, 3);

	}

	private void init() {
		setScreenSize();
		setJFrame();
		setJTabbedPane();
		createHistograms();
		createCanvas();
		// reader = new EvioSource();
		// reader.open();
	}

	private void processEvent() {
		int counter = 0;
		while (reader.hasEvent()) {
			DataEvent event = reader.getNextEvent();
			counter++;
			if (counter % 200 == 0)
				System.out.println("done " + counter + " events");
			if (event.hasBank("TimeBasedTrkg::TBHits"))
				processTBHits(event);
			if (event.hasBank("TimeBasedTrkg::TBCrosses"))
				processTBCrosses(event);
			if (event.hasBank("TimeBasedTrkg::TBTracks"))
				processTBTracks(event);

		}

	}

	private void processTBHits(DataEvent event) {
		DataBank bnkHits = event.getBank("TimeBasedTrkg::TBHits");
		for (int i = 0; i < bnkHits.rows(); i++) {
			occupanciesByCoordinate
					.get(new Coordinate(bnkHits.getInt("superlayer", i) - 1, bnkHits.getInt("sector", i) - 1))
					.fill(bnkHits.getInt("wire", i), bnkHits.getInt("layer", i));
			trkdocasvstime.get(new Coordinate(bnkHits.getInt("sector", i) - 1, bnkHits.getInt("superlayer", i) - 1))
					.fill(bnkHits.getFloat("time", i), bnkHits.getFloat("trkDoca", i));
			Residual.get(new Coordinate(bnkHits.getInt("sector", i) - 1)).fill(bnkHits.getFloat("timeResidual", i));
		}

	}

	private void processTBCrosses(DataEvent event) {
		DataBank tbcrossesBank = event.getBank("TimeBasedTrkg::TBCrosses");
		for (int i = 0; i < tbcrossesBank.rows(); i++) {
			cross_uyvsux.get(new Coordinate(tbcrossesBank.getInt("sector", i) - 1))
					.fill(tbcrossesBank.getFloat("ux", i), tbcrossesBank.getFloat("uy", i));
			cross_yvsx.get(new Coordinate(tbcrossesBank.getInt("sector", i) - 1)).fill(tbcrossesBank.getFloat("x", i),
					tbcrossesBank.getFloat("y", i));
		}
	}

	private void processTBTracks(DataEvent event) {
		Vector3 momentum = new Vector3();
		DataBank tbtracksBank = event.getBank("TimeBasedTrkg::TBTracks");
		DataBank tbcrossBank = event.getBank("TimeBasedTrkg::TBCrosses");
		DataBank tbsegmBank = event.getBank("TimeBasedTrkg::TBSegments");
		DataBank tbhitsBank = event.getBank("TimeBasedTrkg::TBHits");
		for (int i = 0; i < 6; i++) {
			// int ntrck = tbtracksBank.rows();
			Ntksperevnt.get(new Coordinate(i)).fill(tbtracksBank.rows());
		}

		for (int i = 0; i < tbtracksBank.rows(); i++) {
			double px = tbtracksBank.getFloat("p0_x", i);
			double py = tbtracksBank.getFloat("p0_y", i);
			double pz = tbtracksBank.getFloat("p0_z", i);
			double p = Math.sqrt(px * py + py * py + pz * pz);
			double chisq = 1;
			momentum.setXYZ(px, py, pz);
			thetaVSmomenutm.get(new Coordinate(tbtracksBank.getInt("sector", i) - 1)).fill(p, momentum.theta());
			thetaVSphi.get(new Coordinate(tbtracksBank.getInt("sector", i) - 1)).fill(momentum.phi(), momentum.theta());
			Chisqpertrck.get(new Coordinate(tbtracksBank.getInt("sector", i) - 1)).fill(chisq);
			Momentum.get(new Coordinate(tbtracksBank.getInt("sector", i) - 1)).fill(p);
			Theta.get(new Coordinate(tbtracksBank.getInt("sector", i) - 1)).fill(momentum.theta());
			Phi.get(new Coordinate(tbtracksBank.getInt("sector", i) - 1)).fill(momentum.phi());
			/*
			 * for (int j = 1; j < 4; j++) { for (int j2 = 1; j2 < 3; j2++) {
			 * for (int k = 0; k < tbsegmBank.getInt("size",
			 * tbcrossBank.getInt("Segment" + j2 + "_ID",
			 * tbtracksBank.getInt("Cross" + j + "_ID", i))); k++) {
			 * 
			 * for (int k2 = 0; k2 < tbhitsBank.rows(); k2++) {
			 *
			 * if (tbhitsBank.getInt("ID", k2) == tbsegmBank.getInt("Hit" + (k2
			 * + 1) + "_ID", tbcrossBank .getInt("Segment" + j2 + "_ID",
			 * tbtracksBank.getInt("Cross" + j + "_ID", i)))) {
			 * occupanciesintrack .get(new
			 * Coordinate(tbhitsBank.getInt("superlayer", k2) - 1,
			 * tbhitsBank.getInt("sector", k2) - 1))
			 * .fill(tbhitsBank.getInt("wire", k2), tbhitsBank.getInt("layer",
			 * k2)); }
			 *
			 * }
			 * 
			 * 
			 * }
			 * 
			 * }
			 * 
			 * }
			 */

		}
	}

	private void drawPlots() {
		int canvasPlace = 0;
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 6; j++) {
				can1.cd(canvasPlace);
				can1.draw(occupanciesByCoordinate.get(new Coordinate(i, j)));
				can2.cd(canvasPlace);
				can2.draw(occupanciesintrack.get(new Coordinate(i, j)));
				can5.cd(canvasPlace);
				can5.draw(trkdocasvstime.get(new Coordinate(i, j)));
				canvasPlace++;
			}
		}
		for (int i = 0; i < 6; i++) {
			can3.cd(i);
			can3.draw(cross_uyvsux.get(new Coordinate(i)));
			can4.cd(i);
			can4.draw(cross_yvsx.get(new Coordinate(i)));
			can7.cd(i);
			can7.draw(Ntksperevnt.get(new Coordinate(i)));
			can8.cd(i);
			can8.draw(Chisqpertrck.get(new Coordinate(i)));
			can9.cd(i);
			can9.draw(Residual.get(new Coordinate(i)));
			can10.cd(i);
			can10.draw(Momentum.get(new Coordinate(i)));
			can11.cd(i);
			can11.draw(Theta.get(new Coordinate(i)));
			can12.cd(i);
			can12.draw(Phi.get(new Coordinate(i)));
			can13.cd(i);
			can13.draw(thetaVSmomenutm.get(new Coordinate(i)));
			can14.cd(i);
			can14.draw(thetaVSphi.get(new Coordinate(i)));
		}

		// can6.cd(0);
		// can6.draw(thetaVSmomenutm);
		// can6.cd(1);
		// can6.draw(phiVSmomenutm);
		// can6.cd(2);
		// can6.draw(thetaVSphi);
	}

	private void addCanvasToPane() {
		tabbedPane.add("Occupancies all", can1);
		tabbedPane.add("Occupancies track", can2);
		tabbedPane.add("Tracks per Event", can7);
		tabbedPane.add("Crosses angles", can3);
		tabbedPane.add("Crosses position", can4);
		tabbedPane.add("TrkDoca vs Time", can5);
		// tabbedPane.add("Track Kin", can6);
		tabbedPane.add("TrkChisq", can8);
		tabbedPane.add("Residuals", can9);
		tabbedPane.add("Momentum", can10);
		tabbedPane.add("Theta", can11);
		tabbedPane.add("Phi", can12);
		tabbedPane.add("ThetaVSp", can13);
		tabbedPane.add("ThetaVSphi", can14);

		frame.add(tabbedPane);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		// String fileName =
		// "/Volumes/Mac_Storage/Work_Codes/CLAS12/DC_Calibration/data/out_clasdispr.00.e11.000.emn0.75tmn.09.xs65.61nb.dis.1.evio";
		// DCMonitoring test = new DCMonitoring(fileName);

		// Uncomment to test using EvioDataChain
		HipoDataSource chain = new HipoDataSource();
		String fileName = "/Users/omcortes/jlab/rec_file0.hipo";
		chain.open(fileName);
		fileName = "/Users/omcortes/jlab/rec_file0.hipo";
		chain.open(fileName);

		DCMonitoring test = new DCMonitoring(chain);

	}
}
