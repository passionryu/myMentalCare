package com.mymentalcare.server.infrastructure.persistence.aichat

import org.springframework.data.jpa.repository.JpaRepository

interface JpaCrisisDetectionEventRepository : JpaRepository<CrisisDetectionEventEntity, Long>
