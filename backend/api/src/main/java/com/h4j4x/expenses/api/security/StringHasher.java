package com.h4j4x.expenses.api.security;

import java.security.GeneralSecurityException;

public interface StringHasher {
    /**
     * Generates salt.
     * Salt is random data that are used as an additional input to a one-way function that hashes a password or pass-phrase.
     *
     * @return a randomly generated text.
     */
    String salt();

    /**
     * Hashes a string using given salt.
     *
     * @param string value to be hashed.
     * @param salt   salt to be used.
     * @return hashed string or {@literal null} if string is null.
     * @throws GeneralSecurityException
     */
    String hash(String string, String salt) throws GeneralSecurityException;

    /**
     * Validates if string matches with hash using salt.
     *
     * @param string value to be validated.
     * @param salt   salt to be used.
     * @param hash   hashed value to compare.
     * @return {@literal true} if matches, {@literal false} otherwise.
     * @throws GeneralSecurityException
     */
    boolean match(String string, String salt, String hash) throws GeneralSecurityException;
}
