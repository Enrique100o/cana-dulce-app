const pool = require("../db");
const { verificarToken } = require("../tokens");

async function autenticar(req, res, next) {
  const coincidencia = /^Bearer ([A-Za-z0-9._-]+)$/i.exec(
    req.get("Authorization") || "",
  );

  let datosToken;
  let idEstudiante;

  try {
    if (!coincidencia) {
      throw new Error("Token ausente.");
    }

    datosToken = verificarToken(coincidencia[1]);

    if (
      !datosToken ||
      !["ESTUDIANTE", "DOCENTE"].includes(datosToken.rol) ||
      !Number.isSafeInteger(datosToken.exp)
    ) {
      throw new Error("Sesión inválida.");
    }

    if (datosToken.rol === "ESTUDIANTE") {
      idEstudiante = Number(datosToken.sub);

      if (
        !Number.isSafeInteger(idEstudiante) ||
        idEstudiante <= 0 ||
        String(idEstudiante) !== datosToken.sub
      ) {
        throw new Error("Identificador inválido.");
      }
    } else if (datosToken.sub !== "docente") {
      throw new Error("Identificador docente inválido.");
    }
  } catch {
    return res.status(401).json({
      estado: "error",
      mensaje: "Inicia sesión para continuar.",
    });
  }

  req.sesion = {
    rol: datosToken.rol,
  };

  if (datosToken.rol === "ESTUDIANTE") {
    try {
      const [filas] = await pool.execute(
        `SELECT idEstudiante, alias, grado, estado
                 FROM ESTUDIANTE
                 WHERE idEstudiante = ?`,
        [idEstudiante],
      );

      const estudiante = filas[0];

      if (!estudiante || estudiante.estado !== "ACTIVO") {
        return res.status(403).json({
          estado: "error",
          mensaje: "El perfil no está habilitado.",
        });
      }

      req.sesion.estudiante = estudiante;
    } catch (error) {
      console.error("Error al comprobar la sesión:", error.code);

      return res.status(503).json({
        estado: "error",
        mensaje: "No se pudo comprobar la sesión.",
      });
    }
  }

  next();
}

function permitirRol(rolPermitido) {
  if (!["ESTUDIANTE", "DOCENTE"].includes(rolPermitido)) {
    throw new Error("Configura un rol válido para esta ruta.");
  }

  return (req, res, next) => {
    if (!req.sesion) {
      return res.status(401).json({
        estado: "error",
        mensaje: "Inicia sesión para continuar.",
      });
    }

    if (req.sesion.rol !== rolPermitido) {
      return res.status(403).json({
        estado: "error",
        mensaje: "No tienes permiso para realizar esta acción.",
      });
    }

    next();
  };
}

module.exports = {
  autenticar,
  permitirRol,
};
