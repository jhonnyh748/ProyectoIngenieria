package genetic.resources;

import java.util.Arrays;
import java.util.Random;
import java.util.Vector;
import genetic.resources.Archivo;

public class Cromosoma {
	private int dx;
	private int dy;
	private int tapTime;
	private Archivo file = new Archivo();
	
	public Cromosoma(int dx, int dy, int tapTime) {
		this.dx = dx;
		this.dy = dy;
		this.tapTime = tapTime;
	}
	
	public Cromosoma() {
		
	}
	
	public int generarAleatorio(int min, int max) {
		Random random = new Random();
		int num = random.nextInt(max - min + 1) + min;
		return num;
	}
	
	public Vector<Cromosoma> poblacionInicial(int lanzamientos) {
		Vector<Cromosoma> poblacion = new Vector<Cromosoma>(0,1);
		for (int i=0; i<lanzamientos; i++) {
			//System.out.println("PoblacionInicial" + i + " " + lanzamientos);
			int dx = generarAleatorio(-35,-5);
			int dy = generarAleatorio(5,35);
			int tapTime = generarAleatorio(1,30) * 100;
			poblacion.add(new Cromosoma(dx, dy, tapTime));
		}
		//System.out.println(poblacion + "inicial");
		return poblacion;		
	}
	
	public void generarPoblacionInicial(int lanzamientos) {
		System.out.println("GENERANDO POBLACION INICIAL");
		for (int i=0; i<50; i++) {
			System.out.println("PoblacionInicial" + i + " " + lanzamientos);
			for (int j=0; j<lanzamientos; j++) {
				int dx = generarAleatorio(-35,-10);
				int dy = generarAleatorio(10,35);
				int tapTime = generarAleatorio(1,30) * 100;
				//poblacion.add(new Cromosoma(dx, dy, tapTime));
				file.escritura("poblacionInicial", dx + "," + dy + "," + tapTime + " ");
			}
			file.escritura("poblacionInicial", "\n");
		}
	}
	
	public Cromosoma reproducir(Cromosoma c1, Cromosoma c2) {
		int hijoDx = (c1.getDx() + c2.getDx()) / 2;
		int hijoDy = (c1.getDy() + c2.getDy()) / 2;
		int hijoTt = (c1.getTapTime() + c2.getTapTime()) / 2;
		Cromosoma hijo = new Cromosoma(hijoDx, hijoDy, hijoTt);
//		System.out.println("Reproduccion");
		return hijo;
	}
	
	/*La mutacion esta dada por una variacion minima en las coordenadas de lanzamiento*/
	public Cromosoma mutar(Cromosoma original) {
		int dxO = original.getDx();
		int dyO = original.getDy();
		int tTO = original.getDy();
		
		int mutacionDx = generarAleatorio(-2,2); // Se considera el 0, es decir que no mutaria
		int mutacionDy = generarAleatorio(-2,2);
		int mutacionTapT = generarAleatorio(-200,200); // Puede sumar o restar 200 milisegundos al tapTime
		
		int dxM = dxO + mutacionDx;
		int dyM = dyO + mutacionDy;
		int tTM = tTO + mutacionTapT;
		
		// casos especiales, si dx o dy son , el lanzamiento no se realiza
		if(tTM < 100) tTM = 100;
		if(dxM > -10) dxM = -10;
		if(dyM < 10) dyM = 10;
		Cromosoma mutado = new Cromosoma(dxM, dyM, tTM);
		
		return mutado;
	}
	
	public int ajustarSeleccion(int mejores) {
		int seleccion = mejores / 2;
		if(seleccion %2 != 0) {
			seleccion -=1;
		}
		if(seleccion == 0) seleccion = 1;
		System.out.println("SELECCION: " + seleccion);
		return seleccion;
	}
	
	// De la seleccion realizada, se obtendran el 70% que corresponde a los mejores, los restantes seran elejidos de manera aleatoria
	public int calcularPorcentaje(int seleccion) {
		int mejores = 0;
		if(seleccion > 20) {
			mejores = (int)(seleccion * 0.7);
			if(mejores % 2 != 0) mejores += 1;
		}else {
			mejores = seleccion;
		}
		System.out.println("CACULAR MEJORES: " + mejores);
		return mejores;
	}
	
