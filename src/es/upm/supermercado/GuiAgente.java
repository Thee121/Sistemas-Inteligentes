package es.upm.supermercado;

import jade.core.Agent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class GuiAgente extends Agent {
	private static final long serialVersionUID = -4163603376273249462L;
	private Map<String, Integer> inventario;
	JFrame frame = new JFrame("Simple JFrame Example");

	public void setup() {
		this.inventario = LeeEscribeAlmacenAgente.getInventario();
		initUI();
	}

	private void initUI() {
		frame.setTitle("Inventario del Supermercado");
		frame.setSize(400, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		for (Map.Entry<String, Integer> entry : inventario.entrySet()) {
			JPanel itemPanel = new JPanel();
			itemPanel.setLayout(new FlowLayout());

			JLabel nameLabel = new JLabel(entry.getKey());
			JLabel countLabel = new JLabel(entry.getValue().toString());
			JLabel clickCounterLabel = new JLabel("0");

			JButton plusButton = new JButton("+");
			JButton minusButton = new JButton("-");

			plusButton.addActionListener(new ActionListener() {
				private int clickCounter = 0;

				@Override
				public void actionPerformed(ActionEvent e) {
					int currentCount = Integer.parseInt(countLabel.getText());
					currentCount++;
					clickCounter++;
					countLabel.setText(String.valueOf(currentCount));
					clickCounterLabel.setText(String.valueOf(clickCounter));
				}
			});

			minusButton.addActionListener(new ActionListener() {
				private int clickCounter = 0;

				@Override
				public void actionPerformed(ActionEvent e) {
					int currentCount = Integer.parseInt(countLabel.getText());
					if (currentCount > 0) {
						currentCount--;
						clickCounter++;
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
	}
}
