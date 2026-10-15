# Resultado de la revisión y pruebas

Preparación/revisión: 14/10/2026.

| Comprobación | Resultado |
|---|---|
| Compilación de los 12 fuentes principales y dos programas de prueba, JDK 21/release 21 | Correcta |
| Programa PruebasValidacion ejecutado | 102 comprobaciones correctas |
| Carga real de los tres FXML e inyección de controladores | Correcta |
| Verificador local de formularios y validación | 34 comprobaciones correctas |
| Selección/limpieza de cliente y cita; controles de filtros | Correctos en prueba de interfaz |
| Inspección visual de ambas pantallas 1180 × 800 | Correcta; capturas sin conexión |
| XML y referencias locales de documentación | Revisados |
| Escaneo de técnicas avanzadas retiradas | Sin Task/ExecutorService/Callable/Consumer/record/streams/JUnit/reflexión en el proyecto |
| Credenciales y contenido del ZIP | Sin conf/db.properties, claves locales, target ni historial Git atribuido |
| Maven con descarga de dependencias | No ejecutado aquí; compilación local directa con javac |
| PruebasPostgreSQL con servidor real | Pendiente: no hay servidor disponible en este entorno |
| Pruebas manuales de cierre/reapertura y CRUD | Pendientes en IntelliJ/PostgreSQL del equipo |
| Historial/autoría del 12, 13 y 14 y acceso docente | Pendiente de verificar en el repositorio real |

La prueba local de controles usa objetos temporales para comprobar selección y limpieza.
Es una prueba del formulario, no persistencia simulada de la aplicación. Las capturas se
hicieron después de limpiar las tablas, con **Conexión pendiente**.

## Cómo completar la verificación

1. `mvn clean test-compile` con Java 21: compila fuentes y programas de prueba.
2. Ejecutar PruebasValidacion.main() en IntelliJ.
3. Crear gestion_citas_pruebas y aplicar en ella el bloque de tablas.
4. Ejecutar PruebasPostgreSQL.main() con las variables indicadas en README.
5. Configurar gestion_citas y ejecutar Launcher; completar todas las pruebas manuales
   de DEFENSA_Y_CAPTURAS.md, incluidos cancelar/confirmar y cerrar/reabrir.
6. Registrar resultados/capturas y comprobar el historial auténtico de los cuatro.

REVISION_REQUISITOS.md contiene el cruce completo con indicadores, modelo, RF01–RF10,
requisitos técnicos, cronograma y evidencias. No se afirma cumplimiento integral ni 30/30
sin demostrar la ejecución con PostgreSQL y las evidencias académicas pendientes.
