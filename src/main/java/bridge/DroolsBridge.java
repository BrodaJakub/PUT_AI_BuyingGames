package bridge;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;

import java.lang.reflect.Method;
import java.util.Optional;

public class DroolsBridge {

    private final KieSession ksession;
    private FactHandle guiStateHandle;

    public DroolsBridge() {
        KieServices kieServices = KieServices.Factory.get();
        KieContainer container = kieServices.getKieClasspathContainer();
        this.ksession = container.newKieSession("ksession-rules");

        ksession.fireAllRules();

        Object gui = getGUIState();
        if (gui != null) {
            guiStateHandle = ksession.getFactHandle(gui);
        }
    }


    public Object getGUIState() {
        Optional<?> gui = ksession.getObjects(o ->
                o.getClass().getSimpleName().equals("GUIState")
        ).stream().findFirst();

        return gui.orElse(null);
    }


    public void answer(String value) {
        Object guiState = getGUIState();
        if (guiState == null || guiStateHandle == null) {
            return;
        }

        try {
            Method setAnswer = guiState.getClass().getMethod("setAnswer", String.class);
            setAnswer.invoke(guiState, value);

            ksession.update(guiStateHandle, guiState);

            ksession.fireAllRules();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object getRecommendation() {
        Optional<?> rec = ksession.getObjects(o ->
                o.getClass().getSimpleName().equals("Recommendation")
        ).stream().findFirst();

        return rec.orElse(null);
    }
}
