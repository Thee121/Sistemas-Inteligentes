package es.upm.supermercado;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class GuiAgente extends Agent {
    private static final long serialVersionUID = -4163603376273249462L;

	private static ConcurrentHashMap<String, Integer> inventario = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Integer, String> historialPedidos = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, Integer> pedido = new ConcurrentHashMap<>();
    private static Random random = new Random();
    private static boolean pedidoRealizado = false;

    private JFrame frame;
    private JPanel panelAux;
    private JPanel frutasPanel;

    protected void setup() {
		System.out.println("Agente JADE con Parametros. Inicializado el agente: " + getName());
        crearFrame();
        inicializarServicios();
        addBehaviour(new RecepcionMensajeBehaviour());
    }

    private void inicializarServicios() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("gui-agente");
        sd.setName(getLocalName() + "-gui-agente");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
			System.out.println("Servicio " + dfd.getName() + " registrado correctamente");

        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private void crearFrame() {
        frame = new JFrame("Inventario del Supermercado");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        frutasPanel = new JPanel(new BorderLayout());
        panelAux = new JPanel(new GridLayout(0, 3, 10, 10));
        panelAux.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(panelAux);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        frutasPanel.add(scrollPane, BorderLayout.CENTER);

        JTextField textField = new JTextField("Inventario de Supermercado", 20);
        textField.setHorizontalAlignment(JTextField.CENTER);
        textField.setEditable(false);
        textField.setFont(new Font("Arial", Font.BOLD, 16));
        textField.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        frutasPanel.add(textField, BorderLayout.NORTH);

        frame.add(frutasPanel);
        frame.setVisible(true);
    }

    private class RecepcionMensajeBehaviour extends CyclicBehaviour {
		private static final long serialVersionUID = -5336301168490360286L;

		@SuppressWarnings("unchecked")
		public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                try {
                    
        			Object[] mensaje = new Object[2];
                    mensaje= (Object[]) msg.getContentObject();
                    inventario = (ConcurrentHashMap<String, Integer>) mensaje[0];
                    historialPedidos  =(ConcurrentHashMap<Integer, String>) mensaje[1];
                    System.out.println("GuiAgente ha recibido el inventario: " + inventario);
                    System.out.println("GuiAgente ha recibido el Historial Pedidos: " + historialPedidos);

                    actualizarInventario(inventario);
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }
    }

    private void actualizarInventario(ConcurrentHashMap<String, Integer> nuevoInventario) {
        SwingUtilities.invokeLater(() -> {
            GuiAgente.setInventario(nuevoInventario);
            actualizarUICliente();
        });
    }

    private void actualizarUICliente() {
        panelAux.removeAll();

        for (Map.Entry<String, Integer> entry : inventario.entrySet()) {
            JPanel itemPanel = new JPanel(new GridBagLayout());
            itemPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);

            JLabel nameLabel = new JLabel(entry.getKey());
            nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.WEST;
            itemPanel.add(nameLabel, gbc);

            JLabel countLabel = new JLabel("Disponible: " + entry.getValue().toString());
            countLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            itemPanel.add(countLabel, gbc);

            JTextField quantityField = new JTextField("0", 5);
            quantityField.setFont(new Font("Arial", Font.PLAIN, 14));
            gbc.gridx = 1;
            gbc.gridy = 1;
            itemPanel.add(quantityField, gbc);

            int max = entry.getValue();
            JSlider quantitySlider;
            if (max >= 0) {
                quantitySlider = new JSlider(0, max, 0);
            } else {
                quantitySlider = new JSlider(0, 0, 0);
                quantitySlider.setEnabled(false);
                quantityField.setEnabled(false);
            }

            quantitySlider.addChangeListener(e -> {
                int value = quantitySlider.getValue();
                quantityField.setText(String.valueOf(value));
                GuiAgente.pedido.put(entry.getKey(), value);  // Actualizar el pedido
            });

            quantityField.addActionListener(e -> {
                int value = 0;
                try {
                    value = Integer.parseInt(quantityField.getText());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Por favor, introduce un número válido.");
                }
                if (value >= 0 && value <= max) {
                    quantitySlider.setValue(value);
                    GuiAgente.pedido.put(entry.getKey(), value);  // Actualizar el pedido
                } else {
                    JOptionPane.showMessageDialog(frame, "Cantidad no válida. Debe estar entre 0 y " + max);
                    quantityField.setText("0");
                    quantitySlider.setValue(0);
                }
            });

            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            itemPanel.add(quantitySlider, gbc);

            panelAux.add(itemPanel);
        }

        JButton realizarPedidoButton = new JButton("Realizar Pedido");
        realizarPedidoButton.setFont(new Font("Arial", Font.BOLD, 14));
        realizarPedidoButton.setBackground(new Color(0, 153, 76));
        realizarPedidoButton.setForeground(Color.WHITE);
        realizarPedidoButton.setFocusPainted(false);
        realizarPedidoButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        realizarPedidoButton.addActionListener(e -> {
            try {
                realizarPedido();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(realizarPedidoButton, BorderLayout.CENTER);

        frutasPanel.add(buttonPanel, BorderLayout.SOUTH);
        panelAux.revalidate();
        panelAux.repaint();
    }

    private void realizarPedido() throws IOException {
        // Generar un ID de pedido único (puede ser útil para tracking interno, aunque no se use el historial aquí)
        int idPedido;
        do {
            idPedido = GuiAgente.getRandom().nextInt(Integer.MAX_VALUE);
        } while (GuiAgente.getPedido().containsKey(String.valueOf(idPedido)));

        // Crear un panel para el mensaje de confirmación
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Pedido realizado con Exito!"));
        panel.add(new JLabel("�Guarda bien el numero! ID del pedido:"));

        JTextField idField = new JTextField(String.valueOf(idPedido));
        idField.setEditable(false);
        idField.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        panel.add(idField);

        // Mostrar el panel en un JOptionPane
        JOptionPane.showMessageDialog(null, panel);

        // Enviar el pedido al TrataInfoAgente
        enviarPedidoTrataInfoAgente(GuiAgente.getPedido(), idPedido);

        GuiAgente.setPedidoRealizado(true);
    }

    private void enviarPedidoTrataInfoAgente(Map<String, Integer> pedido, int num) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("TrataInfoAgente", AID.ISLOCALNAME));
        try {
        	Object[] mensaje = new Object[2];
        	mensaje[0] = num;
        	mensaje[1] = pedido;
            msg.setContentObject((Object[]) mensaje);
            send(msg);
            System.out.println("Pedido enviado a TrataInfoAgente: " + pedido);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            }
        }
        enviarMensajeActualizarInventario(inventario);
        return inventario;
    }

    private void enviarMensajeActualizarInventario(ConcurrentHashMap<String, Integer> inventario) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID("LeeEscribeAlmacenAgente", AID.ISLOCALNAME));
        try {
            msg.setContentObject(inventario);
            send(msg);
            System.out.println("Mensaje de actualizacion de inventario enviado a LeeEscribeAlmacenAgente: " + inventario);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String pedidoToString(ConcurrentHashMap<String, Integer> pedido) {
        StringBuilder sb = new StringBuilder("Producto,Cantidad\n");
        for (Map.Entry<String, Integer> entry : pedido.entrySet()) {
            sb.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
        }
        sb.append("Fecha y Hora: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())).append("\n");
        return sb.toString();
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Agente GuiAgente finalizado.");
    }

    public static ConcurrentHashMap<String, Integer> getInventario() {
        return GuiAgente.inventario;
    }

    public static void setInventario(ConcurrentHashMap<String, Integer> nuevoInventario) {
        inventario = nuevoInventario;
    }

    public static void setInventarioElem(String fruta, Integer cantidad) {
        inventario.put(fruta, cantidad);
    }

    public static Random getRandom() {
        return random;
    }

    public static void setRandom(Random random) {
        GuiAgente.random = random;
    }

    public static Map<String, Integer> getPedido() {
        return pedido;
    }

    public static void setPedido(ConcurrentHashMap<String, Integer> pedido) {
        GuiAgente.pedido = pedido;
    }

    public static boolean isPedidoRealizado() {
        return pedidoRealizado;
    }

    public static void setPedidoRealizado(boolean pedidoRealizadoNuevo) {
        GuiAgente.pedidoRealizado = pedidoRealizadoNuevo;
    }
}
