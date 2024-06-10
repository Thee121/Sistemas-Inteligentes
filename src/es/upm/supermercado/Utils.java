package es.upm.supermercado;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Utils {

	public static ConcurrentHashMap<String, Integer> LeeArchivoAlmacen(String path) {
		ConcurrentHashMap<String, Integer> inventario = new ConcurrentHashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;
			br.readLine(); // Skip header
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				if (parts.length == 2) {
					String producto = parts[0].trim();
					Integer cantidad = Integer.parseInt(parts[1].trim());
					inventario.put(producto, cantidad);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inventario;
	}

	public static ConcurrentHashMap<Integer, String> LeeArchivoHistorial(String path) {
		ConcurrentHashMap<Integer, String> historial = new ConcurrentHashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(",");
				if (parts.length == 2) {
					Integer id = Integer.parseInt(parts[0].trim());
					String detalle = parts[1].trim();
					historial.put(id, detalle);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return historial;
	}

	private void guardarInventarioEnArchivo(String pathInventario, Map<String, Integer> inventario) throws IOException {
		try (FileWriter writer = new FileWriter(pathInventario)) {
			writer.write("Producto,Cantidad\n");
			for (Map.Entry<String, Integer> entry : inventario.entrySet()) {
				writer.write(entry.getKey() + "," + entry.getValue() + "\n");
			}
		}
	}

	private void guardarHistorialEnArchivo(String pathHistorialPedidos, Map<Integer, String> historialPedidos)
			throws IOException {
		try (FileWriter writer = new FileWriter(pathHistorialPedidos)) {
			for (Map.Entry<Integer, String> entry : historialPedidos.entrySet()) {
				writer.write(entry.getKey() + "," + entry.getValue() + "\n");
			}
		}
	}
}
