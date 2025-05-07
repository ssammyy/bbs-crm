package com.bbs.bbsapi.enums;

enum class ProductOffering {
    JENGA_KWAKO,
    JENGA_STRESS_FREE,
    LABOUR_ONLY_CONTRACTING,
    DIASPORA_BUILDING_SOLUTIONS,
    BUILDING_CONSULTANCY,
    RENOVATIONS_REMODELING_REPAIRS;

    companion object {
        fun fromString(value: String): ProductOffering? {
            return try {
                valueOf(value.uppercase().replace(" ", "_"))
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}

enum class ProductTag(val offering: ProductOffering) {
    JENGA_KWAKO(ProductOffering.JENGA_KWAKO),
    JENGA_STRESS_FREE_COMMERCIAL(ProductOffering.JENGA_STRESS_FREE),
    JENGA_STRESS_FREE_RESIDENTIAL(ProductOffering.JENGA_STRESS_FREE),
    LABOUR_ONLY_COMMERCIAL(ProductOffering.LABOUR_ONLY_CONTRACTING),
    LABOUR_ONLY_RESIDENTIAL(ProductOffering.LABOUR_ONLY_CONTRACTING),
    DIASPORA_COMMERCIAL(ProductOffering.DIASPORA_BUILDING_SOLUTIONS),
    DIASPORA_RESIDENTIAL(ProductOffering.DIASPORA_BUILDING_SOLUTIONS),
    BUILDING_CONSULTANCY(ProductOffering.BUILDING_CONSULTANCY),
    RENOVATIONS_REMODELING_REPAIRS(ProductOffering.RENOVATIONS_REMODELING_REPAIRS);

    companion object {
        fun fromString(value: String): ProductTag? {
            return try {
                valueOf(value.uppercase().replace("-", "_"))
            } catch (e: IllegalArgumentException) {
                null
            }
        }

        fun getTagsForOffering(offering: ProductOffering): List<ProductTag> {
            return values().filter { it.offering == offering }
        }
    }
}

enum class ConsultancySubtag {
    ARCHITECTURE,
    COSTING,
    APPROVALS,
    MATERIALS,
    ELECTRICAL,
    PLUMBING,
    WATERPROOFING,
    INTERIOR_DESIGN,
    BIO_DIGESTER,
    LANDSCAPING;

    companion object {
        fun fromString(value: String): ConsultancySubtag? {
            return try {
                valueOf(value.uppercase().replace("-", "_"))
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}