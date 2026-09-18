package demo.chat;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParsedResponseTest {

    @Test
    void parseResponseWithEmptySources() {
        /* Given */
        final String response = """
                I'm a ticketing application assistant, and turning on lights doesn't seem to be related to tickets or events. Can you please clarify what you're trying to achieve?
                
                ---SOURCES_JSON---
                {}
                ---END_SOURCES_JSON---""";

        /* When */
        final ParsedResponse parsed = ParsedResponse.parse(response);

        /* Then */
        assertThat(parsed)
                .isEqualTo(new ParsedResponse("I'm a ticketing application assistant, and turning on lights doesn't seem to be related to tickets or events. Can you please clarify what you're trying to achieve?", List.of()));
    }

    @Test
    void parseMultiLineResponseWithEmptySources() {
        /* Given */
        final String response = """
                You can reset your password through one of three methods:
                
                1. Reset Using Your Company Phone (Preferred)
                   - Open the Company Authenticator app on your company-issued phone.
                   - Select Password Reset from the menu.
                   - Follow the on-screen instructions to verify your identity.
                   - Enter your new password and confirm.
                
                ---SOURCES_JSON---
                {}
                ---END_SOURCES_JSON---""";

        /* When */
        final ParsedResponse parsed = ParsedResponse.parse(response);

        /* Then */
        assertThat(parsed)
                .isEqualTo(new ParsedResponse("""
                        You can reset your password through one of three methods:
                        
                        1. Reset Using Your Company Phone (Preferred)
                           - Open the Company Authenticator app on your company-issued phone.
                           - Select Password Reset from the menu.
                           - Follow the on-screen instructions to verify your identity.
                           - Enter your new password and confirm.""", List.of()));
    }

    @Test
    void parseResponseWithSourcesAndOnlyFootnotes() {
        /* Given */
        final String response = """
                There are several ways to reset your password, depending on your situation.
                
                If you have a company phone, you can use the preferred method: Open the Company Authenticator app, select Password Reset from the menu, follow the on-screen instructions to verify your identity, enter your new password and confirm. You'll receive a confirmation message once your password has been successfully updated. [^/guide/password-reset-guide]
                
                ---
                
                ---SOURCES_JSON---
                {"sources_used":[{"id":"/guide/password-reset-guide"}]}
                ---END_SOURCES_JSON---""";

        /* When */
        final ParsedResponse parsed = ParsedResponse.parse(response);

        /* Then */
        assertThat(parsed)
                .isEqualTo(new ParsedResponse("""
                        There are several ways to reset your password, depending on your situation.
                        
                        If you have a company phone, you can use the preferred method: Open the Company Authenticator app, select Password Reset from the menu, follow the on-screen instructions to verify your identity, enter your new password and confirm. You'll receive a confirmation message once your password has been successfully updated.""",
                        List.of("/guide/password-reset-guide")));
    }

    @Test
    void parseResponseWithSourcesFootnotesAndLinks() {
        /* Given */
        final String response = """
                To reset your password, you have a few options[^/guide/password-reset-guide].
                
                You can try resetting it using the Company Authenticator app on your company-issued phone, or ask your direct manager to assist with the process if you don't have a company phone.
                
                If those options aren't available, you can visit an IT Support Desk in person. However, there's also an alternative method mentioned in [this ticket](^/ticket/password-reset-no-company-phone-and-manager-is-out-of-office) where you can arrange a secure video call with an IT agent for remote identity verification.
                
                Please let me know which option works best for you so I can provide more specific guidance!
                
                ---SOURCES_JSON---
                {"sources_used":[{"id":"/guide/password-reset-guide"}]}
                ---END_SOURCES_JSON---""";

        /* When */
        final ParsedResponse parsed = ParsedResponse.parse(response);

        /* Then */
        assertThat(parsed)
                .isEqualTo(new ParsedResponse("""
                        To reset your password, you have a few options.
                        
                        You can try resetting it using the Company Authenticator app on your company-issued phone, or ask your direct manager to assist with the process if you don't have a company phone.
                        
                        If those options aren't available, you can visit an IT Support Desk in person. However, there's also an alternative method mentioned in [this ticket](^/ticket/password-reset-no-company-phone-and-manager-is-out-of-office) where you can arrange a secure video call with an IT agent for remote identity verification.
                        
                        Please let me know which option works best for you so I can provide more specific guidance!""",
                        List.of("/guide/password-reset-guide")));
    }

    @Test
    void parseBadlyFormattedResponse() {
        /* Given */
        final String response = """
                You can try resetting it using the Company Authenticator app on your company-issued phone, or ask your direct manager to assist with the process if you don't have a company phone.
                
                ---
                
                SOURCES_JSON---
                {"sources_used":[{"id":"/guide/password-reset-guide"}]}
                ---END_SOURCES_JSON---""";

        /* When */
        final ParsedResponse parsed = ParsedResponse.parse(response);

        /* Then */
        assertThat(parsed)
                .isEqualTo(new ParsedResponse("""
                        You can try resetting it using the Company Authenticator app on your company-issued phone, or ask your direct manager to assist with the process if you don't have a company phone.""",
                        List.of("/guide/password-reset-guide")));
    }
}
