@P1
Feature: Matrícula del alumno
  Como alumno de CIBERTEC
  Quiero matricularme en mis cursos
  Para llevarlos en el ciclo

  Background:
    Given que Carla consulta la API de matricula
    And inicia sesion con usuario "i202610003" y clave "Cibertec2026"

  @cotizacion
  Scenario: Cotizar el curso
    When cotiza el curso "PI5620" para el alumno "i202610003"
    Then el codigo de respuesta es 200
    And el total de la cotizacion es 720.0
    And la respuesta cumple el esquema "cotizacion.json"

  @matricula_pago
  Scenario: Matricularse y pagar
    When cotiza el curso "PI5620" para el alumno "i202610003"
    Then el codigo de respuesta es 200
    And el total de la cotizacion es 720.0
    When se matricula con el codigo "i202610003" en el curso "PI5620"
    Then el codigo de respuesta es 201
    And el estado de la matricula es "REGISTRADA"
    And el monto de la matricula coincide con la cotizacion
    And la respuesta cumple el esquema "matricula.json"
    And guarda el id de la matricula creada para "i202610003" en "PI5620"
    When paga la matricula creada con el metodo "TARJETA"
    Then el codigo de respuesta es 200
    And el estado de la matricula es "PAGADA"
    And la fecha de pago tiene valor
    When consulta sus pagos
    Then el codigo de respuesta es 200
    And existe el pago de la matricula creada con metodo "TARJETA" y monto 720.0
    And el numero de operacion del pago cumple el formato "^OP-[0-9]{6}$"

  # Ejecutar despues de Matricularse y pagar, sin reiniciar la API entre escenarios.
  @regla_negocio
  Scenario: Regla de negocio que debe fallar
    Given consulta la matricula pagada de "i202610003" en "PI5620" del periodo "2026-2"
    When anula la matricula pagada
    Then el codigo de respuesta es 409
    And el codigo de error es "MATRICULA_PAGADA"
    And la respuesta cumple el esquema "error.json"
