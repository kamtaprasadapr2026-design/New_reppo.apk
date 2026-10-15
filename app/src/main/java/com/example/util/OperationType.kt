package com.example.util

enum class OperationType(val value: String) {
    READ("read"),
    WRITE("write"),
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete")
}
