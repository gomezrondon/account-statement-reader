package com.gomezrondon.accountstatementreader

import com.gomezrondon.accountstatementreader.repository.CustomerRepository
import com.gomezrondon.accountstatementreader.service.*
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class Application(val process:ProcessService,val repository: CustomerRepository): CommandLineRunner {



	override fun run(vararg args: String?) {
		val option: String? = args[0]

		when (option) {
			"1" -> process.insertOneElement(args[1]!!) // insert one - file path
			"2" -> process.insertManyElements(args[1]!!) // insert many
			"3" -> repository.deleteAll() // delete
			"4" -> getMD5HashId(args[1]!!) // date in string format dd/MM/yyyy HH:mm:ss
			"5" -> process.deleteOneById(args[1]!!) //aa5ff475e0109de3121f0c91c5fa9a4d
			"6" -> convertToUTF8Encoding(args[1]!!)
			else -> println("<< No option was selected >>")
		}
	}



}


fun main(args: Array<String>) {
	runApplication<Application>(*args)
}
