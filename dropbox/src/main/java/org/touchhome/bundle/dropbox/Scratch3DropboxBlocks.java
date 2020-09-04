package org.touchhome.bundle.dropbox;

import com.dropbox.core.DbxException;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.scratch.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Getter
@Component
public class Scratch3DropboxBlocks extends Scratch3ExtensionBlocks {
    private final DropboxEntrypoint dropboxEntrypoint;

    private final MenuBlock.ServerMenuBlock filesMenu;

    private final Scratch3Block sendFile;
    private final Scratch3Block getFileContent;
    private final Scratch3Block deleteFile;

    public Scratch3DropboxBlocks(EntityContext entityContext, DropboxEntrypoint dropboxEntrypoint) {
        super("#51633C", entityContext, null, "dropbox");
        this.dropboxEntrypoint = dropboxEntrypoint;

        // menu
        this.filesMenu = MenuBlock.ofServer("FILES", "rest/dropbox/file", "-", "-");

        // blocks
        this.sendFile = Scratch3Block.ofHandler(10, "send_file", BlockType.command, "Send file path [PATH] value [CONTENT]", this::sendFileHandle);
        this.sendFile.addArgument("PATH", ArgumentType.string);
        this.sendFile.addArgument("CONTENT", ArgumentType.string);

        this.getFileContent = Scratch3Block.ofEvaluate(20, "get_file_content", BlockType.reporter, "Get file [FILE] content | Raw: [RAW]", this::getFieldContent);
        this.getFileContent.addArgument("FILE", ArgumentType.string, "-", this.filesMenu);
        this.getFileContent.addArgument("RAW", ArgumentType.checkbox);

        this.deleteFile = Scratch3Block.ofHandler(30, "delete_file", BlockType.command, "Delete file [FILE]", this::deleteFileHandle);
        this.deleteFile.addArgument("FILE", ArgumentType.string, "-", this.filesMenu);

        postConstruct();
    }

    private void deleteFileHandle(WorkspaceBlock workspaceBlock) throws DbxException {
        String fileId = workspaceBlock.getMenuValue("FILE", this.filesMenu, String.class);
        if (!"-".equals(fileId)) {
            this.dropboxEntrypoint.getClient().files().deleteV2(fileId);
        }
    }

    private Object getFieldContent(WorkspaceBlock workspaceBlock) throws DbxException, IOException {
        String fileId = workspaceBlock.getMenuValue("FILE", this.filesMenu, String.class);
        boolean isRaw = workspaceBlock.getInputBoolean("RAW");
        if (!"-".equals(fileId)) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            this.dropboxEntrypoint.getClient().files().download(fileId).download(stream);
            return isRaw ? stream.toByteArray() : stream.toString();
        }
        return null;
    }

    @SneakyThrows
    private void sendFileHandle(WorkspaceBlock workspaceBlock) {
        String path = workspaceBlock.getInputString("PATH");
        String content = workspaceBlock.getInputString("CONTENT");
        if (isNotEmpty(path)) {
            this.dropboxEntrypoint.getClient().files().uploadBuilder(path).uploadAndFinish(new ByteArrayInputStream(content.getBytes()));
        } else {
            workspaceBlock.logErrorAndThrow("Dropbox send file block requires file name");
        }
    }
}
