const pool = require("../db");
const { ejecutarTransaccion } = require("../transacciones");

const {
  obtenerLecturaParaEdicion,
  esLecturaPublicable,
} = require("./lecturas");
const { fallar } = require("../http");
async function guardarOpciones(idPregunta, opciones, conexion) {
  const opcionesGuardadas = [];

  for (const opcion of opciones) {
    const [resultado] = await conexion.execute(
      `INSERT INTO OPCION (
                idPregunta,
                texto,
                orden,
                esCorrecta
             ) VALUES (?, ?, ?, ?)`,
      [idPregunta, opcion.texto, opcion.orden, Number(opcion.esCorrecta)],
    );

    opcionesGuardadas.push({
      idOpcion: resultado.insertId,
      texto: opcion.texto,
      orden: opcion.orden,
      esCorrecta: opcion.esCorrecta,
    });
  }

  return opcionesGuardadas;
}

async function crearPregunta(idLectura, datos) {
  return ejecutarTransaccion(async (conexion) => {
    const lectura = await obtenerLecturaParaEdicion(idLectura, conexion);

    if (!lectura) {
      fallar(404, "No se encontró la lectura.");
    }

    if (lectura.bloqueadaParaEdicion) {
      fallar(
        409,
        "La lectura ya tiene intentos y no permite cambiar sus preguntas.",
      );
    }

    const [resultado] = await conexion.execute(
      `INSERT INTO PREGUNTA (
                idLectura,
                enunciado,
                tipo,
                orden,
                estado
             ) VALUES (?, ?, ?, ?, ?)`,
      [idLectura, datos.enunciado, datos.tipo, datos.orden, datos.estado],
    );

    const idPregunta = resultado.insertId;

    const opciones = await guardarOpciones(
      idPregunta,
      datos.opciones,
      conexion,
    );

    return {
      idPregunta,
      idLectura,
      enunciado: datos.enunciado,
      tipo: datos.tipo,
      orden: datos.orden,
      estado: datos.estado,
      opciones,
    };
  });
}

async function obtenerOpciones(
  idPregunta,
  incluirCorrectas = false,
  conexion = pool,
) {
  const [filas] = await conexion.execute(
    `SELECT
            idOpcion,
            texto,
            orden,
            esCorrecta
         FROM OPCION
         WHERE idPregunta = ?
         ORDER BY orden, idOpcion`,
    [idPregunta],
  );

  return filas.map((fila) => {
    const opcion = {
      idOpcion: fila.idOpcion,
      texto: fila.texto,
      orden: fila.orden,
    };

    if (incluirCorrectas === true) {
      opcion.esCorrecta = Number(fila.esCorrecta) === 1;
    }

    return opcion;
  });
}

async function obtenerPreguntas(
  idLectura,
  administrativa = false,
  conexion = pool,
) {
  const esAdministrativa = administrativa === true;

  const filtroEstado = esAdministrativa ? "" : "AND estado = 'ACTIVA'";

  const [filas] = await conexion.execute(
    `SELECT
            idPregunta,
            idLectura,
            enunciado,
            tipo,
            orden,
            estado
         FROM PREGUNTA
         WHERE idLectura = ?
         ${filtroEstado}
         ORDER BY orden, idPregunta`,
    [idLectura],
  );

  const preguntas = [];

  for (const fila of filas) {
    const opciones = await obtenerOpciones(
      fila.idPregunta,
      esAdministrativa,
      conexion,
    );

    const pregunta = {
      idPregunta: fila.idPregunta,
      idLectura: fila.idLectura,
      enunciado: fila.enunciado,
      tipo: fila.tipo,
      orden: fila.orden,
      opciones,
    };

    if (esAdministrativa) {
      pregunta.estado = fila.estado;
    }

    preguntas.push(pregunta);
  }

  return preguntas;
}

async function editarPregunta(idPregunta, datos, desactivar = false) {
  return ejecutarTransaccion(async (conexion) => {
    const [[referencia]] = await conexion.execute(
      "SELECT idLectura FROM PREGUNTA WHERE idPregunta = ?",
      [idPregunta],
    );
    if (!referencia) fallar(404, "No se encontró la pregunta.");
    const lectura = await obtenerLecturaParaEdicion(
      referencia.idLectura,
      conexion,
    );
    if (lectura.bloqueadaParaEdicion) {
      fallar(
        409,
        "La lectura ya tiene intentos y no permite cambiar sus preguntas.",
      );
    }
    if (desactivar) {
      await conexion.execute(
        "UPDATE PREGUNTA SET estado = 'INACTIVA' WHERE idPregunta = ?",
        [idPregunta],
      );
    } else {
      await conexion.execute(
        "UPDATE PREGUNTA SET enunciado = ?, tipo = ?, orden = ?, estado = ? WHERE idPregunta = ?",
        [datos.enunciado, datos.tipo, datos.orden, datos.estado, idPregunta],
      );
      await conexion.execute("DELETE FROM OPCION WHERE idPregunta = ?", [
        idPregunta,
      ]);
      await guardarOpciones(idPregunta, datos.opciones, conexion);
    }
    if (
      lectura.estado === "ACTIVA" &&
      !(await esLecturaPublicable(lectura.idLectura, conexion))
    ) {
      fallar(
        409,
        "Una lectura publicada debe conservar al menos una pregunta activa válida.",
      );
    }
    const preguntas = await obtenerPreguntas(lectura.idLectura, true, conexion);
    return preguntas.find((pregunta) => pregunta.idPregunta === idPregunta);
  });
}

const actualizarPregunta = (idPregunta, datos) =>
  editarPregunta(idPregunta, datos);
const desactivarPregunta = (idPregunta) =>
  editarPregunta(idPregunta, null, true);

module.exports = {
  crearPregunta,
  obtenerPreguntas,
  actualizarPregunta,
  desactivarPregunta,
};
