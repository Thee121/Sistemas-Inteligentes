package es.upm.supermercado;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.core.behaviours.CyclicBehaviour;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class LeeEscribeAlmacenAgente extends Agent {
	private static final long serialVersionUID = 4395092232132395178L;

	private DFAgentDescription dfdLeeEscribe;
	private ServiceDescription sdTrataActualiza;

	private ConcurrentHashMap<String, Integer> inventario = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, String> historialPedidos = new ConcurrentHashMap<>();

	private String path = new File("").getAbsolutePath();
	private String directorioAlmacen = "\\src\\es\\upm\\resources\\Almacen.txt";
	private String directorioHistorialPedidos = "\\src\\es\\upm\\resources\\HistorialPedidos.txt";
	private String pathInventario = path + directorioAlmacen;
	private String pathHistorialPedidos = path + directorioHistorialPedidos;

	public void setup() {
		System.out.println("Agente JADE con Parametros. Inicializado el agente: " + getName());
		setInventario(LeeArchivoAlmacen(pathInventario));
		setHistorialPedidos(LeeArchivoHistorial(pathHistorialPedidos));

		jade.wrapper.AgentContainer container = getContainerController();

		try {
			AgentController infoController = container.createNewAgent("TrataInfoAgente",
					"es.upm.supermercado.TrataInfoAgente", null);
			infoController.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		addBehaviour(new RecepcionMensajeBehaviour());
		inicializarServicios();
		enviarDatosAGuiAgente();

	}

	private class RecepcionMensajeBehaviour extends CyclicBehaviour {

		private static final long serialVersionUID = -2977239725669514974L;

		public void action() {
			ACLMessage msg = receive();
			if (msg != null) {
				inventario = LeeArchivoAlmacen(pathInventario);
				historialPedidos = LeeArchivoHistorial(pathHistorialPedidos);
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
			Object[] mensaje = new Object[2];
			mensaje[0] = inventario;
			mensaje[1] = historialPedidos;
			msg.setContentObject(mensaje);
			send(msg);
			System.out.println("Inventario enviado al GuiAgente: " + inventario);
			System.out.println("HistorialPedidos enviado al GuiAgente : " + historialPedidos);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void inicializarServicios() {
		// Descriptor del Agente TrataInfo
		dfdLeeEscribe = new DFAgentDescription();
		dfdLeeEscribe.setName(getAID());

		// Servicio para actualizar información a TrataInfoAgente
		sdTrataActualiza = new ServiceDescription();
		sdTrataActualiza.setName("ActualizacionDesdeLee");
		sdTrataActualiza.setType("TrasladoDesdeLee");
		dfdLeeEscribe.addServices(sdTrataActualiza);

		// Registrar los servicios del Agente
		try {
			DFService.register(this, dfdLeeEscribe);
			System.out.println("Servicio " + dfdLeeEscribe.getName() + " registrado correctamente");

		} catch (FIPAException e) {
			System.err.println("Agente " + getLocalName() + ": " + e.getMessage());
		}
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
		System.out.println("LeeEscribeAlmacenAgente ha leido correctamente el inventario del archivo: " + inventario);

		return inventario;
	}
	
	public static ConcurrentHashMap<Integer, String> LeeArchivoHistorial(String path) {
		ConcurrentHashMap<Integer, String> historialPedido = new ConcurrentHashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;	
			br.readLine(); // Skip header

			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				int tam = parts.length;
				String valor = parts[0].trim();
				Integer pedidoCodigo = Integer.parseInt(valor);
		        StringBuilder sb2 = new StringBuilder();
		        StringBuilder sb3 = new StringBuilder();


				for(int i = 1; i<tam; i++) {
					String pedidoHistorial = parts[i].trim();
					if(pedidoHistorial.contains("}")) {
						for(int j = 0; j<pedidoHistorial.length(); j++) {
							if(pedidoHistorial.charAt(j) == '}') {
								continue;
							}
							sb3.append(pedidoHistorial.charAt(j));
						}
					}
					if(sb3.length() == 0) {
						sb2.append(pedidoHistorial);
					}else {
						sb2.append(sb3);
					}
					if(i!=tam-1) {
						sb2.append(",");						
					}
				}
				historialPedido.put(pedidoCodigo, sb2.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("LeeEscribeAlmacenAgente ha leido correctamente el historialPedido del archivo: " + historialPedido);

		return historialPedido;
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

	public void setHistorialPedidos(ConcurrentHashMap<Integer, String> historial) {
		this.historialPedidos = historial;
	}
}