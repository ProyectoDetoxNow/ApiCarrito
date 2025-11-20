package cl.detoxnow.CarritoDeCompras.DTO;

import lombok.Data;
import java.sql.Date;

@Data
public class UsuarioDTO {
    private Long id;
    private String rut;
    private String nombre;
    private String apellido;
    private String email;
    private Integer telefono;
    private String direccion;
    private Date fechaNacimiento;
    private String rolUsuario;
}
