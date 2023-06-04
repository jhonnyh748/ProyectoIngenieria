/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014, XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 **  Sahan Abeyasinghe,Jim Keys,  Andrew Wang, Peng Zhang
 ** All rights reserved.
**This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
**To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package ab.demo;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.utils.StateUtil;
import ab.vision.ABObject;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;

public class HeuristicAgent implements Runnable {

	private ActionRobot aRobot;
	private Random randomGenerator;
	
	/* NIVEL QUE SE VA A EJECUTAR */
	public int currentLevel = 3;
	
	public static int time_limit = 12;
	private Map<Integer,Integer> scores = new LinkedHashMap<Integer,Integer>();
	TrajectoryPlanner tp;
	private boolean firstShot;
	private Point prevTarget;
	public HeuristicAgent() {
		
		aRobot = new ActionRobot();
		tp = new TrajectoryPlanner();
		prevTarget = null;
		firstShot = true;
		randomGenerator = new Random();
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();

	}

	
	// run the client
	public void run() {

		aRobot.loadLevel(currentLevel);
		while (true) {
			GameState state = solve();
			if (state == GameState.WON) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int score = StateUtil.getScore(ActionRobot.proxy);
				if(!scores.containsKey(currentLevel))
					scores.put(currentLevel, score);
				else
				{
					if(scores.get(currentLevel) < score)
						scores.put(currentLevel, score);
				}
				int totalScore = 0;
				for(Integer key: scores.keySet()){

					totalScore += scores.get(key);
					System.out.println(" Level " + key
							+ " Score: " + scores.get(key) + " ");
				}
				System.out.println("Total Score: " + totalScore);
				aRobot.loadLevel(++currentLevel);
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
			} else if (state == GameState.LOST) {
				System.out.println("Restart");
				aRobot.restartLevel();
			} else if (state == GameState.LEVEL_SELECTION) {
				System.out
				.println("Unexpected level selection page, go to the last current level : "
						+ currentLevel);
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.MAIN_MENU) {
				System.out
				.println("Unexpected main menu page, go to the last current level : "
						+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			} else if (state == GameState.EPISODE_MENU) {
				System.out
				.println("Unexpected episode menu page, go to the last current level : "
						+ currentLevel);
				ActionRobot.GoFromMainMenuToLevelSelection();
				aRobot.loadLevel(currentLevel);
			}

		}

	}

	private double distance(Point p1, Point p2) {
		return Math
				.sqrt((double) ((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y)
						* (p1.y - p2.y)));
	}

