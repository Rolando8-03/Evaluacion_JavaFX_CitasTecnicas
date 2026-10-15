# Revisión completa contra la guía del docente

Se revisaron el texto y las cinco tablas del documento de evaluación.
**Implementado** significa que existe en código; no significa que ya se haya demostrado
con el PostgreSQL del equipo ni que las evidencias de GitHub estén completas.

## Indicadores y modelo

| Punto | Implementación / evidencia necesaria |
|---|---|
| IL2.1 | ConexionDB centralizada; comprobar() usa Statement fijo SELECT 1 |
| IL2.2 | Dos DAO con CRUD y valores parametrizados |
| IL2.3 | Validación en formulario y DAO; UNIQUE, NOT NULL, CHECK y FK en SQL |
| Base gestion_citas | Nombre por defecto y script con instrucciones separadas de creación/conexión |
| cliente | id SERIAL PK; nombres/apellidos VARCHAR(80); telefono VARCHAR(20); correo VARCHAR(120) UNIQUE; activo BOOLEAN; todos los datos obligatorios NOT NULL |
| cita_tecnica | id SERIAL PK; cliente_id FK NOT NULL; fecha DATE; hora TIME; servicio VARCHAR(80); descripcion VARCHAR(255); estado VARCHAR(20); todos NOT NULL |
| 1 cliente → N citas | FK de cita_tecnica.cliente_id a cliente.id |
| Estados | Pendiente, En proceso, Finalizada, Cancelada; ComboBox y CHECK |
| Eliminar físicamente | DELETE en ambas entidades; activo no sustituye DELETE |
| Cliente con citas | ON DELETE RESTRICT; alerta específica explica eliminar primero sus citas |

## RF01–RF10

| Código | Revisión del código | Comprobación pendiente en base real |
|---|---|---|
| RF01 | ConexionDB, mensajes de autenticación/conexión; botón reintentar | Conectar al PostgreSQL del equipo |
| RF02 | Formulario/TableView/DAO: crear, listar, seleccionar, modificar y eliminar cliente | Ejecutar ambos casos de eliminación |
| RF03 | CRUD cita con cliente existente en ComboBox; todos los campos editables | Registrar, editar y borrar citas |
| RF04 | TableView cita: ID, nombre completo por JOIN, fecha, hora, servicio, estado | Verificar fila guardada |
| RF05 | ILIKE parametrizado para nombres, apellidos y correo | Buscar apellido parcial variando mayúsculas |
| RF06 | Estado, fecha, ambos o ninguno; limpieza de dos controles | Ejecutar combinaciones y limpiar |
| RF07 | Campos, correo/formato/duplicado, fecha/hora, cliente, estados | Probar duplicados y FK real |
| RF08 | Confirmación, éxito, error y restricciones; Cancelar predeterminado | Cancelar y confirmar cada eliminación |
| RF09 | Recarga de tablas y ComboBox después de INSERT/UPDATE/DELETE | Observar actualización sin reiniciar |
| RF10 | DAO guarda en PostgreSQL; listas son solo visualización | Cerrar/reabrir y comparar cambios |

## Requisitos técnicos

| Requisito | Estado del código |
|---|---|
| JavaFX y controladores FXML | Tres FXML y tres controladores; contenedores AnchorPane |
| Cliente y CitaTecnica | Dos modelos con constructores y getters |
| DAO separado | ClienteDAO y CitaTecnicaDAO |
| Connection / PreparedStatement / ResultSet | Usados y cerrados en DAO |
| Statement al menos una vez | SELECT 1 fijo en comprobar(); sin entradas |
| Parámetros en altas, cambios, bajas y filtros | Sí; nombres/búsquedas/IDs/fechas/estados son valores de ? |
| Sin concatenar entradas SQL | Solo se concatenan fragmentos constantes de la consulta |
| try-with-resources | Conexiones, sentencias, resultados y lectura de configuración |
| SQLException comprensible | Alertas según código, sin exponer contraseña |
| Credenciales fuera de commits | Archivo local ignorado; ejemplo vacío; instrucciones en README |
| PostgreSQL real, sin ORM | Solo JDBC y driver pgJDBC; no hay Hibernate/JPA ni persistencia simulada |
| Ejecutar en IntelliJ | Maven con Java 21; Launcher; pasos y carpeta de ejecución documentados |

## Técnicas y mejoras dentro del nivel recuperado

Se retiraron Task, ExecutorService, Callable, Consumer, record, streams, JUnit,
reflexión de pruebas, switch con flechas, patrones de instanceof y SQL procedural.
Se usan if/else, ciclos, clases, arreglos/listas, propiedades simples JavaFX,
Thread/Runnable/runLater y las clases JDBC que el docente solicita.

Mejoras adicionales: normalización de correo, búsqueda literal de %/_; nombres/teléfono
validados; límites VARCHAR; invisibles y caracteres de control; IDs y filas desaparecidas;
no repetir operaciones mientras una está en curso; mensajes específicos; cancelar por defecto.
No se impusieron fechas futuras, exclusión de clientes inactivos ni prohibición de citas
coincidentes porque la guía no define esas reglas.

## Organización, cronograma y entrega

| Condición del profesor | Estado |
|---|---|
| Equipo de 1–4, nunca 5 | Cuatro integrantes registrados: Rolando, Mauro, Aris, Dylan |
| Repositorio único con nombre exacto | Guía lista; falta enlace real y verificar acceso del docente |
| Acceso del docente desde 12/10 | Solo verificable en el repositorio real; no lo acredita el ZIP |
| Cada persona con cuenta/commits propios | Rolando8-03 confirmado; faltan usuarios de Mauro, Aris y Dylan y commits reales |
| Nombres completos y reparto en README | Nombres suministrados/recuperados y reparto incluidos; usuarios de tres compañeros pendientes |
| Aporte funcional sustantivo de todos | Debe acreditarse con cambios/PR/commits reales de cada uno |
| Dos commits sustantivos por día 12, 13 y 14 | Solo verificable con su historial; no se inventan fechas anteriores |
| Un commit por integrante entre 12–14 | Solo verificable con autores/diffs de GitHub |
| Final evaluable al revisar el docente | Trabajar/publicar antes de esa revisión; no basta un ZIP local |
| Fuentes/FXML/SQL/README | Incluidos y compilados localmente con Java 21 |
| Capturas de funcionamiento | Guía incluida; capturas entregadas son de interfaz sin conectar; faltan capturas de CRUD real |
| Defensa individual y general | Guion y archivos por persona incluidos; todos deben practicar ejecución/explicación |

## Pruebas obligatorias

Todas están detalladas en DEFENSA_Y_CAPTURAS.md: registrar cliente, correo repetido,
modificar teléfono, cita con nombre, buscar apellido, filtrar/limpiar, Finalizada,
rechazo de cliente con citas, eliminar cita y cliente con confirmación, reabrir y mostrar
historial de tres fechas. La prueba de PostgreSQL incluida verifica también varios de
esos flujos, pero la ventana y el historial se demuestran manualmente.

## Dictamen

La implementación cubre los requisitos funcionales y técnicos revisados y añade
validaciones útiles. **No se puede certificar cumplimiento íntegro de la evaluación**
hasta ejecutar la aceptación con PostgreSQL real, completar las cuentas/evidencias y
comprobar el historial auténtico del equipo. El código compilado y la interfaz probada
no permiten asignar por sí solos 30/30 ni confirmar avances en días anteriores.
