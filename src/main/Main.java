package main;

import TDAs.api.MapaTDA;
import TDAs.impl.Mapa;
import apis.ColaPrioridadTDA;
import apis.ConjuntoTDA;
import impl.ColaPrioridadAO;
import impl.ConjuntoLD;
import modelo.Camino;
import modelo.Cliente;
import modelo.Par;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

    private static MapaTDA mapa = new Mapa();
    private static List<Cliente> clientes = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        generarClientes();
        generarMapa();
        
        planificarRecorrido(1, new ArrayList<Integer>(), (double)0, 7 * 60, 0, new ArrayList<Integer>());
    }

    private static void planificarRecorrido(Integer clienteActual, List<Integer> visitados, Double cotaInf, Integer hora, double kmVisitados,List<Integer> noVisitar) {
    	
        MapaTDA mapaAux = new Mapa();
        mapaAux.InicializarMapa();
        mapaAux = copiarGrafo(mapa, mapaAux);

        ConjuntoTDA hijos = mapa.Adyacentes(clienteActual);
   
        double cota = cotaInf;
        Integer clienteIdAux = null;
        Integer horarioFin = hora;

        while (!hijos.conjuntoVacio()){
            Integer hijoId = hijos.elegir();
            if(!visitados.contains(hijoId) && !noVisitar.contains(hijoId)) {
            	Cliente clienteHijo = clientes.stream().filter(c -> c.getId().equals(hijoId)).findFirst().orElse(null);
                while(mapaAux.ExisteArista(clienteActual,hijoId)){
                    Integer tiempo = mapaAux.PesoAristaMinutos(clienteActual, hijoId);
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
                        mapaPrim = prim(mapaPrim);  // ver 
                   
                        totalKmRecubrimiento = calcularArbolRecubrimiento(mapaPrim);
                        cotaAux = calcularCotaInferior(km, totalKmRecubrimiento, calcularARecubrimiento(primerClienteId, mapaPrim),
                                calcularARecubrimiento(ultimoClienteId, mapaPrim));
                        
                        if(cotaAux < cota){
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

        if(visitados.size() == clientes.size()){
            mostrarRecorrido(visitados); // CAMBIAR A LISTA CAMINOS
        }
        else{
        	if (clienteIdAux==null) {
        		noVisitar.add(clienteActual);
        		Integer ultimo=visitados.get(visitados.size()-1);
        		planificarRecorrido(ultimo, visitados, cota, horarioFin, kmVisitados,noVisitar);
        	}
        	else {
        		visitados.add(clienteActual);
        		planificarRecorrido(clienteIdAux, visitados, cota, horarioFin, kmVisitados,new ArrayList<Integer>());
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

    public static MapaTDA copiarGrafo(MapaTDA grOrigen, MapaTDA grDestino) {
        ConjuntoTDA con = grOrigen.Vertices();
        ConjuntoTDA con2 = grOrigen.Vertices();
        ConjuntoTDA aux = new ConjuntoLD();
        aux.inicializarConjunto();
        while(!con.conjuntoVacio()) {
            int x = con.elegir();
            grDestino.AgregarVertice(x);
            con.sacar(x);
        }
        con = grOrigen.Vertices();
        con2 = grOrigen.Vertices();
        while(!con.conjuntoVacio()) {
            int o = con.elegir();
            con.sacar(o);
            con2.sacar(o);
            while(!con2.conjuntoVacio()) {
                int d = con2.elegir();
                aux.agregar(d);
                con2.sacar(d);
                if (grOrigen.ExisteArista(o, d) && !grDestino.ExisteArista(o, d)) {
                    double pk = grOrigen.PesoAristaKm(o, d);
                    int pm = grOrigen.PesoAristaMinutos(o, d);
                    grDestino.AgregarArista(o, d, pm,pk);
                } else if (grOrigen.ExisteArista(d, o) && !grDestino.ExisteArista(d, o)) {
                    double pk = grOrigen.PesoAristaKm(d, o);
                    int pm = grOrigen.PesoAristaMinutos(d, o);
                    grDestino.AgregarArista(d, o,pm, pk);
                }
            }
            while(!aux.conjuntoVacio()) {
                int x = aux.elegir();
                con2.agregar(x);
                aux.sacar(x);

            }
        }

        return grDestino;

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

    public static MapaTDA prim (MapaTDA g) {
    	MapaTDA mapaAux = new Mapa();
        mapaAux.InicializarMapa();
        
        mapaAux=copiarGrafo(g,mapaAux);
        
        MapaTDA resultado = new Mapa();
        resultado.InicializarMapa();

        ConjuntoTDA vertices = g.Vertices();
        
        while (!vertices.conjuntoVacio()) {
        	int elemento=vertices.elegir();
        	vertices.sacar(elemento);
        	resultado.AgregarVertice(elemento);
        }
        
        vertices= g.Vertices();
        
        
        while(!vertices.conjuntoVacio()) {
        	int elemento=vertices.elegir();
        	vertices.sacar(elemento);
        	ConjuntoTDA verticesAux = g.Vertices();
        	verticesAux.sacar(elemento);
        	while (!verticesAux.conjuntoVacio()) {
        		int aux = verticesAux.elegir();
        		verticesAux.sacar(aux);
        		if (mapaAux.ExisteArista(elemento, aux)) {
        			if (!resultado.ExisteArista(elemento, aux)) {
        				resultado.AgregarArista(aux, elemento , mapaAux.PesoAristaMinutos(elemento, aux) , mapaAux.PesoAristaKm(aux, elemento) );	
        			}
        			else if (resultado.PesoAristaKm(aux, elemento)  > mapaAux.PesoAristaKm(aux, elemento)) {
        					resultado.ElminarArista(elemento, aux);
        					resultado.AgregarArista(aux, elemento , mapaAux.PesoAristaMinutos(elemento, aux) , mapaAux.PesoAristaKm(aux, elemento) );	
        				}
        			}
        			mapaAux.ElminarArista(elemento, aux);
        		}
        	}
        
        return resultado;
    }
    
    /*
    public static MapaTDA caminoMinimo(MapaTDA grafo) {
        MapaTDA grafoR = new Mapa();
        grafoR.InicializarMapa();
        ConjuntoTDA conjunto = grafo.Vertices();
        Map<Integer,Integer> conjuto3 = new HashMap<>();
        int cantidadVertices = 0;
        int v = 0; // Número de nodos
        while(!conjunto.conjuntoVacio()){
            int valor = conjunto.elegir();
            grafoR.AgregarVertice(valor);
            conjuto3.put(valor,valor);
            cantidadVertices++;
            conjunto.sacar(valor);
        }
        ColaPrioridadTDA cola = new ColaPrioridadAO();
        ColaPrioridadTDA aux = new ColaPrioridadAO();
        aux.inicializarCola();
        cola.inicializarCola();
        conjunto = grafo.Vertices();
        ConjuntoTDA conjunto1 = grafo.Vertices();
        ConjuntoTDA aux1 = new ConjuntoLD();
        aux1.inicializarConjunto();
        while(!conjunto.conjuntoVacio()) {
            int o = conjunto.elegir();
            conjunto.sacar(o);
            conjunto1.sacar(o);
            while(!conjunto1.conjuntoVacio()) {
                int d = conjunto1.elegir();
                aux1.agregar(d);
                conjunto1.sacar(d);
                if (grafo.ExisteArista(o, d) && !existe(cola,arista(o,d,grafo))) {
                    cola.acolarPrioridad(arista(o,d,grafo),grafo.peso(o,d));
                }
            }
            while(!aux1.conjuntoVacio()) {
                int x = aux1.elegir();
                conjunto1.agregar(x);
                aux1.sacar(x);
            }
        }
        while (cantidadVertices > 1 && !cola.colaVacia()){
            Edge arista = cola.primero();
            cola.desacolar();
            if(conjuto3.get(arista.from) != conjuto3.get(arista.to)){
                cantidadVertices--;
                Set<Integer> integerSet = conjuto3.keySet();
                for(Integer e: integerSet){
                    if (conjuto3.get(e) == conjuto3.get(arista.to)){
                        conjuto3.replace(e,conjuto3.get(e));
                    }
                }
                grafoR.agregarArista(arista.from, arista.to, arista.data);
            }
        }
        return grafoR;
    }

    private static Par arista(int o, int d, MapaTDA grafo) {
        Par p = new Par();
        if(grafo.ExisteArista(o,d)) {
            p.from = o;
            p.to = d;
            p.data = grafo.PesoAristaKm(o, d);
            p.minutos=grafo.PesoAristaMinutos(o, d);
        }
        return p;
    }

    private static boolean existe(ColaPrioridadTDA cola, Par arista) {
        boolean exist = false;
        ColaPrioridadTDA copia = new ColaPrioridadAO();
        copia.inicializarCola();
        while (!cola.colaVacia()){
            copia.acolarPrioridad(cola.primero(), cola.prioridad());
            if(arista.from == cola.primero().from && arista.to == cola.primero().to && arista.data == cola.primero().data ){
                exist = true;
            }
            cola.desacolar();
        }
        while (!copia.colaVacia()){
            cola.acolarPrioridad(copia.primero(), copia.prioridad());
            copia.desacolar();
        }
        return exist;
    }
    */
    
}

