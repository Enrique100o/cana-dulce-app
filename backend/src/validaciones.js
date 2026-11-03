function validarRegistroEstudiante(datos) {
    if (!datos || typeof datos !== "object" || Array.isArray(datos)) {
        return { error: "Envía los datos del estudiante." };
    }

    if (typeof datos.alias !== "string") {
        return { error: "El alias debe ser un texto." };
    }

    const alias = datos.alias.trim().normalize("NFC");
    const longitudAlias = [...alias].length;

    if (longitudAlias < 3 || longitudAlias > 30) {
        return { error: "El alias debe tener entre 3 y 30 caracteres." };
    }

    if (datos.grado !== 3 && datos.grado !== 4) {
        return { error: "Selecciona tercero o cuarto." };
    }

    if (typeof datos.codigoAcceso !== "string") {
        return { error: "El código de acceso debe ser un texto." };
    }

    const longitudCodigo = [...datos.codigoAcceso].length;

    if (longitudCodigo < 6 || longitudCodigo > 32) {
        return {
            error: "El código de acceso debe tener entre 6 y 32 caracteres."
        };
    }

    return {
        datos: {
            alias,
            aliasNormalizado: alias.toLocaleLowerCase("es").normalize("NFC"),
            grado: datos.grado,
            codigoAcceso: datos.codigoAcceso
        }
    };
}
function validarInicioSesionEstudiante(datos) {
    if (!datos || typeof datos !== "object" || Array.isArray(datos)) {
        return { error: "Envía el alias y el código de acceso." };
    }

    if (typeof datos.alias !== "string") {
        return { error: "El alias debe ser un texto." };
    }

    const alias = datos.alias.trim().normalize("NFC");
    const longitudAlias = [...alias].length;

    if (longitudAlias < 3 || longitudAlias > 30) {
        return { error: "El alias debe tener entre 3 y 30 caracteres." };
    }

    if (typeof datos.codigoAcceso !== "string") {
        return { error: "El código de acceso debe ser un texto." };
    }

    const longitudCodigo = [...datos.codigoAcceso].length;

    if (longitudCodigo < 6 || longitudCodigo > 32) {
        return {
            error: "El código de acceso debe tener entre 6 y 32 caracteres."
        };
    }

    return {
        datos: {
            aliasNormalizado: alias.toLocaleLowerCase("es").normalize("NFC"),
            codigoAcceso: datos.codigoAcceso
        }
    };
}
function validarInicioSesionDocente(datos) {
    if (!datos || typeof datos !== "object" || Array.isArray(datos)) {
        return { error: "Envía el usuario y la contraseña." };
    }

    if (typeof datos.usuario !== "string") {
        return { error: "El usuario debe ser un texto." };
    }

    const usuario = datos.usuario.trim().normalize("NFC");
    const longitudUsuario = [...usuario].length;

    if (longitudUsuario < 1 || longitudUsuario > 50) {
        return {
            error: "El usuario debe tener entre 1 y 50 caracteres."
        };
    }

    if (typeof datos.contrasena !== "string") {
        return { error: "La contraseña debe ser un texto." };
    }

    const longitudContrasena = [...datos.contrasena].length;

    if (longitudContrasena < 1 || longitudContrasena > 128) {
        return {
            error: "La contraseña debe tener entre 1 y 128 caracteres."
        };
    }

    return {
        datos: {
            usuario,
            contrasena: datos.contrasena
        }
    };
}
function validarTexto(valor, nombreCampo, minimo, maximo) {
    if (typeof valor !== "string") {
        return {
            error: `${nombreCampo} debe ser un texto.`
        };
    }

    const texto = valor.trim().normalize("NFC");
    const longitud = [...texto].length;

    if (longitud < minimo || longitud > maximo) {
        return {
            error: `${nombreCampo} debe tener entre ${minimo} y ${maximo} caracteres.`
        };
    }

    return {
        datos: texto
    };
}
function validarCreacionLectura(datos) {
    if (!datos || typeof datos !== "object" || Array.isArray(datos)) {
        return { error: "Envía los datos de la lectura." };
    }

    const camposPermitidos = [
        "titulo",
        "contenido",
        "grado",
        "dificultad",
        "categoria"
    ];

    const tieneCamposNoPermitidos = Object.keys(datos).some(
        campo => !camposPermitidos.includes(campo)
    );

    if (tieneCamposNoPermitidos) {
        return {
            error: "Hay campos no permitidos para crear una lectura."
        };
    }

    const titulo = validarTexto(datos.titulo, "El título", 3, 120);

    if (titulo.error) {
        return titulo;
    }

    const contenido = validarTexto(
        datos.contenido,
        "El contenido",
        20,
        8000
    );

    if (contenido.error) {
        return contenido;
    }

    if (datos.grado !== 3 && datos.grado !== 4) {
        return { error: "Selecciona tercero o cuarto." };
    }

    if (!["BAJA", "MEDIA", "ALTA"].includes(datos.dificultad)) {
        return {
            error: "Selecciona una dificultad: BAJA, MEDIA o ALTA."
        };
    }

    let categoria = null;

    if (datos.categoria !== undefined && datos.categoria !== null) {
        const resultadoCategoria = validarTexto(
            datos.categoria,
            "La categoría",
            0,
            50
        );

        if (resultadoCategoria.error) {
            return resultadoCategoria;
        }

        categoria = resultadoCategoria.datos || null;
    }

    return {
        datos: {
            titulo: titulo.datos,
            contenido: contenido.datos,
            grado: datos.grado,
            dificultad: datos.dificultad,
            categoria,
            estado: "INACTIVA"
        }
    };
}
function validarConsultaLecturas(parametros) {
    if (
        !parametros ||
        typeof parametros !== "object" ||
        Array.isArray(parametros)
    ) {
        return { error: "Los filtros deben ser un objeto." };
    }

    const camposPermitidos = ["grado", "q", "estado"];

    const tieneCamposNoPermitidos = Object.keys(parametros).some(
        campo => !camposPermitidos.includes(campo)
    );

    if (tieneCamposNoPermitidos) {
        return { error: "Hay filtros no permitidos." };
    }

    const filtros = {};

    if (parametros.grado !== undefined) {
        if (!["3", "4"].includes(parametros.grado)) {
            return { error: "El filtro grado debe ser 3 o 4." };
        }

        filtros.grado = Number(parametros.grado);
    }

    if (parametros.estado !== undefined) {
        if (!["ACTIVA", "INACTIVA"].includes(parametros.estado)) {
            return {
                error: "El estado debe ser ACTIVA o INACTIVA."
            };
        }

        filtros.estado = parametros.estado;
    }

    if (parametros.q !== undefined) {
        const busqueda = validarTexto(
            parametros.q,
            "La búsqueda",
            1,
            100
        );

        if (busqueda.error) {
            return busqueda;
        }

        filtros.q = busqueda.datos
            .toLocaleLowerCase("es")
            .normalize("NFC");
    }

    return {
        datos: filtros
    };
}
function validarIdentificador(valor, nombreCampo = "El identificador") {
    if (typeof valor !== "string" || !/^[1-9]\d*$/.test(valor)) {
        return {
            error: `${nombreCampo} debe ser un entero positivo.`
        };
    }

    const identificador = Number(valor);

    if (!Number.isSafeInteger(identificador)) {
        return {
            error: `${nombreCampo} está fuera del rango permitido.`
        };
    }

    return {
        datos: identificador
    };
}
function validarOpcion(datos) {
    if (!datos || typeof datos !== "object" || Array.isArray(datos)) {
        return { error: "Cada opción debe ser un objeto." };
    }

    const camposPermitidos = ["texto", "orden", "esCorrecta"];

    const tieneCamposNoPermitidos = Object.keys(datos).some(
        campo => !camposPermitidos.includes(campo)
    );

    if (tieneCamposNoPermitidos) {
        return { error: "Hay campos no permitidos en una opción." };
    }

    const texto = validarTexto(
        datos.texto,
        "El texto de la opción",
        1,
        250
    );

    if (texto.error) {
        return texto;
    }

    if (
        !Number.isInteger(datos.orden) ||
        datos.orden < 1 ||
        datos.orden > 4
    ) {
        return {
            error: "El orden de la opción debe ser un entero entre 1 y 4."
        };
    }

    if (typeof datos.esCorrecta !== "boolean") {
        return {
            error: "Indica esCorrecta como true o false."
        };
    }

    return {
        datos: {
            texto: texto.datos,
            orden: datos.orden,
            esCorrecta: datos.esCorrecta
        }
    };
}
function validarOpciones(opciones) {
    if (
        !Array.isArray(opciones) ||
        opciones.length < 2 ||
        opciones.length > 4
    ) {
        return {
            error: "La pregunta debe tener entre 2 y 4 opciones."
        };
    }

    const opcionesValidadas = [];
    const ordenes = new Set();
    const textos = new Set();

    for (const opcion of opciones) {
        const resultado = validarOpcion(opcion);

        if (resultado.error) {
            return resultado;
        }

        const opcionValidada = resultado.datos;

        const textoNormalizado = opcionValidada.texto
            .toLocaleLowerCase("es")
            .normalize("NFC");

        if (ordenes.has(opcionValidada.orden)) {
            return {
                error: "Las opciones no pueden repetir el orden."
            };
        }

        if (textos.has(textoNormalizado)) {
            return {
                error: "Las opciones no pueden tener textos repetidos."
            };
        }

        ordenes.add(opcionValidada.orden);
        textos.add(textoNormalizado);
        opcionesValidadas.push(opcionValidada);
    }

    const cantidadCorrectas = opcionesValidadas.filter(
        opcion => opcion.esCorrecta
    ).length;

    if (cantidadCorrectas !== 1) {
        return {
            error: "La pregunta debe tener exactamente una opción correcta."
        };
    }

    return {
        datos: opcionesValidadas
    };
}
function validarCreacionPregunta(datos) {
    if (!datos || typeof datos !== "object" || Array.isArray(datos)) {
        return {
            error: "Envía los datos de la pregunta."
        };
    }

    const camposPermitidos = [
        "enunciado",
        "tipo",
        "orden",
        "opciones"
    ];

    const tieneCamposNoPermitidos = Object.keys(datos).some(
        campo => !camposPermitidos.includes(campo)
    );

    if (tieneCamposNoPermitidos) {
        return {
            error: "Hay campos no permitidos para crear una pregunta."
        };
    }

    const enunciado = validarTexto(
        datos.enunciado,
        "El enunciado",
        5,
        400
    );

    if (enunciado.error) {
        return enunciado;
    }

    const tiposPermitidos = [
        "LITERAL",
        "INFERENCIAL",
        "IDEA_PRINCIPAL",
        "VOCABULARIO",
        "SECUENCIA"
    ];

    if (!tiposPermitidos.includes(datos.tipo)) {
        return {
            error: "Selecciona un tipo de pregunta válido."
        };
    }

    if (
        !Number.isInteger(datos.orden) ||
        datos.orden < 1 ||
        datos.orden > 999
    ) {
        return {
            error: "El orden de la pregunta debe ser un entero entre 1 y 999."
        };
    }

    const opciones = validarOpciones(datos.opciones);

    if (opciones.error) {
        return opciones;
    }

    return {
        datos: {
            enunciado: enunciado.datos,
            tipo: datos.tipo,
            orden: datos.orden,
            estado: "ACTIVA",
            opciones: opciones.datos
        }
    };
}
function validarActualizacionLectura(datos) {
    if (!datos || typeof datos !== "object" || Array.isArray(datos)) {
        return {
            error: "Envía los datos de la lectura."
        };
    }

    const camposPermitidos = [
        "titulo",
        "contenido",
        "grado",
        "dificultad",
        "categoria",
        "estado"
    ];

    const tieneCamposNoPermitidos = Object.keys(datos).some(
        campo => !camposPermitidos.includes(campo)
    );

    if (tieneCamposNoPermitidos) {
        return {
            error: "Hay campos no permitidos para actualizar una lectura."
        };
    }

    const faltanCampos = camposPermitidos.some(
        campo => (
            !Object.hasOwn(datos, campo) ||
            datos[campo] === undefined
        )
    );

    if (faltanCampos) {
        return {
            error: "Envía todos los campos de la lectura."
        };
    }

    if (!["ACTIVA", "INACTIVA"].includes(datos.estado)) {
        return {
            error: "El estado debe ser ACTIVA o INACTIVA."
        };
    }

    const validacion = validarCreacionLectura({
        titulo: datos.titulo,
        contenido: datos.contenido,
        grado: datos.grado,
        dificultad: datos.dificultad,
        categoria: datos.categoria
    });

    if (validacion.error) {
        return validacion;
    }

    return {
        datos: {
            ...validacion.datos,
            estado: datos.estado
        }
    };
}
module.exports = {
    validarTexto,
    validarRegistroEstudiante,
    validarInicioSesionEstudiante,
    validarInicioSesionDocente,
    validarCreacionLectura,
    validarConsultaLecturas,
    validarIdentificador,
    validarOpcion,
    validarOpciones,
    validarCreacionPregunta,
    validarActualizacionLectura
};