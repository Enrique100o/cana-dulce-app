const express = require("express");

const {
    validarIdentificador
} = require("../validaciones");

const {
    autenticar,
    permitirRol
} = require("../middleware/autenticacion");

const {
    ejecutarTransaccion
} = require("../transacciones");

const {
    obtenerLecturaParaEdicion
} = require("../servicios/lecturas");

const {
    obtenerPreguntas
} = require("../servicios/preguntas");

const router = express.Router();

router.get(
    "/lecturas/:id/preguntas",
    autenticar,
    permitirRol("DOCENTE"),
    async (req, res) => {
        if (
            req.body !== undefined ||
            Number(req.get("Content-Length") || 0) > 0 ||
            req.get("Transfer-Encoding") ||
            Object.keys(req.query).length > 0
        ) {
            return res.status(400).json({
                estado: "error",
                mensaje: "Esta consulta no recibe filtros ni cuerpo."
            });
        }

        const identificador = validarIdentificador(
            req.params.id,
            "El identificador de la lectura"
        );

        if (identificador.error) {
            return res.status(400).json({
                estado: "error",
                mensaje: identificador.error
            });
        }

        try {
            const resultado = await ejecutarTransaccion(
                async conexion => {
                    const lectura = await obtenerLecturaParaEdicion(
                        identificador.datos,
                        conexion
                    );

                    if (!lectura) {
                        return null;
                    }

                    const preguntas = await obtenerPreguntas(
                        lectura.idLectura,
                        true,
                        conexion
                    );

                    return {
                        datos: preguntas,
                        total: preguntas.length
                    };
                }
            );

            if (resultado === null) {
                return res.status(404).json({
                    estado: "error",
                    mensaje: "No se encontró la lectura."
                });
            }

            return res.json(resultado);
        } catch (error) {
            console.error(
                "Error al consultar preguntas del docente:",
                error.code || error.name
            );

            return res.status(500).json({
                estado: "error",
                mensaje: "No se pudieron consultar las preguntas."
            });
        }
    }
);

module.exports = router;