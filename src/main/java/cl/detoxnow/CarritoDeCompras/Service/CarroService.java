package cl.detoxnow.CarritoDeCompras.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import cl.detoxnow.CarritoDeCompras.DTO.ProductoDTO;
import cl.detoxnow.CarritoDeCompras.DTO.UsuarioDTO;
import cl.detoxnow.CarritoDeCompras.Model.Carrito;
import cl.detoxnow.CarritoDeCompras.Model.DetalleCarrito;
import cl.detoxnow.CarritoDeCompras.Repository.CarroRepository;
import cl.detoxnow.CarritoDeCompras.Repository.DetalleCarritoRepository;

@Service
public class CarroService {
    @Autowired
    private CarroRepository carroRepository;

    @Autowired
    private DetalleCarritoRepository detalleRepository;



    
    @Autowired
    private RestTemplate rest;

    public Carrito agregarProducto(int idUsuario, int idProducto, int cantidad) {
        // Buscar usuario
        UsuarioDTO usuario = rest.getForObject("http://localhost:8082/api/v1/usuarios/" + idUsuario, UsuarioDTO.class);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // Buscar producto
        ProductoDTO producto = rest.getForObject("http://localhost:8083/Api/v1/inventario/" + idProducto, ProductoDTO.class);
        if (producto == null) {
            throw new RuntimeException("Producto no encontrado");
        }

        // Verificar stock
        if (producto.getCantidad() < cantidad) {
            throw new RuntimeException("Stock insuficiente");
        }

        // Buscar carrito existente o crear uno nuevo
        Carrito carrito = carroRepository.findByIdUsuarioAndEstado(idUsuario, "ACTIVO").orElseGet(() -> {
            Carrito nuevoCarrito = new Carrito();
            nuevoCarrito.setIdUsuario(idUsuario);
            nuevoCarrito.setEstado("ACTIVO");
            nuevoCarrito.setFechaCreacion(java.time.LocalDateTime.now());
            return carroRepository.save(nuevoCarrito);
        });

                Optional<DetalleCarrito> detalleExistente =
                detalleRepository.findByCarritoIdAndIdProducto(carrito.getId(), idProducto);

        if (detalleExistente.isPresent()) {
            DetalleCarrito detalle = detalleExistente.get();
            detalle.setCantidad(detalle.getCantidad() + cantidad);
            detalleRepository.save(detalle);
        }else {
            DetalleCarrito nuevoDetalle = new DetalleCarrito();
            nuevoDetalle.setIdProducto(idProducto);
            nuevoDetalle.setCantidad(cantidad);
            nuevoDetalle.setCarrito(carrito);
            detalleRepository.save(nuevoDetalle);
        }

        // 6️⃣ Descontar stock en API Productos
        descontarStock(idProducto, cantidad);

        return carrito;
    }

    private void descontarStock(int idProducto, int cantidad) {
        String url = "http://localhost:8083/Api/v1/inventario/descontar/" + idProducto + "/" + cantidad;
        rest.put(url, null); // Llamada PUT sin body
    }

    //  OBTENER CARRITO POR ID
    public Carrito getCarritoId(int id) {
        return carroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Carrito no encontrado con ID " + id));
    }

    //  LISTAR TODOS LOS CARRITOS
    public List<Carrito> getAllItems() {
        return carroRepository.findAll();
    }

    //  ELIMINAR PRODUCTO DEL CARRITO
    public void deleteItem(int idCarrito, int idProducto) {

        DetalleCarrito detalle = detalleRepository
                .findByCarritoIdAndIdProducto(idCarrito, idProducto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado en el carrito"));

        detalleRepository.delete(detalle);
    }

    //  CALCULAR TOTAL DEL CARRITO (consultando API productos)
    public double calcularTotalCarrito(int idCarrito) {

        Carrito carrito = getCarritoId(idCarrito);

        double total = 0;

        for (DetalleCarrito det : carrito.getDetalles()) {

            ProductoDTO producto = rest.getForObject(
                    "http://localhost:8083/Api/v1/inventario/" + det.getIdProducto(),
                    ProductoDTO.class
            );

            if (producto == null) {
                throw new RuntimeException("Producto no encontrado al calcular total");
            }

            total += producto.getPrecio() * det.getCantidad();
        }

        return total;
    }

    //  CERRAR CARRITO (cuando se paga)
    public void cerrarCarrito(int idCarrito) {

        Carrito carrito = getCarritoId(idCarrito);
        carrito.setEstado("CERRADO");

        carroRepository.save(carrito);
    }


public DetalleCarrito actualizarCantidad(int idCarrito, int idProducto, int nuevaCantidad) {

    DetalleCarrito detalle = detalleRepository
            .findByCarritoIdAndIdProducto(idCarrito, idProducto)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Producto no encontrado en el carrito"));

    // Si la cantidad es 0 o menor, se elimina
    if (nuevaCantidad <= 0) {
        detalleRepository.delete(detalle);
        return null;
    }

    // Validar stock en microservicio productos
    ProductoDTO producto = rest.getForObject(
            "http://localhost:8083/Api/v1/inventario/" + idProducto,
            ProductoDTO.class
    );

    if (producto == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
    }

    if (producto.getCantidad() < nuevaCantidad) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente");
    }

    detalle.setCantidad(nuevaCantidad);
    return detalleRepository.save(detalle);
}}