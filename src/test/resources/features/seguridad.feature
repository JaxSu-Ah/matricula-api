@P2
Feature: Seguridad de la sesión

  Background:
    Given que Carla consulta la API de matricula

  Scenario: Rotación del refresh token
    Given inicia sesion de seguridad con usuario "i202610003" y clave "Cibertec2026"
    When renueva los tokens de su sesion
    Then el codigo de respuesta es 200
    And recibe tokens distintos a los originales
    And la respuesta cumple el esquema "token.json"
    When reutiliza el refresh token original
    Then el codigo de respuesta es 401
    And el codigo de error es "REFRESH_REUTILIZADO"
    And la respuesta cumple el esquema "error.json"
    When consulta su perfil con el access token guardado
    Then el codigo de respuesta es 401
    And el codigo de error es "TOKEN_INVALIDO"
    And la respuesta cumple el esquema "error.json"

  Scenario: Cierre de sesión
    Given inicia sesion de seguridad con usuario "i202610003" y clave "Cibertec2026"
    When cierra su sesion
    Then el codigo de respuesta es 204
    When consulta su perfil con el access token guardado
    Then el codigo de respuesta es 401
    And el codigo de error es "TOKEN_INVALIDO"
    And la respuesta cumple el esquema "error.json"

  Scenario: Bloqueo de cuenta por intentos fallidos
    Given que Jorge consulta la API de matricula
    When intenta iniciar sesion con usuario "i202610004" usando estos datos
      | intento | clave           | status | codigo                 |
      | 1       | ClaveIncorrecta1 | 401    | CREDENCIALES_INVALIDAS |
      | 2       | ClaveIncorrecta1 | 401    | CREDENCIALES_INVALIDAS |
      | 3       | ClaveIncorrecta1 | 401    | CREDENCIALES_INVALIDAS |
      | 4       | ClaveIncorrecta1 | 401    | CREDENCIALES_INVALIDAS |
      | 5       | ClaveIncorrecta1 | 423    | CUENTA_BLOQUEADA       |
      | 6       | Cibertec2026     | 423    | CUENTA_BLOQUEADA       |

  Scenario: Alumno sin permiso de administrador
    Given inicia sesion de seguridad con usuario "i202610003" y clave "Cibertec2026"
    When solicita el listado de alumnos
    Then el codigo de respuesta es 403
    And el codigo de error es "ACCESO_DENEGADO"
    And la respuesta cumple el esquema "error.json"
