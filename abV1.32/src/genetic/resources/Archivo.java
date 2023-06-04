package genetic.resources;

import java.io.*;
import java.util.*;


public class Archivo {
	
	public Archivo() {
		
	}
	
	public void escritura(String generacion, String texto) {
		FileWriter fichero = null;
		PrintWriter pw = null;
		try
		{
			fichero = new FileWriter("src/" + generacion + ".txt", true);
			pw = new PrintWriter(fichero);
			pw.print(texto);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != fichero)
					fichero.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
	
	public void lecturaArchivo(String ruta) {
		File archivo = null;
		FileReader fr = null;
		BufferedReader br = null;

		try {
			// Apertura del fichero y creacion de BufferedReader para poder
			// hacer una lectura comoda (disponer del metodo readLine()).
			archivo = new File ("/" + ruta + ".txt");
			fr = new FileReader (archivo);
			br = new BufferedReader(fr);

			// Lectura del fichero
			String linea;
			while((linea=br.readLine())!=null)
				System.out.println(linea);
		}
		catch(Exception e){
			e.printStackTrace();
		}finally{
			// En el finally cerramos el fichero, para asegurarnos
			// que se cierra tanto si todo va bien como si salta 
			// una excepcion.
			try{                    
				if( null != fr ){   
					fr.close();     
				}                  
			}catch (Exception e2){ 
				e2.printStackTrace();
			}
		}
	}
	
	public String lecturaLinea(String ruta) {
		File archivo = null;
		FileReader fr = null;
		BufferedReader br = null;
		String linea = "";
		try {
			// Apertura del fichero y creacion de BufferedReader para poder
			// hacer una lectura comoda (disponer del metodo readLine()).
			archivo = new File ("src/" + ruta + ".txt");
			fr = new FileReader (archivo);
			br = new BufferedReader(fr);

			// Lectura de linea			
			String lineaTemp = br.readLine();
			if(lineaTemp != null) {				
				System.out.println(lineaTemp);
				linea = lineaTemp;
			}
			
			// Escribe la linea en el archivo de resultados
			File resultados = new File ("src/" + ruta + "_resultados.txt");
	        FileWriter fws = new FileWriter(resultados, true);
	        BufferedWriter bws = new BufferedWriter(fws);	        
	        //bws.newLine();
	        bws.write(lineaTemp);
			
			// Se crea un archivo temporal para escribir el resto del archivo
			File archivoTemp = new File ("src/" + ruta + "_temp.txt");
	        FileWriter fw = new FileWriter(archivoTemp);
	        BufferedWriter bw = new BufferedWriter(fw);
	        
	        // Lee las líneas restantes del archivo y escribirlas en el archivo temporal
	        String lineaSig;
	        while ((lineaSig = br.readLine()) != null) {
	            bw.write(lineaSig);
	            bw.newLine();
	        }
	        
	        // Cierra el archivo original y el archivo temporal
	        fr.close();
	        bw.close();
	        bws.close();

	        // Borra el archivo original y renombra el archivo temporal como el archivo original
	        archivo.delete();
	        archivoTemp.renameTo(archivo);
			
		}
		catch(Exception e){
			e.printStackTrace();
		}finally{
			// En el finally cerramos el fichero, para asegurarnos
			// que se cierra tanto si todo va bien como si salta 
			// una excepcion.
			try{                    
				if( null != fr ){   
					fr.close();     
				}                  
			}catch (Exception e2){ 
				e2.printStackTrace();
			}
		}
		return linea;
	}
	
	// lee un archivo de resultados y lo ordena deacuerdo al score
	public void leerResultados(String ruta) {
		// Se lee el archivo y almacena cada línea en una lista
        List<String> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("src/" + ruta + ".txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                lineas.add(linea);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Se ordena la lista según el último elemento de cada línea
        Comparator<String> comparador = new Comparator<String>() {
            @Override
            public int compare(String linea1, String linea2) {
                String[] elementos1 = linea1.split(" ");
                String[] elementos2 = linea2.split(" ");
                int ultimoElemento1 = Integer.parseInt(elementos1[elementos1.length - 1]);
                int ultimoElemento2 = Integer.parseInt(elementos2[elementos2.length - 1]);
                return Integer.compare(ultimoElemento2, ultimoElemento1);
            }
        };
        Collections.sort(lineas, comparador);
        
        // Se escriben las líneas ordenadas en un nuevo archivo de texto
        try (PrintWriter pw = new PrintWriter(new FileWriter("src/" + ruta + "_Or" + ".txt"))) {
            for (String linea : lineas) {
                pw.println(linea);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	// lee una linea dada en un archivo de texto
	public String lineaEspecifica(String ruta, int numeroLinea) {
		File archivo = null;
		FileReader fr = null;
		BufferedReader br = null;
		String linea = "";
		try {
			// Apertura del fichero y creacion de BufferedReader para poder
			// hacer una lectura comoda (disponer del metodo readLine()).
			archivo = new File ("src/" + ruta + ".txt");
			fr = new FileReader (archivo);
			br = new BufferedReader(fr);

			// Lectura de linea			
			int numeroActual = 1;
	        while ((linea = br.readLine()) != null) {
	            if (numeroActual == numeroLinea) {
	                return linea;
	            }
	            numeroActual++;
	        }
	        	        
	        // Cierra el archivo original y el archivo temporal
	        fr.close();			
		}
		catch(Exception e){
			e.printStackTrace();
		}finally{
			// En el finally cerramos el fichero, para asegurarnos
			// que se cierra tanto si todo va bien como si salta 
			// una excepcion.
			try{                    
				if( null != fr ){   
					fr.close();     
				}                  
			}catch (Exception e2){ 
				e2.printStackTrace();
			}
		}
		return linea;
	}
	
	public boolean existe(String ruta) {
		File archivo = new File("src/" + ruta + ".txt");
        boolean existe = false;
        if(archivo.exists()) {
            existe = true;
        }
        return existe;
	}
	
	// Verifica si un archivo esta vacio o si tiene contenido
	public boolean verificarArchivo(String ruta) {
		boolean contenido = false;
		File archivo = new File("src/" + ruta + ".txt");
		if (archivo.length() == 0) {
			contenido = false;
			System.out.println("El archivo está vacío " + ruta);
		} else {
			contenido = true;
			System.out.println("El archivo tiene contenido " + ruta);
		}
		return contenido;
	}
	
	public int contarLineas(String ruta) {
		File archivo = new File("src/" + ruta + ".txt");
        int contadorLineas = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {

            String linea;

            while ((linea = br.readLine()) != null) {
                contadorLineas++;
            }

        } catch (IOException e) {
            System.err.format("Error de E/S: %s%n", e);
        }
        return contadorLineas;
	}

}
