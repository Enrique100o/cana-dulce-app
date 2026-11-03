const pool = require("../db");
const {
    ejecutarTransaccion
} = require("../transacciones");

async function esLecturaPublicable(idLectura, conexion = pool) {
    const [preguntas] = await conexion.execute(
        `SELECT
            p.idPregunta,
            COUNT(o.idOpcion) AS totalOpciones,
            COALESCE(SUM(o.esCorrecta), 0) AS totalCorrectas
         FROM PREGUNTA p
         LEFT JOIN OPCION o ON o.idPregunta = p.idPregunta
         WHERE p.idLectura = ?
           AND p.estado = 'ACTIVA'
         GROUP BY p.idPregunta`,
        [idLectura]
    );

    return preguntas.length > 0 && preguntas.every(pregunta => {
        const totalOpciones = Number(pregunta.totalOpciones);
        const totalCorrectas = Number(pregunta.totalCorrectas);

        return totalOpciones >= 2
            && totalOpciones <= 4
            && totalCorrectas === 1;
    });
}
async function obtenerLectura(idLectura, conexion = pool) {
    const [lecturas] = await conexion.execute(
        `SELECT
            l.idLectura,
            l.titulo,
            l.contenido,
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
         WHERE l.idLectura = ?`,
        [idLectura]
    );

    const lectura = lecturas[0];

    if (!lectura) {
        return null;
    }

    return {
        ...lectura,
        totalPreguntasActivas: Number(
            lectura.totalPreguntasActivas
        ),
        bloqueadaParaEdicion: Number(
            lectura.bloqueadaParaEdicion
        ) === 1
    };
}
async function obtenerLecturaParaEdicion(idLectura, conexion) {
    const [lecturas] = await conexion.execute(
        `SELECT idLectura
         FROM LECTURA
         WHERE idLectura = ?
         FOR UPDATE`,
        [idLectura]
    );

    if (!lecturas[0]) {
        return null;
    }

    return obtenerLectura(idLectura, conexion);
}
async function puedeConsultarLectura(
    sesion,
    lectura,
    conexion = pool
) {
    if (!sesion || !lectura) {
        return false;
    }

    if (sesion.rol === "DOCENTE") {
        return true;
    }

    if (
        sesion.rol !== "ESTUDIANTE" ||
        !sesion.estudiante
    ) {
        return false;
    }

    const estudiante = sesion.estudiante;

    const [historial] = await conexion.execute(
        `SELECT idIntento
         FROM INTENTO
         WHERE idEstudiante = ?
           AND idLectura = ?
         LIMIT 1`,
        [estudiante.idEstudiante, lectura.idLectura]
    );

    if (historial.length > 0) {
        return true;
    }

    return (
        lectura.grado === estudiante.grado &&
        lectura.estado === "ACTIVA" &&
        (await esLecturaPublicable(lectura.idLectura, conexion))
    );
}
async function actualizarLectura(idLectura, datos) {
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

        const camposContenido = [
            "titulo",
            "contenido",
            "grado",
            "dificultad",
            "categoria"
        ];

        const cambiaContenido = camposContenido.some(
            campo => lectura[campo] !== datos[campo]
        );

        if (lectura.bloqueadaParaEdicion && cambiaContenido) {
            const error = new Error(
                "El contenido de una lectura con intentos no se puede modificar."
            );

            error.estadoHTTP = 409;

            throw error;
        }

        if (
            datos.estado === "ACTIVA" &&
            !(await esLecturaPublicable(idLectura, conexion))
        ) {
            const error = new Error(
                "La lectura no tiene un cuestionario válido para publicarse."
            );

            error.estadoHTTP = 409;

            throw error;
        }

        await conexion.execute(
            `UPDATE LECTURA
             SET titulo = ?,
                 contenido = ?,
                 grado = ?,
                 dificultad = ?,
                 categoria = ?,
                 estado = ?
             WHERE idLectura = ?`,
            [
                datos.titulo,
                datos.contenido,
                datos.grado,
                datos.dificultad,
                datos.categoria,
                datos.estado,
                idLectura
            ]
        );

        return obtenerLectura(idLectura, conexion);
    });
}
async function desactivarLectura(idLectura) {
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

        await conexion.execute(
            `UPDATE LECTURA
             SET estado = 'INACTIVA'
             WHERE idLectura = ?`,
            [idLectura]
        );
    });
}
module.exports = {
    esLecturaPublicable,
    obtenerLectura,
    obtenerLecturaParaEdicion,
    puedeConsultarLectura,
    actualizarLectura,
    desactivarLectura
};