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

    private static ConcurrentHashMap<String, Integer> inventario = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer, String> historialPedidos = new ConcurrentHashMap<>();

    String path = new File("").getAbsolutePath();

    public void setup() {
        System.out.println("Agente JADE con Parametros. Inicializado el agente: " + getLocalName());

        String directorioAlmacen = "\\src\\es\\upm\\resources\\Almacen.txt";
        String directorioHistorialPedidos = "\\src\\es\\upm\\resources\\HistorialPedidos.txt";
        String pathInventario = path + directorioAlmacen;
        String pathHistorialPedidos = path + directorioHistorialPedidos;

        try {
            setInventario(Utils.LeeArchivoAlmacen(pathInventario));
            setHistorialPedidos(Utils.LeeArchivoHistorial(pathHistorialPedidos));

            addBehaviour(new TickerBehaviour(this, 1000) {  // Ejecutar cada segundo
                protected void onTick() {
                    // Leer el archivo cada segundo
                    setInventario(Utils.LeeArchivoAlmacen(pathInventario));
                    setHistorialPedidos(Utils.LeeArchivoHistorial(pathHistorialPedidos));
                    DFAgentDescription dfd = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("Inicial");
                    dfd.addServices(sd);

                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, dfd);
                        if (result.length > 0) {
                            AID guiAgenteAID = result[0].getName();
                            enviarInventarioAGuiAgente(guiAgenteAID, inventario);
                        } else {
                            System.out.println("GuiAgente no encontrado, reintentando...");
                        }
                    } catch (FIPAException e) {
                        e.printStackTrace();
                    }
                }
            });

            jade.wrapper.AgentContainer container = getContainerController();

            // Start the TrataInfoAgente
            try {
                AgentController infoController = container.createNewAgent("TrataInfoAgente", "es.upm.supermercado.TrataInfoAgente", null);
                infoController.start();
                System.out.println("TrataInfoAgente iniciado.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Error durante la configuración del agente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void enviarInventarioAGuiAgente(AID guiAgenteAID, ConcurrentHashMap<String, Integer> inventario) {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(guiAgenteAID);
            msg.setContentObject(inventario);
            send(msg);
            System.out.println("Inventario enviado al GuiAgente: " + inventario + " al agente: " + guiAgenteAID.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void takeDown() {
        System.out.println("Apagando Agente LeeEscribeAlmacen");
    }

    public static ConcurrentHashMap<String, Integer> getInventario() {
        return LeeEscribeAlmacenAgente.inventario;
    }

    public static void setInventario(ConcurrentHashMap<String, Integer> almacen) {
        LeeEscribeAlmacenAgente.inventario = almacen;
    }

    public static ConcurrentHashMap<Integer, String> getHistorialPedidos() {
        return historialPedidos;
    }

    public static void setHistorialPedidos(ConcurrentHashMap<Integer, String> historialPedidos) {
        LeeEscribeAlmacenAgente.historialPedidos = historialPedidos;
    }
}

