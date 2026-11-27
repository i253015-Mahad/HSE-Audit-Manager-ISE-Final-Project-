package com.hse.reporting.useraccess;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Guards the development seed data (data.sql): every seeded account must use
 * one of the documented demo passwords, stored as a BCrypt hash — never in plain text.
 */
class DemoSeedDataTest {

    private static final String SLICE_1_DEMO_PASSWORD = "hse-demo-123";
    private static final String SLICE_3_DEMO_PASSWORD = "password";
    // Only user rows carry a BCrypt hash; designation rows also start with an email.
    private static final Pattern SEEDED_ACCOUNT =
            Pattern.compile("\\('([^']+@[^']+)',\\s*'(\\$2[aby]\\$\\d{2}\\$[^']+)'");

    @Test
    void everySeededAccountUsesADocumentedDemoPasswordAsBcryptHash() throws Exception {
        String dataSql = Files.readString(Path.of("src", "main", "resources", "data.sql"));
        Matcher matcher = SEEDED_ACCOUNT.matcher(dataSql);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        int accounts = 0;
        while (matcher.find()) {
            accounts++;
            String hash = matcher.group(2);
            assertThat(encoder.matches(SLICE_1_DEMO_PASSWORD, hash)
                    || encoder.matches(SLICE_3_DEMO_PASSWORD, hash))
                    .as("seeded password hash for %s", matcher.group(1))
                    .isTrue();
        }

        assertThat(accounts)
                .as("number of seeded accounts in data.sql")
                .isGreaterThanOrEqualTo(3);
    }

    @Test
    void allThreeSystemRolesAreRepresentedInTheSeedData() throws Exception {
        String dataSql = Files.readString(Path.of("src", "main", "resources", "data.sql"));

        assertThat(dataSql).contains("'ADMIN'");
        assertThat(dataSql).contains("'MANAGER'");
        assertThat(dataSql).contains("'INSPECTOR'");
    }
}
