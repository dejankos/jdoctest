package io.github.dejankos.jdoctest.core

fun main(args: Array<String>) {
//    val spoon = Launcher()
//    spoon.addInputResource("../jdoctest/example/src/main/java/")
//    val model = spoon.buildModel()
//    val elements = model.getElements(TypeFilter(CtClass::class.java))
//
//    for (element in elements) {
//        val comments = element.comments
//
//        println(comments)
//
//        val imports = element.getUsedTypes(true)
//
//        val methods = element.methods
//        for (method in methods) {
//            val comments1 = method.comments
//            for (ctComment in comments1) {
//
// //                (ctComment.asJavaDoc().javadocElements[1] as JavadocInlineTag).content
//
//                println(ctComment)
//            }
//        }

    DocTestParser("../jdoctest/example/src/main/java/").extract()
}