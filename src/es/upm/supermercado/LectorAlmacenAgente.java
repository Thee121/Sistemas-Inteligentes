package es.upm.supermercado;

import jade.core.Agent;

public class LectorAlmacenAgente extends Agent {
	private static final long serialVersionUID = 4395092232132395178L;

	public void setup() {
		Object[] listaparametros = getArguments();
		if ((listaparametros == null) || (listaparametros.length < 1)) {
			System.out.println("No se han introducido parametros");
		} else {
			System.out.println("Agente JADE con Parametros: Soy el agente " + getLocalName());
			int i;
			for (i = 0; i < listaparametros.length; i++) {
				System.out.println("Parametro " + i + " es: " + (String) listaparametros[i]);
			}
			doDelete();
		}
	}

	protected void takeDown() {
		System.out.println("goodbye");
	}
	// Implementación de Comportamientos
}
