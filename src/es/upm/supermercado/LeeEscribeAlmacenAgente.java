package es.upm.supermercado;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LeeEscribeAlmacenAgente extends Agent {
	private static final long serialVersionUID = 4395092232132395178L;
	
	private static Map<String, Integer> inventario = new ConcurrentHashMap<>();
	private static Map<String, Integer> historialPedidos = new HashMap<String, Integer>();

	
	String path = new File("").getAbsolutePath();

	public void setup() {
		System.out.println("Agente JADE con Parametros. Inicializado el agente: " + getLocalName());


		String directorioAlmacen = "\\src\\es\\upm\\resources\\Almacen.txt";
		String directorioHistorialPedidos = "\\src\\es\\upm\\resources\\HistorialPedidos.txt";
		String pathInvenario = path + directorioAlmacen;
		String pathHistorialPedidos = path + directorioHistorialPedidos;
		LeeArchivo(pathInvenario,inventario);
		LeeArchivo(pathHistorialPedidos,historialPedidos);
		
        addBehaviour(new CyclicBehaviour() {
            private static final long serialVersionUID = 9090607020824006811L;

            @SuppressWarnings("unchecked")
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    try {
                    	//Inventario a Escribir
                        if (msg.getPerformative() == ACLMessage.INFORM) {
                            setInventario((Map<String, Integer>) msg.getContentObject());
                        } else if (msg.getPerformative() == ACLMessage.REQUEST) {
                        	//Hacer cosas
                        }
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                } else {
                    block();
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

		// Start the TrataInfoAgente
		try {
			AgentController infoAgnt = container.createNewAgent("TrataInfoAgente", "es.upm.supermercado.TrataInfoAgente",
					null);
			infoAgnt.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		sendInventoryToTrataInfoAgente();
	}

	protected void takeDown() {
		System.out.println("Apagando Agente LeeEscribeAlmacen");
	}

    private void EscribeArchivo() {
        File archivo = new File(path);

        try {
            // Verificar si el directorio padre existe, si no, crearlo
            File directorioPadre = archivo.getParentFile();
            if (directorioPadre != null && !directorioPadre.exists()) {
                directorioPadre.mkdirs();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
                writer.write("Producto,Cantidad\n");
                for (Map.Entry<String, Integer> entry : inventario.entrySet()) {
                    writer.write(entry.getKey() + "," + entry.getValue() + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void LeeArchivo(String localizacion ,Map<String, Integer> estructura) {
		String almacen;
		String cvsSplitBy = ",";
		try (BufferedReader br = new BufferedReader(new FileReader(localizacion))) {
			br.readLine();

			while ((almacen = br.readLine()) != null) {
				// Separar la archivo por comas
				String[] item = almacen.split(cvsSplitBy);

				// Obtener el nombre del elemento y la cantidad
				String elemento = item[0];
				int cantidad = Integer.parseInt(item[1]);

				// Guardar en el mapa
				inventario.put(elemento, cantidad);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	private void sendInventoryToTrataInfoAgente() {
		try {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(new AID("TrataInfoAgente", AID.ISLOCALNAME));
			msg.setContentObject((ConcurrentHashMap<String, Integer>) inventario);
			send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, Integer> getInventario() {
		return LeeEscribeAlmacenAgente.inventario;
	}

	public static void setInventario(Map<String, Integer> almacen) {
		LeeEscribeAlmacenAgente.inventario = almacen;
	}

	public static Map<String, Integer> getHistorialPedidos() {
		return historialPedidos;
	}

	public static void setHistorialPedidos(Map<String, Integer> historialPedidos) {
		LeeEscribeAlmacenAgente.historialPedidos = historialPedidos;
	}
}
