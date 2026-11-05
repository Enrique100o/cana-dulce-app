const pool = require("../src/db");
const biblioteca = require("./misiones-espaciales.json");
const { validarCreacionLectura, validarCreacionPregunta } = require("../src/validaciones");

function validarBiblioteca() {
  return biblioteca.map(({ preguntas, ...lectura }) => {
    const ficha = validarCreacionLectura(lectura);
    const cuestionario = preguntas.map(validarCreacionPregunta);
    const error = ficha.error || cuestionario.find((pregunta) => pregunta.error)?.error;
    if (error) throw new Error(error);
    return { lectura: ficha.datos, preguntas: cuestionario.map((pregunta) => pregunta.datos) };
  });
}

async function importar() {
  const misiones = validarBiblioteca();
  const conexion = await pool.getConnection();
  const bloqueo = "cana_dulce_biblioteca_espacial";
  try {
    const [[{ adquirido }]] = await conexion.query("SELECT GET_LOCK(?, 10) AS adquirido", [bloqueo]);
    if (adquirido !== 1) throw new Error("La biblioteca se está cargando en otra sesión.");
    await conexion.beginTransaction();
    const creadas = [];
    const existentes = [];
    for (const { lectura, preguntas } of misiones) {
      const [previas] = await conexion.execute(
        "SELECT idLectura FROM LECTURA WHERE titulo = ? AND grado = ?",
        [lectura.titulo, lectura.grado],
      );
      if (previas.length > 0) {
        existentes.push(lectura.titulo);
        continue;
      }
      const [alta] = await conexion.execute(
        `INSERT INTO LECTURA (titulo, contenido, grado, dificultad, categoria, estado)
         VALUES (?, ?, ?, ?, ?, 'INACTIVA')`,
        [lectura.titulo, lectura.contenido, lectura.grado, lectura.dificultad, lectura.categoria],
      );
      for (const pregunta of preguntas) {
        const [fila] = await conexion.execute(
          `INSERT INTO PREGUNTA (idLectura, enunciado, tipo, orden, estado)
           VALUES (?, ?, ?, ?, 'ACTIVA')`,
          [alta.insertId, pregunta.enunciado, pregunta.tipo, pregunta.orden],
        );
        for (const opcion of pregunta.opciones) {
          await conexion.execute(
            "INSERT INTO OPCION (idPregunta, texto, orden, esCorrecta) VALUES (?, ?, ?, ?)",
            [fila.insertId, opcion.texto, opcion.orden, Number(opcion.esCorrecta)],
          );
        }
      }
      await conexion.execute("UPDATE LECTURA SET estado = 'ACTIVA' WHERE idLectura = ?", [alta.insertId]);
      creadas.push({ titulo: lectura.titulo, grado: lectura.grado, id: alta.insertId });
    }
    await conexion.commit();
    console.log(JSON.stringify({ creadas, existentes }));
  } catch (error) {
    await conexion.rollback();
    throw error;
  } finally {
    try {
      await conexion.query("SELECT RELEASE_LOCK(?)", [bloqueo]);
    } finally {
      conexion.release();
    }
  }
}

importar()
  .catch((error) => {
    console.error(error.code || error.message);
    process.exitCode = 1;
  })
  .finally(() => pool.end());
