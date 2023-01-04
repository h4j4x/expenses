package com.h4j4x.expenses.api.security;

import com.h4j4x.expenses.common.security.StringHasher;
import com.h4j4x.expenses.common.security.impl.PBKDF2StringHasher;
import javax.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public class StringHasherProducer {
    @ConfigProperty(name = "app.security.hash-iterations", defaultValue = "512")
    Integer iterations;

    @ConfigProperty(name = "app.security.hash-key-length", defaultValue = "128")
    Integer keyLength;

    @Singleton
    StringHasher stringHasher() {
        return new PBKDF2StringHasher(iterations, keyLength);
    }
}
