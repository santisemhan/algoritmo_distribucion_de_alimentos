package main;

import TDAs.api.MapaTDA;
import TDAs.impl.Mapa;
import TDAs.impl.Mapa.NodoArista;
import TDAs.impl.Mapa.NodoMapa;
import apis.ColaPrioridadTDA;
import apis.ConjuntoTDA;
import impl.ColaPrioridadDA;
import impl.ColaPrioridadLD;
import modelo.Camino;
import modelo.Cliente;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static MapaTDA mapa = new Mapa();
    private static List<Cliente> clientes = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        generarClientes();
        generarMapa();        
        planificarRecorrido(1, new ArrayList<Integer>(), Double.MAX_VALUE, 7 * 60, 0, new ArrayList<Integer>());
    }

    private static void planificarRecorrido(Integer clienteActual, List<Integer> visitados, Double cotaFinal, Integer hora, 
    		double kmVisitados,List<Integer> noVisitar) {
    	
    	if(!visitados.contains(clienteActual)) {    		
    		visitados.add(clienteActual);
    	}
    	
    	if(clienteActual == 1 && noVisitar.size() == (clientes.size() - 1)) {
    		mostrarRecorrido(visitados);
    	}
    	
        MapaTDA mapaAux = new Mapa();
        mapaAux.InicializarMapa();
        mapaAux = copiarGrafo(mapa, mapaAux);

        ConjuntoTDA hijos = mapa.Adyacentes(clienteActual);
   
        double cota = Double.MAX_VALUE;
        Integer clienteIdAux = null;
        Integer horarioFin = hora;

        while (!hijos.conjuntoVacio()){
            Integer hijoId = hijos.elegir();
            if(!visitados.contains(hijoId) && !noVisitar.contains(hijoId)) {
            	Cliente clienteHijo = clientes.stream().filter(c -> c.getId().equals(hijoId)).findFirst().orElse(null);
                while(mapaAux.ExisteArista(clienteActual,hijoId)){
                    Integer tiempo = mapaAux.PesoAristaMinutos(clienteActual, hijoId);
                    Double caminoKms= mapaAux.PesoAristaKm(clienteActual, hijoId);
                    if (clienteHijo.getMinutosDisponibleDesde() <= hora + tiempo && hora + tiempo <= clienteHijo.getMinutosDisponibleHasta()) {
                        double cotaAux = 0, km, totalKmRecubrimiento = 0;
                        MapaTDA mapaPrim = new Mapa();
                        mapaPrim.InicializarMapa();
                        mapaPrim = copiarGrafo(mapa, mapaPrim);
                      
                        Integer primerClienteId = 1;
                        Integer ultimoClienteId = hijoId;
                        
                        for(Integer visitado : visitados){
                            mapaPrim.EliminarVertice(visitado);
                        }
                        mapaPrim.EliminarVertice(1);
                        mapaPrim.EliminarVertice(hijoId);

                        km = mapaAux.PesoAristaKm(clienteActual, hijoId) + kmVisitados;
                        //mapaPrim = prim(mapaPrim);  
                   
                        totalKmRecubrimiento = calcularArbolRecubrimiento(mapaPrim);
                        cotaAux = calcularCotaInferior(km, totalKmRecubrimiento, calcularARecubrimiento(primerClienteId, mapa),
                                calcularARecubrimiento(ultimoClienteId, mapa));
                        
                        if(cotaAux < cota && cota <= cotaFinal){                                                                       
                        	cota = cotaAux;
                            kmVisitados = km ;
                            horarioFin = hora+tiempo;
                            clienteIdAux = hijoId;
                        }
                    }
                    mapaAux.ElminarArista(clienteActual,hijoId);
                }
            }
            hijos.sacar(hijoId);
        }
	
        if(noVisitar.size() == clientes.size() - 1) {
        	System.out.println("No existen caminos posibles que satisfacen a todos los clientes");
        }
        
        else{
        	if(visitados.size() == clientes.size() - 1) {
        		Integer ultimo=visitados.get(visitados.size()-2);
        		visitados.remove(clienteActual);
        		noVisitar.add(clienteActual);
        		planificarRecorrido(ultimo, visitados, cota, horarioFin, kmVisitados ,noVisitar);
        	}
        	else if(clienteActual.equals(1) && clienteIdAux == null) {
        		System.out.print(kmVisitados);
        	}
            else if (clienteIdAux==null) {
        		noVisitar.add(clienteActual);
        		visitados.remove(visitados.size()-1);
        		Integer ultimo=visitados.get(visitados.size()-1);
        		kmVisitados = kmVisitados - mapa.PesoAristaKm(ultimo, clienteActual);
        		planificarRecorrido(ultimo, visitados, cotaFinal, horarioFin, kmVisitados ,noVisitar); // restar km
        	}        	
        	else {
        		planificarRecorrido(clienteIdAux, visitados, cotaFinal, horarioFin, kmVisitados,new ArrayList<Integer>());
        	}
        }
    }    

    private static void mostrarRecorrido(List<Integer> visitados){
    	System.out.println("Recorrido final...");
    	/*
    	for (Camino camino: caminos) {
    		System.out.println("De : " + origenDestinoToChar(camino.getIdClienteOrigen()) + " hasta : " + origenDestinoToChar(camino.getIdClienteDestino()) + 
    				" con kms : " + camino.getKm() + " llegando a las  : " + camino.getTiempo());
    	}
    	*/
    	for (Integer v : visitados) {
    		System.out.println(v);
    	}
    }

    public static MapaTDA copiarGrafo(MapaTDA mapa, MapaTDA nuevo){
    	ConjuntoTDA c = mapa.Vertices();
    	while(!c.conjuntoVacio()) {
    		int v = c.elegir();
    		c.sacar(v);
    		nuevo.AgregarVertice(v);
    	}
    	
    	c = mapa.Vertices();
    	while(!c.conjuntoVacio()) {
    		int v = c.elegir();
    		c.sacar(v);
			for(NodoArista n : mapa.getAristas(v)) {
				nuevo.AgregarArista(v, n.nodoDestino.nodo, n.minutos, n.km);
			}   		
    	}
    	
    	
        return nuevo;
    }

    private static double calcularArbolRecubrimiento(MapaTDA m){
        ConjuntoTDA vertices = m.Vertices();

        double sumaTotal = 0;
        while (!vertices.conjuntoVacio()){
            Integer i = vertices.elegir();
            ConjuntoTDA verticesAux = m.Vertices();
            verticesAux.sacar(i);
            while(!verticesAux.conjuntoVacio()){
                Integer v2 = verticesAux.elegir();
                verticesAux.sacar(v2);
                if(m.ExisteArista(i, v2)){
                    sumaTotal += m.PesoAristaKm(i, v2);
                }
            }
            vertices.sacar(i);
        }

        return sumaTotal;

    }

    private static double calcularARecubrimiento(Integer vertice, MapaTDA mapaPrim){
        // Revisar tiempo. Porque no sabemos si la cota tiene que tener en cuenta si llega o no llega al destino en horario

         ConjuntoTDA vertices = mapaPrim.Vertices();
         vertices.sacar(vertice);

         double mejorArista = 0;

         while (!vertices.conjuntoVacio()){
             Integer v = vertices.elegir();
             if(mapaPrim.ExisteArista(vertice, v)){
                 if(mejorArista > mapaPrim.PesoAristaKm(vertice, v)){
                     mejorArista = mapaPrim.PesoAristaKm(vertice, v);
                 }
             }
             vertices.sacar(v);
         }

         return mejorArista;
    }

    private static double calcularCotaInferior(double solucionParcial, double totalPrim, double recubrimientoAPrimero, double ultimoARecubrimiento){
        return solucionParcial + totalPrim + recubrimientoAPrimero + ultimoARecubrimiento;
    }

    private static void generarClientes() throws IOException {
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
    }

    private static Cliente crearCliente(String line){
        String[] lineArray = line.split("\t");
        mapa.AgregarVertice(getIdClienteByChar(lineArray[0]));
        return new Cliente(getIdClienteByChar(lineArray[0]), Integer.parseInt(lineArray[4]), Integer.parseInt(lineArray[5]));
    }

    private static void generarMapa() throws IOException {
        FileReader fr = new FileReader("src/files/Caminos.txt");
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();

        line = br.readLine(); // Salteo los headers

        while(line != null) {
            crearCamino(line);
            line = br.readLine();
        }

        br.close();
    }

    private static void crearCamino(String line){
        String[] lineArray = line.split("\t");
        mapa.AgregarArista(getIdClienteByChar(lineArray[0]), getIdClienteByChar(lineArray[1]),
                Integer.parseInt(lineArray[2]), Double.parseDouble(lineArray[3].replace(",", ".")));   
        mapa.AgregarArista(getIdClienteByChar(lineArray[1]), getIdClienteByChar(lineArray[0]),
                Integer.parseInt(lineArray[2]), Double.parseDouble(lineArray[3].replace(",", "."))); 
    }

    private static Integer getIdClienteByChar(String letra){
        char character = letra.charAt(0);
        return (int) character - 64; // Codigo ascii -  ascii (64)
    }

    private static char origenDestinoToChar(int origenDestino){
        int asciiNumber = origenDestino + 64;
        char letra = (char) asciiNumber;
        return letra;
    }
    
    
}

