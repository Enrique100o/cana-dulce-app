const express = require("express");
const { validarIdentificador } = require("../validaciones");
const { autenticar, permitirRol } = require("../middleware/autenticacion");
const { ejecutarTransaccion } = require("../transacciones");
const { obtenerLecturaParaEdicion } = require("../servicios/lecturas");
const { obtenerPreguntas } = require("../servicios/preguntas");
const { sinCuerpo, filtros } = require("../http");

const router = express.Router();

router.get(
  "/lecturas/:id/preguntas",
  autenticar,
  permitirRol("DOCENTE"),
  async (req, res) => {
    sinCuerpo(req);
    filtros(req, ["estado"]);

    if (
      req.query.estado !== undefined &&
      !["ACTIVA", "INACTIVA"].includes(req.query.estado)
    ) {
      return res
        .status(400)
        .json({ estado: "error", mensaje: "Estado de pregunta inválido." });
    }

    const identificador = validarIdentificador(
      req.params.id,
      "El identificador de la lectura",
    );

    if (identificador.error) {
      return res.status(400).json({
        estado: "error",
        mensaje: identificador.error,
      });
    }

    try {
      const resultado = await ejecutarTransaccion(async (conexion) => {
        const lectura = await obtenerLecturaParaEdicion(
          identificador.datos,
          conexion,
        );

        if (!lectura) {
          return null;
        }

        const preguntas = await obtenerPreguntas(
          lectura.idLectura,
          true,
          conexion,
        );

        const datos = preguntas.filter(
          (pregunta) =>
            !req.query.estado || pregunta.estado === req.query.estado,
        );
        return { datos, total: datos.length };
      });

      if (resultado === null) {
        return res.status(404).json({
          estado: "error",
          mensaje: "No se encontró la lectura.",
        });
      }

      return res.json(resultado);
    } catch (error) {
      console.error(
        "Error al consultar preguntas del docente:",
        error.code || error.name,
      );

      return res.status(500).json({
        estado: "error",
        mensaje: "No se pudieron consultar las preguntas.",
      });
    }
  },
);

module.exports = router;
