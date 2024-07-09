package es.upm.supermercado;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;


public class TrataInfoAgente extends Agent {
	private static final long serialVersionUID = -5513148827856003070L;
	
	private AID trataInfoAgenteAID = getAID();

	private DFAgentDescription dfdLeeEscribe;
	private ServiceDescription sdLeeActualiza;

	private DFAgentDescription dfdGui;
	private ServiceDescription sdGuiActualiza;

	private ConcurrentHashMap<String, Integer> inventario = new ConcurrentHashMap<>();
	private ConcurrentHashMap<Integer, String> historialPedidos = new ConcurrentHashMap<>();

	private String path = new File("").getAbsolutePath();
	private String directorioAlmacen = "\\src\\es\\upm\\resources\\Almacen.txt";
	private String directorioHistorialPedidos = "\\src\\es\\upm\\resources\\HistorialPedidos.txt";
	private String pathInventario = path + directorioAlmacen;
	private String pathHistorialPedidos = path + directorioHistorialPedidos;

	public void setup() {
		System.out.println("Agente JADE con Parametros. Inicializado el agente: " + getName());
		jade.wrapper.AgentContainer container = getContainerController();

		// Start the GuiAgente
		try {
			AgentController guiAgnt = container.createNewAgent("GuiAgente", "es.upm.supermercado.GuiAgente", null);
			guiAgnt.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
        addBehaviour(new RecepcionMensajeBehaviour());
		inicializarServicios();
		controladorAgente();
	}

	private void controladorAgente() {
		// Recibe informacion de LeeEscribeAlmacente
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

					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				} else {
					block();
				}
			}
		});
	}
		
		 private class RecepcionMensajeBehaviour extends CyclicBehaviour {
			private static final long serialVersionUID = 7157368655577734166L;

				public void action() {
		            ACLMessage msg = receive();
		            if (msg != null) {
		                try {
		                    Object[] mensaje =  (Object[]) msg.getContentObject();
		                    int num = (int) mensaje[0];
		                    @SuppressWarnings("unchecked")
							ConcurrentHashMap<String, Integer> inventario = (ConcurrentHashMap<String, Integer>) mensaje[1];
		                	mensaje[0] = num;
		                    guardarInventarioEnArchivo(pathInventario,actualizarInventarioConPedido(LeeArchivoAlmacen(pathInventario), inventario));
		                    System.out.println("TrataInfoAgente ha recibido el pedido: " + inventario);
		                    guardarHistorialEnArchivo(pathHistorialPedidos,inventario, num);
		                    enviarConfirmacion();
		                } catch (UnreadableException e) {
		                    e.printStackTrace();
		                } catch (IOException e) {
							e.printStackTrace();
						}
		            } else {
		                block();
		            }
		        }
		    }
		 private void enviarConfirmacion() {
		        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		        msg.addReceiver(new AID("LeeEscribeAlmacenAgente", AID.ISLOCALNAME));
		        try {
		            msg.setContentObject(true);
		            send(msg);
		            System.out.println("Confirmacion a LeeEscribeAlmacenAgente enviada");
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
		
		// Servicio para actualizacion estructuras desde LeeEscribeAlmacenAgente
		sdLeeActualiza = new ServiceDescription();
		sdLeeActualiza.setName("ActualizacionDesdeLee");
		sdLeeActualiza.setType("TrasladoDesdeLee");
		dfdLeeEscribe.addServices(sdLeeActualiza);

		// Servicio para actualizacion informacion a GuiAgente
		sdGuiActualiza = new ServiceDescription();
		sdGuiActualiza.setName("ActualizaciondesdeTrata");
		sdGuiActualiza.setType("TrasladodesdeTrarta");
		dfdGui.addServices(sdGuiActualiza);

		//Registrar los servicios del Agente
		try {
			DFService.register(this, dfdLeeEscribe);
			System.out.println("Servicio " + dfdLeeEscribe.getName() + " registrado correctamente");

		} catch (FIPAException e) {
			System.err.println("Agente " + getLocalName() + ": " + e.getMessage());
		}
	}
    
    public ConcurrentHashMap<String, Integer> LeeArchivoAlmacen(String path) {
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
        System.out.println("TrataInventario leido del archivo. El inventario es: " + inventario);

		return inventario;
	}
    
    
    
    private ConcurrentHashMap<String, Integer> actualizarInventarioConPedido(ConcurrentHashMap<String, Integer> inventario, Map<String, Integer> pedido) {
        for (Map.Entry<String, Integer> entry : pedido.entrySet()) {
            String producto = entry.getKey();
            int cantidadPedido = entry.getValue();
            if (inventario.containsKey(producto)) {
                int cantidadActual = inventario.get(producto);
                int nuevaCantidad = cantidadActual - cantidadPedido;
                if (nuevaCantidad < 0) {
                    nuevaCantidad = 0; // No permitir inventario negativo
                }
                inventario.put(producto, nuevaCantidad);
                System.out.println("Inventario actualizado con el pedido: " + producto + " y cantidad: " + nuevaCantidad);
            }
        }
        return inventario;
    }
    
    private void guardarInventarioEnArchivo(String pathInventario, ConcurrentHashMap<String, Integer> inventario) throws IOException {
		try (FileWriter writer = new FileWriter(pathInventario)) {
			writer.write("Producto,Cantidad\n");
			for (Map.Entry<String, Integer> entry : inventario.entrySet()) {
				writer.write(entry.getKey() + "," + entry.getValue() + "\n");
			}
			System.out.println("Inventario guardado correctamente en el archivo");
		}
	}
    private void guardarHistorialEnArchivo(String pathInventario, ConcurrentHashMap<String, Integer> inventario2, int num) throws IOException {
		try (FileWriter writer = new FileWriter(pathInventario)) {
			writer.write("Codigo,Pedido\n");
	        StringBuilder sb = new StringBuilder();
	        sb.append(num);
	        for (Entry<String, Integer> entry : inventario2.entrySet()) {
	            sb.append(",").append(entry.getKey()).append(",").append(entry.getValue());
	        }
	        writer.write(sb.toString());
			System.out.println("HistorialPedido guardado correctamente en el archivo");
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


	public Map<Integer, String> getHistorialPedidos() {
		return this.historialPedidos;
	}

	public void setHistorialPedidos(ConcurrentHashMap<Integer, String> historial) {
		this.historialPedidos = historial;
	}

}
