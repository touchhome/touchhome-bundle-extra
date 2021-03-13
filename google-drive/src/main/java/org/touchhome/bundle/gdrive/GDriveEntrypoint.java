package org.touchhome.bundle.gdrive;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.BundleEntryPoint;
import org.touchhome.bundle.api.EntityContext;

@Log4j2
@Component
@RequiredArgsConstructor
public class GDriveEntrypoint implements BundleEntryPoint {

    private final EntityContext entityContext;

    @Override
    public void init() {
        entityContext.bgp().run("gdrive-init", () -> {
            for (GDriveEntity gDriveEntity : entityContext.findAll(GDriveEntity.class)) {
                log.info("Restarting GDrive: <{}>", gDriveEntity.getTitle());
                gDriveEntity.getFileSystem(entityContext).restart(true);
                log.info("Done init GDrive: <{}>", gDriveEntity.getTitle());
            }
        }, true);
    }

    @Override
    public int order() {
        return 2100;
    }

    @Override
    public BundleImageColorIndex getBundleImageColorIndex() {
        return BundleImageColorIndex.ONE;
    }
}
