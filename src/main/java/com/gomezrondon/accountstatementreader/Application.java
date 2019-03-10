package com.gomezrondon.accountstatementreader;


import com.gomezrondon.accountstatementreader.repository.CustomerRepository;
import com.gomezrondon.accountstatementreader.service.Consolidado;
import com.gomezrondon.accountstatementreader.service.Util;
import com.gomezrondon.accountstatementreader.service.LoadFileService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.gomezrondon.accountstatementreader.service.Util.getMD5HashId;


@SpringBootApplication
public class Application implements CommandLineRunner {

	private final LoadFileService loadFileService;
	private final CustomerRepository repository;

	public Application(LoadFileService loadFileService, CustomerRepository repository) {
		this.loadFileService = loadFileService;
		this.repository = repository;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


	@Override
	public void run(String... args) throws Exception {

		String option = args[0];

		switch(option){
			case "1":
				insertOneElement(args[1]); // insert one - file path
				break;
			case "2": repository.deleteAll(); // insert many
				break;
			case "3": repository.deleteAll(); // delete
				break;
			case "4": getMD5HashId(args[1]); // date in string format dd/MM/yyyy HH:mm:ss
				break;
			default: System.out.println("<< No option was selected >>");
		}

	}





	private void insertOneElement(String workingDirectory) throws IOException {

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
	/*
		System.out.println(Util.convertToJson(consolidado));
		Consolidado one = repository.findByCreationDate(consolidado.getCreationDate());
		Consolidado one = repository.findConsolidadoBy(consolidado);
		*/


		repository.save(consolidado);
		Optional<Consolidado> id = repository.findById(consolidado.getId());
		//System.out.println(consolidado.getId());
		System.out.println(Util.convertToJson(id.get()));
	}


}
