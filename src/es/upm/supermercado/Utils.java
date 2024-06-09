package es.upm.supermercado;

import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class Utils 
{
	/**
	 * Permite buscar a todos los agentes que implementa un servicio de un tipo dado
	 * @param agent Agente con el que se realiza la busqueda
	 * @param tipo  Tipo de servidio buscado
	 * @return Listado de agentes que proporciona el servicio
	 * 
	 * 
	 */
    public static DFAgentDescription [] buscarAgentes(Agent agent, String tipo){
        //indico las caracteristicas el tipo de servicio que quiero encontrar
        DFAgentDescription template=new DFAgentDescription();
        ServiceDescription templateSd=new ServiceDescription();
        templateSd.setType(tipo); //como define el tipo el agente coordinador tambien podriamos buscar por nombre
        template.addServices(templateSd);
        
        SearchConstraints sc = new SearchConstraints();
        sc.setMaxResults(Long.MAX_VALUE);
        try
        {
            DFAgentDescription [] results = DFService.search(agent, template, sc);
            return results;
        }
        catch(FIPAException e)
        {
        	e.printStackTrace();
        }
        
        return null;
    }
    
    
    /**
     * Envia un objeto desde el agente indicado a un agente que proporciona un servicio del tipo dado
     * @param agent Agente desde el que se va a enviar el servicio
     * @param tipo Tipo del servicio buscado
     * @param objeto Mensaje a Enviar
     */
    public static void enviarMensaje(Agent agent, String tipo, Object objeto)
    {
        
    	//Buscamos en el ddf todos los agentes que ofrecen el servicio tipo
    	DFAgentDescription[] dfd;
        dfd=buscarAgentes(agent, tipo);
        
        //En este caso definimos un mensaje de tipo REQUEST
        //La ontologia es del tipo "ontologia"
        //El mensaje se envia al agente a todos los agente que hemos encontrado en el DF y como contenido se pasa objeto
        
        try
        {
            if(dfd!=null)
            {
            	ACLMessage aclMessage = new ACLMessage(ACLMessage.REQUEST);
            	
            	for(int i=0;i<dfd.length;i++)
	        		aclMessage.addReceiver(dfd[i].getName());
            	
                aclMessage.setOntology("ontologia");
                //el lenguaje que se define para el servicio
                aclMessage.setLanguage(new SLCodec().getName());
                //el mensaje se transmita en XML
                aclMessage.setEnvelope(new Envelope());
				//cambio la codificacion de la carta
				aclMessage.getEnvelope().setPayloadEncoding("ISO8859_1");
                //aclMessage.getEnvelope().setAclRepresentation(FIPANames.ACLCodec.XML); 
        		aclMessage.setContentObject((Serializable)objeto);
        		agent.send(aclMessage);       		
            }
        }
        catch(IOException e)
        {
            //JOptionPane.showMessageDialog(null, "Agente "+getLocalName()+": "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Permite buscar los agents que dan un servicio de un determinado tipo. Devuelve el primero de ellos.
     * @param agent Agentes desde el que se realiza la busqueda
     * @param tipo Tipo de servicio buscado
     * @return Primer agente que proporciona el servicio
     */
    public static DFAgentDescription buscarAgente(Agent agent, String tipo)
    {
        //indico las caracteristicas el tipo de servicio que quiero encontrar
        DFAgentDescription template=new DFAgentDescription();
        ServiceDescription templateSd=new ServiceDescription();
        templateSd.setType(tipo); //como define el tipo el agente coordinador tamiben podriamos buscar por nombre
        template.addServices(templateSd);
        
        SearchConstraints sc = new SearchConstraints();
        Long num = (long) 1;
        sc.setMaxResults(num);
        
        try
        {
            DFAgentDescription [] results = DFService.search(agent, template, sc);
            if (results.length > 0) 
            {
                //System.out.println("Agente "+agent.getLocalName()+" encontro los siguientes agentes");
                for (int i = 0; i < results.length; ++i) 
                {
                    DFAgentDescription dfd = results[i];
                    AID provider = dfd.getName();
                    
                    //un mismo agente puede proporcionar varios servicios, solo estamos interasados en "tipo"
                    Iterator<?> it = dfd.getAllServices();
                    while (it.hasNext())
                    {
                        ServiceDescription sd = (ServiceDescription) it.next();
                        if (sd.getType().equals(tipo))
                        {
                            System.out.println("- Servicio \""+sd.getName()+"\" proporcionado por el agente "+provider.getName());
                            return dfd;
                        }
                    }
                }
            }	
            else
            {
                //JOptionPane.showMessageDialog(null, "Agente "+getLocalName()+" no encontro ningun servicio buscador", "Error", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        catch(FIPAException e)
        {
            //JOptionPane.showMessageDialog(null, "Agente "+getLocalName()+": "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        	e.printStackTrace();
        }
        
        return null;
    }
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
}
