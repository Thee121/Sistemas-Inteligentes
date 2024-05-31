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
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import javax.swing.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;

public class GuiAgente extends Agent {
	private static final long serialVersionUID = -4163603376273249462L;

	private static Map<String, Integer> inventario = new ConcurrentHashMap<>();
	private static Map<Integer, String> historialPedidos = new ConcurrentHashMap<>();
	private static Map<String, Integer> pedido = new HashMap<>();
	private static Random random = new Random();
	private static String codigoPedido = "";
	private static String codigoCancelar = "";
	private static String propietarioClave = "admin123"; // Clave del propietario

	public void setup() {
		InterfazGrafica gui = new InterfazGrafica();

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
						GuiAgente.setInventario(mapaInicial);
						SwingUtilities.invokeLater(() -> {
							gui.actualizarUICliente();
							gui.actualizarUIPropietario();
						});
					} catch (UnreadableException e) {
						e.getMessage();
					}
				}

			}
		});
	}
	protected void takeDown() {
		System.out.println("Apagando Agente" + getLocalName());
	}

	// Getters y setters
	public static Map<String, Integer> getInventario() {
		return GuiAgente.inventario;
	}

	public static void setInventarioElem(String fruta, Integer cantidad) {
		GuiAgente.inventario.put(fruta, cantidad);
	}

	public static void setInventario(Map<String, Integer> almacen) {
		GuiAgente.inventario = almacen;
	}

	public static String getCodigoPedido() {
		return codigoPedido;
	}

	public static void setCodigoPedido(String codigoEntrada) {
		GuiAgente.codigoPedido = codigoEntrada;
	}

	public static String getCodigoCancelar() {
		return GuiAgente.codigoCancelar;
	}

	public static void setCodigoCancelar(String codigoEntrada) {
		GuiAgente.codigoCancelar = codigoEntrada;
	}

	public static String getPropietarioClave() {
		return GuiAgente.propietarioClave;
	}

	public static void setPropietarioClave(String codigoEntrada) {
		GuiAgente.propietarioClave = codigoEntrada;
	}

	public static Random getRandom() {
		return random;
	}

	public static void setRandom(Random random) {
		GuiAgente.random = random;
	}

	public static Map<Integer, String> getHistorialPedidos() {
		return historialPedidos;
	}

	public static void setHistorialPedidosElem(String pedido, Integer codigo) {
		GuiAgente.historialPedidos.put(codigo, pedido);
	}

	public static Map<String, Integer> getPedido() {
		return pedido;
	}

	public static void setPedido(Map<String, Integer> pedido) {
		GuiAgente.pedido = pedido;
	}

	public static void setPedidoElem(String pedido, Integer codigo) {
		GuiAgente.historialPedidos.put(codigo, pedido);
	}
}
