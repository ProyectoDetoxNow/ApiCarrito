package cl.detoxnow.CarritoDeCompras.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${api.inventario.url}")
    private String inventarioUrl;

    @Value("${api.usuarios.url}")
    private String usuariosUrl;


    // ------------------------------------------------------------------
    // AGREGAR PRODUCTO AL CARRITO
    // ------------------------------------------------------------------
    public Carrito agregarProducto(int idUsuario, int idProducto, int cantidad) {

        // Buscar usuario
        UsuarioDTO usuario = rest.getForObject(
                usuariosUrl + "/" + idUsuario,
                UsuarioDTO.class
        );

        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }

        // Buscar producto
        ProductoDTO producto = rest.getForObject(
                inventarioUrl + "/" + idProducto,
                ProductoDTO.class
        );

        if (producto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }

        // Verificar stock
        if (producto.getCantidad() < cantidad) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente");
        }

        // Buscar carrito existente
        Carrito carrito = carroRepository
                .findByIdUsuarioAndEstado(idUsuario, "ACTIVO")
                .orElseGet(() -> {
                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setIdUsuario(idUsuario);
                    nuevoCarrito.setEstado("ACTIVO");
                    nuevoCarrito.setFechaCreacion(java.time.LocalDateTime.now());
                    return carroRepository.save(nuevoCarrito);
                });

        // Revisar si el producto ya existe
        Optional<DetalleCarrito> detalleExistente =
                detalleRepository.findByCarritoIdAndIdProducto(carrito.getId(), idProducto);

        if (detalleExistente.isPresent()) {
            DetalleCarrito detalle = detalleExistente.get();
            detalle.setCantidad(detalle.getCantidad() + cantidad);
            detalleRepository.save(detalle);
        } else {
            DetalleCarrito nuevoDetalle = new DetalleCarrito();
            nuevoDetalle.setIdProducto(idProducto);
            nuevoDetalle.setCantidad(cantidad);
            nuevoDetalle.setCarrito(carrito);
            detalleRepository.save(nuevoDetalle);
        }

        // Descontar stock en Inventario
        descontarStock(idProducto, cantidad);

        return carrito;
    }


    // ------------------------------------------------------------------
    // DESCONTAR STOCK EN API INVENTARIO
    // ------------------------------------------------------------------
    private void descontarStock(int idProducto, int cantidad) {
        String url = inventarioUrl + "/descontar/" + idProducto + "/" + cantidad;
        rest.put(url, null);
    }


    // ------------------------------------------------------------------
    // OBTENER CARRITO POR ID
    // ------------------------------------------------------------------
    public Carrito getCarritoId(int id) {
        return carroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Carrito no encontrado con ID " + id));
    }


    // ------------------------------------------------------------------
    // LISTAR TODOS LOS CARRITOS
    // ------------------------------------------------------------------
    public List<Carrito> getAllItems() {
        return carroRepository.findAll();
    }


    // ------------------------------------------------------------------
    // ELIMINAR PRODUCTO DEL CARRITO
    // ------------------------------------------------------------------
    public void deleteItem(int idCarrito, int idProducto) {

        DetalleCarrito detalle = detalleRepository
                .findByCarritoIdAndIdProducto(idCarrito, idProducto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado en el carrito"));

        detalleRepository.delete(detalle);
    }


    // ------------------------------------------------------------------
    // CALCULAR TOTAL DEL CARRITO
    // ------------------------------------------------------------------
    public double calcularTotalCarrito(int idCarrito) {

        Carrito carrito = getCarritoId(idCarrito);
        double total = 0;

        for (DetalleCarrito det : carrito.getDetalles()) {

            ProductoDTO producto = rest.getForObject(
                    inventarioUrl + "/" + det.getIdProducto(),
                    ProductoDTO.class
            );

            if (producto == null) {
                throw new RuntimeException("Producto no encontrado");
            }

            total += producto.getPrecio() * det.getCantidad();
        }

        return total;
    }


    // ------------------------------------------------------------------
    // CERRAR CARRITO (PAGAR)
    // ------------------------------------------------------------------
    public void cerrarCarrito(int idCarrito) {
        Carrito carrito = getCarritoId(idCarrito);
        carrito.setEstado("CERRADO");
        carroRepository.save(carrito);
    }


    // ------------------------------------------------------------------
    // ACTUALIZAR CANTIDAD
    // ------------------------------------------------------------------
    public DetalleCarrito actualizarCantidad(int idCarrito, int idProducto, int nuevaCantidad) {

        DetalleCarrito detalle = detalleRepository
                .findByCarritoIdAndIdProducto(idCarrito, idProducto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado en el carrito"));

        if (nuevaCantidad <= 0) {
            detalleRepository.delete(detalle);
            return null;
        }

        ProductoDTO producto = rest.getForObject(
                inventarioUrl + "/" + idProducto,
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
    }

}
