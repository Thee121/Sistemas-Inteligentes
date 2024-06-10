package es.upm.supermercado;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.TextField;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class InterfazGrafica {

	static JFrame frameInicial;
	static JFrame frameUsuario;
	static JFrame framePropietario;
	static JPanel panelAux;
	static JPanel frutasPanel;
	static JPanel panelPrincipal;
	static JPanel buttonPanel;


	public InterfazGrafica() {
		crearFrameUsuario();
		crearFramePropietario();
		crearFrameInicial();
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
		framePropietario = new JFrame("Gestion del Inventario");
		framePropietario.setTitle("Gestion del Inventario");
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

		JTextField textField = new JTextField("Gestion del Inventario", 20);
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

			if(clave != null) {
				if (GuiAgente.getPropietarioClave().equals(clave)) {
					frameInicial.setVisible(false);
					framePropietario.setVisible(true);
					actualizarUIPropietario(); // Actualizar la UI para el propietario
				} else{
					JOptionPane.showMessageDialog(frameInicial, "Clave incorrecta", "Error", JOptionPane.ERROR_MESSAGE);
				}	
			}

		});

		JLabel principalLabel = new JLabel("Menu Principal", SwingConstants.CENTER);
		principalLabel.setFont(new Font("Arial", Font.BOLD, 30));
		principalLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
		principalLabel.setForeground(new Color(0, 102, 204));
		panelPrincipal.add(principalLabel, BorderLayout.NORTH);

		buttonPanel = new JPanel();
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

	public void actualizarUICliente() {
		panelAux.removeAll();

		for (Map.Entry<String, Integer> entry : GuiAgente.getInventario().entrySet()) {
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
				GuiAgente.setPedidoElem(entry.getKey(), value);
			});

			quantityField.addActionListener(e -> {
				int value = 0;
				try {
					value = Integer.parseInt(quantityField.getText());
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(frameUsuario, "Por favor, introduce un numero valido.");
				}
				if (value >= 0 && value <= max) {
					quantitySlider.setValue(value);
					GuiAgente.setPedidoElem(entry.getKey(), value);
				} else {
					JOptionPane.showMessageDialog(frameUsuario, "Cantidad no valida. Debe estar entre 0 y " + max);
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

		TextField pedidoField = new TextField(8);
		pedidoField.addTextListener(e -> {
			GuiAgente.setCodigoPedido(pedidoField.getText());
		});

		TextField cancelarField = new TextField(8);
		cancelarField.addTextListener(e -> {
			GuiAgente.setCodigoCancelar(cancelarField.getText());
		});

		// Seccion busqueda de Pedido
		JButton searchButton = new JButton("-->");
		searchButton.setFont(new Font("Arial", Font.ITALIC, 10));
		searchButton.setBackground(Color.YELLOW);
		searchButton.addActionListener(e -> buscarPedido());
		JLabel consultarLabel = new JLabel("Consulta Pedidos:");

		JPanel searchPanel = new JPanel();
		searchPanel.add(consultarLabel, BorderLayout.EAST);
		searchPanel.add(pedidoField);
		searchPanel.add(searchButton);

		// Seccion Cancelacion de Pedido
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

	public void actualizarUIPropietario() {
		JPanel propietarioAux = (JPanel) ((JScrollPane) ((JPanel) framePropietario.getContentPane().getComponent(0))
				.getComponent(0)).getViewport().getView();
		propietarioAux.removeAll();

		for (Map.Entry<String, Integer> entry : GuiAgente.getInventario().entrySet()) {
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
					GuiAgente.setInventarioElem(entry.getKey(), value);
					countLabel.setText("Disponible: " + value);
					GuiAgente.setInventarioElem(entry.getKey(), value);
					actualizarUICliente(); // Actualizar la UI del cliente
					JOptionPane.showMessageDialog(framePropietario, "Cantidad actualizada correctamente.");
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(framePropietario, "Por favor, introduce un numero valido.");
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

	public static void buscarPedido() {
		try {
			int idPedido = Integer.parseInt(GuiAgente.getCodigoPedido().trim());
			if (GuiAgente.getHistorialPedidos().containsKey(idPedido)) {
				String pedido = GuiAgente.getHistorialPedidos().get(idPedido);
				JOptionPane.showMessageDialog(frameUsuario, pedido, "Detalle del Pedido",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(frameUsuario, "No hay un pedido con el numero " + idPedido, "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(frameUsuario,
					"Por favor, introduce un numero valido. Solo se admiten digitos de 0-9", "Error",
					JOptionPane.ERROR_MESSAGE);
		} catch (NullPointerException e) {
			JOptionPane.showMessageDialog(frameUsuario, "Por favor, introduce un numero de pedido.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}


	public static void cancelarPedido() {
		Integer pedidoCancelarNum = null;
		try {
			pedidoCancelarNum = Integer.parseInt(GuiAgente.getCodigoCancelar());
			if (!GuiAgente.getHistorialPedidos().containsKey(pedidoCancelarNum)) {
				JOptionPane.showMessageDialog(frameUsuario,
						"No hay un pedido con el numero " + GuiAgente.getCodigoCancelar(), "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(frameUsuario,
					"Por favor, introduce un numero valido. Solo se admiten digitos de 0-9", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		} catch (NullPointerException e) {
			JOptionPane.showMessageDialog(frameUsuario, "Por favor, introduce un numero de pedido.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(new JLabel("¿Seguro que quieres cancelar el pedido?"));

		// Devuelve:
		// 0 - Si­
		// 1 - No
		// 2 - Cancelar
		int option = JOptionPane.showConfirmDialog(frameUsuario, panel);
		if (option == 0) {
			try {
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.addReceiver(new AID("TrataInfoAgente", AID.ISLOCALNAME));
				msg.setContentObject(GuiAgente.getHistorialPedidos().get(pedidoCancelarNum));
				// send(msg);
			} catch (IOException exc) {
				exc.printStackTrace();
				JOptionPane.showMessageDialog(frameUsuario, "Error al cancelar el pedido.");
			}
		}
	}

	private static void realizarPedido() throws IOException {
		// Guardar el pedido en el historial
		int idPedido;
		do {
			idPedido = GuiAgente.getRandom().nextInt(Integer.MAX_VALUE);
		} while (GuiAgente.getHistorialPedidos().containsKey(idPedido));
		GuiAgente.setHistorialPedidosElem(pedidoToString(GuiAgente.getPedido()), idPedido);

		// Crear un panel para el mensaje de confirmaciÃ³n
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(new JLabel("Pedido realizado con Exito!"));
		panel.add(new JLabel("¡Guarda el numero bien! ID del pedido:"));

		JTextField idField = new JTextField(String.valueOf(idPedido));
		idField.setEditable(false);
		idField.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		panel.add(idField);

		// Mostrar el panel en un JOptionPane
		JOptionPane.showMessageDialog(frameUsuario, panel);

		// Limpiar el pedido despuÃ©s de realizarlo
		GuiAgente.getPedido().clear();
	}
	private static String pedidoToString(Map<String, Integer> pedido) {
		StringBuilder sb = new StringBuilder("Producto,Cantidad\n");
		for (Map.Entry<String, Integer> entry : pedido.entrySet()) {
			sb.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
		}
		sb.append("Fecha y Hora: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(5))).append("\n");
		return sb.toString();
	}
}
