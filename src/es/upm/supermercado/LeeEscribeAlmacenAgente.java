package es.upm.supermercado;

import jade.core.Agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class LeeEscribeAlmacenAgente extends Agent {
	private static final long serialVersionUID = 4395092232132395178L;
	private Map<String, Integer> almacen = new HashMap<String, Integer>();;

	public void setup() {
		String line;
		String cvsSplitBy = ",";
		Object[] listaparametros = getArguments();
		if ((listaparametros == null) || (listaparametros.length < 1)) {
			System.out.println("No se han introducido parametros");
		} else {
			System.out.println("Agente JADE con Parametros: Soy el agente " + getLocalName());
			String path = new File("").getAbsolutePath();
			String directorio = (String)listaparametros[0];
			path = path + directorio;
	        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
	            br.readLine();

	            while ((line = br.readLine()) != null) {
	                // Separar la línea por comas
	                String[] item = line.split(cvsSplitBy);

	                // Obtener el nombre del elemento y la cantidad
	                String elemento = item[1];
	                int cantidad = Integer.parseInt(item[2]);

	                // Guardar en el mapa
	                almacen.put(elemento, cantidad);
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        System.out.println(almacen.toString());
			//doDelete();
		}
	}
	
	//Solo se utiliza al llamar al método doDelete();
	protected void takeDown() {
		System.out.println("Apagando Agente LeeEscribeAlmacen");
	}
}
