package es.upm.supermercado;
import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;


public class LeeEscribeAlmacenAgente extends Agent {
	private static final long serialVersionUID = 4395092232132395178L;

	public AID LeeEscribeAlmacenAGenteAID = getAID();
	private String nombreTrataInfoAGente = getLocalName();


	private DFAgentDescription dfdLeeEscribe;
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
		System.out.println("Agente JADE con Parametros. Inicializado el agente: " + nombreTrataInfoAGente);
		setInventario(LeeArchivoAlmacen(pathInventario));
		
		jade.wrapper.AgentContainer container = getContainerController();

		try {
			AgentController infoController = container.createNewAgent("TrataInfoAgente",
					"es.upm.supermercado.TrataInfoAgente", null);
			infoController.start();
			System.out.println("TrataInfoAgente iniciado.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		addBehaviour(new RecepcionMensajeBehaviour());
		inicializarServicios();
		enviarDatosAGuiAgente();

	}
	
	 private class RecepcionMensajeBehaviour extends CyclicBehaviour {
	        public void action() {
	            ACLMessage msg = receive();
	            if (msg != null) {
	            	inventario = LeeArchivoAlmacen(pathInventario);
	            	enviarDatosAGuiAgente();
	            } else {
	                block();
	            }
	        }
	    }
	
	
	private void enviarDatosAGuiAgente() {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("GuiAgente", AID.ISLOCALNAME));
        try {
            msg.setContentObject(inventario);
            send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	private void inicializarServicios() {
		// Descriptor del Agente TrataInfo
		dfdLeeEscribe = new DFAgentDescription();
		dfdLeeEscribe.setName(LeeEscribeAlmacenAGenteAID);
		
		// Servicio para actualizar información a TrataInfoAgente
		sdTrataActualiza = new ServiceDescription();
		sdTrataActualiza.setName("ActualizacionDesdeLee");
		sdTrataActualiza.setType("TrasladoDesdeLee");
		dfdLeeEscribe.addServices(sdTrataActualiza);
	}
	
	public static ConcurrentHashMap<String, Integer> LeeArchivoAlmacen(String path) {
		ConcurrentHashMap<String, Integer> inventario = new ConcurrentHashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;
			br.readLine(); // Skip header
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				if (parts.length == 2) {
					String producto = parts[0].trim();
					Integer cantidad = Integer.parseInt(parts[1].trim());
					inventario.put(producto, cantidad);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inventario;
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

}
