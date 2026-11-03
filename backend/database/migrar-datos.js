const fs = require("node:fs");
const mysql = require("mysql2/promise");

function abrirConexion(prefijo, usarTLS = false) {
  const configuracion = {
    host: process.env[`${prefijo}_HOST`],
    port: Number(process.env[`${prefijo}_PORT`]),
    user: process.env[`${prefijo}_USER`],
    password: process.env[`${prefijo}_PASSWORD`],
    database: process.env[`${prefijo}_NAME`],
    connectTimeout: 10000,
    dateStrings: true,
  };

  if (usarTLS) {
    configuracion.ssl = {
      ca: fs.readFileSync(process.env.CLOUD_DB_CA_PATH),
      rejectUnauthorized: true,
      verifyIdentity: true,
    };
  }

  return mysql.createConnection(configuracion);
}
const tablas = [
  {
    nombre: "ESTUDIANTE",
    columnas: [
      "idEstudiante",
      "alias",
      "aliasNormalizado",
      "grado",
      "estado",
      "codigoAccesoHash",
    ],
  },
  {
    nombre: "LECTURA",
    columnas: [
      "idLectura",
      "titulo",
      "contenido",
      "grado",
      "dificultad",
      "categoria",
      "estado",
    ],
  },
  {
    nombre: "PREGUNTA",
    columnas: [
      "idPregunta",
      "idLectura",
      "enunciado",
      "tipo",
      "orden",
      "estado",
    ],
  },
  {
    nombre: "OPCION",
    columnas: [
      "idOpcion",
      "idPregunta",
      "texto",
      "orden",
      "esCorrecta",
    ],
  },
  {
    nombre: "INTENTO",
    columnas: [
      "idIntento",
      "idEstudiante",
      "idLectura",
      "fechaInicio",
      "fechaFinalizacion",
      "cantidadCorrectas",
      "totalPreguntas",
      "puntaje",
      "estado",
    ],
  },
  {
    nombre: "RESPUESTA",
    columnas: [
      "idRespuesta",
      "idIntento",
      "idPregunta",
      "idOpcion",
      "esCorrecta",
    ],
  },
];
async function copiarTabla(origen, destino, tabla) {
  const columnas = tabla.columnas.join(", ");
  const marcadores = tabla.columnas.map(() => "?").join(", ");

  const consulta =
    `SELECT ${columnas} FROM ${tabla.nombre} ` +
    `ORDER BY ${tabla.columnas[0]}`;

  const [filas] = await origen.query(consulta);

  const insercion =
    `INSERT INTO ${tabla.nombre} (${columnas}) ` +
    `VALUES (${marcadores})`;

  for (const fila of filas) {
    const valores = tabla.columnas.map(
      (columna) => fila[columna]
    );

    await destino.execute(insercion, valores);
  }

  const [copiadas] = await destino.query(consulta);

  if (JSON.stringify(copiadas) !== JSON.stringify(filas)) {
    throw new Error(
      `La copia de ${tabla.nombre} no coincide con el origen.`
    );
  }

  return filas.length;
}
async function migrarDatos() {
  let origen;
  let destino;
  let transaccionActiva = false;

  try {
    origen = await abrirConexion("DB");
    destino = await abrirConexion("CLOUD_DB", true);

    await origen.query(
      "SET TRANSACTION ISOLATION LEVEL REPEATABLE READ"
    );

    await origen.query(
      "START TRANSACTION WITH CONSISTENT SNAPSHOT, READ ONLY"
    );

    await destino.query(
      "SET TRANSACTION ISOLATION LEVEL REPEATABLE READ"
    );

    await destino.beginTransaction();
    transaccionActiva = true;

    for (const tabla of tablas) {
      const [filas] = await destino.query(
        `SELECT ${tabla.columnas[0]} FROM ${tabla.nombre} ` +
        "LIMIT 1 FOR UPDATE"
      );

      if (filas.length > 0) {
        throw new Error(
          `La tabla ${tabla.nombre} de Aiven ya contiene datos.`
        );
      }
    }

    const resumen = [];

    for (const tabla of tablas) {
      const cantidad = await copiarTabla(
        origen,
        destino,
        tabla
      );

      resumen.push({
        tabla: tabla.nombre,
        registros: cantidad,
      });
    }

    await origen.commit();
    await destino.commit();
    transaccionActiva = false;

    console.table(resumen);
    console.log("Copia completada.");
  } catch (error) {
    if (destino && transaccionActiva) {
      try {
        await destino.rollback();
      } catch (errorReversion) {
        console.error(
          "No se pudo confirmar la reversión:",
          errorReversion.code || errorReversion.name
        );
      }
    }

    throw error;
  } finally {
    await Promise.allSettled(
      [origen, destino]
        .filter(Boolean)
        .map((conexion) => conexion.end())
    );
  }
}

migrarDatos().catch((error) => {
  console.error(
    "Error durante la copia:",
    error.code || error.message
  );

  process.exitCode = 1;
});