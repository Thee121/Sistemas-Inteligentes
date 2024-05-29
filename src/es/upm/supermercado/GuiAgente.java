package es.upm.supermercado;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GuiAgente extends Agent {
    private static final long serialVersionUID = -4163603376273249462L;
    private static Map<String, Integer> inventario;
    JFrame frame;
    JPanel panel;
    JPanel mainPanel;
    Map<String, Integer> pedido = new HashMap<>();

    public void setup() {
        frame = new JFrame("Inventario del Supermercado");
        mainPanel = new JPanel(new BorderLayout());
        panel = new JPanel();

        frame.setTitle("Inventario del Supermercado");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Aumenta la velocidad del scroll
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        addBehaviour(new CyclicBehaviour() {
            private static final long serialVersionUID = 9090607020824006811L;

            @SuppressWarnings("unchecked")
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    try {
                        setInventario((Map<String, Integer>) msg.getContentObject());
                        SwingUtilities.invokeLater(() -> actualizarUI());
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                } else {
                    block();
                }
            }
        });

        SwingUtilities.invokeLater(() -> {
            panel.setLayout(new GridLayout(0, 3, 10, 10)); // 0 rows, 4 columns, 10px horizontal and vertical gaps
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));
            JTextField textField = new JTextField("Inventario de Supermercado", 20);
            textField.setHorizontalAlignment(JTextField.CENTER);
            textField.setEditable(false);
            textField.setFont(new Font("Arial", Font.BOLD, 16));
            textField.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            mainPanel.add(textField, BorderLayout.NORTH);
            frame.add(mainPanel);
            frame.setVisible(true);
        });
    }

    private void actualizarUI() {
        panel.removeAll();

        for (Map.Entry<String, Integer> entry : inventario.entrySet()) {
            JPanel itemPanel = new JPanel(new GridBagLayout());
            itemPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
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

            quantitySlider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int value = quantitySlider.getValue();
                    quantityField.setText(String.valueOf(value));
                    pedido.put(entry.getKey(), value);
                }
            });

            quantityField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int value = 0;
                    try {
                        value = Integer.parseInt(quantityField.getText());
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "Por favor, introduce un número válido.");
                    }
                    if (value >= 0 && value <= max) {
                        quantitySlider.setValue(value);
                        pedido.put(entry.getKey(), value);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Cantidad no válida. Debe estar entre 0 y " + max);
                        quantityField.setText("0");
                        quantitySlider.setValue(0);
                    }
                }
            });

            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            itemPanel.add(quantitySlider, gbc);

            panel.add(itemPanel);
        }

        // Asegurarse de que el botón esté siempre visible
        JPanel buttonPanel = new JPanel();
        JButton realizarPedidoButton = new JButton("Realizar Pedido");
        realizarPedidoButton.setFont(new Font("Arial", Font.BOLD, 14));
        realizarPedidoButton.setBackground(Color.BLUE);
        realizarPedidoButton.setForeground(Color.WHITE);
        realizarPedidoButton.setFocusPainted(false);
        realizarPedidoButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        realizarPedidoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                realizarPedido();
            }
        });

        buttonPanel.add(realizarPedidoButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.revalidate();
        panel.repaint();
    }

    private void realizarPedido() {
        try {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID("TrataInfoAgente", AID.ISLOCALNAME));
            msg.setContentObject((HashMap<String, Integer>) pedido);
            send(msg);
            JOptionPane.showMessageDialog(frame, "Pedido realizado con éxito!");
            pedido.clear();  // Limpiar el pedido después de realizarlo
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error al realizar el pedido.");
        }
    }

    protected void takeDown() {
        System.out.println("Apagando Agente" + getLocalName());
    }

    public static Map<String, Integer> getInventario() {
        return GuiAgente.inventario;
    }

    public static void setInventario(Map<String, Integer> almacen) {
        GuiAgente.inventario = almacen;
    }
}
