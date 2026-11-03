const express = require("express");
const pool = require("../db");
const { crearHashCodigo } = require("../seguridad");
const { validarRegistroEstudiante } = require("../validaciones");
const {
    autenticar,
    permitirRol
} = require("../middleware/autenticacion");

const router = express.Router();

router.post("/", async (req, res) => {
    const validacion = validarRegistroEstudiante(req.body);

    if (validacion.error) {
        return res.status(400).json({
            estado: "error",
            mensaje: validacion.error
        });
    }

    const {
        alias,
        aliasNormalizado,
        grado,
        codigoAcceso
    } = validacion.datos;

    try {
        const codigoAccesoHash = await crearHashCodigo(codigoAcceso);

        const [resultado] = await pool.execute(
            `INSERT INTO ESTUDIANTE (
                alias, aliasNormalizado, grado, codigoAccesoHash
            ) VALUES (?, ?, ?, ?)`,
            [alias, aliasNormalizado, grado, codigoAccesoHash]
        );

        res.status(201).json({
            idEstudiante: resultado.insertId,
            alias,
            grado,
            estado: "ACTIVO"
        });
    } catch (error) {
        if (error.code === "ER_DUP_ENTRY") {
            return res.status(409).json({
                estado: "error",
                mensaje: "Ese alias ya está en uso. Elige otro."
            });
        }

        console.error("Error al registrar estudiante:", error.code);

        res.status(500).json({
            estado: "error",
            mensaje: "No se pudo registrar al estudiante."
        });
    }
});
router.get(
    "/",
    autenticar,
    permitirRol("DOCENTE"),
    async (req, res) => {
        try {
            const [estudiantes] = await pool.query(
                `SELECT idEstudiante, alias, grado, estado
                 FROM ESTUDIANTE
                 ORDER BY aliasNormalizado, idEstudiante`
            );

            res.json({
                datos: estudiantes,
                total: estudiantes.length
            });
        } catch (error) {
            console.error(
                "Error al consultar estudiantes:",
                error.code
            );

            res.status(500).json({
                estado: "error",
                mensaje: "No se pudo consultar la lista de estudiantes."
            });
        }
    }
);
module.exports = router;