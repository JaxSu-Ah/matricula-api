package pe.edu.cibertec.matricula_api.screenplay.stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.rest.interactions.Get;
import pe.edu.cibertec.matricula_api.screenplay.questions.ElHistorial;
import pe.edu.cibertec.matricula_api.screenplay.questions.LaRespuesta;
import pe.edu.cibertec.matricula_api.screenplay.task.CalificarMatricula;
import pe.edu.cibertec.matricula_api.screenplay.task.ConsultarHistorial;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static net.serenitybdd.screenplay.actors.OnStage.theActorCalled;
import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;
import static net.serenitybdd.screenplay.rest.questions.ResponseConsequence.seeThatResponse;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

public class CalificacionStepDefinitions {

    private Actor alumna;

    @Given("obtiene su matricula del curso {string} del periodo {string} con estado {string}")
    public void obtieneSuMatricula(String curso, String periodo, String estado) {
        alumna = theActorInTheSpotlight();
        String token = alumna.recall("accessToken");
        alumna.attemptsTo(Get.resource("/api/matriculas")
                .with(req -> req.header("Authorization", "Bearer " + token)
                        .queryParam("curso", curso)
                        .queryParam("periodo", periodo)
                        .queryParam("estado", estado)));
        alumna.should(seeThatResponse(res -> res.statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].codigoCurso", equalTo(curso))
                .body("[0].periodo", equalTo(periodo))
                .body("[0].estado", equalTo(estado))));
        long id = SerenityRest.lastResponse().jsonPath().getLong("[0].id");
        alumna.should(seeThat("el id de la matricula consultada", actor -> id, greaterThan(0L)));
        alumna.remember("idMatricula", id);
    }

    @When("registra la nota {double} en la matricula consultada")
    public void registraLaNota(double nota) {
        long id = alumna.recall("idMatricula");
        theActorInTheSpotlight().attemptsTo(CalificarMatricula.conNota(id, nota));
    }

    @When("la alumna consulta su historial con codigo {string}")
    public void consultaElHistorial(String codigo) {
        theActorCalled(alumna.getName()).attemptsTo(ConsultarHistorial.de(codigo));
    }

    @And("el historial corresponde al alumno {string}")
    public void elHistorialCorrespondeAlAlumno(String codigo) {
        theActorInTheSpotlight().should(seeThat(LaRespuesta.campo("codigoAlumno"), equalTo(codigo)));
    }

    @And("el historial del curso {string} del periodo {string} tiene nota {double} y condicion {string}")
    public void verificaElCurso(String curso, String periodo, double nota, String condicion) {
        theActorInTheSpotlight().should(
                seeThat(ElHistorial.notaDelCurso(curso, periodo), equalTo(nota)),
                seeThat(ElHistorial.condicionDelCurso(curso, periodo), equalTo(condicion)));
    }

    @And("el historial tiene {int} creditos aprobados y promedio ponderado {double}")
    public void verificaLosTotales(int creditos, double promedio) {
        theActorInTheSpotlight().should(
                seeThat(ElHistorial.creditosAprobados(), equalTo(creditos)),
                seeThat(ElHistorial.promedioPonderado(), equalTo(promedio)));
    }
}
