package main;

import java.util.ArrayList;
import java.util.List;

import TDAs.api.MapaTDA;
import TDAs.impl.Mapa;
import apis.ConjuntoTDA;
import modelo.Camino;
import modelo.Cliente;

public class Resolucion {
	
	private List<Cliente> clientes;
	private MapaTDA mapa;
	
	
    public Resolucion(List<Cliente> clientes, MapaTDA mapa) {
    	this.clientes = clientes;
    	this.mapa = mapa;
    }
	
    public void planificarRecorrido(Integer clienteActual, List<Integer> visitados, Double cotaFinal, Integer hora, 
    		int[][] matriz, List<Camino> solucionAux, List<Camino> solucionParcial,List<Integer> tiempoAux) {

    	if(!visitados.contains(clienteActual)) {    		
    		visitados.add(clienteActual);
    	}
    	   	     	    	
        MapaTDA mapaAux = new Mapa();
        mapaAux.InicializarMapa();
        mapaAux = MapaHelpper.copiarMapa(mapa, mapaAux);

        ConjuntoTDA hijos = mapa.Adyacentes(clienteActual);
   
        Camino camino = null;
        double cota = Double.MAX_VALUE;
        Integer clienteIdAux = null;
        Integer horarioFin = hora;
        int mejorTiempo= 0;

        while (!hijos.conjuntoVacio()){
            Integer hijoId = hijos.elegir();
            if(!visitados.contains(hijoId) && matriz[clienteActual-1][hijoId-1]==0) {
            	Cliente clienteHijo = clientes.stream().filter(c -> c.getId().equals(hijoId)).findFirst().orElse(null);
                while(mapaAux.ExisteArista(clienteActual,hijoId)){
                    Integer tiempo = mapaAux.PesoAristaMinutos(clienteActual, hijoId);
                    Double caminoKms= mapaAux.PesoAristaKm(clienteActual, hijoId);
                    if (1>0) {
                        double cotaAux = 0, km = 0, totalKmRecubrimiento = 0;
                        MapaTDA mapaPrim = new Mapa();
                        mapaPrim.InicializarMapa();
                        mapaPrim = MapaHelpper.copiarMapa(mapa, mapaPrim);
                      
                        Integer primerClienteId = 1;
                        Integer ultimoClienteId = hijoId;
                        
                        for(Integer visitado : visitados){
                            mapaPrim.EliminarVertice(visitado);
                        }
                        mapaPrim.EliminarVertice(1);
                        mapaPrim.EliminarVertice(hijoId);
                                               
                        ConjuntoTDA verticesPrim = mapaPrim.Vertices();
                        
                        if(!verticesPrim.conjuntoVacio()) {                        	
                        	mapaPrim = MapaHelpper.prim(mapaPrim);
                        }
                                                
                        for(Camino c : solucionAux) {
                        	km += c.getKm();
                        }
                        
                        km += caminoKms;                                           
                   
                        totalKmRecubrimiento = MapaHelpper.calcularArbolRecubrimiento(mapaPrim);
                        cotaAux = MapaHelpper.calcularCotaInferior(km, totalKmRecubrimiento, 
                        		MapaHelpper.calcularARecubrimiento(primerClienteId, mapa, verticesPrim),
                        		MapaHelpper.calcularARecubrimiento(ultimoClienteId, mapa, verticesPrim));
                        
                        if(cotaAux < cota && cotaAux < cotaFinal){ 
                        	cota = cotaAux;
                            horarioFin = hora+tiempo;
                            mejorTiempo=tiempo;
                            clienteIdAux = hijoId;
                            camino = new Camino(clienteActual, clienteIdAux, caminoKms, tiempo);
                        }
                    }
                    mapaAux.ElminarArista(clienteActual,hijoId);
                }
            }
            hijos.sacar(hijoId);
        }
        if(clienteIdAux==null && visitados.size()==clientes.size()-1 ) {
        	visitados.remove(visitados.size()-1);
		    Integer ultimo=visitados.get(visitados.size()-1);
		    matriz[ultimo-1][clienteActual-1]=1;
		    hijos=mapa.Adyacentes(clienteActual);
		    while (!hijos.conjuntoVacio()) {
		    	int elem=hijos.elegir();
		    	hijos.sacar(elem);
		    	matriz[clienteActual-1][elem-1]=0;
		    }
		    planificarRecorrido(clienteActual, visitados, cotaFinal, hora, matriz, solucionAux, solucionParcial,tiempoAux) ; 
        }
        else if (visitados.size()==clientes.size()-1) {
        	Integer ultimo=visitados.get(visitados.size()-2);
            visitados.remove(clienteActual);
            solucionParcial.add(camino);
            matriz[ultimo-1][clienteActual-1]=1;
		    hijos=mapa.Adyacentes(clienteActual);
		    while (!hijos.conjuntoVacio()) {
		    	int elem=hijos.elegir();
		    	hijos.sacar(elem);
		    	matriz[clienteActual-1][elem-1]=0;
		    }
            planificarRecorrido(ultimo, visitados, cota, horarioFin ,matriz, solucionAux,solucionParcial,tiempoAux);
        }
        else if(clienteActual.equals(1) && clienteIdAux==null){
        	Integer ultimoVisitado = solucionParcial.get(solucionParcial.size() - 1).getIdClienteDestino();
            Camino vuelta = new Camino(ultimoVisitado, 1,mapa.getAristaMenorPesoKm(ultimoVisitado, 1), mapa.PesoAristaMinutos(1, solucionParcial.size() - 1));
            solucionParcial.add(vuelta);
            mostrarRecorrido(solucionParcial, horarioFin);
        }
        else if(clienteIdAux==null) { // no hay viable
		    visitados.remove(visitados.size()-1);
		    Integer ultimo=visitados.get(visitados.size()-1);
		    matriz[ultimo-1][clienteActual-1]=1;
		    hijos=mapa.Adyacentes(clienteActual);
		    while (!hijos.conjuntoVacio()) {
		    	int elem=hijos.elegir();
		    	hijos.sacar(elem);
		    	matriz[clienteActual-1][elem-1]=0;
		    }
		    planificarRecorrido(ultimo, visitados, cotaFinal, hora, matriz, solucionAux, solucionParcial,tiempoAux) ; 
	   }
	   else {
		   solucionParcial.add(camino);
		   planificarRecorrido(clienteIdAux, visitados, cotaFinal, horarioFin, matriz,solucionAux, solucionParcial,tiempoAux);
	   }
    }  

    public void mostrarRecorrido(List<Camino> recorridoFinal, Integer horarioFin){
    	Double totalKm = (double)0;
    	Double totalMinutos = (double)0;
    	for(Camino c : recorridoFinal) {
    		totalKm += c.getKm();
    		totalMinutos += c.getTiempo();
    		System.out.println("De " + FileUpload.origenDestinoToChar(c.getIdClienteOrigen()) + " a " + 
    		FileUpload.origenDestinoToChar(c.getIdClienteDestino()) +  " km: " + c.getKm() + 
    		" minutos: " + c.getTiempo());
    	}
    	 	
    	System.out.println("----------------------------");
    	System.out.println("Total km: " + totalKm);
    	System.out.println("Total minutos: " + (totalMinutos + 7 * 60));
    	System.out.println("Total real: " + horarioFin);
    }  
}
