package cl.detoxnow.CarritoDeCompras.Controller;

import java.util.List;

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
import cl.detoxnow.CarritoDeCompras.Model.Pedido;
import cl.detoxnow.CarritoDeCompras.Service.CarroService;

@RestController
@RequestMapping("/Api/v1/Carrito")
public class CarroController {

    @Autowired
    private CarroService carroService;

    @GetMapping
    public List<Carrito> verCarrito() {
        return carroService.getAllItems();
    }

    @GetMapping("/{id}")
    public Carrito getItem(@PathVariable("id") int id) {
        return carroService.getCarritoId(id);
    }

    @PostMapping("/agregar/{idUsuario}/{idProducto}/{cantidad}")
    public Carrito addItem(@PathVariable("idUsuario") int idUsuario, @PathVariable("idProducto") int idProducto, @PathVariable("cantidad") int cantidad) {
        return carroService.agregarProducto(idUsuario, idProducto, cantidad);
    }

    @PutMapping("/{idCarrito}/pedido/{idPedido}")
    public Pedido updatePedido(@PathVariable("idCarrito") int idCarrito, @PathVariable("idPedido") int idPedido, @RequestBody Pedido item) {
        return carroService.updateItem(idCarrito, idPedido, item);
    }

    @DeleteMapping("/{idCarrito}/pedido/{idPedido}")
        public ResponseEntity<String> deleteItem(@PathVariable("idCarrito") int idCarrito, @PathVariable("idPedido") int idPedido) {
            carroService.deleteItem(idCarrito, idPedido);
            return ResponseEntity.ok("Pedido eliminado");
        }

        @GetMapping("/{id}/total")
            public double obtenerTotalDelCarrito(@PathVariable("id") int id) {
             return carroService.calcularTotalCarrito(id);
        }

}
