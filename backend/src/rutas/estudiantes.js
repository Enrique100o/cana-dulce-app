const express = require("express");
const pool = require("../db");
const { crearHashCodigo } = require("../seguridad");
const { validarRegistroEstudiante } = require("../validaciones");
const { sinCuerpo, filtros, identificador, fallar } = require("../http");
const { obtenerProgreso } = require("../servicios/progreso");
const { permitirPropietario } = require("../servicios/intentos");
const { autenticar, permitirRol } = require("../middleware/autenticacion");

const router = express.Router();

router.post("/", async (req, res) => {
  const validacion = validarRegistroEstudiante(req.body);

  if (validacion.error) {
    return res.status(400).json({
      estado: "error",
      mensaje: validacion.error,
    });
  }

  const { alias, aliasNormalizado, grado, codigoAcceso } = validacion.datos;

  try {
    const codigoAccesoHash = await crearHashCodigo(codigoAcceso);

    const [resultado] = await pool.execute(
      `INSERT INTO ESTUDIANTE (
                alias, aliasNormalizado, grado, codigoAccesoHash
            ) VALUES (?, ?, ?, ?)`,
      [alias, aliasNormalizado, grado, codigoAccesoHash],
    );

    res.status(201).json({
      idEstudiante: resultado.insertId,
      alias,
      grado,
      estado: "ACTIVO",
    });
  } catch (error) {
    if (error.code === "ER_DUP_ENTRY") {
      return res.status(409).json({
        estado: "error",
        mensaje: "Ese alias ya está en uso. Elige otro.",
      });
    }

    console.error("Error al registrar estudiante:", error.code);

    res.status(500).json({
      estado: "error",
      mensaje: "No se pudo registrar al estudiante.",
    });
  }
});
router.get("/", autenticar, permitirRol("DOCENTE"), async (req, res) => {
  sinCuerpo(req);
  const consulta = filtros(req, ["grado", "q", "estado"]);
  const condiciones = [];
  const parametros = [];
  if (consulta.grado !== undefined) {
    if (!["3", "4"].includes(consulta.grado))
      fallar(400, "El grado debe ser 3 o 4.");
    condiciones.push("grado = ?");
    parametros.push(Number(consulta.grado));
  }
  if (consulta.estado !== undefined) {
    if (!["ACTIVO", "INACTIVO"].includes(consulta.estado))
      fallar(400, "Estado de estudiante inválido.");
    condiciones.push("estado = ?");
    parametros.push(consulta.estado);
  }
  if (consulta.q !== undefined) {
    const busqueda = consulta.q.trim().normalize("NFC").toLocaleLowerCase("es");
    if (!busqueda || [...busqueda].length > 100)
      fallar(400, "La búsqueda debe tener entre 1 y 100 caracteres.");
    condiciones.push("LOCATE(?, aliasNormalizado) > 0");
    parametros.push(busqueda);
  }
  const [estudiantes] = await pool.execute(
    `SELECT idEstudiante, alias, grado, estado
                 FROM ESTUDIANTE
                 ${condiciones.length ? "WHERE " + condiciones.join(" AND ") : ""}
                 ORDER BY aliasNormalizado, idEstudiante`,
    parametros,
  );

  res.json({
    datos: estudiantes,
    total: estudiantes.length,
  });
});

router.get("/:id/progreso", autenticar, async (req, res) => {
  sinCuerpo(req);
  filtros(req);
  const id = identificador(req.params.id);
  permitirPropietario(req.sesion, id);
  const [[estudiante]] = await pool.execute(
    "SELECT idEstudiante, alias, grado, estado FROM ESTUDIANTE WHERE idEstudiante = ?",
    [id],
  );
  if (!estudiante) fallar(404, "No se encontró el estudiante.");
  res.json(await obtenerProgreso(estudiante));
});
module.exports = router;
