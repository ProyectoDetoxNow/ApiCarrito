package cl.detoxnow.CarritoDeCompras.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.detoxnow.CarritoDeCompras.Model.Carrito;
import cl.detoxnow.CarritoDeCompras.Model.DetalleCarrito;
import cl.detoxnow.CarritoDeCompras.Service.CarroService;

@RestController
@RequestMapping("/Api/v1/Carrito")
public class CarroController {

    @Autowired
    private CarroService carroService;

    //  VER TODOS LOS CARRITOS
    @GetMapping
    public List<Carrito> verCarrito() {
        return carroService.getAllItems();
    }

    //  OBTENER CARRITO POR ID
    @GetMapping("/{id}")
    public Carrito getItem(@PathVariable("id") int id) {
        return carroService.getCarritoId(id);
    }

    //  AGREGAR PRODUCTO AL CARRITO
    @PostMapping("/agregar/{idUsuario}/{idProducto}/{cantidad}")
    public Carrito addItem(
            @PathVariable("idUsuario") int idUsuario,
            @PathVariable("idProducto") int idProducto,
            @PathVariable("cantidad") int cantidad) {

        return carroService.agregarProducto(idUsuario, idProducto, cantidad);
    }

    //  ACTUALIZAR CANTIDAD DE UN PRODUCTO DEL CARRITO
    //  CAMBIO: ya no es idPedido, ahora es idProducto
@PutMapping("/{idCarrito}/producto/{idProducto}")
public DetalleCarrito updateProducto(
        @PathVariable("idCarrito") int idCarrito,
        @PathVariable("idProducto") int idProducto,
        @RequestBody Map<String, Integer> body) {

    int nuevaCantidad = body.get("cantidad");
    return carroService.actualizarCantidad(idCarrito, idProducto, nuevaCantidad);
}

    //  ELIMINAR PRODUCTO DEL CARRITO
    //  CAMBIO: ya no es idPedido, ahora es idProducto
    @DeleteMapping("/{idCarrito}/producto/{idProducto}")
    public ResponseEntity<String> deleteItem(
            @PathVariable("idCarrito") int idCarrito,
            @PathVariable("idProducto") int idProducto) {

        carroService.deleteItem(idCarrito, idProducto);
        return ResponseEntity.ok("Producto eliminado del carrito");
    }

    //  OBTENER TOTAL DEL CARRITO
    @GetMapping("/{id}/total")
    public double obtenerTotalDelCarrito(@PathVariable("id") int id) {
        return carroService.calcularTotalCarrito(id);
    }
}
