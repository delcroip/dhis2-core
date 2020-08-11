package org.hisp.dhis.security.oprovider;

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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.hisp.dhis.security.SecurityService;
import org.hisp.dhis.security.oauth2.DefaultClientDetailsUserDetailsService;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @author Henning Håkonsen
 */
@Slf4j
@Component
public class OldOauthAuthenticationProvider extends DaoAuthenticationProvider
{
    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    DefaultClientDetailsUserDetailsService defaultClientDetailsUserDetailsService;

    @Autowired
    public OldOauthAuthenticationProvider( @Qualifier( "defaultClientDetailsUserDetailsService" ) DefaultClientDetailsUserDetailsService detailsService )
    {
//        super();
        setUserDetailsService( detailsService );
        setPasswordEncoder( NoOpPasswordEncoder.getInstance() );
        this.defaultClientDetailsUserDetailsService = detailsService;
    }

    @Override
    public Authentication authenticate( Authentication auth )
        throws AuthenticationException
    {
        log.info( String.format( "OldOauthAuthenticationProvider Login attempt: %s", auth.getName() ) );

        String username = auth.getName();

        UserDetails userCredentials = getUserDetailsService().loadUserByUsername( username );

        if ( userCredentials == null )
        {
            throw new BadCredentialsException( "Invalid username or password" );
        }

        // Initialize all required properties of user credentials since these will become detached

//        userCredentials.getAllAuthorities();
//
//        // -------------------------------------------------------------------------
//        // Check two-factor authentication
//        // -------------------------------------------------------------------------
//
//        if ( userCredentials.isTwoFA() )
//        {
//            TwoFactorWebAuthenticationDetails authDetails =
//                (TwoFactorWebAuthenticationDetails) auth.getDetails();
//
//            // -------------------------------------------------------------------------
//            // Check whether account is locked due to multiple failed login attempts
//            // -------------------------------------------------------------------------
//
//            if ( authDetails == null )
//            {
//                log.info( "Missing authentication details in authentication request." );
//                throw new PreAuthenticatedCredentialsNotFoundException(
//                    "Missing authentication details in authentication request." );
//            }
//
//            String ip = authDetails.getIp();
//            String code = StringUtils.deleteWhitespace( authDetails.getCode() );
//
//            if ( securityService.isLocked( username ) )
//            {
//                log.info( String.format( "Temporary lockout for user: %s and IP: %s", username, ip ) );
//
//                throw new LockedException( String.format( "IP is temporarily locked: %s", ip ) );
//            }
//
//            if ( !LongValidator.getInstance().isValid( code ) || !SecurityUtils.verify( userCredentials, code ) )
//            {
//                log.info(
//                    String.format( "Two-factor authentication failure for user: %s", userCredentials.getUsername() ) );
//
//                throw new BadCredentialsException( "Invalid verification code" );
//            }
//        }

        // -------------------------------------------------------------------------
        // Delegate authentication downstream, using UserCredentials as principal
        // -------------------------------------------------------------------------

        Authentication result = super.authenticate( auth );

        // Put detached state of the user credentials into the session as user
        // credentials must not be updated during session execution

        userCredentials = SerializationUtils.clone( userCredentials );

        // Initialize cached authorities

//        userCredentials.isSuper();
//        userCredentials.getAllAuthorities();

        return new UsernamePasswordAuthenticationToken( userCredentials, result.getCredentials(),
            result.getAuthorities() );
    }

    @Override
    public boolean supports( Class<?> authentication )
    {
        return authentication.equals( UsernamePasswordAuthenticationToken.class );
    }
}
