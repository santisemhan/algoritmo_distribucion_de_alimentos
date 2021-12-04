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

    public static double calcularARecubrimiento(Integer vertice, MapaTDA mapa, ConjuntoTDA verticesPrim){
         ConjuntoTDA vertices = mapa.Vertices();
         vertices.sacar(vertice);

         double mejorArista = Double.MAX_VALUE;

         while (!vertices.conjuntoVacio()){
             Integer v = vertices.elegir();
             if(verticesPrim.pertenece(v)){
                 if(mejorArista > mapa.PesoAristaKm(vertice, v)){
                     mejorArista = mapa.PesoAristaKm(vertice, v);
                 }
             }
             vertices.sacar(v);
         }

         return mejorArista;
    }
    
    public static MapaTDA prim(MapaTDA mapaPrim) {
    	MapaTDA mapaResultado = new Mapa();
    	mapaResultado.InicializarMapa();
    	
    	ConjuntoTDA vertices = mapaPrim.Vertices();
    	while(!vertices.conjuntoVacio()) {
    		int v = vertices.elegir();
    		vertices.sacar(v);
    		mapaResultado.AgregarVertice(v);
    	}
    	
    	vertices = mapaPrim.Vertices();
    	while(!vertices.conjuntoVacio()){
    		int v = vertices.elegir();
    		vertices.sacar(v);
    		ConjuntoTDA verticesAux = mapaPrim.Vertices();
    		verticesAux.sacar(v);
    		while(!verticesAux.conjuntoVacio()) {
    			int vAux = verticesAux.elegir();
    			verticesAux.sacar(vAux);
    			double pesoArista = mapaPrim.getAristaMenorPesoKm(v, vAux);
    			mapaResultado.AgregarArista(v, vAux, 0, pesoArista);
    		}
    	}
    	
    	return mapaResultado;
    }
    
    public static double calcularCotaInferior(double solucionParcial, double totalPrim, double recubrimientoAPrimero, double ultimoARecubrimiento){
    	return solucionParcial + totalPrim + recubrimientoAPrimero + ultimoARecubrimiento;
    }
}
