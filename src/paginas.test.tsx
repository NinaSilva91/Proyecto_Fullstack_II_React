// paginas.test.tsx: prueba rápida de que cada página se dibuja sin errores y muestra su contenido.
import { renderToString } from "react-dom/server";
import { Router } from "wouter";
import { describe, expect, it, vi } from "vitest";

vi.stubGlobal("window", { location: { origin: "http://localhost:5173" } });

const casos: [string, string, string][] = [
  ["/", "", "Nuestra historia"],
  ["/historia", "", "Nace la comunidad"],
  ["/maids", "", "Ver su merch"],
  ["/eventos", "", "Tokyo Maid Expo"],
  ["/productos", "maid=shikku", "Sticker Shikku"],
  ["/login", "", "Iniciar sesión"],
  ["/register", "", "Crear cuenta nueva"],
  ["/contacto", "", "Síguenos"],
  ["/carrito", "", "carrito"],
];

describe("las páginas se dibujan sin errores", () => {
  it.each(casos)("%s", async (ruta, busqueda, texto) => {
    const { default: App } = await import("./App");
    const html = renderToString(
      <Router ssrPath={ruta} ssrSearch={busqueda}>
        <App />
      </Router>,
    );
    expect(html).toContain(texto);
  });
  it("con maid=shikku no muestra productos de otra maid", async () => {
    const { default: App } = await import("./App");
    const html = renderToString(
      <Router ssrPath="/productos" ssrSearch="maid=shikku"><App /></Router>,
    );
    expect(html).not.toContain("Sticker Fumi");
  });
});
