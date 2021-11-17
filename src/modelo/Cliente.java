package modelo;

import java.util.Date;

public class Cliente {
    private Integer id;
    private Integer disponibleDesde;
    private Integer disponibleHasta;


    public Cliente(Integer id, Integer disponibleDesde, Integer disponibleHasta){
        this.id = id;
        this.disponibleDesde = disponibleDesde;
        this.disponibleHasta = disponibleHasta;
    }

    public Integer getId(){
        return this.id;
    }

    public Integer getDisponibleHasta(){
        return this.disponibleHasta;
    }

    public Integer getDisponibleDesde(){
        return this.disponibleDesde;
    }

    public Integer getMinutosDisponibleHasta(){return this.disponibleHasta * 60;}

    public Integer getMinutosDisponibleDesde(){return this.disponibleDesde * 60;}
}
