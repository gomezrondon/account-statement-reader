package com.gomezrondon.accountstatementreader.service

import com.gomezrondon.accountstatementreader.repository.CustomerRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux


@Service
class ProcessService(val loadFileService: LoadFileService, val repository: CustomerRepository) {

    fun insertOneElement(workingDirectory: String) {

        convertToUTF8Encoding(workingDirectory)//we convert from ANSI to UTF-8

        val block = getBlocsFromFile(workingDirectory)

        val consolidado = mapToConsolidadoObject(block)
        consolidado.fileName = workingDirectory

        val consFromDB:Consolidado? = repository.findByStrDate(consolidado.strDate)
        println(">>>>>>> PROCESSING ${consolidado.fileName}")
        if (consFromDB == null){
            repository.save(consolidado)
            println(">>>>>>> ${consolidado.strDate} INSERTED!")

        }else{
            println(">>>>>>> ${consFromDB.strDate} ALREADY EXIST!!")
        }

    }




    private fun mapToConsolidadoObject(block: MutableList<Flux<String>>?): Consolidado {
        var block1 = getBlock(block!!, 1)
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


        return consolidado
    }

    private fun getBlocsFromFile(workingDirectory: String): MutableList<Flux<String>>? {
        val block = loadFileService.readFile(workingDirectory)
                .skip(1)
                .windowWhile({ linea -> linea.length > 0 })
                //.subscribe(System.out::println);
                .collectList().block()
        return block
    }

    fun insertManyElements(workingDirectory: String){

        val pathOfFilesInFolder = loadFileService.getPathOfFilesInFolder(workingDirectory)

        pathOfFilesInFolder.forEach {
            insertOneElement(it.toString())
        }
    }

    fun deleteOneById(id: String) {
        repository.deleteById(id)
    }


}


