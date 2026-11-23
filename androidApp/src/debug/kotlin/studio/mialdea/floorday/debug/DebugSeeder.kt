package studio.mialdea.floorday.debug

import kotlin.time.Clock
import kotlinx.coroutines.flow.first
import studio.mialdea.floorday.core.domain.model.IdGenerator
import studio.mialdea.floorday.core.domain.repository.HabitRepository
import studio.mialdea.floorday.core.domain.time.DayStart
import studio.mialdea.floorday.core.domain.time.LogicalDayResolver

/**
 * Debug builds only: fills an empty database with the habits in the seed, so the phone shows
 * real habits before the editor exists (#14). It never touches a database that has habits.
 */
internal class DebugSeeder(
    private val habits: HabitRepository,
    private val clock: Clock,
    private val logicalDay: LogicalDayResolver,
    private val readSeed: () -> String?,
) {
    /** Number of habits saved: 0 when the database already had some or there is no seed. */
    suspend fun seedIfEmpty(): Int {
        if (habits.observeHabits(includeArchived = true).first().isNotEmpty()) return 0
        val json = readSeed() ?: return 0
        val seeded = SeedParser.parse(json, IdGenerator.Random, logicalDay.today(DayStart.Default), clock.now())
        seeded.forEach { habits.save(it) }
        return seeded.size
    }
}
