package es.upm.supermercado;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class GuiAgente extends Agent {
	private static final long serialVersionUID = -4163603376273249462L;

	private DFAgentDescription dfdTrata;
	private ServiceDescription sdGui;

	private static Map<String, Integer> inventario = new ConcurrentHashMap<>();
	private static Map<Integer, String> historialPedidos = new ConcurrentHashMap<>();
	private static Map<String, Integer> pedido = new HashMap<>();

	private static Random random = new Random();
	private static String codigoPedido = "";
	private static String codigoCancelar = "";
	private static String propietarioClave = "admin123";
	boolean datosRecibidos = false;

	public AID GuiAgenteAID = getAID();

	private InterfazGrafica gui;

	public void setup() {
		gui = new InterfazGrafica();

		inicializarServicios();
		controladorAgente();

	}

	private void controladorAgente() {
		addBehaviour(new TickerBehaviour(this, 1000) {
			private static final long serialVersionUID = 9090607020824006811L;

			@SuppressWarnings("unchecked")
			@Override
			public void onTick() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage msg = receive(mt);
				if (msg != null) {
					datosRecibidos = false;
				}
				if (datosRecibidos) {
					block();
					return;
				}

				if (msg != null) {
					try {
						Object[] datos = (Object[]) msg.getContentObject();
						ConcurrentHashMap<String, Integer> nuevoInventario = (ConcurrentHashMap<String, Integer>) datos[0];
						ConcurrentHashMap<Integer, String> nuevoHistorialPedidos = (ConcurrentHashMap<Integer, String>) datos[1];

						setInventario(nuevoInventario);
						setHistorialPedidos(nuevoHistorialPedidos);
						datosRecibidos = true;
						SwingUtilities.invokeLater(() -> {
							gui.actualizarUICliente();
							gui.actualizarUIPropietario();
						});
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("No se ha recibido ningun mensaje.");
				}
			}
		});
		try {
			DFService.register(this, dfdTrata);
			System.out.println("Servicio GuiAgente Inicial registrado correctamente");
		} catch (FIPAException e) {
			System.err.println("Agente " + getLocalName() + ": " + e.getMessage());
		}
	}

	private void inicializarServicios() {
		// Servicio para actualización estructuras desde TrataInfoAGente
		dfdTrata = new DFAgentDescription();
		sdGui = new ServiceDescription();
		sdGui.setName("ActualizaciondesdeTrata");
		sdGui.setType("TrasladodesdeTrarta");
		dfdTrata.addServices(sdGui);
		dfdTrata.setName(GuiAgenteAID);
	}

	protected void takeDown() {
		System.out.println("Apagando Agente " + getLocalName());
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

	public void setHistorialPedidos(Map<Integer, String> historial) {
		GuiAgente.historialPedidos = historial;
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