	public GameState solve()
	{

		// capture Image
		BufferedImage screenshot = ActionRobot.doScreenShot();

		// process image
		Vision vision = new Vision(screenshot);

		// find the slingshot
		Rectangle sling = vision.findSlingshotMBR();

		// confirm the slingshot
		while (sling == null && aRobot.getState() == GameState.PLAYING) {
			System.out
			.println("No slingshot detected. Please remove pop up or zoom out");
			ActionRobot.fullyZoomOut();
			screenshot = ActionRobot.doScreenShot();
			vision = new Vision(screenshot);
			sling = vision.findSlingshotMBR();
		}
        // get all the pigs
 		List<ABObject> pigs = vision.findPigsMBR();
 		
 		// get all objects
 		List<ABObject> blocks = vision.findBlocksMBR();
 		System.out.println("Objetos "+pigs);

		GameState state = aRobot.getState();

		// if there is a sling, then play, otherwise just skip.
		if (sling != null) {

			if (!pigs.isEmpty()) {

				Point releasePoint = null;
				Shot shot = new Shot();
				int dx,dy;
				{
					// random pick up a pig
					ABObject pig = pigs.get(randomGenerator.nextInt(pigs.size()));
					
					/* Se verifica si los cerdos estan situados verticalmente y en este caso, 
					 * se elige el que estan en la mitad de todos como el objetivo.
					 * Si los cerdos se encuantran en una orientacion horizontal, se elige como
					 * objetivo el que esta mas a la izquierda. */
					if(pigs.size() > 1) {
						ArrayList<Integer> _Cy = new ArrayList<Integer>();
						double differences = 0;
						double average = 0;
						
						ABObject pigTest = pigs.get(0);
						_Cy.add(pigTest.getCenter().y);
						for(int i=0; i < pigs.size()-1; i++) {
							Point tptTemp = pigTest.getCenter();
							System.out.println("TEMP" + pigTest);
							ABObject pig1 = pigs.get(i);
							Point tpt1 = pig1.getCenter();
							ABObject pig2 = pigs.get(i+1);
							Point tpt2 = pig2.getCenter();
							differences += Math.abs( tpt1.x - tpt2.x );
							// System.out.println("Differences " + differences);
							_Cy.add(tpt2.y);
							if(tpt2.x < tptTemp.x) {
								pigTest = pig2;
							}
						}
						average = differences / pigs.size();
						// System.out.println("Average " + average);
						if(average < 15) {
							int centerPigY = middlePosition(_Cy);
							pigTest = pigs.get(centerPigY);
						}
						pig = pigTest;
					}
								
					
					// System.out.println("PIGS ->" + pigs);
					// Se obtienen las coordenadas que representa el cerdo objetivo
					Point _tpt = pig.getCenter();
					System.out.println("CoordenadasPIG ->" +_tpt); //Coordenadas del cerdo seleccionado JH
					if (prevTarget != null && distance(prevTarget, _tpt) < 10) {
						double _angle = randomGenerator.nextDouble() * Math.PI * 2;
						_tpt.x = _tpt.x + (int) (Math.cos(_angle) * 10);
						_tpt.y = _tpt.y + (int) (Math.sin(_angle) * 10);
						System.out.println("Randomly changing to " + _tpt);
					}

					prevTarget = new Point(_tpt.x, _tpt.y);

					// Se estiman los puntos de liberacion de la honda
					ArrayList<Point> pts = tp.estimateLaunchPoint(sling, _tpt);
					System.out.println("lista de puntos " + pts);

					if (pts.size() == 1)
						releasePoint = pts.get(0);
					else if (pts.size() == 2)
					{
						System.out.println("Mas de un punto de lanzamiento disponible ");						 
						
						Point releasePointTemp1 = pts.get(0);
						Point releasePointTemp2 = pts.get(1);
						
						// Predecir trayectoria para ver si se encuentra con un obstaculo, se realizará el lanzamiento
						// por la trayectoria que se encuentre libre, si en ambas se detectan obstaculo, se elige una aleatoriamente.
						List<Point> trajectory1 = tp.predictTrajectory(sling, releasePointTemp1);
						List<Point> trajectory2 = tp.predictTrajectory(sling, releasePointTemp2);
						System.out.println("Trayectoria1 ");
						boolean blockDetected1 = blockExist(blocks, trajectory1, _tpt);
						System.out.println("Trayectoria2 ");
						boolean blockDetected2 = blockExist(blocks, trajectory2, _tpt);
						System.out.println("Bloques detectados: "+ blockDetected1 +" "+ blockDetected2);
						if(!blockDetected1) {
							releasePoint = releasePointTemp1;
						}else if(!blockDetected2) {
							releasePoint = releasePointTemp2;
						}else {
							if (randomGenerator.nextInt(6) < 2)
								releasePoint = pts.get(1);
							else
								releasePoint = pts.get(0);
						}
					}
					else
						if(pts.isEmpty())
						{
							System.out.println("No release point found for the target");
							System.out.println("Try a shot with 45 degree");
							releasePoint = tp.findReleasePoint(sling, Math.PI/4);
						}
					
					// Get the reference point
					Point refPoint = tp.getReferencePoint(sling);
					
					//Calculate the tapping time according the bird type 
					if (releasePoint != null) {
						double releaseAngle = tp.getReleaseAngle(sling,
								releasePoint);
						System.out.println("Release Point: " + releasePoint);
						System.out.println("Release Angle: "
								+ Math.toDegrees(releaseAngle));
						int tapInterval = 0;
						switch (aRobot.getBirdTypeOnSling()) 
						{

						case RedBird:
							tapInterval = 0; break;               // start of trajectory
						case YellowBird:
							tapInterval = 65 + randomGenerator.nextInt(25);break; // 65-90% of the way
						case WhiteBird:
							tapInterval =  70 + randomGenerator.nextInt(20);break; // 70-90% of the way
						case BlackBird:
							tapInterval =  70 + randomGenerator.nextInt(20);break; // 70-90% of the way
						case BlueBird:
							tapInterval =  65 + randomGenerator.nextInt(20);break; // 65-85% of the way
						default:
							tapInterval =  60;
						}

						int tapTime = tp.getTapTime(sling, releasePoint, _tpt, tapInterval);
						dx = (int)releasePoint.getX() - refPoint.x;
						dy = (int)releasePoint.getY() - refPoint.y;
						shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
					}
					else
						{
							System.err.println("No Release Point Found");
							return state;
						}
				}

				{
					ActionRobot.fullyZoomOut();
					screenshot = ActionRobot.doScreenShot();
					vision = new Vision(screenshot);
					Rectangle _sling = vision.findSlingshotMBR();
					if(_sling != null)
					{
						double scale_diff = Math.pow((sling.width - _sling.width),2) +  Math.pow((sling.height - _sling.height),2);
						if(scale_diff < 25)
						{
							if(dx < 0)
							{
								aRobot.cshoot(shot);
								state = aRobot.getState();
								if ( state == GameState.PLAYING )
								{
									screenshot = ActionRobot.doScreenShot();
									vision = new Vision(screenshot);
									List<Point> traj = vision.findTrajPoints();
									tp.adjustTrajectory(traj, sling, releasePoint);
									firstShot = false;
								}
							}
						}
						else
							System.out.println("Scale is changed, can not execute the shot, will re-segement the image");
					}
					else
						System.out.println("no sling detected, can not execute the shot, will re-segement the image");
				}

			}

		}
		return state;
	}
	
	
	/* Busca el cerdo del centro segun su coordenada Y */
	public int middlePosition(ArrayList<Integer> positions) {
		int pos = 0;
		int mid = 0;
		
		ArrayList<Integer> orderList = positions;
		Collections.sort(orderList);
		for (Integer in : orderList) {
		    System.out.println(in);
		}
		pos = (int) orderList.size() / 2;
		
		for(int i=0; i < positions.size(); i++) {
			if(positions.get(i) == orderList.get(pos)) {
				mid = pos;
			}
		}
		System.out.println(mid);
		return mid;
	}
	
	/* Devuelve true cuando encuentra un obstaculo en la trayectoria */
	public boolean blockExist(List<ABObject> blocks, List<Point> trajectory, Point target) {
		boolean into = false;
		for(int i = 0; i < blocks.size(); i++) {
			for(int j = 0; j < trajectory.size(); j++) {
				into = pointIntoObject(blocks.get(i), trajectory.get(j));
				if(into && trajectory.get(j).x <= target.x && trajectory.get(j).y <= target.y) {
					System.out.println("Obstaculo detectado!!!" + blocks.get(i) +""+ trajectory.get(j));
					return true;
				}
			}
		}
		return into;
	}
	
	/* Devuelve true cuando un punto esta dentro de un objeto */
	public boolean pointIntoObject(ABObject object, Point point) {
		int xO = object.x;
		int yO = object.y;
		int pX = point.x;
		int pY = point.y;
		if(pX >= xO && pX <= xO + object.width && pY >= yO && pY <= yO + object.width) {
			return true;
		}else {
			return false;
		}
		
	}

	public static void main(String args[]) {

		HeuristicAgent ha = new HeuristicAgent();
		if (args.length > 0)
			ha.currentLevel = Integer.parseInt(args[0]);
		ha.run();

	}
}
