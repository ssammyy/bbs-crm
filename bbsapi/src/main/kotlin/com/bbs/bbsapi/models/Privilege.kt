package com.bbs.bbsapi.models

import jakarta.persistence.*

@Entity
@Table(name = "privileges")
data class Privilege(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val name: String
): BaseEntity()
{
    constructor() : this(null, "")
}
