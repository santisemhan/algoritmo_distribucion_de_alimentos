package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import TDAs.api.MapaTDA;
import TDAs.impl.Mapa;
import modelo.Cliente;

public class FileUpload {
	
	private static MapaTDA mapa = new Mapa();
    private static List<Cliente> clientes = new ArrayList<>();
	
	 public static List<Cliente> generarClientes() throws IOException {
	        FileReader fr = new FileReader("src/files/DatosClientes.txt");
	        BufferedReader br = new BufferedReader(fr);
	        String line = br.readLine();

	        line = br.readLine(); // Salteo los headers
	        clientes.add(new Cliente(0, 0, 0)); // Creo el centro de distribucion
	        mapa.AgregarVertice(1);
	        line = br.readLine();

	        while(line != null) {
	            Cliente cliente = crearCliente(line);
	            clientes.add(cliente);
	            line = br.readLine();
	        }

	        br.close();
	        
	        return clientes;
	 }

	    public static Cliente crearCliente(String line){
	        String[] lineArray = line.split("\t");
	        mapa.AgregarVertice(getIdClienteByChar(lineArray[0]));
	        return new Cliente(getIdClienteByChar(lineArray[0]), Integer.parseInt(lineArray[4]), Integer.parseInt(lineArray[5]));
	    }

	    public static MapaTDA generarMapa() throws IOException {
	        FileReader fr = new FileReader("src/files/Caminos.txt");
	        BufferedReader br = new BufferedReader(fr);
	        String line = br.readLine();

	        line = br.readLine(); // Salteo los headers

	        while(line != null) {
	            crearCamino(line);
	            line = br.readLine();
	        }

	        br.close();
	        return mapa;
	    }

	    public static void crearCamino(String line){
	        String[] lineArray = line.split("\t");
	        mapa.AgregarArista(getIdClienteByChar(lineArray[0]), getIdClienteByChar(lineArray[1]),
	                Integer.parseInt(lineArray[2]), Double.parseDouble(lineArray[3].replace(",", ".")));   
	        mapa.AgregarArista(getIdClienteByChar(lineArray[1]), getIdClienteByChar(lineArray[0]),
	                Integer.parseInt(lineArray[2]), Double.parseDouble(lineArray[3].replace(",", "."))); 
	    }

	    public static Integer getIdClienteByChar(String letra){
	        char character = letra.charAt(0);
	        return (int) character - 64; // Codigo ascii -  ascii (64)
	    }

	    public static char origenDestinoToChar(int origenDestino){
	        int asciiNumber = origenDestino + 64;
	        char letra = (char) asciiNumber;
	        return letra;
	    }
	    	    
}
