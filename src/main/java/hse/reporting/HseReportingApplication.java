package hse.reporting;

import hse.reporting.config.ReportOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ReportOptions.class)
public class HseReportingApplication {

    public static void main(String[] args) {
        SpringApplication.run(HseReportingApplication.class, args);
    }
}