	// Obtengo un arreglo de posiciones diferentes entre si y mayores a mejores
	public int[] seleccionAleatoria(int seleccion, int mejores, int numLineas) {
		int totalAleatorios = seleccion - mejores;
		int[] posiciones = new int[totalAleatorios];
		int index = 0;
		while(index < totalAleatorios) {
			int aleatorio = generarAleatorio(mejores + 1, numLineas);
			if(!numEnArray(posiciones, aleatorio)) {
				posiciones[index] = aleatorio;
				index += 1;
			}
		}
		System.out.println("ALEATORIOS: " + posiciones.toString());
		return posiciones;
	}
	
	// Retorna true si un numero buscado se encuentra en el arreglo
	public boolean numEnArray(int[] aleatorios, int numB) {
		boolean existe = false;
		for(int i=0; i< aleatorios.length; i++) {
			if(aleatorios[i] == numB) {
				existe = true;
			}
		}		
		return false;
	}

	// Crea la generacion siguiente con los n mejores cromosomas y algunos aleatorios diferentes
	public void crearGeneracionSig(int generacion, int seleccion) {
		String resultados = (generacion-1 == 0)? "poblacionInicial_resultados" : "generacion " + (generacion-1) + "_resultados";
		//boolean contenido = file.verificarArchivo(resultados);
		//System.out.println("resultados " + resultados);
		int cantidadLineas = file.contarLineas(resultados);
		int mejores = calcularPorcentaje(seleccion);
		int [] aleatorios = seleccionAleatoria(seleccion, mejores, cantidadLineas);
		System.out.println("DISTRUBUCION: Mejores " + mejores + " Aleatorios " + aleatorios.toString());
		file.leerResultados(resultados);
		int numeroLinea = 1;
		String linea1 = file.lineaEspecifica(resultados+"_Or", numeroLinea);
		String linea2 = file.lineaEspecifica(resultados+"_Or", numeroLinea + 1);
		
		// Seleccion del 70% de los mejores
		while(linea1 != null && numeroLinea <= mejores) {
			String newLinea1 = obtenerCromosoma(linea1);
			String newLinea2 = obtenerCromosoma(linea2);
			Vector<Cromosoma> c1 = construirCromosoma(newLinea1);
			Vector<Cromosoma> c2 = construirCromosoma(newLinea2);
			Vector<Vector<Cromosoma>> reproducir_mutar = reproducir_mutarCromosomas(c1, c2);
			String rep_mutar_toString = vectorToString(reproducir_mutar);
			
			file.escritura("generacion " + generacion, rep_mutar_toString);
			numeroLinea += 2;
			linea1 = file.lineaEspecifica(resultados+"_Or", numeroLinea);
			linea2 = file.lineaEspecifica(resultados+"_Or", numeroLinea + 1);
			//System.out.println(newLinea);
		}
		System.out.println("CREANDO ALEATORIOS");
		// Seleccion del 30% aleatorio
		for(int i=0; i<aleatorios.length-1; i++) {
			String lineaAleatoria1 = file.lineaEspecifica(resultados+"_Or", aleatorios[i]);
			String lineaAleatoria2 = file.lineaEspecifica(resultados+"_Or", aleatorios[i+1]);
			String newLineaAl1 = obtenerCromosoma(lineaAleatoria1);
			String newLineaAl2 = obtenerCromosoma(lineaAleatoria2);
			
			Vector<Cromosoma> c1 = construirCromosoma(newLineaAl1);
			Vector<Cromosoma> c2 = construirCromosoma(newLineaAl2);
			Vector<Vector<Cromosoma>> reproducir_mutar = reproducir_mutarCromosomas(c1, c2);
			String rep_mutar_toString = vectorToString(reproducir_mutar);
			
			file.escritura("generacion " + generacion, rep_mutar_toString);
		}		
	}
	
