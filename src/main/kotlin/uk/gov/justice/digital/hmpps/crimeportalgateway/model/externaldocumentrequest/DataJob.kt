package uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class DataJob(
    var name: String? = null
) {
    @field:Valid
    @field:NotNull
    @JsonManagedReference
    val job: Job = Job()

    @JsonBackReference
    lateinit var document: Document
}
