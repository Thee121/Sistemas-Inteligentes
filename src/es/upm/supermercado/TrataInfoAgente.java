package es.upm.supermercado;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.core.AID;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrataInfoAgente extends Agent {
    private static final long serialVersionUID = -5513148827856003070L;
    
	private static Map<String, Integer> inventario = new ConcurrentHashMap<>();
    private static Map<String, Integer> historialPedidos;

    static String rutaArchivo = "";

    public void setup() {
        System.out.println("Agente JADE con Parametros. Inicializado el agente: " + getLocalName());
        addBehaviour(new CyclicBehaviour() {
            private static final long serialVersionUID = 9090607020824006811L;

            @SuppressWarnings("unchecked")
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    try {
                        if ((msg.getPerformative() == ACLMessage.INFORM)) {
                                TrataInfoAgente.setInventario((Map<String, Integer>) msg.getContentObject());
                                sendInventoryToGuiAgent();                       		

                        } else if (msg.getPerformative() == ACLMessage.REQUEST) {
                            HashMap<String, Integer> pedido = (HashMap<String, Integer>) msg.getContentObject();
                            procesarPedido(pedido);
                        }
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                } else {
                    block();
                }
            }
        });
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
        sendInventoryToGuiAgent();
    }

	private void sendInventoryToGuiAgent() {
		try {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(new AID("GuiAgente", AID.ISLOCALNAME));
			msg.setContentObject((ConcurrentHashMap<String, Integer>) inventario);
			send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendInventoryToLeeEscribeAlmacenAgente() {
		try {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(new AID("LeeEscribeAlmacenAgente", AID.ISLOCALNAME));
			msg.setContentObject((HashMap<String, Integer>) inventario);
			send(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    protected void takeDown() {
        System.out.println("Apagando Agente" + getLocalName());
    }

    public static Map<String, Integer> getInventario() {
        return TrataInfoAgente.inventario;
    }

    public static void setInventario(Map<String, Integer> inventario) {
        TrataInfoAgente.inventario = inventario;
    }
    public static String getrutaArchivo() {
    	return TrataInfoAgente.rutaArchivo;
    }
    public static void setrutaArchivo(String archivo) {
    	TrataInfoAgente.rutaArchivo = archivo;
    }

	public static Map<String, Integer> getHistorialPedidos() {
		return historialPedidos;
	}

	public static void setHistorialPedidos(Map<String, Integer> historialPedidos) {
		TrataInfoAgente.historialPedidos = historialPedidos;
	}
}
