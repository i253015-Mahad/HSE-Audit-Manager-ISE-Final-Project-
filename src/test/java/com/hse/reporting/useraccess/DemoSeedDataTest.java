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
 * the single documented demo password, stored as a BCrypt hash — never in plain text.
 */
class DemoSeedDataTest {

    private static final String DEMO_PASSWORD = "hse-demo-123";
    private static final Pattern SEEDED_ACCOUNT =
            Pattern.compile("\\('([^']+@[^']+)',\\s*'([^']+)'");

    @Test
    void everySeededAccountUsesTheDocumentedDemoPasswordAsBcryptHash() throws Exception {
        String dataSql = Files.readString(Path.of("src", "main", "resources", "data.sql"));
        Matcher matcher = SEEDED_ACCOUNT.matcher(dataSql);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        int accounts = 0;
        while (matcher.find()) {
            accounts++;
            assertThat(encoder.matches(DEMO_PASSWORD, matcher.group(2)))
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
