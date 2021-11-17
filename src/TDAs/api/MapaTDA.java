package TDAs.api;

import TDAs.impl.Mapa;
import apis.ConjuntoTDA;

public interface MapaTDA {
    void InicializarMapa();
    void AgregarVertice(int v);
    void AgregarArista(int v1, int v2, int minutos, double km);
    void EliminarVertice(int v);
    void ElminarArista(int v1, int v2);
    double PesoAristaKm(int v1, int v2);
    int PesoAristaMinutos(int v1, int v2);
    boolean ExisteArista (int v1, int v2);
    ConjuntoTDA Adyacentes(int v);
    ConjuntoTDA Vertices();

}
