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

	private static Map<String, Integer> inventario = new ConcurrentHashMap<>();
	private static Map<Integer, String> historialPedidos = new ConcurrentHashMap<>();
	private static Map<String, Integer> pedido = new HashMap<>();
	private Random random = new Random();
	private String codigoPedido = "";
	private String codigoCancelar = "";
	private String propietarioClave = "admin123"; // Clave del propietario

	public void setup() {
		crearFrameUsuario();
		crearFramePropietario();
		crearFrameInicial();

		addBehaviour(new CyclicBehaviour() {
			private static final long serialVersionUID = 9090607020824006811L;

			@SuppressWarnings("unchecked")
			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					try {
						setInventario((Map<String, Integer>) msg.getContentObject());
						SwingUtilities.invokeLater(() -> {
							actualizarUI();
							actualizarUIPropietario();
						});
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				} else {
					block();
				}
			}
		});
	}

	private void crearFrameUsuario() {
		frameUsuario = new JFrame("Inventario del Supermercado");
		frameUsuario.setTitle("Inventario del Supermercado");
		frameUsuario.setSize(800, 600);
		frameUsuario.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frameUsuario.setLocationRelativeTo(null);
		frameUsuario.setVisible(false);

		frutasPanel = new JPanel(new BorderLayout());
		panelAux = new JPanel(new GridLayout(0, 3, 10, 10));
		panelAux.setBorder(new EmptyBorder(10, 10, 10, 10));
		JScrollPane scrollPane = new JScrollPane(panelAux);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Aumenta la velocidad del scroll
		frutasPanel.add(scrollPane, BorderLayout.CENTER);

		JButton atrasBoton = new JButton("ATRAS");
		atrasBoton.setFont(new Font("Arial", Font.BOLD, 14));
		atrasBoton.setFocusPainted(false);
		atrasBoton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		atrasBoton.addActionListener(e -> {
			frameInicial.setVisible(true);
			frameUsuario.setVisible(false);
		});
		frutasPanel.add(atrasBoton, BorderLayout.WEST);

		JTextField textField = new JTextField("Inventario de Supermercado", 20);
		textField.setHorizontalAlignment(JTextField.CENTER);
		textField.setEditable(false);
		textField.setFont(new Font("Arial", Font.BOLD, 16));
		textField.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		frutasPanel.add(textField, BorderLayout.NORTH);

		frameUsuario.add(frutasPanel);
	}

	private void crearFramePropietario() {
		framePropietario = new JFrame("Gestión del Inventario");
		framePropietario.setTitle("Gestión del Inventario");
		framePropietario.setSize(800, 600);
		framePropietario.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		framePropietario.setLocationRelativeTo(null);
		framePropietario.setVisible(false);

		JPanel propietarioPanel = new JPanel(new BorderLayout());
		JPanel propietarioAux = new JPanel(new GridLayout(0, 3, 10, 10));
		propietarioAux.setBorder(new EmptyBorder(10, 10, 10, 10));
		JScrollPane propietarioScrollPane = new JScrollPane(propietarioAux);
		propietarioScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Aumenta la velocidad del scroll
		propietarioPanel.add(propietarioScrollPane, BorderLayout.CENTER);

		JButton atrasBoton = new JButton("ATRAS");
		atrasBoton.setFont(new Font("Arial", Font.BOLD, 14));
		atrasBoton.setFocusPainted(false);
		atrasBoton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		atrasBoton.addActionListener(e -> {
			frameInicial.setVisible(true);
			framePropietario.setVisible(false);
		});
		propietarioPanel.add(atrasBoton, BorderLayout.WEST);

		JTextField textField = new JTextField("Gestión del Inventario", 20);
		textField.setHorizontalAlignment(JTextField.CENTER);
		textField.setEditable(false);
		textField.setFont(new Font("Arial", Font.BOLD, 16));
		textField.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		propietarioPanel.add(textField, BorderLayout.NORTH);

		framePropietario.add(propietarioPanel);
	}

	private void crearFrameInicial() {
		frameInicial = new JFrame("Menu Inicial de Fruteria");
		frameInicial.setTitle("Menu Inicial");
		frameInicial.setSize(800, 600);
		frameInicial.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frameInicial.setLocationRelativeTo(null);
		frameInicial.setVisible(true);

		panelPrincipal = new JPanel(new BorderLayout());

		JButton clienteBoton = new JButton("Cliente");
		clienteBoton.setFont(new Font("Arial", Font.BOLD, 35));
		clienteBoton.setFocusPainted(false);
		clienteBoton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		clienteBoton.setBackground(new Color(51, 153, 255));
		clienteBoton.setForeground(Color.WHITE);
		clienteBoton.addActionListener(e -> {
			frameInicial.setVisible(false);
			frameUsuario.setVisible(true);
		});

		JButton propietarioBoton = new JButton("Propietario");
		propietarioBoton.setFont(new Font("Arial", Font.BOLD, 35));
		propietarioBoton.setFocusPainted(false);
		propietarioBoton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		propietarioBoton.setBackground(new Color(51, 153, 255));
		propietarioBoton.setForeground(Color.WHITE);
		propietarioBoton.addActionListener(e -> {
			String clave = JOptionPane.showInputDialog(frameInicial, "Ingrese la clave del propietario:");
			if (propietarioClave.equals(clave)) {
				frameInicial.setVisible(false);
				framePropietario.setVisible(true);
				actualizarUIPropietario(); // Actualizar la UI para el propietario
			} else {
				JOptionPane.showMessageDialog(frameInicial, "Clave incorrecta", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		JLabel principalLabel = new JLabel("Menu Principal", SwingConstants.CENTER);
		principalLabel.setFont(new Font("Arial", Font.BOLD, 30));
		principalLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
		principalLabel.setForeground(new Color(0, 102, 204));
		panelPrincipal.add(principalLabel, BorderLayout.NORTH);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		buttonPanel.setBackground(new Color(255, 255, 255));

		clienteBoton.setAlignmentX(Component.CENTER_ALIGNMENT);
		propietarioBoton.setAlignmentX(Component.CENTER_ALIGNMENT);

		buttonPanel.add(Box.createVerticalGlue());
		buttonPanel.add(clienteBoton);
		buttonPanel.add(Box.createVerticalStrut(20));
		buttonPanel.add(propietarioBoton);
		buttonPanel.add(Box.createVerticalGlue());

		panelPrincipal.add(buttonPanel, BorderLayout.CENTER);
		panelPrincipal.setBackground(new Color(240, 240, 240));
		frameInicial.add(panelPrincipal);
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
		realizarPedidoButton.setBackground(new Color(0, 153, 76));
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

	private void actualizarUIPropietario() {
		JPanel propietarioAux = (JPanel) ((JScrollPane) ((JPanel) framePropietario.getContentPane().getComponent(0)).getComponent(0)).getViewport().getView();
		propietarioAux.removeAll();

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

			JTextField quantityField = new JTextField(entry.getValue().toString(), 5);
			quantityField.setFont(new Font("Arial", Font.PLAIN, 14));
			gbc.gridx = 1;
			gbc.gridy = 1;
			itemPanel.add(quantityField, gbc);

			JButton aplicarBoton = new JButton("Aplicar");
			aplicarBoton.setFont(new Font("Arial", Font.PLAIN, 14));
			gbc.gridx = 2;
			gbc.gridy = 1;
			itemPanel.add(aplicarBoton, gbc);

			aplicarBoton.addActionListener(e -> {
				try {
					int value = Integer.parseInt(quantityField.getText());
					inventario.put(entry.getKey(), value);
					countLabel.setText("Disponible: " + value);
					actualizarInventario(entry.getKey(), value);
					actualizarUI(); // Actualizar la UI del cliente
					JOptionPane.showMessageDialog(framePropietario, "Cantidad actualizada correctamente.");
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(framePropietario, "Por favor, introduce un número válido.");
				}
			});

			gbc.gridx = 0;
			gbc.gridy = 2;
			gbc.gridwidth = 2;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			itemPanel.add(quantityField, gbc);

			propietarioAux.add(itemPanel);
		}

		propietarioAux.revalidate();
		propietarioAux.repaint();
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
			panel.add(new JLabel("Guarda el número bien! ID del pedido:"));

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
					"Por favor, introduce un número válido. Solo se admiten dígitos de 0-9", "Error",
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

	private void actualizarInventario(String producto, int cantidad) {
		// Enviar mensaje al agente que maneja el inventario con la actualización
		try {
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(new AID("TrataInfoAgente", AID.ISLOCALNAME));
			Map<String, Integer> actualizacion = new HashMap<>();
			actualizacion.put(producto, cantidad);
			msg.setContentObject((HashMap<String, Integer>) actualizacion);
			send(msg);
		} catch (IOException e) {
			e.printStackTrace();
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
					"Por favor, introduce un número válido. Solo se admiten dígitos de 0-9", "Error",
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
