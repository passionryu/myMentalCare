package com.mymentalcare.server.infrastructure.persistence.inquiry

import org.springframework.data.jpa.repository.JpaRepository

interface JpaInquiryRepository : JpaRepository<InquiryEntity, Long>
