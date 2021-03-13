package org.touchhome.bundle.dropbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.BundleEntryPoint;
import org.touchhome.bundle.api.EntityContext;

@Log4j2
@Component
@RequiredArgsConstructor
public class DropboxEntrypoint implements BundleEntryPoint {

    private final EntityContext entityContext;

    public void init() {
        entityContext.bgp().run("gdrive-init", () -> {
            for (DropboxEntity dropboxEntity : entityContext.findAll(DropboxEntity.class)) {
                log.info("Restarting Dropbox: <{}>", dropboxEntity.getTitle());
                dropboxEntity.getFileSystem(entityContext).restart(true);
                log.info("Done init Dropbox: <{}>", dropboxEntity.getTitle());
            }
        }, true);
    }

    @Override
    public int order() {
        return 3000;
    }
}
