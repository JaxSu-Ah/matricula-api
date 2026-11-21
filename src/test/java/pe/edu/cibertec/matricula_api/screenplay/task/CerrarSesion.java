package pe.edu.cibertec.matricula_api.screenplay.task;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class CerrarSesion implements Task {

    public static CerrarSesion actual() {
        return instrumented(CerrarSesion.class);
    }

    @Override
    @Step("{0} cierra su sesion")
    public <T extends Actor> void performAs(T actor) {
        String token = actor.recall("accessToken");
        actor.attemptsTo(Post.to("/api/auth/logout")
                .with(req -> req.header("Authorization", "Bearer " + token)));
    }
}
