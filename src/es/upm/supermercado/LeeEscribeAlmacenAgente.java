package es.upm.supermercado;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.core.behaviours.TickerBehaviour;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class LeeEscribeAlmacenAgente extends Agent {
	private static final long serialVersionUID = 4395092232132395178L;

	public AID LeeEscribeAlmacenAGenteAID = getAID();

	private DFAgentDescription dfdTrataActualiza;
	private ServiceDescription sdTrataActualiza;

	private ConcurrentHashMap<String, Integer> inventario = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, String> historialPedidos = new ConcurrentHashMap<>();

	private boolean datosEnviados = false;

	private String path = new File("").getAbsolutePath();
	private String directorioAlmacen = "\\src\\es\\upm\\resources\\Almacen.txt";
	private String directorioHistorialPedidos = "\\src\\es\\upm\\resources\\HistorialPedidos.txt";
	private String pathInventario = path + directorioAlmacen;
	private String pathHistorialPedidos = path + directorioHistorialPedidos;

	public void setup() {
		System.out.println("Agente JADE con Parametros. Inicializado el agente: " + getLocalName());
		setInventario(Utils.LeeArchivoAlmacen(pathInventario));
		setHistorialPedidos(Utils.LeeArchivoHistorial(pathHistorialPedidos));

		jade.wrapper.AgentContainer container = getContainerController();

		// Start the TrataInfoAgente
		try {
			AgentController infoController = container.createNewAgent("TrataInfoAgente",
					"es.upm.supermercado.TrataInfoAgente", null);
			infoController.start();
			System.out.println("TrataInfoAgente iniciado.");
		} catch (Exception e) {
			e.printStackTrace();
		}

		inicializarServicios();
		controladorAgente();

	}

	private void controladorAgente() {
		try {
			addBehaviour(new TickerBehaviour(this, 1000) {
				private static final long serialVersionUID = -8223053198587330126L;

				protected void onTick() {
					ConcurrentHashMap<String, Integer> nuevoInventario = Utils.LeeArchivoAlmacen(pathInventario);
					ConcurrentHashMap<Integer, String> nuevoHistorialPedidos = Utils
							.LeeArchivoHistorial(pathHistorialPedidos);

					if (!getInventario().equals(nuevoInventario)
							|| !getHistorialPedidos().equals(nuevoHistorialPedidos)) {
						setInventario(nuevoInventario);
						setHistorialPedidos(nuevoHistorialPedidos);
						datosEnviados = false;
					}
					if (datosEnviados) {
						block();
						return;
					}

					try {
						DFAgentDescription[] result = DFService.search(myAgent, dfdTrataActualiza);
						if (result.length > 0) {
							AID trataInfoAgenteAID = result[0].getName();
							actualizarInfoTrataInfoAgente(trataInfoAgenteAID, inventario, historialPedidos);
							datosEnviados = true;
						} else {
							System.out.println("trataInfoAgente no encontrado, reintentando...");
							block(1000); // Reintentar después de 1 segundo
						}
					} catch (FIPAException e) {
						e.printStackTrace();
					}
				}
			});

		} catch (Exception e) {
			System.err.println("Error durante la configuracion del agente: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void actualizarInfoTrataInfoAgente(AID agenteAID, ConcurrentHashMap<String, Integer> inventario,
			ConcurrentHashMap<Integer, String> historialPedidos) {
		try {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
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
		// Servicio para actualizar información a TrataInfoAgente
		dfdTrataActualiza = new DFAgentDescription();
		sdTrataActualiza = new ServiceDescription();
		sdTrataActualiza.setName("ActualizacionDesdeLee");
		sdTrataActualiza.setType("TrasladoDesdeLee");
		dfdTrataActualiza.addServices(sdTrataActualiza);
		dfdTrataActualiza.setName(LeeEscribeAlmacenAGenteAID);
	}

	protected void takeDown() {
		System.out.println("Apagando Agente LeeEscribeAlmacen");
	}

	public ConcurrentHashMap<String, Integer> getInventario() {
		return this.inventario;
	}

	public void setInventario(ConcurrentHashMap<String, Integer> almacen) {
		this.inventario = almacen;
	}

	public ConcurrentHashMap<Integer, String> getHistorialPedidos() {
		return this.historialPedidos;
	}

	public void setHistorialPedidos(ConcurrentHashMap<Integer, String> historialPedidos) {
		this.historialPedidos = historialPedidos;
	}

}
