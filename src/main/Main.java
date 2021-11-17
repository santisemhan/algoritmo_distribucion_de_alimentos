package main;

import TDAs.api.MapaTDA;
import TDAs.impl.Mapa;
import apis.ConjuntoTDA;
import impl.ConjuntoLD;
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

        planificarRecorrido(1, new ArrayList<Integer>(), (double)0, 7 * 60, 0);
    }

    private static void planificarRecorrido(Integer clienteActual, List<Integer> visitados, Double cotaInf, Integer hora, double kmVisitados) {
        MapaTDA mapaAux = new Mapa();
        mapaAux.InicializarMapa();
        mapaAux = copiarGrafo(mapa, mapaAux);

        visitados.add(clienteActual);

        ConjuntoTDA hijos = mapa.Adyacentes(clienteActual);

        double cota = cotaInf;
        Integer clienteIdAux = null;
        Integer horarioFin = hora;

        while (!hijos.conjuntoVacio()){
            Integer hijoId = hijos.elegir();
            Cliente clienteHijo = clientes.stream().filter(c -> c.getId().equals(hijoId)).findFirst().orElse(null);
            if(!visitados.contains(hijoId)) {
                while(mapaAux.ExisteArista(clienteActual,hijoId)){
                    Integer tiempo = mapaAux.PesoAristaMinutos(clienteActual, hijoId);
                    if (clienteHijo.getDisponibleDesde() * 60 <= hora + tiempo && hora + tiempo <= clienteHijo.getDisponibleHasta() * 60) {
                        double cotaAux = 0, km, totalKmRecubrimiento = 0;
                        MapaTDA mapaPrim = new Mapa();
                        mapaPrim.InicializarMapa();
                        mapaPrim = copiarGrafo(mapa, mapaPrim);

                        for(Integer visitado : visitados){
                            mapaPrim.EliminarVertice(visitado);
                        }

                        Integer primerClienteId = 1;
                        Integer ultimoClienteId = hijoId;

                        double pesoActual = mapaAux.PesoAristaKm(clienteActual, hijoId) + kmVisitados;
                        if(kmVisitados == 0){
                            MapaTDA mapaPrimAux = prim(mapaPrim);
                            km = pesoActual;
                            totalKmRecubrimiento = calcularArbolRecubrimiento(mapaPrimAux);
                            cotaAux = calcularCotaInferior(km, totalKmRecubrimiento, calcularARecubrimiento(primerClienteId, mapaPrimAux),
                                    calcularARecubrimiento(ultimoClienteId, mapaPrimAux));
                        }
                        else if(kmVisitados > pesoActual){
                            MapaTDA mapaPrimAux = prim(mapaPrim);
                            km = pesoActual;
                            totalKmRecubrimiento = calcularArbolRecubrimiento(mapaPrimAux);
                            cotaAux = calcularCotaInferior(km, totalKmRecubrimiento, calcularARecubrimiento(primerClienteId, mapaPrimAux),
                                    calcularARecubrimiento(ultimoClienteId, mapaPrimAux));
                        }

                        if(cotaAux < cota){
                            cota = cotaAux;
                            kmVisitados = totalKmRecubrimiento;
                            horarioFin += tiempo;
                            clienteIdAux = hijoId;
                        }
                    }
                    mapaAux.ElminarArista(clienteActual,hijoId);
                }
            }
            hijos.sacar(hijoId);
        }

        visitados.add(clienteIdAux);

        if(visitados.size() == clientes.size()){
            mostrarRecorrido();
        }
        else{
            planificarRecorrido(clienteIdAux, visitados, cota, horarioFin, kmVisitados);
        }
    }

    private static void mostrarRecorrido(){
        System.out.println("Recorrido final...");
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
        clientes.add(new Cliente(0, 0, 9999999)); // Creo el centro de distribucion
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
                resultado.AgregarArista(aux_vertice, vertice, g.PesoAristaMinutos(aux_vertice, vertice), g.PesoAristaKm(aux_vertice, vertice));
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
                    mejor_distancia = resultado.PesoAristaKm(aux_vertice,resultado.Adyacentes(aux_vertice).elegir());
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
                        resultado.AgregarArista(aux_vertice, vertice, g.PesoAristaMinutos(aux_vertice, vertice), g.PesoAristaKm(aux_vertice, vertice));
                    }else {
                        if(resultado.PesoAristaKm(aux_vertice, resultado.Adyacentes(aux_vertice).elegir()) > g.PesoAristaKm(aux_vertice, vertice)){
                            resultado.ElminarArista(aux_vertice, resultado.Adyacentes(aux_vertice).elegir());
                            resultado.AgregarArista(aux_vertice, vertice, g.PesoAristaMinutos(aux_vertice, vertice), g.PesoAristaKm(aux_vertice, vertice));
                        }
                    }
                }
            }
        }

        return resultado;
    }
}

