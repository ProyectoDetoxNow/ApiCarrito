package cl.detoxnow.CarritoDeCompras.DTO;

import lombok.Data;

@Data
public class ProductoDTO {
    private int id;
    private String nombreProducto;
    private String descripcion;
    private String categoria;
    private double precio;
    private int cantidad;
    private String imagen;
}

