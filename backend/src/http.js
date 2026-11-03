function fallar(estadoHTTP, mensaje, datos = {}) {
  const error = new Error(mensaje);
  error.estadoHTTP = estadoHTTP;
  error.datos = datos;
  throw error;
}

function objeto(valor, campos) {
  if (!valor || typeof valor !== "object" || Array.isArray(valor)) {
    fallar(400, "Envía un objeto con los datos solicitados.");
  }
  if (Object.keys(valor).some((campo) => !campos.includes(campo))) {
    fallar(400, "Hay campos no permitidos.");
  }
  return valor;
}

function entero(valor, nombre = "El identificador") {
  if (!Number.isSafeInteger(valor) || valor < 1 || valor > 4294967295) {
    fallar(400, `${nombre} debe ser un entero positivo válido.`);
  }
  return valor;
}

function identificador(valor, nombre) {
  if (typeof valor !== "string" || !/^[1-9]\d*$/.test(valor)) {
    fallar(400, "El identificador debe ser un entero positivo.");
  }
  return entero(Number(valor), nombre);
}

function sinCuerpo(req) {
  if (
    req.body !== undefined ||
    Number(req.get("Content-Length") || 0) > 0 ||
    req.get("Transfer-Encoding")
  ) {
    fallar(400, "Esta consulta no recibe un cuerpo.");
  }
}

function filtros(req, permitidos = []) {
  objeto(req.query, permitidos);
  if (Object.values(req.query).some((valor) => typeof valor !== "string")) {
    fallar(400, "Cada filtro debe tener un solo valor.");
  }
  return req.query;
}

function ruta(operacion) {
  return async (req, res, next) => {
    try {
      await operacion(req, res);
    } catch (error) {
      next(error);
    }
  };
}

module.exports = {
  fallar,
  objeto,
  entero,
  identificador,
  sinCuerpo,
  filtros,
  ruta,
};
