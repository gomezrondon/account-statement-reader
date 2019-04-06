package com.gomezrondon.accountstatementreader.service

import com.gomezrondon.accountstatementreader.repository.CustomerRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.LocalDateTime
import kotlin.reflect.KMutableProperty1


@Service
class ProcessService(val loadFileService: LoadFileService, val repository: CustomerRepository) {

    fun selector(p: Consolidado): LocalDateTime = p.creationDate

    fun processTotalCuenta(){
        val findAll = repository.findAll()

        findAll.sortBy ({selector(it)})

        val printList:MutableList<String> = mutableListOf<String>()
       
        printList.add("fecha|totalCuentas|Dif. total cuentas|totalTDC|Dif. total TDC|Prestamo|Dif. Prestamo")
    
        var difference:Double=0.0
        var index :Int=1;
        val df = DecimalFormat("#.###")
        df.roundingMode = RoundingMode.CEILING
        for (it in findAll) {
            val val1 = it.creationDate.formatShortDateTime()

            difference= if(difference.equals(0.0)){
                 it.totalCuentas
            }else{
                 it.totalCuentas.minus(difference)
            }

            val val2 = convertDecimalSystem(df, difference)
            val val3=it.totalCuentas.toString().replace(".", ",")

            difference = it.totalCuentas
            printList.add("$val1|$val3|$val2")
        }

        difference=0.0
        index=1
        for (it in findAll) {
            difference= if(difference.equals(0.0)){
                it.totalTDC
            }else{
                it.totalTDC.minus(difference)
            }

            val val2 = convertDecimalSystem(df, difference)
            val val3=it.totalTDC.toString().replace(".", ",")

            difference = it.totalTDC

            val line = printList.get(index)
            printList.set(index,"$line|$val3|$val2")
            index++
        }

        difference=0.0
        index=1
        for (it in findAll) {
            difference= if(difference.equals(0.0)){
                it.totalPrestamo
            }else{
                it.totalPrestamo.minus(difference)
            }

            val val2 = convertDecimalSystem(df, difference)
            val val3=it.totalPrestamo.toString().replace(".", ",")

            difference = it.totalPrestamo

            val line = printList.get(index)
            printList.set(index,"$line|$val3|$val2")
            index++
        }

        //calculo de totales de totales
        var transform: KMutableProperty1<Consolidado, Double> = Consolidado::totalCuentas
        val val2 = totalDifference(findAll,transform, difference, df)
        transform = Consolidado::totalTDC
        val val3 = totalDifference(findAll,transform, difference, df)
        transform = Consolidado::totalPrestamo
        val val4 = totalDifference(findAll,transform, difference, df)

        printList.forEach { println(it) }
        println("Total Dif|$val2||$val3||$val4")
    }

    private fun totalDifference(findAll: List<Consolidado>, transform: KMutableProperty1<Consolidado, Double>, difference: Double, df: DecimalFormat): String {
        var difference1 = difference
        val firstTotalCuentas = findAll.map(transform).first()
        val lastTotalCuentas = findAll.map(transform).last()
        difference1 = firstTotalCuentas.minus(lastTotalCuentas)
        val val2 = convertDecimalSystem(df, difference1)
        return val2
    }

    private fun convertDecimalSystem(df: DecimalFormat, diffTotalCuenta: Double) =
            df.format(diffTotalCuenta).replace(".", ",")


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


