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
import jade.wrapper.AgentController;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrataInfoAgente extends Agent {
	private static final long serialVersionUID = -5513148827856003070L;
	
	private AID trataInfoAgenteAID = getAID();
	private String nombreTrataInfoAGente = getLocalName();

	private DFAgentDescription dfdLeeEscribe;
	private ServiceDescription sdLeeActualiza;

	private DFAgentDescription dfdGui;
	private ServiceDescription sdGuiActualiza;

	private ConcurrentHashMap<String, Integer> inventario = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, String> historialPedidos = new ConcurrentHashMap<>();

	private String rutaArchivoInventario = "src/es/upm/resources/Almacen.txt";
	private String rutaArchivoHistorial = "src/es/upm/resources/HistorialPedidos.txt";
	private Boolean datosEnviados = true;

	public void setup() {
		jade.wrapper.AgentContainer container = getContainerController();

		// Start the GuiAgente
		try {
			AgentController guiAgnt = container.createNewAgent("GuiAgente", "es.upm.supermercado.GuiAgente", null);
			guiAgnt.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		inicializarServicios();
		controladorAgente();
	}

	private void controladorAgente() {
		// Recibe información de LeeEscribeAlmacente
		addBehaviour(new TickerBehaviour(this, 1000) {
			private static final long serialVersionUID = 3652551545563224088L;

			@SuppressWarnings("unchecked")
			@Override
			public void onTick() {

				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE);
				ACLMessage msg = receive(mt);
				if (msg != null) {
					try {
						Object[] datos = (Object[]) msg.getContentObject();
						ConcurrentHashMap<String, Integer> nuevoInventario = (ConcurrentHashMap<String, Integer>) datos[0];
						ConcurrentHashMap<Integer, String> nuevoHistorialPedidos = (ConcurrentHashMap<Integer, String>) datos[1];

						setInventario(nuevoInventario);
						setHistorialPedidos(nuevoHistorialPedidos);
						datosEnviados = false;

					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				} else {
					block();
				}
			}
		});

		// Manda información a GuiAgente
		addBehaviour(new TickerBehaviour(this, 1000) {
			private static final long serialVersionUID = 6825347731815201155L;

			@Override
			public void onTick() {
				if (datosEnviados) {
					block();
					return;
				}
				try {
					DFAgentDescription[] result = DFService.search(myAgent, dfdGui);
					if (result.length > 0) {
						AID guiAgenteAID = result[0].getName();
						actualizaInfoGuiAgente(guiAgenteAID, inventario, historialPedidos);
						datosEnviados = true;
					} else {
						System.out.println("guiAgenteAID no encontrado, reintentando...");
						block(1000); // Reintentar después de 1 segundo
					}
				} catch (FIPAException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void actualizaInfoGuiAgente(AID agenteAID, ConcurrentHashMap<String, Integer> inventario,
			ConcurrentHashMap<Integer, String> historialPedidos) {
		try {
			ACLMessage msg = new ACLMessage(ACLMessage.PROPAGATE);
			msg.addReceiver(agenteAID);
			msg.setContentObject(new Object[] { inventario, historialPedidos });
			send(msg);
			System.out.println("Datos enviados al: " + agenteAID.getName() + " " + this.inventario + " y "
					+ this.historialPedidos + " al agente: " + agenteAID.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void inicializarServicios() {
		
		// Descriptor del Agente LeeEscribeAlmacen
		dfdLeeEscribe = new DFAgentDescription();
		dfdLeeEscribe.setName(trataInfoAgenteAID);
		
		// Descriptor del Agente Gui
		dfdGui = new DFAgentDescription();
		dfdGui.setName(trataInfoAgenteAID);
		
		// Servicio para actualización estructuras desde LeeEscribeAlmacenAgente
		sdLeeActualiza = new ServiceDescription();
		sdLeeActualiza.setName("ActualizacionDesdeLee");
		sdLeeActualiza.setType("TrasladoDesdeLee");
		dfdLeeEscribe.addServices(sdLeeActualiza);

		// Servicio para actualización información a GuiAgente
		sdGuiActualiza = new ServiceDescription();
		sdGuiActualiza.setName("ActualizaciondesdeTrata");
		sdGuiActualiza.setType("TrasladodesdeTrarta");
		dfdGui.addServices(sdGuiActualiza);

		//Registrar los servicios del Agente
		try {
			DFService.register(this, dfdLeeEscribe);
			System.out.println("Servicio " + getLocalName() + " registrado correctamente");
		} catch (FIPAException e) {
			System.err.println("Agente " + getLocalName() + ": " + e.getMessage());
		}
	}

	protected void takeDown() {
		System.out.println("Apagando Agente TrataInfo");
	}

	public ConcurrentHashMap<String, Integer> getInventario() {
		return this.inventario;
	}

	public void setInventario(ConcurrentHashMap<String, Integer> almacen) {
		this.inventario = almacen;
	}

	public String getRutaArchivoInventario() {
		return this.rutaArchivoInventario;
	}

	public void setRutaArchivoInventario(String archivo) {
		this.rutaArchivoInventario = archivo;
	}

	public Map<Integer, String> getHistorialPedidos() {
		return this.historialPedidos;
	}

	public void setHistorialPedidos(ConcurrentHashMap<Integer, String> historial) {
		this.historialPedidos = historial;
	}

	public String getRutaArchivoHistorial() {
		return this.rutaArchivoHistorial;
	}

	public void setRutaArchivoHistorial(String archivo) {
		this.rutaArchivoHistorial = archivo;
	}
}