package org.touchhome.bundle.drive;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.touchhome.bundle.api.json.Option;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rest/drive")
public class GoogleDriveController {

    private final GoogleDriveEntrypoint googleDriveEntrypoint;

    @GetMapping("file")
    public Set<Option> getFiles() {
        return googleDriveEntrypoint.getGoogleDriveFileSystem().getFiles()
                .stream().map(f -> Option.of(f.getId(), f.getName())).collect(Collectors.toSet());
    }
}
