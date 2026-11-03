const jwt = require("jsonwebtoken");

const secreto = process.env.JWT_SECRET;

if (!/^[0-9a-f]{64}$/i.test(secreto || "")) {
    throw new Error("Configura JWT_SECRET con 64 caracteres hexadecimales.");
}

function crearTokenEstudiante(idEstudiante) {
    if (!Number.isSafeInteger(idEstudiante) || idEstudiante <= 0) {
        throw new Error("El identificador del estudiante no es válido.");
    }

    return jwt.sign(
        { rol: "ESTUDIANTE" },
        secreto,
        {
            algorithm: "HS256",
            expiresIn: "8h",
            subject: String(idEstudiante),
            issuer: "cana-dulce-api",
            audience: "cana-dulce-android"
        }
    );
}
function crearTokenDocente() {
    return jwt.sign(
        { rol: "DOCENTE" },
        secreto,
        {
            algorithm: "HS256",
            expiresIn: "8h",
            subject: "docente",
            issuer: "cana-dulce-api",
            audience: "cana-dulce-android"
        }
    );
}
function verificarToken(token) {
    return jwt.verify(
        token,
        secreto,
        {
            algorithms: ["HS256"],
            issuer: "cana-dulce-api",
            audience: "cana-dulce-android"
        }
    );
}
module.exports = {
    crearTokenEstudiante,
    crearTokenDocente,
    verificarToken
};