	// Toma la linea del archivo de resultados y elimina el score
	public String obtenerCromosoma(String linea) {
		System.out.println("Obtener cromosoma");
		String[] cromosomas = linea.split(" ");
		String[] newCromosomas = new String[cromosomas.length-1];
		System.arraycopy(cromosomas, 0, newCromosomas, 0, newCromosomas.length);
		
		String lineaSinScore = "";
		for (String cromosoma : newCromosomas) {
			lineaSinScore += cromosoma + " ";
		}
		//System.out.println(lineaSinScore);
		return lineaSinScore;
	}
	
	// Transforma una linea del archivo leido en un vector de cromosomas
	public Vector<Cromosoma> construirCromosoma(String linea) {
		Vector<Cromosoma> poblacion = new Vector<Cromosoma>(0,1);
		String[] cromosomas = linea.split(" ");
		for (String cromosoma : cromosomas) {
			// System.out.println(cromosoma);
			String[] genes = cromosoma.split(",");
			int ldx = Integer.parseInt(genes[0]);
			int ldy = Integer.parseInt(genes[1]);
			int ltT = Integer.parseInt(genes[2]);
			Cromosoma nuevo = new Cromosoma(ldx, ldy, ltT);
			poblacion.add(nuevo);				   
		}
		return poblacion;
	}
	
	// Toma 2 vectores de cromosomas y crea un hijo por herencia por cada par de padres y dos hijos mutados
	public Vector<Vector<Cromosoma>> reproducir_mutarCromosomas(Vector<Cromosoma> cromosomas1, Vector<Cromosoma> cromosomas2) {
		Vector<Cromosoma> reproduccion = new Vector<Cromosoma>(0,1);
		Vector<Cromosoma> mutacion1 = new Vector<Cromosoma>(0,1);
		Vector<Cromosoma> mutacion2 = new Vector<Cromosoma>(0,1);
		Vector<Vector<Cromosoma>> rep_mut = new Vector<Vector<Cromosoma>>(0,1);
		
		for(int i=0; i<cromosomas1.size(); i++) {
			Cromosoma p1 = cromosomas1.get(i);
			Cromosoma p2 = cromosomas2.get(i);
			Cromosoma hijo = reproducir(p1, p2);
			Cromosoma mutado1 = mutar(p1);
			Cromosoma mutado2 = mutar(p2);
			reproduccion.add(hijo);
			mutacion1.add(mutado1);
			mutacion2.add(mutado2);
		}
		rep_mut.add(reproduccion);
		rep_mut.add(mutacion1);
		rep_mut.add(mutacion2);
//		System.out.println("Reproducir " + reproduccion);
//		System.out.println("Mutar " + mutacion1);
//		System.out.println("Mutar " + mutacion2);
		System.out.println("Repoducir_Mutar " + rep_mut);
		return rep_mut;
	}
	
	/* Convierte un vector de vector de cromosoma a una cadena que pueda ser escrita en el archivo de la nueva generacion */
	public String vectorToString(Vector<Vector<Cromosoma>> reproducion_mutacion) {
		String resp = "";
		for(int i=0; i < reproducion_mutacion.size(); i++) {
			Vector<Cromosoma> vc = reproducion_mutacion.get(i);
			for(int j=0; j< vc.size(); j++) {
				Cromosoma cr = vc.get(j);
				int dx = cr.getDx();
				int dy = cr.getDy();
				int tp = cr.getTapTime();
				resp += "" + dx + "," + dy + "," + tp + " ";
			}
			resp += "\n";
		}
		System.out.println("RESPUESTA: " + resp);
		return resp;
	}
	
	public int getDx() {
		return dx;
	}

	public void setDx(int dx) {
		this.dx = dx;
	}

	public int getDy() {
		return dy;
	}

	public void setDy(int dy) {
		this.dy = dy;
	}

	public int getTapTime() {
		return tapTime;
	}

	public void setTapTime(int tapTime) {
		this.tapTime = tapTime;
	}
	
	
	
	
	
}
