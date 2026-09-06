package demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

@Configuration
class ModelConfiguration {

    @Bean
    RestClientCustomizer logbookCustomizer(final LogbookClientHttpRequestInterceptor interceptor) {
        return restClient -> restClient.requestInterceptor(interceptor);
    }

    @Bean
    ChatClient chatClient(final @Qualifier("chatModel") ChatModel model) {
        return ChatClient.create(model);
    }

    @Bean
    ChatClient judgeClient(final @Qualifier("judgeModel") ChatModel model) {
        return ChatClient.create(model);
    }

    @Bean
    ChatModel chatModel(final DemoProperties properties, final OllamaApi ollamaApi) {
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .options(OllamaChatOptions.builder()
                        .model(properties.getChat().getModel())
                        .temperature(properties.getChat().getTemperature())
                        .build())
                .build();
    }

    @Bean
    ChatModel judgeModel(final DemoProperties properties, final OllamaApi ollamaApi) {
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .options(OllamaChatOptions.builder()
                        .model(properties.getJudge().getModel())
                        .temperature(properties.getJudge().getTemperature())
                        .build())
                .build();
    }
}
