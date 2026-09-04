# DIY Store demo — build plan

## Purpose

Build one small web application that evolves through six workshop
checkpoints. Each checkpoint answers a limitation exposed by the previous one:

1. A conventional Spring Web and Thymeleaf application establishes a DIY-store
   product-search journey backed by H2, without AI.
2. Chat translates a customer’s natural-language DIY request into a structured
   recommendation brief.
3. A photo upload lets a vision-capable model infer one or more DIY tasks; the
   customer selects one and reuses the chat recommendation journey.
4. An LLM-as-a-judge check reviews the proposed DIY-task choices against
   explicit, narrow safety criteria before they are shown to the customer.
5. RAG grounds those recommendations in a small, local product catalogue.
6. Local tool calling lets the model decide when to retrieve catalogue products.

The application demonstrates the sequence explored in the workshop
presentation: text intent, visual intent, response review, grounding, then
orchestration. It deliberately does not cover audio, persistence, or a
production-ready web interface.

## Agreed technical choices

- Java 25, Spring Boot, and Spring AI.
- OpenAI for vision-capable chat and embeddings, configured through the
  existing local OpenAI configuration import; never store an API key in the
  repository.
- `SimpleVectorStore` as the in-memory vector store.
- Spring AI `@Tool` methods for local tool calling. The application owns both
  the vector-store retrieval and the operational fixture data; no MCP server,
  Docker daemon, network port, or external service is required.
- A small server-rendered web application using Spring Web and Thymeleaf. It is
  the stable user journey through every checkpoint; no SPA or frontend framework
  is required.
- Do not introduce HTMX at the starting point. A normal form submission makes
  the initial prompt flow easy to inspect. Add HTMX only if a later checkpoint
  benefits from progressively revealing retrieval or tool-call evidence.

## Fixture convention

Fixtures keep the live build short without concealing the lesson.

```text
fixtures/
  01-chat/        # Text-based structured-chat source files
  02-image/       # Image upload and visual task-inference source files
  03-judge/       # LLM-as-a-judge guard and response-review prompt
  04-rag/         # Catalogue data and RAG-specific source files
  05-tools/       # Local tool-calling source files and operational fixture data
```

Checkpoint 0 is the base application, not a fixture. From checkpoint 1 onwards,
the runbook copies only named files from the current fixture. It must never
replace the whole source tree: attendees should be able to see precisely what
changed and retain their earlier work.

## Checkpoint 0 — conventional web application

Create the Spring Web and Thymeleaf application before introducing an AI
dependency. Provide one page with a small H2 catalogue and a traditional
product-name or keyword search. Define the controller, view model, validation
behaviour, and templates that later checkpoints will retain.

This checkpoint must not pretend to provide recommendations. It establishes the
ordinary application boundary: a customer searches a local catalogue, and
a later checkpoint can enhance that journey with a recommendation service.

**Evidence to show**

- The application starts locally without an OpenAI API key or any AI-specific
  dependency.
- The browser renders product-search results and its validation feedback.
- A focused MVC test demonstrates the page contract.

**Acceptance criteria**

- `./mvnw clean verify` succeeds without external services or credentials.
- The runbook documents how to start the application and open the local page.
- The Checkpoint 0 application has no class, property, dependency, prompt, or
  template that refers to OpenAI, image analysis, RAG, or tool calling. Later fixtures
  may of course introduce those concepts.

## Checkpoint 1 — structured chat

**Customer question:** “I would like to paint my living room.”

Integrate the existing form with a `ChatClient`-backed recommendation service.
The system message tells the model to return a Java record describing the DIY
task and the product categories needed, rather than prose or invented products.
The application uses those categories to perform conventional keyword searches
against the H2 catalogue and renders only the resulting store products.

**Evidence to show**

- The prompt and system message are visible in the source.
- The page renders a structured recommendation brief and catalogue-backed products.
- The model response contains generic needs, such as paint, brushes, rollers, and
  preparation materials, but no catalogue product ID.

**Limitation to explain**

The application accepts only a text description. A customer who can show the
problem but cannot name the task still has to translate the image into words
before the application can help.

It also matches category words literally against names and keywords. It does
not understand semantic similarity, rank results by relevance, or resolve
meaningful wording differences between a customer request and the catalogue.

**Acceptance criteria**

- `./mvnw clean verify` succeeds without an OpenAI key.
- Running the application with a valid OpenAI key renders a structured result and
  matching H2 catalogue products.
- The runbook documents API-key setup, paid API usage, model configuration, and
  non-deterministic output.

## Checkpoint 2 — photo upload and visual task inference

Add a multipart photo-upload form to the existing page. Send the uploaded image
as media in a `ChatClient` user message, alongside a visible system prompt that
asks the model to identify supported DIY tasks from the image and return a Java
record rather than prose. The response must be an application-owned list of
task choices; it must not name catalogue products or product IDs.

The application validates the upload before making a model request: accept only
documented image media types, apply an explicit size limit, and reject an empty
or unsupported file with page-level feedback. Do not persist uploaded photos;
keep the bytes only for the duration of the request. The runbook must name the
vision-capable chat model required by this checkpoint.

**Evidence to show**

- The page accepts a supported image and displays the inferred DIY-task choices.
- The source visibly attaches the image and its media type to the user message.
- A photo with no useful DIY context produces a safe, supported=false result or
  clear validation feedback rather than an invented task.

**Limitation to explain**

The model infers plausible tasks from visual evidence; it does not know which
products the store stocks. A photo can be ambiguous, incomplete, or unrelated
to DIY, so the customer must select a task before passing it to the structured
chat recommendation journey introduced in Checkpoint 1.

**Acceptance criteria**

