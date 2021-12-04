package main;

import TDAs.api.MapaTDA;
import TDAs.impl.Mapa;
import TDAs.impl.Mapa.NodoArista;
import apis.ConjuntoTDA;

public class MapaHelpper {
	
	public static MapaTDA copiarMapa(MapaTDA mapa, MapaTDA nuevo){
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

    public static double calcularArbolRecubrimiento(MapaTDA m){
        return m.getAllkmArista();
    }

    public static double calcularARecubrimiento(Integer vertice, MapaTDA mapa, ConjuntoTDA verticesPrim){
         ConjuntoTDA vertices = mapa.Vertices();
         vertices.sacar(vertice);

         double mejorArista = Double.MAX_VALUE;

         while (!vertices.conjuntoVacio()){
             Integer v = vertices.elegir();
             if(verticesPrim.pertenece(v)){
                 if(mejorArista > mapa.getAristaMenorPesoKm(vertice, v)){
                     mejorArista = mapa.getAristaMenorPesoKm(vertice, v);
                 }
             }
             vertices.sacar(v);
         }

         if(mejorArista == Double.MAX_VALUE) {
        	 return 0;
         }
         
         return mejorArista;
    }
    
    public static double calcularCotaInferior(double solucionParcial, double totalPrim, double recubrimientoAPrimero, double ultimoARecubrimiento){
    	return solucionParcial + totalPrim + recubrimientoAPrimero + ultimoARecubrimiento;
    }
}
