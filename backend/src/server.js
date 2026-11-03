const express = require("express");
const pool = require("./db");
const rutasEstudiantes = require("./rutas/estudiantes");
const rutasSesiones = require("./rutas/sesiones");
const rutasLecturas = require("./rutas/lecturas");
const rutasDocente = require("./rutas/docente");
const app = express();
const puerto = Number(process.env.PORT || 3000);
app.use(express.json({ limit: "64kb" }));
app.use("/api/estudiantes", rutasEstudiantes);
app.use("/api/sesiones", rutasSesiones);
app.use("/api/lecturas", rutasLecturas);
app.use("/api/docente", rutasDocente);

app.get("/health", async (req, res) => {
  try {
    await pool.query("SELECT 1");

    res.json({
      estado: "ok",
      mensaje: "API de Caña Dulce en funcionamiento",
      baseDeDatos: "conectada",
    });
  } catch (error) {
    console.error("Error al consultar MySQL:", error.code || error.name);

    res.status(503).json({
      estado: "error",
      mensaje: "No se pudo conectar con la base de datos",
    });
  }
});

const servidor = app.listen(puerto, "0.0.0.0", () => {
  console.log(
    `Servidor disponible en http://localhost:${servidor.address().port}/health`
  );
});

let cierreIniciado = false;

function cerrarServidor() {
  if (!cierreIniciado) {
    cierreIniciado = true;

  servidor.close(async () => {
      try {
        await pool.end();
        process.exitCode = 0;
      } catch (error) {
        console.error("Error al cerrar MySQL:", error.code || error.name);
        process.exitCode = 1;
      }
    });
  }
}

process.once("SIGTERM", cerrarServidor);
process.once("SIGINT", cerrarServidor);
