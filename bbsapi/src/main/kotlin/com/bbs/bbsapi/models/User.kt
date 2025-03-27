package com.bbs.bbsapi.models

import jakarta.persistence.*
import lombok.Data
import lombok.NoArgsConstructor

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    var username: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    var email: String,

    @Column(nullable = false)
    var phonenumber: String,


    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    val role: Role?=null,

): BaseEntity()
{
    constructor() : this(null, "","", "", "" , Role())
}