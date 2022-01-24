package io.github.dejankos.jdoctest.core

data class DocTestContext(
    val typeInfo: TypeInfo,
    val docsCode: List<DocTestCode>,
)

data class TypeInfo(
    val `package`: String,
    val name: String,
    val imports: List<String>,
)

data class DocTestCode(
    val docTestImports: List<String>,
    val docTestCode: List<String>
)
