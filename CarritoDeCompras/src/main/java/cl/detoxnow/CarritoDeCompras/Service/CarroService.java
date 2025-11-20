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
import cl.detoxnow.CarritoDeCompras.Model.Pedido;
import cl.detoxnow.CarritoDeCompras.Repository.CarroRepository;

@Service
public class CarroService {
    @Autowired
    private CarroRepository carroRepository;

    
    @Autowired
    private RestTemplate rest;

    public Carrito agregarProducto(int idUsuario, int idProducto, int cantidad) {
        // Buscar usuario
        UsuarioDTO usuario = rest.getForObject("http://localhost:8082/api/v1/usuarios/" + idUsuario, UsuarioDTO.class);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // Buscar producto
        ProductoDTO producto = rest.getForObject("http://localhost:8081/Api/v1/inventario/" + idProducto, ProductoDTO.class);
        if (producto == null) {
            throw new RuntimeException("Producto no encontrado");
        }

        // Verificar stock
        if (producto.getCantidad() < cantidad) {
            throw new RuntimeException("Stock insuficiente");
        }

        // Buscar carrito existente o crear uno nuevo
        Carrito carrito = carroRepository.findByIdUsuario(idUsuario).orElse(null);
        if (carrito == null) {
            carrito = new Carrito();
            carrito.setIdUsuario(idUsuario);
        }

        // Crear pedido
        Pedido pedido = new Pedido();
        pedido.setIdProducto(idProducto);
        pedido.setIdUsuario(idUsuario);
        pedido.setNombreProducto(producto.getNombreProducto());
        pedido.setPrecioProducto(producto.getPrecio());
        pedido.setCantidad(cantidad);
        pedido.setCarrito(carrito);

        // Agregar al carrito
        carrito.getItems().add(pedido);

        // Guardar carrito
        

        descontarStock(idProducto, cantidad); // <-- Aquí llamas al nuevo método

        return carroRepository.save(carrito);
    }
    
    private void descontarStock(int idProducto, int cantidad) {
        String url = "http://localhost:8081/Api/v1/inventario/descontar/" + idProducto + "/" + cantidad;
        rest.put(url, null); // Llamada PUT sin body
    }

    public Optional<Carrito> getItem(int id) {
        return carroRepository.findById(id);
    }
    
    public Pedido updateItem(int idCarrito, int idPedido, Pedido itemActualizado) {
    Carrito carrito = carroRepository.findById(idCarrito)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrito no encontrado con ID: " + idCarrito));

    Pedido pedidoExistente = carrito.getItems().stream()
        .filter(p -> p.getId() == idPedido)
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado con ID: " + idPedido));

    pedidoExistente.setCantidad(itemActualizado.getCantidad());
    pedidoExistente.setNombreProducto(itemActualizado.getNombreProducto());
    pedidoExistente.setPrecioProducto(itemActualizado.getPrecioProducto());
    pedidoExistente.setCarrito(carrito); // ← Asegurarse de mantener la relación

    carroRepository.save(carrito);

    return pedidoExistente;
    }

    public void deleteItem(int idCarrito, int idPedido) {
        Carrito carrito = carroRepository.findById(idCarrito)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrito no encontrado con ID: " + idCarrito));

        boolean removed = carrito.getItems().removeIf(p -> p.getId() == idPedido);
    
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado con ID: " + idPedido);
        }

        carroRepository.save(carrito);
    }

    // Otros métodos...
        public List<Carrito> getAllItems(){
        return carroRepository.findAll();
    }

    public Carrito getCarritoId(int id) {
    return carroRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrito no encontrado con ID " + id));
    }

    public double calcularTotalCarrito(int idCarrito) {
    Carrito carrito = carroRepository.findById(idCarrito)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrito no encontrado con ID: " + idCarrito));

    return carrito.getItems().stream()
        .mapToDouble(p -> p.getPrecioProducto() * p.getCantidad())
        .sum();
}

    
}

    