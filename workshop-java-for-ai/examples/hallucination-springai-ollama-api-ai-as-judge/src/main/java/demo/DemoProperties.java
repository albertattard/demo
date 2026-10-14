package demo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@ConfigurationProperties(prefix = "demo.ai")
class DemoProperties {

    private final ModelProperties chat = new ModelProperties();
    private final ModelProperties judge = new ModelProperties();

    ModelProperties getChat() {
        return chat;
    }

    ModelProperties getJudge() {
        return judge;
    }

    static final class ModelProperties {
        private String model;
        private double temperature;

        String getModel() {
            return model;
        }

        void setModel(final String model) {
            this.model = model;
        }

        double getTemperature() {
            return temperature;
        }

        void setTemperature(final double temperature) {
            this.temperature = temperature;
        }

        @Override
        public boolean equals(final Object object) {
            return object instanceof final ModelProperties other
                   && Double.compare(temperature, other.temperature) == 0
                   && Objects.equals(model, other.model);
        }

        @Override
        public int hashCode() {
            return Objects.hash(model, temperature);
        }

        @Override
        public String toString() {
            return "ModelProperties[" +
                   "model=" + model +
                   ", temperature=" + temperature +
                   ']';
        }
    }
}
