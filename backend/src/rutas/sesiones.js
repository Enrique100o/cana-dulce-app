const express = require("express");
const pool = require("../db");
const { crearHashCodigo, verificarCodigo } = require("../seguridad");
const {
    validarInicioSesionEstudiante,
    validarInicioSesionDocente
} = require("../validaciones");

const {
    crearTokenEstudiante,
    crearTokenDocente
} = require("../tokens");
const { autenticar } = require("../middleware/autenticacion");
const router = express.Router();

const promesaHashFicticio = crearHashCodigo("comparacion-interna");

router.post("/estudiante", async (req, res) => {
    const validacion = validarInicioSesionEstudiante(req.body);

    if (validacion.error) {
        return res.status(400).json({
            estado: "error",
            mensaje: validacion.error
        });
    }

    const { aliasNormalizado, codigoAcceso } = validacion.datos;

    try {
        const hashFicticio = await promesaHashFicticio;

        const [filas] = await pool.execute(
            `SELECT idEstudiante, alias, grado, estado, codigoAccesoHash
             FROM ESTUDIANTE
             WHERE aliasNormalizado = ?`,
            [aliasNormalizado]
        );

        const estudiante = filas[0];

        const hashComparacion = estudiante
            ? estudiante.codigoAccesoHash
            : hashFicticio;

        const codigoCorrecto = await verificarCodigo(
            codigoAcceso,
            hashComparacion
        );

        if (
            !estudiante ||
            !codigoCorrecto ||
            estudiante.estado !== "ACTIVO"
        ) {
            return res.status(401).json({
                estado: "error",
                mensaje: "Alias o código de acceso incorrectos."
            });
        }

        res.status(200).json({
            token: crearTokenEstudiante(estudiante.idEstudiante),
            tipoToken: "Bearer",
            expiraEnSegundos: 28800,
            rol: "ESTUDIANTE",
            estudiante: {
                idEstudiante: estudiante.idEstudiante,
                alias: estudiante.alias,
                grado: estudiante.grado,
                estado: estudiante.estado
            }
        });
    } catch (error) {
        console.error("Error al iniciar sesión:", error.code);

        res.status(500).json({
            estado: "error",
            mensaje: "No se pudo iniciar sesión."
        });
    }
});
router.post("/docente", async (req, res) => {
    const validacion = validarInicioSesionDocente(req.body);

    if (validacion.error) {
        return res.status(400).json({
            estado: "error",
            mensaje: validacion.error
        });
    }

    const { usuario, contrasena } = validacion.datos;

    try {
        const usuarioDocente = process.env.DOCENTE_USUARIO;
        const hashDocente = process.env.DOCENTE_PASSWORD_HASH;

        if (!usuarioDocente || !hashDocente) {
            return res.status(503).json({
                estado: "error",
                mensaje: "El acceso docente no está disponible."
            });
        }

        const contrasenaCorrecta = await verificarCodigo(
            contrasena,
            hashDocente
        );

        if (usuario !== usuarioDocente || !contrasenaCorrecta) {
            return res.status(401).json({
                estado: "error",
                mensaje: "Usuario o contraseña incorrectos."
            });
        }

        res.status(200).json({
            token: crearTokenDocente(),
            tipoToken: "Bearer",
            expiraEnSegundos: 28800,
            rol: "DOCENTE"
        });
    } catch (error) {
        console.error(
            "Error al iniciar sesión docente:",
            error.code || error.name
        );

        res.status(500).json({
            estado: "error",
            mensaje: "No se pudo iniciar sesión."
        });
    }
});
router.get("/actual", autenticar, (req, res) => {
    res.json(req.sesion);
});
module.exports = router;