- `./mvnw clean verify` succeeds without an OpenAI key; MVC tests cover upload
  validation and rendering a mocked task-inference result.
- Running the application with a valid OpenAI key and a vision-capable model
  renders one or more inferred task choices for a suitable local image.
- Selecting an inferred task invokes the existing structured-chat recommendation
  flow and renders only catalogue-backed products.
- The runbook documents supported image types, the size limit, API-key setup,
  paid vision requests, model configuration, non-deterministic output, and the
  fact that uploads are not persisted.

## Checkpoint 3 — LLM as a Judge for DIY-task choices

The vision model’s task choices are still customer-facing model output. There
is a small chance that a choice is inappropriate for this teaching application.
Before rendering it, add a second, independent `ChatClient` call that judges the
proposed task choices. The customer sees the original choices only when the
judge returns an application-owned `PASSED` decision; otherwise the application
shows a fixed, safe fallback and does not expose the rejected choices or the
judge’s reasoning.

The judge prompt must evaluate a narrow, visible policy suitable for the demo:
the choices must describe supported DIY tasks, must not contain abusive, sexual,
discriminatory, or self-harm content, and must not include catalogue products or
product IDs. It must return a Java record containing only a decision and a short
reason for application logs. The application, not the judge, owns the display
text and fallback behaviour.

This is an output guardrail, not proof that task choices are safe, correct,
lawful, or suitable for every audience. Input validation and the vision prompt
remain necessary, and the customer must still select a task before it enters the
structured-chat recommendation journey.

**Evidence to show**

- The source shows the inferred task choices, judge prompt, structured decision,
  and the branch that either renders the choices or the fallback.
- A mocked `PASSED` decision renders the original choices unchanged.
- A mocked rejection renders the application-owned fallback and never renders
  the rejected choices or a model-generated explanation.

**Acceptance criteria**

- Unit or MVC tests cover both decisions and assert that rejected task text is
  absent from the rendered page.
- `./mvnw clean verify` succeeds without an OpenAI key by mocking both model
  calls.
- With a valid OpenAI key, the runbook labels the additional paid judge request,
  names the judge model and prompt, and demonstrates both the normal path and a
  deterministic mocked-rejection path.
- The runbook states that LLM-as-a-judge output is non-deterministic and can
  make false-positive and false-negative decisions; it is not a substitute for
  policy enforcement, human review, or domain-specific safeguards.

## Checkpoint 4 — RAG with an in-memory catalogue

Add a small fixture catalogue containing enough distinct DIY products to make
semantic retrieval visible. Each item has a stable internal ID, title,
description, and the attributes necessary to distinguish similar products.

On startup, convert the catalogue entries to `Document` instances and add them
to `SimpleVectorStore`. Use the selected task and its structured recommendation
categories to form a visible retrieval query, retrieve a small set of relevant
documents, and supply them as context to the chat model. The final response
must recommend only products returned by retrieval and identify them by
internal ID.

**Evidence to show**

- The page and application output show the retrieval query and selected
  documents before the model’s final recommendation.
- A natural-language request can retrieve relevant items even when it does not
  use the catalogue’s exact title.
- The recommendation cites only retrieved product IDs.

**Limitation to explain**

This is a teaching store, not a production vector database. Its contents are
rebuilt on every start, it has no durable index, and retrieval quality depends
on the descriptions, embedding model, query, top-K value, and threshold.

**Acceptance criteria**

- Tests verify catalogue-to-document conversion and the response guard that
  rejects non-retrieved product IDs.
- The runbook clearly distinguishes an embedding request from the chat request
  and calls out the associated API cost.

## Checkpoint 5 — local tool calling for catalogue retrieval

Create `fixtures/05-tools/` with local Spring AI `@Tool` methods. This is a
tool-calling checkpoint, not an MCP checkpoint: the methods run inside the DIY
Store application and the application owns the vector-store retrieval.

Expose one focused tool:

- `search_catalogue(query)` searches the `SimpleVectorStore` and returns a
  small, structured list of catalogue products with their internal IDs.

**Customer question:** “What do I need to paint my living room?”

When the `demo.tools.enabled` system property is `true`, this checkpoint replaces
the default RAG recommendation flow. The expected flow is: the model receives the customer’s request without a
preselected catalogue context, decides to call `search_catalogue`, and selects
the relevant product ID or IDs from that result. The application executes the requested
method and rejects IDs that do not match its own catalogue retrieval.

**Evidence to show**

- Logs or application output show each requested tool name, arguments, and
  returned data.
- The initial model request contains no retrieved products; retrieval appears
  only as the result of `search_catalogue`.
- The existing product-results view renders only products whose IDs pass
  application-owned catalogue retrieval.

**Limitation to explain**

This demonstrates local tool calling, not MCP. The tool methods share the
application process, deployment boundary, and failure domain with the web
application. A later MCP-focused example would need a separate MCP server and
client to demonstrate protocol interoperability.

**Acceptance criteria**

- Unit tests cover the catalogue tool, including a catalogue
  search that returns products for semantically related wording.
- A mocked tool-calling interaction proves that the application does not pass
  retrieved products to the model before it requests `search_catalogue`, and
  that the final response rejects an ID outside application-owned catalogue retrieval.
- With a valid OpenAI key and `-Ddemo.tools.enabled=true`, the application
  completes the recommendation using the local tools and logs the requested calls.
- The runbook documents that tool invocations are model-selected and
  non-deterministic, as well as the paid chat and embedding requests.

## Runbook delivery rule

After each checkpoint is implemented, update `sw-runbook.yaml` to document only
the working state, then run `sw validate`, `sw check`, and the narrowest
relevant Maven verification. Generate `README.md` with `sw run` only after the
documented commands are correct. Do not document future checkpoints as runnable
steps.
