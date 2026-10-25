// PÁGINA: CARRITO (ruta "/carrito")  <- mi carrito.html de antes
// Acá muestro los productos que agregué, puedo cambiar la cantidad, quitarlos, vaciar todo
// y ver el total. El carrito vive en App.tsx y acá solo lo muestro con props.
import { useState } from "react";
import { Link } from "wouter";
import PageTitle from "../../components/page-title/PageTitle";
import CantidadInput from "../../components/cantidad-input/CantidadInput";
import Photo from "../../components/photo/Photo";
import { productos } from "../../data/productos";
import { calcularTotal, type ItemCarrito } from "../../utils/carrito";
import { formatearPrecio } from "../../utils/formato";
import type { Sesion } from "../../utils/storage";

interface CarritoProps {
  carrito: ItemCarrito[];
  sesion: Sesion | null;
  onCambiarCantidad: (codigo: string, cantidad: number) => void;
  onQuitar: (codigo: string) => void;
  onVaciar: () => void;
}

const Carrito = ({ carrito, sesion, onCambiarCantidad, onQuitar, onVaciar }: CarritoProps) => {
  // RECOMENDACIÓN: acá guardo si ya "compré", para mostrar el mensaje de gracias
  const [compraRealizada, setCompraRealizada] = useState(false);

  const finalizarCompra = () => {
    onVaciar();
    setCompraRealizada(true);
  };

  return (
    <div className="mx-auto max-w-4xl">
      <PageTitle>Tu carrito</PageTitle>

      {compraRealizada && (
        <p role="status" className="mb-4 rounded-2xl border border-emerald-600 bg-emerald-50 p-3 text-sm font-bold text-emerald-800">
          ¡Gracias por tu compra, {sesion?.nombre}! 💖 (es una demostración: no se cobra nada)
        </p>
      )}

      {carrito.length === 0 ? (
        <div className="panel text-center">
          <p className="mb-4 font-bold">Tu carrito está vacío.</p>
          <Link href="/productos" className="boton">
            Ir al catálogo
          </Link>
        </div>
      ) : (
        <>
          {/* una fila por producto; en celular se apila y en pantallas más grandes queda en línea */}
          <ul className="flex flex-col gap-3">
            {carrito.map((item) => {
              const stock = productos.find((p) => p.codigo === item.codigo)?.stock ?? item.cantidad;
              return (
                <li key={item.codigo} className="panel flex flex-col gap-3 sm:flex-row sm:items-center">
                  {/* 🖼️ acá va la foto chiquita del producto */}
                  <Photo src={item.imagen} alt={item.nombre} contener className="h-20 w-20 shrink-0 rounded-xl bg-rosa-100 p-1" />
                  <div className="flex-1">
                    <Link href={`/productos/${item.codigo}`} className="font-extrabold text-rosa-500">
                      {item.nombre}
                    </Link>
                    <p className="text-sm">Precio unitario: {formatearPrecio(item.precio)}</p>
                  </div>
                  <div className="flex items-center gap-2">
                    <label htmlFor={`cantidad-${item.codigo}`} className="text-sm font-bold">
                      Cant.
                    </label>
                    <CantidadInput
                      id={`cantidad-${item.codigo}`}
                      valor={item.cantidad}
                      max={stock}
                      onCambio={(cantidad) => onCambiarCantidad(item.codigo, cantidad)}
                    />
                  </div>
                  <p className="w-24 font-extrabold text-rosa-500 sm:text-right">
                    {formatearPrecio(item.precio * item.cantidad)}
                  </p>
                  <button type="button" onClick={() => onQuitar(item.codigo)} className="boton-suave !px-3 !py-1">
                    Quitar
                  </button>
                </li>
              );
            })}
          </ul>

          <div className="panel mt-4 flex flex-wrap items-center justify-between gap-3">
            <button type="button" onClick={onVaciar} className="boton-suave">
              Vaciar carrito
            </button>
            <p className="text-xl font-extrabold text-rosa-500">Total: {formatearPrecio(calcularTotal(carrito))}</p>
          </div>

          <div className="mt-4 text-right">
            {sesion ? (
              <button type="button" onClick={finalizarCompra} className="boton">
                Finalizar compra
              </button>
            ) : (
              <p className="text-sm font-bold">
                Para finalizar tu compra,{" "}
                <Link href="/login" className="text-rosa-500 underline">
                  inicia sesión
                </Link>
                .
              </p>
            )}
          </div>
        </>
      )}
    </div>
  );
};

export default Carrito;
