const express = require("express");
const pool = require("./db");
const rutasEstudiantes = require("./rutas/estudiantes");
const rutasSesiones = require("./rutas/sesiones");
const rutasLecturas = require("./rutas/lecturas");
const rutasDocente = require("./rutas/docente");
const rutasPreguntas = require("./rutas/preguntas");
const rutasIntentos = require("./rutas/intentos");
const app = express();
const puerto = Number(process.env.PORT || 3000);
app.use(express.json({ limit: "64kb" }));
app.use("/api/estudiantes", rutasEstudiantes);
app.use("/api/sesiones", rutasSesiones);
app.use("/api/lecturas", rutasLecturas);
app.use("/api/docente", rutasDocente);
app.use("/api/preguntas", rutasPreguntas);
app.use("/api/intentos", rutasIntentos);

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

app.use((req, res) => {
  res
    .status(404)
    .json({ estado: "error", mensaje: "No se encontró esta ruta." });
});

app.use((error, req, res, next) => {
  if (res.headersSent) return next(error);
  let estado = error.estadoHTTP || 500;
  let mensaje = error.estadoHTTP
    ? error.message
    : "No se pudo completar la operación.";
  if (error.type === "entity.parse.failed") {
    estado = 400;
    mensaje = "El cuerpo debe contener JSON válido.";
  } else if (error.type === "entity.too.large") {
    estado = 413;
    mensaje = "Los datos enviados superan el tamaño permitido.";
  } else if (error.code === "ER_DUP_ENTRY") {
    estado = 409;
    mensaje = "Ya existe un registro con esos datos únicos.";
  } else if (
    ["ER_LOCK_DEADLOCK", "ER_LOCK_WAIT_TIMEOUT"].includes(error.code)
  ) {
    estado = 503;
    mensaje = "La operación está ocupada. Inténtalo de nuevo.";
  }
  if (estado >= 500) console.error("Error de API:", error.code || error.name);
  res.status(estado).json({ estado: "error", mensaje, ...(error.datos || {}) });
});

const servidor = app.listen(puerto, "0.0.0.0", () => {
  console.log(
    `Servidor disponible en http://localhost:${servidor.address().port}/health`,
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
