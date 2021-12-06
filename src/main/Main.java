package main;

import java.util.ArrayList;
import java.util.List;

import TDAs.api.MapaTDA;
import modelo.Camino;
import modelo.Cliente;

public class Main {

    public static void main(String[] args) throws Exception {
    	
    	List<Cliente> clientes = FileUpload.generarClientes();
    	MapaTDA mapa = FileUpload.generarMapa();   
    	
    	Resolucion resultado = new Resolucion(clientes,mapa);
    	
        resultado.planificarRecorrido(1, new ArrayList<Integer>(), Double.MAX_VALUE, 8 * 60, MapaHelpper.crearMatriz(clientes.size()),
        		 new ArrayList<Camino>(), new ArrayList<Camino>(),new ArrayList<Integer>());
        		
    }
       
}

