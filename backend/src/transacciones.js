const pool = require("./db");

async function ejecutarTransaccion(operacion) {
  const conexion = await pool.getConnection();
  let reutilizarConexion = true;

  try {
    await conexion.query("SET TRANSACTION ISOLATION LEVEL READ COMMITTED");

    await conexion.beginTransaction();

    const resultado = await operacion(conexion);

    await conexion.commit();

    return resultado;
  } catch (error) {
    try {
      await conexion.rollback();
    } catch (errorReversion) {
      reutilizarConexion = false;

      console.error(
        "No se pudo revertir la transacción:",
        errorReversion.code || errorReversion.name,
      );
    }

    throw error;
  } finally {
    if (reutilizarConexion) {
      conexion.release();
    } else {
      conexion.destroy();
    }
  }
}

module.exports = {
  ejecutarTransaccion,
};
