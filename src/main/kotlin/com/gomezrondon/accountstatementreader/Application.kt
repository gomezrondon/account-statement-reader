package com.gomezrondon.accountstatementreader

import com.gomezrondon.accountstatementreader.repository.CustomerRepository
import com.gomezrondon.accountstatementreader.service.Consolidado
import com.gomezrondon.accountstatementreader.service.LoadFileService
import com.gomezrondon.accountstatementreader.service.getBlock
import com.gomezrondon.accountstatementreader.service.getCuentas
import com.gomezrondon.accountstatementreader.service.getFileDate
import com.gomezrondon.accountstatementreader.service.getMD5HashId
import com.gomezrondon.accountstatementreader.service.getPrestamos
import com.gomezrondon.accountstatementreader.service.getTDC
import com.gomezrondon.accountstatementreader.service.getTipoDeCambio
import com.gomezrondon.accountstatementreader.service.getTotales
import com.gomezrondon.accountstatementreader.service.convertToJson

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.IOException

@SpringBootApplication
class Application(val loadFileService: LoadFileService, val repository: CustomerRepository): CommandLineRunner {



	override fun run(vararg args: String?) {
		val option: String? = args[0]

		when (option) {
			"1" -> insertOneElement(args[1]!!) // insert one - file path
			"2" -> repository.deleteAll() // insert many
			"3" -> repository.deleteAll() // delete
			"4" -> getMD5HashId(args[1]!!) // date in string format dd/MM/yyyy HH:mm:ss
			else -> println("<< No option was selected >>")
		}
	}



	fun insertOneElement(workingDirectory: String) {

		val block = loadFileService.readFile(workingDirectory)
				.skip(1)
				.windowWhile({ linea -> linea.length > 0 })
				//.subscribe(System.out::println);
				.collectList().block()

		println("number of blocks:" + block!!.size)
		//block.get(2).subscribe(System.out::println);

		var block1 = getBlock(block, 1)
		//System.out.println(block1);
		var consolidado = getFileDate(block1!!, Consolidado())
		block1 = getBlock(block, 2)
		consolidado = getCuentas(block1!!, consolidado)
		block1 = getBlock(block, 4)
		consolidado = getTDC(block1!!, consolidado)
		block1 = getBlock(block, 5)
		consolidado = getPrestamos(block1!!, consolidado)
		block1 = getBlock(block, 6)
		consolidado = getTotales(block1!!, consolidado)

		block1 = getBlock(block, 7)
		consolidado = getTipoDeCambio(block1!!, consolidado)
		/*
            System.out.println(Util.convertToJson(consolidado));
            Consolidado one = repository.findByCreationDate(consolidado.getCreationDate());
            Consolidado one = repository.findConsolidadoBy(consolidado);
            */


		repository.save(consolidado)
		/*
            Optional<Consolidado> id = repository.findById(consolidado.getId());
            System.out.println(Util.convertToJson(id.get()));
            */

		val consolidado1 = repository.findByStrDate(consolidado.strDate)
		System.out.println(convertToJson(consolidado1));

	}

}



fun main(args: Array<String>) {
	runApplication<Application>(*args)
}
