/*
 * Copyright (c) 2019, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.controller

import javax.inject.Inject
import java.time.Instant

import grails.gorm.transactions.Transactional
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.jwt.render.AccessRefreshToken
import io.micronaut.security.token.jwt.validator.JwtTokenValidator
import io.micronaut.test.annotation.MicronautTest
import io.reactivex.Flowable
import io.seqera.tower.Application
import io.seqera.tower.domain.User
import io.seqera.tower.domain.UserRole
import io.seqera.tower.service.auth.AuthenticationByMailAuthToken
import io.seqera.tower.util.AbstractContainerBaseTest
import io.seqera.tower.util.DomainCreator

@MicronautTest(application = Application.class)
@Transactional
class LoginControllerTest extends AbstractContainerBaseTest {

    @Inject
    JwtTokenValidator tokenValidator
    @Inject
    AuthenticationByMailAuthToken authenticationProviderByAuthToken

    @Inject
    @Client('/')
    RxHttpClient client

    void 'login with valid credentials (email and authToken) for a user'() {
        given: "a user"
        User user = new DomainCreator().createUser()

        and: "grant a role to the user"
        UserRole userRole = new DomainCreator().createUserRole(user: user)

        when: "do the login request attaching userName and authToken as credentials"
        HttpRequest request = HttpRequest.create(HttpMethod.POST, '/login')
                                         .accept(MediaType.APPLICATION_JSON_TYPE)
                                         .body(new UsernamePasswordCredentials(user.getUid(), user.authToken))
        HttpResponse<AccessRefreshToken> response = client.toBlocking().exchange(request, AccessRefreshToken)

        then:
        response.status == HttpStatus.OK
        response.body.get().accessToken
        response.body.get().refreshToken

        when:
        String accessToken = response.body.get().accessToken
        Authentication authentication = Flowable.fromPublisher(tokenValidator.validateToken(accessToken)).blockingFirst()

        then:
        authentication.attributes
        authentication.attributes.roles == [userRole.role.authority]
        authentication.attributes.iss
        authentication.attributes.exp
        authentication.attributes.iat
    }

    void 'try to login with valid credentials (email and authToken) for a user which has an expired authToken'() {
        given: "a user"
        def duration = authenticationProviderByAuthToken.authMailDuration
        def time = Instant.now().minus(duration).minus(duration)
        User user = new DomainCreator().createUser(authTime: time)

        when: "do the login request attaching userName and authToken as credentials"
        HttpRequest request = HttpRequest.create(HttpMethod.POST, '/login')
                                         .accept(MediaType.APPLICATION_JSON_TYPE)
                                         .body(new UsernamePasswordCredentials(user.getUid(), user.authToken))
        client.toBlocking().exchange(request, AccessRefreshToken)

        then: "the server responds UNAUTHORIZED"
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.UNAUTHORIZED
    }

    void 'try to login without supplying credentials'() {
        when: "do the login request"
        HttpRequest request = HttpRequest.create(HttpMethod.POST, '/login')
                                         .accept(MediaType.APPLICATION_JSON_TYPE)
        client.toBlocking().exchange(request)

        then: "the server responds BAD REQUEST"
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.BAD_REQUEST
    }

    void 'try to login supplying a bad combination of credentials (username and authToken)'() {
        given: 'a user'
        User user = new DomainCreator().createUser()

        when: "do the login request attaching a bad authToken"
        HttpRequest request = HttpRequest.create(HttpMethod.POST, '/login')
                                          .accept(MediaType.APPLICATION_JSON_TYPE)
                                          .body(new UsernamePasswordCredentials(user.getUid(), 'badToken'))
        client.toBlocking().exchange(request, AccessRefreshToken)

        then: "the server responds UNAUTHORIZED"
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.UNAUTHORIZED
    }

}
