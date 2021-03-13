package org.touchhome.bundle.ftp;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.BundleEntryPoint;
import org.touchhome.bundle.api.EntityContext;

@Log4j2
@Component
@RequiredArgsConstructor
public class FtpEntrypoint implements BundleEntryPoint {

    private final EntityContext entityContext;

    public void init() {
        entityContext.bgp().run("gdrive-init", () -> {
            for (FtpEntity ftpEntity : entityContext.findAll(FtpEntity.class)) {
                log.info("Restarting FtpEntity: <{}>", ftpEntity.getTitle());
                ftpEntity.getFileSystem(entityContext).restart(true);
                log.info("Done init FtpEntity: <{}>", ftpEntity.getTitle());
            }
        }, true);
    }

    @Override
    public int order() {
        return 5000;
    }
}
