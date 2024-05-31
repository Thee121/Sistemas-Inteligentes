package es.upm.supermercado;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LeeEscribeAlmacenAgente extends Agent {
	private static final long serialVersionUID = 4395092232132395178L;

	private static ConcurrentHashMap<String, Integer> inventario = new ConcurrentHashMap<String, Integer>();
	private static ConcurrentHashMap<Integer, String> historialPedidos = new ConcurrentHashMap<Integer, String>();

	String path = new File("").getAbsolutePath();

	public void setup() {
		System.out.println("Agente JADE con Parametros. Inicializado el agente: " + getLocalName());

		String directorioAlmacen = "\\src\\es\\upm\\resources\\Almacen.txt";
		String directorioHistorialPedidos = "\\src\\es\\upm\\resources\\HistorialPedidos.txt";
		String pathInvenario = path + directorioAlmacen;
		String pathHistorialPedidos = path + directorioHistorialPedidos;
		setInventario(Utils.LeeArchivoAlmacen(pathInvenario));
		setHistorialPedidos(Utils.LeeArchivoHistorial(pathHistorialPedidos));

		/* addBehaviour(new CyclicBehaviour() {
			private static final long serialVersionUID = 9090607020824006811L;
			
			@SuppressWarnings("unchecked")
			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					try {
						// Actualizar Inventario
						if (msg.getPerformative() == ACLMessage.PROPAGATE) {
							setInventario((ConcurrentHashMap<String, Integer>) msg.getContentObject());
						} else if (msg.getPerformative() == ACLMessage.REQUEST) {
							// Hacer cosas
						}
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				} else {
					block();
				}
			}
		});
		*/

		jade.wrapper.AgentContainer container = getContainerController();

		// Start the TrataInfoAgente
		try {
			AgentController infoController = container.createNewAgent("TrataInfoAgente", "es.upm.supermercado.TrataInfoAgente", null);
			infoController.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Utils.enviarMensaje(this,"Inicial", inventario);
	}

	protected void takeDown() {
		System.out.println("Apagando Agente LeeEscribeAlmacen");
	}

	public static Map<String, Integer> getInventario() {
		return LeeEscribeAlmacenAgente.inventario;
	}

	public static void setInventario(ConcurrentHashMap<String, Integer> almacen) {
		LeeEscribeAlmacenAgente.inventario = almacen;
	}

	public static Map<Integer, String> getHistorialPedidos() {
		return historialPedidos;
	}

	public static void setHistorialPedidos(ConcurrentHashMap<Integer, String> historialPedidos) {
		LeeEscribeAlmacenAgente.historialPedidos = historialPedidos;
	}
}
