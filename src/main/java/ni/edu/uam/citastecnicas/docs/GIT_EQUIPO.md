# Repositorio, ramas, commits y colaboración

## 1. Crear el único repositorio

El responsable crea en GitHub **Evaluacion_JavaFX_CitasTecnicas**, sin generar otro README
ni `.gitignore` (ya vienen aquí). Configurar acceso del docente y añadir al resto como colaboradores:
Settings → Collaborators → Add people. No compartir cuentas ni contraseñas.

En una terminal ubicada en la carpeta del proyecto, reemplazar `USUARIO_RESPONSABLE`
por el propietario real y completar la identidad de quien ejecuta los comandos:

```bash
git init -b main
git config user.name "TU NOMBRE COMPLETO"
git config user.email "TU CORREO VERIFICADO EN GITHUB"
git status --short
git add .
git commit -m "chore: incorporar base JavaFX JDBC de citas para revision del equipo"
git remote add origin https://github.com/USUARIO_RESPONSABLE/Evaluacion_JavaFX_CitasTecnicas.git
git push -u origin main
```

Si ya tienen repositorio, **no ejecutar git init ni reemplazar su historial**.
Copiar los archivos a una rama del repositorio existente, revisar diferencias y conservar sus commits.
Antes del `git add .`, comprobar que `conf/db.properties` está ignorado.
El commit inicial describe la incorporación de esta base; no atribuye desarrollos previos a compañeros.

## 2. Configurar la identidad de cada persona

Cada integrante clona el mismo repositorio desde su computadora:

```bash
git clone https://github.com/USUARIO_RESPONSABLE/Evaluacion_JavaFX_CitasTecnicas.git
cd Evaluacion_JavaFX_CitasTecnicas
git config user.name "NOMBRE COMPLETO DEL INTEGRANTE"
git config user.email "CORREO VERIFICADO DEL INTEGRANTE"
git config --get user.name
git config --get user.email
```

Puede usar el correo `noreply` que **su propia cuenta** muestra en GitHub → Settings → Emails.
No inventar ese correo. Cada persona inicia sesión con su cuenta para hacer push.
La autoría del commit y la cuenta que autentica el push son cosas distintas: revisar ambas.

## 3. Ramas por responsabilidad

| Rama | Responsable |
|---|---|
| `main` | Versión integrada que debe compilar y funcionar |
| `rolando/base-jdbc` | Rolando: SQL, conexión e integración |
| `mauro/clientes` | Mauro: CRUD y pruebas de clientes |
| `aris/citas` | Aris: CRUD y pruebas de citas |
| `dylan/pruebas` | Dylan: validación, pruebas y evidencia |

Las cuatro ramas corresponden a los integrantes indicados por el equipo.
No hace falta una rama `develop` para este equipo pequeño.

Cada persona crea **su** rama, por ejemplo:

```bash
git switch main
git pull --ff-only origin main
git switch -c mauro/clientes
```

Trabajar, probar, revisar y hacer commits de cambios reales:

```bash
git diff
git status --short
git add src/test/java/ni/edu/uam/citastecnicas/PruebasValidacion.java
git commit -m "test(clientes): cubrir el caso real agregado en esta revision"
git push -u origin mauro/clientes
```

Ese mensaje y ese archivo son un ejemplo: ejecutar `git add` sobre los archivos que
realmente cambiaste y describir el resultado concreto. Si no hay cambios sustantivos,
no crear un commit vacío ni cambiar espacios para aumentar la cantidad.

## 4. Pull requests y revisión

1. GitHub → Pull requests → New pull request: base `main`, compare tu rama.
2. Usar la plantilla: problema, cambio realizado y pruebas con PostgreSQL.
3. Pedir revisión a otro compañero, que compruebe el código y el flujo.
4. Compilar y ejecutar las pruebas de validación/PostgreSQL; corregir errores antes de integrar.
5. Elegir **Create a merge commit** para mantener los commits individuales en el historial.
6. El resto actualiza su copia de `main`.

Si se permite en el repositorio, proteger `main` para exigir revisión mediante PR.
Para actualizar una rama de trabajo sin reescribir commits publicados:

```bash
git fetch origin
git switch mauro/clientes
git merge origin/main
```

Si hay conflictos, resolver los bloques del editor manteniendo el código correcto,
probar, `git add ARCHIVOS_RESUELTOS` y `git commit`. No usar `git push --force`.
Coordinar cambios en archivos compartidos (`MainController`, `Validacion`, `README`)
para que una persona los integre después de revisar cada propuesta.

## 5. Commits sustantivos: ejemplos de propósito

Estos mensajes ilustran trabajos distintos, **no son commits creados ni un historial existente**:

| Área | Ejemplo de mensaje, solo si se realizó ese cambio |
|---|---|
| Base | `feat(db): crear tablas cliente y cita con restriccion de eliminacion` |
| Conexión | `feat(jdbc): centralizar credenciales locales y prueba SELECT 1` |
| Clientes | `feat(clientes): implementar CRUD y busqueda parametrizada` |
| Citas | `feat(citas): asociar clientes y combinar filtros de estado y fecha` |
| Validación | `fix(validacion): rechazar horas fuera del formato HH:mm` |
| Pruebas | `test(jdbc): ampliar pruebas simples de duplicados y eliminacion restringida` |
| Defensa | `docs(defensa): documentar ejecucion y resultados reales de aceptacion` |

## 6. Fechas exigidas por el docente

| Fecha | Evidencia exigida |
|---|---|
| 12/10/2026 | Al menos 2 commits sustantivos: inicio, estructura, SQL, JDBC o clientes |
| 13/10/2026 | Al menos 2 commits sustantivos: citas, relación, búsqueda o filtros |
| 14/10/2026 | Al menos 2 commits sustantivos: validación, pruebas o documentación técnica |
| 15/10/2026 | Entrega y defensa; vale lo que exista al momento de la revisión final |

Además, **cada integrante** necesita al menos un commit sustantivo propio del 12 al 14.
No modificar `GIT_AUTHOR_DATE`, `GIT_COMMITTER_DATE` ni la identidad para fabricar evidencia.
Al recibir esta base el 14 de octubre, conservar lo que ya se haya trabajado los días anteriores.
Si esos avances no existen, indicarlo al docente; no se reconstruyen con fechas falsas.

## 7. Mostrar autores y evidencias

```bash
git log --all --date=iso-local --format="%h | %ad | %an <%ae> | %s"
git shortlog -sne --all
git log --all --since="2026-10-12T00:00:00-06:00" --until="2026-10-14T23:59:59-06:00" --stat
git show HASH_DEL_COMMIT --stat
```

Las cantidades no bastan: mostrar el diff de commits representativos y explicar la funcionalidad.
En GitHub revisar Commits, autores, ramas y PR. El ZIP no contiene una carpeta `.git`;
el historial auténtico se genera y conserva en el repositorio del equipo.

## 8. Lista final

- Nombre exacto del repositorio y acceso del docente.
- README con nombres completos, cuentas reales y responsabilidades de quienes participaron.
- SQL, fuentes, FXML, CSS, configuración de ejemplo y pruebas incluidos.
- Credenciales reales excluidas y ausencia de `target/` e `.idea/`.
- Cambios de cada persona respaldados por commits propios, revisables.
- Pruebas manuales hechas en PostgreSQL; capturas reales enlazadas al README.
- Todos pueden abrir el proyecto y explicar su bloque y el flujo JDBC.
