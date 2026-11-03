const pool = require("../db");

function puntaje(correctas, total) {
  const divisor = BigInt(total);
  return Number((BigInt(correctas) * 10000n + divisor / 2n) / divisor) / 100;
}

function estrellas(valor) {
  if (valor === null) return null;
  return valor === 0 ? 0 : valor < 50 ? 1 : valor < 80 ? 2 : 3;
}

async function obtenerProgreso(estudiante, conexion = pool) {
  const [filas] = await conexion.execute(
    `SELECT idLectura, puntaje FROM INTENTO
     WHERE idEstudiante = ? AND estado = 'COMPLETADO'`,
    [estudiante.idEstudiante],
  );
  const valores = filas.map((fila) => Number(fila.puntaje));
  const lecturas = new Set(filas.map((fila) => fila.idLectura)).size;
  const centesimas = valores.reduce(
    (suma, valor) => suma + Math.round(valor * 100),
    0,
  );
  const total = filas.length;
  return {
    idEstudiante: estudiante.idEstudiante,
    alias: estudiante.alias,
    grado: estudiante.grado,
    intentosCompletados: total,
    lecturasCompletadas: lecturas,
    puntajePromedio: total
      ? Number((BigInt(centesimas) + BigInt(total) / 2n) / BigInt(total)) / 100
      : null,
    mejorPuntaje: total
      ? valores.reduce((mejor, valor) => Math.max(mejor, valor), 0)
      : null,
    puntosAcumulados: centesimas / 100,
    estrellasAcumuladas: valores.reduce(
      (suma, valor) => suma + estrellas(valor),
      0,
    ),
    insignias: [
      {
        codigo: "PRIMERA_AVENTURA",
        nombre: "Primera aventura",
        obtenida: lecturas >= 1,
      },
      {
        codigo: "EXPLORADOR_HISTORIAS",
        nombre: "Explorador de historias",
        obtenida: lecturas >= 3,
      },
      {
        codigo: "LECTURA_BRILLANTE",
        nombre: "Lectura brillante",
        obtenida: valores.includes(100),
      },
    ],
  };
}

module.exports = { puntaje, estrellas, obtenerProgreso };
