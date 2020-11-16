package org.touchhome.bundle.drive;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.scratch.*;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Getter
@Component
public class Scratch3GoogleDriveBlocks extends Scratch3ExtensionBlocks {

    private final GoogleDriveEntrypoint googleDriveEntrypoint;

    private final MenuBlock.ServerMenuBlock filesMenu;

    private final Scratch3Block sendFile;
    private final Scratch3Block getFileContent;

    public Scratch3GoogleDriveBlocks(EntityContext entityContext, GoogleDriveEntrypoint googleDriveEntrypoint) {
        super("#51633C", entityContext, googleDriveEntrypoint);
        this.googleDriveEntrypoint = googleDriveEntrypoint;

        // menu
        this.filesMenu = MenuBlock.ofServer("FILES", "rest/drive/file");

        // blocks
        this.sendFile = Scratch3Block.ofHandler(10, "send_file", BlockType.command, "Send file name [NAME] value [CONTENT] parent [PARENT]", this::sendFileHandle);
        this.sendFile.addArgument("NAME", ArgumentType.string);
        this.sendFile.addArgument("CONTENT", ArgumentType.string);
        this.sendFile.addArgument("PARENT", ArgumentType.string);

        this.getFileContent = Scratch3Block.ofEvaluate(10, "get_file_content", BlockType.reporter, "Get file [FILE] content | Raw: [RAW]", this::getFieldContent);
        this.getFileContent.addArgument("FILE", this.filesMenu);
        this.getFileContent.addArgument("RAW", ArgumentType.checkbox);

        postConstruct();
    }

    private Object getFieldContent(WorkspaceBlock workspaceBlock) {
        String fileId = workspaceBlock.getMenuValue("FILE", this.filesMenu);
        boolean isRaw = workspaceBlock.getInputBoolean("RAW");
        if (!"-".equals(fileId)) {
            if (isRaw) {
                return googleDriveEntrypoint.getGoogleDriveFileSystem().getFileRawContent(fileId);
            } else {
                return googleDriveEntrypoint.getGoogleDriveFileSystem().getFileContent(fileId);
            }
        }
        return null;
    }

    private void sendFileHandle(WorkspaceBlock workspaceBlock) {
        String fileName = workspaceBlock.getInputString("NAME");
        String content = workspaceBlock.getInputString("CONTENT");
        String parent = workspaceBlock.getInputString("PARENT");
        if (isNotEmpty(fileName)) {
            googleDriveEntrypoint.getGoogleDriveFileSystem().addOrUpdateFile(parent, content.getBytes(), fileName, null);
        } else {
            workspaceBlock.logErrorAndThrow("Google drive send file block requires file name");
        }
    }
}
