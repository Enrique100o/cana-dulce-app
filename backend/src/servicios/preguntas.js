const pool = require("../db");
const {
    ejecutarTransaccion
} = require("../transacciones");

const {
    obtenerLecturaParaEdicion
} = require("./lecturas");
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
            [
                idPregunta,
                opcion.texto,
                opcion.orden,
                Number(opcion.esCorrecta)
            ]
        );

        opcionesGuardadas.push({
            idOpcion: resultado.insertId,
            texto: opcion.texto,
            orden: opcion.orden,
            esCorrecta: opcion.esCorrecta
        });
    }

    return opcionesGuardadas;
}
async function crearPregunta(idLectura, datos) {
    return ejecutarTransaccion(async conexion => {
        const lectura = await obtenerLecturaParaEdicion(
            idLectura,
            conexion
        );

        if (!lectura) {
            const error = new Error(
                "No se encontró la lectura."
            );

            error.estadoHTTP = 404;

            throw error;
        }

        if (lectura.bloqueadaParaEdicion) {
            const error = new Error(
                "La lectura ya tiene intentos y no permite cambiar sus preguntas."
            );

            error.estadoHTTP = 409;

            throw error;
        }

        const [resultado] = await conexion.execute(
            `INSERT INTO PREGUNTA (
                idLectura,
                enunciado,
                tipo,
                orden,
                estado
             ) VALUES (?, ?, ?, ?, ?)`,
            [
                idLectura,
                datos.enunciado,
                datos.tipo,
                datos.orden,
                datos.estado
            ]
        );

        const idPregunta = resultado.insertId;

        const opciones = await guardarOpciones(
            idPregunta,
            datos.opciones,
            conexion
        );

        return {
            idPregunta,
            idLectura,
            enunciado: datos.enunciado,
            tipo: datos.tipo,
            orden: datos.orden,
            estado: datos.estado,
            opciones
        };
    });
}
async function obtenerOpciones(
    idPregunta,
    incluirCorrectas = false,
    conexion = pool
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
        [idPregunta]
    );

    return filas.map(fila => {
        const opcion = {
            idOpcion: fila.idOpcion,
            texto: fila.texto,
            orden: fila.orden
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
    conexion = pool
) {
    const esAdministrativa = administrativa === true;

    const filtroEstado = esAdministrativa
        ? ""
        : "AND estado = 'ACTIVA'";

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
        [idLectura]
    );

    const preguntas = [];

    for (const fila of filas) {
        const opciones = await obtenerOpciones(
            fila.idPregunta,
            esAdministrativa,
            conexion
        );

        const pregunta = {
            idPregunta: fila.idPregunta,
            idLectura: fila.idLectura,
            enunciado: fila.enunciado,
            tipo: fila.tipo,
            orden: fila.orden,
            opciones
        };

        if (esAdministrativa) {
            pregunta.estado = fila.estado;
        }

        preguntas.push(pregunta);
    }

    return preguntas;
}
module.exports = {
    guardarOpciones,
    crearPregunta,
    obtenerOpciones,
    obtenerPreguntas
};