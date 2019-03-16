package com.gomezrondon.accountstatementreader.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class LoadFileServiceImp implements LoadFileService {


    private static Flux<String> fromPath(Path path) {
        return Flux.using(() -> Files.lines(path),
                Flux::fromStream,
                BaseStream::close
        );
    }


    public List<Path> getPathOfFilesInFolder(String folder) throws IOException {

        try (Stream<Path> paths = Files.walk(Paths.get(folder))) {
            return paths
                    .filter(Files::isRegularFile)
                    //.peek(System.out::println)
                    .collect(Collectors.toList());
        }
    }


     public Flux<String> readFile(String file) throws IOException {

          if(file != null){
              Flux<String> stringFlux = fromPath(Paths.get(file));
              return stringFlux;
         }

        return null;
     }

}
