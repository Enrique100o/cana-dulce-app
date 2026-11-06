const express = require("express");
const pool = require("../db");
const {
  validarCreacionLectura,
  validarConsultaLecturas,
  validarIdentificador,
  validarCreacionPregunta,
  validarActualizacionLectura,
} = require("../validaciones");
const {
  esLecturaPublicable,
  obtenerLectura,
  puedeConsultarLectura,
  obtenerLecturaParaEdicion,
  actualizarLectura,
  desactivarLectura,
  eliminarLectura,
} = require("../servicios/lecturas");
const { autenticar, permitirRol } = require("../middleware/autenticacion");
const { crearPregunta, obtenerPreguntas } = require("../servicios/preguntas");
const { ejecutarTransaccion } = require("../transacciones");
const { sinCuerpo, filtros } = require("../http");
const router = express.Router();

router.post("/", autenticar, permitirRol("DOCENTE"), async (req, res) => {
  const validacion = validarCreacionLectura(req.body);

  if (validacion.error) {
    return res.status(400).json({
      estado: "error",
      mensaje: validacion.error,
    });
  }

  const { titulo, contenido, grado, dificultad, categoria, estado } =
    validacion.datos;

  try {
    const [resultado] = await pool.execute(
      `INSERT INTO LECTURA (
                    titulo, contenido, grado,
                    dificultad, categoria, estado
                ) VALUES (?, ?, ?, ?, ?, ?)`,
      [titulo, contenido, grado, dificultad, categoria, estado],
    );

    res.location(`/api/lecturas/${resultado.insertId}`);

    res.status(201).json({
      idLectura: resultado.insertId,
      titulo,
      contenido,
      grado,
      dificultad,
      categoria,
      estado,
      totalPreguntasActivas: 0,
      bloqueadaParaEdicion: false,
    });
  } catch (error) {
    console.error("Error al crear lectura:", error.code);

    res.status(500).json({
      estado: "error",
      mensaje: "No se pudo crear la lectura.",
    });
  }
});
router.get("/", autenticar, async (req, res) => {
  sinCuerpo(req);

  const validacion = validarConsultaLecturas(req.query);

  if (validacion.error) {
    return res.status(400).json({
      estado: "error",
      mensaje: validacion.error,
    });
  }

  const { grado, estado, q } = validacion.datos;
  const esEstudiante = req.sesion.rol === "ESTUDIANTE";
  const condiciones = [];
  const parametros = [];

  if (esEstudiante) {
    const gradoEstudiante = req.sesion.estudiante.grado;

    if (
      (grado !== undefined && grado !== gradoEstudiante) ||
      estado === "INACTIVA"
    ) {
      return res.status(403).json({
        estado: "error",
        mensaje: "No tienes permiso para consultar esas lecturas.",
      });
    }

    condiciones.push("l.grado = ?", "l.estado = 'ACTIVA'");
    parametros.push(gradoEstudiante);
  } else {
    if (grado !== undefined) {
      condiciones.push("l.grado = ?");
      parametros.push(grado);
    }

    if (estado !== undefined) {
      condiciones.push("l.estado = ?");
      parametros.push(estado);
    }
  }

  if (q !== undefined) {
    condiciones.push("LOCATE(?, LOWER(l.titulo)) > 0");
    parametros.push(q);
  }

  const filtroSQL =
    condiciones.length > 0 ? "WHERE " + condiciones.join(" AND ") : "";

  try {
    const [lecturas] = await pool.execute(
      `SELECT
                l.idLectura,
                l.titulo,
                l.grado,
                l.dificultad,
                l.categoria,
                l.estado,
                (
                    SELECT COUNT(*)
                    FROM PREGUNTA p
                    WHERE p.idLectura = l.idLectura
                      AND p.estado = 'ACTIVA'
                ) AS totalPreguntasActivas,
                EXISTS (
                    SELECT 1
                    FROM INTENTO i
                    WHERE i.idLectura = l.idLectura
                ) AS bloqueadaParaEdicion
             FROM LECTURA l
             ${filtroSQL}
             ORDER BY LOWER(l.titulo), l.idLectura`,
      parametros,
    );

    const datos = [];

    for (const lectura of lecturas) {
      if (esEstudiante && !(await esLecturaPublicable(lectura.idLectura))) {
        continue;
      }

      datos.push({
        ...lectura,
        totalPreguntasActivas: Number(lectura.totalPreguntasActivas),
        bloqueadaParaEdicion: Number(lectura.bloqueadaParaEdicion) === 1,
      });
    }

    res.json({
      datos,
      total: datos.length,
    });
  } catch (error) {
    console.error("Error al consultar lecturas:", error.code);

    res.status(500).json({
      estado: "error",
      mensaje: "No se pudieron consultar las lecturas.",
    });
  }
});
router.get("/:id", autenticar, async (req, res) => {
  sinCuerpo(req);
  filtros(req);

  const validacion = validarIdentificador(
    req.params.id,
    "El identificador de la lectura",
  );

  if (validacion.error) {
    return res.status(400).json({
      estado: "error",
      mensaje: validacion.error,
    });
  }

  try {
    const lectura = await obtenerLectura(validacion.datos);

    const tieneAcceso = await puedeConsultarLectura(req.sesion, lectura);

    if (!tieneAcceso) {
      return res.status(404).json({
        estado: "error",
        mensaje: "No se encontró la lectura.",
      });
    }

    return res.json(lectura);
  } catch (error) {
    console.error("Error al consultar lectura:", error.code || error.name);

    return res.status(500).json({
      estado: "error",
      mensaje: "No se pudo consultar la lectura.",
    });
  }
});
router.post(
  "/:id/preguntas",
  autenticar,
  permitirRol("DOCENTE"),
  async (req, res) => {
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

    const validacion = validarCreacionPregunta(req.body);

    if (validacion.error) {
      return res.status(400).json({
        estado: "error",
        mensaje: validacion.error,
      });
    }

    try {
      const pregunta = await crearPregunta(
        identificador.datos,
        validacion.datos,
      );

      return res.status(201).json(pregunta);
    } catch (error) {
      if ([404, 409].includes(error.estadoHTTP)) {
        return res.status(error.estadoHTTP).json({
          estado: "error",
          mensaje: error.message,
        });
      }

      if (error.code === "ER_DUP_ENTRY") {
        return res.status(409).json({
          estado: "error",
          mensaje: "Ya existe una pregunta con ese orden en la lectura.",
        });
      }

      console.error("Error al crear la pregunta:", error.code || error.name);

      return res.status(500).json({
        estado: "error",
        mensaje: "No se pudo crear la pregunta.",
      });
    }
  },
);
router.get("/:id/preguntas", autenticar, async (req, res) => {
  sinCuerpo(req);
  filtros(req);

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

      const tieneAcceso = await puedeConsultarLectura(
        req.sesion,
        lectura,
        conexion,
      );

      if (!tieneAcceso) {
        return null;
      }

      const preguntas = await obtenerPreguntas(
        lectura.idLectura,
        false,
        conexion,
      );

      return {
        datos: preguntas,
        total: preguntas.length,
      };
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
      "Error al consultar el cuestionario:",
      error.code || error.name,
    );

    return res.status(500).json({
      estado: "error",
      mensaje: "No se pudieron consultar las preguntas.",
    });
  }
});
router.put("/:id", autenticar, permitirRol("DOCENTE"), async (req, res) => {
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

  const validacion = validarActualizacionLectura(req.body);

  if (validacion.error) {
    return res.status(400).json({
      estado: "error",
      mensaje: validacion.error,
    });
  }

  try {
    const lectura = await actualizarLectura(
      identificador.datos,
      validacion.datos,
    );

    return res.json(lectura);
  } catch (error) {
    if ([404, 409].includes(error.estadoHTTP)) {
      return res.status(error.estadoHTTP).json({
        estado: "error",
        mensaje: error.message,
      });
    }

    console.error("Error al actualizar lectura:", error.code || error.name);

    return res.status(500).json({
      estado: "error",
      mensaje: "No se pudo actualizar la lectura.",
    });
  }
});
router.delete("/:id", autenticar, permitirRol("DOCENTE"), async (req, res) => {
  sinCuerpo(req);
  filtros(req);

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
    await desactivarLectura(identificador.datos);

    return res.status(204).end();
  } catch (error) {
    if (error.estadoHTTP === 404) {
      return res.status(404).json({
        estado: "error",
        mensaje: error.message,
      });
    }

    console.error("Error al desactivar lectura:", error.code || error.name);

    return res.status(500).json({
      estado: "error",
      mensaje: "No se pudo desactivar la lectura.",
    });
  }
});
router.delete(
  "/:id/eliminar",
  autenticar,
  permitirRol("DOCENTE"),
  async (req, res) => {
    sinCuerpo(req);
    filtros(req);

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
      await eliminarLectura(identificador.datos);
      return res.status(204).end();
    } catch (error) {
      if (error.estadoHTTP === 409) {
        return res.status(409).json({ estado: "error", mensaje: error.message });
      }
      console.error("Error al eliminar lectura:", error.code || error.name);
      return res.status(500).json({
        estado: "error",
        mensaje: "No se pudo eliminar la lectura.",
      });
    }
  },
);

module.exports = router;
