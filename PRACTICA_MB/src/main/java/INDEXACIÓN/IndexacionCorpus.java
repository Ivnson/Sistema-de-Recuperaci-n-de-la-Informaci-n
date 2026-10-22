package INDEXACIÓN;

import java.io.*;

import java.util.*;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.QueryingApi.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument; 

public class IndexacionCorpus {

	public static void main(String[] args) throws IOException, SolrServerException {

		try (SolrClient cliente = new HttpSolrClient.Builder("http://localhost:8983/solr").build()) {
			System.out.println("LEYENDO EL CORPUS\n");

			leerFicheroMedAll(cliente);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	private static void leerFicheroMedAll(SolrClient cliente) {
		String fileName = "C:\\Users\\ivan\\Desktop\\apache solar\\MED.ALL";

		String idActual = null;
		Boolean leyendoTexto = false;
		StringBuilder texto = new StringBuilder();

		Scanner scan;
		try {
			scan = new Scanner(new File(fileName));

			while (scan.hasNextLine()) {
				String line = scan.nextLine();

				if (line.startsWith(".I") == true) {
					if (idActual != null) {
						indexar(cliente, idActual, texto.toString().trim());
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
				indexar(cliente, idActual, texto.toString().trim());
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void indexar(SolrClient cliente, String idActual, String texto) {
		System.out.println(texto);

		String titulo = "";
		String resumen = "";

		int primerPunto = texto.indexOf('.');

		if (primerPunto == -1) {
			titulo = texto;
		} else {
			titulo = texto.substring(0, primerPunto + 1).trim();
			resumen = texto.substring(primerPunto + 1).trim();
		}

		final SolrInputDocument doc = new SolrInputDocument();
		doc.addField("id", idActual);
		doc.addField("titulo", titulo);
		doc.addField("resumen", resumen);

		// Limpiar espacios dobles o múltiples
		// String textoLimpio = texto.replaceAll("\\s+", " ").trim();
		// doc.addField("text", textoLimpio);
		try {
			final UpdateResponse updateResponse = cliente.add("pruebas", doc);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Indexed documents must be committed
		try {
			cliente.commit("pruebas");
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("INDEXADO");
	}
}
