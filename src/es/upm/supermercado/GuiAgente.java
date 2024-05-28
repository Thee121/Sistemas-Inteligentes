package es.upm.supermercado;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class GuiAgente extends Agent {
	private static final long serialVersionUID = -4163603376273249462L;
	private static Map<String, Integer> inventario;
	JFrame frame = new JFrame("Inventario del Supermercado");
	JPanel panel = new JPanel();

	public void setup() {
		addBehaviour(new CyclicBehaviour() {
			private static final long serialVersionUID = 9090607020824006811L;

			@SuppressWarnings("unchecked")
			@Override
			public void action() {
				ACLMessage msg = receive();
				if (msg != null) {
					try {
						setInventario((Map<String, Integer>) msg.getContentObject());
						SwingUtilities.invokeLater(() -> initUI());
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				} else {
					block();
				}
			}
		});
	}

	private void initUI() {
		System.out.println("Agente JADE con Parametros. Inicializado el agente: " + getLocalName());
		frame.setTitle("Inventario del Supermercado");
		frame.setSize(400, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		// Agregar caja de texto encima de los nombres de los elementos
		JTextField textField = new JTextField("Inventario de Supermercado", 20);
		textField.setHorizontalAlignment(JTextField.CENTER);
		textField.setEditable(false);
		panel.add(textField);

		for (Map.Entry<String, Integer> entry : inventario.entrySet()) {
			JPanel itemPanel = new JPanel();
			itemPanel.setLayout(new FlowLayout());

			JLabel nameLabel = new JLabel(entry.getKey());
			JLabel countLabel = new JLabel(entry.getValue().toString());
			JLabel clickCounterLabel = new JLabel("0");

			JButton plusButton = new JButton("+");
			JButton minusButton = new JButton("-");

			plusButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					int currentCount = Integer.parseInt(countLabel.getText());
					int clickCounter = Integer.parseInt(clickCounterLabel.getText());
					if (currentCount > 0) {
						currentCount--;
						clickCounter++;
						countLabel.setText(String.valueOf(currentCount));
						clickCounterLabel.setText(String.valueOf(clickCounter));
					}

				}
			});

			minusButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					int currentCount = Integer.parseInt(countLabel.getText());
					int clickCounter = Integer.parseInt(clickCounterLabel.getText());
					if (clickCounter > 0) {
						currentCount++;
						clickCounter--;
						countLabel.setText(String.valueOf(currentCount));
						clickCounterLabel.setText(String.valueOf(clickCounter));
					}
				}
			});

			itemPanel.add(nameLabel);
			itemPanel.add(minusButton);
			itemPanel.add(countLabel);
			itemPanel.add(plusButton);
			itemPanel.add(clickCounterLabel);

			panel.add(itemPanel);
		}

		JScrollPane scrollPane = new JScrollPane(panel);
		frame.add(scrollPane);
		frame.setVisible(true);
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
