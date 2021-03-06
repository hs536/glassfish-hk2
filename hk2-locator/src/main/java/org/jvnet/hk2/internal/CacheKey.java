/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.glassfish.hk2.api.Unqualified;
import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.glassfish.hk2.utilities.reflection.Pretty;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;

/**
 * This is the cache key, which encapsulates very specific lookup queries.
 * The point of this is to be useable as the key in a hash map, so that
 * equals and hashCode must work properly
 *
 * @author jwells
 *
 */
public class CacheKey {
    private final String removalName;
    private final Type lookupType;
    private final String name;
    private final Annotation qualifiers[];
    private final Unqualified unqualified;

    /** Pre-calculated in order to improve hashMap lookups */
    private final int hashCode;

    /**
     * Key used for LRU cache
     *
     * @param lookupType The type in the lookup call
     * @param name The name in the lookup call
     * @param qualifiers The set of qualifiers being looked up
     */
    public CacheKey(Type lookupType, String name, Unqualified unqualified, Annotation... qualifiers) {
        this.lookupType = lookupType;
        
        Class<?> rawClass = ReflectionHelper.getRawClass(lookupType);
        if (rawClass != null) {
            removalName = rawClass.getName();
        }
        else {
            removalName = null;
        }
        
        this.name = name;
        if (qualifiers.length > 0) {
            this.qualifiers = qualifiers;
        }
        else {
            this.qualifiers = null;
        }
        
        this.unqualified = unqualified;

        int retVal = 0;

        if (lookupType != null) {
            retVal ^= lookupType.hashCode();
        }

        if (name != null) {
            retVal ^= name.hashCode();
        }

        for (Annotation anno : qualifiers) {
            retVal ^= anno.hashCode();
        }
        
        if (unqualified != null) {
            retVal ^= 0xffffffff;
            
            for (Class<?> clazz : unqualified.value()) {
                retVal ^= clazz.hashCode();
            }
        }

        hashCode = retVal;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof CacheKey)) return false;

        final CacheKey other = (CacheKey) o;

        if (hashCode != other.hashCode) return false;
        if (!GeneralUtilities.safeEquals(lookupType, other.lookupType)) return false;
        if (!GeneralUtilities.safeEquals(name, other.name)) return false;

        if (qualifiers != null) {
            if (other.qualifiers == null) return false;

            if (qualifiers.length != other.qualifiers.length) return false;

            boolean isEqual = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

                @Override
                public Boolean run() {
                    for (int lcv = 0; lcv < qualifiers.length; lcv++) {
                        if (!GeneralUtilities.safeEquals(qualifiers[lcv], other.qualifiers[lcv])) return false;
                    }

                    return true;
                }

            });

            if (!isEqual) return false;

        }
        else if (other.qualifiers != null) return false;
        
        if (unqualified != null) {
            if (other.unqualified == null) return false;
            
            Class<?> myClazzes[] = unqualified.value();
            Class<?> otherClazzes[] = other.unqualified.value();
            
            if (myClazzes.length != otherClazzes.length) return false;
            
            for (int lcv = 0; lcv < myClazzes.length; lcv++) {
                if (!GeneralUtilities.safeEquals(myClazzes[lcv], otherClazzes[lcv])) return false;
            }
        }
        else if (other.unqualified != null) return false;

        return true;
    }
    
    /**
     * Used when bulk removing a contract that has
     * been removed from the system
     * 
     * @param name The name of the contract that
     * has been removed from the system
     * @return true if this CacheKey is associated
     * with the name contract, and should thus
     * be removed
     */
    public boolean matchesRemovalName(String name) {
        if (removalName == null) return false;
        if (name == null) return false;
        
        return removalName.equals(name);
    }
    
    public String toString() {
        return "CacheKey(" + Pretty.type(lookupType) + "," + name + "," +
            ((qualifiers == null) ? 0 : qualifiers.length) + "," +
                System.identityHashCode(this) + "," + hashCode + ")";
    }

}
