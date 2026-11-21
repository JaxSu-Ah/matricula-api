package pe.edu.cibertec.matricula_api.screenplay.stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import net.serenitybdd.model.environment.EnvironmentSpecificConfiguration;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.model.environment.SystemEnvironmentVariables;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyOrNullString;

public class MatriculaStepDefinitions {

    private final String baseUrl = EnvironmentSpecificConfiguration
            .from(SystemEnvironmentVariables.currentEnvironmentVariables())
            .getProperty("restapi.baseurl");

    private String token;
    private double totalCotizado;
    private long idMatricula;
    private Map<String, Object> pagoEncontrado;

    @And("inicia sesion con usuario {string} y clave {string}")
    public void iniciaSesion(String usuario, String clave) {
        token = SerenityRest.given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .body(Map.of("username", usuario, "password", clave))
                .post("/api/auth/login")
                .then().statusCode(200)
                .extract().path("accessToken");
        assertThat(token, not(emptyOrNullString()));
    }

    @When("cotiza el curso {string} para el alumno {string}")
    public void cotizaElCurso(String curso, String alumno) {
        SerenityRest.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("codigoAlumno", alumno, "cursos", List.of(curso)))
                .post("/api/matriculas/cotizacion");
    }

    @And("el total de la cotizacion es {double}")
    public void elTotalDeLaCotizacionEs(double total) {
        totalCotizado = SerenityRest.lastResponse().jsonPath().getDouble("total");
        assertThat(totalCotizado, equalTo(total));
    }

    @When("se matricula con el codigo {string} en el curso {string}")
    public void seMatriculaConElCodigo(String alumno, String curso) {
        SerenityRest.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("codigoAlumno", alumno, "codigoCurso", curso))
                .post("/api/matriculas");
    }

    @And("el estado de la matricula es {string}")
    public void elEstadoDeLaMatriculaEs(String estado) {
        assertThat(SerenityRest.lastResponse().jsonPath().getString("estado"), equalTo(estado));
    }

    @And("el monto de la matricula coincide con la cotizacion")
    public void elMontoCoincideConLaCotizacion() {
        assertThat(SerenityRest.lastResponse().jsonPath().getDouble("monto"), equalTo(totalCotizado));
    }

    @And("guarda el id de la matricula creada para {string} en {string}")
    public void guardaElIdDeLaMatricula(String alumno, String curso) {
        assertThat(SerenityRest.lastResponse().jsonPath().getString("codigoAlumno"), equalTo(alumno));
        assertThat(SerenityRest.lastResponse().jsonPath().getString("codigoCurso"), equalTo(curso));
        idMatricula = SerenityRest.lastResponse().jsonPath().getLong("id");
        assertThat(idMatricula, greaterThan(0L));
    }

    @When("paga la matricula creada con el metodo {string}")
    public void pagaLaMatriculaCreada(String metodo) {
        SerenityRest.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .pathParam("id", idMatricula)
                .body(Map.of("metodo", metodo))
                .patch("/api/matriculas/{id}/pagar");
    }

    @And("la fecha de pago tiene valor")
    public void laFechaDePagoTieneValor() {
        assertThat(SerenityRest.lastResponse().jsonPath().getLong("id"), equalTo(idMatricula));
        assertThat(SerenityRest.lastResponse().jsonPath().getString("fechaPago"), not(emptyOrNullString()));
    }

    @When("consulta sus pagos")
    public void consultaSusPagos() {
        SerenityRest.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + token)
                .get("/api/pagos");
    }

    @And("existe el pago de la matricula creada con metodo {string} y monto {double}")
    public void existeElPagoDeLaMatricula(String metodo, double monto) {
        List<Map<String, Object>> pagos = SerenityRest.lastResponse().jsonPath().getList("$");
        List<Map<String, Object>> coincidencias = pagos.stream()
                .filter(pago -> ((Number) pago.get("idMatricula")).longValue() == idMatricula)
                .toList();
        assertThat("Debe existir un pago para la matricula creada", coincidencias.size(), equalTo(1));
        pagoEncontrado = coincidencias.get(0);
        assertThat(pagoEncontrado.get("metodo"), equalTo(metodo));
        assertThat(((Number) pagoEncontrado.get("monto")).doubleValue(), equalTo(monto));
    }

    @And("el numero de operacion del pago cumple el formato {string}")
    public void elNumeroDeOperacionCumpleElFormato(String formato) {
        String numeroOperacion = (String) pagoEncontrado.get("numeroOperacion");
        assertThat(numeroOperacion, not(emptyOrNullString()));
        assertThat("Formato del numero de operacion: " + numeroOperacion,
                numeroOperacion.matches(formato), equalTo(true));
    }

    @And("consulta la matricula pagada de {string} en {string} del periodo {string}")
    public void consultaLaMatriculaPagada(String alumno, String curso, String periodo) {
        List<Map<String, Object>> matriculas = SerenityRest.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + token)
                .queryParam("alumno", alumno)
                .queryParam("curso", curso)
                .queryParam("estado", "PAGADA")
                .queryParam("periodo", periodo)
                .get("/api/matriculas")
                .then().statusCode(200)
                .extract().jsonPath().getList("$");

        assertThat("Ejecuta primero el escenario Matricularse y pagar", matriculas.size(), equalTo(1));
        Map<String, Object> matricula = matriculas.get(0);
        assertThat(matricula.get("codigoAlumno"), equalTo(alumno));
        assertThat(matricula.get("codigoCurso"), equalTo(curso));
        assertThat(matricula.get("periodo"), equalTo(periodo));
        assertThat(matricula.get("estado"), equalTo("PAGADA"));
        idMatricula = ((Number) matricula.get("id")).longValue();
        assertThat(idMatricula, greaterThan(0L));
    }

    @When("anula la matricula pagada")
    public void anulaLaMatriculaPagada() {
        SerenityRest.given()
                .baseUri(baseUrl)
                .header("Authorization", "Bearer " + token)
                .pathParam("id", idMatricula)
                .patch("/api/matriculas/{id}/anular");
    }
}
