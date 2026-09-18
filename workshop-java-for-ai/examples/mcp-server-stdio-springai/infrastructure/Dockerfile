FROM container-registry.oracle.com/os/oraclelinux:9

ARG TARGETARCH

# Install Java 25 (needed to run the application that provides the tools)
RUN case "${TARGETARCH:-$(uname -m)}" in \
        amd64|x86_64) jdk_rpm='https://download.oracle.com/java/25/latest/jdk-25_linux-x64_bin.rpm' ;; \
        arm64|aarch64) jdk_rpm='https://download.oracle.com/java/25/latest/jdk-25_linux-aarch64_bin.rpm' ;; \
        *) echo "Unsupported architecture: ${TARGETARCH:-$(uname -m)}" >&2; exit 1 ;; \
    esac \
    && curl --fail --location --silent --show-error --output '/tmp/jdk-25.rpm' "${jdk_rpm}" \
    && dnf install -y '/tmp/jdk-25.rpm' \
    && rm -f '/tmp/jdk-25.rpm'

# Install Node 24 to run the inspector (required by @modelcontextprotocol/inspector@0.17.2)
RUN curl --fail --location --silent --show-error 'https://rpm.nodesource.com/setup_24.x' | bash - \
    && dnf install -y nodejs

WORKDIR /opt/mcp
COPY ./target/mcp-server-stdio-springai-1.0.0.jar mcp-server-stdio-springai-1.0.0.jar

# The environment variable is needed so that we can connect to the MCP server from the host machine
# Let inspector bind on all interfaces and allow browser origins
ENV HOST=0.0.0.0
ENV ALLOWED_ORIGINS=http://localhost:6274,http://127.0.0.1:6274

# Set default port values as environment variables
ENV CLIENT_PORT=6274
ENV SERVER_PORT=6277

# Document which ports the application uses internally
EXPOSE ${CLIENT_PORT} ${SERVER_PORT}

CMD ["npx", "--yes", "@modelcontextprotocol/inspector@0.17.2"]
