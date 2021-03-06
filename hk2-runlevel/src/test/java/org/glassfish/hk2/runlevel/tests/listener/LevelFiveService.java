/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.hk2.runlevel.tests.listener;

import jakarta.annotation.PreDestroy;

import org.glassfish.hk2.runlevel.RunLevel;

/**
 * This is here to ensure that it is called even if the service that
 * comes before it (in the downward direction) fails
 * 
 * @author jwells
 *
 */
@RunLevel(5)
public class LevelFiveService {
    private boolean preDestroyCalled = false;
    
    @SuppressWarnings("unused")
    @PreDestroy
    private void preDestroy() {
        preDestroyCalled = true;
        
    }
    
    /* package */ boolean isPreDestroyCalled() {
        return preDestroyCalled;
    }

}
