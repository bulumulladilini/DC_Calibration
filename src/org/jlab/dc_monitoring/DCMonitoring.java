/*  +__^_________,_________,_____,________^-.-------------------,                            (  )
 *  | |||||||||   `--------'     |          |                   O-------------------------(  )()(  ) 
 *  `+-------------USMC----------^----------|___________________|                            (  )
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jlab.clas.physics.Vector3;
import org.jlab.dc_calibration.domain.Coordinate;
import org.jlab.detector.decode.CLASDecoder;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;
import org.jlab.service.dc.DCHBEngine;
import org.jlab.service.dc.DCTBEngine;

public class DCMonitoring implements IDataEventListener {

	JPanel mainPanel = null;
	DataSourceProcessorPane processorPane = null;
	private JTabbedPane tabbedPane = null;

	private Map<Coordinate, H2F> occupanciesByCoordinate = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H2F> occupanciesintrack = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H2F> trkdocasvstime = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H2F> cross_yvsx = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H2F> cross_uyvsux = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H1F> times = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> timediff_s1 = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> timediff_s2 = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> timediff_s3 = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> timediff_s4 = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> timediff_s5 = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> timediff_s6 = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> Ntksperevnt = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> Nhitspertrk = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> Chisqpertrck = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> Residual = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> Momentum = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> Theta = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H1F> Phi = new HashMap<Coordinate, H1F>();
	private Map<Coordinate, H2F> thetaVSmomenutm = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H2F> thetaVSphi = new HashMap<Coordinate, H2F>();
	private Map<Coordinate, H2F> residualVSDOCA = new HashMap<Coordinate, H2F>();

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
	private EmbeddedCanvas can15 = null;
	private EmbeddedCanvas can16 = null;
	private EmbeddedCanvas can17 = null;
	private EmbeddedCanvas can18 = null;
	private EmbeddedCanvas can19 = null;
	private EmbeddedCanvas can20 = null;
	private EmbeddedCanvas can21 = null;
	private EmbeddedCanvas can22 = null;

	int counter = 0;
	int updateTime = 2000;

	CLASDecoder clasDecoder = new CLASDecoder();

	DCHBEngine enHB = new DCHBEngine();

	DCTBEngine enTB = new DCTBEngine();

	public DCMonitoring() {

		// create main panel
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		tabbedPane = new JTabbedPane();

		processorPane = new DataSourceProcessorPane();
		processorPane.setUpdateRate(100);

		mainPanel.add(tabbedPane);
		mainPanel.add(processorPane, BorderLayout.PAGE_END);

		this.processorPane.addEventListener(this);

		createCanvas();
		addCanvasToPane();
		init();

		enHB.init();
		enTB.init();
	}

	private void createHistograms() {

		for (int i = 0; i < 6; i++) {

			for (int j = 0; j < 3; j++) {
				cross_yvsx.put(new Coordinate(j, i), new H2F("y vs x sector" + (i + 1) + "region" + (j + 1),
				        "sector" + (i + 1) + "region" + (j + 1), 180, -180, 180, 120, -140, 140));
				cross_yvsx.get(new Coordinate(j, i)).setTitleX("x Sector" + (i + 1));
				cross_yvsx.get(new Coordinate(j, i)).setTitleY("y Region" + (j + 1));
				cross_uyvsux.put(new Coordinate(j, i),
				        new H2F("Uy vs Ux sector" + (i + 1) + "region" + (j + 1), "", 100, -1, 1, 100, -1, 1));
				cross_uyvsux.get(new Coordinate(j, i)).setTitleX("Ux Sector" + (i + 1));
				cross_uyvsux.get(new Coordinate(j, i)).setTitleY("Uy Region" + (j + 1));
				if (j == 0) {
					times.put(new Coordinate(j, i),
							new H1F("time sector" + (i + 1), "sector" + (i + 1), 250, -100, 400));
				} else if (j == 1) {
					times.put(new Coordinate(j, i),
							new H1F("time sector" + (i + 1), "sector" + (i + 1), 350, -100, 600));
				} else {
					times.put(new Coordinate(j, i),
							new H1F("time sector" + (i + 1), "sector" + (i + 1), 300, -100, 1200));
				}
				times.get(new Coordinate(j, i)).setTitleX("time Sector" + (i + 1));
				times.get(new Coordinate(j, i)).setTitleY("Region" + (j + 1));
			}
			for (int j = 0; j < 7; j++) {
				if ((i == 0) || (i == 1)) {
					timediff_s1.put(new Coordinate(i, j), new H1F("Delta t Sector 1 SL" + (i + 1) + "board" + (j + 1),
							"superlayer" + (i + 1) + "board" + (j + 1), 100, -400, 400));
					timediff_s2.put(new Coordinate(i, j), new H1F("Delta t Sector 2 SL" + (i + 1) + "board" + (j + 1),
							"superlayer" + (i + 1) + "board" + (j + 1), 100, -400, 400));
					timediff_s3.put(new Coordinate(i, j), new H1F("Delta t Sector 3 SL" + (i + 1) + "board" + (j + 1),
							"superlayer" + (i + 1) + "board" + (j + 1), 100, -400, 400));
					timediff_s4.put(new Coordinate(i, j), new H1F("Delta t Sector 4 SL" + (i + 1) + "board" + (j + 1),
							"superlayer" + (i + 1) + "board" + (j + 1), 100, -400, 400));
					timediff_s5.put(new Coordinate(i, j), new H1F("Delta t Sector 5 SL" + (i + 1) + "board" + (j + 1),
							"superlayer" + (i + 1) + "board" + (j + 1), 100, -400, 400));
					timediff_s6.put(new Coordinate(i, j), new H1F("Delta t Sector 6 SL" + (i + 1) + "board" + (j + 1),
							"superlayer" + (i + 1) + "board" + (j + 1), 100, -400, 400));
				} else if ((i == 2) || (i == 3)) {

					timediff_s1.put(new Coordinate(i, j), new H1F("Delta t Sector 1 SL" + (i + 1) + "board" + (j + 1),
							"superlayer" + (i + 1) + "board" + (j + 1), 100, -600, 600));
					timediff_s2.put(new Coordinate(i, j), new H1F("Delta t Sector 2 SL" + (i + 1) + "board" + (j + 1),
							"superlayer" + (i + 1) + "board" + (j + 1), 100, -600, 600));
					timediff_s3.put(new Coordinate(i, j), new H1F("Delta t Sector 3 SL" + (i + 1) + "board" + (j + 1),
							"superlayer" + (i + 1) + "board" + (j + 1), 100, -600, 600));
					timediff_s4.put(new Coordinate(i, j), new H1F("Delta t Sector 4 SL" + (i + 1) + "board" + (j + 1),
							"superlayer" + (i + 1) + "board" + (j + 1), 100, -600, 600));
					timediff_s5.put(new Coordinate(i, j), new H1F("Delta t Sector 5 SL" + (i + 1) + "board" + (j + 1),
							"superlayer" + (i + 1) + "board" + (j + 1), 100, -600, 600));
					timediff_s6.put(new Coordinate(i, j), new H1F("Delta t Sector 6 SL" + (i + 1) + "board" + (j + 1),
							"superlayer" + (i + 1) + "board" + (j + 1), 100, -600, 600));
				} else {
					timediff_s1.put(new Coordinate(i, j), new H1F("Delta t Sector 1 SL" + (i + 1) + "board" + (j + 1),
							"superlayer" + (i + 1) + "board" + (j + 1), 100, -1000, 1000));
					timediff_s2.put(new Coordinate(i, j), new H1F("Delta t Sector 2 SL" + (i + 1) + "board" + (j + 1),
							"superlayer" + (i + 1) + "board" + (j + 1), 100, -1000, 1000));
					timediff_s3.put(new Coordinate(i, j), new H1F("Delta t Sector 3 SL" + (i + 1) + "board" + (j + 1),
							"superlayer" + (i + 1) + "board" + (j + 1), 100, -1000, 1000));
					timediff_s4.put(new Coordinate(i, j), new H1F("Delta t Sector 4 SL" + (i + 1) + "board" + (j + 1),
							"superlayer" + (i + 1) + "board" + (j + 1), 100, -1000, 1000));
					timediff_s5.put(new Coordinate(i, j), new H1F("Delta t Sector 5 SL" + (i + 1) + "board" + (j + 1),
							"superlayer" + (i + 1) + "board" + (j + 1), 100, -1000, 1000));
					timediff_s6.put(new Coordinate(i, j), new H1F("Delta t Sector 6 SL" + (i + 1) + "board" + (j + 1),
							"superlayer" + (i + 1) + "board" + (j + 1), 100, -1000, 1000));
				}
				timediff_s1.get(new Coordinate(i, j)).setTitleX("Delta time Board" + (j + 1));
				timediff_s1.get(new Coordinate(i, j)).setTitleY("SL" + (i + 1));
				timediff_s2.get(new Coordinate(i, j)).setTitleX("Delta time Board" + (j + 1));
				timediff_s2.get(new Coordinate(i, j)).setTitleY("SL" + (i + 1));
				timediff_s3.get(new Coordinate(i, j)).setTitleX("Delta time Board" + (j + 1));
				timediff_s3.get(new Coordinate(i, j)).setTitleY("SL" + (i + 1));
				timediff_s4.get(new Coordinate(i, j)).setTitleX("Delta time Board" + (j + 1));
				timediff_s4.get(new Coordinate(i, j)).setTitleY("SL" + (i + 1));
				timediff_s5.get(new Coordinate(i, j)).setTitleX("Delta time Board" + (j + 1));
				timediff_s5.get(new Coordinate(i, j)).setTitleY("SL" + (i + 1));
				timediff_s6.get(new Coordinate(i, j)).setTitleX("Delta time Board" + (j + 1));
				timediff_s6.get(new Coordinate(i, j)).setTitleY("SL" + (i + 1));
			}

			Ntksperevnt.put(new Coordinate(i), new H1F("tracks per sector" + (i + 1), "sector" + (i + 1), 20, 0, 20));
			Ntksperevnt.get(new Coordinate(i)).setTitleX("Number of tracks/ evnt sector" + (i + 1));
			Nhitspertrk.put(new Coordinate(i), new H1F("hits per tracks  sector" + (i + 1), "sector" + (i + 1), 100, 0, 100));
			Nhitspertrk.get(new Coordinate(i)).setTitleX("Number of hits/ track sector" + (i + 1));
			Chisqpertrck.put(new Coordinate(i),
					new H1F("Track Chi squared sector " + (i + 1), "sector" + (i + 1), 300, 0, 100));

			Chisqpertrck.get(new Coordinate(i)).setTitleX("Track  Chi squared Sector" + (i + 1));
			Residual.put(new Coordinate(i), new H1F("Residual sector" + (i + 1), "sector" + (i + 1), 100, -1, 1));
			Residual.get(new Coordinate(i)).setTitleX("Residual sector" + (i + 1));
			Momentum.put(new Coordinate(i), new H1F("momentum sector" + (i + 1), "sector" + (i + 1), 100, 0, 8));
			Momentum.get(new Coordinate(i)).setTitleX("momentum sector" + (i + 1));
			Theta.put(new Coordinate(i), new H1F("Theta sector" + (i + 1), "sector" + (i + 1), 100, 0, Math.PI));
			Theta.get(new Coordinate(i)).setTitleX("Theta sector" + (i + 1));
			Phi.put(new Coordinate(i), new H1F("Phi sector" + (i + 1), "sector" + (i + 1), 100, -Math.PI, Math.PI));
			Phi.get(new Coordinate(i)).setTitleX("Phi sector" + (i + 1));
			thetaVSmomenutm.put(new Coordinate(i), new H2F("Theta VS p sector" + (i + 1), "sector" + (i + 1), 100, 0, 8, 100, 0, Math.PI));
			thetaVSmomenutm.get(new Coordinate(i)).setTitleX("p sector" + (i + 1));
			thetaVSmomenutm.get(new Coordinate(i)).setTitleY("theta ");
			thetaVSphi.put(new Coordinate(i),
			        new H2F("Theta VS phi sector" + (i + 1), "sector" + (i + 1), 100, -Math.PI, Math.PI, 100, 0, Math.PI));
			thetaVSphi.get(new Coordinate(i)).setTitleX("phi sector" + (i + 1));
			thetaVSphi.get(new Coordinate(i)).setTitleY("theta ");
			for (int j = 0; j < 6; j++) {
				occupanciesByCoordinate.put(new Coordinate(i, j),
				        new H2F("Occupancy all hits SL" + i + "sector" + j, "", 112, 1, 113, 6, 1, 7));
				occupanciesByCoordinate.get(new Coordinate(i, j)).setTitleX("Wire Sector" + (j + 1));
				occupanciesByCoordinate.get(new Coordinate(i, j)).setTitleY("Layer SL" + (i + 1));
				occupanciesintrack.put(new Coordinate(i, j), new H2F("Occupancy used in track" + i, "", 112, 1, 113, 6, 1, 7));
				occupanciesintrack.get(new Coordinate(i, j)).setTitleX("Wire Sector" + (j + 1));
				occupanciesintrack.get(new Coordinate(i, j)).setTitleY("Layer SL" + (i + 1));
				
				
				if ((i == 0) || (i == 1)) {
					trkdocasvstime.put(new Coordinate(i, j),
							new H2F("TrackDoca vs time" + j, "", 250, -100, 200, 50, 0, 1));
					residualVSDOCA.put(new Coordinate(i, j), new H2F("Residual vs DOCA sector "+(j+1)+" SL " + (i+1), "", 200, 0, 1.0, 100, -1, 1));
				} else if ((i == 2) || (i == 3)) {
					trkdocasvstime.put(new Coordinate(i, j),
							new H2F("TrackDoca vs time" + j, "", 350, -100, 600, 100, 0, 2));
					residualVSDOCA.put(new Coordinate(i, j), new H2F("Residual vs DOCA sector "+(j+1)+" SL " + (i+1), "", 200, 0, 1.6, 100, -1, 1));
				} else {
					trkdocasvstime.put(new Coordinate(i, j),
							new H2F("TrackDoca vs time" + j, "", 450, -100, 800, 100, 0, 2));
					residualVSDOCA.put(new Coordinate(i, j), new H2F("Residual vs DOCA sector "+(j+1)+" SL " + (i+1), "", 200, 0, 2.5, 100, -1, 1));
				}
				trkdocasvstime.get(new Coordinate(i, j)).setTitleX("time Sector" + (j + 1));
				trkdocasvstime.get(new Coordinate(i, j)).setTitleY("TrackDoca SL" + (i + 1));
				residualVSDOCA.get(new Coordinate(i, j)).setTitleX("trkDOCA Sector" + (j + 1));
				residualVSDOCA.get(new Coordinate(i, j)).setTitleY("Residual SL" + (i + 1));
			}
		}
	}

	private void createCanvas() {
		can1 = new EmbeddedCanvas();
		can1.initTimer(updateTime);
		can5 = new EmbeddedCanvas();
		can5.initTimer(updateTime);
		can2 = new EmbeddedCanvas();
		can2.initTimer(updateTime);
		can3 = new EmbeddedCanvas();
		can3.initTimer(updateTime);
		can4 = new EmbeddedCanvas();
		can4.initTimer(updateTime);
		can6 = new EmbeddedCanvas();
		can6.initTimer(updateTime);
		can7 = new EmbeddedCanvas();
		can7.initTimer(updateTime);
		can8 = new EmbeddedCanvas();
		can8.initTimer(updateTime);
		can9 = new EmbeddedCanvas();
		can9.initTimer(updateTime);
		can10 = new EmbeddedCanvas();
		can10.initTimer(updateTime);
		can11 = new EmbeddedCanvas();
		can11.initTimer(updateTime);
		can12 = new EmbeddedCanvas();
		can12.initTimer(updateTime);
		can13 = new EmbeddedCanvas();
		can13.initTimer(updateTime);
		can14 = new EmbeddedCanvas();
		can14.initTimer(updateTime);
		can15 = new EmbeddedCanvas();
		can15.initTimer(updateTime);
		can16 = new EmbeddedCanvas();
		can16.initTimer(updateTime);
		can17 = new EmbeddedCanvas();
		can17.initTimer(updateTime);
		can18 = new EmbeddedCanvas();
		can18.initTimer(updateTime);
		can19 = new EmbeddedCanvas();
		can19.initTimer(updateTime);
		can20 = new EmbeddedCanvas();
		can20.initTimer(updateTime);
		can21 = new EmbeddedCanvas();
		can21.initTimer(updateTime);
		can22 = new EmbeddedCanvas();
		can22.initTimer(updateTime);
		can1.divide(6, 6);
		can2.divide(6, 6);
		can3.divide(6, 3);
		can4.divide(6, 3);
		can5.divide(6, 6);
		can6.divide(6, 3);
		can7.divide(2, 3);
		can8.divide(2, 3);
		can9.divide(2, 3);
		can10.divide(2, 3);
		can11.divide(2, 3);
		can12.divide(2, 3);
		can13.divide(2, 3);
		can14.divide(2, 3);
		can15.divide(7, 6);
		can16.divide(2, 3);
		can17.divide(6, 6);
		can18.divide(7, 6);
		can19.divide(7, 6);
		can20.divide(7, 6);
		can21.divide(7, 6);
		can22.divide(7, 6);
	}

	private void init() {
		createHistograms();
		drawPlots();
	}

	@Override
	public void dataEventAction(DataEvent event) {
		counter++;
		// if (counter % 200 == 0)
		// System.out.println("done " + counter + " events");

		HipoDataEvent hipo = null;
		if (event instanceof EvioDataEvent) { // starting from raw event
			hipo = (HipoDataEvent) clasDecoder.getDataEvent(event);
		} else {
			hipo = (HipoDataEvent) event;
		}
		if (hipo.getBank("TimeBasedTrkg::TBHits").rows() == 0 && hipo.getBank("DC::tdc").rows() > 0) { // can
																										// assume
																										// these
																										// are
																										// reconstructed
																										// events
			// System.out.println("running tracking");
			// run HBT
			enHB.processDataEvent(hipo);
			// Processing TBT
			enTB.processDataEvent(hipo);
			// hipo.show();
		}
		if (hipo.hasBank("TimeBasedTrkg::TBHits"))
			processTBHits(event);
		if (hipo.hasBank("TimeBasedTrkg::TBCrosses"))
			processTBCrosses(event);
		if (hipo.hasBank("TimeBasedTrkg::TBTracks"))
			processTBTracks(event);

	}

	@Override
	public void timerUpdate() {
		// throw new UnsupportedOperationException("Not supported yet."); //To
		// change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void resetEventListener() {
		counter = 0;
		this.init();
	}

	private void processTBHits(DataEvent event) {
		DataBank bnkHits = event.getBank("TimeBasedTrkg::TBHits");

		Map<Integer, Integer> map1 = new HashMap<>();
		Map<Integer, Integer> map2 = new HashMap<>();
		for (int i = 0; i < bnkHits.rows(); i++) {
			occupanciesByCoordinate.get(new Coordinate(bnkHits.getInt("superlayer", i) - 1, bnkHits.getInt("sector", i) - 1))
			        .fill(bnkHits.getInt("wire", i), bnkHits.getInt("layer", i));

			if (bnkHits.getByte("trkID", i) > 0) {
				occupanciesintrack
						.get(new Coordinate(bnkHits.getInt("superlayer", i) - 1, bnkHits.getInt("sector", i) - 1))
						.fill(bnkHits.getInt("wire", i), bnkHits.getInt("layer", i));
				residualVSDOCA.get(new Coordinate(bnkHits.getInt("superlayer", i) - 1, bnkHits.getInt("sector", i) - 1))
				.fill(bnkHits.getFloat("trkDoca", i), bnkHits.getFloat("timeResidual", i));
			}
			

			trkdocasvstime.get(new Coordinate(bnkHits.getInt("superlayer", i) - 1, bnkHits.getInt("sector", i) - 1))
			        .fill(bnkHits.getFloat("time", i), bnkHits.getFloat("trkDoca", i));
			Residual.get(new Coordinate(bnkHits.getInt("sector", i) - 1)).fill(bnkHits.getFloat("timeResidual", i));

			int key = bnkHits.getInt("trkID", i);
			if (key > 0) {
				if (map1.containsKey(key)) {
					int occurrence = map1.get(key);
					occurrence++;
					map1.put(bnkHits.getInt("trkID", i), occurrence);
				} else
					map1.put(key, 1);
				map2.put(key, bnkHits.getInt("sector", i));
			}
			int region = (int) Math.ceil(bnkHits.getInt("superlayer", i) / 2.0);// needs
			                                                                    // the
			                                                                    // .0
			                                                                    // to
			                                                                    // be
			                                                                    // correct
			times.get(new Coordinate(region - 1, bnkHits.getInt("sector", i) - 1)).fill(bnkHits.getFloat("time", i));

			for (int j = i + 1; j < bnkHits.rows(); j++) {
				
				if (bnkHits.getInt("sector", i) == 1 && bnkHits.getInt("sector", j) == 1) {
					int board1 = (int) Math.ceil(bnkHits.getInt("wire", i) / 16.0);// needs
																					// the
																					// .0
																					// to
																					// be
																					// correct
					int board2 = (int) Math.ceil(bnkHits.getInt("wire", j) / 16.0);
					if ((board1 == board2) && (bnkHits.getByte("superlayer", i) == bnkHits.getByte("superlayer", j))) {

						timediff_s1.get(new Coordinate(bnkHits.getInt("superlayer", i) - 1, board1 - 1))
								.fill(bnkHits.getFloat("time", i) - bnkHits.getFloat("time", j));
					}

				}
				else if (bnkHits.getInt("sector", i) == 2 && bnkHits.getInt("sector", j) == 2) {
					int board1 = (int) Math.ceil(bnkHits.getInt("wire", i) / 16.0);// needs
					                                                               // the
					                                                               // .0
					                                                               // to
					                                                               // be
					                                                               // correct
					int board2 = (int) Math.ceil(bnkHits.getInt("wire", j) / 16.0);
					if ((board1 == board2) && (bnkHits.getByte("superlayer", i) == bnkHits.getByte("superlayer", j))) {

						timediff_s2.get(new Coordinate(bnkHits.getInt("superlayer", i) - 1, board1 - 1))
								.fill(bnkHits.getFloat("time", i) - bnkHits.getFloat("time", j));
					}

				}
				else if (bnkHits.getInt("sector", i) == 3 && bnkHits.getInt("sector", j) == 3) {
					int board1 = (int) Math.ceil(bnkHits.getInt("wire", i) / 16.0);// needs
																					// the
																					// .0
																					// to
																					// be
																					// correct
					int board2 = (int) Math.ceil(bnkHits.getInt("wire", j) / 16.0);
					if ((board1 == board2) && (bnkHits.getByte("superlayer", i) == bnkHits.getByte("superlayer", j))) {

						timediff_s3.get(new Coordinate(bnkHits.getInt("superlayer", i) - 1, board1 - 1))
								.fill(bnkHits.getFloat("time", i) - bnkHits.getFloat("time", j));
					}

				}
				else if (bnkHits.getInt("sector", i) == 4 && bnkHits.getInt("sector", j) == 4) {
					int board1 = (int) Math.ceil(bnkHits.getInt("wire", i) / 16.0);// needs
																					// the
																					// .0
																					// to
																					// be
																					// correct
					int board2 = (int) Math.ceil(bnkHits.getInt("wire", j) / 16.0);
					if ((board1 == board2) && (bnkHits.getByte("superlayer", i) == bnkHits.getByte("superlayer", j))) {

						timediff_s4.get(new Coordinate(bnkHits.getInt("superlayer", i) - 1, board1 - 1))
								.fill(bnkHits.getFloat("time", i) - bnkHits.getFloat("time", j));
					}

				}
				else if (bnkHits.getInt("sector", i) == 5 && bnkHits.getInt("sector", j) == 5) {
					int board1 = (int) Math.ceil(bnkHits.getInt("wire", i) / 16.0);// needs
																					// the
																					// .0
																					// to
																					// be
																					// correct
					int board2 = (int) Math.ceil(bnkHits.getInt("wire", j) / 16.0);
					if ((board1 == board2) && (bnkHits.getByte("superlayer", i) == bnkHits.getByte("superlayer", j))) {

						timediff_s5.get(new Coordinate(bnkHits.getInt("superlayer", i) - 1, board1 - 1))
								.fill(bnkHits.getFloat("time", i) - bnkHits.getFloat("time", j));
					}

				}
				else{
					int board1 = (int) Math.ceil(bnkHits.getInt("wire", i) / 16.0);// needs
																					// the
																					// .0
																					// to
																					// be
																					// correct
					int board2 = (int) Math.ceil(bnkHits.getInt("wire", j) / 16.0);
					if ((board1 == board2) && (bnkHits.getByte("superlayer", i) == bnkHits.getByte("superlayer", j))) {

						timediff_s6.get(new Coordinate(bnkHits.getInt("superlayer", i) - 1, board1 - 1))
								.fill(bnkHits.getFloat("time", i) - bnkHits.getFloat("time", j));

					}

				}

			}
		}
		for (int key : map1.keySet()) {
			int occurrence = map1.get(key);

			Nhitspertrk.get(new Coordinate(map2.get(key) - 1)).fill(occurrence);

		}
	}

	private void processTBCrosses(DataEvent event) {
		DataBank tbcrossesBank = event.getBank("TimeBasedTrkg::TBCrosses");
		for (int i = 0; i < tbcrossesBank.rows(); i++) {
			cross_uyvsux.get(new Coordinate(tbcrossesBank.getInt("region", i) - 1, tbcrossesBank.getInt("sector", i) - 1))
			        .fill(tbcrossesBank.getFloat("ux", i), tbcrossesBank.getFloat("uy", i));
			cross_yvsx.get(new Coordinate(tbcrossesBank.getInt("region", i) - 1, tbcrossesBank.getInt("sector", i) - 1))
			        .fill(tbcrossesBank.getFloat("x", i), tbcrossesBank.getFloat("y", i));

		}
	}

	private void processTBTracks(DataEvent event) {
		Vector3 momentum = new Vector3();
		DataBank tbtracksBank = event.getBank("TimeBasedTrkg::TBTracks");

		Map<Integer, Integer> map = new HashMap<>();
		for (int i = 0; i < tbtracksBank.rows(); i++) {
			int key = tbtracksBank.getInt("sector", i);
			if (map.containsKey(key)) {
				int occurrence = map.get(key);
				occurrence++;
				map.put(tbtracksBank.getInt("sector", i), occurrence);
			} else
				map.put(key, 1);

			double chisq = tbtracksBank.getFloat("chi2", i);
			double NDF = tbtracksBank.getShort("ndf", i);
			momentum.setXYZ(tbtracksBank.getFloat("p0_x", i), tbtracksBank.getFloat("p0_y", i),
					tbtracksBank.getFloat("p0_z", i));
			thetaVSmomenutm.get(new Coordinate(tbtracksBank.getInt("sector", i) - 1)).fill(momentum.mag(),
					momentum.theta());

			thetaVSphi.get(new Coordinate(tbtracksBank.getInt("sector", i) - 1)).fill(momentum.phi(), momentum.theta());
			Chisqpertrck.get(new Coordinate(tbtracksBank.getInt("sector", i) - 1)).fill(chisq/NDF);
			Momentum.get(new Coordinate(tbtracksBank.getInt("sector", i) - 1)).fill(momentum.mag());
			Theta.get(new Coordinate(tbtracksBank.getInt("sector", i) - 1)).fill(momentum.theta());
			Phi.get(new Coordinate(tbtracksBank.getInt("sector", i) - 1)).fill(momentum.phi());

		}
		for (int key : map.keySet()) {
			int occurrence = map.get(key);
			// System.out.println(key + " occur " + occurrence + " time(s).");
			Ntksperevnt.get(new Coordinate(key - 1)).fill(occurrence);

		}
	}

	private void drawPlots() {

		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 6; j++) {
				can1.cd(6 * i + j);
				can1.draw(occupanciesByCoordinate.get(new Coordinate(i, j)));
				can2.cd(6 * i + j);
				can2.draw(occupanciesintrack.get(new Coordinate(i, j)));
				can5.cd(6 * i + j);
				can5.draw(trkdocasvstime.get(new Coordinate(i, j)));
				can17.cd(6 * i + j);
				can17.draw(residualVSDOCA.get(new Coordinate(i, j)));
				can17.getPad(6 * i + j).getAxisZ().setLog(true);

			}
		}

		for (int i = 0; i < 6; i++) {

			for (int j = 0; j < 3; j++) {
				can3.cd(6 * j + i);
				can3.draw(cross_uyvsux.get(new Coordinate(j, i)));
				can4.cd(6 * j + i);
				can4.draw(cross_yvsx.get(new Coordinate(j, i)));
				can6.cd(6 * j + i);
				can6.draw(times.get(new Coordinate(j, i)));

			}
			for (int j = 0; j < 7; j++) {
				can15.cd(7 * i + j);
				can15.draw(timediff_s1.get(new Coordinate(i, j)));
				can18.cd(7 * i + j);
				can18.draw(timediff_s2.get(new Coordinate(i, j)));
				can19.cd(7 * i + j);
				can19.draw(timediff_s3.get(new Coordinate(i, j)));
				can20.cd(7 * i + j);
				can20.draw(timediff_s4.get(new Coordinate(i, j)));
				can21.cd(7 * i + j);
				can21.draw(timediff_s5.get(new Coordinate(i, j)));
				can22.cd(7 * i + j);
				can22.draw(timediff_s6.get(new Coordinate(i, j)));
			}

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
			can16.cd(i);
			can16.draw(Nhitspertrk.get(new Coordinate(i)));
		}
		can1.update();
		can2.update();
		can3.update();
		can4.update();
		can5.update();
		can6.update();
		can7.update();
		can8.update();
		can9.update();
		can10.update();
		can11.update();
		can12.update();
		can13.update();
		can14.update();
		can15.update();
		can16.update();
		can17.update();
		can18.update();
		can19.update();
		can20.update();
		can21.update();
		can22.update();
	}

	private void addCanvasToPane() {
		tabbedPane.add("Occupancies all", can1);
		tabbedPane.add("Occupancies track", can2);
		tabbedPane.add("Tracks per Event", can7);
		tabbedPane.add("Crosses angles", can3);
		tabbedPane.add("Crosses position", can4);
		tabbedPane.add("TrkDoca vs Time", can5);
		tabbedPane.add("time", can6);
		tabbedPane.add("TrkChisq", can8);
		tabbedPane.add("Residuals", can9);
		tabbedPane.add("Momentum", can10);
		tabbedPane.add("Theta", can11);
		tabbedPane.add("Phi", can12);
		tabbedPane.add("ThetaVSp", can13);
		tabbedPane.add("ThetaVSphi", can14);
		tabbedPane.add("Delta t Sector1", can15);
		tabbedPane.add("Delta t Sector2", can18);
		tabbedPane.add("Delta t Sector3", can19);
		tabbedPane.add("Delta t Sector4", can20);
		tabbedPane.add("Delta t Sector5", can21);
		tabbedPane.add("Delta t Sector6", can22);
		tabbedPane.add("Hits per track", can16);
		tabbedPane.add("residualVSDOCA", can17);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("DC MONITORING");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension screensize = null;
		screensize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize((int) (screensize.getHeight() * .75 * 1.618), (int) (screensize.getHeight() * .75));
		DCMonitoring viewer = new DCMonitoring();
		// frame.add(viewer.getPanel());
		frame.add(viewer.mainPanel);
		frame.setVisible(true);


	}

}
