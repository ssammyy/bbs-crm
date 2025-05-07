package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.entities.ReceiptDTO
import com.bbs.bbsapi.services.ReceiptService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/receipts")
class ReceiptController(private val receiptService: ReceiptService) {

    @PostMapping
    fun createReceipt(@RequestBody receiptDTO: ReceiptDTO): ResponseEntity<ReceiptDTO> {
        return try {
            val createdReceipt = receiptService.createReceipt(receiptDTO)
            ResponseEntity.status(HttpStatus.CREATED).body(createdReceipt)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
        }
    }

    @GetMapping("/invoice/{invoiceId}")
    fun getReceiptsForInvoice(@PathVariable invoiceId: Long): ResponseEntity<List<ReceiptDTO>> {
        val receipts = receiptService.getReceiptsForInvoice(invoiceId)
        return ResponseEntity.ok(receipts)
    }
}