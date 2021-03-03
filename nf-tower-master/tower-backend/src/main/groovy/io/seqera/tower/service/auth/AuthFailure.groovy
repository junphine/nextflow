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

package io.seqera.tower.service.auth

import groovy.transform.CompileStatic
import io.micronaut.security.authentication.AuthenticationFailed

/**
 * Auth response object that allows a custom response message
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@CompileStatic
class AuthFailure extends AuthenticationFailed {

    private String message

    AuthFailure() {
    }

    AuthFailure(String message) {
        this.message = message
    }

    @Override
    Optional<String> getMessage() {
        return message ? Optional.of(message) : super.getMessage()
    }

}
