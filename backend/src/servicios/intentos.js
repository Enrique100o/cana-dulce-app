const { ejecutarTransaccion } = require("../transacciones");
const { fallar } = require("../http");
const {
  obtenerLecturaParaEdicion,
  esLecturaPublicable,
} = require("./lecturas");
const { obtenerPreguntas } = require("./preguntas");
const { puntaje, estrellas } = require("./progreso");

async function buscarIntento(idIntento, conexion, bloquear = false) {
  const [filas] = await conexion.execute(
    "SELECT * FROM INTENTO WHERE idIntento = ?" +
      (bloquear ? " FOR UPDATE" : ""),
    [idIntento],
  );
  if (!filas[0]) fallar(404, "No se encontró el intento.");
  return filas[0];
}

function permitirPropietario(sesion, idEstudiante) {
  if (
    sesion.rol === "ESTUDIANTE" &&
    sesion.estudiante.idEstudiante !== idEstudiante
  ) {
    fallar(403, "Solo puedes consultar tus propias actividades.");
  }
}

async function representarIntento(intento, conexion, detalle = true) {
  const [[lectura]] = await conexion.execute(
    "SELECT titulo FROM LECTURA WHERE idLectura = ?",
    [intento.idLectura],
  );
  const valor = intento.puntaje === null ? null : Number(intento.puntaje);
  const resultado = {
    idIntento: intento.idIntento,
    idEstudiante: intento.idEstudiante,
    idLectura: intento.idLectura,
    tituloLectura: lectura.titulo,
    fechaInicio: intento.fechaInicio,
    fechaFinalizacion: intento.fechaFinalizacion,
    cantidadCorrectas: intento.cantidadCorrectas,
    totalPreguntas: intento.totalPreguntas,
    puntaje: valor,
    estrellas: estrellas(valor),
    estado: intento.estado,
  };
  if (detalle) {
    resultado.respuestas = [];
    if (intento.estado === "COMPLETADO") {
      const [filas] = await conexion.execute(
        `SELECT r.idRespuesta, r.idPregunta, p.enunciado, p.tipo, p.orden,
                r.idOpcion, o.texto AS textoOpcionElegida, r.esCorrecta
         FROM RESPUESTA r JOIN PREGUNTA p ON p.idPregunta = r.idPregunta
         JOIN OPCION o ON o.idOpcion = r.idOpcion
         WHERE r.idIntento = ? ORDER BY p.orden, p.idPregunta`,
        [intento.idIntento],
      );
      resultado.respuestas = filas.map((fila) => ({
        ...fila,
        esCorrecta: Boolean(fila.esCorrecta),
      }));
    }
  }
  return resultado;
}

async function iniciarIntento(estudiante, idLectura) {
  return ejecutarTransaccion(async (conexion) => {
    const lectura = await obtenerLecturaParaEdicion(idLectura, conexion);
    if (
      !lectura ||
      lectura.estado !== "ACTIVA" ||
      !(await esLecturaPublicable(idLectura, conexion))
    ) {
      fallar(404, "No se encontró una misión disponible.");
    }
    if (lectura.grado !== estudiante.grado)
      fallar(403, "Elige una misión de tu grado.");
    const [pendientes] = await conexion.execute(
      `SELECT idIntento FROM INTENTO WHERE idEstudiante = ?
       AND idLectura = ? AND estado = 'EN_PROGRESO'`,
      [estudiante.idEstudiante, idLectura],
    );
    if (pendientes[0]) {
      fallar(409, "Ya tienes una actividad en curso para esta lectura.", {
        idIntentoExistente: pendientes[0].idIntento,
      });
    }
    const preguntas = await obtenerPreguntas(idLectura, false, conexion);
    const [insertado] = await conexion.execute(
      `INSERT INTO INTENTO (idEstudiante, idLectura, fechaInicio, totalPreguntas)
       VALUES (?, ?, ?, ?)`,
      [estudiante.idEstudiante, idLectura, new Date(), preguntas.length],
    );
    return representarIntento(
      await buscarIntento(insertado.insertId, conexion),
      conexion,
    );
  });
}

async function consultarIntento(sesion, idIntento) {
  return ejecutarTransaccion(async (conexion) => {
    const intento = await buscarIntento(idIntento, conexion, true);
    permitirPropietario(sesion, intento.idEstudiante);
    return representarIntento(intento, conexion);
  });
}

async function finalizarIntento(sesion, idIntento, selecciones) {
  return ejecutarTransaccion(async (conexion) => {
    const intento = await buscarIntento(idIntento, conexion, true);
    permitirPropietario(sesion, intento.idEstudiante);
    if (intento.estado === "COMPLETADO") {
      const [guardadas] = await conexion.execute(
        "SELECT idPregunta, idOpcion FROM RESPUESTA WHERE idIntento = ?",
        [idIntento],
      );
      const mismas =
        guardadas.length === selecciones.length &&
        guardadas.every((guardada) =>
          selecciones.some(
            (seleccion) =>
              seleccion.idPregunta === guardada.idPregunta &&
              seleccion.idOpcion === guardada.idOpcion,
          ),
        );
      if (!mismas)
        fallar(409, "El intento ya está completado y no puede modificarse.");
      return representarIntento(intento, conexion);
    }
    const preguntas = (
      await obtenerPreguntas(intento.idLectura, true, conexion)
    ).filter((pregunta) => pregunta.estado === "ACTIVA");
    if (preguntas.length !== intento.totalPreguntas)
      fallar(409, "El cuestionario no coincide con el intento.");
    if (selecciones.length !== preguntas.length)
      fallar(400, "Responde todas las preguntas antes de finalizar.");
    const evaluadas = selecciones.map((seleccion) => {
      const pregunta = preguntas.find(
        (item) =>
          item.idPregunta === seleccion.idPregunta && item.estado === "ACTIVA",
      );
      if (!pregunta) fallar(400, "Una pregunta no pertenece a esta actividad.");
      const opcion = pregunta.opciones.find(
        (item) => item.idOpcion === seleccion.idOpcion,
      );
      if (!opcion) fallar(400, "Una opción no pertenece a su pregunta.");
      return { ...seleccion, esCorrecta: Number(opcion.esCorrecta) };
    });
    const correctas = evaluadas.filter(
      (seleccion) => seleccion.esCorrecta === 1,
    ).length;
    for (const seleccion of evaluadas) {
      await conexion.execute(
        "INSERT INTO RESPUESTA (idIntento, idPregunta, idOpcion, esCorrecta) VALUES (?, ?, ?, ?)",
        [
          idIntento,
          seleccion.idPregunta,
          seleccion.idOpcion,
          seleccion.esCorrecta,
        ],
      );
    }
    await conexion.execute(
      `UPDATE INTENTO SET fechaFinalizacion = ?, cantidadCorrectas = ?, puntaje = ?,
       estado = 'COMPLETADO' WHERE idIntento = ?`,
      [
        new Date(),
        correctas,
        puntaje(correctas, intento.totalPreguntas),
        idIntento,
      ],
    );
    return representarIntento(
      await buscarIntento(idIntento, conexion),
      conexion,
    );
  });
}

module.exports = {
  buscarIntento,
  permitirPropietario,
  representarIntento,
  iniciarIntento,
  consultarIntento,
  finalizarIntento,
};
