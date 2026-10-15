# Trabajo de Rolando, Mauro, Aris y Dylan

Todos deben comprender el flujo general:
**FXML → controlador → validación → DAO → Connection → SQL con parámetros → resultado → TableView**.
Cada persona trabaja con su cuenta, en su rama, y realiza cambios reales que puede explicar.

| Persona / rama | Archivos a cargo | Qué debe hacer y probar | Qué defiende |
|---|---|---|---|
| Rolando Mayorga — `rolando/base-jdbc` | gestion_citas.sql, ConexionDB.java, pom.xml, MainApp.java, Launcher.java, MainController.java | Crear base real; configurar conexión sin subir clave; probar conexión correcta/incorrecta; revisar SQL; integrar cambios del equipo | SERIAL, FK, Connection, Statement fijo, configuración, Thread y runLater |
| Mauro Delgado — `mauro/clientes` | Cliente.java, ClienteDAO.java, ClienteController.java, cliente-view.fxml | Probar alta, selección, editar teléfono y nombres, mismo correo al editar, correo de otro cliente, búsqueda, limpiar, rechazo de eliminación y eliminación válida | INSERT/UPDATE/DELETE/SELECT, parámetros, ResultSet, TableView y correo repetido |
| Aris Gavarrete — `aris/citas` | CitaTecnica.java, CitaTecnicaDAO.java, CitaController.java, cita-view.fxml | Probar alta, selección y edición de todos los campos, cambio de cliente, JOIN, filtros separados/combinados, limpiar, Finalizada, eliminación | ComboBox, relación 1–N, fecha/hora, JOIN y filtros con parámetros |
| Dylan Mora — `dylan/pruebas` | Validacion.java, Alertas.java, src/test/java, README, docs y capturas | Ejecutar pruebas de validación y PostgreSQL; registrar defectos y resultados; ampliar casos útiles; revisar alertas/confirmación; preparar evidencias y documentación | Reglas, SQLException, integridad, persistencia y resultados de pruebas |

## Orden de trabajo

1. Rolando crea el único repositorio, invita colaboradores y confirma SQL/conexión en la base real.
2. Mauro y Aris revisan y prueban sus módulos en ramas separadas; corrigen problemas encontrados.
3. Dylan ejecuta aceptación de ambos módulos, registra resultados y coordina correcciones.
4. Mauro revisa el PR de Aris; Aris revisa el de Mauro. Rolando y Dylan revisan sus PR entre sí.
5. Rolando integra los PR en main; los cuatro actualizan sus copias y repiten la defensa juntos.

## Qué se considera aportación

La asignación no demuestra participación por sí sola. Cada integrante debe realizar
cambios sustantivos en código, SQL, pruebas o documentación técnica y hacer commits propios.
El commit inicial puede incorporar esta base; no atribuye el desarrollo previo a compañeros.

Ejemplos de aportaciones útiles sobre esta versión:

- Rolando: documentar y corregir un caso real de configuración/conexión, respaldado por prueba.
- Mauro: ampliar pruebas de búsqueda con apóstrofos, signos %/_ y modificación de correo duplicado.
- Aris: ampliar pruebas de los cuatro estados y cambios de cliente, fecha y hora; corregir fallos reales.
- Dylan: ejecutar y documentar aceptación reproducible, añadir nuevos casos límite y capturas reales.

No ejecutar un commit con un mensaje que afirme un cambio que no se hizo. Coordinar
cambios en PruebasPostgreSQL, PruebasValidacion y README para no sobrescribir trabajo ajeno.
