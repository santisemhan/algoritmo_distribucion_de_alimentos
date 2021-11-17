package modelo;

public class Camino {
	private Integer idClienteOrigen;
	private Integer idClienteDestino;
	private double km;
	private Integer tiempo;
	
	
	
	public Camino(Integer idClienteOrigen, Integer idClienteDestino, double km, Integer tiempo) {
		this.idClienteOrigen = idClienteOrigen;
		this.idClienteDestino = idClienteDestino;
		this.km = km;
		this.tiempo = tiempo;
	}
	
	public Integer getIdClienteOrigen() {
		return idClienteOrigen;
	}
	public void setIdClienteOrigen(Integer idClienteOrigen) {
		this.idClienteOrigen = idClienteOrigen;
	}
	public Integer getIdClienteDestino() {
		return idClienteDestino;
	}
	public void setIdClienteDestino(Integer idClienteDestino) {
		this.idClienteDestino = idClienteDestino;
	}
	public double getKm() {
		return km;
	}
	public void setKm(double km) {
		this.km = km;
	}
	public Integer getTiempo() {
		return tiempo;
	}
	public void setTiempo(Integer tiempo) {
		this.tiempo = tiempo;
	}
	
	
	
	
}
