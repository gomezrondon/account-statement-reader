package com.gomezrondon.accountstatementreader;


import com.gomezrondon.accountstatementreader.repository.CustomerRepository;
import com.gomezrondon.accountstatementreader.service.Consolidado;
import com.gomezrondon.accountstatementreader.service.Util;
import com.gomezrondon.accountstatementreader.service.LoadFileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

import java.util.List;


@SpringBootApplication
public class Application implements CommandLineRunner {
	@Value("${working.dierectory}")
	private String workingDirectory;

	private final LoadFileService loadFileService;

	@Autowired
	private CustomerRepository repository;

	public Application(LoadFileService loadFileService) {
		this.loadFileService = loadFileService;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	@Override
	public void run(String... args) throws Exception {

		workingDirectory = args.length > 0 ? args[0] : workingDirectory ;

		repository.deleteAll();

		List<Flux<String>> block = loadFileService.readFile(workingDirectory)
				.skip(1)
				.windowWhile(linea -> linea.length() > 0)
				//.subscribe(System.out::println);
				.collectList().block();

		System.out.println("number of blocks:"+block.size());
		//block.get(2).subscribe(System.out::println);

		List<String> block1 = Util.getBlock(block, 1);
		//System.out.println(block1);
		Consolidado consolidado = Util.getFileDate(block1, new Consolidado());
		block1 = Util.getBlock(block, 2);
		consolidado = Util.getCuentas(block1, consolidado);
		block1 = Util.getBlock(block, 4);
		consolidado = Util.getTDC(block1, consolidado);
		block1 = Util.getBlock(block, 5);
		consolidado = Util.getPrestamos(block1, consolidado);
		block1 = Util.getBlock(block, 6);
		consolidado = Util.getTotales(block1, consolidado);

		block1 = Util.getBlock(block, 7);
		consolidado = Util.getTipoDeCambio(block1,consolidado);
		 System.out.println(Util.convertToJson(consolidado));

	}



}
