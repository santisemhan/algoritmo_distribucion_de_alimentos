package TDAs.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import TDAs.api.MapaTDA;
import apis.ConjuntoTDA;
import impl.ConjuntoLD;
import modelo.AristaDTO;

public class Mapa implements MapaTDA {
    public class NodoMapa{
        public int nodo;
        public NodoArista arista;
        public NodoMapa sigNodo;
    }

    public class NodoArista{
        public double km;
        public int minutos;
        public NodoMapa nodoDestino;
        public NodoArista sigArista;
    }

    NodoMapa origen;

    @Override
    public void InicializarMapa() {
        origen = null;
    }

    @Override
    public void AgregarVertice(int v) {
        NodoMapa aux = new NodoMapa();
        aux.nodo = v;
        aux.arista = null;
        aux.sigNodo = origen;
        origen = aux;
    }

    private NodoMapa Vert2Nodo(int v){
        NodoMapa aux = origen;
        while(aux != null && aux.nodo != v){
            aux = aux.sigNodo;
        }
        return aux;
    }

    @Override
    public void AgregarArista(int v1, int v2, int minutos, double km) {
        NodoMapa n1 = Vert2Nodo(v1);
        NodoMapa n2 = Vert2Nodo(v2);
        NodoArista aux = new NodoArista();
        aux.km = km;
        aux.minutos = minutos;
        aux.nodoDestino = n2;
        aux.sigArista = n1.arista;
        n1.arista = aux;
    }

    private void EliminarAristaNodo(NodoMapa nodo, int v){
        NodoArista aux = nodo.arista;
        if(aux != null){
            if(aux.nodoDestino.nodo == v){
                nodo.arista = aux.sigArista;
            }
            else{
                while(aux.sigArista != null && aux.sigArista.nodoDestino.nodo != v){
                    aux = aux.sigArista;
                }
                if(aux.sigArista != null){
                    aux.sigArista = aux.sigArista.sigArista;
                }
            }
        }
    }

    @Override
    public void EliminarVertice(int v) {
       if(origen.nodo == v){
           origen = origen.sigNodo;
       }
       NodoMapa aux = origen;
       while(aux != null){
           this.EliminarAristaNodo(aux, v);
           if(aux.sigNodo != null && aux.sigNodo.nodo == v){
               aux.sigNodo = aux.sigNodo.sigNodo;
           }
           aux = aux.sigNodo;
       }
    }

    @Override
    public void ElminarArista(int v1, int v2) {
        NodoMapa n1 = Vert2Nodo(v1);
        EliminarAristaNodo(n1, v2);
        
        NodoMapa n2 = Vert2Nodo(v2);
        EliminarAristaNodo(n2, v1);
    }

    @Override
    public double PesoAristaKm(int v1, int v2) {   	
        NodoMapa n1 = Vert2Nodo(v1);
        NodoArista aux = n1.arista;
       
    	while(aux.nodoDestino.nodo != v2){
            aux = aux.sigArista;
        }
        
        return aux.km;
    }

    @Override
    public int PesoAristaMinutos(int v1, int v2) {
        NodoMapa n1 = Vert2Nodo(v1);
        NodoArista aux = n1.arista;
        while (aux.nodoDestino.nodo != v2) {
            aux = aux.sigArista;
        }
        
        return aux.minutos;
    }

    @Override
    public boolean ExisteArista (int v1, int v2){
        NodoMapa n1 = Vert2Nodo(v1);
        NodoArista aux = n1.arista;
        while(aux != null && aux.nodoDestino.nodo != v2){
            aux = aux.sigArista;
        }                     
        return aux != null;
    }

    @Override
    public ConjuntoTDA Vertices() {
        ConjuntoTDA c = new ConjuntoLD();
        c.inicializarConjunto();
        NodoMapa aux = origen;
        while(aux != null){
            c.agregar(aux.nodo);
            aux = aux.sigNodo;
        }
        return c;
    }

    @Override
    public ConjuntoTDA Adyacentes(int v) {
        ConjuntoTDA conjResultante = new ConjuntoLD();
        conjResultante.inicializarConjunto();
        ConjuntoTDA vertices=this.Vertices();
        vertices.sacar(v);
        while (!vertices.conjuntoVacio()) {
        	Integer v1=vertices.elegir();
        	vertices.sacar(v1);
        	if (this.ExisteArista(v, v1)) {
        		conjResultante.agregar(v1);
        	}
        	
        }
        return conjResultante;
    }
    
    public List<NodoArista> getAristas(int v){
    	NodoArista aux = Vert2Nodo(v).arista;
    	List<NodoArista> result = new ArrayList();
    	while(aux != null) {
    		result.add(aux);
    		aux = aux.sigArista;
    	}
    	return result;
    }
    
    public double getAristaMenorPesoKm(int v, int v1) {
    	NodoArista aux = Vert2Nodo(v).arista;
    	double mejorArista = Double.MAX_VALUE;
    	while(aux != null) {
    		if(mejorArista > aux.km && aux.nodoDestino.nodo == v1) {
    			mejorArista = aux.km;
    		}
    		aux = aux.sigArista;
    	}
    	
    	return mejorArista;
    }

    public List<AristaDTO> getAristasSort(){
    	List<AristaDTO> result = new ArrayList<AristaDTO>();
    	ConjuntoTDA c = this.Vertices();
    	
    	while(!c.conjuntoVacio()) {
    		int v = c.elegir();
    		c.sacar(v);
    		List<AristaDTO> aristasNodo = new ArrayList<AristaDTO>();
    		
    		for(NodoArista na : this.getAristas(v)) {
    			AristaDTO a = new AristaDTO();
    			a.origen = v;
    			a.destino = na.nodoDestino.nodo;
    			a.km = na.km;
    		}
    		
    		result.addAll(aristasNodo);
    	}
    	
        Collections.sort(result, (e1,e2) -> Double.compare(e1.km, e2.km));
    	
    	return result;
    }
}
