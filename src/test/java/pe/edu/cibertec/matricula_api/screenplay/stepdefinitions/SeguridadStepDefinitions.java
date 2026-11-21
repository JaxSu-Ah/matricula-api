package pe.edu.cibertec.matricula_api.screenplay.stepdefinitions;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.rest.interactions.Get;
import pe.edu.cibertec.matricula_api.screenplay.questions.ElCodigoDeError;
import pe.edu.cibertec.matricula_api.screenplay.task.CerrarSesion;
import pe.edu.cibertec.matricula_api.screenplay.task.IniciarSesion;
import pe.edu.cibertec.matricula_api.screenplay.task.RenovarToken;

import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;
import static net.serenitybdd.screenplay.rest.questions.ResponseConsequence.seeThatResponse;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class SeguridadStepDefinitions {

    @Given("inicia sesion de seguridad con usuario {string} y clave {string}")
    public void iniciaSesionDeSeguridad(String usuario, String clave) {
        Actor actor = theActorInTheSpotlight();
        actor.attemptsTo(IniciarSesion.con(usuario, clave));
        actor.should(seeThatResponse(res -> res.statusCode(200)
                .body("accessToken", not(emptyOrNullString()))
                .body("refreshToken", not(emptyOrNullString()))));
    }

    @When("renueva los tokens de su sesion")
    public void renuevaLosTokens() {
        Actor actor = theActorInTheSpotlight();
        String accessToken = actor.recall("accessToken");
        String refreshToken = actor.recall("refreshToken");
        actor.remember("accessTokenOriginal", accessToken);
        actor.remember("refreshTokenOriginal", refreshToken);
        actor.attemptsTo(RenovarToken.conElTokenGuardadoEn("refreshToken"));
    }

    @And("recibe tokens distintos a los originales")
    public void recibeTokensDistintos() {
        Actor actor = theActorInTheSpotlight();
        String accessOriginal = actor.recall("accessTokenOriginal");
        String refreshOriginal = actor.recall("refreshTokenOriginal");
        actor.should(seeThatResponse(res -> res
                .body("accessToken", not(emptyOrNullString()))
                .body("refreshToken", not(emptyOrNullString()))
                .body("accessToken", not(equalTo(accessOriginal)))
                .body("refreshToken", not(equalTo(refreshOriginal)))));
    }

    @When("reutiliza el refresh token original")
    public void reutilizaElRefreshOriginal() {
        theActorInTheSpotlight().attemptsTo(RenovarToken.conElTokenGuardadoEn("refreshTokenOriginal"));
    }

    @When("cierra su sesion")
    public void cierraSuSesion() {
        theActorInTheSpotlight().attemptsTo(CerrarSesion.actual());
    }

    @When("consulta su perfil con el access token guardado")
    public void consultaSuPerfil() {
        consultaConToken("/api/auth/perfil");
    }

    @When("solicita el listado de alumnos")
    public void solicitaElListadoDeAlumnos() {
        consultaConToken("/api/alumnos");
    }

    private void consultaConToken(String ruta) {
        Actor actor = theActorInTheSpotlight();
        String token = actor.recall("accessToken");
        actor.attemptsTo(Get.resource(ruta)
                .with(req -> req.header("Authorization", "Bearer " + token)));
    }

    @When("intenta iniciar sesion con usuario {string} usando estos datos")
    public void intentaIniciarSesion(String usuario, DataTable tabla) {
        Actor actor = theActorInTheSpotlight();
        for (Map<String, String> fila : tabla.asMaps(String.class, String.class)) {
            actor.attemptsTo(IniciarSesion.con(usuario, fila.get("clave")));
            int status = Integer.parseInt(fila.get("status"));
            actor.should(
                    seeThatResponse("respuesta del intento " + fila.get("intento"),
                            res -> res.statusCode(status)
                                    .body(matchesJsonSchemaInClasspath("schemas/error.json"))),
                    seeThat(ElCodigoDeError.deLaRespuesta(), equalTo(fila.get("codigo"))));
        }
    }
}
