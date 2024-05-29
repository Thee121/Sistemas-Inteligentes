package es.upm.supermercado;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.core.AID;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GuiAgente extends Agent {
	private static final long serialVersionUID = -4163603376273249462L;
	JFrame frameInicial;
	JFrame frameUsuario;
	JFrame framePropietario;
	JPanel panelAux;
	JPanel frutasPanel;
	JPanel panelPrincipal;

	private static Map<String, Integer> inventario;
	private static Map<Integer, String> historialPedidos = new ConcurrentHashMap<>();
	private static Map<String, Integer> pedido = new HashMap<>();
	private Random random = new Random();
	private String codigoPedido = "";
	private String codigoCancelar = "";

	public void setup() {
		// Creacion de frameUsuario
		frameUsuario = new JFrame("Inventario del Supermercado");
		frameUsuario.setTitle("Inventario del Supermercado");
		frameUsuario.setSize(800, 600);
		frameUsuario.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frameUsuario.setLocationRelativeTo(null);
		frameUsuario.setVisible(false);
		frutasPanel = new JPanel(new BorderLayout());
		panelAux = new JPanel();
		JScrollPane scrollPane = new JScrollPane(panelAux);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Aumenta la velocidad del scroll
		frutasPanel.add(scrollPane, BorderLayout.CENTER);

		SwingUtilities.invokeLater(() -> {
			JButton atrasBoton = new JButton("ATRAS");
			atrasBoton.setFont(new Font("Arial", Font.BOLD, 14));
			atrasBoton.setFocusPainted(false);
			atrasBoton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			atrasBoton.addActionListener(e -> {
				frameInicial.setVisible(true);
				frameUsuario.setVisible(false);
			});
			panelAux.setLayout(new GridLayout(0, 3, 10, 10)); // 0 rows, 4 columns, 10px horizontal and vertical gaps
			panelAux.setBorder(new EmptyBorder(10, 10, 10, 10));
			JTextField textField = new JTextField("Inventario de Supermercado", 20);
			textField.setHorizontalAlignment(JTextField.CENTER);
			textField.setEditable(false);
			textField.setFont(new Font("Arial", Font.BOLD, 16));
			textField.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
			frutasPanel.add(textField, BorderLayout.NORTH);
			frutasPanel.add(atrasBoton, BorderLayout.WEST);
			frameUsuario.add(frutasPanel);
		});
		// Creacion de FrameInicial
		frameInicial = new JFrame("Menu Inicial de Fruteria");
		frameInicial.setTitle("Menu Inicial");
		frameInicial.setSize(800, 600);
		frameInicial.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frameInicial.setLocationRelativeTo(null);
		frameInicial.setVisible(true);
		panelPrincipal = new JPanel(new BorderLayout());

		JButton clienteBoton = new JButton("Cliente");
		clienteBoton.setFont(new Font("Arial", Font.BOLD, 14));
		clienteBoton.setFocusPainted(false);
		clienteBoton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		clienteBoton.addActionListener(e -> {
			frameInicial.setVisible(false);
			frameUsuario.setVisible(true);
		});
		JButton propietarioBoton = new JButton("Propietario");
		propietarioBoton.setFont(new Font("Arial", Font.BOLD, 14));
		propietarioBoton.setFocusPainted(false);
		propietarioBoton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		propietarioBoton.addActionListener(e -> {
			frameInicial.setVisible(true);
			frameUsuario.setVisible(false);
		});
		JLabel PrincipalLabel = new JLabel("Menu Principal");
		panelPrincipal.add(PrincipalLabel, BorderLayout.CENTER);
		panelPrincipal.add(propietarioBoton, BorderLayout.NORTH);
		panelPrincipal.add(clienteBoton, BorderLayout.SOUTH);
		frameInicial.add(panelPrincipal);

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
	}

	private void actualizarUI() {
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
				pedido.put(entry.getKey(), value);
			});

			quantityField.addActionListener(e -> {
				int value = 0;
				try {
					value = Integer.parseInt(quantityField.getText());
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(frameUsuario, "Por favor, introduce un número válido.");
				}
				if (value >= 0 && value <= max) {
					quantitySlider.setValue(value);
					pedido.put(entry.getKey(), value);
				} else {
					JOptionPane.showMessageDialog(frameUsuario, "Cantidad no válida. Debe estar entre 0 y " + max);
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
		realizarPedidoButton.setBackground(Color.BLUE);
		realizarPedidoButton.setForeground(Color.WHITE);
		realizarPedidoButton.setFocusPainted(false);
		realizarPedidoButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		realizarPedidoButton.addActionListener(e -> realizarPedido());

		TextField pedidoField = new TextField(8);
		pedidoField.addTextListener(e -> {
			codigoPedido = pedidoField.getText();
		});

		TextField cancelarField = new TextField(8);
		cancelarField.addTextListener(e -> {
			codigoCancelar = cancelarField.getText();
		});

		// Sección búsqueda de Pedido
		JButton searchButton = new JButton("-->");
		searchButton.setFont(new Font("Arial", Font.ITALIC, 10));
		searchButton.setBackground(Color.YELLOW);
		searchButton.addActionListener(e -> buscarPedido());
		JLabel consultarLabel = new JLabel("Consulta Pedidos:");
		JPanel searchPanel = new JPanel();
		searchPanel.add(consultarLabel, BorderLayout.EAST);
		searchPanel.add(pedidoField);
		searchPanel.add(searchButton);

		// Sección Cancelación de Pedido
		JButton cancelarBoton = new JButton("X");
		cancelarBoton.setFont(new Font("Arial", Font.ITALIC, 10));
		cancelarBoton.setBackground(Color.MAGENTA);
		cancelarBoton.addActionListener(e -> cancelarPedido());
		JLabel cancelarLabel = new JLabel("Cancela Pedidos:");
		JPanel cancelPanel = new JPanel();
		cancelPanel.add(cancelarLabel, BorderLayout.EAST);
		cancelPanel.add(cancelarField);
		cancelPanel.add(cancelarBoton);

		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(realizarPedidoButton, BorderLayout.CENTER);
		buttonPanel.add(searchPanel, BorderLayout.EAST);
		buttonPanel.add(cancelPanel, BorderLayout.WEST);

		frutasPanel.add(buttonPanel, BorderLayout.SOUTH);
		panelAux.revalidate();
		panelAux.repaint();
	}

	private void realizarPedido() {
		try {
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.addReceiver(new AID("TrataInfoAgente", AID.ISLOCALNAME));
			msg.setContentObject((HashMap<String, Integer>) pedido);
			send(msg);

			// Guardar el pedido en el historial
			int idPedido;
			do {
				idPedido = random.nextInt(Integer.MAX_VALUE);
			} while (historialPedidos.containsKey(idPedido));

			historialPedidos.put(idPedido, pedidoToString(pedido));

			// Crear un panel para el mensaje de confirmación
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(new JLabel("Pedido realizado con éxito!"));
			panel.add(new JLabel("¡Guárda el número bien! ID del pedido:"));

			JTextField idField = new JTextField(String.valueOf(idPedido));
			idField.setEditable(false);
			idField.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
			panel.add(idField);

			// Mostrar el panel en un JOptionPane
			JOptionPane.showMessageDialog(frameUsuario, panel);

			// Limpiar el pedido después de realizarlo
			pedido.clear();

		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frameUsuario, "Error al realizar el pedido.");
		}
	}

	private void cancelarPedido() {
		Integer pedidoCancelarNum = null;
		try {
			pedidoCancelarNum = Integer.parseInt(codigoCancelar);
			if (!historialPedidos.containsKey(pedidoCancelarNum)) {
				JOptionPane.showMessageDialog(frameUsuario, "No hay un pedido con el número " + codigoCancelar, "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(frameUsuario,
					"Por favor, introduce un número válido. Sólo se admiten dígitos de 0-9", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		} catch (NullPointerException e) {
			JOptionPane.showMessageDialog(frameUsuario, "Por favor, introduce un número de pedido.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(new JLabel("¿Seguro que quieres cancelar el pedido?"));

		// Devuelve:
		// 0 - Sí
		// 1 - No
		// 2 - Cancelar
		int option = JOptionPane.showConfirmDialog(frameUsuario, panel);
		if (option == 0) {
			try {
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(new AID("TrataInfoAgente", AID.ISLOCALNAME));
				msg.setContentObject(historialPedidos.get(pedidoCancelarNum));
				send(msg);
			} catch (IOException exc) {
				exc.printStackTrace();
				JOptionPane.showMessageDialog(frameUsuario, "Error al cancelar el pedido.");
			}
		}
	}

	private String pedidoToString(Map<String, Integer> pedido) {
		StringBuilder sb = new StringBuilder("Producto,Cantidad\n");
		for (Map.Entry<String, Integer> entry : pedido.entrySet()) {
			sb.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
		}
		sb.append("Fecha y Hora: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
		return sb.toString();
	}

	private void buscarPedido() {
		try {
			int idPedido = Integer.parseInt(codigoPedido.trim());
			if (historialPedidos.containsKey(idPedido)) {
				String pedido = historialPedidos.get(idPedido);
				JOptionPane.showMessageDialog(frameUsuario, pedido, "Detalle del Pedido",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(frameUsuario, "No hay un pedido con el número " + idPedido, "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(frameUsuario,
					"Por favor, introduce un número válido. Sólo se admiten dígitos de 0-9", "Error",
					JOptionPane.ERROR_MESSAGE);
		} catch (NullPointerException e) {
			JOptionPane.showMessageDialog(frameUsuario, "Por favor, introduce un número de pedido.", "Error",
					JOptionPane.ERROR_MESSAGE);
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
