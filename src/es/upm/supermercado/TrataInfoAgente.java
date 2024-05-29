package es.upm.supermercado;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.core.AID;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TrataInfoAgente extends Agent {
    private static final long serialVersionUID = -5513148827856003070L;
    private static Map<String, Integer> inventario;
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
                        if (msg.getPerformative() == ACLMessage.INFORM) {
                            setInventario((Map<String, Integer>) msg.getContentObject());
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
        actualizarArchivoInventario();
        enviarInventarioActualizado();
    }

    private void actualizarArchivoInventario() {
        File archivo = new File(rutaArchivo);

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

    private void enviarInventarioActualizado() {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("GuiAgente", AID.ISLOCALNAME));
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
}
