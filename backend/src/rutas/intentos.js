const express = require("express");
const pool = require("../db");
const { autenticar, permitirRol } = require("../middleware/autenticacion");
const {
  fallar,
  objeto,
  entero,
  identificador,
  sinCuerpo,
  filtros,
  ruta,
} = require("../http");
const {
  iniciarIntento,
  consultarIntento,
  finalizarIntento,
  permitirPropietario,
  representarIntento,
} = require("../servicios/intentos");

const router = express.Router();
router.use(autenticar);

router.post(
  "/",
  permitirRol("ESTUDIANTE"),
  ruta(async (req, res) => {
    filtros(req);
    objeto(req.body, ["idLectura"]);
    const lectura = entero(req.body.idLectura, "La lectura");
    const resultado = await iniciarIntento(req.sesion.estudiante, lectura);
    res
      .status(201)
      .location(`/api/intentos/${resultado.idIntento}`)
      .json(resultado);
  }),
);

router.get(
  "/",
  ruta(async (req, res) => {
    sinCuerpo(req);
    const consulta = filtros(req, ["estudianteId", "idLectura", "estado"]);
    let estudianteId =
      consulta.estudianteId === undefined
        ? undefined
        : identificador(consulta.estudianteId);
    if (estudianteId !== undefined)
      permitirPropietario(req.sesion, estudianteId);
    if (req.sesion.rol === "ESTUDIANTE")
      estudianteId = req.sesion.estudiante.idEstudiante;
    const condiciones = [];
    const parametros = [];
    if (estudianteId !== undefined) {
      const [[estudiante]] = await pool.execute(
        "SELECT idEstudiante FROM ESTUDIANTE WHERE idEstudiante = ?",
        [estudianteId],
      );
      if (!estudiante) fallar(404, "No se encontró el estudiante.");
      condiciones.push("i.idEstudiante = ?");
      parametros.push(estudianteId);
    }
    if (consulta.idLectura !== undefined) {
      const lecturaId = identificador(consulta.idLectura);
      const [[lectura]] = await pool.execute(
        "SELECT idLectura FROM LECTURA WHERE idLectura = ?",
        [lecturaId],
      );
      if (!lectura) fallar(404, "No se encontró la lectura.");
      condiciones.push("i.idLectura = ?");
      parametros.push(lecturaId);
    }
    if (consulta.estado !== undefined) {
      if (!["EN_PROGRESO", "COMPLETADO"].includes(consulta.estado))
        fallar(400, "Estado de intento inválido.");
      condiciones.push("i.estado = ?");
      parametros.push(consulta.estado);
    }
    const [filas] = await pool.execute(
      `SELECT i.* FROM INTENTO i ${condiciones.length ? "WHERE " + condiciones.join(" AND ") : ""}
     ORDER BY i.fechaInicio DESC, i.idIntento DESC`,
      parametros,
    );
    const datos = [];
    for (const fila of filas)
      datos.push(await representarIntento(fila, pool, false));
    res.json({ datos, total: datos.length });
  }),
);

router.get(
  "/:id",
  ruta(async (req, res) => {
    sinCuerpo(req);
    filtros(req);
    res.json(await consultarIntento(req.sesion, identificador(req.params.id)));
  }),
);

router.post(
  "/:id/finalizar",
  permitirRol("ESTUDIANTE"),
  ruta(async (req, res) => {
    filtros(req);
    objeto(req.body, ["respuestas"]);
    if (
      !Array.isArray(req.body.respuestas) ||
      req.body.respuestas.length > 999
    ) {
      fallar(400, "Envía la lista de respuestas de la actividad.");
    }
    const selecciones = req.body.respuestas.map((respuesta) => {
      objeto(respuesta, ["idPregunta", "idOpcion"]);
      return {
        idPregunta: entero(respuesta.idPregunta),
        idOpcion: entero(respuesta.idOpcion),
      };
    });
    if (
      new Set(selecciones.map((seleccion) => seleccion.idPregunta)).size !==
      selecciones.length
    ) {
      fallar(400, "Envía una sola respuesta por pregunta.");
    }
    res.json(
      await finalizarIntento(
        req.sesion,
        identificador(req.params.id),
        selecciones,
      ),
    );
  }),
);

module.exports = router;
