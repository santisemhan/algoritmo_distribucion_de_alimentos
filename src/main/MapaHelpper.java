package main;

import TDAs.api.MapaTDA;
import TDAs.impl.Mapa;
import TDAs.impl.Mapa.NodoArista;
import apis.ConjuntoTDA;
import impl.ConjuntoLD;

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
    
    public static MapaTDA prim (MapaTDA g) {
        int vertice;
        int aux_vertice;
        int mejor_vertice;
        double mejor_distancia;
        MapaTDA resultado = new Mapa();
        resultado.InicializarMapa();

        ConjuntoTDA vertices = g.Vertices();
             
        vertice = vertices.elegir();
        vertices.sacar(vertice);
        resultado.AgregarVertice(vertice);

        while(!vertices.conjuntoVacio()){
            aux_vertice = vertices.elegir();
            vertices.sacar(aux_vertice);
            resultado.AgregarVertice(aux_vertice);
            if(g.ExisteArista(aux_vertice, vertice)){
            	NodoArista na = g.getAristaObjMenorPesoKm(aux_vertice, vertice);
                resultado.AgregarArista(aux_vertice, vertice, na.minutos, na.km);
            }
        }

        ConjuntoTDA pendientes = g.Vertices();
        pendientes.sacar(vertice);

        ConjuntoTDA aux_pendientes = new ConjuntoLD();
        aux_pendientes.inicializarConjunto();

        while (!pendientes.conjuntoVacio()){
            mejor_distancia = 0;
            mejor_vertice = 0;

            while (!pendientes.conjuntoVacio()){
                aux_vertice = pendientes.elegir();
                pendientes.sacar(aux_vertice);
                aux_pendientes.agregar(aux_vertice);
                if((!resultado.Adyacentes(aux_vertice).conjuntoVacio()) &&
                    (mejor_distancia == 0 || (mejor_distancia > resultado.PesoAristaKm(aux_vertice , resultado.Adyacentes(aux_vertice ).elegir())))){
                    mejor_distancia = resultado.getAristaMenorPesoKm(aux_vertice,resultado.Adyacentes(aux_vertice).elegir());
                    mejor_vertice = aux_vertice;
                }
            }

            vertice = mejor_vertice;
            aux_pendientes.sacar(vertice);

            while (!aux_pendientes.conjuntoVacio()){
                aux_vertice = aux_pendientes.elegir();
                aux_pendientes.sacar(aux_vertice);
                pendientes.agregar(aux_vertice);
                if(g.ExisteArista(aux_vertice, vertice)){
                    if(resultado.Adyacentes(aux_vertice).conjuntoVacio()){
                    	NodoArista na = g.getAristaObjMenorPesoKm(aux_vertice, vertice);
                        resultado.AgregarArista(aux_vertice, vertice, na.minutos, na.km);
                    }else {
                        if(resultado.getAristaMenorPesoKm(aux_vertice, resultado.Adyacentes(aux_vertice).elegir()) > g.getAristaMenorPesoKm(aux_vertice, vertice)){
                            resultado.ElminarArista(aux_vertice, resultado.Adyacentes(aux_vertice).elegir());
                            NodoArista na = g.getAristaObjMenorPesoKm(aux_vertice, vertice);
                            resultado.AgregarArista(aux_vertice, vertice, na.minutos, na.km);
                        }
                    }
                }
            }
        }

        return resultado;
    }
}
