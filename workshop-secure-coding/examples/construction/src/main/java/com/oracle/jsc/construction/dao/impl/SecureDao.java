package com.oracle.jsc.construction.dao.impl;

import java.io.Closeable;

import com.oracle.jsc.construction.dao.User;
import com.oracle.jsc.construction.dao.UserDao;
import com.oracle.jsc.construction.dao.UserStateException;

public class SecureDao implements UserDao, Closeable {
    protected static final boolean ADMIN = true;
    private boolean initialized = false;

    public SecureDao() {
    }

    public boolean isValid() {
        try {
            return init();
        } catch (UserStateException e) {
            return false;
        }
    }

    private boolean init() throws UserStateException {
        if (!initialized) {
            throw new UserStateException("Unable to connect to the database or find the backup config.");
        }
        initialized = true;
        return initialized;
    }

    public User getUser(String userId) throws UserStateException {
        init();

        // Retrieve by user ID, do whatever checks are needed, return a spiffy immutable user.
        // This process probably throws IOException too.
        //
        return new User(userId, ADMIN);
    }

    @Override
    public void close() {
        // success!
    }

}
