const express = require("express");
const { autenticar, permitirRol } = require("../middleware/autenticacion");
const {
  fallar,
  objeto,
  identificador,
  sinCuerpo,
  filtros,
  ruta,
} = require("../http");
const { validarCreacionPregunta } = require("../validaciones");
const {
  actualizarPregunta,
  desactivarPregunta,
} = require("../servicios/preguntas");
const router = express.Router();
router.use(autenticar, permitirRol("DOCENTE"));

router.put(
  "/:id",
  ruta(async (req, res) => {
    filtros(req);
    objeto(req.body, ["enunciado", "tipo", "orden", "estado", "opciones"]);
    if (!["ACTIVA", "INACTIVA"].includes(req.body.estado))
      fallar(400, "Selecciona un estado válido.");
    const { estado, ...campos } = req.body;
    const validacion = validarCreacionPregunta(campos);
    if (validacion.error) fallar(400, validacion.error);
    res.json(
      await actualizarPregunta(identificador(req.params.id), {
        ...validacion.datos,
        estado,
      }),
    );
  }),
);

router.delete(
  "/:id",
  ruta(async (req, res) => {
    sinCuerpo(req);
    filtros(req);
    await desactivarPregunta(identificador(req.params.id));
    res.status(204).end();
  }),
);

module.exports = router;
