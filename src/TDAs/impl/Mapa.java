package TDAs.impl;

import TDAs.api.MapaTDA;
import apis.ConjuntoTDA;
import impl.ConjuntoLD;

public class Mapa implements MapaTDA {
    class NodoMapa{
        int nodo;
        NodoArista arista;
        NodoMapa sigNodo;
    }

    class NodoArista{
        double km;
        int minutos;
        NodoMapa nodoDestino;
        NodoArista sigArista;
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
        ConjuntoTDA ws;
        ConjuntoTDA xs= this.Vertices();
        if (xs.pertenece(v)) {
            xs.sacar(v);
            while(!xs.conjuntoVacio()) {//Candidatos a x
                int x =xs.elegir();
                if(this.ExisteArista(v,x)) {
                    ws=this.Vertices();
                    ws.sacar(v);
                    while(!ws.conjuntoVacio()) {//Candidatos a w
                        int w=ws.elegir();
                        if (this.ExisteArista(x, w))// Por cada x debo verificar que exista un w
                            conjResultante.agregar(w);
                        ws.sacar(w);
                    }
                }
                xs.sacar(x);
            }
        }
        return conjResultante;
    }

}
