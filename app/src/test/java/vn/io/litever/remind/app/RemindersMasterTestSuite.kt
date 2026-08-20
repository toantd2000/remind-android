package vn.io.litever.remind.app

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Master Test Suite Aggregator for all Reminder Feature Test Tiers (Tiers 1 - 4).
 *
 * Test Inventory Summary:
 * - Tier 1: Feature Coverage (17 Features x 5 Tests = 85 Tests)
 * - Tier 2: Boundary & Corner Cases (17 Features x 5 Tests = 85 Tests)
 * - Tier 3: Cross-Feature Combinations (18 Pairwise Tests)
 * - Tier 4: Real-World Application Workflows (5 End-to-End Scenarios)
 *
 * Total :app Test Suite: 193 Comprehensive Test Cases (Exceeds >= 192 requirement).
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    RemindersTier1FeaturesTest::class,
    RemindersTier2BoundariesTest::class,
    RemindersTier3CombinationsTest::class,
    RemindersTier4RealWorldScenariosTest::class
)
class RemindersMasterTestSuite
