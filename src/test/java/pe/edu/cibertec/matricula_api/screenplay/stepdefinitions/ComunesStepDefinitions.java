package pe.edu.cibertec.matricula_api.screenplay.stepdefinitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import net.serenitybdd.model.environment.EnvironmentSpecificConfiguration;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import net.thucydides.model.environment.SystemEnvironmentVariables;
import pe.edu.cibertec.matricula_api.screenplay.questions.LaRespuesta;
import pe.edu.cibertec.matricula_api.screenplay.questions.ElCodigoDeError;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static net.serenitybdd.screenplay.actors.OnStage.theActorCalled;
import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;
import static org.hamcrest.Matchers.equalTo;

public class ComunesStepDefinitions {

    private final String baseUrl = EnvironmentSpecificConfiguration
            .from(SystemEnvironmentVariables.currentEnvironmentVariables())
            .getProperty("restapi.baseurl");

    @Given("que {word} consulta la API de matricula")
    public void consultaLaAPI(String nombre) {
        theActorCalled(nombre).whoCan(CallAnApi.at(baseUrl));
    }


    @Then("el codigo de respuesta es {int}")
    public void elCodigoDeRespuestaEsInt(int status) {
        theActorInTheSpotlight().should(seeThat(LaRespuesta.codigoDeEstado(), equalTo(status)));
    }

    @And("el codigo de error es {string}")
    public void elCodigoDeErrorEsString(String codigo) {
        theActorInTheSpotlight().should(seeThat(ElCodigoDeError.deLaRespuesta(), equalTo(codigo)));
    }


}
