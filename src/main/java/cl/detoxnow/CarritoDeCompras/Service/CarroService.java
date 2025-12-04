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

    // ---------------------------------------------------------
    // AGREGAR PRODUCTO AL CARRITO
    // ---------------------------------------------------------
    public Carrito agregarProducto(Integer idCarrito, int idProducto, int cantidad) {

        ProductoDTO producto = rest.getForObject(inventarioUrl + "/" + idProducto, ProductoDTO.class);

        if (producto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }

        if (producto.getCantidad() < cantidad) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente");
        }

        Carrito carrito;

        if (idCarrito != null && idCarrito > 0) {
            carrito = carroRepository.findById(idCarrito)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrito no encontrado"));
        } else {
            carrito = new Carrito();
            carrito.setEstado("ACTIVO");
            carrito.setFechaCreacion(java.time.LocalDateTime.now());
            carrito = carroRepository.save(carrito);
        }

        Optional<DetalleCarrito> existente =
                detalleRepository.findByCarritoIdAndIdProducto(carrito.getId(), idProducto);

        if (existente.isPresent()) {
            DetalleCarrito det = existente.get();
            det.setCantidad(det.getCantidad() + cantidad);
            detalleRepository.save(det);
        } else {
            DetalleCarrito det = new DetalleCarrito();
            det.setIdProducto(idProducto);
            det.setCantidad(cantidad);
            det.setCarrito(carrito);
            detalleRepository.save(det);
        }

        descontarStock(idProducto, cantidad);

        // 🔥 Muy importante → devolver carrito completo con productos
        return getCarritoId(carrito.getId());
    }

    // ---------------------------------------------------------
    // DESCONTAR STOCK
    // ---------------------------------------------------------
    private void descontarStock(int idProducto, int cantidad) {
        String url = inventarioUrl + "/descontar/" + idProducto + "/" + cantidad;
        rest.put(url, null);
    }

    // ---------------------------------------------------------
    // OBTENER CARRITO COMPLETO + PRODUCTOS + TOTAL
    // ---------------------------------------------------------
    public Carrito getCarritoId(int id) {

        Carrito carrito = carroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Carrito no encontrado con ID " + id));

        double total = 0;

        for (DetalleCarrito det : carrito.getDetalles()) {

            ProductoDTO producto = rest.getForObject(
                    inventarioUrl + "/" + det.getIdProducto(),
                    ProductoDTO.class
            );

            if (producto != null) {
                det.setProducto(producto);
                total += producto.getPrecio() * det.getCantidad();
            }
        }

        carrito.setTotal(total);
        return carrito;
    }

    // ---------------------------------------------------------
    // LISTAR
    // ---------------------------------------------------------
    public List<Carrito> getAllItems() {
        return carroRepository.findAll();
    }

    // ---------------------------------------------------------
    // ELIMINAR PRODUCTO DEL CARRITO
    // ---------------------------------------------------------
    public void deleteItem(int idCarrito, int idProducto) {

        DetalleCarrito detalle = detalleRepository
                .findByCarritoIdAndIdProducto(idCarrito, idProducto)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado en carrito"));

        detalleRepository.delete(detalle);
    }

    // ---------------------------------------------------------
    // CALCULAR TOTAL
    // ---------------------------------------------------------
    public double calcularTotalCarrito(int idCarrito) {
        return getCarritoId(idCarrito).getTotal();
    }

    // ---------------------------------------------------------
    // CERRAR CARRITO
    // ---------------------------------------------------------
    public void cerrarCarrito(int idCarrito) {
        Carrito carrito = getCarritoId(idCarrito);
        carrito.setEstado("CERRADO");
        carroRepository.save(carrito);
    }

    // ---------------------------------------------------------
    // ACTUALIZAR CANTIDAD
    // ---------------------------------------------------------
    public DetalleCarrito actualizarCantidad(int idCarrito, int idProducto, int nuevaCantidad) {

        DetalleCarrito detalle = detalleRepository
                .findByCarritoIdAndIdProducto(idCarrito, idProducto)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado en carrito"));

        if (nuevaCantidad <= 0) {
            detalleRepository.delete(detalle);
            return null;
        }

        ProductoDTO producto = rest.getForObject(inventarioUrl + "/" + idProducto, ProductoDTO.class);

        if (producto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }

        if (producto.getCantidad() < nuevaCantidad) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente");
        }

        detalle.setCantidad(nuevaCantidad);
        detalleRepository.save(detalle);

        return detalle;
    }
}
