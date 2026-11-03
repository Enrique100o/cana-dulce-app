const fs = require("node:fs");
const path = require("node:path");
const mysql = require("mysql2/promise");

const valorTLS = process.env.DB_SSL || "false";

if (!["true", "false"].includes(valorTLS)) {
  throw new Error("DB_SSL debe ser true o false.");
}

if (process.env.NODE_ENV === "production" && valorTLS !== "true") {
  throw new Error("La conexión con MySQL en producción requiere TLS.");
}

const configuracion = {
  host: process.env.DB_HOST,
  port: Number(process.env.DB_PORT),
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  timezone: "Z",
  connectTimeout: 10000,
  waitForConnections: true,
  connectionLimit: 5,
  queueLimit: 20,
};

if (valorTLS === "true") {
  if (!process.env.DB_SSL_CA_PATH) {
    throw new Error("Configura DB_SSL_CA_PATH con el certificado de MySQL.");
  }

  const rutaCertificado = path.resolve(
    __dirname,
    "..",
    process.env.DB_SSL_CA_PATH,
  );

  configuracion.ssl = {
    ca: fs.readFileSync(rutaCertificado),
    rejectUnauthorized: true,
    verifyIdentity: true,
  };
}

module.exports = mysql.createPool(configuracion);
