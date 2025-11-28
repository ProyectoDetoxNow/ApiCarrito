package cl.detoxnow.CarritoDeCompras.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.detoxnow.CarritoDeCompras.Model.DetalleCarrito;

@Repository
public interface DetalleCarritoRepository extends JpaRepository<DetalleCarrito, Integer> {

    Optional<DetalleCarrito> findByCarritoIdAndIdProducto(int carritoId, int idProducto);
}
