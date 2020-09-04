package org.touchhome.bundle.dropbox;

import com.dropbox.core.v2.files.FileMetadata;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.touchhome.bundle.api.json.Option;

import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@RestController
@RequestMapping("/rest/dropbox")
@RequiredArgsConstructor
public class DropboxController {

    private final DropboxEntrypoint dropboxEntrypoint;

    @SneakyThrows
    @GetMapping("file")
    public Set<Option> getFiles() {
        return dropboxEntrypoint.getFiles().values().stream()
                .filter(m -> m instanceof FileMetadata)
                .map(m -> (FileMetadata) m)
                .map(m -> Option.of(m.getId(), m.getPathDisplay())).collect(Collectors.toSet());
    }
}
