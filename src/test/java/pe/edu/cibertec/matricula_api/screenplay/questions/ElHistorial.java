package pe.edu.cibertec.matricula_api.screenplay.questions;

import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Question;

import java.util.List;
import java.util.Map;

public class ElHistorial {

    public static Question<Integer> creditosAprobados() {
        return Question.about("los creditos aprobados del historial")
                .answeredBy(actor -> SerenityRest.lastResponse().jsonPath().getInt("creditosAprobados"));
    }

    public static Question<Double> promedioPonderado() {
        return Question.about("el promedio ponderado del historial")
                .answeredBy(actor -> SerenityRest.lastResponse().jsonPath().getDouble("promedioPonderado"));
    }

    public static Question<Double> notaDelCurso(String curso, String periodo) {
        return Question.about("la nota de " + curso + " en " + periodo)
                .answeredBy(actor -> ((Number) curso(curso, periodo).get("nota")).doubleValue());
    }

    public static Question<String> condicionDelCurso(String curso, String periodo) {
        return Question.about("la condicion de " + curso + " en " + periodo)
                .answeredBy(actor -> (String) curso(curso, periodo).get("condicion"));
    }

    private static Map<String, Object> curso(String codigo, String periodo) {
        List<Map<String, Object>> cursos = SerenityRest.lastResponse().jsonPath().getList("cursos");
        List<Map<String, Object>> coincidencias = cursos.stream()
                .filter(item -> codigo.equals(item.get("codigoCurso")) && periodo.equals(item.get("periodo")))
                .toList();
        if (coincidencias.size() != 1) {
            throw new AssertionError("Se esperaba un curso " + codigo + " del periodo " + periodo
                    + " en el historial, se encontraron " + coincidencias.size());
        }
        return coincidencias.get(0);
    }
}
