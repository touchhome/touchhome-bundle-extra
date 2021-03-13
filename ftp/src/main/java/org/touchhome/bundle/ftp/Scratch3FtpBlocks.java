package org.touchhome.bundle.ftp;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.fs.Scratch3BaseFileSystemExtensionBlocks;

@Getter
@Component
public class Scratch3FtpBlocks extends Scratch3BaseFileSystemExtensionBlocks<FtpEntrypoint, FtpEntity> {

    public Scratch3FtpBlocks(EntityContext entityContext, FtpEntrypoint ftpEntrypoint) {
        super("Ftp", "#306b75", entityContext, ftpEntrypoint, FtpEntity.class);
    }
}
