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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.utils.StateUtil;
import ab.vision.ABObject;
import ab.vision.GameStateExtractor.GameState;
import ab.vision.Vision;
import genetic.resources.Archivo;
import genetic.resources.Cromosoma;

public class GeneticAgent implements Runnable {

	private ActionRobot aRobot;
	private Random randomGenerator;
	public int currentLevel = 13;
	public static int time_limit = 12;
	private Map<Integer,Integer> scores = new LinkedHashMap<Integer,Integer>();
	TrajectoryPlanner tp;
	private boolean firstShot;
	private Cromosoma generaciones = new Cromosoma();
	private Vector<Cromosoma> poblacion = new Vector<Cromosoma>(0,1);
	private Archivo file = new Archivo();
	private String linea = "";
	private int generacion = 0; // default = 0
	private int seleccion = 24; // default = 1/2 de la poblacion inicial
	private String tipoGeneracion = "poblacionInicial";
//	private Point prevTarget;
	// a standalone implementation of the Naive Agent
	public GeneticAgent() {
		
		aRobot = new ActionRobot();
		tp = new TrajectoryPlanner();
//		prevTarget = null;
//		firstShot = true;
		randomGenerator = new Random();
		// --- go to the Poached Eggs episode level selection page ---
		ActionRobot.GoFromMainMenuToLevelSelection();

	}

	
	// run the client
	public void run() {

		aRobot.loadLevel(currentLevel);
		while (true) {
			tipoGeneracion = (generacion==0)? "poblacionInicial" : "generacion "+generacion;
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
				System.out.println("SCORE: " + score);
				int totalScore = 0;
				int currentScore = 0;
				for(Integer key: scores.keySet()){

					currentScore = scores.get(key);
					totalScore += scores.get(key);
					System.out.println(" Level " + key
							+ " Score: " + scores.get(key) + " ");
				}
				file.escritura(tipoGeneracion + "_resultados", " "+ score +"\n");
				poblacion = new Vector<Cromosoma>(0,1);
				System.out.println("Total Score: " + totalScore);
				//aRobot.loadLevel(++currentLevel);
				aRobot.loadLevel(currentLevel);
				// make a new trajectory planner whenever a new level is entered
				tp = new TrajectoryPlanner();

				// first shot on this level, try high shot first
				firstShot = true;
			} else if (state == GameState.LOST) {
				int score = StateUtil.getScore(ActionRobot.proxy);
				if(!scores.containsKey(currentLevel))
					scores.put(currentLevel, score);
				else
				{
					if(scores.get(currentLevel) < score)
						scores.put(currentLevel, score);
				}
				System.out.println("SCORE LOST: " + score);
				file.escritura(tipoGeneracion + "_resultados", " 0" + "\n");
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

	public GameState solve()
	{
		System.out.println("SOLVEEEE " + "Seleccion: " + seleccion + " Generacion: " + generacion);
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
 		// get all the birds, la version MBR no devuelve la cantidad real de aves
 		List<ABObject> birds = vision.findBirdsRealShape();
 		//System.out.println("BIRDS: " + birds);
 		
		GameState state = aRobot.getState();

		// if there is a sling, then play, otherwise just skip.
		if (sling != null) {

			if (!pigs.isEmpty()) {

				Point releasePoint = new Point();
				Shot shot = new Shot();
				int dx,dy;
				
				// Si el archivo existe se ejecuta solo este, el cual contiene la solucion
				boolean concluido = file.existe("final");
				if(concluido && poblacion.size()==0) {
					System.out.println("EJECUTANDO ARCHIVO FINAL");
					String cromosomaFinalS = file.lineaEspecifica("final", 1);
					Vector<Cromosoma> cromosomaFinalO = generaciones.construirCromosoma(cromosomaFinalS);
					poblacion = cromosomaFinalO;
				}
				
				System.out.println("CONCLUIDO: " + concluido);
				// No se crearán mas generaciones, se elige el mejor lanzamiento de la ultima generacion evaluada
				if(seleccion == 2 && !concluido && !file.verificarArchivo(tipoGeneracion) && poblacion.size()==0) {
					System.out.println("CREANDO ARCHIVO FINAL");
					file.leerResultados("generacion " + (generacion) + "_resultados");
					String masApto = file.lineaEspecifica("generacion " + (generacion) + "_resultados_Or", 1);
					String cromosomaFinalS = generaciones.obtenerCromosoma(masApto);
					file.escritura("final", cromosomaFinalS);
					Vector<Cromosoma> cromosomaFinalO = generaciones.construirCromosoma(cromosomaFinalS);
					poblacion = cromosomaFinalO;
				}
				
				System.out.println("BIRDS: " + birds.size());
				// Se genera la poblacion inicial si no ha sido generada
				if(!file.existe("poblacionInicial") && generacion == 0) {
					generaciones.generarPoblacionInicial(birds.size());
					
				}
				
				if(!file.verificarArchivo(tipoGeneracion) && poblacion.size() == 0) { // Se crea la nueva generacion
					System.out.println("CREANDO GENERACION");
					generacion ++;
					System.out.println("generacion " + generacion);
					generaciones.crearGeneracionSig(generacion, seleccion);
					// seleccion -=4;
					seleccion = generaciones.ajustarSeleccion(seleccion);
					System.out.println("GENERACION CON SELECCION ="+seleccion);
				}
							
				
				if(poblacion.size()==0) {
					tipoGeneracion = (generacion==0)? "poblacionInicial" : "generacion " + generacion;
					linea = file.lecturaLinea(tipoGeneracion);
					System.out.println("LINEA: "+linea);
					String[] cromosomas = linea.split(" ");
					System.out.println("strCromosoma: ");
					for (String cromosoma : cromosomas) {
						System.out.println(cromosoma);
						String[] genes = cromosoma.split(",");
						int ldx = Integer.parseInt(genes[0]);
						int ldy = Integer.parseInt(genes[1]);
						int ltT = Integer.parseInt(genes[2]);
						Cromosoma nuevo = new Cromosoma(ldx, ldy, ltT);
						poblacion.add(nuevo);				   
					}
				}
				
				{							
					// Get the reference point
					Point refPoint = tp.getReferencePoint(sling);


					//Calculate the tapping time according the bird type 
					if (releasePoint != null) {				
						
//						System.out.println("Poblacion: " + poblacion);
						int tapTime = poblacion.get(0).getTapTime(); //tp.getTapTime(sling, releasePoint, _tpt, tapInterval);
						dx = poblacion.get(0).getDx();
						dy = poblacion.get(0).getDy();
						birds = vision.findBirdsRealShape();
						System.out.println("SIZE POBLACION "+ poblacion.size());
						System.out.println("Lanzamiento: " + dx + ", " + dy + ", " + tapTime);
						shot = new Shot(refPoint.x, refPoint.y, dx, dy, 0, tapTime);
						
					}
					else
						{
							System.err.println("No Release Point Found");
							return state;
						}
				}

				// check whether the slingshot is changed. the change of the slingshot indicates a change in the scale.
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
								System.out.println("SHOT: " + shot);
								aRobot.cshoot(shot);
								poblacion.remove(0);
								state = aRobot.getState();
								if ( state == GameState.PLAYING )
								{
									screenshot = ActionRobot.doScreenShot();
									vision = new Vision(screenshot);
//									List<Point> traj = vision.findTrajPoints();
//									tp.adjustTrajectory(traj, sling, releasePoint);
//									firstShot = false;
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
		System.out.println("ENDSOLVEEEE: " + state);
		return state;
	}

	public static void main(String args[]) {

		GeneticAgent ga = new GeneticAgent();
		if (args.length > 0)
			ga.currentLevel = Integer.parseInt(args[0]);
		ga.run();

	}
}
