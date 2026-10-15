# Pruebas obligatorias y defensa

Ejecutar con PostgreSQL real antes de presentar. Las capturas complementan la ejecución;
no reemplazan una funcionalidad que no se pueda demostrar.

Usar datos de prueba propios. Ejemplo: Ana López, 8888-8888, ana.defensa@example.com.
Fecha de ejemplo: 15/10/2026, hora 09:30, servicio Mantenimiento, descripción Limpieza de equipo.
Si ese correo ya existe, usar otro y mantenerlo consistente en las pruebas.

| Paso | Acción exacta | Resultado esperado | Archivo de captura sugerido |
|---|---|---|---|
| 1 | En Clientes, completar datos y Registrar cliente | Alerta de éxito y fila nueva en TableView | `01-cliente-creado.png` |
| 2 | Nuevo / limpiar, registrar otra persona con el mismo correo | Alerta de correo ya registrado; no inserta otra fila | `02-correo-duplicado.png` |
| 3 | Seleccionar cliente, cambiar teléfono y Guardar cambios | Tabla muestra el nuevo teléfono | `03-cliente-editado.png` |
| 4 | En Citas, elegir cliente existente, fecha, hora, servicio, descripción y Pendiente; Registrar cita | Fila con nombre completo, fecha, hora, servicio y estado | `04-cita-creada.png` |
| 5 | En Clientes buscar `lÓp` | Devuelve López sin distinguir mayúsculas | `05-busqueda.png` |
| 6 | En Citas elegir Pendiente y fecha registrada; Aplicar filtros | Muestra la cita que coincide con ambas condiciones | `06-filtros.png` |
| 7 | Limpiar filtros | Estado Todos, fecha vacía y todas las citas otra vez | `07-filtros-limpios.png` |
| 8 | Seleccionar cita, cambiar estado a Finalizada, Guardar cambios | Muestra Finalizada; al filtrar Pendiente deja de aparecer | `08-finalizada.png` |
| 9 | Seleccionar su cliente, Eliminar y confirmar | Rechazo por citas asociadas; conserva cliente y cita | `09-cliente-protegido.png` |
| 10 | Cerrar y volver a ejecutar con la misma base | Mantiene teléfono modificado y cita Finalizada | `10-persistencia.png` |
| 11 | Eliminar cita y confirmar; luego eliminar cliente y confirmar | Ambas filas desaparecen; eliminación física | `11-eliminacion.png` |
| 12 | Cerrar y abrir nuevamente | Los registros eliminados siguen ausentes | `12-persistencia-eliminacion.png` |
| 13 | Mostrar GitHub Commits / log y cambios del 12, 13 y 14 | Autores reales y al menos dos commits sustantivos por fecha | `13-historial.png` |

Guardar capturas en `docs/capturas/`, añadir enlaces al README y hacer commit propio
de la documentación con resultados. No mostrar la contraseña ni el archivo local de credenciales.
Si cambian los datos de prueba, escribir los datos utilizados en el registro de resultados.

## Comprobación en pgAdmin

Conectarse a gestion_citas y ejecutar:

```sql
SELECT current_database();
SELECT id, nombres, apellidos, telefono, correo, activo FROM public.cliente ORDER BY id;
SELECT ct.id, cl.nombres || ' ' || cl.apellidos AS cliente,
       ct.fecha, ct.hora, ct.tipo_servicio, ct.estado
FROM public.cita_tecnica ct
JOIN public.cliente cl ON cl.id = ct.cliente_id
ORDER BY ct.id;
```

## Qué explica cada persona

- Base/JDBC: creación de la base, SERIAL, FK, configuración local, SELECT 1 y cierre de recursos.
- Clientes: validación → parámetros → INSERT/UPDATE/DELETE → búsqueda ILIKE → TableView.
- Citas: cliente existente → FK → JOIN para nombre → filtros opcionales → cambio de estado.
- Pruebas/calidad: duplicado, fecha/hora, error SQL, eliminación restringida, persistencia y resultados de las pruebas.

## Guion corto de demostración

1. Abrir el proyecto Maven en IntelliJ con JDK 21 y ejecutar Launcher.
2. Mostrar configuración sin exponer la contraseña y comprobar PostgreSQL conectado.
3. Registrar y editar cliente; demostrar duplicado y búsqueda.
4. Registrar cita, aplicar/limpiar filtros y finalizarla.
5. Intentar borrar cliente; mostrar rechazo. Reabrir y comprobar persistencia.
6. Eliminar cita y luego cliente con confirmación.
7. Mostrar DAO, Statement fijo, PreparedStatement, ResultSet y try-with-resources.
8. Mostrar repositorio, responsabilidades, commits reales y resultados de pruebas.

## Registro real de resultados (completar al ejecutar)

| Fecha y autor | Paso | Resultado observado | Commit / captura | Incidencia y solución |
|---|---|---|---|---|
| PENDIENTE | PENDIENTE | PENDIENTE | PENDIENTE | PENDIENTE |
