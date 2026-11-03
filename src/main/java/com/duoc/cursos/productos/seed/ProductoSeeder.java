package com.duoc.cursos.productos.seed;

import com.duoc.cursos.productos.model.Producto;
import com.duoc.cursos.productos.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seed automático: carga el catálogo inicial de la tienda (los 29 productos que
 * antes vivían solo en src/data/productos.ts del frontend) si la tabla está vacía.
 * Corre en cada arranque, pero solo inserta cuando no hay productos en la base.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductoSeeder implements ApplicationRunner {

    private final ProductoRepository productoRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (productoRepository.count() > 0) {
            log.info("Seed de productos omitido: la tabla 'productos' ya tiene datos");
            return;
        }

        List<Producto> productos = new java.util.ArrayList<>();

        // 4 productos por maid (sticker, sticker XL, photocard y chapita), igual que en el frontend
        productos.addAll(deMaid("SHI", "shikku", "Shikku",
                "/imgs/shikku.webp", "/imgs/shikkuXL.webp", "/imgs/photocardShikku.webp", "/imgs/chapaShikku.webp"));
        productos.addAll(deMaid("FUM", "fumi", "Fumi",
                "/imgs/fumi.webp", "/imgs/fumiXL.webp", "/imgs/photocardFumi.webp", "/imgs/chapaFumi.webp"));
        productos.addAll(deMaid("MIC", "michiru", "Michiru",
                "/imgs/michiru.webp", "/imgs/michiruXL.webp", "/imgs/photocardMichiru.webp", "/imgs/chapaMichiru.webp"));
        productos.addAll(deMaid("BEK", "bekyuu", "Bekyuu",
                "/imgs/bekyuu.webp", "/imgs/bekyuuXL.webp", "/imgs/photocardBekyuu.webp", "/imgs/chapaBekyuu.webp"));
        productos.addAll(deMaid("RIT", "ritsu", "Ritsu",
                "/imgs/ritsu.webp", "/imgs/ritsuXL.webp", "/imgs/photocardRitsu.webp", "/imgs/chapaRitsu.webp"));
        productos.addAll(deMaid("AIM", "aimi", "Aimi",
                "/imgs/aimi.webp", "/imgs/aimiXL.webp", "/imgs/photocardAimi.webp", "/imgs/chapaAimi.webp"));

        // merch general de Secret Sweet Maid (no es de una maid en particular)
        productos.add(prod("SSM001", "Llavero Secret Sweet Maid",
                "Llavero oficial de Secret Sweet Maid.", 3000, 10, "grupal", "llavero",
                "/imgs/llavero3D.webp", true));
        productos.add(prod("SSM002", "Gashapon",
                "Gashapon de Secret Sweet Maid.", 500, 10, "grupal", "gashapon",
                "/imgs/gashapon.webp", false));
        productos.add(prod("SSM003", "Bolsa sorpresa",
                "Bolsa sorpresa con productos de Secret Sweet Maid.", 2000, 10, "grupal", "bolsa",
                "/imgs/sobresSorpresa.webp", true));
        productos.add(prod("SSM004", "Cartas Pokemaid",
                "Cartas coleccionables Pokemaid.", 1000, 10, "grupal", "cartas",
                "/imgs/cartasPokemaid.webp", true));
        productos.add(prod("SSM005", "Separador de libro",
                "Separador de libro de Secret Sweet Maid.", 1000, 10, "grupal", "separador",
                "/imgs/marcaPaginas.webp", false));

        productoRepository.saveAll(productos);
        log.info("Seed de productos completado: {} productos insertados", productos.size());
    }

    /** Los 4 productos de cada maid, con los mismos precios que tenía el catálogo local. */
    private static List<Producto> deMaid(String prefijo, String categoria, String nombre,
                                         String sticker, String stickerXL, String photocard, String chapita) {
        return List.of(
                prod(prefijo + "001", "Sticker " + nombre,
                        "Sticker de " + nombre + " de Secret Sweet Maid.", 300, 10, categoria, "sticker", sticker, false),
                prod(prefijo + "002", "Sticker XL " + nombre,
                        "Sticker XL de " + nombre + ".", 1000, 10, categoria, "sticker-xl", stickerXL, false),
                prod(prefijo + "003", "Photocard " + nombre,
                        "Photocard de " + nombre + ".", 1000, 10, categoria, "photocard", photocard, false),
                prod(prefijo + "004", "Chapita " + nombre,
                        "Chapita de " + nombre + ".", 1000, 10, categoria, "chapita", chapita, false)
        );
    }

    private static Producto prod(String codigo, String nombre, String descripcion, int precio, int stock,
                                 String categoria, String tipo, String imagen, boolean destacado) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setStockCritico(null);
        p.setCategoria(categoria);
        p.setTipo(tipo);
        p.setImagen(imagen);
        p.setDestacado(destacado);
        return p;
    }
}
