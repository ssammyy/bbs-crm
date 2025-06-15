package com.bbs.bbsapi.controllers

import com.bbs.bbsapi.dtos.ContractDTO
import com.bbs.bbsapi.models.ClientContract
import com.bbs.bbsapi.services.ClientContractService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/contracts")
class ClientContractController(
    private val contractService: ClientContractService
) {
    @PostMapping
    fun createContract(@RequestBody contractDTO: ContractDTO): ResponseEntity<Map<String, Any?>> {
        val contract = ClientContract(
            clientId = contractDTO.clientId,
            projectName = contractDTO.projectName,
            boqAmount = contractDTO.boqAmount,
            projectStartDate = contractDTO.projectStartDate,
            projectDuration = contractDTO.projectDuration,
            contractFileUrl = contractDTO.contractFileUrl
        )
        val result = contractService.createContract(contract, contractDTO)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/by-client/{clientId}")
    fun getContractByClientId(@PathVariable clientId: Long): ResponseEntity<Map<String, Any?>?> {
        return ResponseEntity.ok(contractService.getContractWithInvoicesByClientId(clientId))
    }

    @PutMapping("/{contractId}")
    fun updateContract(@PathVariable contractId: Long, @RequestBody contract: ClientContract): ResponseEntity<ClientContract?> {
        return ResponseEntity.ok(contractService.updateContract(contractId, contract))
    }
} 