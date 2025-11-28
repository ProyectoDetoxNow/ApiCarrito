package cl.detoxnow.CarritoDeCompras.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.detoxnow.CarritoDeCompras.Model.Carrito;

@Repository
public interface CarroRepository extends JpaRepository<Carrito, Integer> {
    Optional<Carrito> findByIdUsuario(int idUsuario);
    Optional<Carrito> findByIdUsuarioAndEstado(int idUsuario, String estado);

    

}
