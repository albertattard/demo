package demo.ui;

import org.owasp.encoder.Encode;

public final class DemoString {
    private String string;
    public String getString() {
        return string;
    }
    public void setString(String string) {
        this.string = string;
    }

    public String forJava() {
        return Encode.forJava(string);
    }

    public String forXml() {
        return Encode.forXml(string);
    }

    public String forJavaScript() {
        return Encode.forJavaScript(string);
    }

    public String forCDATA() {
        return Encode.forCDATA(string);
    }

    public String forCssString() {
        return Encode.forCssString(string);
    }

    public String forHtml() {
        return Encode.forHtml(string);
    }

    public String forHtmlAttribute() {
        return Encode.forHtmlAttribute(string);
    }
}
