/**
 * Copyright (c) 2002-2014 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.util;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Test;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.neo4j.kernel.impl.util.JobScheduler.Group.indexPopulation;

public class Neo4jJobSchedulerTest
{
    private Neo4jJobScheduler scheduler;
    private AtomicInteger invocations = new AtomicInteger( 0 );

    private Runnable countInvocationsJob = new Runnable()
    {
        public void run()
        {
            try
            {
                invocations.incrementAndGet();
            }
            catch(Throwable e)
            {
                e.printStackTrace();
            }
        }
    };

    @After
    public void stopScheduler()
    {
        scheduler.stop();
    }

    @Test
    public void shouldRunRecurringJob() throws Exception
    {
        // Given
        scheduler = new Neo4jJobScheduler( StringLogger.DEV_NULL );

        // When
        scheduler.start();
        scheduler.scheduleRecurring( indexPopulation, countInvocationsJob, 10, MILLISECONDS );
        sleep( 15 );
        scheduler.stop();

        // Then
        int actualInvocations = invocations.get();
        assertThat( actualInvocations, greaterThanOrEqualTo( 2 )); // <-- Dunno how to better assert that this works correctly :/
        assertThat( actualInvocations, lessThan( 6 ) );

        sleep( 10 );
        assertThat( invocations.get(), equalTo(actualInvocations) );
    }

    @Test
    public void shouldCancelRecurringJob() throws Exception
    {
        // Given
        scheduler = new Neo4jJobScheduler( StringLogger.DEV_NULL );

        scheduler.start();
        scheduler.scheduleRecurring( indexPopulation, countInvocationsJob, 2, MILLISECONDS );

        // When
        scheduler.cancelRecurring( indexPopulation,  countInvocationsJob);

        // Then
        int recorded = invocations.get();
        sleep( 10 );
        assertThat( invocations.get(), equalTo(recorded) );
    }

}
