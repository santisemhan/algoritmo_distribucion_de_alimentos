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
    		List<Integer> noVisitar, List<Camino> solucionParcial,List<Integer> tiempoAux,int mejorTiempoSolucion) {
        int mejorTiempo=0;
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

        while (!hijos.conjuntoVacio()){
            Integer hijoId = hijos.elegir();
            if(!visitados.contains(hijoId) && !noVisitar.contains(hijoId)) {
            	Cliente clienteHijo = clientes.stream().filter(c -> c.getId().equals(hijoId)).findFirst().orElse(null);
                while(mapaAux.ExisteArista(clienteActual,hijoId)){
                    Integer tiempo = mapaAux.PesoAristaMinutos(clienteActual, hijoId);
                    Double caminoKms= mapaAux.PesoAristaKm(clienteActual, hijoId);
                    if (clienteHijo.getMinutosDisponibleDesde() <= hora + tiempo && hora + tiempo <= clienteHijo.getMinutosDisponibleHasta()) {
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

                        mapaPrim = MapaHelpper.prim(mapaPrim);  
                        
                        for(Camino c : solucionParcial) {
                        	km += c.getKm();
                        }
                        
                        km +=  caminoKms;
                   
                        totalKmRecubrimiento = MapaHelpper.calcularArbolRecubrimiento(mapaPrim);
                        cotaAux = MapaHelpper.calcularCotaInferior(km, totalKmRecubrimiento, MapaHelpper.calcularARecubrimiento(primerClienteId, mapa),
                        		MapaHelpper.calcularARecubrimiento(ultimoClienteId, mapa));
                        
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
	
        if(noVisitar.size() == clientes.size() - 1) {
        	System.out.println("No existen caminos posibles que satisfacen a todos los clientes");
        }
        
        else{
        	if(visitados.size() == clientes.size() - 1) {
        		Integer ultimo=visitados.get(visitados.size()-2);
        		visitados.remove(clienteActual);
        		noVisitar.add(clienteActual);
        		solucionParcial.add(camino);
        		int ultimoTiempo=tiempoAux.remove(tiempoAux.size()-1);
        		mejorTiempoSolucion=horarioFin;
        		horarioFin=horarioFin-ultimoTiempo;
        		planificarRecorrido(ultimo, visitados, cota, horarioFin ,noVisitar, solucionParcial,tiempoAux,mejorTiempoSolucion);
        	}
        	else if(clienteActual.equals(1) && clienteIdAux == null) {        		
        		Integer ultimoVisitado = solucionParcial.get(solucionParcial.size() - 1).getIdClienteDestino();
        		System.out.println(mejorTiempoSolucion); // mejor tiempo
        		System.out.println(  mejorTiempoSolucion + mapa.PesoAristaMinutos(1, solucionParcial.size() - 1)); //mejor tiempo + tiempo ultimo camino
        		Camino vuelta = new Camino(ultimoVisitado, 1,mapa.getAristaMenorPesoKm(ultimoVisitado, 1), mapa.PesoAristaMinutos(1, solucionParcial.size() - 1));        		
        		solucionParcial.add(vuelta);
        		mostrarRecorrido(solucionParcial);
        	}
            else if (clienteIdAux==null) {
        		noVisitar.add(clienteActual);
        		visitados.remove(visitados.size()-1);
        		Integer ultimo=visitados.get(visitados.size()-1);
        		int ultimoTiempo=tiempoAux.remove(tiempoAux.size()-1);
        		horarioFin=horarioFin-ultimoTiempo;
        		planificarRecorrido(ultimo, visitados, cotaFinal, horarioFin ,noVisitar, solucionParcial,tiempoAux,mejorTiempoSolucion); // restar km
        	}        	
        	else { 
        		solucionParcial.add(camino);
        		System.out.println(horarioFin);
        		if (mejorTiempo!=0) {
        			tiempoAux.add(mejorTiempo);
        		}
        		planificarRecorrido(clienteIdAux, visitados, cotaFinal, horarioFin,new ArrayList<Integer>(), solucionParcial,tiempoAux,mejorTiempoSolucion);
        	}
        }
    }    

    public void mostrarRecorrido(List<Camino> recorridoFinal){
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
    	System.out.println("Total minutis: " + totalMinutos);
    }  
}
