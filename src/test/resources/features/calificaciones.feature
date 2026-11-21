@P3
Feature: Calificación de matrículas

  Background:
    Given que Rosa consulta la API de matricula
    And inicia sesion de seguridad con usuario "i202610005" y clave "Cibertec2026"

  Scenario Outline: El docente registra la nota y el historial se actualiza
    Given obtiene su matricula del curso "PI5620" del periodo "2026-2" con estado "PAGADA"
    And que Docente consulta la API de matricula
    And inicia sesion de seguridad con usuario "D0001" y clave "Cibertec2026"
    When registra la nota <nota> en la matricula consultada
    Then el codigo de respuesta es 200
    When la alumna consulta su historial con codigo "i202610005"
    Then el codigo de respuesta es 200
    And la respuesta cumple el esquema "historial.json"
    And el historial corresponde al alumno "i202610005"
    And el historial del curso "PI5620" del periodo "2026-2" tiene nota <nota> y condicion "<condicion>"
    And el historial tiene <creditos> creditos aprobados y promedio ponderado <promedio>

    Examples:
      | nota | condicion   | creditos | promedio |
      | 15.0 | APROBADO    | 13       | 15.23    |
      | 12.5 | DESAPROBADO | 7        | 14.08    |

  Scenario: Un docente que no dicta el curso intenta registrar la nota
    Given obtiene su matricula del curso "PI5620" del periodo "2026-2" con estado "PAGADA"
    And que Docente consulta la API de matricula
    And inicia sesion de seguridad con usuario "D0002" y clave "Cibertec2026"
    When registra la nota 15.0 en la matricula consultada
    Then el codigo de respuesta es 403
    And el codigo de error es "ACCESO_DENEGADO"
    And la respuesta cumple el esquema "error.json"

  Scenario: No se puede calificar una matrícula que no está pagada
    Given obtiene su matricula del curso "PS5511" del periodo "2026-2" con estado "REGISTRADA"
    And que Docente consulta la API de matricula
    And inicia sesion de seguridad con usuario "D0001" y clave "Cibertec2026"
    When registra la nota 15.0 en la matricula consultada
    Then el codigo de respuesta es 409
    And el codigo de error es "MATRICULA_NO_PAGADA"
    And la respuesta cumple el esquema "error.json"
