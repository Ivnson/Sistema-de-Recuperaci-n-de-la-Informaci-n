package INDEXACIÓN;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.QueryingApi.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

public class CONSULTA {

	public static void main(String[] args) throws SolrServerException, IOException {

		HttpSolrClient consulta = new HttpSolrClient.Builder("http://localhost:8983/solr/pruebas").build();
		
		String ficheroSalida = "C:\\Users\\ivan\\Desktop\\apache solar\\RESULTADO.txt" ; 
		
		try (BufferedWriter escribir = new BufferedWriter(new FileWriter(ficheroSalida))){
			leerFicheroMedQry(consulta,escribir) ; 
		} catch (Exception e) {
			e.printStackTrace() ;
		}

		consulta.close() ;  
	}
	
	private static void leerFicheroMedQry(HttpSolrClient consulta, BufferedWriter escribir) {
		String fileName = "C:\\Users\\ivan\\Desktop\\apache solar\\MED.QRY";

		String idActual = null;
		Boolean leyendoTexto = false;
		StringBuilder texto = new StringBuilder(); 
		String palabras = null ; 
		
		Scanner scan ; 
		
		try {
			scan = new Scanner(new File(fileName));
			
			while (scan.hasNextLine()) {
				String line = scan.nextLine();

				if (line.startsWith(".I") == true) {
					if (idActual != null) {
						
						//CREAR LA QUERY EN UN METODO 
						palabras  = obtener5PrimerasPalabras(texto.toString().trim()); 
						
						hacerConsulta(consulta, palabras, escribir, idActual);
					}

					leyendoTexto = false;
					texto.setLength(0);
					idActual = line.substring(2).trim();
					System.out.println(idActual);
				} else if (line.startsWith(".W") == true) {
					leyendoTexto = true;
				} else if (leyendoTexto == true) {
					// texto.append(line) ;
					// texto.append("\n") ;
					texto.append(line.trim()).append(" ");
					
				}
			}
			if (idActual != null) {
				palabras = obtener5PrimerasPalabras(texto.toString().trim());
				hacerConsulta(consulta, palabras, escribir, idActual);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String obtener5PrimerasPalabras(String texto)
	{
		String textoLimpio = texto.replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s]", " ").trim();
		String[] palabras = textoLimpio.split("\\s+", 6) ; 
		 
		
		int numRealdePalabras = Math.min(palabras.length,5) ; 
		
		StringBuilder resultado = new StringBuilder() ; 
		for (int i = 0; i < numRealdePalabras; i++) {
			resultado.append(palabras[i]) ; 
			resultado.append(" ") ; 
		}
		
		System.out.println(resultado.toString().trim()) ; 
		
		return resultado.toString().trim() ; 
	}
	
	private static void hacerConsulta (HttpSolrClient consulta, String palabras, BufferedWriter escribir, String idActual)
	{
		SolrQuery query = new SolrQuery() ;

		query.setQuery(palabras);
		query.set("defType", "edismax");
		
		//APLICANDO PESOS EN DISTINTOS CAMPOS -> TITULO VALE MAS QUE RESUMEN 
		query.set("qf", "titulo^2.0 resumen^1.0") ; 
		
		//BENEFICIAMOS EL SCORE SI LAS PALABRAS APARECEN JUNTAS 
		query.set("pf", "titulo^2.5 resumen^1.5") ; 
		
		//PERMITIMOS QUE HAYA 2 PALABRAS AJENAS DE POR MEDIO 
		query.set("ps", 2) ; 
		
		query.setFields("id","score") ; 
		query.setRows(50) ; 
		
		org.apache.solr.client.solrj.response.QueryResponse rsp = null;
		try {
			rsp = consulta.query(query);
			SolrDocumentList docs = rsp.getResults() ; 
			
			for (int i = 0; i < docs.size(); i++) {
			    String idDoc = (String) docs.get(i).getFieldValue("id");
			    Float score = (Float) docs.get(i).getFieldValue("score");

			    escribir.write(idActual + " Q0 " + idDoc + " " + (i+1) +
			    		" " + score + " USUARIO->IVAN\n");
			}
			
			escribir.write("\n------------------------------------\n\n");
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
