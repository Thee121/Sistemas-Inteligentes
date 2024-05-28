package es.upm.supermercado;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LeeEscribeAlmacenAgente extends Agent {
    private static final long serialVersionUID = 4395092232132395178L;
    private static Map<String, Integer> inventario = new HashMap<String, Integer>();

    public void setup() {
        String line;
        String cvsSplitBy = ",";
        Object[] listaparametros = getArguments();
        if ((listaparametros == null) || (listaparametros.length < 1)) {
            System.out.println("No se han introducido parametros");
        } else {
            System.out.println("Agente JADE con Parametros. Inicializado el agente: " + getLocalName());
            String path = new File("").getAbsolutePath();
            String directorio = (String) listaparametros[0];
            path = path + directorio;
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                br.readLine();

                while ((line = br.readLine()) != null) {
                    // Separar la línea por comas
                    String[] item = line.split(cvsSplitBy);

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

        // Start the GuiAgente
        jade.wrapper.AgentContainer container = getContainerController();
        try {
            AgentController agnt = container.createNewAgent("GuiAgente", "es.upm.supermercado.GuiAgente", null);
            agnt.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Send the inventory map to GuiAgente
        sendInventoryToGuiAgent();
        
        // Start the GuiAgente
        try {
            AgentController agnt = container.createNewAgent("TrataInfoAgente", "es.upm.supermercado.TrataInfoAgente", null);
            agnt.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        sendInventoryToTrataInfoAgente();
    }

    protected void takeDown() {
        System.out.println("Apagando Agente LeeEscribeAlmacen");
    }

    private void sendInventoryToGuiAgent() {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("GuiAgente", AID.ISLOCALNAME));
            msg.setContentObject((HashMap<String, Integer>) inventario);
            send(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void sendInventoryToTrataInfoAgente() {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("TrataInfoAgente", AID.ISLOCALNAME));
            msg.setContentObject((HashMap<String, Integer>) inventario);
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
}
