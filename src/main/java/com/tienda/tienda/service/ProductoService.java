package com.tienda.tienda.service;

import com.tienda.tienda.domain.Producto;
import com.tienda.tienda.repository.ProductoRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoService {

    // El repositorio es final para asegurar la inmutabilidad
    private final ProductoRepository ProductoRepository;    
    private final FirebaseStorageService firebaseStorageService;

    @Transactional(readOnly = true)
    public List<Producto> getProductos(boolean activo) {
        if (activo) { //Sólo activos...            
            return ProductoRepository.findByActivoTrue();
        }
        return ProductoRepository.findAll();
    }
    
     @Transactional(readOnly = true)
    public Optional<Producto> getProducto(Integer idProducto) {
        return ProductoRepository.findById(idProducto);
    }

    @Transactional
    public void save(Producto Producto, MultipartFile imagenFile) {
        Producto = ProductoRepository.save(Producto);
        if (imagenFile != null && !imagenFile.isEmpty()) { //Si no está vacío... pasaron una imagen...            
            try {
                String rutaImagen = firebaseStorageService.uploadImage(
                        imagenFile, "Producto",
                        Producto.getIdProducto());
                Producto.setRutaImagen(rutaImagen);
                ProductoRepository.save(Producto);
            } catch (IOException e) {
                log.warn("No fue posible almacenar la imagen de la Producto {}",Producto.getIdProducto(),e);
            }
        }
    }

    @Transactional
    public void delete(Integer idProducto) {
        // Verifica si la categoría existe antes de intentar eliminarlo
        if (!ProductoRepository.existsById(idProducto)) {
            // Lanza una excepción para indicar que el usuario no fue encontrado
            throw new IllegalArgumentException("La categoría con ID " + idProducto + " no existe.");
        }
        try {
            ProductoRepository.deleteById(idProducto);
            ProductoRepository.flush();
        } catch (DataIntegrityViolationException e) {
            // Lanza una nueva excepción para encapsular el problema de integridad de datos
            throw new IllegalStateException("No se puede eliminar la Producto. Tiene datos asociados.", e);
        }
    }
}