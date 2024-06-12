package es.upm.supermercado;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import javax.swing.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class GuiAgente extends Agent {
	private static final long serialVersionUID = -4163603376273249462L;
	
	private AID GuiAgenteAID = getAID();
	private String nombreGuiAGente = getLocalName();

	private DFAgentDescription dfdGui;
	private ServiceDescription sdGui;
	private ServiceDescription sdPedidoCliente;
	private ServiceDescription sdModificacionAdmin;


	private static Map<String, Integer> inventario = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Integer, String> historialPedidos = new ConcurrentHashMap<>();
	private static Map<String, Integer> pedido = new HashMap<>();

	private static Random random = new Random();
	
	private static String codigoPedido = "";
	private static String codigoCancelar = "";
	private static String propietarioClave = "admin123";
	
	private boolean datosRecibidos = false;
	private static boolean pedidoRealizado = false;

	private InterfazGrafica gui;

	public void setup() {
		gui = new InterfazGrafica();

		inicializarServicios();
		controladorAgente();

	}

	private void controladorAgente() {
		//Recibe Actualización del Almacen y pedidosHistorial desde TrataInfoAgente
		addBehaviour(new TickerBehaviour(this, 1000) {
			private static final long serialVersionUID = 9090607020824006811L;

			@SuppressWarnings("unchecked")
			@Override
			public void onTick() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);
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
					System.out.println("No ha recibido ningun mensaje inicial el agente GuiAgente.");
				}
			}
		});
		//Envía pedido y pedidosHistorial a TrataInfoAgente para que los procese.
		addBehaviour(new CyclicBehaviour() {
			private static final long serialVersionUID = -6161882159320004932L;

			@Override
			public void action() {
				if(pedidoRealizado) {
					try {
						DFAgentDescription[] result = DFService.search(myAgent, dfdGui);
						if (result.length > 0) {
							AID guiAgenteAID = result[0].getName();
							enviarPedidoHistorial(guiAgenteAID, pedido, historialPedidos);
							System.out.println("nani");
							pedidoRealizado = false;
						} else {
							System.out.println("guiAgenteAID no encontrado, reintentando...");
						}
					} catch (FIPAException e) {
						e.printStackTrace();
					}
				}else{
					block();
				}
			}
			
		});
	}

	private void inicializarServicios() {
		// Descriptor del Agente Gui
		dfdGui = new DFAgentDescription();
		dfdGui.setName(GuiAgenteAID);
		
		// Servicio para escuchar la actualización procedente del agente TrataInfo
		sdGui = new ServiceDescription();
		sdGui.setName("ActualizaciondesdeTrata");
		sdGui.setType("TrasladodesdeTrarta");
		dfdGui.addServices(sdGui);

		// Servicio para enviar el pedido al agente TrataInfoAgente
		sdPedidoCliente = new ServiceDescription();
		sdPedidoCliente.setName("PedidoCliente");
		sdPedidoCliente.setType("TrasladodeGuiCliente");
		dfdGui.addServices(sdPedidoCliente);
		
		// Servicio para enviar la modificación de Admin al agente TrataInfogente
		sdModificacionAdmin = new ServiceDescription();
		sdModificacionAdmin.setName("ModificacionAdmin");
		sdPedidoCliente.setType("TrasladodeGuiAdmin");
		dfdGui.addServices(sdPedidoCliente);
		
		//Registrar los servicios del Agente
		try {
			DFService.register(this, dfdGui);
			System.out.println("Servicio " + nombreGuiAGente + " registrado correctamente");
		} catch (FIPAException e) {
			System.err.println("Agente " + nombreGuiAGente + ": " + e.getMessage());
		}
	}
	private void enviarPedidoHistorial(AID agenteAID, Map<String, Integer> pedidoUsuario,
			ConcurrentHashMap<Integer, String> historialPedidos) {
		try {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(agenteAID);
			msg.setContentObject(new Object[] { pedidoUsuario, historialPedidos });
			send(msg);
			System.out.println("Datos enviados al: " + agenteAID.getName() + " " + pedidoUsuario + " y "
					+ GuiAgente.historialPedidos + " al agente: " + agenteAID.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	protected void takeDown() {
		System.out.println("Apagando Agente Gui");
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

	public void setHistorialPedidos(ConcurrentHashMap<Integer, String> historial) {
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

	public static boolean isPedidoRealizado() {
		return GuiAgente.pedidoRealizado;
	}

	public static void setPedidoRealizado(boolean pedidoRealizado) {
		GuiAgente.pedidoRealizado = pedidoRealizado;
	}
}