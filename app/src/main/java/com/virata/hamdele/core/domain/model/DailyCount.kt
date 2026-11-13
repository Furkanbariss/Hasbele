package com.virata.hamdele.core.domain.model

import java.time.LocalDate

data class DailyCount(val date: LocalDate, val zikirId: Long, val count: Int)
