package org.hisp.dhis.dxf2.events.importer.insert.validation;

/*
 * Copyright (c) 2004-2020, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import static org.hisp.dhis.DhisConvenienceTest.createProgram;
import static org.hisp.dhis.DhisConvenienceTest.createProgramStage;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.dxf2.events.importer.shared.ImmutableEvent;
import org.hisp.dhis.dxf2.events.importer.validation.BaseValidationTest;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramStage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;

public class ProgramInstanceRepeatableStageCheckTest extends BaseValidationTest
{
    private ProgramInstanceRepeatableStageCheck rule;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp()
    {
        rule = new ProgramInstanceRepeatableStageCheck();
    }

    @Test
    public void failOnNonRepeatableStageAndExistingEvents()
    {
        // Data preparation
        Program program = createProgram( 'P' );

        event.setProgramStage( CodeGenerator.generateUid() );
        event.setProgram( program.getUid() );
        ProgramStage programStage = createProgramStage( 'A', program );
        programStage.setRepeatable( false );

        when( workContext.getProgramStage( programStageIdScheme, event.getProgramStage() ) ).thenReturn( programStage );

        Map<String, Program> programMap = new HashMap<>();
        programMap.put( program.getUid(), program );
        when( workContext.getProgramsMap() ).thenReturn( programMap );

        Map<String, ProgramInstance> programInstanceMap = new HashMap<>();
        ProgramInstance programInstance = new ProgramInstance();
        programInstanceMap.put( event.getUid(), programInstance );

        when( workContext.getProgramsMap() ).thenReturn( programMap );
        when( workContext.getProgramInstanceMap() ).thenReturn( programInstanceMap );
        when( workContext.getServiceDelegator() ).thenReturn( serviceDelegator );
        when( serviceDelegator.getJdbcTemplate() ).thenReturn( jdbcTemplate );
        when( jdbcTemplate.queryForObject( anyString(), eq( Boolean.class ), eq( programStage.getUid() ) ) )
            .thenReturn( true );

        // Method under test
        ImportSummary summary = rule.check( new ImmutableEvent( event ), workContext );
        assertHasError( summary, event, "Program stage is not repeatable and an event already exists" );
    }
}