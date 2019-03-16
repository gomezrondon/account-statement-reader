package com.gomezrondon.accountstatementreader.service;


import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface LoadFileService {

    Flux<String> readFile(String file) throws IOException;

    List<Path> getPathOfFilesInFolder(@NotNull String workingDirectory) throws IOException ;
}
