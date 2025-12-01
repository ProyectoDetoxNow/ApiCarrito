package cl.detoxnow.CarritoDeCompras.DTO;

import lombok.Data;
import java.sql.Date;

@Data
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String email;
    private Integer telefono;
    private String direccion;
    private String comuna;
    private String region;
    private String password;
}
