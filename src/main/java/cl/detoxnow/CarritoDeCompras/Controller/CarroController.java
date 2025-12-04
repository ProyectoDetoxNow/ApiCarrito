package cl.detoxnow.CarritoDeCompras.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.detoxnow.CarritoDeCompras.Model.Carrito;
import cl.detoxnow.CarritoDeCompras.Model.DetalleCarrito;
import cl.detoxnow.CarritoDeCompras.Service.CarroService;

@RestController
@RequestMapping("/Api/v1/Carrito")
public class CarroController {

    @Autowired
    private CarroService carroService;

    // LISTAR CARRITOS
    @GetMapping
    public List<Carrito> verCarrito() {
        return carroService.getAllItems();
    }

    // OBTENER CARRITO POR ID
    @GetMapping("/{id}")
    public Carrito getItem(@PathVariable("id") int id) {
        return carroService.getCarritoId(id);
    }

    // AGREGAR PRODUCTO (acepta "null")
    @PostMapping("/agregar/{idCarrito}/{idProducto}/{cantidad}")
    public Carrito addItem(
            @PathVariable("idCarrito") String rawId,
            @PathVariable("idProducto") int idProducto,
            @PathVariable("cantidad") int cantidad) {

        Integer idCarrito = null;

        if (rawId != null && !rawId.equalsIgnoreCase("null")) {
            idCarrito = Integer.parseInt(rawId);
        }

        return carroService.agregarProducto(idCarrito, idProducto, cantidad);
    }

    // ACTUALIZAR CANTIDAD
    @PutMapping("/{idCarrito}/producto/{idProducto}")
    public DetalleCarrito updateProducto(
            @PathVariable("idCarrito") int idCarrito,
            @PathVariable("idProducto") int idProducto,
            @RequestBody Map<String, Integer> body) {

        int nuevaCantidad = body.get("cantidad");
        return carroService.actualizarCantidad(idCarrito, idProducto, nuevaCantidad);
    }

    // ELIMINAR PRODUCTO
    @DeleteMapping("/{idCarrito}/producto/{idProducto}")
    public ResponseEntity<String> deleteItem(
            @PathVariable("idCarrito") int idCarrito,
            @PathVariable("idProducto") int idProducto) {

        carroService.deleteItem(idCarrito, idProducto);
        return ResponseEntity.ok("Producto eliminado del carrito");
    }

    // TOTAL
    @GetMapping("/{id}/total")
    public double obtenerTotalDelCarrito(@PathVariable("id") int id) {
        return carroService.calcularTotalCarrito(id);
    }

    // CERRAR CARRITO
    @PutMapping("/cerrar/{idCarrito}")
    public ResponseEntity<String> cerrarCarrito(@PathVariable("idCarrito") int idCarrito) {

        carroService.cerrarCarrito(idCarrito);
        return ResponseEntity.ok("Carrito cerrado correctamente");
    }
}
