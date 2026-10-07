package com.zioanacleto.feedtracker.locale

import io.kotest.matchers.shouldBe
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test

class StringResourceCatalogTest {

    @Test
    fun italianStringsMatchTheDefaultCatalog() {
        val resources = File("src/commonMain/composeResources")
        stringNames(File(resources, "values/strings.xml")) shouldBe
            stringNames(File(resources, "values-it/strings.xml"))
    }

    private fun stringNames(file: File): List<String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val nodes = document.getElementsByTagName("string")
        return List(nodes.length) { index ->
            nodes.item(index).attributes.getNamedItem("name").nodeValue
        }
    }
}
