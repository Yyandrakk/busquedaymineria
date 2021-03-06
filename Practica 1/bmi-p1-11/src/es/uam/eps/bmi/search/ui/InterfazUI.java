
package es.uam.eps.bmi.search.ui;

import es.uam.eps.bmi.search.SearchEngine;
import es.uam.eps.bmi.search.index.lucene.LuceneIndex;
import es.uam.eps.bmi.search.ranking.SearchRanking;
import es.uam.eps.bmi.search.ranking.SearchRankingDoc;
import es.uam.eps.bmi.search.vsm.VSMEngine;

import java.io.IOException;
import java.util.Scanner;

/** Clase que sirve de interfaz para probar VSMEngine
 *
 * @version 1.0
 * @author jorge
 * @author oscar
 */
public class InterfazUI{

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_PURPLE = "\u001B[35m";

    /**
     * Main que permite ejecutar la interfaz
     * @param a None
     * @throws IOException
     */
    public static void main (String a[]) throws IOException {

        boolean finalizar=true,salir=true;
        int id=-1,opcion=-1;
        String ruta=null;
        SearchEngine engine = null;
        Scanner s = new Scanner(System.in);

        System.out.println("Bienvenido:\n");
        do{
            do {
                System.out.println("Opciones de indice:\n");
                for (IndexSource r : IndexSource.values()) {
                    System.out.println("Id: "+ANSI_RED + r.ordinal() +ANSI_RESET+ " Nombre: " + r);
                }
                System.out.print("Introduzca "+ANSI_RED+"id"+ANSI_RESET+": ");
                id = getNumber();
                ruta = IndexSource.routeIndex(id);
            }while (ruta==null);
            engine =new VSMEngine(new LuceneIndex(ruta));
            salir = true;
            do{

                System.out.println("Accion a realizar:\n");
                System.out.println("Opcion "+ANSI_PURPLE+"1"+ANSI_RESET+": Insertar query");
                System.out.println("Opcion "+ANSI_PURPLE+"2"+ANSI_RESET+": Cambiar indice");
                System.out.println("Opcion "+ANSI_PURPLE+"3"+ANSI_RESET+": Salir\n");
                System.out.print("Introduzca opcion("+ANSI_PURPLE+"numero"+ANSI_RESET+"): ");
                opcion = getNumber();
                switch (opcion){
                    case 1:
                        System.out.print("\nQuery, separar por espacios las distintas palabras: ");
                        String query = s.nextLine();
                        busqueda(engine,query.replaceAll("[^a-zA-Z0-9\\s]",""));
                        break;
                    case 2:
                        salir = false;
                        break;
                    case 3:
                        finalizar = salir = false;


                }
            }while (salir);

        }while(finalizar);

        s.close();


    }

    /**
     *
     * @param engine Searchengine para hacer la busqueda
     * @param query Expresion a buscar
     * @throws IOException
     */
    private static void busqueda(SearchEngine engine, String query) throws IOException {
        System.out.println(" \nQuery procesada:'" + query.toLowerCase().trim() + "'");
        SearchRanking ranking = engine.search(query.toLowerCase().trim(), 10);
        if(ranking.size()>0){
            for (SearchRankingDoc result : ranking)
                System.out.println("\t" + "Score: "+ANSI_GREEN +result.getScore()+ANSI_RESET+ " Ruta: "+result.getPath());
            System.out.println("\n");
        }else{
            System.out.println("Sin resultados");
        }

    }

    /**
     * Obtiene un numero por teclado
     * @return mumero
     */
    private static int getNumber(){
        Scanner s = new Scanner(System.in);
        do {
            if (s.hasNext()) {
                if (s.hasNextInt()) {
                    return s.nextInt();
                }
                s.next();
            } else {
                System.out.println("EOF");
                return -1;
            }
        } while (true);
    }
}
