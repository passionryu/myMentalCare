package com.mymentalcare.server.bootstrap

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MyMentalCareApplication

fun main(args: Array<String>) {
    runApplication<MyMentalCareApplication>(*args)
}
