const {
    randomBytes,
    scrypt,
    timingSafeEqual
} = require("node:crypto");
const { promisify } = require("node:util");

const scryptAsync = promisify(scrypt);

const parametros = {
    N: 16384,
    r: 8,
    p: 5,
    maxmem: 32 * 1024 * 1024
};

async function crearHashCodigo(codigo) {
    const sal = randomBytes(16).toString("hex");

    const hash = await scryptAsync(
        codigo,
        sal,
        64,
        parametros
    );

    return [
        "scrypt",
        parametros.N,
        parametros.r,
        parametros.p,
        sal,
        hash.toString("hex")
    ].join("$");
}
async function verificarCodigo(codigo, hashGuardado) {
    if (
        typeof codigo !== "string" ||
        typeof hashGuardado !== "string"
    ) {
        return false;
    }

    const partes = hashGuardado.split("$");

    if (partes.length !== 6) {
        return false;
    }

    const [algoritmo, n, r, p, sal, hashHex] = partes;

    if (
        algoritmo !== "scrypt" ||
        Number(n) !== parametros.N ||
        Number(r) !== parametros.r ||
        Number(p) !== parametros.p ||
        !/^[0-9a-f]{32}$/.test(sal) ||
        !/^[0-9a-f]{128}$/.test(hashHex)
    ) {
        return false;
    }

    const hashCalculado = await scryptAsync(
        codigo,
        sal,
        64,
        parametros
    );

    const hashEsperado = Buffer.from(hashHex, "hex");

    return timingSafeEqual(hashCalculado, hashEsperado);
}
module.exports = {
    crearHashCodigo,
    verificarCodigo
};