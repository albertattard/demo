module demo.linkverifier {
    requires java.net.http;
    requires org.jsoup;
    requires com.google.common;
    requires info.picocli;

    opens demo to info.picocli;
}
