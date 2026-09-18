package demo.chat.spring;

import org.springframework.ai.document.Document;

import java.util.function.Function;
import java.util.stream.Stream;

@FunctionalInterface
public interface DocumentConvertor<T> extends Function<T, Stream<Document>> {}
