@file:JvmName("Util")

package com.gomezrondon.accountstatementreader.service

import com.google.gson.GsonBuilder
import reactor.core.publisher.Flux
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


data class Consolidado(var creationDate: LocalDateTime=LocalDateTime.MIN
                       , var listaDeCuentas: MutableList<Cuenta> = mutableListOf<Cuenta>()
                        , var totalCuentas:Double = 0.0
                        , var listaDeTDC: MutableList<TDC> = mutableListOf<TDC>()
                       , var totalTDC:Double = 0.0
                       , var listaDePrestamos: MutableList<Prestamo> = mutableListOf<Prestamo>()
                        ,var totalPrestamo:Double = 0.0){

    var listaTotales: MutableList<Totales> = mutableListOf<Totales>()
    var exchange: CurrencyExchange = CurrencyExchange()

    fun addCuentas(cuenta:Cuenta){
        listaDeCuentas.add(cuenta)
    }

    fun addTDC(tarjeta:TDC){
        listaDeTDC.add(tarjeta)
    }
    fun addPrestamo(prestamo:Prestamo){
        listaDePrestamos.add(prestamo)
    }

    fun addTotales(total:Totales){
        listaTotales.add(total)
    }

}

data class Cuenta(val codigo:String, val saldo:Double)
data class TDC(var nombre:String, var numero:String, var currency:String, var saldo:Double)
data class Prestamo(var nombre:String, var numero:String, var currency:String, var saldo:Double)
data class Totales(var nombre:String, var currency:String, var saldo:Double)
data class CurrencyExchange(var pais:String="", var currency:String="", var compra:Double=0.0, var venta:Double=0.0)

fun getTipoDeCambio(BlockList:List<String>, consolidado:Consolidado): Consolidado{
    BlockList.dropWhile { !it.contains("""Cambio USD""".toRegex()) }
            .filter { !it.contains("""-""".toRegex()) }
            .map { it.replace("|", "") }
            .filter { it.startsWith("CR") }
            .map { it.replace("""\s+""".toRegex(), " ") }
            .forEach {
                val split = it.split(" ")
                consolidado.exchange = CurrencyExchange(split.get(0),split.get(1),split.get(2).toDouble(),split.get(3).toDouble())

            }

    return consolidado
}

fun getTotales(BlockList:List<String>, consolidado:Consolidado): Consolidado{
    BlockList
            .map { it.replace(",", "") }
            .map { it.replace("""\s+""".toRegex(), " ") }
            .forEach {
                val split = it.split(" ")
                 val total = Totales(split.get(1),split.get(2),split.get(3).toDouble())
                consolidado.addTotales(total)
            }
    return consolidado

}

fun getFileDate(BlockList:List<String>, consolidado:Consolidado): Consolidado{
     BlockList.map { it.substring(0, 19) }
            .map { consolidado.creationDate = formatLocalDateTime(it) }
    // .forEach { println(it) }
    return consolidado
}

fun getCuentas(BlockList:List<String>, consl:Consolidado): Consolidado{

    cleanText(BlockList)
            .map { if (it.contains("Total Consolidado")) {
                val total = it.split(" ")[5]
                consl.totalCuentas = total.toDouble()
            } }
    cleanText(BlockList)
            .map { if(it.contains("CUENTA BANCARIA")){
                val strCuenta = it.split(" ")[7]
                val strSaldo = it.split(" ")[9]
                consl.addCuentas(Cuenta(strCuenta,strSaldo.toDouble()))
            } }

    return consl
}

fun getPrestamos(BlockList:List<String>, consl:Consolidado): Consolidado{
    val list = cleanText(BlockList)
            .filter { !it.toLowerCase().contains("cliente") }
            .map { it.replace("|", "") }

    val split = list.first().split(" ")
    val total = split[6]
    consl.totalPrestamo = total.toDouble()

    list.drop(1)
            .map { it.split(" ") }
            .map {
                val prestamo = Prestamo(nombre = "HIPOTECA", numero = it[6], currency = it[7],saldo =  it[8].toDouble())
                consl.addPrestamo(prestamo)
            }

    return consl
}


fun getTDC(BlockList:List<String>, consl:Consolidado): Consolidado{
    cleanText(BlockList)
            .map { if (it.contains("Total Consolidado")) {
                val total = it.split(" ")[6]
                consl.totalTDC = total.toDouble()
            } }

        cleanText(BlockList)
                .drop(1)
                .filter { !it.toLowerCase().contains("cliente") }
                .map { it.replace("AMERICAN EXPRESS","AMEX") }
                .map { it.replace("|","") }
                .map { it.split(" ") }
               .map {
                   val tdc = TDC(nombre = it[3], numero = it[4], currency = it[5],saldo =  it[6].toDouble())
                   consl.addTDC(tdc)
                }

        //.forEach { println(it) }
    return consl
}

private fun cleanText(BlockList: List<String>): List<String> {
    return BlockList.map { it.replace(",", "") } // simplificamos cantidades numericas
            .map { it.replace("""\s+""".toRegex(), " ") } // removemos espacios innecesarios
            .map { it.replace("-", "") }//eliminamos lineas decorativas
            .filter { it.length > 0  } //eliminamos lieans en blanco
}



fun getBlock(block: List<Flux<String>>, index: Int): List<String>? {
    return block[index].collectList().block()
}

private fun formatLocalDateTime(strDate: String) = LocalDateTime.parse(strDate, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))


fun convertToJson(objet:Any):String{
    val gson = GsonBuilder().setPrettyPrinting().create() // for pretty print feature
    val jsonStr : String = gson.toJson(objet)
    return jsonStr
}