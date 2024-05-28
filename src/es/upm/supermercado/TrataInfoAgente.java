package es.upm.supermercado;

import java.util.Map;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class TrataInfoAgente extends Agent {
	private static final long serialVersionUID = -5513148827856003070L;
	private static Map<String, Integer> inventario;

	public void setup() {
		System.out.println("Agente JADE con Parametros. Inicializado el agente: " + getLocalName());
		addBehaviour(new CyclicBehaviour() {
			private static final long serialVersionUID = 9090607020824006811L;

			@SuppressWarnings("unchecked")
			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					try {
						setInventario((Map<String, Integer>) msg.getContentObject());
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				} else {
					block();
				}
			}
		});
	}

	protected void takeDown() {
		System.out.println("Apagando Agente" + getLocalName());
	}

	public static Map<String, Integer> getInventario() {
		return inventario;
	}

	public static void setInventario(Map<String, Integer> inventario) {
		TrataInfoAgente.inventario = inventario;
	}

}
