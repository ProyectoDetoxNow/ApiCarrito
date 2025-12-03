package cl.detoxnow.CarritoDeCompras.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.detoxnow.CarritoDeCompras.Model.Carrito;

@Repository
public interface CarroRepository extends JpaRepository<Carrito, Integer> {
}
