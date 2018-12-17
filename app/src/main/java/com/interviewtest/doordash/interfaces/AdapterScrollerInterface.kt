package com.interviewtest.doordash.interfaces

interface AdapterScrollerInterface<T> {
    fun removeFirstNItems(newItems: List<T>)
    fun removeLastNItems(newItems: List<T>)
    fun update(newItems: List<T>)
    fun update()
    fun getItemDetailByPosition(index: Int): T
    fun getAllItems(): List<T>
}