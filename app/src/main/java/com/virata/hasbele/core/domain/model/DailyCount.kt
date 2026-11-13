package com.virata.hasbele.core.domain.model

import java.time.LocalDate

data class DailyCount(val date: LocalDate, val zikirId: Long, val count: Int)
