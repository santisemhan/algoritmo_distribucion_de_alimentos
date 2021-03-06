package TDAs.api;

import java.util.List;

import TDAs.impl.Mapa;
import TDAs.impl.Mapa.NodoArista;
import apis.ConjuntoTDA;
import modelo.AristaDTO;

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
    List<NodoArista> getAristas(int v);
    double getAristaMenorPesoKm(int v, int v1);
    List<AristaDTO> getAristasSort();
    double getAllkmArista();
    NodoArista getAristaObjMenorPesoKm(int v, int v1);
}
