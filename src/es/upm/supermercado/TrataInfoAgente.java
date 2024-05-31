package es.upm.supermercado;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrataInfoAgente extends Agent {
	private static final long serialVersionUID = -5513148827856003070L;

	private static ConcurrentHashMap<String, Integer> inventario = new ConcurrentHashMap<String, Integer>();
	private static ConcurrentHashMap<Integer, String> historialPedidos = new ConcurrentHashMap<Integer, String>();

	static String rutaArchivo = "";

	public void setup() {
		// Crear servicios proporcionados por el agente y registrarlos en la plataforma
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName("InformacionInicial");
		// establezco el tipo del servicio “buscar” para poder localizarlo cuando haga
		// una busqueda
		sd.setType("Inicial");
		sd.addOntologies("ontologia");
		sd.addLanguages(new SLCodec().getName());
		dfd.addServices(sd);
		try {
			// registro el servicio en el DF
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			System.err.println("Agente " + getLocalName() + ": " + e.getMessage());
		}
		addBehaviour(new CyclicBehaviour() {
			private static final long serialVersionUID = 9090607020824006811L;

			@SuppressWarnings("unchecked")
			@Override
			public void action() {
				ACLMessage msg = this.myAgent.blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
				if ((msg == null)) {
					System.out.println("No se han introducido parametros");
				} else {
					try {
						System.out.println("Agente JADE con Parametros: Soy el agente " + getLocalName());
						ConcurrentHashMap<String, Integer> mapaInicial = new ConcurrentHashMap<String, Integer>();
						mapaInicial = (ConcurrentHashMap<String, Integer>) msg.getContentObject();
						TrataInfoAgente.setInventario(mapaInicial);
					} catch (UnreadableException e) {
						e.getMessage();
					}
				}

			}
		});
		jade.wrapper.AgentContainer container = getContainerController();
		
		// Start the GuiAgente
		try {
			AgentController guiAgnt = container.createNewAgent("GuiAgente", "es.upm.supermercado.GuiAgente", null);
			guiAgnt.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Utils.enviarMensaje(this, "Inicial", inventario);
	}

	private void procesarPedido(Map<String, Integer> pedido) {
		System.out.println("Procesando pedido: " + pedido);
		// Actualizar inventario
		for (Map.Entry<String, Integer> entry : pedido.entrySet()) {
			String item = entry.getKey();
			int cantidad = entry.getValue();
			int inventarioActual = inventario.getOrDefault(item, 0);
			inventario.put(item, inventarioActual - cantidad);
		}
		System.out.println("Inventario actualizado: " + inventario);

		// sendInventoryToGuiAgent();
	}

	protected void takeDown() {
		System.out.println("Apagando Agente" + getLocalName());
	}

	public static Map<String, Integer> getInventario() {
		return TrataInfoAgente.inventario;
	}

	public static void setInventario(ConcurrentHashMap<String, Integer> almacen) {
		TrataInfoAgente.inventario = almacen;
	}

	public static String getrutaArchivo() {
		return TrataInfoAgente.rutaArchivo;
	}

	public static void setrutaArchivo(String archivo) {
		TrataInfoAgente.rutaArchivo = archivo;
	}

	public static Map<Integer, String> getHistorialPedidos() {
		return historialPedidos;
	}

	public static void setHistorialPedidos(ConcurrentHashMap<Integer, String> historial) {
		TrataInfoAgente.historialPedidos = historial;
	}
}
