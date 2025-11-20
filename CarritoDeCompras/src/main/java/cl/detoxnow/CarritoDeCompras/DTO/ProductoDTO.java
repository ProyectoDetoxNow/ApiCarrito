package cl.detoxnow.CarritoDeCompras.DTO;

import lombok.Data;

@Data
public class ProductoDTO {
    private int id;
    private String nombreProducto;
    private double precio;
    private int cantidad;
}
