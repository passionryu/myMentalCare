package com.mymentalcare.server.bootstrap

import org.springframework.boot.autoconfigure.AutoConfigurationPackage
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.mymentalcare.server"])
@AutoConfigurationPackage(basePackages = ["com.mymentalcare.server"])
class MyMentalCareApplication

fun main(args: Array<String>) {
    runApplication<MyMentalCareApplication>(*args)
}
