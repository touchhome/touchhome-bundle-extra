package org.touchhome.bundle.ipscanner;

import net.azib.ipscan.IPScannerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.touchhome.bundle.api.BundleConfiguration;

@Configuration
@BundleConfiguration
public class IPScannerConfiguration {

    @Bean
    public IPScannerService ipScannerService() {
        return new IPScannerService(IPScannerService.Fetcher.values());
    }
}
