# ProyectoIngenieria

## RESUMEN
En este trabajo de grado se inicia con la investigación de técnicas de inteligencia artificial (IA) que puedan ser usadas para la implementación de dos agentes inteligentes que puedan jugar y pasar con éxito los niveles del video juego Angry Birds Poached Eggs, luego se pretende realizar una comparación basada en las pruebas y puntajes obtenidos por cada agente durante la ejecución del juego y mostrar cuál de las dos técnicas de IA utilizadas es más apta.

La investigación inicial es la base para cumplir con los objetivos propuestos en este proyecto, ya que de ahí se realiza la selección de las técnicas de IA (heurísticas y algoritmos genéticos) que se utilizan para la realización del diseño y posterior implementación de los agentes en el lenguaje de programación java. Una vez realizada la implementación, se realizan las pruebas necesarias tomando nota sobre el comportamiento de los agentes y haciendo uso de los criterios de comparación definidos, se establecen conclusiones y trabajos futuros.

Según los resultados obtenidos en la fase de pruebas se pudo establecer que para los niveles para los cuales fueron probados los agentes de juego, el algoritmo heurístico tuvo una efectividad del 85.71% frente al algoritmo genético que fue del 100% , se debe tener en cuenta que este último es entrenado previamente para cada nivel antes de la realización de las pruebas y comparaciones.

Finalmente se determina que pese a que el algoritmo genético obtiene mejores puntajes, debido a que requiere un tiempo de entrenamiento aproximando de 6.02 horas para cada nivel, no sería realmente útil en una competencia como la de ai birds ya que en dicha competencia los tiempos son muy limitados.


A continuación se hace una descripción de la estructura de los y su contenido archivos.

  ### abV1.32: 
  Directorio principal del proyecto hecho en java. Su contenido se relaciona a continuación.

  * Directorio doc/ que contiene las guías de configuración incluidas en la versión del proyecto base proporcionada en la página de AIBirds, tanto del video juego en chromium como de las librerías del proyecto java, específicamente en el documento ABDoc.pdf.

  * Directorio src/ que contiene todos los archivos necesarios para la ejecución del proyecto y los agentes implementados; en la raíz de esta carpeta se incluyen carpetas renombradas como Level 1, Level 3… Level n, que contienen los documentos en formato txt correspondientes al entrenamiento del algoritmo genético para esos niveles y su solución.

    Para probar una solución es necesario extraer el documento “final.txt” de la carpeta que corresponda con el nivel que se va a probar y colocarlo en la raíz de src/. Además en la clase principal Heuristic Agent se debe configurar el nivel del juego que se va a ejecutar en la variable currentLevel.

    Si en el directorio src/ no se encuentra el archivo “final.txt” y se ejecuta el algoritmo genético, este comenzará a entrenar, sin embargo no debe haber más documentos txt en src/ para que el algoritmo entrene correctamente.

  * En la ruta src/genetic/resources/ se encuentran las clases java (Archivo y Cromosoma) utilizadas para la implementación y entrenamiento del algoritmo genético.

  * En la ruta src/ab/demo/ se encuentran las clases principales (HeuristicAgent y GeneticAgent) que ejecutan el agente según corresponda para iniciar los lanzamientos; el juego en Chromium ya debe estar en ejecución.



### Videos demostrativos: 
Contiene una serie de videos que evidencian el comportamiento de los agentes de juego implementados.

### Application Cache:
Un archivo en formato zip que contiene los archivos necesarios para la ejecución del video juego el el navegador Chromium; el contenido de esta carpeta debe ser extraído en la ruta del sistema C:/Users/NAMEUSER/AppData/Local/Chromium/User Data/Default/Application Cache/

### chrome-win32: 
Es la versión del navegador recomendada en http://aibirds.org/ para ejecutar el juego y que sea compatible con el proyecto. Descargable desde https://drive.google.com/file/d/10OUsS7X8h_xt-fH-WvnXgn1sgxpyRzSj/view?usp=sharing
